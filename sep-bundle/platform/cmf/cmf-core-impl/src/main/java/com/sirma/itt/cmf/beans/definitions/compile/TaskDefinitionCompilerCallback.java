package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionImpl;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;

/**
 * Callback implementation for task definitions.
 * <p>
 * <b>NOTE: this callback is disabled to manual loading. This means the callback will not be called
 * when top level definitions are loaded, but only via the template task definition loader.</b>
 * 
 * @author BBonev
 */
@DefinitionType(ObjectTypesCmf.STANDALONE_TASK)
public class TaskDefinitionCompilerCallback implements
		DefinitionCompilerCallback<TaskDefinitionImpl> {

	private static final String DMS_MODEL_PREFIX = "cmfwf:";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TaskDefinitionCompilerCallback.class);

	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private DefinitionCompilerHelper compiler;

	private boolean refMode;

	@Override
	public String getCallbackName() {
		return "task";
	}

	@Override
	public Class<TaskDefinitionImpl> getDefinitionClass() {
		return TaskDefinitionImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return com.sirma.itt.cmf.beans.jaxb.TaskDefinition.class;
	}

	@Override
	public XmlType getXmlValidationType() {
		return XmlType.TASK_DEFINITIONS;
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		if (refMode) {
			return Collections.emptyList();
		}
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	@Override
	public void setPropertyRevision(TaskDefinitionImpl definition) {
		Long revision = definition.getRevision();
		compiler.setPropertyRevision(definition, revision);
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compiler.setPropertyRevision(regionDefinition, revision);
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			compiler.setPropertyRevision(transitionDefinition, revision);
		}
	}

	@Override
	public void normalizeFields(TaskDefinitionImpl definition) {
		definition.initBidirection();

		if ((definition.getRevision() == null) || (definition.getRevision() == 0)) {
			definition.setRevision(1L);
		}

		String container = definition.getContainer();

		compiler.normalizeFields(definition.getFields(), definition, false, container);
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			((RegionDefinitionImpl) regionDefinition).setTemplate(true);
			compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
					container);
			if (regionDefinition.getControlDefinition() != null) {
				compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
						regionDefinition.getControlDefinition(), false, container);
			}
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			((TransitionDefinitionImpl) transitionDefinition).setTemplate(true);
			transitionDefinition.setOwnerPrefix(getCallbackName());
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false,
					container);
		}
		AllowedChildrenHelper.optimizeAllowedChildrenConfiguration(definition);
		AllowedChildrenHelper.removeNotSupportedChildrenTypes(definition,
				new HashSet<>(Arrays.asList(ObjectTypesCmf.CASE)));
	}

	@Override
	public String extractDefinitionId(TaskDefinitionImpl definition) {
		return definition.getIdentifier();
	}

	@Override
	public boolean updateReferences(TaskDefinitionImpl definition) {
		String referenceId = definition.getReferenceId();
		if (StringUtils.isNotNullOrEmpty(referenceId)) {
			TaskDefinitionTemplate template = dictionaryService.getDefinition(
					TaskDefinitionTemplate.class, referenceId);
			if (template == null) {
				return false;
			}
			definition.mergeTemplate(template);
		}
		// nothing to do here
		return true;
	}

	@Override
	public List<Label> getLabelDefinitions(Object definition) {
		if (definition instanceof com.sirma.itt.cmf.beans.jaxb.TaskDefinitions) {
			com.sirma.itt.cmf.beans.jaxb.TaskDefinitions definitions = (com.sirma.itt.cmf.beans.jaxb.TaskDefinitions) definition;
			Labels labels = definitions.getLabels();
			if (labels != null) {
				return labels.getLabel();
			}
		}
		return Collections.emptyList();
	}

	@Override
	public TaskDefinitionImpl saveTemplate(TaskDefinitionImpl definition) {
		return mutableDictionaryService.saveDefinition(definition);
	}

	@Override
	public TaskDefinitionImpl findTemplateInSystem(String identifier) {
		return dictionaryService.getDefinition(TaskDefinitionImpl.class, identifier);
	}

	@Override
	public void prepareForPersist(TaskDefinitionImpl definition) {
		compiler.synchRegionProperties(definition);

		compiler.setDefaultProperties(definition, DMS_MODEL_PREFIX, true);

		if ((definition.getExpression() != null)
				&& !compiler.validateExpression(definition, definition.getExpression())) {
			LOGGER.warn(" !!! Expression in task definition " + definition.getIdentifier() + " - "
					+ definition.getExpression() + " is not valid and will be removed !!!");
			definition.setExpression(null);
		}
		compiler.validateModelConditions(definition, definition);
		for (Iterator<TransitionDefinition> it = definition.getTransitions().iterator(); it
				.hasNext();) {
			TransitionDefinition transitionDefinition = it.next();
			// remove hidden definitions
			if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
				it.remove();
			}
			compiler.validateExpressions(definition, transitionDefinition);
			compiler.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
		}
		compiler.sort(definition.getTransitions());
	}

	@Override
	public boolean validateCompiledDefinition(TaskDefinitionImpl definition) {
		return compiler.executeValidators(definition);
	}

	@Override
	public void warmUpCache() {
		dictionaryService.getAllDefinitions(TaskDefinition.class);
	}

	@Override
	public void saveTemplateProperties(TaskDefinitionImpl newDefinition,
			TaskDefinitionImpl oldDefinition) {
		compiler.saveProperties(newDefinition, oldDefinition);
		for (TransitionDefinition transitionDefinition : newDefinition.getTransitions()) {
			compiler.saveProperties(
					transitionDefinition,
					WorkflowHelper.getTransitionById(oldDefinition,
							transitionDefinition.getIdentifier()));
		}
		newDefinition.initBidirection();
	}

	@Override
	public void setReferenceMode() {
		refMode = true;
	}

	@Override
	public List<?> getFilterDefinitions(Object source) {
		return Collections.emptyList();
	}

}
