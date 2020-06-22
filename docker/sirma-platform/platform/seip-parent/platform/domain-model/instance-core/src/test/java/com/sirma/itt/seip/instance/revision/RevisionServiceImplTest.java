package com.sirma.itt.seip.instance.revision;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.REVISION_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.UriConverterProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.steps.PublishStepRunner;
import com.sirma.itt.seip.instance.revision.steps.PublishStepRunner.StepRunner;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link RevisionServiceImpl}.
 *
 * @author BBonev
 */
public class RevisionServiceImplTest extends EmfTest {

	@InjectMocks
	private RevisionServiceImpl service;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private EventService eventService;

	@Mock
	private LinkService linkService;

	@Mock
	private StateService stateService;

	@Mock
	private SearchService searchService;

	@Mock
	private User authenticated;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private PublishStepRunner stepRunner;

	@Mock
	private InstanceService objectService;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	private TypeConverter typeConverter;
	EmfInstance instanceToPublish;

	@Before
	public void setup() {
		typeConverter = spy(createTypeConverter());

		MockitoAnnotations.initMocks(this);

		LinkConstants.init(mock(SecurityContextManager.class), ContextualMap.create());

		instanceToPublish = (EmfInstance) createInstance("emf:id");
		instanceToPublish.getProperties().put(DefaultProperties.STATUS, "APPROVED");

		when(objectService.clone(any(), any())).then(a -> {
			Instance instance = a.getArgumentAt(0, Instance.class);
			Instance clone = new EmfInstance();
			clone.addAllProperties(instance.getOrCreateProperties());
			clone.setId(instance.getId());
			return clone;
		});

		when(objectService.loadByDbId(Matchers.any(Serializable.class))).thenReturn(new EmfInstance());
		when(securityContext.getAuthenticated()).thenReturn(authenticated);
		when(idManager.getRevisionId(any(), anyString())).thenCallRealMethod();
		when(stepRunner.getRunner(any(String[].class))).thenReturn(mock(StepRunner.class));
	}

	private static Instance createInstance(Serializable id) {
		Instance revision = new EmfInstance();
		revision.setId(id);
		revision.setProperties(new HashMap<String, Serializable>());
		return revision;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testPublishApproved() {
		DefinitionMock definition = new DefinitionMock();
		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);
		String definitionId = "revision-context-definition-id";
		PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
		propertyDefinition.setName(RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME);
		propertyDefinition.setValue("{\"recordContextDefinitionId\": \"" + definitionId + "\", \"recordContextName\": \"Revision context name\"}");
		definition.setConfigurations(Collections.singletonList(propertyDefinition));

		Instance revisionContext = createRevisionContext("emf:RevisionContextId", definitionId);

		SearchArguments<? extends Instance> arguments = new SearchArguments<>();
		when(searchService.getFilter(eq("customQueries/getRevisionsWithDifferentStatus"), eq(SearchInstance.class),
				any(Context.class))).thenReturn(arguments);
		doAnswer(a -> {
			SearchArguments<Instance> args = a.getArgumentAt(1, SearchArguments.class);
			args.setResult(Collections.singletonList(new EmfInstance("instance-id-r1.0")));
			return args;
		}).when(searchService).searchAndLoad(eq(Instance.class), eq(arguments));
		when(searchService.stream(any(), any())).then(a -> Stream.empty());

		Operation operation = new Operation(ActionTypeConstants.APPROVE_AND_PUBLISH);
		Instance revision = service.publish(instanceToPublish, operation);
		assertNotNull(revision);
		assertEquals("emf:id-r1.0", revision.getId());
		// this is without children and one old revision which status should be changed to obsolete
		verify(domainInstanceService, times(3)).save(argThat(instanceSaveContextMatcher(Collections.singletonList("instance-id-r1.0"))));
		assertEquals(revisionContext.getId(), revision.get(InstanceContextService.HAS_PARENT));
		verify(contextService).bindContext(eq(revision), eq(revisionContext));
	}

	/**
	 * Asserts if validation is disabled for oldest revision.
	 *
	 * @param revisionIds - collections with oldest revision ids.
	 * @return
	 */
	private CustomMatcher<InstanceSaveContext> instanceSaveContextMatcher(Collection<String> revisionIds) {
		return CustomMatcher.of(instanceSaveContext -> {
			Instance instance = instanceSaveContext.getInstance();
			if (revisionIds.contains(instance.getId())) {
				assertNotNull(instanceSaveContext.getDisableValidationReason());
			} else {
				assertNull(instanceSaveContext.getDisableValidationReason());
			}
		});
	}

	@Test
	public void testPublishRejected() {
		when(searchService.stream(any(), any())).then(a -> Stream.empty());
		DefinitionMock definition = new DefinitionMock();
		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);
		Operation operation = new Operation(ActionTypeConstants.REJECT_AND_PUBLISH);
		Instance revision = service.publish(instanceToPublish, operation);
		assertNotNull(revision);
		assertEquals("emf:id-r1.0", revision.getId());
		// this is without children
		verify(domainInstanceService, times(2)).save(any(InstanceSaveContext.class));
	}

	@Test
	public void isRevision_FalseWhenNotSpecifiedType() throws Exception {
		assertFalse(service.isRevision(new EmfInstance()));
	}

	@Test
	public void isRevision_FalseWhenCurrent() throws Exception {
		Instance instance = new EmfInstance();
		instance.add(REVISION_TYPE, "emf:current");
		assertFalse(service.isRevision(instance));
	}

	@Test
	public void isRevision_TrueWhenEmfRevision() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add(REVISION_TYPE, "emf:revision");
		assertTrue(service.isRevision(instance));
	}

	@Test
	public void isRevision_WhenNotStringConvertToTheSameType() throws Exception {
		Instance instance = new EmfInstance();
		instance.add(REVISION_TYPE, new UriConverterProvider.StringUriProxy(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#current"));
		when(typeConverter.convert(eq(Uri.class), any())).then(a -> a.getArgumentAt(1, Uri.class));
		assertFalse(service.isRevision(instance));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getRevisions_queryNotFound_noResults(){
		when(searchService.getFilter(eq("customQueries/getRevisions"), eq(SearchInstance.class), any(Context.class)))
				.thenReturn(null);
		Collection<Instance> result = service.getRevisions(InstanceReferenceMock.createGeneric("instance-id"), false);
		assertTrue(result.isEmpty());
	}

	private Instance createRevisionContext(String revisionContextId, String definitionId) {
		String recordSpaceShortUri = "emf:RecordSpace";
		Instance revisionContext = createInstance(revisionContextId);
		DefinitionMock revisionContextDefinition = new DefinitionMock();
		PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
		propertyDefinition.setName(DefaultProperties.SEMANTIC_TYPE);
		propertyDefinition.setValue(EMF.RECORD_SPACE.toString());
		revisionContextDefinition.setFields(Collections.singletonList(propertyDefinition));
		Mockito.when(namespaceRegistryService.getShortUri(EMF.RECORD_SPACE.toString())).thenReturn(recordSpaceShortUri);
		Mockito.when(definitionService.find(definitionId)).thenReturn(revisionContextDefinition);
		doAnswer(invocation -> {
			invocation.getArgumentAt(1, SearchArguments.class).setResult(Collections.singletonList(revisionContext));
			return null;
		}).when(searchService).searchAndLoad(eq(Instance.class), argThat(CustomMatcher.of((searchArguments) -> {
			return recordSpaceShortUri.equals(searchArguments.getArguments()
													  .get(RevisionServiceImpl.RECORD_SPACE_QUERY_PARAMETER));
		})));
		return revisionContext;
	}
}
