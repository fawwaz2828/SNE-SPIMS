package com.school.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database access point for the whole application (Database layer entry).
 *
 * <p>Owns a single HikariCP connection pool built from {@code db.properties} on the
 * classpath. MyBatis borrows connections from this same pool via
 * {@link #getDataSource()} (wired in through {@link HikariDataSourceFactory}); the
 * pool's lifecycle is still owned here.</p>
 */
public final class Db {

    private static volatile HikariDataSource dataSource;

    private Db() {
    }

    /** Lazily builds the pool on first use. */
    private static HikariDataSource dataSource() {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            synchronized (Db.class) {
                ds = dataSource;
                if (ds == null) {
                    ds = build();
                    dataSource = ds;
                }
            }
        }
        return ds;
    }

    private static HikariDataSource build() {
        Properties props = new Properties();
        try (InputStream in = Db.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            props.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load db.properties", e);
        }

        HikariConfig cfg = new HikariConfig();
        // Connection settings prefer environment variables (so secrets need not be
        // committed); db.properties values are the local-dev fallback.
        cfg.setJdbcUrl(env("DB_URL", props.getProperty("db.url")));
        cfg.setUsername(env("DB_USERNAME", props.getProperty("db.username")));
        cfg.setPassword(env("DB_PASSWORD", props.getProperty("db.password")));
        cfg.setDriverClassName(props.getProperty("db.driver"));
        cfg.setMaximumPoolSize(intProp(props, "db.pool.maximumPoolSize", 10));
        cfg.setMinimumIdle(intProp(props, "db.pool.minimumIdle", 2));
        cfg.setConnectionTimeout(intProp(props, "db.pool.connectionTimeout", 30000));
        cfg.setIdleTimeout(intProp(props, "db.pool.idleTimeout", 600000));
        cfg.setPoolName("sne-spims-pool");
        return new HikariDataSource(cfg);
    }

    private static int intProp(Properties p, String key, int def) {
        String v = p.getProperty(key);
        return v == null ? def : Integer.parseInt(v.trim());
    }

    /** Environment variable {@code name} if set (non-blank), otherwise {@code fallback}. */
    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    /** The shared connection pool — handed to MyBatis as its {@link DataSource}. */
    public static DataSource getDataSource() {
        return dataSource();
    }

    /** Borrow a pooled connection. Callers must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return dataSource().getConnection();
    }

    /** Closes the pool. Invoked by the context listener on shutdown. */
    public static synchronized void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
