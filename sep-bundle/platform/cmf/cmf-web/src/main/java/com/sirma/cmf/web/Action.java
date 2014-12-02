package com.sirma.cmf.web;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.cmf.web.menu.NavigationMenuAction;
import com.sirma.itt.emf.converter.SerializableConverter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.web.notification.NotificationSupport;

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

	/** The authentication service. */
	@Inject
	protected AuthenticationService authenticationService;

	/** The document context. {@link DocumentContext} instance. */
	@Inject
	private DocumentContext documentContext;

	/** The navigation menu action. */
	@Inject
	protected NavigationMenuAction navigationMenuAction;

	@Inject
	@SerializableConverter
	protected TypeConverter typeConverter;

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
	 * Checks if is current user.
	 * 
	 * @param userId
	 *            the user id
	 * @return true, if is current user
	 */
	public boolean isCurrentUser(String userId) {
		return authenticationService.isCurrentUser(userId);
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
	 * Initialize root context if any.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void initializeRoot(Instance instance) {
		// next call returns null if the root instance is ProjectInstance
		Instance rootInstance = InstanceUtil.getRootInstance(instance, true);
		if (rootInstance instanceof RootInstanceContext) {
			getDocumentContext().addInstance(rootInstance);
			getDocumentContext().setRootInstance(rootInstance);
		}
		// if PM is not deployed we will not have projects and InstanceUtil.getRootInstance should
		// return top most instance which will be a case instance probably
		if (rootInstance == null) {
			getDocumentContext().setRootInstance(instance);
		} else {
			getDocumentContext().setRootInstance(rootInstance);
		}
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
		Instance contextInstance = InstanceUtil.getContext(instance, true);
		if (contextInstance == null) {
			getDocumentContext().addContextInstance(instance);
		} else {
			getDocumentContext().addContextInstance(contextInstance);
			getDocumentContext().addInstance(contextInstance);
		}
	}

}
