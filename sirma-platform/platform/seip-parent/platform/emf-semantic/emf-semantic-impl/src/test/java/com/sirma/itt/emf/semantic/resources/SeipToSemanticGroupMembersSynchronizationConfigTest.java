package com.sirma.itt.emf.semantic.resources;

import static com.sirma.itt.seip.collections.CollectionUtils.addValueToSetMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesReadConverter;
import com.sirma.itt.emf.semantic.resources.SeipToSemanticGroupMembersSynchronizationConfig.GroupInfo;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Test for {@link SeipToSemanticGroupMembersSynchronizationConfig}
 *
 * @author BBonev
 */
public class SeipToSemanticGroupMembersSynchronizationConfigTest {

	private static final Group GROUP_1 = buildGroup("group1");
	private static final Group GROUP_2 = buildGroup("group2");
	private static final Group GROUP_3 = buildGroup("group3");
	private static final Group GROUP_4 = buildGroup("group4");

	@InjectMocks
	private SeipToSemanticGroupMembersSynchronizationConfig config;

	@Mock
	private ResourceService resourceService;
	@Mock
	private DbDao dbDao;
	@Mock
	private RepositoryConnection transactionalRepositoryConnection;
	@Mock
	private NamespaceRegistryService registryService;
	@Mock
	private HashCalculator hashCalculator;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private SemanticPropertiesReadConverter propertiesConverter;
	@Spy
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Mock
	private TupleQuery tupleQuery;
	@Mock
	private TupleQueryResult queryResult;

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(registryService.buildUri(anyString())).then(a -> valueFactory.createIRI(a.getArgumentAt(0, String.class)));
		when(registryService.getDataGraph()).thenReturn(EMF.DATA_CONTEXT);
		when(hashCalculator.equalsByHash(any(), any()))
				.then(a -> a.getArgumentAt(0, Object.class).equals(a.getArgumentAt(1, Object.class)));

		when(transactionalRepositoryConnection.prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString()))
				.thenReturn(tupleQuery);

		when(resourceService.getAllResources(ResourceType.GROUP, null))
				.thenReturn(Arrays.asList(GROUP_1, GROUP_2, GROUP_3, GROUP_4));

		when(resourceService.getAllOtherUsers())
				.thenReturn(buildGroup(Security.SYSTEM_ALL_OTHER_USERS.getLocalName()));
	}

	@Test
	public void runSynchronization() throws Exception {
		when(tupleQuery.evaluate()).thenReturn(queryResult);
		// current local state of the mapping
		Map<Value, Map<String, Set<Value>>> resultMap = new HashMap<>();
		addMembers(resultMap, GROUP_1, buildGroup("1"), buildGroup("2"), buildGroup("3"));
		addMembers(resultMap, GROUP_2, buildGroup("4"), buildGroup("2"), buildGroup("5"));
		addMembers(resultMap, GROUP_3, buildGroup("5"), buildGroup("6"));
		addMembers(resultMap, GROUP_4, buildGroup("8"));
		when(propertiesConverter.buildQueryResultModel(any(TupleQueryResultIterator.class), eq("uri"))).thenReturn(resultMap);

		// incoming changes
		when(resourceService.getContainedResources(GROUP_1.getId()))
				.thenReturn(Arrays.asList(buildGroup("1"), buildGroup("2"), buildGroup("3")));
		when(resourceService.getContainedResources(GROUP_2.getId()))
				.thenReturn(Arrays.asList(buildGroup("2"), buildGroup("3")));
		when(resourceService.getContainedResources(GROUP_3.getId()))
				.thenReturn(Arrays.asList(buildGroup("4"), buildGroup("5"), buildGroup("6"), buildGroup("7")));
		when(resourceService.getContainedResources(GROUP_4.getId())).thenReturn(Collections.emptyList());
		when(resourceService.getAllResources(ResourceType.USER, null))
				.thenReturn(Arrays.asList(buildUser("regularuser")));
		// current local state of the mapping

		SynchronizationRunner.synchronize(config);

		verify(transactionalRepositoryConnection).remove(argThat(CustomMatcher.of((Model model) -> {
			Set<Pair<Resource, Value>> values = model
					.stream()
						.map(Pair.from(Statement::getSubject, Statement::getObject))
						.collect(Collectors.toSet());

			assertEquals(asSet(new Pair<>(buildUri(GROUP_4), buildUri(buildGroup("8"))),
					new Pair<>(buildUri(buildGroup("8")), buildUri(GROUP_4)),
					new Pair<>(buildUri(GROUP_2), buildUri(buildGroup("4"))),
					new Pair<>(buildUri(buildGroup("4")), buildUri(GROUP_2)),
					new Pair<>(buildUri(GROUP_2), buildUri(buildGroup("5"))),
					new Pair<>(buildUri(buildGroup("5")), buildUri(GROUP_2))), values);
		})), any(Resource.class));
		verify(transactionalRepositoryConnection).add(argThat(CustomMatcher.of((Model model) -> {
			Set<Pair<Resource, Value>> values = model
					.stream()
						.map(Pair.from(Statement::getSubject, Statement::getObject))
						.collect(Collectors.toSet());

			assertEquals(asSet(new Pair<>(buildUri(GROUP_3), buildUri(buildGroup("4"))),
					new Pair<>(buildUri(buildGroup("4")), buildUri(GROUP_3)),
					new Pair<>(buildUri(GROUP_3), buildUri(buildGroup("7"))),
					new Pair<>(buildUri(buildGroup("7")), buildUri(GROUP_3)),
					new Pair<>(buildUri(GROUP_2), buildUri(buildGroup("3"))),
					new Pair<>(buildUri(buildGroup("3")), buildUri(GROUP_2))), values);
		})), eq(EMF.DATA_CONTEXT));
	}

	@Test
	public void should_modifyAll_whenForceSynchronizing() throws Exception {
		when(tupleQuery.evaluate()).thenReturn(queryResult);
		// current local state of the mapping
		Map<Value, Map<String, Set<Value>>> resultMap = new HashMap<>();
		addMembers(resultMap, GROUP_1, buildGroup("1"), buildGroup("2"), buildGroup("3"));
		when(propertiesConverter.buildQueryResultModel(any(TupleQueryResultIterator.class), eq("uri"))).thenReturn(resultMap);

		// incoming changes
		when(resourceService.getContainedResources(GROUP_1.getId()))
				.thenReturn(Arrays.asList(buildGroup("1"), buildGroup("2"), buildGroup("3")));
		// current local state of the mapping

		SyncRuntimeConfiguration configuration = new SyncRuntimeConfiguration();
		configuration.enableForceSynchronization();
		SynchronizationRunner.synchronize(config, configuration);

		verify(transactionalRepositoryConnection).add(argThat(CustomMatcher.of((Model model) -> {
			Set<Pair<Resource, Value>> values = model
					.stream()
						.map(Pair.from(Statement::getSubject, Statement::getObject))
						.collect(Collectors.toSet());

			assertEquals(asSet(new Pair<>(buildUri(GROUP_1), buildUri(buildGroup("2"))),
					new Pair<>(buildUri(buildGroup("2")), buildUri(GROUP_1)),
					new Pair<>(buildUri(GROUP_1), buildUri(buildGroup("3"))),
					new Pair<>(buildUri(buildGroup("3")), buildUri(GROUP_1)),
					new Pair<>(buildUri(GROUP_1), buildUri(buildGroup("1"))),
					new Pair<>(buildUri(buildGroup("1")), buildUri(GROUP_1))), values);
		})), eq(EMF.DATA_CONTEXT));
	}

	@Test(expected = SynchronizationException.class)
	public void runSynchronization_FailToLoadSemanticData() throws Exception {
		when(tupleQuery.evaluate()).thenThrow(QueryEvaluationException.class);

		SynchronizationRunner.synchronize(config);
	}

	@Test
	public void groupInfoEquals() throws Exception {
		GroupInfo info1 = new GroupInfo();

		assertTrue(info1.equals(info1));

		GroupInfo info2 = new GroupInfo();

		assertTrue(info1.equals(info2));
		info1.members.add(valueFactory.createIRI("emf:member"));
		assertFalse(info1.equals(info2));
		info2.members.add(valueFactory.createIRI("emf:member"));
		assertTrue(info1.equals(info2));

		assertFalse(info1.equals(new Object()));
	}

	@Test
	public void groupInfoHashCode() throws Exception {
		GroupInfo info1 = new GroupInfo();

		assertEquals(info1.hashCode(), info1.hashCode());

		GroupInfo info2 = new GroupInfo();

		assertEquals(info1.hashCode(), info2.hashCode());
		info1.members.add(valueFactory.createIRI("emf:member"));
		assertNotEquals(info1.hashCode(), info2.hashCode());
		info2.members.add(valueFactory.createIRI("emf:member"));
		assertEquals(info1.hashCode(), info2.hashCode());
	}

	private void addMembers(Map<Value, Map<String, Set<Value>>> resultMap, Group base, Group... members) {
		Map<String, Set<Value>> mapping = new HashMap<>();

		for (Group group : members) {
			addValueToSetMap(mapping, "member", buildUri(group));
		}

		resultMap.put(buildUri(base), mapping);
	}

	private IRI buildUri(Group base) {
		return valueFactory.createIRI(base.getId().toString());
	}

	@SafeVarargs
	private static <E> Set<E> asSet(E... strings) {
		return new HashSet<>(Arrays.asList(strings));
	}

	private static Group buildGroup(String id) {
		EmfGroup group = new EmfGroup();
		group.setName(id);
		group.setId("emf:" + id);
		return group;
	}

	private static User buildUser(String id) {
		EmfUser user = new EmfUser();
		user.setName(id);
		user.setId("emf:" + id);
		return user;
	}
}
