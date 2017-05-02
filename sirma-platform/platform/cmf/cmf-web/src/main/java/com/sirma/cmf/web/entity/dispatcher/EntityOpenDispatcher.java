package com.sirma.cmf.web.entity.dispatcher;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.bookmark.BookmarkRequestParameters;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.notification.MessageLevel;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * EntityOpenDispatcher handles the GET links (bookmarkable capability) to entity objects in EMF.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class EntityOpenDispatcher extends Action implements BookmarkRequestParameters {

	private static final String IS_OPENED = "isOpened";

	/** Registered context initializers. */
	private Map<Class, PageContextInitializerExtension<Instance>> initializers;

	/** Instance initializer plugins. */
	@Inject
	@ExtensionPoint(value = PageContextInitializerExtension.TARGET_NAME)
	private Iterable<PageContextInitializerExtension<Instance>> extension;

	@Inject
	private InstanceContextInitializer instanceContextInitializer;

	@Inject
	private InstanceTypeResolver typeResolver;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		initializers = PluginUtil.parseSupportedObjects(extension, false);
	}

	/**
	 * Gets the instance open url that can be used to trigger explicit jsf navigation. This url can be returned by an
	 * action method as is. The url also contains <code>faces-redirect=true</code> parameter in order to force a
	 * redirection.
	 *
	 * @param instance
	 *            the instance
	 * @return the instance open url
	 */
	public String getInstanceOpenUrl(Instance instance) {
		return "/entity/open.jsf?type=" + instance.getClass().getSimpleName().toLowerCase() + "&instanceId="
				+ instance.getId() + "&faces-redirect=true";
	}

	/**
	 * Open an instance landing page using request parameters passed trough a bookmark link.
	 */
	public void open() {
		// prevent multiple execution of the code below
		Map<String, Object> viewMap = getViewMap();
		Boolean isOpened = (Boolean) viewMap.get(IS_OPENED);

		if (isOpened == null || isOpened == Boolean.FALSE) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			Map<String, String> requestMap = getRequestMap(facesContext);
			String instanceType = getParameterByName(TYPE, requestMap);
			String instanceId = getParameterByName(INSTANCE_ID, requestMap);
			String tab = getParameterByName(TAB, requestMap);
			executeOpen(instanceType, instanceId, tab, requestMap, facesContext);
			viewMap.put(IS_OPENED, Boolean.TRUE);
		}
	}

	/**
	 * Initializes given instance in context and navigates the application to instance landing page.
	 *
	 * @param instanceType
	 *            the instance type
	 * @param instanceId
	 *            the instance id
	 * @param tab
	 *            the tab
	 * @param requestMap
	 *            the request map
	 * @param facesContext
	 *            the faces context
	 */
	protected void executeOpen(String instanceType, String instanceId, String tab, Map<String, String> requestMap,
			FacesContext facesContext) {
		TimeTracker timer = TimeTracker.createAndStart();
		String navigation = NavigationConstants.NAVIGATE_HOME;
		if (StringUtils.isNotNullOrEmpty(instanceId)) {
			log.debug("Openning page for instance id[" + instanceId + "]");

			// initialize the selected tab if provided
			if (StringUtils.isNotNullOrEmpty(tab)) {
				getDocumentContext().setSelectedTab(tab);
			}

			navigation = invokeInitializer(instanceId, requestMap);

			Boolean print = Boolean.valueOf(getParameterByName("print", requestMap));
			if (Boolean.TRUE.equals(print)) {
				navigation = "idoc-print";
			}

			if (StringUtils.isNotNullOrEmpty(tab)) {
				navigation = tab;
			}
		} else {
			notificationSupport.addMessage(null,
					"Requested uri can not be opened because of wrong or missing request parameters. You are redirected to user dashboard instead.",
					MessageLevel.ERROR);
			log.warn("Can not open because of wrong or missing request parameters: type[" + instanceType + "] with id["
					+ instanceId + "]");
		}

		facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, navigation);
		log.debug("Page openning took " + timer.stopInSeconds() + " s");
	}

	/**
	 * Call page context initializer for the instance which landing page should be opened.
	 *
	 * @param id
	 *            the id
	 * @param requestMap
	 *            the request map
	 * @return the string
	 */
	public String invokeInitializer(String id, Map<String, String> requestMap) {
		TimeTracker timer = TimeTracker.createAndStart();
		String navigation = NavigationConstants.NAVIGATE_HOME;

		if (id == null || "null".equals(id)) {
			notificationSupport.addMessage(null, "Invalid object link", MessageLevel.ERROR);
			return navigation;
		}
		InstanceReference reference = typeResolver.resolveReference(id).orElse(null);
		if (reference == null) {
			notificationSupport.addMessage(null, "Requested object not found in system!: " + id, MessageLevel.ERROR);
			log.warn("Not found instance with id: " + id);
			return navigation;
		}

		Instance selectedInstance = typeConverter.convert(InitializedInstance.class, reference).getInstance();

		// if context data is passed, then we use it in context resolving
		String contextId = requestMap.get("contextId");
		String contextType = requestMap.get("contextType");
		Instance context = null;
		if (StringUtils.isNotNullOrEmpty(contextId) && StringUtils.isNotNullOrEmpty(contextType)) {
			context = fetchInstance(contextId, contextType);
		}

		if (selectedInstance != null) {
			instanceContextInitializer.restoreHierarchy(selectedInstance);

			// add context instance if any
			Instance owningInstance = ((OwnedModel) selectedInstance).getOwningInstance();
			getDocumentContext().addContextInstance(owningInstance);

			PageContextInitializerExtension<Instance> contextInitializer = initializers
					.get(selectedInstance.getClass());
			if (contextInitializer != null) {
				navigation = contextInitializer.initContextFor(selectedInstance);
			}
		} else {
			notificationSupport.addMessage(null, "Requested object not found in system!", MessageLevel.WARN);
		}
		log.debug("Context initialization took " + timer.stopInSeconds() + " s");
		return navigation;
	}

	/**
	 * Gets request parameter by name.
	 *
	 * @param name
	 *            the name
	 * @param requestParameterMap
	 *            the request parameter map
	 * @return the parameter by name
	 */
	protected String getParameterByName(String name, Map<String, String> requestParameterMap) {
		return requestParameterMap.get(name);
	}

	/**
	 * Getter for the request map is extracted for testability.
	 *
	 * @param facesContext
	 *            the faces context
	 * @return the request map
	 */
	protected Map<String, String> getRequestMap(FacesContext facesContext) {
		return facesContext.getExternalContext().getRequestParameterMap();
	}

	/**
	 * Getter for the view map is extracted for testability.
	 *
	 * @return the view map
	 */
	protected Map<String, Object> getViewMap() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewMap();
	}

}
