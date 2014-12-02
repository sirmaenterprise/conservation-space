package com.sirma.itt.cmf.script;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.emf.script.ScriptNode;

/**
 * Script node implementation that has specific task methods
 * 
 * @author BBonev
 */
public class TaskScriptNode extends WorkflowScriptNode {

	/**
	 * Gets the processing documents.
	 * 
	 * @return the processing documents
	 */
	@Override
	public ScriptNode[] getProcessingDocuments() {
		if (target == null) {
			return new ScriptNode[0];
		}
		// collect all unique documents from the links set in the tasks and in workflow
		Set<ScriptNode> instances = new LinkedHashSet<ScriptNode>(16);
		if (target instanceof TaskInstance
				&& (((TaskInstance) target).getOwningInstance() instanceof WorkflowInstanceContext)) {
			ScriptNode[] documentsOnWorkflow = getParent().getLinks(LinkConstantsCmf.PROCESSES);
			for (ScriptNode scriptNode : documentsOnWorkflow) {
				instances.add(scriptNode);
			}
		}
		List<ScriptNode> documentsOnTask = getLinksInternal(LinkConstantsCmf.PROCESSES);
		instances.addAll(documentsOnTask);
		return toArray(instances);
	}
}
