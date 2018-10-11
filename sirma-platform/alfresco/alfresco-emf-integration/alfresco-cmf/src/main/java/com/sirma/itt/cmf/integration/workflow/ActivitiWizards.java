package com.sirma.itt.cmf.integration.workflow;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowReportService;
import org.alfresco.repo.workflow.activiti.ActivitiTypeConverter;
import org.alfresco.repo.workflow.activiti.ActivitiUtil;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowEngine;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowManager;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Repository;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * Base class to handle some common fields and methods for ActivitiWizards.
 */
public class ActivitiWizards {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ActivitiWizards.class);

	/** The Constant DEBUG_ENABLED. */
	protected static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();

	/** is initialized?. */
	private static Boolean initialized;

	/** The workflow report service. */
	private static WorkflowReportService workflowReportService;

	/** The activiti util. */
	private static ActivitiUtil activitiUtil;

	/** The type converter. */
	private static ActivitiTypeConverter typeConverter;

	/** The property converter. */
	private static ActivitiPropertyConverter propertyConverter;

	/** The object factory. */
	private static WorkflowObjectFactory objectFactory;

	/** The activiti workflow manager. */
	private static ActivitiWorkflowManager activitiWorkflowManager;

	/** The person service. */
	private static PersonService personService;

	/** The service registry. */
	private static ServiceRegistry serviceRegistry;

	/** The workflow service. */
	private WorkflowService workflowService;

	/** The unprotected node service. */
	private NodeService unprotectedNodeService;
	/** The registry. */
	protected ServiceRegistry registry;

	/** The case service. */
	protected CMFService caseService;

	/**
	 * Instantiates a new start workflow wizard.
	 *
	 * @param registry
	 *            the registry
	 * @param caseService
	 *            the case service
	 */
	public ActivitiWizards(ServiceRegistry registry, CMFService caseService) {
		this.registry = registry;
		this.caseService = caseService;
		initBeans(registry);
	}

	/**
	 * Inits the beans. Class synchronization to prevent collision
	 *
	 * @param registry
	 *            the registry to use
	 */
	protected synchronized static void initBeans(ServiceRegistry registry) {
		if (initialized == null) {
			LOGGER.debug("initBeans()");
			try {
				WorkflowUtil.setNamespaceService(registry.getNamespaceService());
				Field field = Repository.class.getDeclaredField("namespaceService");
				field.setAccessible(true);
				field.set(null, registry.getNamespaceService());
				field.setAccessible(false);

				field = Repository.class.getDeclaredField("serviceRegistry");
				field.setAccessible(true);
				field.set(null, registry);
				field.setAccessible(false);
				activitiWorkflowManager = (ActivitiWorkflowManager) registry.getService(QName
						.createQName(NamespaceService.ALFRESCO_URI, "activitiWorkflowManager"));
				WorkflowUtil.setActivitiWorkflowManager(activitiWorkflowManager);
				workflowReportService = (WorkflowReportService) registry
						.getService(CMFModel.TASK_REPORT_SERVICE_URI);
				personService = (PersonService) registry.getService(QName.createQName(
						NamespaceService.ALFRESCO_URI, "PersonService"));
				serviceRegistry = registry;
				initialized = Boolean.TRUE;
			} catch (Exception e) {
				LOGGER.error("Error during beans initialize!", e);
				initialized = Boolean.FALSE;
			}
		}
	}

	/**
	 * Gets the activiti util.
	 *
	 * @return the activiti util
	 */
	public static ActivitiUtil getActivitiUtil() {
		if (activitiUtil == null) {
			ActivitiWorkflowEngine workflowEngine = WorkflowUtil.getActivitiWorkflowManager()
					.getWorkflowEngine();
			try {
				Field declaredField = workflowEngine.getClass().getDeclaredField("activitiUtil");
				declaredField.setAccessible(true);
				setActivitiUtil((ActivitiUtil) declaredField.get(workflowEngine));
				declaredField.setAccessible(false);
			} catch (Exception e) {
				LOGGER.error("Changed API! Please Check ActivitiWorkflowEngine class", e);
			}
		}
		return activitiUtil;
	}

	/**
	 * Sets the activiti util.
	 *
	 * @param activitiUtil
	 *            the new activiti util
	 */
	private static void setActivitiUtil(ActivitiUtil activitiUtil) {
		ActivitiWizards.activitiUtil = activitiUtil;
	}

	/**
	 * Gets the type converter.
	 *
	 * @return the type converter
	 */
	public static ActivitiTypeConverter getTypeConverter() {
		if (typeConverter == null) {
			ActivitiWorkflowEngine workflowEngine = WorkflowUtil.getActivitiWorkflowManager()
					.getWorkflowEngine();
			try {
				Field declaredField = workflowEngine.getClass().getDeclaredField("typeConverter");
				declaredField.setAccessible(true);
				setTypeConverter((ActivitiTypeConverter) declaredField.get(workflowEngine));
				declaredField.setAccessible(false);
			} catch (Exception e) {
				LOGGER.error("Changed API! Please Check ActivitiWorkflowEngine class", e);
			}
		}
		return typeConverter;
	}

	/**
	 * Sets the type converter.
	 *
	 * @param typeConverter
	 *            the new type converter
	 */
	private static void setTypeConverter(ActivitiTypeConverter typeConverter) {
		ActivitiWizards.typeConverter = typeConverter;
	}

	/**
	 * Gets the property converter.
	 *
	 * @return the property converter
	 */
	public static ActivitiPropertyConverter getPropertyConverter() {
		if (propertyConverter == null) {
			setPropertyConverter(WorkflowUtil.getActivitiWorkflowManager().getPropertyConverter());
		}
		return propertyConverter;
	}

	/**
	 * Sets the property converter.
	 *
	 * @param propertyConverter
	 *            the new property converter
	 */
	private static void setPropertyConverter(ActivitiPropertyConverter propertyConverter) {
		ActivitiWizards.propertyConverter = propertyConverter;
	}

	/**
	 * Gets the object factory.
	 *
	 * @return the object factory
	 */
	public WorkflowObjectFactory getObjectFactory() {
		if (objectFactory == null) {
			ActivitiWorkflowEngine workflowEngine = WorkflowUtil.getActivitiWorkflowManager()
					.getWorkflowEngine();
			try {
				Field declaredField = workflowEngine.getClass().getDeclaredField("factory");
				declaredField.setAccessible(true);
				setObjectFactory((WorkflowObjectFactory) declaredField.get(workflowEngine));
				declaredField.setAccessible(false);
			} catch (Exception e) {
				LOGGER.error("Changed API! Please Check ActivitiWorkflowEngine class", e);
			}
		}
		return objectFactory;
	}

	/**
	 * Sets the object factory.
	 *
	 * @param objectFactory
	 *            the new object factory
	 */
	private static void setObjectFactory(WorkflowObjectFactory objectFactory) {
		ActivitiWizards.objectFactory = objectFactory;
	}

	/**
	 * Gets the workflow report service.
	 *
	 * @return the workflow report service
	 */
	public static WorkflowReportService getWorkflowReportService() {
		return workflowReportService;
	}

	/**
	 * Gets the person service.
	 *
	 * @return the person service
	 */
	public static PersonService getPersonService() {
		return personService;
	}

	/**
	 * Gets the activiti workflow manager.
	 *
	 * @return the activitiWorkflowManager
	 */
	public static ActivitiWorkflowManager getActivitiWorkflowManager() {
		return activitiWorkflowManager;
	}

	/**
	 * Gets the service registry of alfresco.
	 *
	 * @return the service registry
	 */
	public static ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/**
	 * Gets the dictionary service.
	 *
	 * @return the dictionary service
	 */
	protected DictionaryService getDictionaryService() {
		return registry.getDictionaryService();
	}

	/**
	 * Gets the node service.
	 *
	 * @return the node service
	 */
	protected NodeService getNodeService() {
		return registry.getNodeService();
	}

	/**
	 * Gets the workflow service.
	 *
	 * @return the workflow service
	 */
	protected WorkflowService getWorkflowService() {
		if (workflowService == null) {
			workflowService = registry.getWorkflowService();
		}
		return workflowService;
	}

	/**
	 * Gets the unprotected node service.
	 *
	 * @return the unprotected node service
	 */
	protected NodeService getUnprotectedNodeService() {
		if (unprotectedNodeService == null) {
			unprotectedNodeService = registry.getNodeService();
		}
		return unprotectedNodeService;
	}

	/**
	 * Extract the multiAssignees field value which is provided as parameter. It
	 * is expected to be collection of user/group names
	 * 
	 * @param multiAssignees
	 *            is the value to process
	 * @return pair of users/groups which are extracted.
	 */
	public static Pair<List<String>, List<String>> extractMultiAssignees(Object multiAssignees) {
		List<String> userAssignees = new LinkedList<String>();
		List<String> groupAssignees = new LinkedList<String>();
		if (multiAssignees == null) {
			return new Pair<List<String>, List<String>>(userAssignees, groupAssignees);
		}
		Collection<?> convertedValue = DefaultTypeConverter.INSTANCE.convert(Collection.class,
				multiAssignees);
		for (Object nextEntry : convertedValue) {
			AuthorityType authorityType = AuthorityType.getAuthorityType(nextEntry.toString());
			switch (authorityType) {
			case GROUP:
				((List<String>) groupAssignees).add(nextEntry.toString());
				break;
			case USER:
				((List<String>) userAssignees).add(nextEntry.toString());
				break;
			default:
				throw new RuntimeException("Invalid authority : " + nextEntry);
			}
		}
		return new Pair<List<String>, List<String>>(userAssignees, groupAssignees);
	}

	/**
	 * Log using arbitrary provided logger.
	 *
	 * @param level
	 *            is the log level
	 * @param error
	 *            the exception to log
	 * @param message
	 *            the messages to print as message
	 */
	protected void log(Level level, Throwable error, Object... message) {
		if (getLogger().isEnabledFor(level)) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string);
			}
			if (error != null) {
				getLogger().log(level, builder.toString(), error);
			} else {
				getLogger().log(level, builder.toString());
			}
		}
	}
	
	protected void debug(Object... message) {
		if (DEBUG_ENABLED) {
			log(Level.DEBUG, message);
		}
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	private Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Log using arbitrary provided logger.
	 *
	 * @param level
	 *            is the log level
	 * @param message
	 *            the messages to print as message
	 */
	protected void log(Level level, Object... message) {
		log(level, null, message);
	}
}
