package com.googlecode.jmxtrans.model.output;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.util.BaseOutputWriter;
import com.googlecode.jmxtrans.util.LifecycleException;
import com.googlecode.jmxtrans.util.ValidationException;

import static org.elasticsearch.node.NodeBuilder.*;

/**
 * Write results to ElasticSearch using the embedded node client
 * 
 * The index name and type name for the ElasticSearch storage can be configured
 * as follows: - typeName : String, default = measurement - indexName : String,
 * default = jmxtrans
 * 
 * Configuration is identical to ElasticSearch's API for NodeBuilder options
 * (all are optional): - clusterName : String - name of ElasticSearch cluster to
 * join - data : boolean - true to store data inside embedded node, default
 * false - local : boolean - discover only in local JVM, default false - client
 * : boolean - true to act as client, default true
 * 
 * @author Shanny Anoep
 */
public class ElasticSearchWriter extends BaseOutputWriter {

	public static final Logger LOG = LoggerFactory.getLogger(ElasticSearchWriter.class);

	private String typeName = null;
	private String indexName = null;

	private Node node = null;
	private Client nodeClient = null;

	public static final String ELASTIC_TYPE_NAME = "elasticTypeName";
	public static final String DEFAULT_ELASTIC_TYPE_NAME = "measurement";

	public static final String ELASTIC_INDEX_NAME = "elasticIndexName";
	public static final String DEFAULT_ELASTIC_INDEX_NAME = "jmxtrans";

	public static final String CLUSTER_NAME = "clusterName";
	public static final String DEFAULT_CLUSTER_NAME = null;

	public static final String ELASTICCONFIG_CLIENT = "client";
	public static final String ELASTICCONFIG_DATA = "data";
	public static final String ELASTICCONFIG_LOCAL = "local";

	public ElasticSearchWriter() {
	}

	@Override
	public void start() throws LifecycleException {
		super.start();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Initializing ElasticSearch client");		
			LOG.debug("Settings: " + getSettings());
		}

		typeName = getStringSetting(ELASTIC_TYPE_NAME, DEFAULT_ELASTIC_TYPE_NAME);
		indexName = getStringSetting(ELASTIC_INDEX_NAME, DEFAULT_ELASTIC_INDEX_NAME);
		String clusterName = getStringSetting(CLUSTER_NAME, DEFAULT_CLUSTER_NAME);

		boolean client = getBooleanSetting(ELASTICCONFIG_CLIENT, true);
		boolean data = getBooleanSetting(ELASTICCONFIG_DATA, false);
		boolean local = getBooleanSetting(ELASTICCONFIG_LOCAL, false);

		if (LOG.isDebugEnabled()) {
			LOG.debug("ElasticSearch configuration:" + " clusterName: [" + clusterName + "]" + " client: [" + client + "]" + " data: [" + data + "]"
					+ " local: [" + local + "] " + " elasticIndexName: [" + indexName + "]" + " elasticTypeName: [" + typeName + "]");
		}

		NodeBuilder nodeBuilder = nodeBuilder();

		if (clusterName != null) {
			nodeBuilder.clusterName(clusterName);
		}

		node = nodeBuilder.client(client).data(data).local(local).node();
		nodeClient = node.client();

		LOG.debug("Finished initializing ElasticSearch client");
	}

	@Override
	public void stop() throws LifecycleException {
		super.stop();
		LOG.debug("Closing ElasticSearch client");

		node.close();
	}

	public void validateSetup(Query query) throws ValidationException {
		LOG.debug("Validating setup of ElasticSearch client");

		if (node == null) {
			throw new ValidationException("ElasticSearch not correctly initialized: node == null", null);
		} else if (nodeClient == null) {
			throw new ValidationException("ElasticSearch not correctly initialized: client == null", null);
		}
	}

	public void doWrite(Query query) throws Exception {
		for (Result r : query.getResults()) {
			String jsonString = converToJsonString(r);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Calling prepareIndex with json: " + jsonString);
			}
			
			IndexResponse response = nodeClient.prepareIndex(indexName, typeName).setSource(jsonString).execute().actionGet();

			if (LOG.isDebugEnabled() && response != null) {
				LOG.debug("Response index: [" + response.getIndex() + "]");
				LOG.debug("Response type: [" + response.getType() + "]");
				LOG.debug("Response id: [" + response.getId() + "]");
			}
		}
	}

	public static String converToJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().set(Feature.WRITE_NULL_MAP_VALUES, false);
		return mapper.writeValueAsString(object);
	}

	public String getTypeName() {
		return typeName;
	}

	public String getIndexName() {
		return indexName;
	}

	protected Client getNodeClient() {
		return nodeClient;
	}

	protected Node getNode() {
		return node;
	}

}
