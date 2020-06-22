package com.sirma.itt.seip.instance.observer;

import static com.sirma.itt.seip.configuration.Options.DISABLE_AUTOMATIC_ID_GENERATION;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.event.HandledEvent;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.event.BeforeInstancePersistEvent;
import com.sirma.itt.seip.instance.event.IdentifierGeneratedEvent;
import com.sirma.itt.seip.instance.event.InstanceCreateEvent;

/**
 * Observer that listens for a {@link BeforeInstancePersistEvent} event and tries to generate ID for property with name
 * {@link DefaultProperties#UNIQUE_IDENTIFIER} if the field is found and expression is defined.
 *
 * @author BBonev
 */
@ApplicationScoped
public class IdGeneratorObserver {

	private static final Pattern DIGITS_ONLY = Pattern.compile("\\D+");

	private static final String NO_ID = "NO_ID";

	@Inject
	private DefinitionService definitionService;

	@Inject
	private EventService eventService;

	@Inject
	private ExpressionsManager manager;

	private static final Logger LOGGER = LoggerFactory.getLogger(IdGeneratorObserver.class);

	/**
	 * Initialize the value for the unique identifier.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceCreate(@Observes InstanceCreateEvent<? extends Instance> event) {
		if (DISABLE_AUTOMATIC_ID_GENERATION.isEnabled()) {
			return;
		}
		event.getInstance().getProperties().put(DefaultProperties.UNIQUE_IDENTIFIER, NO_ID);
	}

	/**
	 * The method listens for persist event for all instance types in order to try to generate identifier if any.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceCreate(@Observes BeforeInstancePersistEvent<?, ?> event) {
		if (DISABLE_AUTOMATIC_ID_GENERATION.isEnabled()) {
			return;
		}
		Instance instance = event.getInstance();
		if (instance.getProperties().containsKey(DefaultProperties.UNIQUE_IDENTIFIER)) {
			Serializable oldKey = instance.getProperties().get(DefaultProperties.UNIQUE_IDENTIFIER);
			if (NO_ID.equals(oldKey)) {
				instance.getProperties().remove(DefaultProperties.UNIQUE_IDENTIFIER);
			} else if (oldKey != null && !oldKey.toString().isEmpty() && oldKey.toString().startsWith("$")) {
				// if id has been generated then we should not generate it again if event is fired
				// again due to problem
				LOGGER.warn("Id for instance {} has been generated before - ignoring the call. " + "The old key is: {}",
						instance.getClass().getSimpleName(), oldKey);
				return;
			}
		}

		PropertyDefinition node = definitionService.getProperty(DefaultProperties.UNIQUE_IDENTIFIER, instance);
		if (node == null) {
			LOGGER.trace("Definition for property {} was not found in model {}", DefaultProperties.UNIQUE_IDENTIFIER,
					PathHelper.getPath(instance));
			return;
		}
		generateId(event, instance, node);
		// REVIEW: we could probably search for all properties that have a ${seq(.. expression not
		// only the uniqueIdentifier field or by specific control
	}

	/**
	 * Generate id for the given instance and the expression defined in the given property definition
	 *
	 * @param event
	 *            the event
	 * @param instance
	 *            the instance
	 * @param node
	 *            the node
	 */
	private void generateId(BeforeInstancePersistEvent<?, ?> event, Instance instance, PropertyDefinition node) {
		try {
			if (StringUtils.isNotBlank(node.getRnc()) && node.getRnc().contains("rootContext")) {
				Instance rootInstance = InstanceUtil.getRootInstance(instance);
				if (rootInstance != null) {
					Serializable identifier = instance.getProperties().get(DefaultProperties.UNIQUE_IDENTIFIER);
					process(event, instance, identifier);
					// we need root context but the object does not have it
					return;
				}
			}
			// try to evaluate the expression if any
			ExpressionContext context = manager.createDefaultContext(instance, node, null);
			Serializable value = manager.evaluateRule(node, context, instance);

			setEvaluatedValueIfValid(event, instance, value);

		} catch (EmfRuntimeException e) {
			String string = "Failed to generate identifier for {} due to: {}";
			LOGGER.warn(string, instance.getClass().getSimpleName(), e.getMessage());
			LOGGER.trace(string, instance.getClass().getSimpleName(), e.getMessage(), e);
		}
	}

	private void process(BeforeInstancePersistEvent<?, ?> event, Instance instance, Serializable identifier) {
		if (instance instanceof CMInstance && identifier == null) {
			String contentManagementId = ((CMInstance) instance).getContentManagementId();
			// for activity tasks that are not part of a project we will remove other
			// characters except the numbers
			if (StringUtils.isNotBlank(contentManagementId) && contentManagementId.contains("$")) {
				contentManagementId = DIGITS_ONLY.matcher(contentManagementId).replaceAll("");
			}
			instance.getProperties().put(DefaultProperties.UNIQUE_IDENTIFIER, contentManagementId);
			updateAsHandled(event);
		}
	}

	/**
	 * Fire event to allow other expressions that depends on identifier to catch up and evaluate itself.
	 * 
	 * @param instance
	 *            Current instance.
	 */
	private void notifyIdentifierGenerated(Instance instance) {
		IdentifierGeneratedEvent identifierGeneratedEvent = new IdentifierGeneratedEvent(instance);
		eventService.fire(identifierGeneratedEvent);
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
	private void setEvaluatedValueIfValid(BeforeInstancePersistEvent<?, ?> event, Instance instance,
			Serializable value) {
		// check if not evaluated or noting to evaluate
		if (value != null && !value.toString().startsWith("$") && !value.toString().endsWith("}")) {
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
		notifyIdentifierGenerated(event.getInstance());
	}
}