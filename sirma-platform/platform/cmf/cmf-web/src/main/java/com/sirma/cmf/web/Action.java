package com.sirma.cmf.web;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.cmf.web.menu.NavigationMenuAction;
import com.sirma.itt.seip.convert.SerializableConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.NullInstance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.notification.NotificationSupport;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Common action class.
 *
 * @author svelikov
 */
public class Action {

	/**
	 * Logger instance.
	 */
	@Inject
	protected transient Logger log;

	/** The notification support. */
	@Inject
	protected NotificationSupport notificationSupport;

	/** The document context. {@link DocumentContext} instance. */
	@Inject
	private DocumentContext documentContext;

	/** The navigation menu action. */
	@Inject
	protected NavigationMenuAction navigationMenuAction;

	@Inject
	@SerializableConverter
	protected TypeConverter typeConverter;

	@Inject
	protected EventService eventService;

	@Inject
	protected DatabaseIdManager idManager;

	@Inject
	protected SecurityContext securityContext;

	/**
	 * Sets the current operation.
	 *
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	protected void setCurrentOperation(Instance instance, String operationId) {
		if (instance != null) {
			getDocumentContext().setCurrentOperation(instance.getClass().getSimpleName(), operationId);
		} else {
			getDocumentContext().setCurrentOperation(NullInstance.class.getSimpleName(), operationId);
		}
	}

	/**
	 * Fetch instance.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            The instance type. This should be the simple class name to lower case!
	 * @return the instance
	 */
	public Instance fetchInstance(Serializable instanceId, String instanceType) {
		Instance instance;
		InstanceReference reference = typeConverter.convert(InstanceReference.class, instanceType);
		reference.setIdentifier(instanceId.toString());
		instance = reference.toInstance();
		return instance;
	}

	/**
	 * Fetch instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the instance
	 */
	public Instance fetchInstance(Instance instance) {
		if (instance != null) {
			return fetchInstance(instance.getId(), instance.getClass().getSimpleName().toLowerCase());
		}
		return null;
	}

	/**
	 * Checks if is current user.
	 *
	 * @param userId
	 *            the user id
	 * @return true, if is current user
	 */
	public boolean isCurrentUser(String userId) {
		return securityContext.isCurrentUser(userId);
	}

	/**
	 * Checks if current request is a ajax/get request.
	 *
	 * @return true if current request is ajax.
	 */
	public boolean isAjaxRequest() {
		return FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest();
	}

	/**
	 * Checks if is postback.
	 *
	 * @return true, if is postback
	 */
	public boolean isPostback() {
		return FacesContext.getCurrentInstance().isPostback();
	}

	/**
	 * Getter method for documentContext.
	 *
	 * @return the documentContext
	 */
	public DocumentContext getDocumentContext() {
		return documentContext;
	}

	/**
	 * Setter method for documentContext.
	 *
	 * @param documentContext
	 *            the documentContext to set
	 */
	public void setDocumentContext(DocumentContext documentContext) {
		this.documentContext = documentContext;
	}

	/**
	 * Initialize context instance if any.
	 *
	 * @param instance
	 *            the instance
	 */
	protected void initializeContextInstance(Instance instance) {
		if (instance == null) {
			log.error("Can not initialize context instance for null instance");
			return;
		}
		Instance contextInstance = InstanceUtil.getDirectParent(instance);
		if (contextInstance == null) {
			getDocumentContext().addContextInstance(instance);
		} else {
			getDocumentContext().addContextInstance(contextInstance);
			getDocumentContext().addInstance(contextInstance);
		}
	}

}
