package com.sirma.itt.emf.semantic.patch;

import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests RemoveDuplicateSecurityRoles patch
 * 
 * @author kirq4e
 */
public class RemoveDuplicateSecurityRolesTest {

	@InjectMocks
	private RemoveDuplicateSecurityRoles patch;

	@Mock
	private DataSource dataSource;

	@Mock
	private ConnectionFactory connectionFactory;

	@Mock
	private NamespaceRegistryService namespaceRegistry;

	/**
	 * Test data for Relation database which is connected to the data in the semantic test file
	 */
	public static final Map<String, String> INSTANCE_ROLE_TEST_DATA;
	static {
		INSTANCE_ROLE_TEST_DATA = new HashMap<>();
		INSTANCE_ROLE_TEST_DATA.put("emf:d0966b1c-69f3-4b91-877b-adc66d726ca9emf:regularuser", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:8be7bb84-2abf-4233-b78d-db44edc4a8d9emf:viktor.ribchev", "COLLABORATOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:05673bab-b82a-412b-85f5-78d4f7ddf651emf:AnnaRadeva", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:1da4b3c5-ed19-4294-bd18-52bce8d65afaemf:AnnaRadeva", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:c0038485-72c4-43e0-962e-64b2323beb0demf:admin", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:8cf977f9-262a-4156-8295-151b9531a484emf:at_COLLABORATOR", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:274a4e3c-829e-436a-9eba-5725ea0fc879emf:daniela.todorova", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:884c2d8c-68b9-4d24-9d62-05ad8c18572cemf:daniela.todorova", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:da4c4af5-b842-49ec-967e-fbfcd3a660b7emf:sibelAll", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:78e5160f-7a7f-4cd9-81da-2bfab153e01eemf:ivasilev2", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:d2441a4e-7751-4000-8524-40e13e962438emf:regularuser", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:78f8ba22-6ea4-43cc-bd6f-2d4ba7a90c4femf:ivasilev3", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:ed3af5d8-eba8-481d-853c-4d6aab75eb6eemf:regularuser", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:7e59aea2-eac3-4845-a338-0479d1a7e3b1emf:ivasilev2", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:4a01c0a1-e098-4e35-8695-f5981b89d8f7emf:sibCOLLABORATOR1", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("emf:ff28c824-dbe6-463b-bc3c-b0d36dd2b34cemf:admin", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:87de81a8-fc93-4b39-94a7-0b539ee6422eemf:admin", "MANAGER");
		INSTANCE_ROLE_TEST_DATA.put("emf:6e2cfa1a-7681-478b-9b0f-f1162a94e2d6emf:Radeva", "CONTRIBUTOR");
		INSTANCE_ROLE_TEST_DATA.put("chd:Samplesec:SYSTEM_ALL_OTHER_USERS", "CONSUMER");
		INSTANCE_ROLE_TEST_DATA.put("hr:SoftSkillsec:SYSTEM_ALL_OTHER_USERS", "CONSUMER");
	}

	/**
	 * Initializes the mocks
	 * 
	 * @throws Exception
	 *             If an error occurs
	 */
	@Before
	public void setUp() throws Exception {
		patch = new RemoveDuplicateSecurityRoles();
		MockitoAnnotations.initMocks(this);

		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection connection = Mockito.spy(repo.getConnection());
		when(connectionFactory.produceConnection()).thenReturn(connection);
		when(namespaceRegistry.getShortUri(Matchers.any(IRI.class))).then(answer -> {
			IRI uri = answer.getArgumentAt(0, IRI.class);
			return EMF.PREFIX + SPARQLQueryHelper.URI_SEPARATOR + uri.getLocalName();
		});
		
		mockRDBData(dataSource, true);
	}

	/**
	 * Tests the patch with no broken instances. The expected result is that nothing is changed
	 * 
	 * @throws Exception
	 *             If an error occurs
	 */
	@Test
	public void executeWithNoResults() throws Exception {
		RepositoryConnection connection = connectionFactory.produceConnection();

		patch.execute(null);

		Mockito.verify(connection, Mockito.times(0)).remove(Matchers.any(Model.class), Matchers.any());
	}

	/**
	 * Tests the patch with broken instances. Expected result is the relation between the user and duplicate roles is
	 * removed
	 * 
	 * @throws Exception
	 *             If an error occurs
	 */
	@Test
	public void executeWithResults() throws Exception {
		RepositoryConnection connection = connectionFactory.produceConnection();
		ArgumentCaptor<Model> modelCapture = ArgumentCaptor.forClass(Model.class);

		initTestData(connection);
		mockRDBData(dataSource, false);
		patch.execute(null);

		Mockito.verify(connection, Mockito.times(2)).remove(modelCapture.capture(), Matchers.any());
		Model model = modelCapture.getValue();
		Assert.assertFalse(model.isEmpty());
	}

	/**
	 * Tests the patch with broken data. There are duplicate roles in the semantic repository but no data in the
	 * Relation database
	 * 
	 * @throws Exception
	 *             If an error occurs
	 */
	@Test
	public void executeWithResultsInSemanticRepositoryAndNotInRDB() throws Exception {
		RepositoryConnection connection = connectionFactory.produceConnection();

		initTestData(connection);
		patch.execute(null);

		Mockito.verify(connection, Mockito.times(2)).remove(Matchers.any(Model.class), Matchers.any());
		Mockito.verify(connection, Mockito.times(1)).add(Matchers.any(Model.class), Matchers.any());
	}

	private static void mockRDBData(DataSource dataSource, boolean returnEmptyResult) throws Exception {
		Connection connection = Mockito.mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.prepareStatement(Matchers.anyString())).then(new Answer<PreparedStatement>() {

			@Override
			public PreparedStatement answer(InvocationOnMock invocation) throws Throwable {
				String query = invocation.getArgumentAt(0, String.class);
				String[] split = query.split("'");
				String instance = split[1];
				String user = split[3];

				ResultSet resultSet = Mockito.mock(ResultSet.class);
				when(resultSet.next()).thenReturn(!returnEmptyResult);
				if (!returnEmptyResult) {
					when(resultSet.getString(Matchers.eq("role")))
							.thenReturn(INSTANCE_ROLE_TEST_DATA.get(instance + user));
				}
				PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
				when(preparedStatement.executeQuery()).thenReturn(resultSet);
				return preparedStatement;
			}
		});
	}

	private static void initTestData(RepositoryConnection connection) throws Exception {
		InputStream inputStream = new FileInputStream(
				"src/test/resources/testData/getDuplicateInstancesAndAssigneeTestData.trig");
		connection.begin();
		connection.add(inputStream, "http://example.org#", RDFFormat.TRIG);
		connection.commit();
	}
}
