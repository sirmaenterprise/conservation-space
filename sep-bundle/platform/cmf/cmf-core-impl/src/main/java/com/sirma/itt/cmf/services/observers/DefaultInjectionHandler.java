/*
 *
 */
package com.sirma.itt.cmf.services.observers;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskOpenEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskOpenEvent;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;

/**
 * Handler class for create and open operation. The handler processes the inject operations from
 * case and documents to tasks
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DefaultInjectionHandler {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInjectionHandler.class);
	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;
	/** The evaluator manager. */
	@Inject
	private ExpressionsManager evaluatorManager;

	@Inject
	private CaseService caseService;

	/**
	 * Listens for task open event.
	 * 
	 * @param taskEvent
	 *            the task event
	 */
	public void taskOpened(@Observes TaskOpenEvent taskEvent) {
		TaskInstance taskInstance = taskEvent.getInstance();

		performInjection(taskInstance, taskEvent, refreshCaseInstance(taskInstance));
	}

	/**
	 * Standalone task opened.
	 * 
	 * @param event
	 *            the event
	 */
	public void standaloneTaskOpened(@Observes StandaloneTaskOpenEvent event) {
		performInjection(event.getInstance(), event, refreshCaseInstance(event.getInstance()));
	}

	/**
	 * On instance changed.
	 * 
	 * @param event
	 *            the event
	 */
	public void onInstanceChanged(@Observes InstanceChangeEvent<?> event) {
		performInjection(event.getInstance(), event, refreshCaseInstance(event.getInstance()));
	}

	/**
	 * Refresh the case instance.
	 * 
	 * @param instance
	 *            the task instance
	 * @return the case instance
	 */
	// REVIEW: why instanceService.refresh doesn't do the job?!
	private CaseInstance refreshCaseInstance(Instance instance) {
		if (instance == null) {
			return null;
		}
		CaseInstance caseInstance = InstanceUtil.getParent(CaseInstance.class, instance);
		if (caseInstance == null) {
			return null;
		}
		return caseService.loadByDbId(caseInstance.getId());
	}

	/**
	 * Perform injection.
	 * 
	 * @param instance
	 *            the task instance
	 * @param taskEvent
	 *            the task event
	 * @param contexts
	 *            the contexts
	 */
	private void performInjection(Instance instance, EmfEvent taskEvent, Instance... contexts) {
		// first check if the instance could be evaluated
		Instance[] args = contexts;
		if ((args == null) || (args.length == 0)) {
			if (instance instanceof OwnedModel) {
				InstanceReference reference = ((OwnedModel) instance).getOwningReference();
				if ((reference == null) || StringUtils.isNullOrEmpty(reference.getIdentifier())) {
					// if we does not have a reference then we cannot inject something from
					// nothing....
					return;
				}
				List<Instance> parentPath = InstanceUtil.getParentPath(instance, true);
				// load properties of the instance
				args = parentPath.toArray(new Instance[parentPath.size()]);
			} else {
				LOGGER.warn("Current instance is not of type " + OwnedModel.class);
				return;
			}
		}

		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);

		if (definition == null) {
			LOGGER.debug("{} definition {} not found. No injection will be done.", instance
					.getClass().getSimpleName(), instance.getIdentifier());
			return;
		}

		// collect all fields that are marked for injection
		Set<PropertyDefinition> injections = new LinkedHashSet<PropertyDefinition>();
		Set<PropertyDefinition> checklists = new LinkedHashSet<PropertyDefinition>();
		boolean modified;
		if (definition instanceof RegionDefinitionModel) {
			modified = processFields(instance, (RegionDefinitionModel) definition, injections,
					checklists);
		} else {
			modified = processFields(instance, definition, injections, checklists);
		}
		if (injections.isEmpty() && checklists.isEmpty()) {
			LOGGER.trace("No field definitions found that need injection or automatic checks.");
			return;
		}
		LOGGER.trace("Found {} fields for injection and {} checklists", injections.size(),
				checklists.size());

		ExpressionContext context = evaluatorManager.createDefaultContext(instance, null, null);
		Map<String, Serializable> extractedFields = evaluatorManager.evaluateRules(injections,
				context, true, (Serializable[]) args);
		instance.getProperties().putAll(extractedFields);
		LOGGER.trace("Processed fields for injection: {}", extractedFields);

		Map<String, Serializable> map = PropertiesUtil.evaluateDefaultValues(checklists,
				instance.getRevision(), evaluatorManager, context);
		// merge properties with override
		if (PropertiesUtil.mergeProperties(map, instance.getProperties(), true)
				&& (taskEvent instanceof HandledEvent)) {
			((HandledEvent) taskEvent).setHandled(true);
		}

		// if we have any data extracted then we should mark the task instance for saving
		if ((taskEvent instanceof HandledEvent) && (!extractedFields.isEmpty() || modified)) {
			((HandledEvent) taskEvent).setHandled(true);
		}
	}

	/**
	 * Iterate all fields and collect the one with expression control. Also populates the default
	 * properties from the definition if any.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @param model
	 *            the model
	 * @param injections
	 *            the fields
	 * @param checklists
	 *            the checklists
	 * @return true, if properties were modified
	 */
	private boolean processFields(Instance taskInstance, RegionDefinitionModel model,
			Set<PropertyDefinition> injections, Set<PropertyDefinition> checklists) {
		boolean modified = false;
		modified |= processFields(taskInstance, (DefinitionModel) model, injections, checklists);
		for (RegionDefinition regionDefinition : model.getRegions()) {
			modified |= processFields(taskInstance, regionDefinition, injections, checklists);
		}
		return modified;
	}

	/**
	 * Iterate all fields and collect the one with expression control. Also populates the default
	 * properties from the definition if any.
	 * 
	 * @param instance
	 *            the task instance
	 * @param model
	 *            the model
	 * @param injections
	 *            the fields
	 * @param checklists
	 *            the checklists
	 * @return true, if properties were modified
	 */
	private boolean processFields(Instance instance, DefinitionModel model,
			Set<PropertyDefinition> injections, Set<PropertyDefinition> checklists) {
		// fill default properties
		int oldSize = instance.getProperties().size();
		// probably this is not needed anymore due to the fact that is executed on task creation but
		// it's not a problem also just minor performance issue
		PropertiesUtil.populateProperties(instance, model.getFields(), evaluatorManager, false);

		for (PropertyDefinition propertyDefinition : model.getFields()) {
			if ((propertyDefinition.getControlDefinition() != null)
					&& (propertyDefinition.getControlDefinition().getIdentifier() != null)) {
				String controlId = propertyDefinition.getControlDefinition().getIdentifier()
						.toUpperCase();
				if (StringUtils.isNotNullOrEmpty(propertyDefinition.getRnc())
						&& "INJECTED_FIELD".equals(controlId)) {
					injections.add(propertyDefinition);
				} else if ("CHECKLIST".equals(controlId)) {
					checklists.add(propertyDefinition);
				}
			}
		}
		// if properties were modified then we need to save them
		return oldSize != instance.getProperties().size();
	}
}
