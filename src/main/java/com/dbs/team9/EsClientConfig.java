package com.dbs.team9;

import org.apache.commons.lang3.StringUtils;

public class EsClientConfig {

	public static final int DEFAULT_PORT = 9300;

	public static final String DEFAULT_PING_TIMEOUT = "15s";

	public static final int DEFAULT_BULK_LIMIT = 1000;

	private String index;

	private String type;

	private String clusterHosts = null;

	private Integer port = 9300;

	private Integer bulkLimit = 1000;

	private String pingTimeOut = "15s";

	private boolean isSniff = true;

	private String mappingFile;

	private boolean dailyIndexCreation = true;

	/**
	 * @return the clusterHosts
	 */
	public String getClusterHosts() {
		return clusterHosts;
	}

	/**
	 * @param clusterHosts
	 *            the clusterHosts to set
	 */
	public void setClusterHosts(String clusterHosts) {
		this.clusterHosts = clusterHosts;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(Integer port) {
		if (port <= 0) {
			this.port = DEFAULT_PORT;
		} else {
			this.port = port;
		}

	}

	/**
	 * @return the pingTimeOut
	 */
	public String getPingTimeOut() {
		return pingTimeOut;
	}

	/**
	 * @param pingTimeOut
	 *            the pingTimeOut to set
	 */
	public void setPingTimeOut(String pingTimeOut) {
		if (StringUtils.isBlank(pingTimeOut)) {
			pingTimeOut = DEFAULT_PING_TIMEOUT;
		} else {
			this.pingTimeOut = pingTimeOut;
		}
	}

	/**
	 * @return the isSniff
	 */
	public boolean isSniff() {
		return isSniff;
	}

	/**
	 * @param isSniff
	 *            the isSniff to set
	 */
	public void setSniff(boolean isSniff) {
		this.isSniff = isSniff;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getBulkLimit() {
		return bulkLimit;
	}

	public void setBulkLimit(Integer bulkLimit) {
		if (bulkLimit < 0) {
			this.bulkLimit = DEFAULT_BULK_LIMIT;
		} else {
			this.bulkLimit = bulkLimit;
		}
	}

	public String getMappingFile() {
		return mappingFile;
	}

	public void setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
	}

	public void setDailyIndexCreation(boolean dailyIndexCreation) {
		this.dailyIndexCreation = dailyIndexCreation;
	}

	public boolean isDailyIndexCreation() {
		return dailyIndexCreation;
	}

}
