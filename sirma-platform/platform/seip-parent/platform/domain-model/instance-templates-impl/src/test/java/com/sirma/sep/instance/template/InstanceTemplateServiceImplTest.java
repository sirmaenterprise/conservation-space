package com.sirma.sep.instance.template;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.template.update.InstanceTemplateUpdateItem;
import com.sirma.itt.seip.instance.template.update.InstanceTemplateUpdateJobProperties;
import com.sirma.itt.seip.instance.template.update.InstanceTemplateUpdater;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidget;
import com.sirma.sep.instance.batch.BatchRequest;
import com.sirma.sep.instance.batch.BatchService;

public class InstanceTemplateServiceImplTest {

	@InjectMocks
	private InstanceTemplateServiceImpl instanceTemplateServceImpl;

	@Mock
	private SearchService searchService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private BatchService batchService;

	@Captor
	private ArgumentCaptor<BatchRequest> batchRequestCaptor;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TemplateService templateService;

	@Mock
	private InstanceTemplateUpdater instanceTemplateUpdater;

	@Mock
	private EventService eventService;

	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Captor
	private ArgumentCaptor<String> stringCaptor;

	final String SOURCE_ID = "sourceId";

	private final String TEMPLATE_INSTANCE_ID = "template1";
	private final String TEMPLATE_VERSION = "123";
	private final String TEMPLATE_PUBLISHED_VERSION = "100";

	@Before
	public void init() {
		instanceTemplateServceImpl = new InstanceTemplateServiceImpl();
		MockitoAnnotations.initMocks(this);

		Template template = new Template();
		template.setPublishedInstanceVersion(TEMPLATE_PUBLISHED_VERSION);

		Instance templateInstance = new EmfInstance();
		Map<String, Serializable> properties = new HashMap<>(3);
		properties.put(DefaultProperties.VERSION, TEMPLATE_VERSION);
		properties.put(TemplateProperties.PUBLISHED_INSTANCE_VERSION, TEMPLATE_PUBLISHED_VERSION);
		templateInstance.setProperties(properties);

		when(templateService.getTemplate(TEMPLATE_INSTANCE_ID)).thenReturn(template);
		when(domainInstanceService.loadInstance(TEMPLATE_INSTANCE_ID)).thenReturn(templateInstance);
	}

	@Test
	public void should_ReturnPublishedTemplateVersion() {
		EmfInstance instance = new EmfInstance("1234");
		Map<String, Serializable> properties = new HashMap<>(2);
		properties.put(LinkConstants.HAS_TEMPLATE, TEMPLATE_INSTANCE_ID);
		instance.setProperties(properties);

		when(domainInstanceService.loadInstance("1234")).thenReturn(instance);
		assertEquals(TEMPLATE_PUBLISHED_VERSION, instanceTemplateServceImpl.getInstanceTemplateVersion("1234"));
	}

	@Test
	public void should_UpdateSingleInstanceViewWithTemplatePublishedVersion() {
		EmfInstance instance = new EmfInstance("1234");
		Map<String, Serializable> properties = new HashMap<>(2);
		properties.put(LinkConstants.HAS_TEMPLATE, TEMPLATE_INSTANCE_ID);
		instance.setProperties(properties);
		when(domainInstanceService.loadInstance("1234")).thenReturn(instance);

		InstanceTemplateUpdateItem updatedInstance = new InstanceTemplateUpdateItem("1234",
				"<sections><section data-id=\"section1\"></section></sections>");
		when(instanceTemplateUpdater.updateItem("1234", TEMPLATE_INSTANCE_ID)).thenReturn(updatedInstance);

		instanceTemplateServceImpl.updateInstanceView("1234");

		verify(instanceTemplateUpdater, times(1)).saveItem(updatedInstance, TEMPLATE_PUBLISHED_VERSION,
				ActionTypeConstants.UPDATE_SINGLE_INSTANCE_TEMPLATE);
	}

	@Test
	public void should_NotUpdateSingleInstanceViewWithTemplatePublishedVersion() {
		EmfInstance instance = new EmfInstance("1234");
		Map<String, Serializable> properties = new HashMap<>(2);
		properties.put(LinkConstants.HAS_TEMPLATE, TEMPLATE_INSTANCE_ID);
		instance.setProperties(properties);
		when(domainInstanceService.loadInstance("1234")).thenReturn(instance);

		InstanceTemplateUpdateItem updatedInstance = new InstanceTemplateUpdateItem("1234",
				"<sections><section data-id=\"section1\"></section></sections>");

		when(instanceTemplateUpdater.updateItem("1234", TEMPLATE_INSTANCE_ID)).thenReturn(null);

		assertNull(instanceTemplateServceImpl.updateInstanceView("1234"));

		verify(instanceTemplateUpdater, times(0)).saveItem(updatedInstance, TEMPLATE_PUBLISHED_VERSION,
				ActionTypeConstants.UPDATE_SINGLE_INSTANCE_TEMPLATE);
	}

	@Test
	public void should_StartBatchJobForInstanceViewUpdate() {
		instanceTemplateServceImpl.updateInstanceViews(TEMPLATE_INSTANCE_ID);

		verify(batchService).execute(batchRequestCaptor.capture());

		BatchRequest batchRequest = batchRequestCaptor.getValue();

		assertEquals(TEMPLATE_INSTANCE_ID,
				batchRequest.getProperties().get(InstanceTemplateUpdateJobProperties.TEMPLATE_INSTANCE_ID));
		assertEquals(TEMPLATE_PUBLISHED_VERSION,
				batchRequest.getProperties().get(InstanceTemplateUpdateJobProperties.TEMPLATE_VERSION));
	}

	@Test
	public void should_CreateTemplate_ModifyingTheTabsNotToBeUserDefined() {
		String content = "<div data-tabs-counter=\"13\"><section></section></div>";

		withSourceContent(content);
		withSourceInstance("test");

		Template template = new Template();
		instanceTemplateServceImpl.createTemplate(template, SOURCE_ID);

		verify(templateService).create(eq(template), stringCaptor.capture());

		assertTrue(stringCaptor.getValue().contains("data-user-defined=\"false\""));
	}

	@Test
	public void should_CreateTemplate_NotifyForAuditAction() {
		String content = "<div data-tabs-counter=\"13\"><section></section></div>";

		withSourceContent(content);
		withSourceInstance("test");

		Template template = new Template();
		instanceTemplateServceImpl.createTemplate(template, SOURCE_ID);

		ArgumentCaptor<AuditableEvent> eventCaptor = ArgumentCaptor.forClass(AuditableEvent.class);

		verify(eventService).fire(eventCaptor.capture());
		AuditableEvent event = eventCaptor.getValue();
		assertEquals("saveAsTemplate", event.getOperationId());
		assertEquals(SOURCE_ID, event.getInstance().getId());
	}

	@Test
	public void should_CreateTemplate_BindingTheSelectedPropertiesInObjectDataWidgetToTheNewForType()
			throws IOException {
		final String SOURCE_TYPE = "CO1002";
		final String NEW_TEMPLATE_TYPE = "newType";

		String content = IOUtils.toString(this.getClass().getResourceAsStream("view.xml"));
		withSourceContent(content);

		withSourceInstance(SOURCE_TYPE);

		verifyPropertiesAreBoundToNewType(SOURCE_TYPE, NEW_TEMPLATE_TYPE, content);
	}

	@Test
	public void should_CreateTemplate_BindingTheSelectedPropertiesToTheForTypeOfTheSource_WhenSourceInstanceIsAlsoTemplate()
			throws IOException {
		final String SOURCE_TYPE = "CO1002";
		final String NEW_TEMPLATE_TYPE = "newType";

		String content = IOUtils.toString(this.getClass().getResourceAsStream("view.xml"));
		withSourceContent(content);

		EmfInstance sourceInstance = withSourceInstance(SOURCE_TYPE);
		InstanceType type = mock(InstanceType.class);
		when(type.is(eq(ObjectTypes.TEMPLATE))).thenReturn(true);
		sourceInstance.setType(type);

		sourceInstance.add(TemplateProperties.FOR_OBJECT_TYPE, SOURCE_TYPE);

		verifyPropertiesAreBoundToNewType(SOURCE_TYPE, NEW_TEMPLATE_TYPE, content);
	}

	private void verifyPropertiesAreBoundToNewType(final String sourceType, final String newType, String content) {
		Idoc viewBeforeProcessing = Idoc.parse(content);
		Widget widget = viewBeforeProcessing.widgets().findFirst().get();
		ObjectDataWidget odw = new ObjectDataWidget(widget.getElement());

		JsonObject selectedProperties = odw.getConfiguration().getSelectedProperties();

		assertNull(selectedProperties.get(newType));
		assertNotNull(selectedProperties.get(sourceType));

		Template template = new Template();
		template.setForType(newType);
		instanceTemplateServceImpl.createTemplate(template, SOURCE_ID);

		verify(templateService).create(eq(template), stringCaptor.capture());

		Idoc idoc = Idoc.parse(stringCaptor.getValue());

		widget = idoc.widgets().findFirst().get();
		odw = new ObjectDataWidget(widget.getElement());

		selectedProperties = odw.getConfiguration().getSelectedProperties();

		assertNotNull(selectedProperties.get(newType));
		assertNull(selectedProperties.get(sourceType));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_NotCreateTemplate_WhenSourceContentDoesNotExist() {
		withSourceContent(null);

		Template template = new Template();
		instanceTemplateServceImpl.createTemplate(template, SOURCE_ID);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_NotCreateTemplate_WhenSourceContentCannotBeLoaded() {
		withNotLoadableContent();

		Template template = new Template();
		instanceTemplateServceImpl.createTemplate(template, SOURCE_ID);
	}

	private EmfInstance withSourceInstance(String type) {
		EmfInstance sourceInstance = new EmfInstance();
		InstanceType instanceType = mock(InstanceType.class);
		sourceInstance.setType(instanceType);
		sourceInstance.setIdentifier(type);
		sourceInstance.setId(SOURCE_ID);
		when(domainInstanceService.loadInstance(SOURCE_ID)).thenReturn(sourceInstance);

		return sourceInstance;
	}

	private void withSourceContent(String content) {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(content != null);
		try {
			when(contentInfo.asString()).thenReturn(content);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		when(instanceContentService.getContent(SOURCE_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
	}

	private void withNotLoadableContent() {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		try {
			doThrow(IOException.class).when(contentInfo).asString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		when(instanceContentService.getContent(SOURCE_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
	}

}
