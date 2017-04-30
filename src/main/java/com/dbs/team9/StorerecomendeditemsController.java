package com.dbs.team9;

import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RestController
public class StorerecomendeditemsController {

	@RequestMapping(value="/test/{userid}/{longitude}/{latitude}")
	public String storeRecommend(@PathVariable("userid") String namespace, 
            @PathVariable("longitude") String set,
            @PathVariable("latitude") String keyvalue) throws EsClientException {
		String res = "recommendation";
		EsConnector esConnector = getEsConnector();
		String alertIdQuery = new StringBuilder("userid").append(CommonConstants.UNDERSCORE).append("longitude")
				.append(CommonConstants.UNDERSCORE).append("latitude").toString();
		QueryBuilder queryBuilder = QueryBuilders.matchQuery("recommendationId", alertIdQuery);
		List<SearchHit> esResponse = esConnector.searchDocuments(queryBuilder);
		if (esResponse != null && !esResponse.isEmpty()) {
			res = new Gson().toJson(esResponse);
		}
		return res;
	}

	private EsConnector getEsConnector() throws EsClientException {
		EsClientConfig esClientConfig = new EsClientConfig();
		EsConnector esConnector = new EsConnectorImpl();
		esConnector.setEsClientConfig(esClientConfig);
		TransportClientFactory tcf = new TransportClientFactory();
		tcf.setEsClientConfig(esClientConfig);
		esConnector.setTransportClient(tcf.createClient());
		return esConnector;
	}

}
