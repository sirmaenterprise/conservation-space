package com.sirma.itt.emf;

import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.extensions.CommonInstanceConverterProvider;
import com.sirma.itt.emf.mocks.DictionaryServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.semantic.ConnectionFactoryImpl;
import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.emf.semantic.RepositoryConnectionMonitor;
import com.sirma.itt.emf.semantic.configuration.SemanticConfigurationImpl;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryCreator;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.instance.SemanticInstanceTypes;
import com.sirma.itt.seip.instance.convert.InstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * General test that initialized the connection to the repository
 *
 * @param <E>
 *            Class of the service that will be tested
 * @author kirq4e
 */
public abstract class GeneralSemanticTest<E> extends EmfTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeneralSemanticTest.class);

	private static final String TEST_DATA_REPOSITORY = "src/test/resources/data/";
	private static final String DEFAULT_NAMESPACE = "http://example.org#";

	protected static ConnectionFactoryImpl connectionFactory;

	protected E service;

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
		ReflectionUtils.setField(provider, "instanceDao", instanceDao);
		SemanticInstanceTypes instanceTypes = new SemanticInstanceTypes();
		ReflectionUtils.setField(instanceTypes, "typeConverter", converter);
		ReflectionUtils.setField(instanceTypes, "semanticDefinitionService",
				new SemanticDefinitionServiceMock(context));
		ReflectionUtils.setField(provider, "instanceTypes", instanceTypes);
		ReflectionUtils.setField(provider, "dictionaryService", new DictionaryServiceMock());

		CommonInstanceConverterProvider instanceConverterProvider = new CommonInstanceConverterProvider();
		ReflectionUtils.setField(instanceConverterProvider, "instanceDao", instanceDao);
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
	 * @throws IOException
	 */
	@BeforeClass
	public void beforeClass() throws IOException {
		repository = RepositoryCreator.createLocalRepository();
		try {
			repository.initialize();
		} catch (RepositoryException e) {
			Assert.fail("Failed initializing repository", e);
		}

		connectionFactory = new ConnectionFactoryImpl();
		SemanticConfiguration semanticConfiguration = new SemanticConfigurationImpl();
		ReflectionUtils.setField(semanticConfiguration, "repository",
				new ConfigurationPropertyMock<>(() -> repository));
		ReflectionUtils.setField(connectionFactory, "semanticConfiguration", semanticConfiguration);
		ReflectionUtils.setField(connectionFactory, "statistics", new NoOpStatistics());

		setUpSecurityContext();

		RepositoryConnectionMonitor monitor = new RepositoryConnectionMonitor();
		ReflectionUtils.setField(monitor, "securityContext", securityContext);
		ReflectionUtils.setField(connectionFactory, "connectionMonitor", monitor);
		// initialize the context
		context = new HashMap<>();
		context.put("securityContext", securityContext);
		context.put("connectionFactory", connectionFactory);
		context.put("valueFactory", connectionFactory.produceValueFactory());
		context.put("idManager", idManager);
		context.put("typeConverter", createTypeConverter());
		context.put("monitor", monitor);
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

		if (StringUtils.isNullOrEmpty(testDataFileLocation)) {
			return;
		}

		File testDataFile = new File(TEST_DATA_REPOSITORY + testDataFileLocation);
		RDFFormat format = RDFFormat.forFileName(testDataFile.getAbsolutePath());
		Reader reader = new FileReader(testDataFile);

		final Model model = new LinkedHashModel();
		RDFParser parser = Rio.createParser(format);
		parser.setRDFHandler(new StatementCollector(model));
		parser.parse(reader, DEFAULT_NAMESPACE);
		RepositoryConnection connection = connectionFactory.produceConnection();

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
		// connection.commit();
		connectionFactory.disposeConnection(connection);
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

}
