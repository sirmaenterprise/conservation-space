package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.testutil.io.FileTestUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonParser;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.utils.JsoupUtil;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;

public class BPMWidgetRendererTest {

	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	private static final String LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA = "The widget has no search criteria configured.";
	private static final String WORKFLOW_CONSTANT = "WFT:2:123";
	private static final String BUSINESS_PROCESS_DIAGRAM_WIDGET = "business-process-diagram-widget";
	private static final String BUSINESS_PROCESS_STANDART_MODE = "business-process-standart-mode.json";
	private static final String BUSINESS_PROCESS_AUTOMATIC_MODE = "business-process-automatic-mode.json";
	private static final String BUSINESS_PROCESS_NO_INSTANCE = "business-process-no-instances.json";
	private static final String BUSINESS_PROCESS_CURRENT_SELECTION_MODE = "business-process-current-selection-mode.json";
	private static final String TEST_FILE_UNDEFINED_SEARCH_CRITERIA = "business-process-undefined-search-criteria.json";

	private static final String INSTANCE_TEST_ID = "emf:id";

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private CamundaBPMNService processService;

	@Mock
	private RepositoryService repositoryService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private JsonToConditionConverter converter;

	@Mock
	private TempFileProvider tempFileProvider;

	@InjectMocks
	private BPMWidgetRenderer renderer;

	private Widget widget;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = Mockito.mock(Widget.class);
		Mockito.when(tempFileProvider.createLongLifeTempDir(BPMWidgetRenderer.EXPORT_DIR)).thenReturn(TEMP_DIR);
	}

	@Test
	public void should_accept_render_when_widget_is_businessPDW() throws Exception {
		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn(BUSINESS_PROCESS_DIAGRAM_WIDGET);
		Assert.assertTrue(renderer.accept(widget));
	}

	@Test
	public void should_not_accept_render_when_widget_is_other_than_businessPDW() throws Exception {
		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn("OtherWidget");
		Assert.assertFalse(renderer.accept(widget));
	}

	@Test
	public void should_not_accept_render_when_type_is_not_widget() throws Exception {
		Mockito.when(widget.isWidget()).thenReturn(false);
		Mockito.when(widget.getName()).thenReturn(BUSINESS_PROCESS_DIAGRAM_WIDGET);
		Assert.assertFalse(renderer.accept(widget));
	}

	@Test
	public void should_render_BPDW_when_data_is_correct() throws Exception {
		String title = "CorrectData";
		File testFile = new File(TEMP_DIR, UUID.randomUUID() + ".jpg");
		Mockito.when(tempFileProvider.createTempFile(Matchers.anyString(), Matchers.anyString(), Matchers.eq(TEMP_DIR))).thenReturn(testFile);
		try {
			Element render = renderer.render(INSTANCE_TEST_ID, generateWidgetNode(BUSINESS_PROCESS_STANDART_MODE, title));
			Assert.assertEquals(JsoupUtil.TAG_TABLE, render.tagName());
			Assert.assertNotNull(render.getElementsByTag(JsoupUtil.TAG_IMG));
			Assert.assertTrue(render.getElementsByTag(JsoupUtil.TAG_IMG).toString().contains(title));
		} finally {
			FileTestUtils.deleteFile(testFile);
		}
	}

	@Test
	public void should_render_BPDW_when_there_is_valid_selection() throws Exception {
		String title = "ValidSelection";
		File testFile = new File(TEMP_DIR, UUID.randomUUID() + ".jpg");
		Mockito.when(tempFileProvider.createTempFile(Matchers.anyString(), Matchers.anyString(), Matchers.eq(TEMP_DIR))).thenReturn(testFile);
		try {
			Element render = renderer.render(INSTANCE_TEST_ID,
											 generateWidgetNode(BUSINESS_PROCESS_CURRENT_SELECTION_MODE, title));
			Assert.assertEquals(JsoupUtil.TAG_TABLE, render.tagName());
			Assert.assertNotNull(render.getElementsByTag(JsoupUtil.TAG_IMG));
			Assert.assertTrue(render.getElementsByTag(JsoupUtil.TAG_IMG).toString().contains(title));
		} finally {
			FileTestUtils.deleteFile(testFile);
		}
	}

	@Test
	public void should_render_message_when_there_is_no_instance() throws Exception {
		String title = "NoInstance";
		WidgetNode widgetNode = generateWidgetNode(BUSINESS_PROCESS_NO_INSTANCE, title);
		Mockito.when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Collections.EMPTY_LIST);
		Mockito.when(labelProvider.getLabel(BPMWidgetRenderer.NO_SELECTION)).thenReturn(BPMWidgetRenderer.NO_SELECTION);

		Element render = renderer.render(INSTANCE_TEST_ID, widgetNode);

		Assert.assertEquals(JsoupUtil.TAG_TABLE, render.tagName());
		Assert.assertEquals(BPMWidgetRenderer.NO_SELECTION, render.text());
		Assert.assertFalse(render.getElementsByTag(JsoupUtil.TAG_IMG).toString().contains(title));
	}

	@Test
	public void should_BuildUndefinedCriteriaTable_When_WidgetConfigurationHasNotSearchCriteria()
			throws URISyntaxException, IOException {
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA))
				.thenReturn(LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);

		String title = "WidgetConfigurationHasNotSearchCriteria";
		WidgetNode widgetTest = generateWidgetNode(TEST_FILE_UNDEFINED_SEARCH_CRITERIA, title);

		Element table = renderer.render(INSTANCE_TEST_ID, widgetTest);

		Assert.assertEquals(LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA, table.text());
		Assert.assertFalse(table.toString().contains(title));
	}

	@Test
	public void should_set_message_for_criteria_when_set_to_Automatic_criteria() throws Exception {
		Mockito.when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA))
				.thenReturn(LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);

		String title = "Automatic_criteria";

		WidgetNode widgetTest = generateWidgetNode(BUSINESS_PROCESS_AUTOMATIC_MODE, title);

		Element table = renderer.render(INSTANCE_TEST_ID, widgetTest);

		Assert.assertEquals(LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA, table.text());
		Assert.assertFalse(table.toString().contains(title));
	}

	/**
	 * Load test resource.
	 *
	 * @param resource
	 *            the resource
	 * @return the com.google.gson. json object
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private com.google.gson.JsonObject loadTestResource(String resource) throws URISyntaxException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL testJsonURL = classLoader.getResource(resource);
		File jsonConfiguration = new File(testJsonURL.toURI());
		try (FileReader fileReader = new FileReader(jsonConfiguration)) {
			return new JsonParser().parse(fileReader).getAsJsonObject();
		}
	}

	/**
	 * Creates a mock widget.
	 * 
	 * @param pathToConfigurationFile
	 *            pathToConfigurationFile for filing mock data.
	 * @return the mock widget.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private WidgetNode generateWidgetNode(String pathToConfigurationFile, String testTitle)
			throws URISyntaxException, IOException {
		WidgetNode widgetNode = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widgetNode.getElement()).thenReturn(element);
		Mockito.when(widgetNode.getConfiguration())
				.thenReturn(new WidgetConfiguration(widgetNode, loadTestResource(pathToConfigurationFile)));

		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		Mockito.when(instance.get("title")).thenReturn(testTitle);
		InstanceType type = Mockito.mock(InstanceType.class);
		Mockito.when(type.getCategory()).thenReturn("workflowinstancecontext");
		Mockito.when(type.is(Matchers.eq("workflowinstancecontext"))).thenReturn(true);
		Mockito.when(instance.type()).thenReturn(type);

		ProcessDefinition definition = Mockito.mock(ProcessDefinition.class);
		Mockito.when(definition.getId()).thenReturn(WORKFLOW_CONSTANT);

		ActivityImpl activity = Mockito.mock(ActivityImpl.class);
		Mockito.when(activity.getWidth()).thenReturn(400);
		Mockito.when(activity.getHeight()).thenReturn(400);
		ArrayList<ActivityImpl> activityList = new ArrayList<>();
		activityList.add(activity);

		ProcessDefinitionEntity entity = Mockito.mock(ProcessDefinitionEntity.class);
		Mockito.when(entity.getActivities()).thenReturn(activityList);

		Mockito.when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		Mockito.when(processService.getProcessDefinition(instance)).thenReturn(definition);
		Mockito.when(repositoryService.getProcessDefinition(WORKFLOW_CONSTANT)).thenReturn(entity);

		return widgetNode;
	}

}
