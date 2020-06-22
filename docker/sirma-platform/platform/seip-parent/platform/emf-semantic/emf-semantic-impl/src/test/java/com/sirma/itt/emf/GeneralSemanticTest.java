package com.sirma.itt.emf;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.emf.converter.extensions.CommonInstanceConverterProvider;
import com.sirma.itt.emf.mocks.DefinitionServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.semantic.ConnectionFactoryImpl;
import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.emf.semantic.configuration.SemanticConfigurationImpl;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryCreator;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.instance.SemanticInstanceTypes;
import com.sirma.itt.seip.instance.convert.InstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.namespaces.DefaultNamespaces;

/**
 * General test that initialized the connection to the repository
 *
 * @param <E>
 *            Class of the service that will be tested
 * @author kirq4e
 */
public abstract class GeneralSemanticTest<E> extends EmfTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeneralSemanticTest.class);

	private static final SPARQLParserFactory QUERY_PARSER = new SPARQLParserFactory();

	public static final String TEST_DATA_REPOSITORY = "/data/";
	public static final String DEFAULT_NAMESPACE = "http://example.org#";

	protected static ConnectionFactoryImpl connectionFactory;

	protected E service;

	protected TransactionManager transactionManager;
	protected TransactionSynchronizationRegistry synchronizationRegistry;
	protected Transaction transaction;

	/**
	 * Context objects that are needed for initialization of the mock services
	 */
	protected static Map<String, Object> context;
	private static Repository repository;

	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter converter = super.createTypeConverter();
		new ValueConverter().register(converter);
		InstanceDao instanceDao = mock(InstanceDao.class);
		InstanceToInstanceReferenceConverterProvider provider = new InstanceToInstanceReferenceConverterProvider();
		ReflectionUtils.setFieldValue(provider, "instanceDao", instanceDao);
		SemanticInstanceTypes instanceTypes = new SemanticInstanceTypes();
		ReflectionUtils.setFieldValue(instanceTypes, "typeConverter", converter);
		ReflectionUtils.setFieldValue(instanceTypes, "semanticDefinitionService",
				new SemanticDefinitionServiceMock(context));
		ReflectionUtils.setFieldValue(provider, "instanceTypes", instanceTypes);
		ReflectionUtils.setFieldValue(provider, "definitionService", new DefinitionServiceMock());
		NamespaceRegistryService registryService = mock(NamespaceRegistryService.class);
		when(registryService.buildFullUri(anyString())).then(AdditionalAnswers.returnsFirstArg());
		ReflectionUtils.setFieldValue(provider, "namespaceRegistryService", registryService);

		CommonInstanceConverterProvider instanceConverterProvider = new CommonInstanceConverterProvider();
		ReflectionUtils.setFieldValue(instanceConverterProvider, "instanceDao", instanceDao);
		provider.register(converter);
		instanceConverterProvider.register(converter);
		return converter;
	}

	/**
	 * Gets the test data file.
	 *
	 * @return the test data file
	 */
	protected abstract String getTestDataFile();

	/**
	 * Gets the connection factory.
	 *
	 * @return the connection factory
	 */
	protected static ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Sets a current user to be admin before class execution.<br>
	 * Initialize the sequence generator.
	 */
	@Override
	public void setUpSecurityContext() {
		initMocks(this);
		super.setUpSecurityContext();
	}

	/**
	 * Before class.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public void beforeClass() throws Exception {
		repository = RepositoryCreator.createLocalRepository();
		try {
			repository.initialize();
		} catch (RepositoryException e) {
			Assert.fail("Failed initializing repository", e);
		}

		setUpSecurityContext();

		SemanticConfiguration semanticConfiguration = new SemanticConfigurationImpl();
		ReflectionUtils.setFieldValue(semanticConfiguration, "repository",
				new ConfigurationPropertyMock<>(() -> repository));

		transactionManager = mock(TransactionManager.class);
		synchronizationRegistry = mock(TransactionSynchronizationRegistry.class);

		connectionFactory = new ConnectionFactoryImpl(semanticConfiguration, transactionManager, synchronizationRegistry,
				securityContext);
		// initialize the context
		context = new HashMap<>();
		context.put("securityContext", securityContext);
		context.put("connectionFactory", connectionFactory);
		context.put("valueFactory", connectionFactory.produceValueFactory());
		context.put("idManager", idManager);
		context.put("typeConverter", createTypeConverter());
	}

	@BeforeMethod
	public void beginTransaction() {
		try {

			transaction = mock(Transaction.class);
			when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);
			when(transactionManager.getTransaction()).thenReturn(transaction);
		} catch (SystemException e) {
			fail("", e);
		}
	}

	public void noTransaction() {
		try {
			when(transactionManager.getTransaction()).thenReturn(null);
		} catch (SystemException e) {
			fail("", e);
		}
	}

	Transaction pausedTransaction;

	public void pauseTransaction() {
		try {
			pausedTransaction = transactionManager.getTransaction();
			noTransaction();
		} catch (SystemException e) {
			fail("", e);
		}
	}

	public void resumeTransaction() {
		if (pausedTransaction != null) {
			try {
				when(transactionManager.getTransaction()).thenReturn(pausedTransaction);
			} catch (SystemException e) {
				fail("", e);
			}
		}
	}

	@AfterMethod
	public void commitTransaction() {
		try {
			ArgumentCaptor<Synchronization> captor = ArgumentCaptor.forClass(Synchronization.class);
			Transaction tx = transactionManager.getTransaction();
			if (tx == null) {
				return;
			}
			verify(synchronizationRegistry, atLeastOnce()).registerInterposedSynchronization(captor.capture());
			Synchronization synchronization = captor.getValue();
			synchronization.beforeCompletion();
			synchronization.afterCompletion(Status.STATUS_COMMITTING);
			when(tx.getStatus()).thenReturn(Status.STATUS_COMMITTED);
		} catch (SystemException e) {
			fail("transaction commit failed", e);
		}
	}

	/**
	 * Sets the test data. Inserts necessary predefined statements (data) in the semantic repository for given test
	 *
	 * @throws IOException
	 *             in case an I/O error occurs while data was read from the test data file
	 * @throws RDFHandlerException
	 *             in case the configured statement handler has encountered an unrecoverable error
	 * @throws RDFParseException
	 *             in case an unrecoverable parse error occurs while reading the test data file
	 * @throws RepositoryException
	 *             in case predefined statements from the test data file could not be added to the repository
	 */
	@BeforeClass
	public void setTestData() throws RDFParseException, RDFHandlerException, IOException, RepositoryException {
		String testDataFileLocation = getTestDataFile();

		if (StringUtils.isBlank(testDataFileLocation)) {
			return;
		}

		RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForFileName(testDataFileLocation).orElse(RDFFormat.TURTLE);
		try (InputStream stream = loadDataFile(testDataFileLocation);
				Reader reader = new InputStreamReader(stream)) {

			final Model model = new LinkedHashModel();
			RDFParser parser = Rio.createParser(format);
			parser.setRDFHandler(new StatementCollector(model));
			parser.parse(reader, DEFAULT_NAMESPACE);
			beginTransaction();
			RepositoryConnection connection = connectionFactory.produceManagedConnection();

			if (!model.contexts().isEmpty()) {
				for (Resource contextUri : model.contexts()) {
					connection.add(model.filter(null, null, null, contextUri), contextUri);
				}

				if (model.contains(null, null, null, (Resource) null)) {
					connection.add(model.filter(null, null, null, (Resource) null), (Resource) null);
				}

			} else {
				connection.add(model, (Resource) null);
			}
			commitTransaction();
		}
	}

	protected InputStream loadDataFile(String location) {
		return getClass().getResourceAsStream(TEST_DATA_REPOSITORY + location);
	}

	/**
	 * After class.
	 */
	@AfterClass
	public static void afterClass() {
		connectionFactory.tearDown();
	}

	/**
	 * Exports the semantic repository to a file
	 *
	 * @param outputFileName
	 *            The path and name of the fiel in which we want to export the repository
	 */
	protected void exportRepository(String outputFileName) {
		try (FileOutputStream out = new FileOutputStream(outputFileName)) {
			RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, out);
			RepositoryConnection connection = connectionFactory.produceConnection();
			connection.export(writer, (Resource) null);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Exports all statements with the given URI as subject
	 *
	 * @param connection
	 *            Connection to the repository
	 * @param uri
	 *            URI of the resource. It will be used as subject for the exported statements
	 * @param outputFileName
	 *            The path and name of the fiel in which we want to export the repository
	 */
	protected void exportResource(RepositoryConnection connection, String uri, String outputFileName) {
		try (FileOutputStream out = new FileOutputStream(outputFileName)) {
			RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, out);
			NamespaceRegistry namespaceRegistry = new NamespaceRegistryMock(context);
			connection.exportStatements(namespaceRegistry.buildUri(uri), null, null, true, writer, (Resource) null);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public static void assertValidSparqlQuery(String query) {
		String localQuery = query;
		if (!localQuery.contains("PREFIX ")) {
			// skip appending the prefixes if they are defined in the query
		localQuery = DefaultNamespaces.ALL_NAMESPACES
				.stream()
				.map(namespace -> new StringBuilder(100)
						.append("PREFIX ")
							.append(namespace.getFirst().trim())
							.append(NamespaceRegistryService.SHORT_URI_DELIMITER)
							.append("<")
							.append(namespace.getSecond())
							.append(">")
							.toString())
				.collect(Collectors.joining("\n")) + query;
		}
		Assert.assertNotNull(QUERY_PARSER.getParser().parseQuery(localQuery, "http://test/test"));
	}
}
