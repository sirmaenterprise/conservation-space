package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.cmf.beans.jaxb.TaskDefinitions;
import com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinition;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;

/**
 * Implementation for loading workflow definitions.
 * 
 * @author BBonev
 */
@Definition
@DefinitionType(ObjectTypesCmf.WORKFLOW)
public class WorkflowDefinitionCompilerCallback implements
		DefinitionCompilerCallback<WorkflowDefinitionImpl> {

	private static final String DMS_MODEL_PREFIX = "cmfwf:";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkflowDefinitionCompilerCallback.class);

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
		return "workflow";
	}

	@Override
	public Class<WorkflowDefinitionImpl> getDefinitionClass() {
		return WorkflowDefinitionImpl.class;
	}

	@Override
	public Class<?> getMappingClass() {
		return WorkflowDefinition.class;
	}

	@Override
	public XmlType getXmlValidationType() {
		return XmlType.WORKFLOW_DEFINITION;
	}

	@Override
	public List<FileDescriptor> getDefinitions() {
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	@Override
	public void setPropertyRevision(WorkflowDefinitionImpl definition) {
		Long revision = definition.getRevision();
		compiler.setPropertyRevision(definition, revision);
		for (TaskDefinitionRef taskDef : definition.getTasks()) {
			compiler.setPropertyRevision(taskDef, revision);
			for (RegionDefinition regionDef : taskDef.getRegions()) {
				compiler.setPropertyRevision(regionDef, revision);
			}
			for (TransitionDefinition transitionDefinition : taskDef.getTransitions()) {
				compiler.setPropertyRevision(transitionDefinition, revision);
			}
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			compiler.setPropertyRevision(transitionDefinition, revision);
		}
	}

	@Override
	public void normalizeFields(WorkflowDefinitionImpl definition) {
		definition.initBidirection();

		definition.setCreationDate(new Date());
		definition.setLastModifiedDate(new Date());
		// initialize the revision
		if ((definition.getRevision() == null) || (definition.getRevision() == 0)) {
			definition.setRevision(1L);
		}

		List<PropertyDefinition> fields = definition.getFields();

		String container = definition.getContainer();

		compiler.normalizeFields(fields, definition, false, container);
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
					container);
			if (regionDefinition.getControlDefinition() != null) {
				compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
						regionDefinition.getControlDefinition(), false, container);
			}
		}
		List<TaskDefinitionRef> taskDefs = definition.getTasks();
		for (TaskDefinitionRef taskDef : taskDefs) {
			compiler.normalizeFields(taskDef.getFields(), taskDef, false, container);
			for (RegionDefinition regionDefinition : taskDef.getRegions()) {
				compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
						container);
				if (regionDefinition.getControlDefinition() != null) {
					compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
							regionDefinition.getControlDefinition(), false, container);
				}
			}
			for (TransitionDefinition transitionDefinition : taskDef.getTransitions()) {
				transitionDefinition.setOwnerPrefix("task");
				compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition,
						false, container);
			}
		}

		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			transitionDefinition.setOwnerPrefix(getCallbackName());
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false,
					container);
		}

		AllowedChildrenHelper.optimizeAllowedChildrenConfiguration(definition);
		AllowedChildrenHelper.removeNotSupportedChildrenTypes(definition,
				new HashSet<>(Arrays.asList(ObjectTypesCmf.CASE)));
	}

	@Override
	public String extractDefinitionId(WorkflowDefinitionImpl definition) {
		return definition.getIdentifier();
	}

	@Override
	public boolean updateReferences(WorkflowDefinitionImpl definition) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Updating task definition references for " + definition.getIdentifier());
		}
		boolean trace = LOGGER.isTraceEnabled();
		List<TaskDefinitionRef> taskDefinitionRefs = definition.getTasks();
		int updatedDocs = 0;
		if (trace) {
			LOGGER.trace("Processing workflow " + definition.getIdentifier() + " with "
					+ taskDefinitionRefs + " task definitions");
		}
		for (Iterator<TaskDefinitionRef> it = taskDefinitionRefs.iterator(); it.hasNext();) {
			TaskDefinitionRef taskDefinitionRef = it.next();

			// convert document references to actual definitions
			// if reference cannot be resolved then process is stopped and case
			// definition is not persisted we do nothing if the reference is present
			if (((TaskDefinitionRefImpl) taskDefinitionRef).getDefinitionTemplate() != null) {
				continue;
			}

			String referenceId = taskDefinitionRef.getReferenceTaskId();
			if (StringUtils.isEmpty(referenceId)) {
				LOGGER.warn("No task reference in definition: " + taskDefinitionRef);
				it.remove();
			} else {
				referenceId += DefinitionIdentityUtil.SEPARATOR + definition.getContainer();
				// get the complete document definition
				TaskDefinitionTemplate definitionTemplate = dictionaryService.getDefinition(
						TaskDefinitionTemplate.class, referenceId);
				if (definitionTemplate == null) {
					LOGGER.error("Missing task definition: "
							+ referenceId
							+ ". Check if the ID is correct or add new task definition in you definitions file.");
					return false;
				}
				((TaskDefinitionRefImpl) taskDefinitionRef)
						.setDefinitionTemplate(definitionTemplate);

				updatedDocs++;
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Updated " + updatedDocs + " task definitions in workflow "
						+ definition.getIdentifier());
			}
		}
		return true;
	}

	@Override
	public List<Label> getLabelDefinitions(Object source) {
		if (source instanceof com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition) {
			com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition wd = (com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition) source;
			List<Label> list = new LinkedList<Label>();
			Labels labels = wd.getLabels();
			if (labels != null) {
				list.addAll(labels.getLabel());
			}
			TaskDefinitions taskDefinitions = wd.getTaskDefinitions();
			if ((taskDefinitions != null) && (taskDefinitions.getLabels() != null)) {
				list.addAll(taskDefinitions.getLabels().getLabel());
			}
			return list;
		}
		return Collections.emptyList();
	}

	@Override
	public WorkflowDefinitionImpl saveTemplate(WorkflowDefinitionImpl definition) {
		return mutableDictionaryService.saveDefinition(definition);
	}

	@Override
	public WorkflowDefinitionImpl findTemplateInSystem(String identifier) {
		return (WorkflowDefinitionImpl) dictionaryService.getDefinition(
				com.sirma.itt.cmf.beans.definitions.WorkflowDefinition.class, identifier);
	}

	@Override
	public void prepareForPersist(WorkflowDefinitionImpl definition) {
		if (definition.getAbstract() == null) {
			definition.setAbstract(Boolean.FALSE);
		}

		if ((definition.getExpression() != null)
				&& !compiler.validateExpression(definition, definition.getExpression())) {
			LOGGER.warn(" !!! Expression in workflow definition " + definition.getIdentifier()
					+ " - " + definition.getExpression() + " is not valid and will be removed !!!");
			definition.setExpression(null);
		}
		compiler.validateModelConditions(definition, definition);

		for (TaskDefinitionRef taskDefinitionRef : definition.getTasks()) {
			compiler.synchRegionProperties(taskDefinitionRef);
			compiler.setDefaultProperties(taskDefinitionRef, DMS_MODEL_PREFIX, true);

			// copy all properties to the root of the task as system fields
			// they will be used for property conversion
			// all fields from the workflow should be present once in a task no matter were in the
			// task

			synchWfFields(taskDefinitionRef, definition.getFields(), true);

			if ((taskDefinitionRef.getExpression() != null)
					&& !compiler.validateExpression(taskDefinitionRef,
							taskDefinitionRef.getExpression())) {
				LOGGER.warn(" !!! Expression in taskRef definition "
						+ taskDefinitionRef.getIdentifier() + " - "
						+ taskDefinitionRef.getExpression()
						+ " is not valid and will be removed !!!");
				((TaskDefinitionRefImpl) taskDefinitionRef).setExpression(null);
			}

			compiler.validateModelConditions(taskDefinitionRef, taskDefinitionRef);

			for (Iterator<TransitionDefinition> it = taskDefinitionRef.getTransitions().iterator(); it
					.hasNext();) {
				TransitionDefinition transitionDefinition = it.next();
				// remove hidden definitions
				if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
					it.remove();
				}
				compiler.validateExpressions(taskDefinitionRef, transitionDefinition);
				compiler.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
			}
			compiler.sort(taskDefinitionRef.getTransitions());
		}

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

		// merge base fields to all regions in the workflow definition
		// because at the end if the fields are duplicate some of them will be removed
		// thats why this code is at the end of the method when all fields from the base level are
		// synchronized
		synchWfFields(definition, definition.getFields(), false);
		// first merge properties from parent fields and then set default values
		compiler.setDefaultProperties(definition, DMS_MODEL_PREFIX, true);
		compiler.synchRegionProperties(definition);
	}

	/**
	 * Synch wf fields.
	 * 
	 * @param target
	 *            the target
	 * @param fields
	 *            the fields
	 * @param updateBaseLevel
	 *            the update base level
	 */
	private void synchWfFields(RegionDefinitionModel target, List<PropertyDefinition> fields,
			boolean updateBaseLevel) {
		if (updateBaseLevel) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(target.getFields()),
					MergeHelper.convertToMergable(fields));
		}

		for (RegionDefinition regionDefinition : target.getRegions()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(regionDefinition.getFields()),
					MergeHelper.convertToMergable(fields));
		}
	}

	@Override
	public boolean validateCompiledDefinition(WorkflowDefinitionImpl definition) {
		boolean valid = true;
		valid &= compiler.executeValidators(definition);
		for (TaskDefinitionRef definitionRef : definition.getTasks()) {
			valid &= compiler.executeValidators(definitionRef);
		}
		return valid;
	}

	@Override
	public void warmUpCache() {
		dictionaryService
				.getAllDefinitions(com.sirma.itt.cmf.beans.definitions.WorkflowDefinition.class);
	}

	@Override
	public void saveTemplateProperties(WorkflowDefinitionImpl newDefinition,
			WorkflowDefinitionImpl oldDefinition) {
		compiler.saveProperties(newDefinition, oldDefinition);
		for (TaskDefinitionRef taskDefinitionRef : newDefinition.getTasks()) {
			compiler.saveProperties(taskDefinitionRef,
					WorkflowHelper.getTaskById(oldDefinition, taskDefinitionRef.getIdentifier()));
			for (TransitionDefinition transitionDefinition : taskDefinitionRef.getTransitions()) {
				compiler.saveProperties(
						transitionDefinition,
						WorkflowHelper.getTransitionById(
								WorkflowHelper.getTaskById(oldDefinition,
										taskDefinitionRef.getIdentifier()),
								transitionDefinition.getIdentifier()));
			}
		}
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
		// nothing to do here
	}

	@Override
	public List<FilterDefinition> getFilterDefinitions(Object source) {
		if (source instanceof com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition) {
			com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition wd = (com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition) source;
			TaskDefinitions taskDefinitions = wd.getTaskDefinitions();
			if ((taskDefinitions != null) && (taskDefinitions.getFilterDefinitions() != null)) {
				return taskDefinitions.getFilterDefinitions().getFilter();
			}
		}
		return Collections.emptyList();
	}

}
