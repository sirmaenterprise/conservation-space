package com.sirma.itt.emf.semantic.patch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests for {@link RemoveDuplicateTitlesOfGroups}.
 *
 * @author smustafov
 */
public class RemoveDuplicateTitlesOfGroupsTest {

	private static final String ASSERT_FAILURE_MESSAGE = "The number of groups with duplicate title is different from expected";

	@InjectMocks
	private RemoveDuplicateTitlesOfGroups patch;

	@Mock
	private ResourceService resourceService;
	@Mock
	private NamespaceRegistryService namespaceRegistry;
	@Mock
	private ConnectionFactory connectionFactory;

	private static final Map<String, String> EXPECTED_TEST_DATA = new HashMap<>();

	static {
		EXPECTED_TEST_DATA.put("http://www.sirma.com/ontologies/2014/11/security#SYSTEM_ALL_OTHER_USERS",
				"SYSTEM_ALL_OTHER_USERS");
		EXPECTED_TEST_DATA.put(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#GROUP_SYSTEM_ADMINISTRATORS",
				"SYSTEM_ADMINISTRATORS");
		EXPECTED_TEST_DATA.put("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#GROUP_Group_1",
				"GROUP_Group_1");
		EXPECTED_TEST_DATA.put("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#GROUP_support",
				"support");
	}

	@Before
	public void beforeEach() {
		initMocks(this);

		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection connection = spy(repository.getConnection());
		when(connectionFactory.produceConnection()).thenReturn(connection);

		when(namespaceRegistry.getShortUri(any(IRI.class))).then(answer -> {
			IRI uri = answer.getArgumentAt(0, IRI.class);
			return EMF.PREFIX + SPARQLQueryHelper.URI_SEPARATOR + uri.getLocalName();
		});

		mockResourceService();
	}

	@Test
	public void should_DoNothing_When_NoData() throws Exception {
		RepositoryConnection connection = connectionFactory.produceConnection();

		patch.execute(null);

		verify(connection, never()).remove(any(Model.class), any());
	}

	@Test
	public void should_RemoveDuplicateTitlesOfGroups() throws Exception {
		RepositoryConnection connection = connectionFactory.produceConnection();
		initTestData(connection);

		patch.execute(null);

		ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
		verify(connection).remove(modelCaptor.capture(), any());

		Model removeModel = modelCaptor.getValue();
		assertEquals(ASSERT_FAILURE_MESSAGE, 4, removeModel.size());

		verifyStatementsForRemove(removeModel);
	}

	private static void verifyStatementsForRemove(Model model) {
		for (Iterator<Statement> it = model.iterator(); it.hasNext();) {
			Statement statement = it.next();
			String duplicateTitle = EXPECTED_TEST_DATA.get(statement.getSubject().stringValue());
			assertEquals(duplicateTitle, statement.getObject().stringValue());
		}
	}

	private void mockResourceService() {
		when(resourceService.getResource("emf:SYSTEM_ALL_OTHER_USERS"))
				.thenReturn(buildGroup(ResourceService.EVERYONE_GROUP_DISPLAY_NAME));

		when(resourceService.getResource("emf:GROUP_SYSTEM_ADMINISTRATORS"))
				.thenReturn(buildGroup(ResourceService.SYSTEM_ADMIN_GROUP_DISPLAY_NAME));

		when(resourceService.getResource("emf:GROUP_Group_1")).thenReturn(buildGroup("One renamed group"));

		when(resourceService.getResource("emf:GROUP_support")).thenReturn(buildGroup("Support team"));
	}

	private static Resource buildGroup(String displayName) {
		EmfGroup group = new EmfGroup();
		group.setDisplayName(displayName);
		return group;
	}

	private void initTestData(RepositoryConnection connection) throws Exception {
		try (InputStream inputStream = getClass()
				.getResourceAsStream("/testData/duplicateTitlesOfGroupsTestData.trig")) {
			connection.begin();
			connection.add(inputStream, "http://example.org#", RDFFormat.TRIG);
			connection.commit();
		}
	}

}
