package com.sirma.itt.cmf.security.provider;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.provider.BaseDefinitionActionProvider;

/**
 * Action provider for document operations.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 20)
public class DocumentActionProvider extends BaseDefinitionActionProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<? extends DefinitionModel> getDefinitions() {
		List<CaseDefinition> allDefinitions = dictionaryService
				.getAllDefinitions(CaseDefinition.class);
		List<DefinitionModel> definitions = new LinkedList<DefinitionModel>();
		for (CaseDefinition caseDefinition : allDefinitions) {
			for (SectionDefinition sectionDefinition : caseDefinition.getSectionDefinitions()) {
				definitions.addAll(sectionDefinition.getDocumentDefinitions());
			}
		}
		return definitions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		return DocumentInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return DocumentDefinitionRef.class;
	}

}
