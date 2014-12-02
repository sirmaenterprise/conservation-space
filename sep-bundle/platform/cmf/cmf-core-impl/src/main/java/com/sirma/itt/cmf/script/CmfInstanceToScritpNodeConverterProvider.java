package com.sirma.itt.cmf.script;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.script.InstanceToScriptNodeConverterProvider;
import com.sirma.itt.emf.script.ScriptNode;

/**
 * Cmf converter register for specific converters for
 * {@link com.sirma.itt.emf.instance.model.Instance} to {@link ScriptNode}.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class CmfInstanceToScritpNodeConverterProvider extends InstanceToScriptNodeConverterProvider {

	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW)
	private Instance<WorkflowScriptNode> workflowNodes;
	@Inject
	private Instance<DocumentScriptNode> documentNodes;
	@Inject
	private Instance<TaskScriptNode> taskNodes;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(WorkflowInstanceContext.class, ScriptNode.class,
				new InstanceToScriptNodeConverter<WorkflowInstanceContext>(workflowNodes));
		converter.addConverter(DocumentInstance.class, ScriptNode.class,
				new InstanceToScriptNodeConverter<DocumentInstance>(documentNodes));
		converter.addConverter(TaskInstance.class, ScriptNode.class,
				new InstanceToScriptNodeConverter<TaskInstance>(taskNodes));
		converter.addConverter(StandaloneTaskInstance.class, ScriptNode.class,
				new InstanceToScriptNodeConverter<StandaloneTaskInstance>(taskNodes));
	}
}
