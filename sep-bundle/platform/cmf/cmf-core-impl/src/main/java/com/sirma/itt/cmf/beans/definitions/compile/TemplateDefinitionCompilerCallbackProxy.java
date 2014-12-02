package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.impl.TemplateDefinitionImpl;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;

/**
 * Proxy implementation for loading concrete template from DMS. Instead of searching in DMS for
 * templates the class returns the desired file identifier and forces the compiler to download
 * specific file and process it. This class is not intended for CDI injection but rather for manual
 * call.
 * 
 * @author BBonev
 */
public class TemplateDefinitionCompilerCallbackProxy implements
		DefinitionCompilerCallback<TemplateDefinitionImpl> {

	private static final String DMS_MODEL_PREFIX = "cmf:";
	/** The compiler. */
	private final DefinitionCompilerHelper compiler;
	private final FileDescriptor definitionLocation;

	/**
	 * Instantiates a new template definition compiler callback proxy.
	 * 
	 * @param compiler
	 *            the compiler to use for helper methods.
	 * @param definitionLocation
	 *            the definition location to download
	 */
	public TemplateDefinitionCompilerCallbackProxy(DefinitionCompilerHelper compiler,
			FileDescriptor definitionLocation) {
		this.compiler = compiler;
		this.definitionLocation = definitionLocation;
	}

	@Override
	public void setPropertyRevision(TemplateDefinitionImpl definition) {
		Long revision = definition.getRevision();
		compiler.setPropertyRevision(definition, revision);
	}

	@Override
	public void normalizeFields(TemplateDefinitionImpl definition) {
		definition.initBidirection();

		// initialize the revision
		if ((definition.getRevision() == null) || (definition.getRevision() == 0)) {
			definition.setRevision(1L);
		}

		String container = definition.getContainer();

		compiler.normalizeFields(definition.getFields(), definition, false, container);
	}

	@Override
	public String extractDefinitionId(TemplateDefinitionImpl definition) {
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(definition
				.getIdentifier())) {
			return definition.getIdentifier();
		}
		return null;
	}

	@Override
	public boolean updateReferences(TemplateDefinitionImpl caseDefinition) {
		return true;
	}

	@Override
	public List<Label> getLabelDefinitions(Object source) {
		Labels labels = null;
		if (source instanceof com.sirma.itt.cmf.beans.jaxb.TemplateDefinition) {
			com.sirma.itt.cmf.beans.jaxb.TemplateDefinition c = (com.sirma.itt.cmf.beans.jaxb.TemplateDefinition) source;
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
		return null;
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		return Arrays.asList(definitionLocation);
	}

	@Override
	public Class<TemplateDefinitionImpl> getDefinitionClass() {
		return TemplateDefinitionImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return com.sirma.itt.cmf.beans.jaxb.TemplateDefinition.class;
	}

	@Override
	public XmlType getXmlValidationType() {
		return XmlType.TEMPLATE_DEFINITION;
	}

	@Override
	public boolean validateCompiledDefinition(TemplateDefinitionImpl definition) {
		boolean valid = compiler.executeValidators(definition);
		return valid;
	}

	@Override
	public String getCallbackName() {
		return "template";
	}

	@Override
	public void prepareForPersist(TemplateDefinitionImpl definition) {
		compiler.setDefaultProperties(definition, DMS_MODEL_PREFIX);
	}

	@Override
	public void warmUpCache() {
		// nothing to refresh
	}

	@Override
	public void saveTemplateProperties(TemplateDefinitionImpl newDefinition,
			TemplateDefinitionImpl oldDefinition) {
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

}
