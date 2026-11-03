package com.school.util;

import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Bridges MyBatis to the application's existing HikariCP pool. Referenced from
 * {@code mybatis-config.xml} as the {@code <dataSource>} type so MyBatis borrows
 * connections from {@link Db} instead of opening its own.
 *
 * <p>All pool configuration lives in {@code db.properties} and is owned by
 * {@link Db}; the {@link Properties} MyBatis passes here are intentionally ignored.</p>
 */
public class HikariDataSourceFactory implements DataSourceFactory {

    @Override
    public void setProperties(Properties props) {
        // No-op: the pool is configured by Db from db.properties.
    }

    @Override
    public DataSource getDataSource() {
        return Db.getDataSource();
    }
}