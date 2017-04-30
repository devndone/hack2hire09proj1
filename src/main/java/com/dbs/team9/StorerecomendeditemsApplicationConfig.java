package com.dbs.team9;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@Import({ DatabaseConnectionPoolConfig.class
		, JdbcTemplateConfig.class
		, ElasticSearchConfig.class })
/* 
 * Property Source are order dependent so last property file will override if any duplicate keys 
 * from previous property files
 */
@PropertySources({
	@PropertySource(value = "classpath:elasticsearch.properties" )
	, @PropertySource(value = "classpath:database.connection.properties")
})
@ComponentScan
public class StorerecomendeditemsApplicationConfig {

	public StorerecomendeditemsApplicationConfig() {
		
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
