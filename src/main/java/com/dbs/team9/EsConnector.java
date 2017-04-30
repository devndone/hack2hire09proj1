package com.dbs.team9;

import java.util.List;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

public interface EsConnector {

	public void setEsClientConfig(EsClientConfig esClientConfig);
	
	public void setTransportClient(TransportClient transportClient);
	
  /**
   * @param field name to be used as documentID in elastic search 
   * @param document list of JsonObjects to be added to elastic search.
   * @throws ESClientException
   */
  void insertDocumentsInBulk(String docIdField, List<String> documents) throws EsClientException;

  /**
   * @param field name to be used as documentID in elastic search 
   * @param json document to be added to elastic search.
   * @throws ESClientException
   */
  void insertDocument(String docIdField, String document) throws EsClientException;
  
  /**
   * @param query that is used to filter the documents
   * @throws ESClientException
   */
  List<SearchHit> searchDocuments(QueryBuilder query) throws EsClientException;

  /**
   * @param field name to be used as documentID in elastic search 
   * @param document list of JsonObjects to be updated to elastic search.
   * @throws ESClientException
   */
  void updateDocumentsInBulk(String docIdField, List<String> documents) throws EsClientException;
  
  /**
   * @param list of docId to be deleted
   * @throws ESClientException
   */
  void deleteDocumentsInBulk(QueryBuilder query) throws EsClientException;
}
