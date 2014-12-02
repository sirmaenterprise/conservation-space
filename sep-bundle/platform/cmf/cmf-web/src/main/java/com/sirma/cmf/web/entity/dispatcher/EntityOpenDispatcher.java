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
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.notification.MessageLevel;

/**
 * EntityOpenDispatcher handles the GET links (bookmarkable capability) to entity objects in EMF.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class EntityOpenDispatcher extends Action implements BookmarkRequestParameters {

	/** The Constant IS_OPENED. */
	private static final String IS_OPENED = "isOpened";

	/** The faces context. */
	@Inject
	private FacesContext facesContext;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** Registered context initializers. */
	private Map<Class<?>, PageContextInitializerExtension<Instance>> initializers;

	/** Instance initializer plugins. */
	@Inject
	@ExtensionPoint(value = PageContextInitializerExtension.TARGET_NAME)
	private Iterable<PageContextInitializerExtension<Instance>> extension;

	@Inject
	private InstanceContextInitializerImpl instanceContextInitializer;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		initializers = PluginUtil.parseSupportedObjects(extension, false);
	}

	/**
	 * Open internal is used to programmatically initialize and open given instance.
	 * 
	 * @param instanceType
	 *            the instance type
	 * @param instanceId
	 *            the instance id
	 * @param tab
	 *            the tab
	 */
	public void openInternal(String instanceType, String instanceId, String tab) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			log.warn("CMFWeb: EntityOpenDispatcher.openInternal is missing required arguments: instanceId ["
					+ instanceId + "] or instanceType [" + instanceType + "]");
			return;
		}
		executeOpen(instanceType, instanceId, tab, getRequestMap());
	}

	/**
	 * Open internal is used to programmatically initialize and open given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param tab
	 *            the tab
	 */
	public void openInternal(Instance instance, String tab) {
		openInternal(instance.getClass().getSimpleName().toLowerCase(), (String) instance.getId(),
				tab);
	}

	/**
	 * Open an instance landing page using request parameters passed trough a bookmark link.
	 */
	public void open() {
		// prevent multiple execution of the code below
		Map<String, Object> viewMap = getViewMap();
		Boolean isOpened = (Boolean) viewMap.get(IS_OPENED);

		if ((isOpened == null) || (isOpened == Boolean.FALSE)) {
			Map<String, String> requestMap = getRequestMap();
			String instanceType = getParameterByName(TYPE, requestMap);
			String instanceId = getParameterByName(INSTANCE_ID, requestMap);
			String tab = getParameterByName(TAB, requestMap);
			executeOpen(instanceType, instanceId, tab, requestMap);
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
	 */
	protected void executeOpen(String instanceType, String instanceId, String tab,
			Map<String, String> requestMap) {
		TimeTracker timer = TimeTracker.createAndStart();
		String navigation = NavigationConstants.NAVIGATE_HOME;
		if (StringUtils.isNotNullOrEmpty(instanceType) && StringUtils.isNotNullOrEmpty(instanceId)) {
			log.debug("Openning page for instance type[" + instanceType + "] with id[" + instanceId
					+ "]");

			// initialize the selected tab if provided
			if (StringUtils.isNotNullOrEmpty(tab)) {
				getDocumentContext().setSelectedTab(tab);
			}

			navigation = invokeInitializer(instanceType, instanceId, requestMap);

			if (StringUtils.isNotNullOrEmpty(tab)) {
				navigation = tab;
			}
		} else {
			notificationSupport
					.addMessage(
							null,
							"Requested uri can not be opened because of wrong or missing request parameters. You are redirected to user dashboard instead.",
							MessageLevel.ERROR);
			log.debug("Can not open because of wrong or missing request parameters: type["
					+ instanceType + "] with id[" + instanceId + "]");
		}

		facesContext.getApplication().getNavigationHandler()
				.handleNavigation(facesContext, null, navigation);
		log.debug("Page openning took " + timer.stopInSeconds() + " s");
	}

	/**
	 * Call page context initializer for the instance which landing page should be opened.
	 * 
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @param requestMap
	 *            the request map
	 * @return the string
	 */
	public String invokeInitializer(String type, String id, Map<String, String> requestMap) {
		TimeTracker timer = TimeTracker.createAndStart();
		String navigation = NavigationConstants.NAVIGATE_HOME;

		if (((type == null) || "null".equals(type)) || ((id == null) || "null".equals(id))) {
			notificationSupport.addMessage(null, "Invalid object link", MessageLevel.ERROR);
			return navigation;
		}
		InstanceReference reference = null;
		try {
			reference = typeConverter.convert(InstanceReference.class, type);
		} catch (TypeConversionException e) {
			notificationSupport.addMessage(null, "Requested object type is not supported!",
					MessageLevel.ERROR);
			log.warn("Requested object type is not supported!", e);
			return navigation;
		}
		reference.setIdentifier(id);
		Instance selectedInstance = typeConverter.convert(InitializedInstance.class, reference)
				.getInstance();

		if (selectedInstance != null) {
			instanceContextInitializer.restoreHierarchy(selectedInstance, null);

			// add context instance if any
			Instance owningInstance = ((OwnedModel) selectedInstance).getOwningInstance();
			getDocumentContext().addContextInstance(owningInstance);

			PageContextInitializerExtension<Instance> instanceContextInitializer = initializers
					.get(selectedInstance.getClass());
			if (instanceContextInitializer != null) {
				navigation = instanceContextInitializer.initContextFor(selectedInstance);
			}
		} else {
			notificationSupport.addMessage(null, "Requested object not found in system!",
					MessageLevel.WARN);
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
	 * @return the request map
	 */
	protected Map<String, String> getRequestMap() {
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
