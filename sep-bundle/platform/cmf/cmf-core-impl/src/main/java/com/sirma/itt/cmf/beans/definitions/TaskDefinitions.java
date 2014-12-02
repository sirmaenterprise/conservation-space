package com.sirma.itt.cmf.beans.definitions;

import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.emf.definition.model.DefinitionTemplateHolder;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.Node;

/**
 * The Class TasksDefinition.
 *
 * @author BBonev
 */
public class TaskDefinitions extends
		CommonTemplateHolder<TaskDefinitionTemplateImpl, TaskDefinitions> implements
		DefinitionTemplateHolder<TaskDefinitionTemplateImpl>, MergeableTopLevelDefinition<TaskDefinitions> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1016660839198577294L;
	/** The document definition. */
	private List<TaskDefinitionTemplateImpl> templates = new LinkedList<TaskDefinitionTemplateImpl>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaskDefinitionTemplateImpl> getTemplates() {
		return templates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTemplates(List<TaskDefinitionTemplateImpl> templates) {
		this.templates = templates;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

}
