package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.APPROVE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.DELETE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.archive.ArchiveService;
import com.sirma.itt.seip.instance.event.EmptyInstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Tests for {@link InstanceServiceImpl}.
 *
 * @author A. Kunchev
 */
public class InstanceServiceImplTest {

	@InjectMocks
	private InstanceService service;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private EventService eventService;

	@Mock
	private ArchiveService archiveService;

	@Mock
	private InstanceDao instanceDao;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Spy
	private InstanceContextServiceMock contextService;

	@Mock
	private StateService stateService;
	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		service = new InstanceServiceImpl();
		MockitoAnnotations.initMocks(this);
		when(serviceRegistry.getEventProvider(any(Instance.class))).then(a -> new EmptyInstanceEventProvider<>());
	}

	@Test
	public void save_createOperation() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(CREATE, true);

		service.save(instance, operation);

		verify(eventService, times(3)).fire(any());
		verify(eventService, times(1)).fireNextPhase(any());
		verify(stateService).changeState(instance, operation);
	}

	@Test
	public void save_editOperation() {
		Operation operation = new Operation(EDIT_DETAILS, true);
		Instance instance = new EmfInstance();

		service.save(instance, operation);

		verify(eventService, times(3)).fire(any());
		verify(eventService, times(1)).fireNextPhase(any());
	}

	@Test
	public void save_approveOperation() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(APPROVE, true);

		service.save(instance, operation);

		verify(eventService, times(3)).fire(any());
		verify(eventService, times(1)).fireNextPhase(any());
	}

	@Test
	public void delete() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(DELETE, true);
		List<String> deleteResult = Arrays.asList("instance-id", "child-id");
		when(archiveService.scheduleDelete(instance, operation, false)).thenReturn(new ArchiveService.InstanceDeleteResult(deleteResult, Collections.emptyList()));
		Collection<String> deleted = service.delete(instance, operation, false);

		assertNotNull(deleted);
		verify(archiveService).scheduleDelete(instance, operation, false);
		verify(instanceDao).touchInstance(deleteResult);
	}

	@Test
	public void loadDeleted() throws Exception {
		when(instanceDao.loadInstance("emf:instance", null, true)).thenReturn(new EmfInstance());
		Optional<Instance> instance = service.loadDeleted("emf:instance");
		assertNotNull(instance);
		assertTrue(instance.isPresent());
		verify(instanceLoadDecorator).decorateInstance(any());
	}

	@Test
	public void loadDeleted_notFound() throws Exception {
		when(instanceDao.loadInstance("emf:instance", null, true)).thenReturn(null);
		Optional<Instance> instance = service.loadDeleted("emf:instance");
		assertNotNull(instance);
		assertFalse(instance.isPresent());
		verify(instanceLoadDecorator).decorateInstance(any());
	}

	@Test(expected = NullPointerException.class)
	public void clone_nullInstanceToClone_NPE() {
		service.clone(null, new Operation());
	}

	@Test(expected = NullPointerException.class)
	public void clone_nullOperation_NPE() {
		service.clone(new EmfInstance(), null);
	}

	@Test(expected = NullPointerException.class)
	public void clone_nullDefinition() {
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(null);
		service.clone(new EmfInstance(), new Operation());
		verifyZeroInteractions(eventService);
	}

	@Test
	public void clone_successful() {
		DefinitionMock definition = new DefinitionMock();
		definition.setFields(Arrays.asList(buildUriField(NAME), buildUriField(TYPE)));
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(definition);
		when(instanceDao.createInstance(any(DefinitionModel.class), eq(true))).thenReturn(new EmfInstance());
		when(semanticDefinitionService.getRelationsMap())
				.thenReturn(Collections.singletonMap(SEMANTIC_TYPE, new PropertyInstance()));

		Instance instanceToClone = new EmfInstance();
		instanceToClone.add(TYPE, "instance-type");
		instanceToClone.add(NAME, "Darkness");
		instanceToClone.add(TITLE, "Bane");
		instanceToClone.add(SEMANTIC_TYPE, "Evil");
		Operation operation = new Operation();
		Instance clone = service.clone(instanceToClone, operation);

		assertEquals(2, clone.getProperties().size());
		assertEquals("Bane", clone.getString(TITLE));
		assertEquals("Evil", clone.getString(SEMANTIC_TYPE));
		verify(stateService, times(2)).changeState(instanceToClone, operation);
	}

	@Test(expected = NullPointerException.class)
	public void deepClone_nullInstanceToClone_NPE() {
		service.deepClone(null, new Operation());
	}

	@Test(expected = NullPointerException.class)
	public void deepClone_nullOperation_NPE() {
		service.deepClone(new EmfInstance(), null);
	}

	@Test(expected = NullPointerException.class)
	public void deepClone_nullDefinition() {
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(null);
		service.deepClone(new EmfInstance(), new Operation());
		verifyZeroInteractions(eventService);
	}

	@Test
	public void deepClone_successful() {
		DefinitionMock definition = new DefinitionMock();
		definition.setFields(Arrays.asList(buildUriField(NAME), buildUriField(TYPE)));
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(definition);
		when(instanceDao.createInstance(any(DefinitionModel.class), eq(true))).thenReturn(new EmfInstance());
		when(semanticDefinitionService.getRelationsMap()).thenReturn(new HashMap<>(0));

		Instance instanceToClone = new EmfInstance();
		instanceToClone.add(TYPE, "batman-type");
		instanceToClone.add(NAME, "Darkness");
		instanceToClone.add(TITLE, "Batman");
		Operation operation = new Operation();
		Instance clone = service.deepClone(instanceToClone, operation);

		assertEquals(3, clone.getProperties().size());
		assertEquals("Batman", clone.getString(TITLE));

		verify(stateService, times(2)).changeState(instanceToClone, operation);
	}

	private static PropertyDefinition buildUriField(String identifier) {
		PropertyDefinitionMock type = new PropertyDefinitionMock();
		type.setIdentifier(identifier);
		type.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		return type;
	}

	@Test(expected = NullPointerException.class)
	public void createInstance_nullDefinition() {
		service.createInstance(null, null);
	}

	@Test
	public void createInstance_withEventProvider_withoutParent() {
		Instance instance = InstanceReferenceMock.createGeneric("instance").toInstance();
		when(instanceDao.createInstance(any(DefinitionModel.class), eq(true))).thenReturn(instance);
		service.createInstance(new DefinitionMock(), null);
		verify(eventService, times(1)).fire(any());
		verify(instanceDao).synchRevisions(any(Instance.class), any());
		verify(stateService).changeState(any(), any());
		assertFalse(contextService.getContext(instance).isPresent());
	}

	@Test
	public void createInstance_withEventProvider_withParent() {
		Instance instance = InstanceReferenceMock.createGeneric("instance").toInstance();
		when(instanceDao.createInstance(any(DefinitionModel.class), eq(true))).thenReturn(instance);
		EmfInstance parent = new EmfInstance();
		parent.setId("parent-instance-id");
		service.createInstance(new DefinitionMock(), parent);
		verify(eventService, times(1)).fire(any());
		verify(stateService).changeState(any(), any());
		verify(instanceDao).synchRevisions(any(Instance.class), any());
		assertTrue(contextService.getContext(instance).isPresent());
	}

	@Test
	public void exist_instanceDaoCalled() {
		Set<String> identifiers = Collections.singleton("instance-id");
		service.exist(identifiers);
		verify(instanceDao).exist(eq(identifiers), eq(false));
	}
}
