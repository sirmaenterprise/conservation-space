package com.sirma.itt.cmf.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.cmf.beans.entity.CaseEntity;
import com.sirma.itt.cmf.beans.entity.DocumentEntity;
import com.sirma.itt.cmf.beans.entity.EntityIdType;
import com.sirma.itt.cmf.beans.entity.SectionEntity;
import com.sirma.itt.cmf.beans.entity.TaskEntity;
import com.sirma.itt.cmf.beans.entity.TemplateEntity;
import com.sirma.itt.cmf.beans.entity.WorkflowInstanceContextEntity;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.EntityTypeProviderExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.template.TemplateInstance;

/**
 * Default Cmf extension for entity type provider.
 * 
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 20)
public class CmfEntityTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(Arrays.asList(
			CaseEntity.class, CaseInstance.class, SectionEntity.class, SectionInstance.class,
			DocumentEntity.class, DocumentInstance.class, WorkflowInstanceContext.class,
			WorkflowInstanceContextEntity.class, TaskEntity.class, TaskInstance.class,
			StandaloneTaskInstance.class, TemplateInstance.class, TemplateEntity.class,
			FolderInstance.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Object object) {
		return EntityIdType.getType(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(CaseEntity.class) || object.equals(CaseInstance.class)) {
			return EntityIdType.CASE;
		} else if (object.equals(SectionEntity.class) || object.equals(SectionInstance.class)
				|| object.equals(FolderInstance.class)) {
			return EntityIdType.SECTION;
		} else if (object.equals(DocumentEntity.class) || object.equals(DocumentInstance.class)) {
			return EntityIdType.DOCUMENT;
		} else if (object.equals(WorkflowInstanceContext.class)
				|| object.equals(WorkflowInstanceContextEntity.class)) {
			return EntityIdType.WORKFLOW;
		} else if (object.equals(TaskEntity.class) || object.equals(TaskInstance.class)
				|| object.equals(StandaloneTaskInstance.class)) {
			return EntityIdType.TASK;
		} else if (object.equals(TemplateInstance.class) || object.equals(TemplateEntity.class)) {
			return EntityIdType.TEMPLATE_INSTANCE;
		}
		return EmfEntityIdType.UNKNOWN;
	}
}
