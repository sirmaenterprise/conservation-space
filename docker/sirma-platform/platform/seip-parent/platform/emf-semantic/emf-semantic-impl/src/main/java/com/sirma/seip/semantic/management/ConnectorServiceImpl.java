package com.sirma.seip.semantic.management;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;
import static com.sirma.itt.semantic.model.vocabulary.Connectors.PREFIX;
import static com.sirma.itt.semantic.model.vocabulary.Connectors.RECREATED_ON;
import static com.sirma.seip.semantic.management.ConnectorQueryGenerator.QUERY_CONNECTOR_FIELDS;
import static com.sirma.seip.semantic.management.ConnectorQueryGenerator.QUERY_CONNECTOR_INSTANCES;
import static com.sirma.seip.semantic.management.ConnectorQueryGenerator.QUERY_CONNECTOR_INSTANCE_PROPERTIES;
import static com.sirma.seip.semantic.management.ConnectorQueryGenerator.QUERY_DEFAULT_CONNECTOR_FIELDS;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.model.Rdf4JUriProxy;
import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.rest.utils.HttpClientUtil;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.Connectors;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.seip.semantic.management.ConnectorConfiguration.ConnectorField;

/**
 * Connector service implementation
 * 
 * @author kirq4e
 */
@ApplicationScoped
@Transactional
public class ConnectorServiceImpl implements ConnectorService {

	private static final String SOLR_CORE_STATUS_SERVICE_ADDRESS = "/admin/cores?action=STATUS&core=";

	private static final String INPUT_PARAMETER_MUST_NOT_BE_NULL_MESSAGE = "Input parameter: %s must not be null";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String DEFAULT_CONNECTOR_URI = PREFIX + URI_SEPARATOR
			+ Connectors.DEFAULT_CONNECTOR.getLocalName();

	@Inject
	private RepositoryConnection connection;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private TransactionSupport transactionSupport;

	@SuppressWarnings("boxing")
	@Override
	public ConnectorConfiguration createConnector(ConnectorConfiguration configuration) {
		Objects.requireNonNull(configuration, String.format(INPUT_PARAMETER_MUST_NOT_BE_NULL_MESSAGE, "configuration"));
		Objects.requireNonNull(configuration.getAddress(),
				String.format(INPUT_PARAMETER_MUST_NOT_BE_NULL_MESSAGE, "Solr address"));

		TimeTracker createConnectorOperationTracker = TimeTracker.createAndStart();
		
		// initialize connector name if not set
		configuration.addIfNotPresent(ConnectorConfiguration.CONNECTOR_NAME,
				createConnectorName(securityContext.getCurrentTenantId()));
		
		LOGGER.info("Creating Solr connector with name: {}", configuration.getConnectorName());

		// check if the core exists in the solr server. If it exists then the connector is not deleted or the core is
		// duplicated and we musn't try to create the connector or else the core will be corrupted
		if (isConnectorPresent(configuration.getConnectorName())
				|| isCorePresent(configuration.getAddress(), configuration.getConnectorName())) {
			throw new SemanticPersistenceException("Cannot create Solr Connector while the Connector or Solr core "
					+ configuration.getConnectorName() + " still exists!");
		}

		configuration.add(PREFIX + URI_SEPARATOR + RECREATED_ON.getLocalName(), new Date());
		configuration.setRecreate(false);
		saveConnectorConfiguration(configuration);

		TimeTracker executeUpdateTracker = TimeTracker.createAndStart();
		transactionSupport.invokeInNewTx(
				() -> executeUpdate(connection, ConnectorQueryGenerator.generateCreateQuery(configuration)), 25,
				TimeUnit.MINUTES);
		LOGGER.debug("Execution of create solr connector query took: {}", executeUpdateTracker.stopInSeconds());

		initSolrScheme(configuration);

		LOGGER.debug("Creation of Solr connector instance: {} took: {}", configuration.getConnectorName(),
				createConnectorOperationTracker.stopInSeconds());
		return configuration;
	}

	/**
	 * Checks if the Core exists in the Solr. It checks the status of the core by sending HTTP request to the Solr
	 * server. If the response contains information abou the core then the method returns true
	 * 
	 * @param address
	 *            Solr server address
	 * @param connectorName
	 *            Name of the connector
	 * @return true if the Solr Core exists in the server or else false
	 */
	private static boolean isCorePresent(String address, String connectorName) {
		String solrAddress = address + SOLR_CORE_STATUS_SERVICE_ADDRESS + connectorName;
		FileDescriptor fileDescriptor = HttpClientUtil.callRemoteService(new HttpGet(solrAddress));
		try {
			String response = fileDescriptor.asString();
			if (response.contains("<str name=\"name\">" + connectorName + "</str>")) {
				return true;
			}
		} catch (IOException e) {
			LOGGER.debug("Error while checking if Solr core exists: {}", e.getMessage());
		}
		return false;
	}

	@SuppressWarnings("boxing")
	@Override
	public boolean deleteConnector(String connectorName) {
		LOGGER.info("Deleting Solr connector with name: {}", connectorName);
		TimeTracker tracker = TimeTracker.createAndStart();
		try {
			if (loadConnectorConfiguration(connectorName) != null) {
				// delete connector and connector configuration
				deleteConnectorInternal(connectorName, true);
				return true;
			}
			return false;
		} finally {
			LOGGER.debug("Deletion of Solr connector instance: {} took: {}", connectorName, tracker.stopInSeconds());
		}
	}

	@SuppressWarnings("boxing")
	@Override
	public void resetConnector(String connectorName) {
		Objects.requireNonNull(connectorName);
		
		LOGGER.info("Reseting Solr connector with name: {}", connectorName);

		TimeTracker tracker = TimeTracker.createAndStart();

		ConnectorConfiguration connectorConfiguration = loadConnectorConfiguration(connectorName);
		String name = connectorConfiguration.getConnectorName();
		if (isConnectorPresent(name)) {
			// delete connector only
			transactionSupport.invokeInNewTx(() -> deleteConnectorInternal(name, false));
		}
		createConnector(connectorConfiguration);

		LOGGER.debug("Reset of Solr connector instance: {} took: {}", connectorName, tracker.stopInSeconds());
	}

	/**
	 * Deletes the connector from Solr and its configuration from the repository if needed
	 * 
	 * @param connectorName
	 *            Name of the connector
	 * @param deleteConfiguration
	 *            Flag that allows the deletion of the configuration from the repository
	 */
	private void deleteConnectorInternal(String connectorName, boolean deleteConfiguration) {
		if (isConnectorPresent(connectorName)) {
			executeUpdate(connection, ConnectorQueryGenerator.generateDropQuery(connectorName));
		}
		if (!DEFAULT_CONNECTOR_URI.equals(connectorName) && deleteConfiguration) {
			deleteConnectorConfiguration(connectorName, connection);
		}

		LOGGER.info("Deleted Solr connector {}", connectorName);
	}

	private static void deleteConnectorConfiguration(String connectorName,
			RepositoryConnection transactionalConnection) {
		String connectorIri = connectorName;
		if (!connectorName.startsWith(PREFIX)) {
			connectorIri = PREFIX + URI_SEPARATOR + connectorName;
		}

		executeUpdate(transactionalConnection, ConnectorQueryGenerator.generateDeleteConfigurationQuery(connectorIri));
	}

	@Override
	public boolean isConnectorPresent(String connectorName) {
		Objects.requireNonNull(connectorName, String.format(INPUT_PARAMETER_MUST_NOT_BE_NULL_MESSAGE, "connectorName"));

		String query = ConnectorQueryGenerator.generateExistsConnectorQuery(connectorName);

		String cntStatus = null;
		try (TupleQueryResult queryResult = executeQuery(connection, query, CollectionUtils.emptyMap())) {
			if (queryResult.hasNext()) {
				cntStatus = queryResult.next().getBinding("cntStatus").getValue().stringValue();
				JSONObject statusObject = new JSONObject(cntStatus);
				return "BUILT".equals(statusObject.getString("status"));
			}
		} catch (JSONException e) {
			throw new SemanticPersistenceException("Cannot parse connector status response: " + cntStatus); 
		}
		return false;
	}

	@SuppressWarnings("boxing")
	@Override
	public List<ConnectorConfiguration> listConnectors() {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<ConnectorConfiguration> results = new LinkedList<>();

		try (TupleQueryResultIterator resultIterator = executeQuery(connection, QUERY_CONNECTOR_INSTANCES,
				CollectionUtils.emptyMap())) {
			while (resultIterator.hasNext()) {
				BindingSet set = resultIterator.next();
				IRI connectorIRI = (IRI) set.getBinding(SPARQLQueryHelper.OBJECT).getValue();

				results.add(loadConnectorConfiguration(namespaceRegistryService.getShortUri(connectorIRI), false,
						connection));
			}
		}

		LOGGER.debug("List connectors took: {} and returned {} results.", tracker.stopInSeconds(), results.size());

		return results;
	}

	@Override
	public ConnectorConfiguration createDefaultConnectorConfiguration(String connectorName) {
		return loadConnectorConfiguration(connectorName, true, connection);
	}

	@Override
	public ConnectorConfiguration loadConnectorConfiguration(String connectorName) {
		return loadConnectorConfiguration(connectorName, false, connection);
	}

	private ConnectorConfiguration loadConnectorConfiguration(String connectorName, boolean isDefaultConnector,
			RepositoryConnection repositoryConnection) {
		Objects.requireNonNull(connectorName);

		String connectorIri;
		if (connectorName.startsWith(PREFIX)) {
			connectorIri = connectorName;
		} else if (isDefaultConnector) {
			connectorIri = DEFAULT_CONNECTOR_URI;
		} else {
			connectorIri = PREFIX + URI_SEPARATOR + connectorName;
		}

		Map<String, Serializable> connectorProperties = fetchConnectorProperties(connectorIri, repositoryConnection);
		if (connectorProperties.isEmpty()) {
			return null;
		}
		ConnectorConfiguration configuration = new ConnectorConfiguration();
		configuration.setProperties(connectorProperties);

		configuration.setFields(fetchConnectorFields(connectorIri, isDefaultConnector, repositoryConnection));

		if (isDefaultConnector) {
			connectorIri = PREFIX + URI_SEPARATOR + connectorName;
			configuration.setConnectorName(connectorName);
		}
		configuration.setId(connectorIri);

		return configuration;
	}

	private Map<String, Serializable> fetchConnectorProperties(String connectorIri,
			RepositoryConnection repositoryConnection) {
		Map<String, Serializable> properties = CollectionUtils.createHashMap(10);

		Map<String, Serializable> bindings = CollectionUtils.createHashMap(1);
		bindings.put(SPARQLQueryHelper.OBJECT, namespaceRegistryService.buildUri(connectorIri));

		try (TupleQueryResult evaluate = executeQuery(repositoryConnection, QUERY_CONNECTOR_INSTANCE_PROPERTIES,
				bindings)) {
			while (evaluate.hasNext()) {
				BindingSet bindingSet = evaluate.next();
				Binding property = bindingSet.getBinding("property");
				Binding value = bindingSet.getBinding("value");

				properties.put(namespaceRegistryService.getShortUri((IRI) property.getValue()),
						ValueConverter.convertValue(value.getValue()));
			}
		}
		return properties;
	}

	private Map<String, ConnectorField> fetchConnectorFields(String connectorIri, boolean isDefaultConnector,
			RepositoryConnection repositoryConnection) {
		String queryConnectorFields = QUERY_CONNECTOR_FIELDS;
		if (isDefaultConnector) {
			queryConnectorFields = QUERY_DEFAULT_CONNECTOR_FIELDS;
		}
		Map<String, ConnectorField> fields = CollectionUtils.createHashMap(10);

		Map<String, Serializable> bindings = CollectionUtils.createHashMap(1);
		bindings.put("connector", namespaceRegistryService.buildUri(connectorIri));

		try (TupleQueryResult evaluate = executeQuery(repositoryConnection, queryConnectorFields, bindings)) {
			while (evaluate.hasNext()) {
				BindingSet bindingSet = evaluate.next();
				Uri id = new Rdf4JUriProxy((IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue());
				String description = bindingSet.getBinding("description").getValue().stringValue().trim();
				Boolean isSortable = (Boolean) ValueConverter
						.convertValue(bindingSet.getBinding("isSortable").getValue());
				String fieldType = "";
				if (bindingSet.hasBinding("fieldType")) {
					fieldType = ValueConverter.convertValue(bindingSet.getBinding("fieldType").getValue()).toString();
				}

				ConnectorField field = fields.get(id.toString());
				if (field == null) {
					field = new ConnectorField();
					field.setId(id);
					fields.put(id.toString(), field);
				}

				field.addDescription(description);
				field.setIsSortable(isSortable);

				String nativeType = readNativeType(description, fieldType);
				if (field.getType() == null) {
					field.setType(nativeType);
				}
			}
		}

		return fields;
	}

	@Override
	public void saveConnectorConfiguration(ConnectorConfiguration configuration) {
		String id = configuration.getId();
		Model insertModel = new LinkedHashModel();
		
		LOGGER.info("Saving Solr connector configuration for {}", configuration.getConnectorName());

		deleteConnectorConfiguration(id, connection);

		configuration.add(EMF.PREFIX + URI_SEPARATOR + EMF.MODIFIED_ON.getLocalName(), new Date());
		configuration.addIfNotPresent(EMF.PREFIX + URI_SEPARATOR + EMF.CREATED_ON.getLocalName(), new Date());
		configuration.add(DefaultProperties.SEMANTIC_TYPE, Connectors.CONNECTOR);

		incrementVersion(configuration);

		for (Entry<String, Serializable> property : configuration.getProperties().entrySet()) {
			insertModel.add(SemanticPersistenceHelper.createLiteralStatement(id, property.getKey(), property.getValue(),
					namespaceRegistryService, valueFactory));
		}

		for (ConnectorField field : configuration.getFields().values()) {
			insertModel.add(SemanticPersistenceHelper.createStatement(id, Connectors.HAS_FIELD, field.getId(),
					namespaceRegistryService, valueFactory));
		}

		if (!insertModel.isEmpty()) {
			LOGGER.debug("Saving connector instance {}. Inserting statements {}", configuration.getConnectorName(),
					insertModel);
			SemanticPersistenceHelper.saveModel(connection, insertModel, Connectors.CONNECTORS_DATA_GRAPH);
		}
	}

	@SuppressWarnings("boxing")
	private static void incrementVersion(ConnectorConfiguration configuration) {
		double version = configuration.getDouble(DefaultProperties.VERSION);
		configuration.add(DefaultProperties.VERSION, version + 1);
	}

	private void initSolrScheme(ConnectorConfiguration configuration) {
		String initialConnectorData = ConnectorQueryGenerator.generateInitializationQuery(configuration);
		if (StringUtils.isNotEmpty(initialConnectorData)) {

			transactionSupport.invokeInNewTx(() -> executeUpdate(connection, initialConnectorData), 5,
					TimeUnit.MINUTES);
			transactionSupport.invokeInNewTx(() -> executeUpdate(connection,
					ConnectorQueryGenerator.generateDeleteInitializationData(initialConnectorData)));
		}
	}

	private static TupleQueryResultIterator executeQuery(RepositoryConnection repositoryConnection, String query,
			Map<String, Serializable> bindings) {
		LOGGER.debug("Executing query: {} with bindings {}", query, bindings);

		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, query, bindings, true);
		return new TupleQueryResultIterator(tupleQuery.evaluate());
	}

	private static void executeUpdate(RepositoryConnection connection, String updateQuery) {
		LOGGER.debug("Executing Update Sparql Query: {}", updateQuery);

		Update update = SPARQLQueryHelper.prepareUpdateQuery(connection, updateQuery, CollectionUtils.emptyMap(),
				false);
		update.execute();
	}

	private static String readNativeType(String description, String fieldType) {
		String type = "string";

		if (description.contains("\"datatype\"")) {
			String tmpType = description.split("\"datatype\"\\:\\s+\"")[1];
			return tmpType.substring(0, tmpType.indexOf('\"'));
		}

		if (StringUtils.isNotEmpty(fieldType)) {
			return fieldType;
		}

		return type;
	}
}
