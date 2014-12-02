package com.sirma.itt.pm.config;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.xml.XmlSchemaProvider;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;
import com.sirma.itt.pm.domain.jaxb.ProjectDefinition;
import com.sirma.itt.pm.xml.schema.PmSchemaBuilder;

/**
 * Compiler call back for project definitions
 *
 * @author BBonev
 */
@Definition
@DefinitionType(ObjectTypesPm.PROJECT)
public class ProjectDefinitionCompilerCallback implements
		DefinitionCompilerCallback<ProjectDefinitionImpl> {
	private static final String DMS_MODEL_PREFIX = "pm:";
	/** The logger. */
	@Inject
	private Logger LOGGER;
	/** The mutable dictionary service. */
	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;
	/** The definition management service. */
	@Inject
	private DefinitionManagementService definitionManagementService;
	@Inject
	private DefinitionCompilerHelper compilerHelper;

	@Override
	public void warmUpCache() {
		dictionaryService
				.getAllDefinitions(com.sirma.itt.pm.domain.definitions.ProjectDefinition.class);
	}

	@Override
	public String getCallbackName() {
		return "project";
	}

	@Override
	public Class<ProjectDefinitionImpl> getDefinitionClass() {
		return ProjectDefinitionImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return ProjectDefinition.class;
	}

	@Override
	public XmlSchemaProvider getXmlValidationType() {
		return new XmlSchemaProvider() {

			@Override
			public String getMainSchemaFileLocation() {
				return "project.xsd";
			}

			@Override
			public String getIdentifier() {
				return "project";
			}

			@Override
			public String[] getAdditionalSchemaLocations() {
				return new String[] { "../../../cmf/xml/schema/common.xsd" };
			}

			@Override
			public Class<?> baseLoaderClass() {
				return PmSchemaBuilder.class;
			}
		};
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	@Override
	public void setPropertyRevision(ProjectDefinitionImpl definition) {
		compilerHelper.setPropertyRevision(definition, definition.getRevision());
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			compilerHelper.setPropertyRevision(transitionDefinition, definition.getRevision());
		}
	}

	@Override
	public void normalizeFields(ProjectDefinitionImpl definition) {
		definition.initBidirection();

		definition.setCreationDate(new Date());
		definition.setLastModifiedDate(new Date());
		// initialize the revision
		if ((definition.getRevision() == null) || (definition.getRevision() == 0)) {
			definition.setRevision(1L);
		}

		List<PropertyDefinition> fields = definition.getFields();

		String container = definition.getContainer();

		compilerHelper.normalizeFields(fields, definition, false, definition.getContainer());
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compilerHelper.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
					container);
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			transitionDefinition.setOwnerPrefix(getCallbackName());
			compilerHelper.normalizeFields(transitionDefinition.getFields(), transitionDefinition,
					false, container);
		}

		AllowedChildrenHelper.optimizeAllowedChildrenConfiguration(definition);
	}

	@Override
	public String extractDefinitionId(ProjectDefinitionImpl definition) {
		return definition.getIdentifier();
	}

	@Override
	public boolean updateReferences(ProjectDefinitionImpl definition) {
		// no references to update
		return true;
	}

	@Override
	public List<Label> getLabelDefinitions(Object definition) {
		Labels labels = null;
		if (definition instanceof ProjectDefinition) {
			ProjectDefinition projectDefinition = (ProjectDefinition) definition;
			labels = projectDefinition.getLabels();
		}

		if (labels != null) {
			return labels.getLabel();
		}
		return Collections.emptyList();
	}

	@Override
	public ProjectDefinitionImpl saveTemplate(ProjectDefinitionImpl definition) {
		return mutableDictionaryService.saveDefinition(definition);
	}

	@Override
	public ProjectDefinitionImpl findTemplateInSystem(String identifier) {
		return dictionaryService.getDefinition(ProjectDefinitionImpl.class, identifier);
	}

	@Override
	public void saveTemplateProperties(ProjectDefinitionImpl newDefinition,
			ProjectDefinitionImpl oldDefinition) {
		compilerHelper.saveProperties(newDefinition, oldDefinition);
		for (TransitionDefinition transitionDefinition : newDefinition.getTransitions()) {
			compilerHelper.saveProperties(
					transitionDefinition,
					WorkflowHelper.getTransitionById(oldDefinition,
							transitionDefinition.getIdentifier()));
		}
		newDefinition.initBidirection();
	}

	@Override
	public void prepareForPersist(ProjectDefinitionImpl definition) {
		if (definition.getAbstract() == null) {
			definition.setAbstract(Boolean.FALSE);
		}
		compilerHelper.synchRegionProperties(definition);
		compilerHelper.setDefaultProperties(definition, DMS_MODEL_PREFIX, true);
		if ((definition.getExpression() != null)
				&& !compilerHelper.validateExpression(definition, definition.getExpression())) {
			LOGGER.warn(" !!! Expression in case definition " + definition.getIdentifier() + " - "
					+ definition.getExpression() + " is not valid and will be removed !!!");
			definition.setExpression(null);
		}
		compilerHelper.validateModelConditions(definition, definition);

		for (Iterator<TransitionDefinition> it = definition.getTransitions().iterator(); it
				.hasNext();) {
			TransitionDefinition transitionDefinition = it.next();
			// remove hidden definitions
			if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
				it.remove();
			}
			compilerHelper.validateExpressions(definition, transitionDefinition);
			compilerHelper.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
		}
		compilerHelper.sort(definition.getTransitions());
	}

	@Override
	public boolean validateCompiledDefinition(ProjectDefinitionImpl definition) {
		boolean valid = true;
		valid &= compilerHelper.executeValidators(definition);
		// for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
		// valid &= compiler.executeValidators(transitionDefinition);
		// }
		return valid;
	}

	@Override
	public void setReferenceMode() {
	}

	@Override
	public List<?> getFilterDefinitions(Object source) {
		return Collections.emptyList();
	}

}
