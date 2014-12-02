package com.sirma.itt.sch.web.action;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializerExtension;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.emf.web.notification.MessageLevel;

/**
 * Resource allocation actions class. Handle navigation and parameters passing between various
 * functionalities for resource allocation view.
 */
@Named
@ViewAccessScoped
public class ResourceAllocationAction extends Action implements Serializable {

	private static final long serialVersionUID = -3419054446829260382L;

	/** Used to store selected users from the picklist. */
	private String selectedUsers;

	@Inject
	private FacesContext facesContext;

	/** Registered context initializers. */
	private Map<Class<?>, PageContextInitializerExtension<Instance>> initializers;

	/** Instance initializer plugins. */
	@Inject
	@ExtensionPoint(value = PageContextInitializerExtension.TARGET_NAME)
	private Iterable<PageContextInitializerExtension<Instance>> extension;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		initializers = PluginUtil.parseSupportedObjects(extension, false);
	}

	/**
	 * Opens resource allocation view with selected users from the picklist.
	 */
	public void viewForSelectedUsers() {
		getDocumentContext().clear();
		openResourceAllocation(selectedUsers, false);
	}

	/**
	 * Opens resource allocation view for given user in the context of a given project.
	 */
	public void viewForUser() {
		Map<String, String> requestParameterMap = facesContext.getExternalContext()
				.getRequestParameterMap();
		String projectId = requestParameterMap.get("projectId");
		String usernames = requestParameterMap.get("usernames");
		// Init context
		invokeInitializer("projectinstance", projectId);
		openResourceAllocation(usernames, true);
	}

	/**
	 * Opens resource allocation view by URL. Pass users to be displayed as parameter.
	 * Project id is taken from the context if it exists.
	 * 
	 * @param usernames
	 *            comma separated string with users to be displayed in the resource allocation view.
	 *            Can be null.
	 * @param viewOtherProjects
	 *            should display tasks from other projects
	 */
	private void openResourceAllocation(String usernames, Boolean viewOtherProjects) {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String url = "";
		try {
			url = externalContext.getRequestContextPath()
					+ "/project/project-resource-allocation.jsf";
			if (!StringUtils.isNullOrEmpty(usernames)) {
				url += "?usernames=" + URLEncoder.encode(usernames, "UTF-8") + "&";
			} else {
				url += "?";
			}

			url += "viewOtherProjects=" + Boolean.valueOf(viewOtherProjects);

		} catch (UnsupportedEncodingException e) {
			log.error("Error encoding usernames.", e);
		}
		try {
			externalContext.redirect(url);
		} catch (IOException e) {
			log.error("Error redirecting to URL: " + url, e);
		}
	}

	/**
	 * Invoke initializer.
	 * 
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @return the string
	 */
	public String invokeInitializer(String type, String id) {
		String navigation = NavigationConstants.NAVIGATE_HOME;

		if (((type == null) || "null".equals(type)) || ((id == null) || "null".equals(id))) {
			notificationSupport.addMessage(null, "Invalid object link", MessageLevel.ERROR);
			return navigation;
		}
		InstanceReference reference = null;
		try {
			reference = typeConverter.convert(InstanceReference.class, type);
		} catch (TypeConversionException e) {
			String message = "Requested object type is not supported!";
			log.warn(message);
			notificationSupport.addMessage(null, message, MessageLevel.ERROR);
			return navigation;
		}
		reference.setIdentifier(id);
		Instance instance = typeConverter.convert(InitializedInstance.class, reference)
				.getInstance();
		if (instance != null) {
			PageContextInitializerExtension<Instance> initializer = initializers.get(instance
					.getClass());
			if (initializer != null) {
				navigation = initializer.initContextFor(instance);
			}
		} else {
			notificationSupport.addMessage(null, "Requested object not found in system!",
					MessageLevel.WARN);
		}

		return navigation;
	}

	/**
	 * Gets the selected users.
	 * 
	 * @return the selected users
	 */
	public String getSelectedUsers() {
		return selectedUsers;
	}

	/**
	 * Sets the selected users.
	 * 
	 * @param selectedUsers
	 *            the new selected users
	 */
	public void setSelectedUsers(String selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

}
