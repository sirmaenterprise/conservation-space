package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitions;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.load.TemplateDefinition;
import com.sirma.itt.emf.definition.load.TemplateDefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.TemplateDefinitionType;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinitions;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;

/**
 * Callback implementation for task definitions.
 * 
 * @author BBonev
 */
@TemplateDefinition
@TemplateDefinitionType(ObjectTypesCmf.WORKFLOW_TASK)
public class TaskDefinitionTemplateCompilerCallback implements
		TemplateDefinitionCompilerCallback<TaskDefinitionTemplateImpl, TaskDefinitions> {

	private static final String DMS_MODEL_PREFIX = "cmfwf:";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TaskDefinitionTemplateCompilerCallback.class);

	@Inject
	private MutableDictionaryService mutableDictionaryService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private DefinitionCompilerHelper compiler;

	@Inject
	@DefinitionType(ObjectTypesCmf.STANDALONE_TASK)
	private DefinitionCompilerCallback<TopLevelDefinition> standaloneTaskCallback;

	@Override
	public String getCallbackName() {
		return "taskTemplate";
	}

	@Override
	public Class<TaskDefinitionTemplateImpl> getDefinitionClass() {
		return TaskDefinitionTemplateImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return com.sirma.itt.cmf.beans.jaxb.TaskDefinitions.class;
	}

	@Override
	public XmlType getXmlValidationType() {
		return XmlType.TASK_DEFINITIONS;
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	@Override
	public void setPropertyRevision(TaskDefinitionTemplateImpl definition) {
		Long revision = 0L;
		compiler.setPropertyRevision(definition, revision);
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compiler.setPropertyRevision(regionDefinition, revision);
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			compiler.setPropertyRevision(transitionDefinition, revision);
		}
	}

	@Override
	public void normalizeFields(TaskDefinitionTemplateImpl definition) {
		definition.initBidirection();

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
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false,
					container);
		}

		AllowedChildrenHelper.optimizeAllowedChildrenConfiguration(definition);
		AllowedChildrenHelper.removeNotSupportedChildrenTypes(definition,
				new HashSet<>(Arrays.asList(ObjectTypesCmf.CASE)));
	}

	@Override
	public String extractDefinitionId(TaskDefinitionTemplateImpl definition) {
		return definition.getIdentifier();
	}

	@Override
	public boolean updateReferences(TaskDefinitionTemplateImpl definition) {
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
	public List<FilterDefinition> getFilterDefinitions(Object definition) {
		if (definition instanceof com.sirma.itt.cmf.beans.jaxb.TaskDefinitions) {
			com.sirma.itt.cmf.beans.jaxb.TaskDefinitions definitions = (com.sirma.itt.cmf.beans.jaxb.TaskDefinitions) definition;
			FilterDefinitions filterDefinitions = definitions.getFilterDefinitions();
			if (filterDefinitions != null) {
				return filterDefinitions.getFilter();
			}
		}
		return Collections.emptyList();
	}

	@Override
	public TaskDefinitionTemplateImpl saveTemplate(TaskDefinitionTemplateImpl definition) {
		return mutableDictionaryService.saveTemplateDefinition(definition);
	}

	@Override
	public TaskDefinitionTemplateImpl findTemplateInSystem(String identifier) {
		return (TaskDefinitionTemplateImpl) dictionaryService.getDefinition(
				TaskDefinitionTemplate.class, identifier);
	}

	@Override
	public void prepareForPersist(TaskDefinitionTemplateImpl definition) {
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
	public boolean validateCompiledDefinition(TaskDefinitionTemplateImpl definition) {
		boolean valid = true;
		valid &= compiler.executeValidators(definition);
		return valid;
	}

	@Override
	public Class<TaskDefinitions> getTemplateClass() {
		return TaskDefinitions.class;
	}

	@Override
	public void warmUpCache() {
		dictionaryService.getAllDefinitions(TaskDefinitionTemplate.class);
	}

	@Override
	public void saveTemplateProperties(TaskDefinitionTemplateImpl newDefinition,
			TaskDefinitionTemplateImpl oldDefinition) {
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
	public boolean isHybridDefinitionsSupported() {
		return true;
	}

	@Override
	public Pair<List<TaskDefinitionTemplateImpl>, List<TaskDefinitionTemplateImpl>> filerStandaloneDefinitions(
			List<TaskDefinitionTemplateImpl> loadedDefinitions) {
		List<TaskDefinitionTemplateImpl> standAlone = new LinkedList<>();
		List<TaskDefinitionTemplateImpl> templates = new LinkedList<>();

		for (TaskDefinitionTemplateImpl impl : loadedDefinitions) {
			if (Boolean.TRUE.equals(impl.getStandalone())) {
				standAlone.add(impl);
			} else {
				templates.add(impl);
			}
		}
		return new Pair<>(templates, standAlone);
	}

	@Override
	public DefinitionCompilerCallback<TopLevelDefinition> getOtherCallback() {
		standaloneTaskCallback.setReferenceMode();
		return standaloneTaskCallback;
	}

	@Override
	public void setReferenceMode() {
		// do nothing here
	}

}
