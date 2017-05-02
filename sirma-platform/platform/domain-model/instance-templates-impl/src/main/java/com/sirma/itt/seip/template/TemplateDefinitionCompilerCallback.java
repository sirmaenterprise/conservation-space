package com.sirma.itt.seip.template;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerCallback;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.compile.DefinitionType;
import com.sirma.itt.seip.definition.jaxb.Label;
import com.sirma.itt.seip.definition.jaxb.Labels;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.template.jaxb.TemplateSchemaProvider;

/**
 * Implementation for loading template definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
@DefinitionType(TemplateProperties.TEMPLATE_TYPE)
public class TemplateDefinitionCompilerCallback implements DefinitionCompilerCallback<TemplateDefinitionImpl> {

	private static final String DMS_MODEL_PREFIX = "cmf:";

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private DefinitionCompilerHelper compiler;

	@Override
	public void setPropertyRevision(TemplateDefinitionImpl definition) {
		Long revision = definition.getRevision();
		compiler.setPropertyRevision(definition, revision);
	}

	@Override
	public void normalizeFields(TemplateDefinitionImpl definition) {
		definition.initBidirection();

		// initialize the revision
		if (definition.getRevision() == null || definition.getRevision() == 0) {
			definition.setRevision(1L);
		}

		String container = definition.getContainer();

		compiler.normalizeFields(definition.getFields(), definition, false, container);
	}

	@Override
	public String extractDefinitionId(TemplateDefinitionImpl definition) {
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(definition.getIdentifier())) {
			return definition.getIdentifier();
		}
		return null;
	}

	@Override
	public boolean updateReferences(TemplateDefinitionImpl caseDefinition, DefinitionReferenceResolver resolver) {
		return true;
	}

	@Override
	public List<Label> getLabelDefinitions(Object source) {
		Labels labels = null;
		if (source instanceof com.sirma.itt.seip.template.jaxb.TemplateDefinition) {
			com.sirma.itt.seip.template.jaxb.TemplateDefinition c = (com.sirma.itt.seip.template.jaxb.TemplateDefinition) source;
			labels = c.getLabels();
		}

		if (labels != null) {
			return labels.getLabel();
		}
		return Collections.emptyList();
	}

	@Override
	public TemplateDefinitionImpl saveTemplate(TemplateDefinitionImpl definition) {
		return definition;
	}

	@Override
	public TemplateDefinitionImpl findTemplateInSystem(String identifier) {
		return (TemplateDefinitionImpl) dictionaryService.getDefinition(TemplateDefinition.class, identifier);
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	@Override
	public Class<TemplateDefinitionImpl> getDefinitionClass() {
		return TemplateDefinitionImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return com.sirma.itt.seip.template.jaxb.TemplateDefinition.class;
	}

	@Override
	public XmlSchemaProvider getXmlValidationType() {
		return TemplateSchemaProvider.TEMPLATE_PROVIDER;
	}

	@Override
	public boolean validateCompiledDefinition(TemplateDefinitionImpl definition) {
		return compiler.executeValidators(definition);
	}

	@Override
	public String getCallbackName() {
		return "template";
	}

	@Override
	public void prepareForPersist(TemplateDefinitionImpl definition) {
		compiler.removeDeletedElements(definition);
		compiler.setDefaultProperties(definition, DMS_MODEL_PREFIX);
	}

	@Override
	public void warmUpCache() {
		// nothing to refresh
	}

	@Override
	public void saveTemplateProperties(TemplateDefinitionImpl newDefinition, TemplateDefinitionImpl oldDefinition) {
		// nothing to do here
	}

	@Override
	public void setReferenceMode() {
		// nothing to do here
	}

	@Override
	public List<?> getFilterDefinitions(Object source) {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getDependencies(TemplateDefinitionImpl value) {
		if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(value.getParentDefinitionId())) {
			return Collections.emptyList();
		}
		return Collections.singletonList(value.getParentDefinitionId());
	}

}
