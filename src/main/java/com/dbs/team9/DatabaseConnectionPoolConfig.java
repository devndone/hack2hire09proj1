package com.dbs.team9;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConnectionPoolConfig {

	@Value("${" + CommonConstants.DATABASE_CONNECTION_DRIVER_CLASS_NAME + "}")
	private String driverClassName;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_URL + "}")
	private String url;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_USER_NAME + "}")
	private String userName;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_PASSWORD + "}")
	private String password;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_INITIAL_SIZE + "}")
	private int initialSize;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_MAX_ACTIVE + "}")
	private int maxActive;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_MAX_IDLE + "}")
	private int maxIdle;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_MIN_IDLE + "}")
	private int minIdle;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_MAX_WAIT + "}")
	private int maxWait;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_TEST_WHILE_IDLE + "}")
	private boolean testWhileIdle;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_VALIDATION_QUERY + "}")
	private String validationQuery;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_TIME_BETWEEN_EVICTION_RUNS_MILLIS + "}")
	private int timeBetweenEvictionRunsMillis;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_MINE_EVICTABLE_IDLE_TIME_MILLIS + "}")
	private int minEvictableIdleTimeMillis;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_REMOVE_ABONDONED + "}")
	private boolean removeAbandoned;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_REMOVE_ABONDONED_TIMEOUT + "}")
	private int removeAbandonedTimeout;
	@Value("${" + CommonConstants.DATABASE_CONNECTION_LOG_ABONDONED + "}")
	private boolean logAbandoned;

	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		DataSource dataSource = new DataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(userName);
		dataSource.setPassword(password);
		dataSource.setInitialSize(initialSize);
		dataSource.setMaxActive(maxActive);
		dataSource.setMaxIdle(maxIdle);
		dataSource.setMinIdle(minIdle);
		dataSource.setMaxWait(maxWait);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setRemoveAbandoned(removeAbandoned);
		dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		dataSource.setLogAbandoned(logAbandoned);
		return dataSource;
	}

}
