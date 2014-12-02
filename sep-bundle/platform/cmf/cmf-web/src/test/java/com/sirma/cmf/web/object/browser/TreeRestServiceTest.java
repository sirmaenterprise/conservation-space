package com.sirma.cmf.web.object.browser;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkSearchArguments;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.script.ScriptEvaluator;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class ObjectsBrowserRestServiceTest.
 * 
 * @author svelikov
 */
@Test
public class TreeRestServiceTest extends CMFTest {

	/** The controller under test. */
	private TreeRestService controller;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The type converter. */
	private TypeConverter typeConverter;

	/** The script evaluator. */
	private ScriptEvaluator scriptEvaluator;

	/** The solr link service. */
	private LinkService solrLinkService;

	/** The label provider. */
	private LabelProvider labelProvider;

	/**
	 * Instantiates a new objects explorer controller test.
	 */
	@BeforeMethod
	public void init() {
		TypeConverter converter = createTypeConverter();
		controller = new TreeRestService();

		ReflectionUtils.setField(controller, "log", SLF4J_LOG);
		dictionaryService = Mockito.mock(DictionaryService.class);
		ReflectionUtils.setField(controller, "dictionaryService", dictionaryService);
		// create partial proxy of the type converter so it can be mocked
		typeConverter = Mockito.spy(converter);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);
		scriptEvaluator = Mockito.mock(ScriptEvaluator.class);
		ReflectionUtils.setField(controller, "scriptEvaluator", scriptEvaluator);
		ReflectionUtils.setField(controller, "treePageSize", 500);
		solrLinkService = Mockito.mock(LinkService.class);
		ReflectionUtils.setField(controller, "solrLinkService", new InstanceProxyMock<LinkService>(
				solrLinkService));
		labelProvider = Mockito.mock(LabelProvider.class);
		ReflectionUtils.setField(controller, "labelProvider", labelProvider);

	}

	/**
	 * Test for load method .
	 */
	public void loadTest() {
		// Response response = controller.load(null, null);
		// Assert.assertNull(response);
	}

	/**
	 * Test tree expand_search.
	 */
	public void testTreeExpand_search() {
		InstanceReference reference = typeConverter
				.convert(InstanceReference.class, "caseinstance");
		reference.setIdentifier("emf:instance");

		final List<LinkInstance> result = new ArrayList<LinkInstance>();
		Mockito.when(solrLinkService.searchLinks(Mockito.any(LinkSearchArguments.class)))
				.thenAnswer(new Answer<LinkSearchArguments>() {

					@Override
					public LinkSearchArguments answer(InvocationOnMock invocation) throws Throwable {
						LinkSearchArguments arguments = (LinkSearchArguments) invocation
								.getArguments()[0];
						arguments.setResult(result);
						return arguments;
					}
				});
		TaskInstance taskInstance = new TaskInstance();
		taskInstance.setId("emf:tast");
		taskInstance.setProperties(new HashMap<String, Serializable>());
		taskInstance.getProperties().put(DefaultProperties.HEADER_COMPACT, "compactHeader");
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setTo(taskInstance);
		linkInstance.setFrom(new CaseInstance());
		linkInstance.setIdentifier(LinkConstants.TREE_PARENT_TO_CHILD);
		result.add(linkInstance);

		Response response = controller.tree("emf:instance", "caseinstance", null, null, false,
				null, true, true, true,
				0);
		Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(reference);
		arguments.setLinkId(LinkConstants.TREE_PARENT_TO_CHILD);
		arguments.setPageNumber(0);
		arguments.setMaxSize(500);

		Mockito.verify(solrLinkService).searchLinks(Mockito.any(LinkSearchArguments.class));
	}

	/**
	 * Test tree expand_correct arguments.
	 */
	public void testTreeExpand_correctArguments() {
		InstanceReference reference = typeConverter
				.convert(InstanceReference.class, "caseinstance");
		reference.setIdentifier("emf:instance");

		final List<LinkInstance> result = new ArrayList<LinkInstance>();
		Mockito.when(solrLinkService.searchLinks(Mockito.any(LinkSearchArguments.class)))
				.thenAnswer(new Answer<LinkSearchArguments>() {

					@Override
					public LinkSearchArguments answer(InvocationOnMock invocation) throws Throwable {
						LinkSearchArguments arguments = (LinkSearchArguments) invocation
								.getArguments()[0];
						arguments.setResult(result);
						return arguments;
					}
				});
		TaskInstance taskInstance = new TaskInstance();
		taskInstance.setId("emf:tast");
		taskInstance.setProperties(new HashMap<String, Serializable>());
		taskInstance.getProperties().put(DefaultProperties.HEADER_COMPACT, "compactHeader");
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setTo(taskInstance);
		linkInstance.setFrom(new CaseInstance());
		linkInstance.setIdentifier(LinkConstants.TREE_PARENT_TO_CHILD);
		result.add(linkInstance);

		Response response = controller.tree("emf:instance", "caseinstance", null, null, false,
				null, true, true, true,
				0);
		Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(reference);
		arguments.setLinkId(LinkConstants.TREE_PARENT_TO_CHILD);
		arguments.setPageNumber(0);
		arguments.setMaxSize(500);

		Mockito.verify(solrLinkService).searchLinks(arguments);
	}

	/**
	 * Check mimetype filter.
	 * 
	 * @param values
	 *            the values
	 */
	private void checkMimetypeFilter(List<Object> values) {
		assertTrue(values.size() == 1, "The mimetype filter values list should have 1 values!");
		assertEquals(values.get(0), "image");
	}

	/**
	 * Check purpose filter.
	 * 
	 * @param values
	 *            the values
	 */
	private void checkPurposeFilter(List<Object> values) {
		assertTrue(values.size() == 2, "The purpose filter values list should have 2 values!");
		assertEquals(values.get(0), "EMPTY");
		assertEquals(values.get(1), "objectsSection");
	}

	/**
	 * Check instancetype filter.
	 * 
	 * @param values
	 *            the values
	 */
	private void checkInstancetypeFilter(List<Object> values) {
		assertTrue(values.size() == 2, "The instancetype filter values list should have 2 values!");
		assertEquals(values.get(0), "sectioninstance");
		assertEquals(values.get(1), "documentinstance");
	}

	/**
	 * Get root instance test.
	 */
	public void getRootInstanceTest() {
		// for null we should get nulls
		Instance rootInstance = controller.getRootInstance(null);
		assertNull(rootInstance);

		// for not root instance, we should get the root instance
		rootInstance = controller.getRootInstance(createCaseInstance(Long.valueOf(1)));
		// NOTE: can not be fully tested here
	}

	/**
	 * Extract instance id test.
	 */
	public void extractInstanceIdTest() {
		String instanceId = controller.extractInstanceId(null);
		assertEquals(instanceId, "");

		instanceId = controller.extractInstanceId("");
		assertEquals(instanceId, "");

		instanceId = controller.extractInstanceId("1");
		assertEquals(instanceId, "1");

		instanceId = controller.extractInstanceId("1/2");
		assertEquals(instanceId, "2");

		instanceId = controller.extractInstanceId("1/2/3");
		assertEquals(instanceId, "3");

		instanceId = controller.extractInstanceId("1/2/3/456");
		assertEquals(instanceId, "456");
	}

	/**
	 * Test if method groups properly list with instances.
	 */
	public void groupByTypeWithEmptyListTest() {
		List<Instance> emptyList = new ArrayList<Instance>(0);
		Map<String, List<Instance>> grouped = controller.groupByType(emptyList);
		assertTrue(grouped.isEmpty());
	}

	/**
	 * Group by type case list test.
	 */
	public void groupByTypeCaseListTest() {
		List<Instance> caselevel = new ArrayList<Instance>(5);
		// documents sections
		caselevel.add(createSectionInstance(Long.valueOf(1)));
		caselevel.add(createSectionInstance(Long.valueOf(2)));
		caselevel.add(createSectionInstance(Long.valueOf(3)));

		Map<String, List<Instance>> grouped = controller.groupByType(caselevel);

		// we have only one section type, so we should have only one item mapped
		assertTrue(grouped.size() == 1);
		// assertTrue(grouped.containsKey("documentSection"));

		// objects sections
		caselevel.add(createSectionInstance(Long.valueOf(4), "objects"));
		caselevel.add(createSectionInstance(Long.valueOf(5), "objects"));

		// sections should be grouped by purpose and now we should have 2 items mapped
		grouped = controller.groupByType(caselevel);
		// assertTrue(grouped.size() == 2);
	}

	/**
	 * Test method that tell whether instance children should be grouped or not.
	 */
	public void shouldBeGroupedTest() {
		// should be grouped for projectinstance, caseinstance
		boolean shouldBeGrouped = controller.shouldBeGrouped(createCaseInstance(Long.valueOf(1)));
		assertTrue(shouldBeGrouped);
		// NOTE: can't be tested for projectinstance here

		// should not be grouped for sectioninstance, documentinstance, objectinstance,
		// workflowinstance
		shouldBeGrouped = controller.shouldBeGrouped(createSectionInstance(Long.valueOf(1)));
		assertFalse(shouldBeGrouped);
		shouldBeGrouped = controller.shouldBeGrouped(createDocumentInstance(Long.valueOf(1)));
		assertFalse(shouldBeGrouped);
		shouldBeGrouped = controller.shouldBeGrouped(createWorkflowInstance(Long.valueOf(1)));
		assertFalse(shouldBeGrouped);
		// NOTE: can't be tested for objectinstance here
	}

	/**
	 * Map instance test.
	 */
	public void mapInstanceTest() {
		Map<String, List<Instance>> grouped = new HashMap<String, List<Instance>>();

		CaseInstance caseInstance1 = createCaseInstance(Long.valueOf(1));
		controller.mapInstance(caseInstance1, grouped);

		// add one instance of caseinstance type
		String caseInstanceType = caseInstance1.getClass().getSimpleName().toLowerCase();
		assertTrue(grouped.size() == 1);
		assertTrue(grouped.get(caseInstanceType).size() == 1);
		assertEquals(grouped.get(caseInstanceType).get(0), caseInstance1);

		// add second instance of caseinstance type
		CaseInstance caseInstance2 = createCaseInstance(Long.valueOf(2));
		controller.mapInstance(caseInstance2, grouped);
		assertTrue(grouped.size() == 1);
		assertTrue(grouped.get(caseInstanceType).size() == 2);
		assertEquals(grouped.get(caseInstanceType).get(0), caseInstance1);
		assertEquals(grouped.get(caseInstanceType).get(1), caseInstance2);

		// add document instance
		DocumentInstance documentInstance1 = createDocumentInstance(Long.valueOf(1));
		String documentInstanceType = documentInstance1.getClass().getSimpleName().toLowerCase();
		controller.mapInstance(documentInstance1, grouped);
		assertTrue(grouped.size() == 2);
		assertTrue(grouped.get(documentInstanceType).size() == 1);
		assertEquals(grouped.get(documentInstanceType).get(0), documentInstance1);

		// add second of type documentinstance
		DocumentInstance documentInstance2 = createDocumentInstance(Long.valueOf(2));
		controller.mapInstance(documentInstance2, grouped);
		assertTrue(grouped.size() == 2);
		assertTrue(grouped.get(documentInstanceType).size() == 2);
		assertEquals(grouped.get(documentInstanceType).get(0), documentInstance1);
		assertEquals(grouped.get(documentInstanceType).get(1), documentInstance2);

		// add sectioninstance
		SectionInstance sectionInstance1 = createSectionInstance(Long.valueOf(1));
		String sectionInstanceType = sectionInstance1.getClass().getSimpleName().toLowerCase();
		controller.mapInstance(sectionInstance1, grouped);
		assertTrue(grouped.size() == 3);
		assertTrue(grouped.get(sectionInstanceType).size() == 1);
		assertEquals(grouped.get(sectionInstanceType).get(0), sectionInstance1);

		// add task instance
		TaskInstance wfTaskInstance1 = createWorkflowTaskInstance(Long.valueOf(1));
		String wfTaskInstanceType = wfTaskInstance1.getClass().getSimpleName().toLowerCase();
		controller.mapInstance(wfTaskInstance1, grouped);
		assertTrue(grouped.size() == 4);
		assertTrue(grouped.get(wfTaskInstanceType).size() == 1);
		assertEquals(grouped.get(wfTaskInstanceType).get(0), wfTaskInstance1);

		// add standalone task instance
		StandaloneTaskInstance sTaskInstance1 = createStandaloneTaskInstance(Long.valueOf(1));
		String sTaskInstanceType = sTaskInstance1.getClass().getSimpleName().toLowerCase();
		controller.mapInstance(sTaskInstance1, grouped);
		assertTrue(grouped.size() == 5);
		assertTrue(grouped.get(sTaskInstanceType).size() == 1);
		assertEquals(grouped.get(sTaskInstanceType).get(0), sTaskInstance1);

		// add workflow instance
		WorkflowInstanceContext workflowInstance = createWorkflowInstance(Long.valueOf(1));
		String workflowInstanceType = workflowInstance.getClass().getSimpleName().toLowerCase();
		controller.mapInstance(workflowInstance, grouped);
		assertTrue(grouped.size() == 6);
		assertTrue(grouped.get(workflowInstanceType).size() == 1);
		assertEquals(grouped.get(workflowInstanceType).get(0), workflowInstance);
	}

	/**
	 * Map section instance test.
	 */
	public void mapSectionInstanceTest() {
		Map<String, List<Instance>> grouped = new HashMap<String, List<Instance>>();

		// add section with wrong purpose
		SectionInstance wrongPurposeSection = createSectionInstance(Long.valueOf(1), "dummy");
		controller.mapSectionInstance(wrongPurposeSection, grouped);
		assertTrue(grouped.size() == 0);

		// add documents section - no purpose
		SectionInstance documentsSection1 = createSectionInstance(Long.valueOf(1));
		controller.mapSectionInstance(documentsSection1, grouped);
		assertTrue(grouped.size() == 1);
		assertTrue(grouped.get(TreeRestService.DOCUMENTS_SECTION).size() == 1);
		assertEquals(grouped.get(TreeRestService.DOCUMENTS_SECTION).get(0),
				documentsSection1);

		// add documents second section - purpose=documentsSection
		SectionInstance documentsSection2 = createSectionInstance(Long.valueOf(2));
		controller.mapSectionInstance(documentsSection2, grouped);
		assertTrue(grouped.size() == 1);
		assertTrue(grouped.get(TreeRestService.DOCUMENTS_SECTION).size() == 2);
		assertEquals(grouped.get(TreeRestService.DOCUMENTS_SECTION).get(0),
				documentsSection1);
		assertEquals(grouped.get(TreeRestService.DOCUMENTS_SECTION).get(1),
				documentsSection2);

		// add objects section - purpose=objectsSection
		SectionInstance objectsSection1 = createSectionInstance(Long.valueOf(1),
				TreeRestService.OBJECTS_SECTION);
		controller.mapSectionInstance(objectsSection1, grouped);
		assertTrue(grouped.size() == 2);
		assertTrue(grouped.get(TreeRestService.OBJECTS_SECTION).size() == 1);
		assertEquals(grouped.get(TreeRestService.OBJECTS_SECTION).get(0), objectsSection1);

		// add objects section - purpose=objectsSection
		SectionInstance objectsSection2 = createSectionInstance(Long.valueOf(2),
				TreeRestService.OBJECTS_SECTION);
		controller.mapSectionInstance(objectsSection2, grouped);
		assertTrue(grouped.size() == 2);
		assertTrue(grouped.get(TreeRestService.OBJECTS_SECTION).size() == 2);
		assertEquals(grouped.get(TreeRestService.OBJECTS_SECTION).get(0), objectsSection1);
		assertEquals(grouped.get(TreeRestService.OBJECTS_SECTION).get(1), objectsSection2);
	}

	/**
	 * Map task instance test.
	 */
	public void mapTaskInstanceTest() {
		Map<String, List<Instance>> grouped = new HashMap<String, List<Instance>>();

		// add wf task instance
		TaskInstance workflowTaskInstance = createWorkflowTaskInstance(Long.valueOf(1));
		controller.mapTaskInstance(workflowTaskInstance, grouped);
		assertTrue(grouped.size() == 1);
		assertTrue(grouped.get(TreeRestService.TASKS).size() == 1);
		assertEquals(grouped.get(TreeRestService.TASKS).get(0), workflowTaskInstance);

		// add standalone task instance
		StandaloneTaskInstance sTaskInstance = createStandaloneTaskInstance(Long.valueOf(1));
		controller.mapTaskInstance(sTaskInstance, grouped);
		assertTrue(grouped.size() == 1);
		assertTrue(grouped.get(TreeRestService.TASKS).size() == 2);
		assertEquals(grouped.get(TreeRestService.TASKS).get(1), sTaskInstance);
	}

	/**
	 * Test for createMappings method.
	 */
	public void createMappingTest() {
		Map<String, List<Instance>> grouped = new HashMap<String, List<Instance>>();

		controller.createMapping(null, grouped);
		assertTrue(grouped.isEmpty());

		controller.createMapping("", grouped);
		assertTrue(grouped.isEmpty());

		controller.createMapping("key1", grouped);
		assertEquals(grouped.containsKey("key1"), true);

		controller.createMapping("key2", grouped);
		assertEquals(grouped.containsKey("key2"), true);
	}

	/**
	 * Fetch instance children test.
	 */
	public void fetchInstanceChildrenTest() {
		// TODO: implement
	}

	/**
	 * Test for build data method.
	 */
	public void buildDataTest() {
		JSONArray buildData = controller.buildData(null, null, "", true, "", false, false, false,
				null);

		assertTrue(buildData.length() == 0);
		// TODO: extend test
	}

	/**
	 * Tests for isSelectable method.
	 */
	public void isSelectableTest() {
		Set<String> filters = new HashSet<String>(1);

		// if instance is null we expect false as result
		boolean isSelectable = controller.isSelectable(null, null);
		assertFalse(isSelectable);

		// check if result of evaluation is returned
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));
		Map<String, Serializable> properties = new HashMap<String, Serializable>(1);
		caseInstance.setProperties(properties);
		Mockito.when(scriptEvaluator.eval(Mockito.anyString(), Mockito.anyMap())).thenReturn(
				Boolean.TRUE);
		isSelectable = controller.isSelectable(caseInstance, null);
		assertTrue(isSelectable, "Instance should be selectable!");

		// check if result of evaluation is returned
		Mockito.when(scriptEvaluator.eval(Mockito.anyString(), Mockito.anyMap())).thenReturn(
				Boolean.FALSE);
		isSelectable = controller.isSelectable(caseInstance, null);
		assertFalse(isSelectable, "Instance should not be selectable!");

		// if evaluation fails and null is returned, we expect false to be returned
		Mockito.when(scriptEvaluator.eval(Mockito.anyString(), Mockito.anyMap())).thenReturn(null);
		isSelectable = controller.isSelectable(caseInstance, null);
		assertFalse(isSelectable, "Filter evaluation failed!");
	}
}
