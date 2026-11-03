package com.school.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thin MyBatis convenience layer shared by every DAO implementation, the
 * counterpart of the old {@code Jdbc} helper. It owns the lazily-built
 * {@link SqlSessionFactory} and opens a fresh {@link SqlSession} per call,
 * committing writes — matching the previous "one connection per operation,
 * auto-commit" semantics.
 *
 * <p>SQL and result mapping live in the XML mapper files under
 * {@code resources/mappers}; statements are addressed by their fully-qualified
 * {@code namespace.id}.</p>
 *
 * <p><b>Transactions.</b> By default each call is its own committed unit. Wrap a
 * sequence of writes in {@link #tx(Runnable)} to make them atomic: a single
 * {@link SqlSession} is bound to the current thread for the duration, every
 * nested DAO call joins it (no intermediate commit), and the whole block commits
 * once at the end — or rolls back entirely if any exception escapes.</p>
 */
public final class MyBatis {

    private static volatile SqlSessionFactory factory;

    /** The transaction-scoped session for the current thread, if {@link #tx} is active. */
    private static final ThreadLocal<SqlSession> CURRENT_TX = new ThreadLocal<>();

    private MyBatis() {
    }

    private static SqlSessionFactory factory() {
        SqlSessionFactory f = factory;
        if (f == null) {
            synchronized (MyBatis.class) {
                f = factory;
                if (f == null) {
                    f = build();
                    factory = f;
                }
            }
        }
        return f;
    }

    private static SqlSessionFactory build() {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder().build(in);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build MyBatis SqlSessionFactory", e);
        }
    }

    /**
     * Runs {@code body} as a single atomic transaction: all writes inside commit
     * together, or roll back together if a {@link RuntimeException} escapes. Calls
     * nest safely — an inner {@code tx} joins the outer one rather than opening a
     * second transaction, so a service method wrapped in {@code tx} stays atomic
     * even when invoked from another {@code tx}.
     */
    public static <T> T tx(Supplier<T> body) {
        if (CURRENT_TX.get() != null) {
            return body.get(); // already inside a transaction — join it
        }
        SqlSession session = factory().openSession(false); // explicit commit boundary
        CURRENT_TX.set(session);
        try {
            T result = body.get();
            session.commit();
            return result;
        } catch (RuntimeException e) {
            session.rollback();
            throw e;
        } finally {
            CURRENT_TX.remove();
            session.close();
        }
    }

    /** Void overload of {@link #tx(Supplier)}. */
    public static void tx(Runnable body) {
        tx(() -> {
            body.run();
            return null;
        });
    }

    /** Runs a query and returns every mapped row. */
    public static <T> List<T> selectList(String statement) {
        return read(statement, s -> s.selectList(statement));
    }

    /** Runs a parameterised query and returns every mapped row. */
    public static <T> List<T> selectList(String statement, Object param) {
        return read(statement, s -> s.selectList(statement, param));
    }

    /** Runs a query expected to return at most one row. */
    public static <T> Optional<T> selectOne(String statement, Object param) {
        return read(statement, s -> Optional.ofNullable(s.selectOne(statement, param)));
    }

    /** Convenience for a scalar count / aggregate query returning a single long. */
    public static long count(String statement, Object param) {
        Optional<Long> n = selectOne(statement, param);
        return n.orElse(0L);
    }

    /**
     * Executes an INSERT. Statements declaring {@code useGeneratedKeys} populate
     * the key back onto {@code param} (a model object's {@code id}, or an
     * {@code "id"} entry on a Map). Committed immediately unless running inside
     * {@link #tx}, in which case it commits with the surrounding transaction.
     */
    public static void insert(String statement, Object param) {
        write(statement, s -> {
            s.insert(statement, param);
            return null;
        });
    }

    /**
     * Executes an UPDATE/DELETE and returns the affected row count. Committed
     * immediately unless running inside {@link #tx}.
     */
    public static int update(String statement, Object param) {
        return write(statement, s -> s.update(statement, param));
    }

    // --- internals ---------------------------------------------------------

    private interface SessionFn<T> {
        T apply(SqlSession session);
    }

    /** Read path: join the thread's transaction if any, else a throwaway session. */
    private static <T> T read(String statement, SessionFn<T> fn) {
        SqlSession tx = CURRENT_TX.get();
        if (tx != null) {
            try {
                return fn.apply(tx);
            } catch (RuntimeException e) {
                throw wrap(statement, e);
            }
        }
        try (SqlSession s = factory().openSession()) {
            return fn.apply(s);
        } catch (RuntimeException e) {
            throw wrap(statement, e);
        }
    }

    /**
     * Write path: inside a transaction the work joins it and the commit is
     * deferred to {@link #tx}; otherwise it commits immediately (legacy
     * one-operation-per-commit behaviour).
     */
    private static <T> T write(String statement, SessionFn<T> fn) {
        SqlSession tx = CURRENT_TX.get();
        if (tx != null) {
            try {
                return fn.apply(tx); // commit handled by the enclosing tx()
            } catch (RuntimeException e) {
                throw wrap(statement, e);
            }
        }
        try (SqlSession s = factory().openSession()) {
            T result = fn.apply(s);
            s.commit();
            return result;
        } catch (RuntimeException e) {
            throw wrap(statement, e);
        }
    }

    /** Builds a parameter map for statements that take more than one argument. */
    public static Map<String, Object> args(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static DaoException wrap(String statement, RuntimeException e) {
        if (e instanceof DaoException) {
            return (DaoException) e;
        }
        return new DaoException("MyBatis statement failed: " + statement, e);
    }
}