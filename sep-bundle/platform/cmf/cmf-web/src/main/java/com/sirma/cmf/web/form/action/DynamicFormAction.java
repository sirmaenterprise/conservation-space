package com.sirma.cmf.web.form.action;

import java.io.Serializable;
import java.util.Date;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.action.event.DynamicFormActionEvent;
import com.sirma.cmf.web.form.action.event.DynamicFormActionEventTypeBinding;
import com.sirma.cmf.web.upload.OutgoingDocumentUploadController;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * DynamicFormAction to handle action event button ui control actions.
 * 
 * @author svelikov
 */
@Named
public class DynamicFormAction extends EntityAction {

	/** The dynamic form action event. */
	@Inject
	@Any
	private Event<DynamicFormActionEvent> dynamicFormActionEvent;

	/** The outgoing document upload controller. */
	@Inject
	private OutgoingDocumentUploadController outgoingDocumentUploadController;

	@Inject
	private ServiceRegister serviceRegister;

	/**
	 * Fire event with given id.
	 * 
	 * @param target
	 *            the target that has the button attached to
	 * @param buttonName
	 *            the button property name name
	 * @param eventId
	 *            the event id to fire
	 * @param executeOnce
	 *            if the event need to be executed only once
	 * @return navigation string
	 */
	public String fireEvent(Instance target, String buttonName, String eventId, Boolean executeOnce) {

		log.debug("CMFWeb: Executing DynamicFormAction.fireEvent [" + eventId + "]");

		Serializable serializable = target.getProperties().get(buttonName);
		Instance instance = null;
		if (serializable instanceof Instance) {
			instance = (Instance) serializable;
			// if already executed and called again we are not going to fire again
			if (Boolean.TRUE.equals(executeOnce)
					&& Boolean.TRUE.equals(instance.getProperties().get(
							DefaultProperties.ACTION_BUTTON_EXECUTED))) {
				return NavigationConstants.RELOAD_PAGE;
			}
		}

		DynamicFormActionEvent event = new DynamicFormActionEvent(NavigationConstants.RELOAD_PAGE,
				target, instance);
		DynamicFormActionEventTypeBinding eventTypeBinding = new DynamicFormActionEventTypeBinding(
				eventId);
		dynamicFormActionEvent.select(eventTypeBinding).fire(event);

		// fire open event to allow injections to occur if any
		InstanceEventProvider<Instance> eventProvider = serviceRegister.getEventProvider(target);
		if (eventProvider != null) {
			InstanceOpenEvent<Instance> openEvent = eventProvider.createOpenEvent(target);
			log.debug("CMFWeb: DynamicFormAction is firing an event[" + openEvent + "]");
			eventService.fire(openEvent);
		}

		if (event.isHandled() && (instance != null)) {
			log.debug("CMFWeb: DynamicFormAction is processing a handled DynamicFormActionEvent event["
					+ eventId + "]");

			// if the have specified that we want to execute the action more then once
			if (Boolean.TRUE.equals(executeOnce)) {
				instance.getProperties()
						.put(DefaultProperties.ACTION_BUTTON_EXECUTED, Boolean.TRUE);
			}

			// set a info message for the user if any provided
			notificationSupport.addMessage(event.getNotificationMessage());
			// set flag that will allow links to be re-evaluated
			outgoingDocumentUploadController.setProcessedLinks(false);
		}

		if (instance != null) {
			instance.getProperties().put(DefaultProperties.ACTION_BUTTON_EXECUTED_ON, new Date());
			instance.getProperties().put(DefaultProperties.ACTION_BUTTON_EXECUTED_FROM,
					authenticationService.getCurrentUserId());
		}

		return event.getNavigation();
	}

}
