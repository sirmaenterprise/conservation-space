package com.sirma.itt.emf.instance.observer;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.CMInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Observer that listens for a {@link BeforeInstancePersistEvent} event and tries to generate ID for
 * property with name {@link DefaultProperties#UNIQUE_IDENTIFIER} if the field is found and
 * expression is defined.
 *
 * @author BBonev
 */
@ApplicationScoped
public class IdGeneratorObserver {

	private static final Pattern DIGITS_ONLY = Pattern.compile("\\D+");
	/** The Constant NO_ID. */
	private static final String NO_ID = "NO_ID";

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The manager. */
	@Inject
	private ExpressionsManager manager;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdGeneratorObserver.class);

	/**
	 * Initialize the value for the unique identifier.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceCreate(@Observes InstanceCreateEvent<Instance> event) {
		event.getInstance().getProperties().put(DefaultProperties.UNIQUE_IDENTIFIER, NO_ID);
	}

	/**
	 * The method listens for persist event for all instance types in order to try to generate
	 * identifier if any.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceCreate(@Observes BeforeInstancePersistEvent<?, ?> event) {
		Instance instance = event.getInstance();
		if (instance.getProperties().containsKey(DefaultProperties.UNIQUE_IDENTIFIER)) {
			Serializable oldKey = instance.getProperties().get(DefaultProperties.UNIQUE_IDENTIFIER);
			if (NO_ID.equals(oldKey)) {
				instance.getProperties().remove(DefaultProperties.UNIQUE_IDENTIFIER);
			} else if ((oldKey != null) && !oldKey.toString().isEmpty()
					&& oldKey.toString().startsWith("$")) {
				// if id has been generated then we should not generate it again if event is fired
				// again due to problem
				LOGGER.warn("Id for instance " + instance.getClass().getSimpleName()
						+ " has been generated before - ignoring the call. The old key is: "
						+ oldKey);
				return;
			}
		}

		PropertyDefinition node = dictionaryService.getProperty(
				DefaultProperties.UNIQUE_IDENTIFIER, instance.getRevision(), instance);
		if (node == null) {
			LOGGER.trace("Definition for property {} was not found in model {}:{}",
					DefaultProperties.UNIQUE_IDENTIFIER, PathHelper.getPath(instance),
					instance.getRevision());
			return;
		}
		generateId(event, instance, node);
		// REVIEW: we could probably search for all properties that have a ${seq(.. expression not
		// only the uniqueIdentifier field or by specific control
	}

	/**
	 * Generate id for the given instance and the expression defined in the given property
	 * definition
	 * 
	 * @param event
	 *            the event
	 * @param instance
	 *            the instance
	 * @param node
	 *            the node
	 */
	private void generateId(BeforeInstancePersistEvent<?, ?> event, Instance instance,
			PropertyDefinition node) {
		try {
			if (StringUtils.isNotNullOrEmpty(node.getRnc())
					&& node.getRnc().contains("rootContext")) {
				Instance rootInstance = InstanceUtil.getRootInstance(instance, true);
				if (!(rootInstance instanceof RootInstanceContext)) {
					LOGGER.trace(
							"Detected required root context to generate identifier but {} implementation is not present in the current Instance tree model for {} and definition {}:{}",
							RootInstanceContext.class, instance.getClass().getSimpleName(),
							instance.getPath(), instance.getRevision());

					Serializable serializable = instance.getProperties().get(
							DefaultProperties.UNIQUE_IDENTIFIER);
					if ((instance instanceof CMInstance) && (serializable == null)) {
						String contentManagementId = ((CMInstance) instance).getContentManagementId();
						// for activiti tasks that are not part of a project we will remove other
						// characters except the numbers
						if (StringUtils.isNotNullOrEmpty(contentManagementId) && contentManagementId.contains("$")) {
							contentManagementId = DIGITS_ONLY.matcher(contentManagementId).replaceAll("");
						}
						instance.getProperties().put(DefaultProperties.UNIQUE_IDENTIFIER,
								contentManagementId);
						updateAsHandled(event);
					}
					// we need root context but the object does not have it
					return;
				}
			}
			// try to evaluate the expression if any
			ExpressionContext context = manager.createDefaultContext(instance, node, null);
			Serializable value = manager.evaluateRule(node, context, instance);

			setEvaluatedValueIfValid(event, instance, value);
		} catch (EmfRuntimeException e) {
			LOGGER.warn("Failed to generate identifier for {} due to: {}", instance.getClass()
					.getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Set the evaluated value if id was generated successfully
	 * 
	 * @param event
	 *            the event
	 * @param instance
	 *            the instance
	 * @param value
	 *            the value
	 */
	private void setEvaluatedValueIfValid(BeforeInstancePersistEvent<?, ?> event,
			Instance instance,
			Serializable value) {
		// check if not evaluated or noting to evaluate
		if ((value != null) && !value.toString().startsWith("$")
				&& !value.toString().endsWith("}")) {
			instance.getProperties().put(DefaultProperties.UNIQUE_IDENTIFIER, value);
			// most of the time the that identifier is the same as this one so we update is
			// by default
			if (instance instanceof CMInstance) {
				((CMInstance) instance).setContentManagementId(value.toString());
			}
			updateAsHandled(event);
		}
	}

	/**
	 * Marks the event as handled
	 * 
	 * @param event
	 *            the event
	 */
	private void updateAsHandled(BeforeInstancePersistEvent<?, ?> event) {
		if (event instanceof HandledEvent) {
			((HandledEvent) event).setHandled(true);
		}
	}

}
