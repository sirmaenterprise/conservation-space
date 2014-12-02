package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.DocumentsDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.jaxb.DocumentDefinitions;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.TemplateDefinition;
import com.sirma.itt.emf.definition.load.TemplateDefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.TemplateDefinitionType;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinitions;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * Callback implementation for document definitions.
 * 
 * @author BBonev
 */
@TemplateDefinition
@TemplateDefinitionType(ObjectTypesCmf.DOCUMENT)
public class DocumentDefinitionCompilerCallback implements
		TemplateDefinitionCompilerCallback<DocumentDefinitionImpl, DocumentsDefinition> {

	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private DefinitionCompilerHelper compiler;

	@Override
	public String getCallbackName() {
		return "document";
	}

	@Override
	public Class<DocumentDefinitionImpl> getDefinitionClass() {
		return DocumentDefinitionImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return DocumentDefinitions.class;
	}

	@Override
	public XmlType getXmlValidationType() {
		return XmlType.DOCUMENT_DEFINITIONS;
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	@Override
	public void setPropertyRevision(DocumentDefinitionImpl definition) {
		compiler.setPropertyRevision(definition, 0L);
		compiler.setTransactionalPropertyRevision(definition, 0L);
	}

	@Override
	public void normalizeFields(DocumentDefinitionImpl definition) {
		compiler.normalizeFields(definition.getFields(), definition, false,
				definition.getContainer());
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			((RegionDefinitionImpl) regionDefinition).setTemplate(true);
			compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
					definition.getContainer());
			if (regionDefinition.getControlDefinition() != null) {
				compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
						regionDefinition.getControlDefinition(), false, definition.getContainer());
			}
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			transitionDefinition.setOwnerPrefix(getCallbackName());
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false,
					definition.getContainer());
		}
	}

	@Override
	public String extractDefinitionId(DocumentDefinitionImpl definition) {
		return definition.getIdentifier();
	}

	@Override
	public boolean updateReferences(DocumentDefinitionImpl definition) {
		// nothing to do here
		return true;
	}

	@Override
	public List<Label> getLabelDefinitions(Object definition) {
		DocumentDefinitions dd = (DocumentDefinitions) definition;
		Labels labels = dd.getLabels();
		if (labels != null) {
			return labels.getLabel();
		}
		return Collections.emptyList();
	}

	@Override
	public List<FilterDefinition> getFilterDefinitions(Object definition) {
		DocumentDefinitions dd = (DocumentDefinitions) definition;
		FilterDefinitions filterDefinitions = dd.getFilterDefinitions();
		if (filterDefinitions != null) {
			return filterDefinitions.getFilter();
		}
		return Collections.emptyList();
	}

	@Override
	public DocumentDefinitionImpl saveTemplate(DocumentDefinitionImpl definition) {
		return mutableDictionaryService.saveTemplateDefinition(definition);
	}

	@Override
	public DocumentDefinitionImpl findTemplateInSystem(String identifier) {
		return (DocumentDefinitionImpl) dictionaryService.getDefinition(
				DocumentDefinitionTemplate.class, identifier);
	}

	@Override
	public void saveTemplateProperties(DocumentDefinitionImpl newDefinition,
			DocumentDefinitionImpl oldDefinition) {
		compiler.saveProperties(newDefinition, oldDefinition);
		newDefinition.initBidirection();
	}

	@Override
	public void prepareForPersist(DocumentDefinitionImpl definition) {
		compiler.setDefaultProperties(definition, "cmf:", true);
		compiler.synchRegionProperties(definition);
		compiler.validateModelConditions(definition, definition);

		for (Iterator<TransitionDefinition> it = definition.getTransitions().iterator(); it
				.hasNext();) {
			TransitionDefinition transitionDefinition = it.next();
			// remove hidden definitions
			if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
				it.remove();
			}
			compiler.validateExpressions(definition, transitionDefinition);
			compiler.setDefaultProperties(transitionDefinition, "cmf:");
		}
		compiler.sort(definition.getTransitions());
	}

	@Override
	public boolean validateCompiledDefinition(DocumentDefinitionImpl definition) {
		boolean valid = true;
		valid &= compiler.executeValidators(definition);
		return valid;
	}

	@Override
	public Class<DocumentsDefinition> getTemplateClass() {
		return DocumentsDefinition.class;
	}

	@Override
	public void warmUpCache() {
		dictionaryService.getAllDefinitions(DocumentDefinitionTemplate.class);
	}

	@Override
	public boolean isHybridDefinitionsSupported() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Pair<List<DocumentDefinitionImpl>, List<DocumentDefinitionImpl>> filerStandaloneDefinitions(
			List<DocumentDefinitionImpl> loadedDefinitions) {
		return new Pair<List<DocumentDefinitionImpl>, List<DocumentDefinitionImpl>>(
				Collections.EMPTY_LIST, Collections.EMPTY_LIST);
	}

	@Override
	public DefinitionCompilerCallback<TopLevelDefinition> getOtherCallback() {
		return null;
	}

	@Override
	public void setReferenceMode() {
		// nothing to do
	}

}
