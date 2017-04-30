package com.dbs.team9;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportClientFactory {

	private EsClientConfig esClientConfig;

	private TransportClient client;

	public TransportClient createClient() throws EsClientException {

		try {

			if (client != null) {
				return client;
			}
			client = TransportClient.builder()
					.settings(buildSettings(esClientConfig.getPingTimeOut(), esClientConfig.isSniff())).build();

			String[] hosts = StringUtils.split(esClientConfig.getClusterHosts(), ",");

			for (int i = 0; i < hosts.length; i++) {
				client.addTransportAddresses(
						new InetSocketTransportAddress(InetAddress.getByName(hosts[i]), esClientConfig.getPort()));
			}

			return client;

		} catch (ElasticsearchException | UnknownHostException exception) {
			throw new EsClientException("Exception occured while creating transport clinet for ES connection",
					exception);
		}
	}

	private Settings buildSettings(String pingTimeOut, boolean isSniff) {
		return Settings.settingsBuilder().put("client.transport.ignore_cluster_name", true)
				.put("client.transport.ping_timeout", pingTimeOut).put("client.transport.sniff", isSniff).build();
	}

	public EsClientConfig getEsClientConfig() {
		return esClientConfig;
	}

	public void setEsClientConfig(EsClientConfig esClientConfig) {
		this.esClientConfig = esClientConfig;
	}

	public TransportClient getClient() {
		return client;
	}

	public void setClient(TransportClient client) {
		this.client = client;
	}

}
