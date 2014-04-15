package com.googlecode.jmxtrans.model.output;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.util.BaseOutputWriter;
import com.googlecode.jmxtrans.util.LifecycleException;
import com.googlecode.jmxtrans.util.ValidationException;

import static org.elasticsearch.node.NodeBuilder.*;

/**
 * Write results to ElasticSearch
 * 
 * @author Shanny Anoep
 */
public class ElasticSearchWriter extends BaseOutputWriter {

	private static final Logger log = LoggerFactory.getLogger(ElasticSearchWriter.class);

	private static final String TYPE_NAME = "measurement";
	private static final String INDEX_NAME = "jmxtrans";
	private Node node = null;
	private Client client = null;

	@Override
	public void start() throws LifecycleException {
		super.start();
		log.debug("Initializing ElasticSearch client");
		node = nodeBuilder().client(true).node();
		client = node.client();

		log.debug("Finished initializing ElasticSearch client");
	}

	@Override
	public void stop() throws LifecycleException {
		super.stop();
		log.debug("Closing ElasticSearch client");

		node.close();
	}

	public ElasticSearchWriter() {
	}

	public void validateSetup(Query query) throws ValidationException {
		log.debug("Validating setup of ElasticSearch client");
		if (node == null) {
			throw new ValidationException("ElasticSearch not correctly initialized: node == null", null);
		} else if (client == null) {
			throw new ValidationException("ElasticSearch not correctly initialized: client == null", null);
		}
	}

	public void doWrite(Query query) throws Exception {
		for (Result r : query.getResults()) {
			log.debug("Result: " + r);

			String jsonString = converToJsonString(r);
			log.debug("Calling prepareIndex with json: " + jsonString);
			IndexResponse response = client.prepareIndex(INDEX_NAME, TYPE_NAME).setSource(jsonString).execute().actionGet();
			
			if (log.isDebugEnabled() && response != null) {
				log.debug("Response index: " + response.getIndex());
				log.debug("Response type: " + response.getType());
				log.debug("Response id: " + response.getId());
			}
		}
	}

	public static String converToJsonString(Object object) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().set(Feature.WRITE_NULL_MAP_VALUES, false);
		return mapper.writeValueAsString(object);
	}

}
