package com.sirma.itt.cmf.beans.definitions;

import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.emf.definition.model.DefinitionTemplateHolder;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.Node;

/**
 * The Class DocumentsDefinition.
 *
 * @author BBonev
 */
public class DocumentsDefinition extends
		CommonTemplateHolder<DocumentDefinitionImpl, DocumentsDefinition> implements
		DefinitionTemplateHolder<DocumentDefinitionImpl>, MergeableTopLevelDefinition<DocumentsDefinition> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2333705854448604216L;
	/** The document definition. */
	private List<DocumentDefinitionImpl> templates = new LinkedList<DocumentDefinitionImpl>();

	/**
	 * Getter method for templates.
	 *
	 * @return the templates
	 */
	@Override
	public List<DocumentDefinitionImpl> getTemplates() {
		return templates;
	}

	/**
	 * Setter method for templates.
	 *
	 * @param templates
	 *            the templates to set
	 */
	@Override
	public void setTemplates(List<DocumentDefinitionImpl> templates) {
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
