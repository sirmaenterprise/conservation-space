package com.sirma.itt.cmf.domain;

import java.util.Map;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.definition.dao.AllowedChildTypeMappingExtension;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default type mappings for CMF classes as case, workflow and task.
 *
 * @author BBonev
 */
@Extension(target = AllowedChildTypeMappingExtension.TARGET_NAME, order = 10)
public class CmfAllowedChildTypeMappingExtension implements AllowedChildTypeMappingExtension {

	/** The Constant definitionMapping. */
	private static final Map<String, Class<? extends DefinitionModel>> DEFINITION_MAPPING;
	/** The Constant instanceMapping. */
	private static final Map<String, Class<? extends Instance>> INSTANCE_MAPPING;
	/** The Constant typeMapping. */
	private static final Map<String, String> TYPE_MAPPING;

	static {
		DEFINITION_MAPPING = CollectionUtils.createHashMap(10);
		DEFINITION_MAPPING.put(ObjectTypesCmf.CASE, CaseDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypesCmf.WORKFLOW, WorkflowDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypesCmf.WORKFLOW_TASK, TaskDefinitionRef.class);
		DEFINITION_MAPPING.put(ObjectTypesCmf.STANDALONE_TASK, TaskDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypesCmf.DOCUMENT, DocumentDefinitionRef.class);
		DEFINITION_MAPPING.put(ObjectTypesCmf.SECTION, SectionDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypesCmf.FOLDER, GenericDefinition.class);

		INSTANCE_MAPPING = CollectionUtils.createHashMap(10);
		INSTANCE_MAPPING.put(ObjectTypesCmf.CASE, CaseInstance.class);
		INSTANCE_MAPPING.put(ObjectTypesCmf.WORKFLOW, WorkflowInstanceContext.class);
		INSTANCE_MAPPING.put(ObjectTypesCmf.WORKFLOW_TASK, TaskInstance.class);
		INSTANCE_MAPPING.put(ObjectTypesCmf.STANDALONE_TASK, StandaloneTaskInstance.class);
		INSTANCE_MAPPING.put(ObjectTypesCmf.DOCUMENT, DocumentInstance.class);
		INSTANCE_MAPPING.put(ObjectTypesCmf.SECTION, SectionInstance.class);
		INSTANCE_MAPPING.put(ObjectTypesCmf.FOLDER, FolderInstance.class);

		TYPE_MAPPING = CollectionUtils.createHashMap(10);
		TYPE_MAPPING.put(ObjectTypesCmf.CASE, "caseInstance");
		TYPE_MAPPING.put(ObjectTypesCmf.WORKFLOW, "workflowInstanceContext");
		TYPE_MAPPING.put(ObjectTypesCmf.WORKFLOW_TASK, "taskInstance");
		TYPE_MAPPING.put(ObjectTypesCmf.STANDALONE_TASK, "standaloneTaskInstance");
		TYPE_MAPPING.put(ObjectTypesCmf.DOCUMENT, "documentInstance");
		TYPE_MAPPING.put(ObjectTypesCmf.SECTION, "sectionInstance");
		TYPE_MAPPING.put(ObjectTypesCmf.FOLDER, "folderInstance");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends DefinitionModel>> getDefinitionMapping() {
		return DEFINITION_MAPPING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends Instance>> getInstanceMapping() {
		return INSTANCE_MAPPING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTypeMapping() {
		return TYPE_MAPPING;
	}

}
