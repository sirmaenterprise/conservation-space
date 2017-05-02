package com.sirma.itt.cmf.services.observers;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.event.HandledEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.itt.seip.instance.event.InstanceOpenEvent;
import com.sirma.itt.seip.instance.util.PropertiesEvaluationHelper;

/**
 * Handler class for create and open operation. The handler processes the inject operations from case and documents to
 * tasks
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefaultInjectionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInjectionHandler.class);

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ExpressionsManager evaluatorManager;

	@Inject
	private DatabaseIdManager idManager;

	/**
	 * Listens for task open event.
	 *
	 * @param event
	 *            the task event
	 */
	public void onOpened(@Observes InstanceOpenEvent<?> event) {
		Instance instance = event.getInstance();
		performInjection(event.getInstance(), event, instance);
	}

	/**
	 * On instance changed.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceChanged(@Observes InstanceChangeEvent<?> event) {
		Instance instance = event.getInstance();
		performInjection(instance, event, instance);
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
		if (args == null || args.length == 0) {
			if (instance instanceof OwnedModel) {
				InstanceReference reference = ((OwnedModel) instance).getOwningReference();
				if (reference == null || StringUtils.isNullOrEmpty(reference.getIdentifier())) {
					// if we does not have a reference then we cannot inject something from
					// nothing....
					return;
				}
				List<Instance> parentPath = InstanceUtil.getParentPath(instance);
				// load properties of the instance
				args = parentPath.toArray(new Instance[parentPath.size()]);
			} else {
				LOGGER.warn("Current instance is not of type " + OwnedModel.class);
				return;
			}
		}

		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);

		if (definition == null) {
			LOGGER.debug("{} definition {} not found. No injection will be done.", instance.getClass().getSimpleName(),
					instance.getIdentifier());
			return;
		}

		// collect all fields that are marked for injection
		Set<PropertyDefinition> injections = new LinkedHashSet<>();
		Set<PropertyDefinition> checklists = new LinkedHashSet<>();
		boolean modified;
		if (definition instanceof RegionDefinitionModel) {
			modified = processFields(instance, (RegionDefinitionModel) definition, injections, checklists);
		} else {
			modified = processFields(instance, definition, injections, checklists);
		}
		if (injections.isEmpty() && checklists.isEmpty()) {
			LOGGER.trace("No field definitions found that need injection or automatic checks.");
			return;
		}
		LOGGER.trace("Found {} fields for injection and {} checklists", injections.size(), checklists.size());

		ExpressionContext context = evaluatorManager.createDefaultContext(instance, null, null);
		Map<String, Serializable> extractedFields = evaluatorManager.evaluateRules(injections, context, true,
				(Serializable[]) args);
		instance.getProperties().putAll(extractedFields);
		LOGGER.trace("Processed fields for injection: {}", extractedFields);

		Map<String, Serializable> map = PropertiesEvaluationHelper.evaluateDefaultValues(checklists, evaluatorManager,
				context, idManager);
		// merge properties with override
		if (PropertiesUtil.mergeProperties(map, instance.getProperties(), true) && taskEvent instanceof HandledEvent) {
			((HandledEvent) taskEvent).setHandled(true);
		}

		// if we have any data extracted then we should mark the task instance for saving
		if (taskEvent instanceof HandledEvent && (!extractedFields.isEmpty() || modified)) {
			((HandledEvent) taskEvent).setHandled(true);
		}

		if (invokeSubExpressions(instance, args) && taskEvent instanceof HandledEvent) {
			((HandledEvent) taskEvent).setHandled(true);
		}
	}

	private boolean invokeSubExpressions(Instance instance, Instance... contexts) {
		Map<String, Serializable> processed = new LinkedHashMap<>();
		ExpressionContext context = evaluatorManager.createDefaultContext(instance, null, null);
		instance.getProperties().forEach((key, value) -> {
			if (!(value instanceof String) || !evaluatorManager.isExpression((String) value)) {
				return;
			}
			Serializable serializable = evaluatorManager.evaluateRule(value.toString(), Serializable.class, context,
					(Serializable[]) contexts);
			CollectionUtils.addNonNullValue(processed, key, serializable);
		});
		return PropertiesUtil.mergeProperties(processed, instance.getProperties(), true);
	}

	/**
	 * Iterate all fields and collect the one with expression control. Also populates the default properties from the
	 * definition if any.
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
	 * Iterate all fields and collect the one with expression control. Also populates the default properties from the
	 * definition if any.
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
	private boolean processFields(Instance instance, DefinitionModel model, Set<PropertyDefinition> injections,
			Set<PropertyDefinition> checklists) {
		// fill default properties
		int oldSize = instance.getProperties().size();
		// probably this is not needed anymore due to the fact that is executed on task creation but
		// it's not a problem also just minor performance issue
		PropertiesEvaluationHelper.populateProperties(instance, model.getFields(), evaluatorManager, false, idManager);

		for (PropertyDefinition propertyDefinition : model.getFields()) {
			if (propertyDefinition.getControlDefinition() != null
					&& propertyDefinition.getControlDefinition().getIdentifier() != null) {
				String controlId = propertyDefinition.getControlDefinition().getIdentifier().toUpperCase();
				if (StringUtils.isNotNullOrEmpty(propertyDefinition.getRnc()) && "INJECTED_FIELD".equals(controlId)) {
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
