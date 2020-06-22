package com.sirma.itt.seip.instance.headers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.instance.batch.BatchService;
import com.sirma.sep.instance.batch.StreamBatchRequest;

/**
 * Test for {@link InstanceHeaderServiceImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
public class InstanceHeaderServiceImplTest {

	@InjectMocks
	private InstanceHeaderServiceImpl headerService;

	@Mock
	private InstanceHeaderDao headerDao;
	@Mock
	private SchedulerService schedulerService;

	@Mock
	private BatchService batchService;
	@Mock
	private SearchService searchService;

	@Mock
	private ExpressionsManager expressionsManager;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private SystemConfiguration systemConfiguration;


	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(new DefaultSchedulerConfiguration());
		when(systemConfiguration.getSystemLanguage()).thenReturn("en");
	}

	@Test
	public void trackHeader_shouldRegisterNewIfNotExists() throws Exception {
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.empty());
		headerService.trackHeader("definition", "someHeader");
		ArgumentCaptor<HeaderEntity> captor = ArgumentCaptor.forClass(HeaderEntity.class);
		verify(headerDao).persist(captor.capture());
		HeaderEntity entity = captor.getValue();
		assertEquals("someHeader", entity.getHeader());
		assertEquals("definition", entity.getDefinitionId());
	}

	@Test
	public void trackHeader_shouldUpdateEntityIfExists() throws Exception {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("oldHeader");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		headerService.trackHeader("definition", "someHeader");
		ArgumentCaptor<HeaderEntity> captor = ArgumentCaptor.forClass(HeaderEntity.class);
		verify(headerDao).persist(captor.capture());
		HeaderEntity entity = captor.getValue();
		assertEquals("someHeader", entity.getHeader());
		assertEquals("definition", entity.getDefinitionId());
	}

	@Test
	public void trackHeader_shouldTriggerReindexing_ifLabelsAreDifferent() throws Exception {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("oldHeader");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		headerService.trackHeader("definition", "someHeader");
		verify(schedulerService).schedule(eq(InstanceHeaderReindexingAction.NAME), any(), any());
	}

	@Test
	public void trackHeader_shouldNotTriggerReindexing_ifLabelsDifferByWhitespaceOnly() throws Exception {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("   old  \n Header   ");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		headerService.trackHeader("definition", "\nold\nHeader\n");
		verify(schedulerService, never()).schedule(eq(InstanceHeaderReindexingAction.NAME), any(), any());
	}

	@Test
	public void trackHeader_shouldNotTriggerReindexing_ifLabelsDifferByHtmlCommentsOnly() throws Exception {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("old  <!-- some comment here --> Header");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		headerService.trackHeader("definition", "old Header");
		verify(schedulerService, never()).schedule(eq(InstanceHeaderReindexingAction.NAME), any(), any());
	}

	@Test
	public void getHeader() throws Exception {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("oldHeader");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));

		Optional<String> header = headerService.getHeader("definition");
		assertTrue(header.isPresent());
		assertEquals("oldHeader", header.get());
	}

	@Test
	public void reindexDefinition_shouldTriggerBatchReindexing() {
		headerService.reindexDefinition("definitionId");

		verify(batchService).execute(any(StreamBatchRequest.class));
	}

	@Test
	public void evaluateHeader_shouldDoNothing_forNotRegisteredHeader() {
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.empty());

		Instance instance = new EmfInstance("emf:instance");
		instance.setIdentifier("definition");

		Optional<String> header = headerService.evaluateHeader(instance);
		assertNotNull(header);
		assertFalse(header.isPresent());
	}

	@Test
	public void evaluateHeader_shouldDoNothing_forEmptyHeader() {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));

		Instance instance = new EmfInstance("emf:instance");
		instance.setIdentifier("definition");

		Optional<String> header = headerService.evaluateHeader(instance);
		assertNotNull(header);
		assertFalse(header.isPresent());
	}

	@Test
	public void evaluateHeader_shouldDoNothing_headerNotAnExpression() {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("some static text");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		when(expressionsManager.isExpression(any())).thenReturn(Boolean.FALSE);

		Instance instance = new EmfInstance("emf:instance");
		instance.setIdentifier("definition");

		Optional<String> header = headerService.evaluateHeader(instance);
		assertNotNull(header);
		assertFalse(header.isPresent());
	}

	@Test
	public void evaluateHeader_shouldEvaluateHeaderLabel() {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("${get([title])}");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		when(expressionsManager.isExpression("${get([title])}")).thenReturn(Boolean.TRUE);

		DefinitionModel model = mock(DefinitionModel.class);
		PropertyDefinition headerDef = mock(PropertyDefinition.class, withSettings().serializable());
		when(model.getField(DefaultProperties.HEADER_LABEL)).thenReturn(Optional.of(headerDef));

		Instance instance = new EmfInstance("emf:instance");
		instance.setIdentifier("definition");
		when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		when(expressionsManager.evaluateRule(eq("${get([title])}"), eq(String.class), any(), eq(instance))).thenReturn("Instance title");

		Optional<String> header = headerService.evaluateHeader(instance);
		assertNotNull(header);
		assertTrue(header.isPresent());
		assertEquals("Instance title", header.get());
	}

	@Test
	public void evaluateHeader_shouldReturnEmptyIfEvaluatesToEmptyHeader() {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("${get([title])}");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		when(expressionsManager.isExpression("${get([title])}")).thenReturn(Boolean.TRUE);

		DefinitionModel model = mock(DefinitionModel.class);
		PropertyDefinition headerDef = mock(PropertyDefinition.class, withSettings().serializable());
		when(model.getField(DefaultProperties.HEADER_LABEL)).thenReturn(Optional.of(headerDef));

		Instance instance = new EmfInstance("emf:instance");
		instance.setIdentifier("definition");
		when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		when(expressionsManager.evaluateRule(eq("${get([title])}"), eq(String.class), any(), eq(instance))).thenReturn("");

		Optional<String> header = headerService.evaluateHeader(instance);
		assertNotNull(header);
		assertFalse(header.isPresent());
	}

	@Test
	public void evaluateHeader_shouldHandleDoubleExpression() {
		HeaderEntity currentEntity = new HeaderEntity();
		currentEntity.setDefinitionId("definition");
		currentEntity.setHeader("${eval(#{get([title])})}");
		when(headerDao.findByDefinitionId("definition")).thenReturn(Optional.of(currentEntity));
		when(expressionsManager.isExpression("${eval(#{get([title])})}")).thenReturn(Boolean.TRUE);
		when(expressionsManager.isExpression("#{get([title])}")).thenReturn(Boolean.TRUE);

		DefinitionModel model = mock(DefinitionModel.class);
		PropertyDefinition headerDef = mock(PropertyDefinition.class, withSettings().serializable());
		when(model.getField(DefaultProperties.HEADER_LABEL)).thenReturn(Optional.of(headerDef));

		Instance instance = new EmfInstance("emf:instance");
		instance.setIdentifier("definition");
		when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		when(expressionsManager.evaluateRule(eq("${eval(#{get([title])})}"), eq(String.class), any(), eq(instance))).thenReturn("#{get([title])}");
		when(expressionsManager.evaluateRule(eq("#{get([title])}"), eq(String.class), any(), eq(instance))).thenReturn("Instance title");

		Optional<String> header = headerService.evaluateHeader(instance);
		assertNotNull(header);
		assertTrue(header.isPresent());
		assertEquals("Instance title", header.get());
	}
}
