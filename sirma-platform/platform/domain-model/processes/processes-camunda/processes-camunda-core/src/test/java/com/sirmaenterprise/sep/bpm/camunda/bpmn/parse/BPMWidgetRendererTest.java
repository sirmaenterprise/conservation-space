package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.content.idoc.nodes.WidgetNode;

public class BPMWidgetRendererTest {

	private static final String WORKFLOW_CONSTANT = "WFT:2:123";

	private static final String BUSINESS_PROCESS_DIAGRAM_WIDGET = "business-process-diagram-widget";

	private static final String BUSINESS_PROCESS_STANDART_MODE = "business-process-standart-mode.json";
	
	private static final String BUSINESS_PROCESS_NO_INSTANCE = "business-process-no-instances.json";

	private static final String BUSINESS_PROCESS_CURRENT_SELECTION_MODE = "business-process-current-selection-mode.json";

	private static final String INSTANCE_TEST_ID = "emf:id";

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private CamundaBPMNService processService;

	@Mock
	private RepositoryService repositoryService;

	@InjectMocks
	private BPMWidgetRenderer renderer;

	private Widget widget;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = Mockito.mock(Widget.class);
	}

	@Test
	public void testAcceptTrue() throws Exception {
		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn(BUSINESS_PROCESS_DIAGRAM_WIDGET);
		Assert.assertTrue(renderer.accept(widget));
	}

	@Test
	public void testAcceptOtherWidget() throws Exception {
		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn("OtherWidget");
		Assert.assertFalse(renderer.accept(widget));
	}

	@Test
	public void testAcceptFalse() throws Exception {
		Mockito.when(widget.isWidget()).thenReturn(false);
		Mockito.when(widget.getName()).thenReturn(BUSINESS_PROCESS_DIAGRAM_WIDGET);
		Assert.assertFalse(renderer.accept(widget));
	}

	@Test
	public void testRender() throws Exception {
		Element render = renderer.render(INSTANCE_TEST_ID, generateWidgetNode(BUSINESS_PROCESS_STANDART_MODE));
		Assert.assertEquals(render.tagName(), JsoupUtil.TAG_TABLE);
	}

	@Test
	public void testRenderCurrentSelection() throws Exception {
		Element render = renderer.render(INSTANCE_TEST_ID, generateWidgetNode(BUSINESS_PROCESS_CURRENT_SELECTION_MODE));
		Assert.assertEquals(render.tagName(), JsoupUtil.TAG_TABLE);
	}

	@Test
	public void testRenderNoInstance() throws Exception {
		Element render = renderer.render(INSTANCE_TEST_ID, generateWidgetNode(BUSINESS_PROCESS_NO_INSTANCE));
		Assert.assertEquals(render.tagName(), JsoupUtil.TAG_TABLE);
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
	 * @param json
	 *            config for filing mock data.
	 * @return the mock widget.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private WidgetNode generateWidgetNode(String config) throws URISyntaxException, IOException {
		WidgetNode widgetNode = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widgetNode.getElement()).thenReturn(element);
		Mockito.when(widgetNode.getConfiguration()).thenReturn(new WidgetConfiguration(widgetNode, loadTestResource(config)));

		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		Mockito.when(instance.get("title")).thenReturn("Wokflow title");
		InstanceType type = Mockito.mock(InstanceType.class);
		Mockito.when(type.getCategory()).thenReturn("workflowinstancecontext");
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
