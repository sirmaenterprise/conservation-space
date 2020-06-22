package com.sirma.seip.semantic.management;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.rest.RandomPortGenerator;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.Connectors;

/**
 * Test for {@link ConnectorServiceImpl}
 * 
 * @author kirq4e
 */
public class ConnectorServiceTest extends GeneralSemanticTest<ConnectorService> {

	private static final String TENANT_ID = "test.com";
	private static final String CONNECTOR_NAME = "fts_test_com";
	private static final String CORE_NAME_CONSTANT = "$CORE_NAME$";
	private static final String MISSING_CORE_RESPONSE = "<response><lst name=\"responseHeader\"><int name=\"status\">0</int><int name=\"QTime\">0</int></lst><lst name=\"initFailures\"/><lst name=\"status\"><lst name=\""
			+ CORE_NAME_CONSTANT + "\"/></lst></response>";
	private static final String EXISTING_CORE_RESPONSE = "<response><lst name=\"responseHeader\"><int name=\"status\">0</int><int name=\"QTime\">0</int></lst><lst name=\"initFailures\"/><lst name=\"status\"><lst name=\""
			+ CORE_NAME_CONSTANT + "\"><str name=\"name\">" + CORE_NAME_CONSTANT
			+ "</str><str name=\"instanceDir\">/var/lib/solr/" + CORE_NAME_CONSTANT
			+ "</str><str name=\"dataDir\">/var/lib/solr/" + CORE_NAME_CONSTANT
			+ "/data/</str><str name=\"config\">solrconfig.xml</str><str name=\"schema\">managed-schema</str><date name=\"startTime\">2018-02-19T12:06:55.162Z</date><long name=\"uptime\">74081333</long><lst name=\"index\"><int name=\"numDocs\">143</int><int name=\"maxDoc\">143</int><int name=\"deletedDocs\">0</int><long name=\"indexHeapUsageBytes\">-1</long><long name=\"version\">50</long><int name=\"segmentCount\">1</int><bool name=\"current\">true</bool><bool name=\"hasDeletions\">false</bool><str name=\"directory\">org.apache.lucene.store.NRTCachingDirectory:NRTCachingDirectory(MMapDirectory@/var/lib/solr/"
			+ CORE_NAME_CONSTANT
			+ "/data/index lockFactory=org.apache.lucene.store.NativeFSLockFactory@5241e045; maxCacheMB=48.0 maxMergeSizeMB=4.0)</str><str name=\"segmentsFile\">segments_d</str><long name=\"segmentsFileSizeInBytes\">165</long><lst name=\"userData\"><str name=\"commitTimeMSec\">1519042298498</str></lst><date name=\"lastModified\">2018-02-19T12:11:38.498Z</date><long name=\"sizeInBytes\">44043</long><str name=\"size\">43.01 KB</str></lst></lst></lst></response>";

	private NamespaceRegistryService namespaceRegistryMock;

	private static int port = RandomPortGenerator.generatePort(8900, 9000);
	private static final String SOLR_ADDRESS = "http://localhost:" + port + "/solr";
	private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(port));

	private static final boolean CREATED = true;
	private static final boolean NOT_CREATED = false;

	@BeforeClass
	public static void init() {
		wireMockServer.start();
		WireMock.configureFor("localhost", port);
	}

	@BeforeMethod
	public void beforeMethod() {
		service = new ConnectorServiceImpl();
		RepositoryConnection connection = Mockito.spy(connectionFactory.produceManagedConnection());

		Mockito.doAnswer(a -> {
			String createQuery = a.getArgumentAt(1, String.class);
			Object returnObject = a.callRealMethod();
			int beginIndex = createQuery.indexOf("solr-inst:fts") + 10;
			String connectorName = createQuery.substring(beginIndex, createQuery.indexOf(" ", beginIndex));
			
			markConnectorAs(connectorName, CREATED);
			return returnObject;
		}).when(connection).prepareUpdate(Matchers.eq(QueryLanguage.SPARQL), Matchers.contains("solr:createConnector"));

		Mockito.doAnswer(a -> {
			String dropQuery = a.getArgumentAt(1, String.class);
			Object returnObject = a.callRealMethod();
			int beginIndex = dropQuery.indexOf("solr-inst:fts") + 10;
			String connectorName = dropQuery.substring(beginIndex, dropQuery.indexOf(" ", beginIndex));
			
			markConnectorAs(connectorName, NOT_CREATED);
			return returnObject;
		}).when(connection).prepareUpdate(Matchers.eq(QueryLanguage.SPARQL), Matchers.contains("solr:dropConnector"));

		ReflectionUtils.setFieldValue(service, "connection", connection);
		namespaceRegistryMock = new NamespaceRegistryMock(context);
		ReflectionUtils.setFieldValue(service, "namespaceRegistryService", namespaceRegistryMock);
		ReflectionUtils.setFieldValue(service, "valueFactory", connectionFactory.produceValueFactory());

		Mockito.when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);

		ReflectionUtils.setFieldValue(service, "securityContext", securityContext);

		TransactionSupport transactionSupport = new TransactionSupportFake();
		ReflectionUtils.setFieldValue(service, "transactionSupport", transactionSupport);
	}

	@AfterMethod
	public void afterMethod() {
		WireMock.reset();
	}

	@AfterClass
	public static void afterClass() {
		wireMockServer.stop();
	}

	@Test
	public void testIfConnectorExistsCreated() throws Exception {
		noTransaction();
		RepositoryConnection connection = null;
		markConnectorAs(CONNECTOR_NAME, true);

		Assert.assertTrue(service.isConnectorPresent(CONNECTOR_NAME));

		markConnectorAs(CONNECTOR_NAME, false);
	}

	@Test
	public void testIfConnectorExistsNotCreated() throws Exception {
		noTransaction();
		Assert.assertFalse(service.isConnectorPresent(CONNECTOR_NAME + "Test"));
	}

	@Test
	public void testListConnectorsEmptyList() throws Exception {
		noTransaction();
		List<ConnectorConfiguration> list = service.listConnectors();
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testCreateDefaultConnector() throws Exception {
		noTransaction();
		ConnectorConfiguration defaultConnectorConfiguration = service
				.createDefaultConnectorConfiguration(CONNECTOR_NAME);

		Assert.assertNotNull(defaultConnectorConfiguration);
		Assert.assertEquals(defaultConnectorConfiguration.getConnectorName(), CONNECTOR_NAME);
		Assert.assertFalse(defaultConnectorConfiguration.getFields().isEmpty());
		Assert.assertTrue(service.listConnectors().isEmpty());
	}

	@Test
	public void testCreateSolrConnector() throws Exception {
		stubSolrServer(CONNECTOR_NAME, MISSING_CORE_RESPONSE);

		ConnectorConfiguration defaultConnectorConfiguration = service
				.createDefaultConnectorConfiguration(CONNECTOR_NAME);
		defaultConnectorConfiguration.setAddress(SOLR_ADDRESS);

		ConnectorConfiguration configuration = service.createConnector(defaultConnectorConfiguration);
		commitTransaction();

		Assert.assertNotNull(configuration.getId());
		Assert.assertEquals(configuration.getConnectorName(), CONNECTOR_NAME);
		Assert.assertFalse(service.listConnectors().isEmpty());
		Assert.assertEquals(service.listConnectors().get(0).getConnectorName(), CONNECTOR_NAME);

		beginTransaction();

		Assert.assertTrue(service.deleteConnector(CONNECTOR_NAME));
		commitTransaction();
		Assert.assertTrue(service.listConnectors().isEmpty());
	}

	@Test
	public void testResetSolrConnector() throws Exception {
		stubSolrServer(CONNECTOR_NAME, MISSING_CORE_RESPONSE);

		ConnectorConfiguration defaultConnectorConfiguration = service
				.createDefaultConnectorConfiguration(CONNECTOR_NAME);
		defaultConnectorConfiguration.setAddress(SOLR_ADDRESS);

		ConnectorConfiguration configuration = service.createConnector(defaultConnectorConfiguration);
		commitTransaction();

		beginTransaction();
		service.resetConnector(CONNECTOR_NAME);
		commitTransaction();

		ConnectorConfiguration newConfiguration = service.loadConnectorConfiguration(CONNECTOR_NAME);

		Assert.assertNotNull(newConfiguration);
		String recreatedOnIRI = Connectors.PREFIX + SPARQLQueryHelper.URI_SEPARATOR
				+ Connectors.RECREATED_ON.getLocalName();
		Assert.assertNotEquals(newConfiguration.get(recreatedOnIRI), configuration.get(recreatedOnIRI));

		Assert.assertEquals(service.listConnectors().get(0).getConnectorName(), CONNECTOR_NAME);

		beginTransaction();

		Assert.assertTrue(service.deleteConnector(CONNECTOR_NAME));
		commitTransaction();
		Assert.assertTrue(service.listConnectors().isEmpty());
	}

	@Test
	public void testListConnectorMultipleConnectors() throws Exception {
		String firstCore = CONNECTOR_NAME + "1";
		String secondCore = CONNECTOR_NAME + "2";
		stubSolrServer(firstCore, MISSING_CORE_RESPONSE);
		stubSolrServer(secondCore, MISSING_CORE_RESPONSE);

		ConnectorConfiguration defaultConnectorConfiguration = service.createDefaultConnectorConfiguration(firstCore);
		defaultConnectorConfiguration.setAddress(SOLR_ADDRESS);
		service.createConnector(defaultConnectorConfiguration);
		commitTransaction();

		defaultConnectorConfiguration = service.createDefaultConnectorConfiguration(secondCore);
		defaultConnectorConfiguration.setAddress(SOLR_ADDRESS);
		beginTransaction();
		service.createConnector(defaultConnectorConfiguration);
		commitTransaction();

		List<ConnectorConfiguration> connectors = service.listConnectors();
		Assert.assertEquals(connectors.size(), 2);
		Set<String> createdConnectors = connectors.stream().map(ConnectorConfiguration::getConnectorName).collect(
				Collectors.toSet());
		Assert.assertTrue(createdConnectors.contains(firstCore));
		Assert.assertTrue(createdConnectors.contains(secondCore));

		beginTransaction();
		Assert.assertTrue(service.deleteConnector(firstCore));
		Assert.assertTrue(service.deleteConnector(secondCore));
		commitTransaction();
	}

	@Test
	public void testDeleteConnector() throws Exception {
		stubSolrServer(CONNECTOR_NAME, MISSING_CORE_RESPONSE);

		ConnectorConfiguration defaultConnectorConfiguration = service
				.createDefaultConnectorConfiguration(CONNECTOR_NAME);
		defaultConnectorConfiguration.setAddress(SOLR_ADDRESS);
		service.createConnector(defaultConnectorConfiguration);
		commitTransaction();

		List<ConnectorConfiguration> connectors = service.listConnectors();
		Assert.assertEquals(connectors.size(), 1);
		Assert.assertEquals(connectors.get(0).getConnectorName(), CONNECTOR_NAME);

		beginTransaction();
		Assert.assertTrue(service.deleteConnector(CONNECTOR_NAME));
		commitTransaction();

		connectors = service.listConnectors();
		Assert.assertTrue(connectors.isEmpty());
	}

	@Test(expectedExceptions = SemanticPersistenceException.class)
	public void testCreateConnectorWithExistingCore() throws Exception {
		noTransaction();
		stubSolrServer(CONNECTOR_NAME, EXISTING_CORE_RESPONSE);
		ConnectorConfiguration defaultConnectorConfiguration = service
				.createDefaultConnectorConfiguration(CONNECTOR_NAME);
		defaultConnectorConfiguration.setAddress(SOLR_ADDRESS);
		service.createConnector(defaultConnectorConfiguration);
	}

	private static void stubSolrServer(String connectorName, String response) {
		stubFor(get(urlPathEqualTo("/solr/admin/cores?action=STATUS&core=" + connectorName))
				.willReturn(aResponse().withStatus(200).withBody(response.replace(CORE_NAME_CONSTANT, connectorName))));
	}

	/**
	 * Marks the connector as created or not created depending on the flag createFlag. When the connector is marked as
	 * created (createFlag is true) the statement with predicate solr:connectorStatus is inserted in the repository.
	 * When the connector is marked as not created (createFlag is false) then the statemeth with predicate
	 * solr:connectorStatus is removed
	 * 
	 * @param connection
	 *            Repository connection
	 * @param connectorName
	 *            Name of the connector
	 * @param createFlag
	 *            true - Mark the connector as created; false - Mark the connector as not created
	 */
	private void markConnectorAs(String connectorName, boolean createFlag) {
		pauseTransaction();
		RepositoryConnection connection = null;
		try {
			connection = connectionFactory.produceConnection();
			Statement statement = SemanticPersistenceHelper.createLiteralStatement(
					"solr-inst" + SPARQLQueryHelper.URI_SEPARATOR + connectorName, "solr:connectorStatus",
					"{\"status\":\"BUILT\"}", namespaceRegistryMock, connection.getValueFactory());
			if (createFlag) {
				connection.add(statement);
			} else {
				connection.remove(statement);
			}
		} finally {
			connectionFactory.disposeConnection(connection);
		}
		resumeTransaction();
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
