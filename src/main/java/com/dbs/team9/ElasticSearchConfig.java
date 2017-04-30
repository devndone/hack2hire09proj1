package com.dbs.team9;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

	@Value("${" + CommonConstants.ES_BULK_LIMIT + "}")
	private int esBulkLimit;
	@Value("${" + CommonConstants.ES_CLUSTER_HOST + "}")
	private String esClusterHost;
	@Value("${" + CommonConstants.ES_CLUSTER_PORT + "}")
	private int esClusterPort;
	@Value("${" + CommonConstants.ES_INDEX_NAME + "}")
	private String esIndexName;
	@Value("${" + CommonConstants.ES_INDEX_TYPE + "}")
	private String esIndexType;

	@Bean(name = "esClientConfig")
	public EsClientConfig getESClientConfig() {
		EsClientConfig esClientConfig = new EsClientConfig();
		esClientConfig.setBulkLimit(esBulkLimit);
		esClientConfig.setClusterHosts(esClusterHost);
		esClientConfig.setPort(esClusterPort);
		esClientConfig.setIndex(esIndexName);
		esClientConfig.setType(esIndexType);
		esClientConfig.setMappingFile("/esMapping.json");
		return esClientConfig;
	}
}
