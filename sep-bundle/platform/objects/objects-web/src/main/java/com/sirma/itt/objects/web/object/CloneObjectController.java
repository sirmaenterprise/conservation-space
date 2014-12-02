package com.sirma.itt.objects.web.object;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializerExtension;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.web.constants.ObjectNavigationConstants;

/**
 * Controller class for clone object functionality.
 */
@Named
@ViewScoped
public class CloneObjectController extends Action implements Serializable {

	private static final long serialVersionUID = 2519281155965792942L;

	private static final String DOCUMENT_INSTANCE_TYPE = "documentinstance";

	private static final String PARAM_NAME_INSTANCE_TYPE = "type";
	private static final String PARAM_NAME_INSTANCE_ID = "id";
	private static final String PARAM_NAME_SECTION_ID = "sectionId";

	/** The faces context. */
	@Inject
	private FacesContext facesContext;

	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

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
	 * Initializes document context and sends an iDoc or object to iDoc for cloning.
	 * 
	 * @return navigation string
	 */
	public String cloneObject() {

		String objectId = getParameterByName(PARAM_NAME_INSTANCE_ID);
		String sectionId = getParameterByName(PARAM_NAME_SECTION_ID);
		String type = getParameterByName(PARAM_NAME_INSTANCE_TYPE);

		Instance instance = null;

		if (objectId != null) {
			// If the item to clone is document add the it to context else add object to the
			// context.
			if (type.equals(DOCUMENT_INSTANCE_TYPE)) {
				instance = loadInstanceInternal(DocumentInstance.class, objectId);
				getDocumentContext().addInstance(instance);
				getDocumentContext().setCurrentOperation(DocumentInstance.class.getSimpleName(),
						ActionTypeConstants.CLONE);
				DefinitionModel instanceDefinition = dictionaryService
						.getInstanceDefinition(instance);
				getDocumentContext().addDefinition(DefinitionModel.class, instanceDefinition);

			} else {
				getDocumentContext().clear();
				instance = loadInstanceInternal(ObjectInstance.class, objectId);
				getDocumentContext().addInstance(instance);
				getDocumentContext().setCurrentOperation(ObjectInstance.class.getSimpleName(),
						ActionTypeConstants.CLONE);
				ObjectDefinition definition = dictionaryService.getDefinition(getInstanceClass(),
						instance.getIdentifier());
				getDocumentContext().addDefinition(getInstanceClass(), definition);
			}

			if (sectionId != null) {
				SectionInstance sectionInstance = loadInstanceInternal(SectionInstance.class,
						sectionId);
				((EmfInstance) instance).setOwningInstance(sectionInstance);
				PageContextInitializerExtension<Instance> initializer = initializers
						.get(sectionInstance.getClass());
				if (initializer != null) {
					initializer.initContextFor(sectionInstance);
				}
			}
		}

		// navigate to object or idoc depending on the clone type
		if (!type.equals(DOCUMENT_INSTANCE_TYPE)) {
			facesContext.getApplication().getNavigationHandler()
					.handleNavigation(facesContext, null, ObjectNavigationConstants.OBJECT);
			return ObjectNavigationConstants.OBJECT;
		}

		caseDocumentsTableAction.open((DocumentInstance) instance);
		facesContext
				.getApplication()
				.getNavigationHandler()
				.handleNavigation(facesContext, null, NavigationConstants.NAVIGATE_DOCUMENT_DETAILS);
		return NavigationConstants.NAVIGATE_DOCUMENT_DETAILS;
	}

	/**
	 * Gets the instance class.
	 * 
	 * @return the instance class
	 */
	private Class<ObjectDefinition> getInstanceClass() {
		return ObjectDefinition.class;
	}

	/**
	 * Gets request parameter by name.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @return parameter value
	 */
	protected String getParameterByName(String name) {
		Map<String, String> requestParameterMap = facesContext.getExternalContext()
				.getRequestParameterMap();
		return requestParameterMap.get(name);
	}

	/**
	 * Loads an instance by dbId.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type of the instance
	 * @param id
	 *            the dbId
	 * @return the instance
	 */
	private <T extends Instance> T loadInstanceInternal(Class<T> type, Serializable id) {
		InstanceService<Instance, DefinitionModel> service = serviceRegister
				.getInstanceService(type);
		try {
			return type.cast(service.loadByDbId(id));
		} catch (ClassCastException e) {
			log.error("", e);
			return null;
		}
	}

}
