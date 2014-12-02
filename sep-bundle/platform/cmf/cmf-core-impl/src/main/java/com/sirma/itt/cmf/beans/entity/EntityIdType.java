/*
 *
 */
package com.sirma.itt.cmf.beans.entity;

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
import com.sirma.itt.emf.domain.model.PathElementProxy;
import com.sirma.itt.emf.entity.CommonEntity;
import com.sirma.itt.emf.forum.entity.CommentEntity;
import com.sirma.itt.emf.forum.entity.TopicEntity;
import com.sirma.itt.emf.forum.model.ChatInstance;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.emf.template.TemplateInstance;

/**
 * The Enum EntityIdType.
 * 
 * @author BBonev
 */
public enum EntityIdType implements EntityType {

	/** The unknown. */
	UNKNOWN(0),
	/** The case. */
	CASE(1),
	/** The section. */
	SECTION(2),
	/** The document. */
	DOCUMENT(3),
	/** The workflow. */
	WORKFLOW(4),
	/** The workflow task. */
	TASK(5),
	/** The user. */
	USER(6),
	/** The {@link CommonInstance} representation type. */
	INSTANCE(7),
	/** The value instance. */
	VALUE_INSTANCE(8),
	/** The link instance. */
	LINK_INSTANCE(9),
	/** The topic instance. */
	TOPIC_INSTANCE(10),
	/** The comment instance. */
	COMMENT_INSTANCE(11),
	/** The comment instance. */
	TEMPLATE_INSTANCE(14);

	/** The id. */
	private int id;

	/**
	 * Instantiates a new entity id type.
	 * 
	 * @param id
	 *            the id
	 */
	private EntityIdType(int id) {
		this.id = id;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public int getType() {
		return id;
	}

	/**
	 * Gets the {@link EntityIdType} by the given object instance. If the object instance is not
	 * recognized then {@link #UNKNOWN} type will be returned.
	 * <p>
	 * NOTE: this is implementation specific method!
	 * 
	 * @param object
	 *            the object
	 * @return the type
	 */
	public static EntityIdType getType(Object object) {
		if ((object instanceof CaseInstance) || (object instanceof CaseEntity)
				|| (object instanceof CaseDefinition)) {
			return CASE;
		} else if ((object instanceof SectionInstance) || (object instanceof SectionEntity)
				|| (object instanceof FolderInstance)
				|| (object instanceof SectionDefinition)) {
			return SECTION;
		} else if ((object instanceof DocumentInstance) || (object instanceof DocumentEntity)
				|| (object instanceof DocumentDefinitionRef)) {
			return DOCUMENT;
		} else if ((object instanceof WorkflowInstanceContext)
				|| (object instanceof WorkflowInstanceContextEntity)
				|| (object instanceof WorkflowDefinition)) {
			return WORKFLOW;
		} else if ((object instanceof CommonInstance) || (object instanceof CommonEntity)) {
			return INSTANCE;
		} else if ((object instanceof LinkInstance) || (object instanceof LinkEntity)
				|| (object instanceof LinkReference)) {
			return LINK_INSTANCE;
		} else if ((object instanceof TaskInstance)
				|| (object instanceof StandaloneTaskInstance)
				|| (object instanceof TaskDefinitionRef)
				|| (object instanceof TaskDefinition)
				|| ((object instanceof PathElementProxy) && (((PathElementProxy) object)
						.getTarget() instanceof TaskInstance))) {
			return TASK;
		} else if ((object instanceof TopicInstance) || (object instanceof TopicEntity)) {
			return TOPIC_INSTANCE;
		} else if ((object instanceof CommentInstance) || (object instanceof ChatInstance)
				|| (object instanceof CommentEntity)) {
			return COMMENT_INSTANCE;
		} else if ((object instanceof TemplateInstance) || (object instanceof TemplateEntity)) {
			return TEMPLATE_INSTANCE;
		} else {
			// TODO: add checks for other supported types
			return UNKNOWN;
		}

	}

	@Override
	public int getTypeId() {
		return getType();
	}

	@Override
	public String getName() {
		return toString();
	}
}
