package com.dbs.team9;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class EsConnectorImpl implements EsConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(EsConnectorImpl.class);

	private TransportClient transportClient;

	private EsClientConfig esClientConfig;

	private Integer bulkLimit;

	private String index;

	private String type;

	private String currentdayIndex;

	private Map<String, Boolean> alertIndexCache = new ConcurrentHashMap<>();

	private Gson gson = new Gson();

	private JsonObject jsonObject;

	private String mapping;

	@Override
	public void insertDocument(String id, String document) throws EsClientException {

		LOGGER.trace("Inserting document into elastic search in index: {}, type :{}", index, type);

		String docId = null;

		if (StringUtils.isEmpty(id)) {
			LOGGER.error("failed to add documents to ES as document field id is null or empty: {}", id);
			throw new EsClientException("documentId field cannot be null or empty");
		}

		currentdayIndex = (esClientConfig.isDailyIndexCreation()) ? getCurrentDayIndex() : getIndex();

		IndexResponse indexResponse = null;

		try {
			jsonObject = gson.fromJson(document, JsonObject.class);

			docId = jsonObject.get(id).getAsString();
			if (!StringUtils.isEmpty(docId)) {
				indexResponse = transportClient.prepareIndex(currentdayIndex, type, docId).setSource(document).execute()
						.actionGet();
			}
		} catch (JsonSyntaxException exception) {
			LOGGER.error("Invalid Json document {}. Failed to insert into elastic search", document);
			throw new EsClientException("Exception occured while indexing document, invalid json document passed",
					exception);
		} catch (Exception ex) {
			LOGGER.error("Document:{} failed to get indexed into ES with exception {}", document, ex.getMessage());
			throw new EsClientException("Exception occured while indexing document", ex);
		}

		if (indexResponse == null || !indexResponse.isCreated()) {
			LOGGER.error("Document:{} failed to get indexed into ES", document);
			throw new EsClientException("Failed to index document");
		}

		LOGGER.info("Document of id {} index created={} in index:{}", docId, indexResponse.isCreated(),
				currentdayIndex);

	}

	@Override
	public void insertDocumentsInBulk(String id, List<String> documents) throws EsClientException {

		LOGGER.info("Inserting {} documents in bulk into elastic search", documents.size());

		String docId = null;

		if (StringUtils.isEmpty(id)) {
			LOGGER.error("failed to add documents to ES as document id is null or empty: {}", id);
			throw new EsClientException("documentId field cannot be null or empty");
		}

		currentdayIndex = (esClientConfig.isDailyIndexCreation()) ? getCurrentDayIndex() : getIndex();

		LOGGER.info("Resolved current day index:{}, type:{}", currentdayIndex, type);

		BulkProcessor bulkProcessor = BulkProcessor.builder(transportClient, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {

			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

				if (response.hasFailures()) {
					LOGGER.error("Bulk insert into elastic search has few failures");
				} else {
					LOGGER.info("Successfully inserted documents into elastic search index:{}", currentdayIndex);
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				failure.printStackTrace();
			}
		}).setBulkActions(bulkLimit).setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)).setConcurrentRequests(1)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(5000), 3)).build();

		for (String document : documents) {
			try {
				jsonObject = gson.fromJson(document, JsonObject.class);

				docId = jsonObject.get(id).getAsString();

				if (!StringUtils.isEmpty(docId)) {
					bulkProcessor.add(new IndexRequest(currentdayIndex, type, docId).source(document.toString()));
				}
			} catch (JsonSyntaxException exception) {
				LOGGER.error("Invalid Json document {}. Failed to insert into elastic search", document);
			}

		}

		try {
			bulkProcessor.close();
		} catch (Exception exception) {
			LOGGER.error("Exception occured while inserting documents into Elastic search {}", exception.getMessage());
			throw new EsClientException("Exception occured while inserting documents into Elastic search", exception);
		}

	}

	@Override
	public List<SearchHit> searchDocuments(QueryBuilder query) throws EsClientException {

		LOGGER.info("Searching documents in index:{}, type:{}", index, type);

		if (query == null) {
			throw new EsClientException("Null value is passed as query");
		}

		SearchResponse response = null;
		List<SearchHit> totalSearchHits = new ArrayList<SearchHit>();

		long start = System.currentTimeMillis();

		try {
			response = transportClient.prepareSearch().setIndices(index).setTypes(type).setQuery(query)
					.setSize(bulkLimit).addSort("_doc", SortOrder.ASC).setScroll(new TimeValue(60000)).execute()
					.actionGet();

			while (true) {
				Collections.addAll(totalSearchHits, response.getHits().getHits());
				response = transportClient.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();

				// Break condition: No hits are returned
				if (response.getHits().getHits().length == 0) {
					break;
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception occured while searching documents in ES {}", e.getMessage());
			throw new EsClientException("Error occured while fetching documents from ES", e);
		}

		LOGGER.info("Fetched {} documents in {} ms", totalSearchHits.size(), System.currentTimeMillis() - start);
		return totalSearchHits;
	}

	@Override
	public void updateDocumentsInBulk(String id, List<String> documents) throws EsClientException {

		LOGGER.info("Updating {} documents in bulk into elastic search index:{}", documents.size(), index);

		String docId = null;

		if (StringUtils.isEmpty(id)) {
			LOGGER.error("failed to update documents to ES as document id is null or empty: {}", id);
			throw new EsClientException("documentId field cannot be null or empty");
		}

		BulkProcessor bulkProcessor = BulkProcessor.builder(transportClient, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {

			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

				if (response.hasFailures()) {
					LOGGER.error("Bulk update into elastic search has few failures");
				} else {
					LOGGER.info("Successfully updated documents into elastic search index:{}", index);
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				failure.printStackTrace();
			}
		}).setBulkActions(bulkLimit).setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)).setConcurrentRequests(1)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(5000), 3)).build();

		for (String document : documents) {
			try {

				jsonObject = gson.fromJson(document, JsonObject.class);

				docId = jsonObject.get(id).getAsString();

				if (!StringUtils.isEmpty(docId)) {
					bulkProcessor.add(new UpdateRequest(index, type, docId).doc(document));
				}
			} catch (JsonSyntaxException exception) {
				LOGGER.error("Invalid Json document {}. Failed to insert into elastic search", document);
			}
		}

		try {
			bulkProcessor.close();
		} catch (Exception exception) {
			LOGGER.error("Exception occured while updating documents into Elastic search {}", exception.getMessage());
			throw new EsClientException("Exception occured while updating documents into Elastic search", exception);
		}
	}

	@Override
	public void deleteDocumentsInBulk(QueryBuilder query) throws EsClientException {

		LOGGER.info("Deleting documents from elastic search index:{}, type :{}", index, type);

		List<SearchHit> matchedDocuments = searchDocuments(query);

		BulkProcessor bulkProcessor = BulkProcessor.builder(transportClient, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {

			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

				if (response.hasFailures()) {
					LOGGER.error("Bulk delete has few failures");
				} else {
					LOGGER.info("Successfully deleted documents from elastic search index:{}", index);
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				failure.printStackTrace();
			}
		}).setBulkActions(bulkLimit).setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)).setConcurrentRequests(1)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(5000), 3)).build();

		if (!matchedDocuments.isEmpty()) {
			for (SearchHit hit : matchedDocuments) {
				bulkProcessor.add(new DeleteRequest(index, type, hit.getId()));
			}
		}

		try {
			bulkProcessor.close();
		} catch (Exception exception) {
			LOGGER.error("Exception occured while deleting documents from Elastic search {}", exception.getMessage());
			throw new EsClientException("Exception occured while deleting documents from Elastic search", exception);
		}
	}

	@PostConstruct
	private void populateESConfig() throws EsClientException {
		index = esClientConfig.getIndex();
		type = esClientConfig.getType();
		bulkLimit = esClientConfig.getBulkLimit();

		if (StringUtils.isEmpty(index) || StringUtils.isEmpty(type)) {
			LOGGER.error("ES Index or type is set to null");
			throw new EsClientException("Index name or index type is invalid");
		}
		populateIndexCache();
	}

	private void populateIndexCache() {
		try {
			String[] indexs = transportClient.admin().indices().getIndex(new GetIndexRequest()).actionGet()
					.getIndices();
			for (String index : indexs) {
				if (index.startsWith(this.index)) {
					alertIndexCache.put(index, true);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while constructing alert index cache {} ", e.getMessage());
		}
		LOGGER.info("Index cache was sucessfully created");
	}

	private String getCurrentDayIndex() throws EsClientException {

		String dateTime = DateConversionUtil.getCurrentDateInGMTForDB().replaceAll(" .*", "");

		String dateAppendedIndexName = index + "-" + dateTime;

		boolean status = false;

		if (alertIndexCache.containsKey(dateAppendedIndexName)) {
			return dateAppendedIndexName;
		} else {
			try {
				status = createIndex(dateAppendedIndexName);
			} catch (IndexAlreadyExistsException ex) {
				status = true;
			} catch (Exception e) {
				LOGGER.error("Error while creating index {}", e.getMessage());
				throw new EsClientException("Error while creating index", e);
			}
			if (status) {
				alertIndexCache.put(dateAppendedIndexName, true);
				return dateAppendedIndexName;
			}
		}
		return null;
	}

	private String getIndex() throws EsClientException {

		boolean status = false;

		if (alertIndexCache.containsKey(index)) {
			return index;
		}
		try {
			status = createIndex(index);
		} catch (IndexAlreadyExistsException ex) {
			status = true;
		} catch (Exception e) {
			LOGGER.error("Error while creating index {}", e.getMessage());
			throw new EsClientException("Error while creating index", e);
		}
		if (status) {
			alertIndexCache.put(index, true);
			return index;
		}

		return null;
	}

	private boolean createIndex(String indexName) throws EsClientException {

		try {
			mapping = new String(
					Files.readAllBytes(Paths.get(getClass().getResource(esClientConfig.getMappingFile()).toURI())));
		} catch (Exception e) {
			LOGGER.error("Exception while reading mapping file {}", e.getMessage());
			throw new EsClientException("Exception while reading mapping file", e);
		}

		IndicesAdminClient indicesAdminClient = transportClient.admin().indices();
		CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(indexName).addMapping(type, mapping)
				.get();
		return createIndexResponse.isAcknowledged();
	}

	public TransportClient getTransportClient() {
		return transportClient;
	}

	public void setTransportClient(TransportClient transportClient) {
		this.transportClient = transportClient;
	}

	public EsClientConfig getEsClientConfig() {
		return esClientConfig;
	}

	public void setEsClientConfig(EsClientConfig esClientConfig) {
		this.esClientConfig = esClientConfig;
	}

}
