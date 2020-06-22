package com.sirma.itt.cmf.integration.workflow;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.mail.javamail.JavaMailSender;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

// TODO: Auto-generated Javadoc
/**
 * Utility class in use by workflow handlers.
 * 
 * @author Hristo <br/>
 */
public class ServiceProvider {

	/**
	 * Singleton instance.
	 */
	private static final ServiceProvider INSTANCE = new ServiceProvider();

	/**
	 * Bean factory.
	 */
	private BeanFactory factory;

	/** The namespace service. */
	private static NamespaceService namespaceService;

	/** The dictionary service. */
	private static DictionaryService dictionaryService;

	/**
	 * Initialize handlers helper with bean factory. Internal use only.
	 */
	protected ServiceProvider() {
		BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
		BeanFactoryReference factoryReference = factoryLocator.useBeanFactory(null);
		this.factory = factoryReference.getFactory();
	}

	/**
	 * Gets the factory.
	 *
	 * @return the factory
	 */
	public BeanFactory getFactory() {
		return factory;
	}

	/**
	 * Sets the factory.
	 *
	 * @param factory the factory to set
	 */
	public void setFactory(BeanFactory factory) {
		this.factory = factory;
	}

	/**
	 * Gets the single instance of ServiceProvider.
	 *
	 * @return the instance
	 */
	public static ServiceProvider getInstance() {
		return INSTANCE;
	}

	/**
	 * Retrieve bean by its name.
	 *
	 * @param name the name
	 * @return {@link Object}, the bean.
	 * {@link String}, name of the bean
	 */
	public static Object getBean(String name) {
		return INSTANCE.factory.getBean(name);
	}

	/**
	 * Retrieve the node service.
	 * 
	 * @return the node service
	 */
	public static NodeService getNodeService() {
		return (NodeService) getBean("nodeService");
	}

	/**
	 * Retrieve Permission service.
	 * 
	 * @return the permission service
	 */
	public static PermissionService getPermissionService() {
		return (PermissionService) getBean("permissionService");
	}

	/**
	 * Retrieve Copy service.
	 * 
	 * @return the Copy service
	 */
	public static CopyService getCopyService() {
		return (CopyService) getBean("copyService");
	}

	/**
	 * Retrieve Copy service.
	 * 
	 * @return the Copy service
	 */
	public static PersonService getPersonService() {
		return (PersonService) getBean("personService");
	}

	/**
	 * Get version service.
	 * 
	 * @return {@link VersionService}, the Version service
	 */
	public static VersionService getVersionService() {
		return (VersionService) getBean("versionService");
	}

	/**
	 * Get Authority service.
	 * 
	 * @return {@link AuthorityService}, the Authority service
	 */
	public static AuthorityService getAuthorityService() {
		return (AuthorityService) getBean("authorityService");
	}

	/**
	 * Get Mail service.
	 * 
	 * @return {@link JavaMailSender}, the mail service
	 */
	public static JavaMailSender getMailService() {
		return (JavaMailSender) getBean("mailService");
	}

	/**
	 * Get Template service.
	 * 
	 * @return {@link TemplateService}, the Template service
	 */
	public static TemplateService getTemplateService() {
		return (TemplateService) getBean("templateService");
	}

	/**
	 * Get sysAdminParams.
	 * 
	 * @return {@link SysAdminParams}, the system parameters
	 */
	public static SysAdminParams getSysAdminParams() {
		return (SysAdminParams) getBean("sysAdminParams");
	}

	/**
	 * Get Workflow Service.
	 * 
	 * @return {@link WorkflowService}, the workflow service
	 */
	public static WorkflowService getWorkflowService() {
		return (WorkflowService) getBean("workflowServiceImpl");
	}

	/**
	 * Retrieve the OwnableService.
	 * 
	 * @return the OwnableService.
	 */
	public static OwnableService getOwnableService() {
		return (OwnableService) getBean("ownableService");
	}

	/**
	 * Retrieve the AuthenticationService.
	 * 
	 * @return the AuthenticationService.
	 */
	public static AuthenticationService getAuthenticationService() {
		return (AuthenticationService) getBean("authenticationService");
	}

	/**
	 * Retrieve the TransactionService instance.
	 * 
	 * @return the TransactionService.
	 */
	public static TransactionService getTransactionService() {
		return (TransactionService) getBean("transactionService");
	}

	/**
	 * Retrieve the SearchService instance.
	 * 
	 * @return the SearchService.
	 */
	public static SearchService getSearchService() {
		return (SearchService) getBean("searchService");
	}

	/**
	 * Gets the dictionary service.
	 *
	 * @return the dictionary service
	 */
	public static DictionaryService getDictionaryService() {
		if (dictionaryService == null) {
			dictionaryService = (DictionaryService) getBean("dictionaryService");
		}
		return dictionaryService;
	}

	/**
	 * Gets the namespace service.
	 * 
	 * @return the namespace service
	 */
	public static NamespaceService getNamespaceService() {
		if (namespaceService == null) {
			namespaceService = (NamespaceService) getBean("namespaceService");
		}
		return namespaceService;
	}
}
