package com.dbs.team9;

public class EsClientException extends Exception {

	private static final long serialVersionUID = -2423308749392756001L;

	private String description;

	public EsClientException(final String description) {
		super(description);
		this.description = description;
	}

	public EsClientException(final String description, final Exception e) {
		super(description, e);
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
