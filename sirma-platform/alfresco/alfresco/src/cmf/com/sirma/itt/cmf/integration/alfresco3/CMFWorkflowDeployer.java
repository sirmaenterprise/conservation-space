/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.cmf.integration.alfresco3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Alfresco bootstrap Process deployment.
 * 
 * @author davidc
 */
public class CMFWorkflowDeployer extends AbstractLifecycleBean
// implements TenantDeployer
{
	// Logging support
	/** The logger. */
	private static Log logger = LogFactory.getLog(CMFWorkflowDeployer.class);

	// Workflow Definition Properties (used in setWorkflowDefinitions)
	/** The Constant ENGINE_ID. */
	public static final String ENGINE_ID = "engineId";

	/** The Constant LOCATION. */
	public static final String LOCATION = "location";

	/** The Constant MIMETYPE. */
	public static final String MIMETYPE = "mimetype";

	/** The Constant REDEPLOY. */
	public static final String REDEPLOY = "redeploy";

	// Dependencies
	/** The transaction service. */
	private TransactionService transactionService;

	/** The workflow service. */
	private WorkflowService workflowService;
	/** The authentication context. */
	private AuthenticationContext authenticationContext;

	/** The dictionary dao. */
	private DictionaryDAO dictionaryDAO;

	/** The workflow definitions. */
	private List<Properties> workflowDefinitions;

	/** The models. */
	private List<String> models = new ArrayList<String>();

	/** The resource bundles. */
	private List<String> resourceBundles = new ArrayList<String>();
	// private TenantAdminService tenantAdminService;
	/** The tenant service. */
	private TenantService tenantService;

	/** The node service. */
	private NodeService nodeService;

	/** The Constant CRITERIA_ALL. */
	public final static String CRITERIA_ALL = "/*"; // immediate children only

	/**
	 * Sets the Transaction Service.
	 * 
	 * @param transactionService
	 *            the new transaction service
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Sets the workflow service.
	 * 
	 * @param workflowService
	 *            the workflow service
	 */
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * Set the authentication component.
	 * 
	 * @param authenticationContext
	 *            the new authentication context
	 */
	public void setAuthenticationContext(AuthenticationContext authenticationContext) {
		this.authenticationContext = authenticationContext;
	}

	/**
	 * Sets the Dictionary DAO.
	 * 
	 * @param dictionaryDAO
	 *            the new dictionary dao
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	// /**
	// * Sets the tenant admin service
	// *
	// * @param tenantService
	// * the tenant admin service
	// */
	// public void setTenantAdminService(TenantAdminService tenantAdminService)
	// {
	// this.tenantAdminService = tenantAdminService;
	// }

	/**
	 * Sets the tenant service.
	 * 
	 * @param tenantService
	 *            the tenant service
	 */
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	/**
	 * Sets the Workflow Definitions.
	 * 
	 * @param workflowDefinitions
	 *            the new workflow definitions
	 */
	public void setWorkflowDefinitions(List<Properties> workflowDefinitions) {
		this.workflowDefinitions = workflowDefinitions;
	}

	/**
	 * Sets the initial list of Workflow models to bootstrap with.
	 * 
	 * @param modelResources
	 *            the model names
	 */
	public void setModels(List<String> modelResources) {
		this.models = modelResources;
	}

	/**
	 * Sets the initial list of Workflow resource bundles to bootstrap with.
	 * 
	 * @param labels
	 *            the new labels
	 */
	public void setLabels(List<String> labels) {
		this.resourceBundles = labels;
	}

	// used by TenantAdminService when creating a new tenant and bootstrapping
	// the pre-defined workflows
	/**
	 * Gets the workflow definitions.
	 * 
	 * @return the workflow definitions
	 */
	public List<Properties> getWorkflowDefinitions() {
		return this.workflowDefinitions;
	}

	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Deploy the Workflow Definitions.
	 */
	public void init() {
		PropertyCheck.mandatory(this, "transactionService", transactionService);
		PropertyCheck.mandatory(this, "authenticationContext", authenticationContext);
		PropertyCheck.mandatory(this, "workflowService", workflowService);

		String currentUser = authenticationContext.getCurrentUserName();
		if (currentUser == null) {
			authenticationContext.setSystemUserAsCurrentUser();
		}
		if (!transactionService.getAllowWrite()) {
			if (logger.isWarnEnabled())
				logger.warn("Repository is in read-only mode; not deploying workflows.");

			return;
		}

		UserTransaction userTransaction = transactionService.getUserTransaction();
		try {
			userTransaction.begin();

			// bootstrap the workflow models and static labels (from classpath)
			if (models != null && resourceBundles != null
					&& ((models.size() > 0) || (resourceBundles.size() > 0))) {
				DictionaryBootstrap dictionaryBootstrap = new DictionaryBootstrap();
				dictionaryBootstrap.setDictionaryDAO(dictionaryDAO);
				dictionaryBootstrap.setTenantService(tenantService);
				dictionaryBootstrap.setModels(models);
				dictionaryBootstrap.setLabels(resourceBundles);
				dictionaryBootstrap.bootstrap(); // also registers with
													// dictionary
			}

			// bootstrap the workflow definitions (from classpath)
			if (workflowDefinitions != null) {
				for (Properties workflowDefinition : workflowDefinitions) {
					// retrieve workflow specification
					String engineId = workflowDefinition.getProperty(ENGINE_ID);
					if (engineId == null || engineId.length() == 0) {
						throw new WorkflowException("Workflow Engine Id must be provided");
					}

					String location = workflowDefinition.getProperty(LOCATION);
					if (location == null || location.length() == 0) {
						throw new WorkflowException("Workflow definition location must be provided");
					}

					Boolean redeploy = Boolean.valueOf(workflowDefinition.getProperty(REDEPLOY));
					String mimetype = workflowDefinition.getProperty(MIMETYPE);

					// retrieve input stream on workflow definition
					ClassPathResource workflowResource = new ClassPathResource(location);

					// deploy workflow definition
					if (!redeploy
							&& workflowService.isDefinitionDeployed(engineId,
									workflowResource.getInputStream(), mimetype)) {
						if (logger.isDebugEnabled())
							logger.debug("Workflow deployer: Definition '" + location
									+ "' already deployed");
					} else {
						if (!redeploy
								&& workflowService.isDefinitionDeployed(engineId,
										workflowResource.getInputStream(), mimetype)) {
							if (logger.isDebugEnabled())
								logger.debug("Workflow deployer: Definition '" + location
										+ "' already deployed");
						} else {
							WorkflowDeployment deployment = workflowService.deployDefinition(
									engineId, workflowResource.getInputStream(),
									workflowResource.getFilename());
							logDeployment(location, deployment);
						}
					}

				}
			}

			userTransaction.commit();
		} catch (Throwable e) {
			// rollback the transaction
			try {
				if (userTransaction != null) {
					userTransaction.rollback();
				}
			} catch (Exception ex) {
				// NOOP
			}
		} finally {
			if (currentUser == null) {
				authenticationContext.clearCurrentSecurityContext();
			}
		}
	}

	/**
	 * Deploy.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @param redeploy
	 *            the redeploy
	 */
	public void deploy(NodeRef nodeRef, boolean redeploy) {
		// Ignore if the node is a working copy
		if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false) {
			Boolean value = (Boolean) nodeService.getProperty(nodeRef,
					WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
			if ((value != null) && (value.booleanValue() == true)) {
				String engineId = (String) nodeService.getProperty(nodeRef,
						WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID);

				if (!redeploy && workflowService.isDefinitionDeployed(nodeRef)) {
					if (logger.isDebugEnabled())
						logger.debug("Workflow deployer: Definition '" + nodeRef
								+ "' already deployed");
				} else {
					// deploy / re-deploy
					WorkflowDeployment deployment = workflowService.deployDefinition(nodeRef);
					logDeployment(nodeRef, deployment);
					if (deployment != null) {
						WorkflowDefinition def = deployment.getDefinition();

						// Update the meta data for the model
						Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

						props.put(WorkflowModel.PROP_WORKFLOW_DEF_NAME, def.getName());

						// TODO - ability to return and handle deployment
						// problems / warnings
						if (deployment.getProblems().length > 0) {
							for (String problem : deployment.getProblems()) {
								if (logger.isWarnEnabled())
									logger.warn(problem);
							}
						}

						nodeService.setProperties(nodeRef, props);
					}
				}

			}
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Workflow deployer: Definition '" + nodeRef
						+ "' not deployed since it is a working copy");
		}
	}

	/**
	 * Log deployment.
	 * 
	 * @param location
	 *            the location
	 * @param deployment
	 *            the deployment
	 */
	private void logDeployment(Object location, WorkflowDeployment deployment) {
		if (logger.isDebugEnabled()) {
			String title = deployment.getDefinition().getTitle();
			String version = deployment.getDefinition().getVersion();
			int problemLength = deployment.getProblems().length;
			logger.debug("Workflow deployer: Deployed process definition '" + title + "' (version "
					+ version + ") from '" + location + "' with " + problemLength + " problems");
		}
	}

	/**
	 * Undeploy.
	 * 
	 * @param nodeRef
	 *            the node ref
	 */
	public void undeploy(NodeRef nodeRef) {
		// Ignore if the node is a working copy
		if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false) {
			String defName = (String) nodeService.getProperty(nodeRef,
					WorkflowModel.PROP_WORKFLOW_DEF_NAME);
			if (defName != null) {
				// Undeploy the workflow definition - all versions in JBPM
				List<WorkflowDefinition> defs = workflowService.getAllDefinitionsByName(defName);
				for (WorkflowDefinition def : defs) {
					if (logger.isDebugEnabled())
						logger.debug("Undeploying workflow '" + defName + "' ...");

					workflowService.undeployDefinition(def.getId());

					if (logger.isDebugEnabled())
						logger.debug("... undeployed '" + def.getId() + "' v" + def.getVersion());
				}
			}
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Workflow deployer: Definition '" + nodeRef
						+ "' not undeployed since it is a working copy");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap
	 * (org.springframework.context.ApplicationEvent)
	 */
	@Override
	protected void onBootstrap(ApplicationEvent event) {
		// run as System on bootstrap
		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() {
				init();
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());

		// tenantAdminService.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown
	 * (org.springframework.context.ApplicationEvent)
	 */
	@Override
	protected void onShutdown(ApplicationEvent event) {
		// NOOP
	}
	//
	// @Override
	// public void onEnableTenant() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onDisableTenant() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void destroy() {
	// // TODO Auto-generated method stub
	//
	// }

}
