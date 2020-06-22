package com.sirma.sep.bpm.camunda.scripts;

import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * Provides definition scripts access to parent entity workflows.
 *
 * @author Y.Yordanov
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 207)
public class WorkflowsScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final ScriptNode[] EMPTY_NODES = new ScriptNode[0];

	@Inject
	private LinkService linkService;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private CamundaBPMNService camundaBPMNService;
	@Inject
	private ProcessService processService;
	@Inject
	private InstanceTypeResolver instanceResolver;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.singletonMap("workflows", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Publish a documents/objects from a task or workflow.
	 *
	 * @param node the root element to get the source elements to publish
	 * @param operation the operation to use when executing publish operation
	 * @param relationId the relation id used for getting documents to publish
	 * @return an array of script nodes representing latest revisions of all published instances
	 */
	public ScriptNode[] publish(ScriptNode node, String relationId, String operation) {
		if (!isActivityNode(node)) {
			LOGGER.warn("Passed node is not an activity, cannot continue!");
			return EMPTY_NODES;
		}
		ScriptNode[] documents = getProcessingDocuments(node, relationId);
		List<ScriptNode> result = new ArrayList<>(documents.length);
		for (ScriptNode document : documents) {
			ScriptNode publishedDocument = document.publish(operation);
			if (publishedDocument != null) {
				LOGGER.info("publish: Successfully published instance id: " + publishedDocument.getId());
				result.add(publishedDocument);
			}
		}
		return result.toArray(new ScriptNode[result.size()]);
	}

	/**
	 * Gets the all instances linked to passed node with passed relationId.
	 *
	 * @param node the root element to get the document elements.
	 * @param relationId the relation id for which to find linked objects. Example: {@link LinkConstants#PROCESSES}
	 * @return an array of script nodes representing instances linked current node
	 */
	public ScriptNode[] getProcessingDocuments(ScriptNode node, String relationId) {
		if (!isActivityNode(node) || StringUtils.isBlank(relationId)) {
			LOGGER.warn("Passed node is not an activity or blank relationId, cannot continue!");
			return EMPTY_NODES;
		}
		ScriptNode[] documentsOnWorkflow;
		if (node.is("task") && node.getParent() != null) {
			documentsOnWorkflow = node.getParent().getLinks(relationId);
		} else if (isWorkflow(node)) {
			documentsOnWorkflow = node.getLinks(relationId);
		} else {
			List<LinkReference> linkReferences = linkService.getLinks(node.getTarget().toReference(), relationId);
			List<LinkInstance> linkInstances = linkService.convertToLinkInstance(linkReferences);
			List<ScriptNode> result = new ArrayList<>(linkInstances.size());
			for (LinkInstance linkInstance : linkInstances) {
				result.add(typeConverter.convert(ScriptNode.class, linkInstance.getTo()));
			}
			documentsOnWorkflow = result.toArray(new ScriptNode[result.size()]);
		}
		return documentsOnWorkflow;
	}

	/**
	 * Get active workflow based on root element.
	 * @param node the root element to start looking for workflow. Node could be task/workflow
	 * @return workflow node or null if not found
	 */
	public ScriptNode getWorkflowNode(ScriptNode node) {
		if (!isActivityNode(node)) {
			LOGGER.warn("Passed node is not an activity, cannot continue!");
			return null;
		}
		if (node.is("task") && node.getParent() != null && isWorkflow(node.getParent())) {
			return node.getParent();
		} else if (isWorkflow(node)) {
			return node;
		} else {
			ProcessInstance processInstance = camundaBPMNService.getProcessInstance(node.getTarget());
			if (processInstance == null) {
				return null;
			}
			Instance process = BPMInstanceUtil.resolveInstance(processInstance.getBusinessKey(), instanceResolver);
			return typeConverter.convert(ScriptNode.class, process);
		}
	}

	/**
	 * Starts Workflow by messageId, where workflow start event is Message Start Event.
	 * @param context the root element to start looking for workflow
	 * @param messageId the message id used in workflow's start event
	 * @param params the map with initial workflow's properties to start with
	 * @return the created workflow converted to {@link ScriptNode}
	 */
	public ScriptNode startWorkflowByMessage(ScriptNode context, String messageId, Map params) {
		try {
			if (!isNodeCorrect(context)) {
				LOGGER.warn("Context Node is null, cannot continue!");
				return null;
			}
			Instance workflow = processService.startProcess(messageId, context.getTarget().getId().toString(), params);
			return typeConverter.convert(ScriptNode.class, workflow);
		} catch (BPMException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Sends a notification to workflow to continue.
	 * @param eventId the event id used in workflow's message or signal catch event
	 * @param processVariables the variables to add to execution when notify, null is acceptable
	 */
	public void notifyWorkflow(String eventId, Map processVariables) {
		processService.notify(eventId, processVariables);
	}

	/**
	 * Check is the passed node a workflow node.
	 * @param node the root element
	 * @return true if passed node is workflow node
	 */
	public boolean isWorkflow(ScriptNode node) {
		return isActivityNode(node) && node.is("workflow");
	}

	private static boolean isNodeCorrect(ScriptNode node) {
		return node != null && node.getTarget() != null ? true : false;
	}

	private boolean isActivityNode(ScriptNode node) {
		if (!isNodeCorrect(node)) {
			return false;
		}
		if (!isActivity(node.getTarget())) {
			return false;
		}
		return true;
	}
}