package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.APPROVE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.DELETE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;
import static com.sirma.itt.seip.testutil.CustomMatcher.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.archive.ArchiveService;
import com.sirma.itt.seip.instance.event.EmptyInstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.OperationExecutedEvent;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
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
	private Validator validatorService;

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private EventService eventService;

	@Mock
	private ArchiveService archiveService;

	@Mock
	private InstanceDao instanceDao;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Before
	public void setup() {
		service = new InstanceServiceImpl();
		MockitoAnnotations.initMocks(this);

		when(serviceRegistry.getEventProvider(any(Instance.class))).then(a -> {
			return new EmptyInstanceEventProvider<>();
		});
	}

	@Test
	public void save_createOperation() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(CREATE, true);

		service.save(instance, operation);

		verify(validatorService).validate(argThat(validationContextMatcher(instance, operation)));
		verify(eventService, times(4)).fire(any());
		verify(eventService, times(1)).fireNextPhase(any());
	}

	@Test
	public void save_editOperation() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(EDIT_DETAILS, true);

		service.save(instance, operation);

		verify(validatorService).validate(argThat(validationContextMatcher(instance, operation)));
		verify(eventService, times(4)).fire(any());
		verify(eventService, times(1)).fireNextPhase(any());
	}

	@Test
	public void save_approveOperation() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(APPROVE, true);

		service.save(instance, operation);

		verify(validatorService).validate(argThat(validationContextMatcher(instance, operation)));
		verify(eventService, times(4)).fire(any());
		verify(eventService, times(1)).fireNextPhase(any());
	}

	private static CustomMatcher<ValidationContext> validationContextMatcher(Instance instance, Operation operation) {
		return of((ValidationContext context) -> {
			assertSame(instance, context.getInstance());
			assertSame(operation, context.getOperation());
		});
	}

	@Test
	public void delete() {
		Instance instance = new EmfInstance();
		Operation operation = new Operation(DELETE, true);

		service.delete(instance, operation, false);

		verify(archiveService).scheduleDelete(instance, operation, false);
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
		when(dictionaryService.getInstanceDefinition(any(Instance.class))).thenReturn(null);
		service.clone(new EmfInstance(), new Operation());
		verifyZeroInteractions(eventService);
	}

	@Test
	public void clone_successful() {
		DefinitionMock definition = new DefinitionMock();
		definition.setFields(Arrays.asList(buildUriField(NAME), buildUriField(TYPE)));
		when(dictionaryService.getInstanceDefinition(any(Instance.class))).thenReturn(definition);
		when(instanceDao.createInstance(any(DefinitionModel.class), eq(true))).thenReturn(new EmfInstance());
		when(semanticDefinitionService.getRelationsMap()).thenReturn(new HashMap<>(0));

		Instance instanceToClone = new EmfInstance();
		instanceToClone.add(TYPE, "instance-type");
		instanceToClone.add(NAME, "Darkness");
		instanceToClone.add(TITLE, "Bane");
		Instance clone = service.clone(instanceToClone, new Operation());

		assertEquals(1, clone.getProperties().size());
		assertEquals("Bane", clone.getString(TITLE));
		verify(eventService, atLeastOnce()).fire(any(OperationExecutedEvent.class));
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
		when(dictionaryService.getInstanceDefinition(any(Instance.class))).thenReturn(null);
		service.deepClone(new EmfInstance(), new Operation());
		verifyZeroInteractions(eventService);
	}

	@Test
	public void deepClone_successful() {
		DefinitionMock definition = new DefinitionMock();
		definition.setFields(Arrays.asList(buildUriField(NAME), buildUriField(TYPE)));
		when(dictionaryService.getInstanceDefinition(any(Instance.class))).thenReturn(definition);
		when(instanceDao.createInstance(any(DefinitionModel.class), eq(true))).thenReturn(new EmfInstance());
		when(semanticDefinitionService.getRelationsMap()).thenReturn(new HashMap<>(0));

		Instance instanceToClone = new EmfInstance();
		instanceToClone.add(TYPE, "batman-type");
		instanceToClone.add(NAME, "Darkness");
		instanceToClone.add(TITLE, "Batman");
		Instance clone = service.deepClone(instanceToClone, new Operation());

		assertEquals(3, clone.getProperties().size());
		assertEquals("Batman", clone.getString(TITLE));

		verify(eventService, atLeastOnce()).fire(any(OperationExecutedEvent.class));
	}

	private static PropertyDefinition buildUriField(String identifier) {
		PropertyDefinitionMock type = new PropertyDefinitionMock();
		type.setIdentifier(identifier);
		type.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		return type;
	}

}
