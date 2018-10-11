package com.sirma.sep.bpm.camunda.scripts;

import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * Test class for {@link WorkflowsScriptProvider}.
 * @author hlungov
 */
public class WorkflowsScriptProviderTest {

	private static final String PUBLISH_OPERATION = "publish";

	@Mock
	private LinkService linkService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private CamundaBPMNService camundaBPMNService;

	@Mock
	private ProcessService processService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceContextService instanceContextInitializer;

	@InjectMocks
	private WorkflowsScriptProvider provider;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_getBindings() {
		assertTrue(provider.getBindings().get("workflows").equals(provider));
	}

	@Test
	public void test_getScripts() {
		assertTrue(provider.getScripts().isEmpty());
	}

	@Test
	public void test_publish_null_node() {
		ScriptNode[] published = provider.publish(null, null, null);
		assertEquals(0, published.length);
	}

	@Test
	public void test_publish_null_target() {
		ScriptNode[] published = provider.publish(new ScriptNode(), null, null);
		assertEquals(0, published.length);
	}

	@Test
	public void test_publish_not_activity() {
		ScriptNode node = new ScriptNode();
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.FALSE);
		node.setTarget(instance);
		ScriptNode[] published = provider.publish(node, null, null);
		assertEquals(0, published.length);
	}

	@Test
	public void test_publish_no_published() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		when(node.getTarget()).thenReturn(instance);
		when(node.is("workflow")).thenReturn(Boolean.TRUE);
		ScriptNode link = mock(ScriptNode.class);
		when(node.getLinks(eq(LinkConstants.PROCESSES))).thenReturn(Arrays.asList(link).toArray(new ScriptNode[1]));
		when(link.publish(eq(PUBLISH_OPERATION))).thenReturn(null);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		ScriptNode[] published = provider.publish(node,LinkConstants.PROCESSES, PUBLISH_OPERATION);
		assertEquals(0, published.length);
	}

	@Test
	public void test_publish() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		when(node.is("workflow")).thenReturn(Boolean.TRUE);
		ScriptNode link = mock(ScriptNode.class);
		when(node.getLinks(eq(LinkConstants.PROCESSES))).thenReturn(Arrays.asList(link).toArray(new ScriptNode[1]));
		ScriptNode published = mock(ScriptNode.class);
		when(link.publish(eq(PUBLISH_OPERATION))).thenReturn(published);
		ScriptNode[] publishedArr = provider.publish(node,LinkConstants.PROCESSES, PUBLISH_OPERATION);
		assertEquals(1, publishedArr.length);
		assertEquals(published, publishedArr[0]);
	}

	@Test
	public void test_getProcessingDocuments_null_node() {
		ScriptNode[] processingDocuments = provider.getProcessingDocuments(null, null);
		assertEquals(0, processingDocuments.length);
	}

	@Test
	public void test_getProcessingDocuments_null_target() {
		ScriptNode node = mock(ScriptNode.class);
		when(node.getTarget()).thenReturn(null);
		ScriptNode[] processingDocuments = provider.getProcessingDocuments(node, LinkConstants.PROCESSES);
		assertEquals(0, processingDocuments.length);
	}

	@Test
	public void test_getProcessingDocuments_not_activity() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.FALSE);
		when(node.getTarget()).thenReturn(instance);
		ScriptNode[] processingDocuments = provider.getProcessingDocuments(node, LinkConstants.PROCESSES);
		assertEquals(0, processingDocuments.length);
	}

	@Test
	public void test_getProcessingDocuments_from_task() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		when(node.is("task")).thenReturn(Boolean.TRUE);
		ScriptNode parentNode = mock(ScriptNode.class);
		when(node.getParent()).thenReturn(parentNode);
		ScriptNode[] links = new ScriptNode[0];
		when(parentNode.getLinks(eq(LinkConstants.PROCESSES))).thenReturn(links);
		ScriptNode[] processingDocuments = provider.getProcessingDocuments(node, LinkConstants.PROCESSES);
		assertArrayEquals(links, processingDocuments);
	}

	@Test
	public void test_getProcessingDocuments_from_workflow() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		when(node.is("task")).thenReturn(Boolean.FALSE);
		when(node.is("workflow")).thenReturn(Boolean.TRUE);
		ScriptNode[] links = new ScriptNode[0];
		when(node.getLinks(eq(LinkConstants.PROCESSES))).thenReturn(links);
		ScriptNode[] processingDocuments = provider.getProcessingDocuments(node, LinkConstants.PROCESSES);
		assertArrayEquals(links, processingDocuments);
	}

	@Test
	public void test_getProcessingDocuments_from_external() {
		ScriptNode node = mock(ScriptNode.class);
		when(node.is("task")).thenReturn(Boolean.FALSE);
		when(node.is("workflow")).thenReturn(Boolean.FALSE);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceReference);
		LinkReference linkReference = mock(LinkReference.class);
		List<LinkReference> linkReferences = Arrays.asList(linkReference);
		when(linkService.getLinks(eq(instanceReference), eq(LinkConstants.PROCESSES))).thenReturn(linkReferences);
		LinkInstance linkInstance = mock(LinkInstance.class);
		when(linkInstance.getTo()).thenReturn(instance);
		when(linkService.convertToLinkInstance(eq(linkReferences))).thenReturn(Arrays.asList(linkInstance));
		when(node.getLinks(eq(LinkConstants.PROCESSES))).thenReturn(new ScriptNode[0]);
		when(typeConverter.convert(eq(ScriptNode.class), eq(instance))).thenReturn(new ScriptNode());
		ScriptNode[] processingDocuments = provider.getProcessingDocuments(node, LinkConstants.PROCESSES);
		assertEquals(1, processingDocuments.length);
	}

	@Test
	public void test_isWorkflow() {
		Assert.assertFalse(provider.isWorkflow(null));
		ScriptNode node = mock(ScriptNode.class);
		when(node.is("workflow")).thenReturn(Boolean.FALSE, Boolean.TRUE);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		Assert.assertFalse(provider.isWorkflow(node));
		Assert.assertTrue(provider.isWorkflow(node));
	}

	@Test
	public void test_getWorkflowNode_null_check() {
		Assert.assertNull(provider.getWorkflowNode(null));
	}

	@Test
	public void test_getWorkflowNode_is_task() {
		ScriptNode node = mock(ScriptNode.class);
		when(node.is("task")).thenReturn(Boolean.TRUE);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		ScriptNode parent = mock(ScriptNode.class);
		when(parent.getTarget()).thenReturn(instance);
		when(parent.is("workflow")).thenReturn(Boolean.TRUE);
		when(node.getParent()).thenReturn(parent);
		Assert.assertEquals(parent, provider.getWorkflowNode(node));
	}

	@Test
	public void test_getWorkflowNode_is_workflow() {
		ScriptNode node = mock(ScriptNode.class);
		when(node.is("workflow")).thenReturn(Boolean.TRUE);
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		Assert.assertEquals(node, provider.getWorkflowNode(node));
	}

	@Test
	public void test_getWorkflowNode_through_camunda() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(camundaBPMNService.getProcessInstance(eq(instance))).thenReturn(processInstance);
		when(processInstance.getBusinessKey()).thenReturn("processInstanceId");
		when(instanceResolver.resolveReference(eq("processInstanceId"))).thenReturn(Optional.of(instanceReference));
		when(typeConverter.convert(ScriptNode.class, instance)).thenReturn(node);
		Assert.assertEquals(node, provider.getWorkflowNode(node));
	}

	@Test
	public void test_getWorkflowNode_through_camunda_null_processInstance() {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(node.getTarget()).thenReturn(instance);
		when(camundaBPMNService.getProcessInstance(eq(instance))).thenReturn(null);
		Assert.assertNull(provider.getWorkflowNode(node));
	}

	@Test
	public void test_startWorkflowByMessage_incorrect_node() {
		Assert.assertNull(provider.startWorkflowByMessage(null, null, null));
	}

	@Test
	public void test_startWorkflowByMessage_not_started_workflow() throws BPMException {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(instance.getId()).thenReturn("emf:instanceId");
		when(node.getTarget()).thenReturn(instance);
		when(processService.startProcess(eq("testMessageId"), eq("emf:instanceId"), eq(null))).thenReturn(null);
		Assert.assertNull(provider.startWorkflowByMessage(node,"testMessageId", null));
	}

	@Test
	public void test_startWorkflowByMessage_throw_exception() throws BPMException {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(instance.getId()).thenReturn("emf:instanceId");
		when(node.getTarget()).thenReturn(instance);
		when(processService.startProcess(eq("testMessageId"), eq("emf:instanceId"), eq(null))).thenThrow(new CamundaIntegrationException(""));
		Assert.assertNull(provider.startWorkflowByMessage(node,"testMessageId", null));
		verify(typeConverter, never()).convert(eq(ScriptNode.class), any(Instance.class));
	}

	@Test
	public void test_startWorkflowByMessage() throws BPMException {
		ScriptNode node = mock(ScriptNode.class);
		Instance instance = mock(Instance.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(instance.getId()).thenReturn("emf:instanceId");
		when(node.getTarget()).thenReturn(instance);
		Instance workflow = mock(Instance.class);
		when(processService.startProcess(eq("testMessageId"), eq("emf:instanceId"), eq(null))).thenReturn(workflow);
		provider.startWorkflowByMessage(node,"testMessageId", null);
		verify(typeConverter).convert(eq(ScriptNode.class), eq(workflow));
	}

	@Test
	public void test_should_notifyWorkflow() {
		provider.notifyWorkflow("testEventId", null);
		verify(processService).notify("testEventId", null);
	}
}