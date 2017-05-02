/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.security.sync;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.SEIPTenantIntegration;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TraceableThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * A <code>TenantChainingUserRegistrySynchronizer</code> is responsible for
 * synchronizing Alfresco's local user (person) and group (authority)
 * information with the external subsystems in the authentication chain (most
 * typically LDAP directories). When the {@link #synchronize(boolean)} method is
 * called, it visits each {@link UserRegistry} bean in the 'chain' of
 * application contexts, managed by a {@link ChildApplicationContextManager},
 * and compares its timestamped user and group information with the local users
 * and groups last retrieved from the same source. Any updates and additions
 * made to those users and groups are applied to the local copies. The ordering
 * of each {@link UserRegistry} in the chain determines its precedence when it
 * comes to user and group name collisions. The {@link JobLockService} is used
 * to ensure that in a cluster, no two nodes actually run a synchronize at the
 * same time.
 * <p>
 * The <code>force</code> argument determines whether a complete or partial set
 * of information is queried from the {@link UserRegistry}. When
 * <code>true</code> then <i>all</i> users and groups are queried. With this
 * complete set of information, the synchronizer is able to identify which users
 * and groups have been deleted, so it will delete users and groups as well as
 * update and create them. Since processing all users and groups may be fairly
 * time consuming, it is recommended this mode is only used by a background
 * scheduled synchronization job. When the argument is <code>false</code> then
 * only those users and groups modified since the most recent modification date
 * of all the objects last queried from the same {@link UserRegistry} are
 * retrieved. In this mode, local users and groups are created and updated, but
 * not deleted (except where a name collision with a lower priority
 * {@link UserRegistry} is detected). This 'differential' mode is much faster,
 * and by default is triggered on subsystem startup and also by
 * {@link #createMissingPerson(String)} when a user is successfully
 * authenticated who doesn't yet have a local person object in Alfresco. This
 * should mean that new users and their group information are pulled over from
 * LDAP servers as and when required.
 * 
 * @author dward
 */
public class TenantChainingUserRegistrySynchronizer extends AbstractLifecycleBean
		implements UserRegistrySynchronizer, ApplicationEventPublisherAware {
	/** The logger. */
	private static final Log logger = LogFactory.getLog(TenantChainingUserRegistrySynchronizer.class);

	/**
	 * The name of the lock used to ensure that a synchronize does not run on
	 * more than one node at the same time.
	 */
	private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
			"TenantChainingUserRegistrySynchronizer");

	/**
	 * The time this lock will persist for in the database (now only 2 minutes
	 * but refreshed at regular intervals).
	 */
	private static final long LOCK_TTL = 1000 * 60 * 2;

	/** The path in the attribute service below which we persist attributes. */
	public static final String ROOT_ATTRIBUTE_PATH = ".TenantChainingUserRegistrySynchronizer";

	/**
	 * The label under which the last group modification timestamp is stored for
	 * each zone.
	 */
	private static final String GROUP_LAST_MODIFIED_ATTRIBUTE = "GROUP";

	/**
	 * The label under which the last user modification timestamp is stored for
	 * each zone.
	 */
	private static final String PERSON_LAST_MODIFIED_ATTRIBUTE = "PERSON";

	/** The manager for the autentication chain to be traversed. */
	private ChildApplicationContextManager applicationContextManager;

	/**
	 * The name used to look up a {@link UserRegistry} bean in each child
	 * application context.
	 */
	private String sourceBeanName;

	/** The authority service. */
	private AuthorityService authorityService;

	/** The person service. */
	private PersonService personService;

	/** The attribute service. */
	private AttributeService attributeService;

	/** The transaction service. */
	private TransactionService transactionService;

	/** The rule service. */
	private RuleService ruleService;

	/** The job lock service. */
	private JobLockService jobLockService;

	/** The application event publisher. */
	private ApplicationEventPublisher applicationEventPublisher;

	/** Should we trigger a differential sync when missing people log in?. */
	private boolean syncWhenMissingPeopleLogIn = true;

	/** Should we trigger a differential sync on startup?. */
	private boolean syncOnStartup = true;

	/** Should we auto create a missing person on log in?. */
	private boolean autoCreatePeopleOnLogin = true;

	/** The number of entries to process before reporting progress. */
	private int loggingInterval = 100;

	/** The number of worker threads. */
	private int workerThreads = 2;

	private MBeanServerConnection mbeanServer;

	/** Allow a full sync to perform deletions? */
	private boolean allowDeletions = true;

	private TenantAdminDAO tenantAdminDAO;

	/**
	 * Sets the application context manager.
	 * 
	 * @param applicationContextManager
	 *            the applicationContextManager to set
	 */
	public void setApplicationContextManager(ChildApplicationContextManager applicationContextManager) {
		this.applicationContextManager = applicationContextManager;
	}

	/**
	 * Sets the name used to look up a {@link UserRegistry} bean in each child
	 * application context.
	 * 
	 * @param sourceBeanName
	 *            the bean name
	 */
	public void setSourceBeanName(String sourceBeanName) {
		this.sourceBeanName = sourceBeanName;
	}

	/**
	 * Sets the authority service.
	 * 
	 * @param authorityService
	 *            the new authority service
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * Sets the person service.
	 * 
	 * @param personService
	 *            the new person service
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * Sets the attribute service.
	 * 
	 * @param attributeService
	 *            the new attribute service
	 */
	public void setAttributeService(AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	/**
	 * Sets the transaction service.
	 * 
	 * @param transactionService
	 *            the transaction service
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Sets the rule service.
	 * 
	 * @param ruleService
	 *            the new rule service
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * Sets the job lock service.
	 * 
	 * @param jobLockService
	 *            the job lock service
	 */
	public void setJobLockService(JobLockService jobLockService) {
		this.jobLockService = jobLockService;
	}

	public void setTenantAdminDAO(TenantAdminDAO tenantAdminDAO) {
		this.tenantAdminDAO = tenantAdminDAO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.ApplicationEventPublisherAware#
	 * setApplicationEventPublisher(org.springframework.context
	 * .ApplicationEventPublisher)
	 */
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/**
	 * Controls whether we auto create a missing person on log in.
	 * 
	 * @param autoCreatePeopleOnLogin
	 *            <code>true</code> if we should auto create a missing person on
	 *            log in
	 */
	public void setAutoCreatePeopleOnLogin(boolean autoCreatePeopleOnLogin) {
		this.autoCreatePeopleOnLogin = autoCreatePeopleOnLogin;
	}

	/**
	 * Controls whether we trigger a differential sync when missing people log
	 * in.
	 * 
	 * @param syncWhenMissingPeopleLogIn
	 *            <codetrue</code> if we should trigger a sync when missing
	 *            people log in
	 */
	public void setSyncWhenMissingPeopleLogIn(boolean syncWhenMissingPeopleLogIn) {
		this.syncWhenMissingPeopleLogIn = syncWhenMissingPeopleLogIn;
	}

	/**
	 * Controls whether we trigger a differential sync when the subsystem starts
	 * up.
	 * 
	 * @param syncOnStartup
	 *            <codetrue</code> if we should trigger a sync on startup
	 */
	public void setSyncOnStartup(boolean syncOnStartup) {
		this.syncOnStartup = syncOnStartup;
	}

	/**
	 * Sets the number of entries to process before reporting progress.
	 * 
	 * @param loggingInterval
	 *            the number of entries to process before reporting progress or
	 *            zero to disable progress reporting.
	 */
	public void setLoggingInterval(int loggingInterval) {
		this.loggingInterval = loggingInterval;
	}

	/**
	 * Sets the number of worker threads.
	 * 
	 * @param workerThreads
	 *            the number of worker threads
	 */
	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	/**
	 * Fullsync is run with deletions. By default is set to true.
	 * 
	 * @param allowDeletions
	 */
	public void setAllowDeletions(boolean allowDeletions) {
		this.allowDeletions = allowDeletions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.security.sync.UserRegistrySynchronizer#synchronize(
	 * boolean, boolean, boolean)
	 */
	public void synchronize(final boolean forceUpdate, final boolean isFullSync, final boolean splitTxns) {

		if (AuthenticationUtil.isMtEnabled()) {
			List<TenantEntity> listTenants = tenantAdminDAO.listTenants();
			for (TenantEntity tenantEntity : listTenants) {
				logger.info("Sycnhronizing a tenant:" + tenantEntity.getTenantDomain());
				if (tenantEntity.getEnabled().booleanValue()) {
					AuthenticationUtil.runAs(new RunAsWork<Object>() {
						public Object doWork() throws Exception {
							try {
								synchronizeInternal(forceUpdate, isFullSync, splitTxns);
							} catch (Exception e) {
								TenantChainingUserRegistrySynchronizer.logger
										.warn("Failed initial synchronize with user registries", e);
							}
							return null;
						}
					}, SEIPTenantIntegration.getSystemUserByTenantId(tenantEntity.getTenantDomain()));
				}
			}
		}
		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				try {
					logger.info("Sycnhronizing the default tenant:");
					synchronizeInternal(forceUpdate, isFullSync, splitTxns);
				} catch (Exception e) {
					TenantChainingUserRegistrySynchronizer.logger
							.warn("Failed initial synchronize with user registries", e);
				}
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	public void synchronize(String tenantId, final boolean forceUpdate, final boolean isFullSync,
			final boolean splitTxns) {

		if (AuthenticationUtil.isMtEnabled()) {
			TenantEntity tenant = tenantAdminDAO.getTenant(tenantId);
			logger.info("Sycnhronizing a tenant:" + tenantId);
			if (tenant.getEnabled().booleanValue()) {
				logger.warn("Information for tenant is missing or it is disabled!");
			}
			AuthenticationUtil.runAs(new RunAsWork<Object>() {
				public Object doWork() throws Exception {
					try {
						synchronizeInternal(forceUpdate, isFullSync, splitTxns);
					} catch (Exception e) {
						TenantChainingUserRegistrySynchronizer.logger
								.warn("Failed initial synchronize with user registries", e);
					}
					return null;
				}
			}, SEIPTenantIntegration.getSystemUserByTenantId(tenant.getTenantDomain()));
		}
	}

	private void synchronizeInternal(boolean forceUpdate, boolean isFullSync, final boolean splitTxns) {
		TenantChainingUserRegistrySynchronizer.logger
				.debug("Running a sync for domain: " + SEIPTenantIntegration.getTenantId());
		if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {

			if (forceUpdate) {
				TenantChainingUserRegistrySynchronizer.logger.debug("Running a full sync.");
			} else {
				TenantChainingUserRegistrySynchronizer.logger.debug("Running a differential sync.");
			}
			if (allowDeletions) {
				TenantChainingUserRegistrySynchronizer.logger.debug("deletions are allowed");
			} else {
				TenantChainingUserRegistrySynchronizer.logger.debug("deletions are not allowed");
			}
			// Don't proceed with the sync if the repository is read only
			if (this.transactionService.isReadOnly()) {
				TenantChainingUserRegistrySynchronizer.logger
						.warn("Unable to proceed with user registry synchronization. Repository is read only.");
				return;
			}
		}

		// Don't proceed with the sync if the repository is read only
		if (this.transactionService.isReadOnly()) {
			TenantChainingUserRegistrySynchronizer.logger
					.warn("Unable to proceed with user registry synchronization. Repository is read only.");
			return;
		}

		// Create a background executor that will refresh our lock. This means
		// we can request a lock with a relatively
		// small persistence time and not worry about it lasting after server
		// restarts. Note we use an independent
		// executor because this is a compound operation that spans accross
		// multiple batch processors.
		String lockToken = null;
		TraceableThreadFactory threadFactory = new TraceableThreadFactory();
		threadFactory.setNamePrefix("TenantChainingUserRegistrySynchronizer lock refresh");
		threadFactory.setThreadDaemon(true);
		ScheduledExecutorService lockRefresher = new ScheduledThreadPoolExecutor(1, threadFactory);

		// Let's ensure all exceptions get logged
		try {
			// First, try to obtain a lock to ensure we are the only node trying
			// to run this job
			try {
				if (splitTxns) {
					// If this is an automated sync on startup or scheduled
					// sync, don't even wait around for the lock.
					// Assume the sync will be completed on another node.
					lockToken = this.transactionService.getRetryingTransactionHelper()
							.doInTransaction(new RetryingTransactionCallback<String>() {
								public String execute() throws Throwable {
									return TenantChainingUserRegistrySynchronizer.this.jobLockService.getLock(
											TenantChainingUserRegistrySynchronizer.LOCK_QNAME,
											TenantChainingUserRegistrySynchronizer.LOCK_TTL, 0, 1);
								}
							}, false, splitTxns);
				} else {
					// If this is a login-triggered sync, give it a few retries
					// before giving up
					lockToken = this.jobLockService.getLock(TenantChainingUserRegistrySynchronizer.LOCK_QNAME,
							TenantChainingUserRegistrySynchronizer.LOCK_TTL, 3000, 10);
				}
			} catch (LockAcquisitionException e) {
				// Don't proceed with the sync if it is running on another node
				TenantChainingUserRegistrySynchronizer.logger
						.warn("User registry synchronization already running in another thread. Synchronize aborted");
				return;
			}

			// Schedule the lock refresh to run at regular intervals
			final String token = lockToken;
			lockRefresher.scheduleAtFixedRate(new Runnable() {
				public void run() {
					TenantChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper()
							.doInTransaction(new RetryingTransactionCallback<Object>() {
						public Object execute() throws Throwable {
							TenantChainingUserRegistrySynchronizer.this.jobLockService.refreshLock(token,
									TenantChainingUserRegistrySynchronizer.LOCK_QNAME,
									TenantChainingUserRegistrySynchronizer.LOCK_TTL);
							return null;
						}
					}, false, splitTxns);
				}
			}, TenantChainingUserRegistrySynchronizer.LOCK_TTL / 2, TenantChainingUserRegistrySynchronizer.LOCK_TTL / 2,
					TimeUnit.MILLISECONDS);

			Set<String> visitedZoneIds = new TreeSet<String>();
			Collection<String> instanceIds = this.applicationContextManager.getInstanceIds();

			// Work out the set of all zone IDs in the authentication chain so
			// that we can decide which users / groups
			// need 're-zoning'
			Set<String> allZoneIds = new TreeSet<String>();
			for (String id : instanceIds) {
				allZoneIds.add(AuthorityService.ZONE_AUTH_EXT_PREFIX + id);
			}
			for (String id : instanceIds) {
				ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
				try {
					UserRegistry plugin = (UserRegistry) context.getBean(this.sourceBeanName);
					if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive()) {
						if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
							mbeanServer = (MBeanServerConnection) getApplicationContext()
									.getBean("alfrescoMBeanServer");
							try {
								StringBuilder nameBuff = new StringBuilder(200)
										.append("Alfresco:Type=Configuration,Category=Authentication,id1=managed,id2=")
										.append(URLDecoder.decode(id, "UTF-8"));
								ObjectName name = new ObjectName(nameBuff.toString());
								if (mbeanServer != null && mbeanServer.isRegistered(name)) {
									MBeanInfo info = mbeanServer.getMBeanInfo(name);
									MBeanAttributeInfo[] attributes = info.getAttributes();
									TenantChainingUserRegistrySynchronizer.logger.debug(id + " attributes:");
									for (MBeanAttributeInfo attribute : attributes) {
										Object value = mbeanServer.getAttribute(name, attribute.getName());
										TenantChainingUserRegistrySynchronizer.logger
												.debug(attribute.getName() + " = " + value);
									}
								}
							} catch (UnsupportedEncodingException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (MalformedObjectNameException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (InstanceNotFoundException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (IntrospectionException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (AttributeNotFoundException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (ReflectionException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (MBeanException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							} catch (IOException e) {
								if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger.warn("Exception during logging", e);
								}
							}

						}
						if (TenantChainingUserRegistrySynchronizer.logger.isInfoEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger
									.info("Synchronizing users and groups with user registry '" + id + "'");
						}
						if (isFullSync && TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger
									.warn("Full synchronization with user registry '" + id + "'");
							if (allowDeletions) {
								TenantChainingUserRegistrySynchronizer.logger.warn(
										"Some users and groups previously created by synchronization with this user registry may be removed.");
							} else {
								TenantChainingUserRegistrySynchronizer.logger.warn(
										"Deletions are disabled. Users and groups removed from this registry will be logged only and will remain in the repository. Users previously found in a different registry will be moved in the repository rather than recreated.");
							}
						}
						// Work out whether we should do the work in a separate
						// transaction (it's most performant if we
						// bunch it into small transactions, but if we are doing
						// a sync on login, it has to be the same
						// transaction)
						boolean requiresNew = splitTxns
								|| AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;

						syncWithPlugin(id, plugin, forceUpdate, isFullSync, requiresNew, visitedZoneIds, allZoneIds);
					}
				} catch (NoSuchBeanDefinitionException e) {
					// Ignore and continue
				}

			}
		} catch (RuntimeException e) {
			TenantChainingUserRegistrySynchronizer.logger.error("Synchronization aborted due to error", e);
			throw e;
		}
		// Release the lock if necessary
		finally {
			if (lockToken != null) {
				// Cancel the lock refresher
				// Because we may hit a perfect storm when trying to interrupt
				// workers in their unsynchronized getTask()
				// method we can't wait indefinitely and may have to retry the
				// shutdown
				int trys = 0;
				do {
					lockRefresher.shutdown();
					try {
						lockRefresher.awaitTermination(TenantChainingUserRegistrySynchronizer.LOCK_TTL,
								TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
					}
				} while (!lockRefresher.isTerminated() && trys++ < 3);
				if (!lockRefresher.isTerminated()) {
					lockRefresher.shutdownNow();
					TenantChainingUserRegistrySynchronizer.logger.error("Failed to shut down lock refresher");
				}

				final String token = lockToken;
				this.transactionService.getRetryingTransactionHelper()
						.doInTransaction(new RetryingTransactionCallback<Object>() {
							public Object execute() throws Throwable {
								TenantChainingUserRegistrySynchronizer.this.jobLockService.releaseLock(token,
										TenantChainingUserRegistrySynchronizer.LOCK_QNAME);
								return null;
							}
						}, false, splitTxns);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#
	 * getPersonMappedProperties(java.lang.String)
	 */
	public Set<QName> getPersonMappedProperties(String username) {
		Set<String> authorityZones = this.authorityService.getAuthorityZones(username);
		if (authorityZones == null) {
			return Collections.emptySet();
		}
		Collection<String> instanceIds = this.applicationContextManager.getInstanceIds();

		// Visit the user registries in priority order and return the person
		// mapping of the first registry that matches
		// one of the person's zones
		for (String id : instanceIds) {
			String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + id;
			if (!authorityZones.contains(zoneId)) {
				continue;
			}
			ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
			try {
				UserRegistry plugin = (UserRegistry) context.getBean(this.sourceBeanName);
				if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive()) {
					return plugin.getPersonMappedProperties();
				}
			} catch (NoSuchBeanDefinitionException e) {
				// Ignore and continue
			}
		}

		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#
	 * createMissingPerson(java.lang.String)
	 */
	public boolean createMissingPerson(String userName) {
		// synchronize or auto-create the missing person if we are allowed
		if (userName != null && !userName.equals(AuthenticationUtil.getSystemUserName())) {
			if (this.syncWhenMissingPeopleLogIn) {
				try {
					synchronizeInternal(false, false, false);
				} catch (Exception e) {
					// We don't want to fail the whole login if we can help it
					TenantChainingUserRegistrySynchronizer.logger
							.warn("User authenticated but failed to sync with user registry", e);
				}
				if (this.personService.personExists(userName)) {
					return true;
				}
			}
			if (this.autoCreatePeopleOnLogin && this.personService.createMissingPeople()) {
				AuthorityType authorityType = AuthorityType.getAuthorityType(userName);
				if (authorityType == AuthorityType.USER) {
					this.personService.getPerson(userName);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Synchronizes local groups and users with a {@link UserRegistry} for a
	 * particular zone, optionally handling deletions.
	 * 
	 * @param zone
	 *            the zone id. This identifier is used to tag all created groups
	 *            and users, so that in the future we can tell those that have
	 *            been deleted from the registry.
	 * @param userRegistry
	 *            the user registry for the zone.
	 * @param forceUpdate
	 *            Should the complete set of users and groups be updated /
	 *            created locally or just those known to have changed since the
	 *            last sync? When <code>true</code> then <i>all</i> users and
	 *            groups are queried from the user registry and updated locally.
	 *            When <code>false</code> then each source is only queried for
	 *            those users and groups modified since the most recent
	 *            modification date of all the objects last queried from that
	 *            same source.
	 * @param isFullSync
	 *            Should a complete set of user and group IDs be queried from
	 *            the user registries in order to determine deletions? This
	 *            parameter is independent of <code>force</code> as a separate
	 *            query is run to process updates.
	 * @param splitTxns
	 *            Can the modifications to Alfresco be split across multiple
	 *            transactions for maximum performance? If <code>true</code>,
	 *            users and groups are created/updated in batches for increased
	 *            performance. If <code>false</code>, all users and groups are
	 *            processed in the current transaction. This is required if
	 *            calling synchronously (e.g. in response to an authentication
	 *            event in the same transaction).
	 * @param visitedZoneIds
	 *            the set of zone ids already processed. These zones have
	 *            precedence over the current zone when it comes to group name
	 *            'collisions'. If a user or group is queried that already
	 *            exists locally but is tagged with one of the zones in this
	 *            set, then it will be ignored as this zone has lower priority.
	 * @param allZoneIds
	 *            the set of all zone ids in the authentication chain. Helps us
	 *            work out whether the zone information recorded against a user
	 *            or group is invalid for the current authentication chain and
	 *            whether the user or group needs to be 're-zoned'.
	 */
	private void syncWithPlugin(final String zone, UserRegistry userRegistry, boolean forceUpdate, boolean isFullSync,
			boolean splitTxns, final Set<String> visitedZoneIds, final Set<String> allZoneIds) {
		// Create a prefixed zone ID for use with the authority service
		final String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + zone;

		// Ensure that the zoneId exists before multiple threads start using it
		this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
			@Override
			public Void execute() throws Throwable {
				authorityService.getOrCreateZone(zoneId);
				return null;
			}
		}, false, splitTxns);

		// The set of zones we associate with new objects (default plus registry
		// specific)
		final Set<String> zoneSet = getZones(zoneId);

		long lastModifiedMillis = forceUpdate ? -1
				: getMostRecentUpdateTime(TenantChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId,
						splitTxns);
		Date lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);

		if (TenantChainingUserRegistrySynchronizer.logger.isInfoEnabled()) {
			if (lastModified == null) {
				TenantChainingUserRegistrySynchronizer.logger
						.info("Retrieving all groups from user registry '" + zone + "'");
			} else {
				TenantChainingUserRegistrySynchronizer.logger.info("Retrieving groups changed since "
						+ DateFormat.getDateTimeInstance().format(lastModified) + " from user registry '" + zone + "'");
			}
		}

		// First, analyze the group structure. Create maps of authorities to
		// their parents for associations to create
		// and delete. Also deal with 'overlaps' with other zones in the
		// authentication chain.
		final BatchProcessor<NodeDescription> groupProcessor = new BatchProcessor<NodeDescription>(
				zone + " Group Analysis", this.transactionService.getRetryingTransactionHelper(),
				userRegistry.getGroups(lastModified), this.workerThreads, 20, this.applicationEventPublisher,
				TenantChainingUserRegistrySynchronizer.logger, this.loggingInterval);
		class Analyzer extends BaseBatchProcessWorker<NodeDescription> {
			private final Map<String, String> groupsToCreate = new TreeMap<String, String>();
			private final Map<String, Set<String>> personParentAssocsToCreate = newPersonMap();
			private final Map<String, Set<String>> personParentAssocsToDelete = newPersonMap();
			private Map<String, Set<String>> groupParentAssocsToCreate = new TreeMap<String, Set<String>>();
			private final Map<String, Set<String>> groupParentAssocsToDelete = new TreeMap<String, Set<String>>();
			private final Map<String, Set<String>> finalGroupChildAssocs = new TreeMap<String, Set<String>>();
			private List<String> personsProcessed = new LinkedList<String>();
			private Set<String> allZonePersons = Collections.emptySet();
			private Set<String> deletionCandidates;

			private long latestTime;

			public Analyzer(final long latestTime) {
				this.latestTime = latestTime;
			}

			public long getLatestTime() {
				return this.latestTime;
			}

			public Set<String> getDeletionCandidates() {
				return this.deletionCandidates;
			}

			public String getIdentifier(NodeDescription entry) {
				return entry.getSourceId();
			}

			public void process(final NodeDescription group) throws Throwable {
				PropertyMap groupProperties = group.getProperties();
				String tenantDomain = (String) groupProperties.get(ContentModel.PROP_ORGANIZATION);
				logger.debug("Process group: " + groupProperties.get(ContentModel.PROP_AUTHORITY_NAME)
						+ (tenantDomain == null ? "" : ("/" + tenantDomain)));

				if (tenantDomain != null) {
					if (!isTenantEnabled(tenantDomain)) {
						return;
					}
					AuthenticationUtil.runAs(new RunAsWork<Void>() {
						@Override
						public Void doWork() throws Exception {
							processInTenantMode(group);
							return null;
						}
					}, SEIPTenantIntegration.getSystemUserByTenantId(tenantDomain));
				} else {
					processInTenantMode(group);
				}
			}

			private void processInTenantMode(NodeDescription group) {
				PropertyMap groupProperties = group.getProperties();
				String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
				String groupShortName = TenantChainingUserRegistrySynchronizer.this.authorityService
						.getShortName(groupName);
				Set<String> groupZones = TenantChainingUserRegistrySynchronizer.this.authorityService
						.getAuthorityZones(groupName);

				if (groupZones == null) {
					// The group did not exist at all
					updateGroup(group, false);
				} else {
					// Check whether the group is in any of the authentication
					// chain zones
					Set<String> intersection = new TreeSet<String>(groupZones);
					intersection.retainAll(allZoneIds);
					// Check whether the group is in any of the higher priority
					// authentication chain zones
					Set<String> visited = new TreeSet<String>(intersection);
					visited.retainAll(visitedZoneIds);

					if (groupZones.contains(zoneId)) {
						// The group already existed in this zone: update the
						// group
						updateGroup(group, true);
					} else if (!visited.isEmpty()) {
						// A group that exists in a different zone with higher
						// precedence
						return;
					} else if (!allowDeletions || intersection.isEmpty()) {
						// Deletions are disallowed or the group exists, but not
						// in a zone that's in the authentication
						// chain. May be due to upgrade or zone changes. Let's
						// re-zone them
						if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.warn("Updating group '" + groupShortName
									+ "'. This group will in future be assumed to originate from user registry '" + zone
									+ "'.");
						}
						updateAuthorityZones(groupName, groupZones, zoneSet);

						// The group now exists in this zone: update the group
						updateGroup(group, true);
					} else {
						// The group existed, but in a zone with lower
						// precedence
						if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.warn("Recreating occluded group '"
									+ groupShortName
									+ "'. This group was previously created through synchronization with a lower priority user registry.");
						}
						TenantChainingUserRegistrySynchronizer.this.authorityService.deleteAuthority(groupName);

						// create the group
						updateGroup(group, false);
					}
				}

				synchronized (this) {
					// Maintain the last modified date
					Date groupLastModified = group.getLastModified();
					if (groupLastModified != null) {
						this.latestTime = Math.max(this.latestTime, groupLastModified.getTime());
					}
				}
			}

			// Recursively walks and caches the authorities relating to and from
			// this group so that we can later detect potential cycles
			private Set<String> getContainedAuthorities(String groupName) {
				// Return the cached children if it is processed
				Set<String> children = this.finalGroupChildAssocs.get(groupName);
				if (children != null) {
					return children;
				}

				// First, recurse to the parent most authorities
				for (String parent : TenantChainingUserRegistrySynchronizer.this.authorityService
						.getContainingAuthorities(null, groupName, true)) {
					getContainedAuthorities(parent);
				}

				// Now descend on unprocessed parents.
				return cacheContainedAuthorities(groupName);
			}

			private Set<String> cacheContainedAuthorities(String groupName) {
				// Return the cached children if it is processed
				Set<String> children = this.finalGroupChildAssocs.get(groupName);
				if (children != null) {
					return children;
				}

				// Descend on unprocessed parents.
				children = TenantChainingUserRegistrySynchronizer.this.authorityService.getContainedAuthorities(null,
						groupName, true);
				this.finalGroupChildAssocs.put(groupName, children);

				for (String child : children) {
					if (AuthorityType.getAuthorityType(child) != AuthorityType.USER) {
						cacheContainedAuthorities(child);
					}
				}
				return children;
			}

			private synchronized void updateGroup(NodeDescription group, boolean existed) {
				PropertyMap groupProperties = group.getProperties();
				String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
				String groupDisplayName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
				if (groupDisplayName == null) {
					groupDisplayName = TenantChainingUserRegistrySynchronizer.this.authorityService
							.getShortName(groupName);
				}

				// Divide the child associations into person and group
				// associations, dealing with case sensitivity
				Set<String> newChildPersons = newPersonSet();
				Set<String> newChildGroups = new TreeSet<String>();

				for (String child : group.getChildAssociations()) {
					if (AuthorityType.getAuthorityType(child) == AuthorityType.USER) {
						newChildPersons.add(child);
					} else {
						newChildGroups.add(child);
					}
				}

				// Account for differences if already existing
				if (existed) {
					// Update the display name now
					TenantChainingUserRegistrySynchronizer.this.authorityService.setAuthorityDisplayName(groupName,
							groupDisplayName);

					// Work out the association differences
					for (String child : new TreeSet<String>(getContainedAuthorities(groupName))) {
						if (AuthorityType.getAuthorityType(child) == AuthorityType.USER) {
							if (!newChildPersons.remove(child)) {
								recordParentAssociationDeletion(child, groupName);
							}
						} else {
							if (!newChildGroups.remove(child)) {
								recordParentAssociationDeletion(child, groupName);
							}
						}
					}
				}
				// Mark as created if new
				else {
					// Make sure each group to be created features in the
					// association deletion map (as these are handled in the
					// same phase)
					recordParentAssociationDeletion(groupName, null);
					this.groupsToCreate.put(groupName, groupDisplayName);
				}

				// Create new associations
				for (String child : newChildPersons) {
					// Make sure each person with association changes features
					// as a key in the deletion map
					recordParentAssociationDeletion(child, null);
					recordParentAssociationCreation(child, groupName);
				}
				for (String child : newChildGroups) {
					// Make sure each group with association changes features as
					// a key in the deletion map
					recordParentAssociationDeletion(child, null);
					recordParentAssociationCreation(child, groupName);
				}
			}

			private void recordParentAssociationDeletion(String child, String parent) {
				Map<String, Set<String>> parentAssocs;
				if (AuthorityType.getAuthorityType(child) == AuthorityType.USER) {
					parentAssocs = this.personParentAssocsToDelete;
				} else {
					// Reflect the change in the map of final group associations
					// (for cycle detection later)
					parentAssocs = this.groupParentAssocsToDelete;
					if (parent != null) {
						Set<String> children = this.finalGroupChildAssocs.get(parent);
						children.remove(child);
					}
				}
				Set<String> parents = parentAssocs.get(child);
				if (parents == null) {
					parents = new TreeSet<String>();
					parentAssocs.put(child, parents);
				}
				if (parent != null) {
					parents.add(parent);
				}
			}

			private void recordParentAssociationCreation(String child, String parent) {
				Map<String, Set<String>> parentAssocs = AuthorityType.getAuthorityType(child) == AuthorityType.USER
						? this.personParentAssocsToCreate : this.groupParentAssocsToCreate;
				Set<String> parents = parentAssocs.get(child);
				if (parents == null) {
					parents = new TreeSet<String>();
					parentAssocs.put(child, parents);
				}
				if (parent != null) {
					parents.add(parent);
				}
			}

			private void validateGroupParentAssocsToCreate() {
				Iterator<Map.Entry<String, Set<String>>> i = this.groupParentAssocsToCreate.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<String, Set<String>> entry = i.next();
					String group = entry.getKey();
					Set<String> parents = entry.getValue();
					Deque<String> visited = new LinkedList<String>();
					Iterator<String> j = parents.iterator();
					while (j.hasNext()) {
						String parent = j.next();
						visited.add(parent);
						if (validateAuthorityChildren(visited, group)) {
							// The association validated - commit it
							Set<String> children = finalGroupChildAssocs.get(parent);
							if (children == null) {
								children = new TreeSet<String>();
								finalGroupChildAssocs.put(parent, children);
							}
							children.add(group);
						} else {
							// The association did not validate - prune it out
							if (logger.isWarnEnabled()) {
								TenantChainingUserRegistrySynchronizer.logger
										.warn("Not adding group '"
												+ TenantChainingUserRegistrySynchronizer.this.authorityService
														.getShortName(group)
												+ "' to group '"
												+ TenantChainingUserRegistrySynchronizer.this.authorityService
														.getShortName(parent)
												+ "' as this creates a cyclic relationship");
							}
							j.remove();
						}
						visited.removeLast();
					}
					if (parents.isEmpty()) {
						i.remove();
					}
				}

				// Sort the group associations in parent-first order (root
				// groups first) to minimize reindexing overhead
				Map<String, Set<String>> sortedGroupAssociations = new LinkedHashMap<String, Set<String>>(
						this.groupParentAssocsToCreate.size() * 2);
				Deque<String> visited = new LinkedList<String>();
				for (String authority : this.groupParentAssocsToCreate.keySet()) {
					visitGroupParentAssocs(visited, authority, this.groupParentAssocsToCreate, sortedGroupAssociations);
				}

				this.groupParentAssocsToCreate = sortedGroupAssociations;
			}

			private boolean validateAuthorityChildren(Deque<String> visited, String authority) {
				if (AuthorityType.getAuthorityType(authority) == AuthorityType.USER) {
					return true;
				}
				if (visited.contains(authority)) {
					return false;
				}
				visited.add(authority);
				try {
					Set<String> children = this.finalGroupChildAssocs.get(authority);
					if (children != null) {
						for (String child : children) {
							if (!validateAuthorityChildren(visited, child)) {
								return false;
							}
						}
					}
					return true;
				} finally {
					visited.removeLast();
				}
			}

			/**
			 * Visits the given authority by recursively visiting its parents in
			 * associationsOld and then adding the authority to associationsNew.
			 * Used to sort associationsOld into 'parent-first' order to
			 * minimize reindexing overhead.
			 * 
			 * @param visited
			 *            The ancestors that form the path to the authority to
			 *            visit. Allows detection of cyclic child associations.
			 * @param authority
			 *            the authority to visit
			 * @param associationsOld
			 *            the association map to sort
			 * @param associationsNew
			 *            the association map to add to in parent-first order
			 */
			private boolean visitGroupParentAssocs(Deque<String> visited, String authority,
					Map<String, Set<String>> associationsOld, Map<String, Set<String>> associationsNew) {
				if (visited.contains(authority)) {
					// Prevent cyclic paths (Shouldn't happen as we've already
					// validated)
					return false;
				}
				visited.add(authority);
				try {
					if (!associationsNew.containsKey(authority)) {
						Set<String> oldParents = associationsOld.get(authority);
						if (oldParents != null) {
							Set<String> newParents = new TreeSet<String>();

							for (String parent : oldParents) {
								if (visitGroupParentAssocs(visited, parent, associationsOld, associationsNew)) {
									newParents.add(parent);
								}
							}
							associationsNew.put(authority, newParents);
						}
					}
					return true;
				} finally {
					visited.removeLast();
				}
			}

			private Set<String> newPersonSet() {
				return TenantChainingUserRegistrySynchronizer.this.personService.getUserNamesAreCaseSensitive()
						? new TreeSet<String>() : new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			}

			private Map<String, Set<String>> newPersonMap() {
				return TenantChainingUserRegistrySynchronizer.this.personService.getUserNamesAreCaseSensitive()
						? new TreeMap<String, Set<String>>()
						: new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);
			}

			private void logRetainParentAssociations(Map<String, Set<String>> parentAssocs, Set<String> toRetain) {
				if (toRetain.isEmpty()) {
					parentAssocs.clear();
					return;
				}
				Iterator<Map.Entry<String, Set<String>>> i = parentAssocs.entrySet().iterator();
				StringBuilder groupList = null;
				while (i.hasNext()) {
					Map.Entry<String, Set<String>> entry = i.next();
					String child = entry.getKey();
					if (!toRetain.contains(child)) {
						if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
							if (groupList == null) {
								groupList = new StringBuilder(1024);
							} else {
								groupList.setLength(0);
							}
							for (String parent : entry.getValue()) {
								if (groupList.length() > 0) {
									groupList.append(", ");
								}
								groupList.append('\'')
										.append(TenantChainingUserRegistrySynchronizer.this.authorityService
												.getShortName(parent))
										.append('\'');

							}
							TenantChainingUserRegistrySynchronizer.logger
									.debug("Ignoring non-existent member '"
											+ TenantChainingUserRegistrySynchronizer.this.authorityService
													.getShortName(child)
											+ "' in groups {" + groupList.toString() + "}. RunAs user:"
											+ AuthenticationUtil.getRunAsUser());
						}
						i.remove();
					}
				}
			}

			public void processGroups(UserRegistry userRegistry, boolean isFullSync, boolean splitTxns) {
				// If we got back some groups, we have to cross reference them
				// with the set of known authorities
				if (isFullSync || !this.groupParentAssocsToDelete.isEmpty()
						|| !this.groupParentAssocsToDelete.isEmpty()) {
					processGroupSynchInTenantMode(userRegistry, isFullSync, splitTxns);
				}
			}

			private void processGroupSynchInTenantMode(UserRegistry userRegistry, boolean isFullSync,
					boolean splitTxns) {
				final Set<String> allZonePersons = newPersonSet();
				final Set<String> allZoneGroups = new TreeSet<String>();
				final String tenantId = SEIPTenantIntegration.getTenantId();
				// Add in current set of known authorities
				TenantChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper()
						.doInTransaction(new RetryingTransactionCallback<Void>() {
							public Void execute() throws Throwable {
								allZonePersons.addAll(TenantChainingUserRegistrySynchronizer.this.authorityService
										.getAllAuthoritiesInZone(zoneId, AuthorityType.USER));
								allZoneGroups.addAll(TenantChainingUserRegistrySynchronizer.this.authorityService
										.getAllAuthoritiesInZone(zoneId, AuthorityType.GROUP));
								return null;
							}
						}, true, splitTxns);

				allZoneGroups.addAll(this.groupsToCreate.keySet());

				// Prune our set of authorities according to deletions
				if (isFullSync) {
					final Set<String> personDeletionCandidates = newPersonSet();
					personDeletionCandidates.addAll(allZonePersons);

					final Set<String> groupDeletionCandidates = new TreeSet<String>();
					groupDeletionCandidates.addAll(allZoneGroups);

					this.deletionCandidates = new TreeSet<String>();

					for (String person : userRegistry.getPersonNames()) {
						personDeletionCandidates.remove(person);
					}

					for (String group : userRegistry.getGroupNames()) {
						groupDeletionCandidates.remove(group);
					}

					this.deletionCandidates = new TreeSet<String>();
					this.deletionCandidates.addAll(personDeletionCandidates);
					this.deletionCandidates.addAll(groupDeletionCandidates);
					if (allowDeletions) {
						allZonePersons.removeAll(personDeletionCandidates);
						allZoneGroups.removeAll(groupDeletionCandidates);
					} else {
						if (!personDeletionCandidates.isEmpty()) {
							TenantChainingUserRegistrySynchronizer.logger.warn(
									"The following missing users are not being deleted as allowDeletions == false");
							for (String person : personDeletionCandidates) {
								TenantChainingUserRegistrySynchronizer.logger.warn("    " + person);
							}
						}
						if (!groupDeletionCandidates.isEmpty()) {
							TenantChainingUserRegistrySynchronizer.logger.warn(
									"The following missing groups are not being deleted as allowDeletions == false");
							for (String group : groupDeletionCandidates) {
								TenantChainingUserRegistrySynchronizer.logger.warn("    " + group);
							}
						}

						// Complete association deletion information by
						// scanning deleted groups
						BatchProcessor<String> groupScanner = new BatchProcessor<String>(
								zone + " Missing Authority Scanning",
								TenantChainingUserRegistrySynchronizer.this.transactionService
										.getRetryingTransactionHelper(),
								this.deletionCandidates, TenantChainingUserRegistrySynchronizer.this.workerThreads, 20,
								TenantChainingUserRegistrySynchronizer.this.applicationEventPublisher,
								TenantChainingUserRegistrySynchronizer.logger,
								TenantChainingUserRegistrySynchronizer.this.loggingInterval);
						groupScanner.process(new BaseBatchProcessWorker<String>() {

							@Override
							public String getIdentifier(String entry) {
								return entry;
							}

							@Override
							public void process(final String authority) throws Throwable {

								AuthenticationUtil.runAs(new RunAsWork<Void>() {

									@Override
									public Void doWork() throws Exception {
										proceesInTenantMode(zoneId, authority);
										return null;
									}
								}, SEIPTenantIntegration.getSystemUserByTenantId(tenantId));

							}

							private void proceesInTenantMode(final String zoneId, String authority) {
								// Disassociate it from this zone, allowing
								// it to be reclaimed by something further
								// down the chain
								TenantChainingUserRegistrySynchronizer.this.authorityService
										.removeAuthorityFromZones(authority, Collections.singleton(zoneId));

								// For groups, remove all members
								if (AuthorityType.getAuthorityType(authority) != AuthorityType.USER) {
									String groupShortName = TenantChainingUserRegistrySynchronizer.this.authorityService
											.getShortName(authority);
									String groupDisplayName = TenantChainingUserRegistrySynchronizer.this.authorityService
											.getAuthorityDisplayName(authority);
									NodeDescription dummy = new NodeDescription(groupShortName + " (Deleted)");
									PropertyMap dummyProperties = dummy.getProperties();
									dummyProperties.put(ContentModel.PROP_AUTHORITY_NAME, authority);
									if (groupDisplayName != null) {
										dummyProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, groupDisplayName);
									}
									updateGroup(dummy, true);
								}
							}
						}, splitTxns);

					}
				}

				// Prune the group associations now that we have complete
				// information
				this.groupParentAssocsToCreate.keySet().retainAll(allZoneGroups);
				logRetainParentAssociations(this.groupParentAssocsToCreate, allZoneGroups);
				this.finalGroupChildAssocs.keySet().retainAll(allZoneGroups);

				// Pruning person associations will have to wait until we
				// have passed over all persons and built up
				// this set
				this.allZonePersons = allZonePersons;

				if (!this.groupParentAssocsToDelete.isEmpty()) {
					// Create/update the groups and delete parent
					// associations to be deleted
					BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
							zone + " Group Creation and Association Deletion",
							TenantChainingUserRegistrySynchronizer.this.transactionService
									.getRetryingTransactionHelper(),
							this.groupParentAssocsToDelete.entrySet(),
							TenantChainingUserRegistrySynchronizer.this.workerThreads, 20,
							TenantChainingUserRegistrySynchronizer.this.applicationEventPublisher,
							TenantChainingUserRegistrySynchronizer.logger,
							TenantChainingUserRegistrySynchronizer.this.loggingInterval);
					groupCreator.process(new BaseBatchProcessWorker<Map.Entry<String, Set<String>>>() {
						public String getIdentifier(Map.Entry<String, Set<String>> entry) {
							return entry.getKey() + " " + entry.getValue();
						}

						public void process(final Map.Entry<String, Set<String>> entry) throws Throwable {

							AuthenticationUtil.runAs(new RunAsWork<Void>() {

								@Override
								public Void doWork() throws Exception {
									processInternal(zoneSet, entry.getKey());
									return null;
								}
							}, SEIPTenantIntegration.getSystemUserByTenantId(tenantId));

						}

						private void processInternal(final Set<String> zoneSet, String child) {
							String groupDisplayName = Analyzer.this.groupsToCreate.get(child);
							if (groupDisplayName != null) {
								String groupShortName = TenantChainingUserRegistrySynchronizer.this.authorityService
										.getShortName(child);
								if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
									TenantChainingUserRegistrySynchronizer.logger
											.debug("Creating group '" + groupShortName + "'");
								}
								// create the group
								TenantChainingUserRegistrySynchronizer.this.authorityService.createAuthority(
										AuthorityType.getAuthorityType(child), groupShortName, groupDisplayName,
										zoneSet);
							} else {
								// Maintain association deletions now. The
								// creations will have to be done later once
								// we have performed all the deletions in
								// order to avoid creating cycles
								maintainAssociationDeletions(child);
							}
						}
					}, splitTxns);
				}
			}

			public void finalizeAssociations(UserRegistry userRegistry, boolean splitTxns) {
				// First validate the group associations to be created for
				// potential cycles. Remove any offending association
				validateGroupParentAssocsToCreate();

				// Now go ahead and create the group associations
				if (!this.groupParentAssocsToCreate.isEmpty()) {
					BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
							zone + " Group Association Creation",
							TenantChainingUserRegistrySynchronizer.this.transactionService
									.getRetryingTransactionHelper(),
							this.groupParentAssocsToCreate.entrySet(),
							TenantChainingUserRegistrySynchronizer.this.workerThreads, 20,
							TenantChainingUserRegistrySynchronizer.this.applicationEventPublisher,
							TenantChainingUserRegistrySynchronizer.logger,
							TenantChainingUserRegistrySynchronizer.this.loggingInterval);
					groupCreator.process(new BaseBatchProcessWorker<Map.Entry<String, Set<String>>>() {
						public String getIdentifier(Map.Entry<String, Set<String>> entry) {
							return entry.getKey() + " " + entry.getValue();
						}

						public void process(Map.Entry<String, Set<String>> entry) throws Throwable {

							final String user = entry.getKey();
							AuthenticationUtil.runAs(new RunAsWork<Void>() {

								@Override
								public Void doWork() throws Exception {
									maintainAssociationCreations(user);
									return null;
								}
							}, SEIPTenantIntegration.getSystemUser(user));
						}
					}, splitTxns);
				}

				// Remove all the associations we have already dealt with
				this.personParentAssocsToDelete.keySet().removeAll(this.personsProcessed);

				// Filter out associations to authorities that simply can't
				// exist (and log if debugging is enabled)
				logRetainParentAssociations(this.personParentAssocsToCreate, this.allZonePersons);

				// Update associations to persons not updated themselves
				if (!this.personParentAssocsToDelete.isEmpty()) {
					BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
							zone + " Person Association",
							TenantChainingUserRegistrySynchronizer.this.transactionService
									.getRetryingTransactionHelper(),
							this.personParentAssocsToDelete.entrySet(),
							TenantChainingUserRegistrySynchronizer.this.workerThreads, 20,
							TenantChainingUserRegistrySynchronizer.this.applicationEventPublisher,
							TenantChainingUserRegistrySynchronizer.logger,
							TenantChainingUserRegistrySynchronizer.this.loggingInterval);
					groupCreator.process(new BaseBatchProcessWorker<Map.Entry<String, Set<String>>>() {
						public String getIdentifier(Map.Entry<String, Set<String>> entry) {
							return entry.getKey() + " " + entry.getValue();
						}

						public void process(final Map.Entry<String, Set<String>> entry) throws Throwable {
							final String user = entry.getKey();
							AuthenticationUtil.runAs(new RunAsWork<Void>() {

								@Override
								public Void doWork() throws Exception {
									maintainAssociationDeletions(user);
									maintainAssociationCreations(user);
									return null;
								}
							}, SEIPTenantIntegration.getSystemUser(user));

						}
					}, splitTxns);
				}
			}

			private void maintainAssociationDeletions(String authorityName) {
				boolean isPerson = AuthorityType.getAuthorityType(authorityName) == AuthorityType.USER;
				Set<String> parentsToDelete = isPerson ? this.personParentAssocsToDelete.get(authorityName)
						: this.groupParentAssocsToDelete.get(authorityName);
				if (parentsToDelete != null && !parentsToDelete.isEmpty()) {
					for (String parent : parentsToDelete) {
						if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.debug("Removing '"
									+ TenantChainingUserRegistrySynchronizer.this.authorityService
											.getShortName(authorityName)
									+ "' from group '"
									+ TenantChainingUserRegistrySynchronizer.this.authorityService.getShortName(parent)
									+ "'");
						}
						TenantChainingUserRegistrySynchronizer.this.authorityService.removeAuthority(parent,
								authorityName);
					}
				}

			}

			private void maintainAssociationCreations(String authorityName) {
				boolean isPerson = AuthorityType.getAuthorityType(authorityName) == AuthorityType.USER;
				Set<String> parents = isPerson ? this.personParentAssocsToCreate.get(authorityName)
						: this.groupParentAssocsToCreate.get(authorityName);
				if (parents != null && !parents.isEmpty()) {
					if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
						for (String groupName : parents) {
							TenantChainingUserRegistrySynchronizer.logger.debug("Adding '"
									+ TenantChainingUserRegistrySynchronizer.this.authorityService
											.getShortName(authorityName)
									+ "' to group '" + TenantChainingUserRegistrySynchronizer.this.authorityService
											.getShortName(groupName)
									+ "'");
						}
					}
					try {
						TenantChainingUserRegistrySynchronizer.this.authorityService.addAuthority(parents,
								authorityName);
					} catch (UnknownAuthorityException e) {
						// Let's force a transaction retry if a parent doesn't
						// exist. It may be because we are
						// waiting for another worker thread to create it
						throw new ConcurrencyFailureException("Forcing batch retry for unknown authority", e);
					} catch (InvalidNodeRefException e) {
						// Another thread may have written the node, but it is
						// not visible to this transaction
						// See: ALF-5471: 'authorityMigration' patch can report
						// 'Node does not exist'
						throw new ConcurrencyFailureException("Forcing batch retry for invalid node", e);
					}
				}
				// Remember that this person's associations have been maintained
				if (isPerson) {
					synchronized (this) {
						this.personsProcessed.add(authorityName);
					}
				}
			}
		}

		final Analyzer groupAnalyzer = new Analyzer(lastModifiedMillis);
		int groupProcessedCount = groupProcessor.process(groupAnalyzer, splitTxns);

		groupAnalyzer.processGroups(userRegistry, isFullSync, splitTxns);

		// Process persons and their parent associations

		lastModifiedMillis = forceUpdate ? -1
				: getMostRecentUpdateTime(TenantChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId,
						splitTxns);
		lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);
		if (TenantChainingUserRegistrySynchronizer.logger.isInfoEnabled()) {
			if (lastModified == null) {
				TenantChainingUserRegistrySynchronizer.logger
						.info("Retrieving all users from user registry '" + zone + "'");
			} else {
				TenantChainingUserRegistrySynchronizer.logger.info("Retrieving users changed since "
						+ DateFormat.getDateTimeInstance().format(lastModified) + " from user registry '" + zone + "'");
			}
		}
		final BatchProcessor<NodeDescription> personProcessor = new BatchProcessor<NodeDescription>(
				zone + " User Creation and Association", this.transactionService.getRetryingTransactionHelper(),
				userRegistry.getPersons(lastModified), this.workerThreads, 10, this.applicationEventPublisher,
				TenantChainingUserRegistrySynchronizer.logger, this.loggingInterval);
		class PersonWorker extends BaseBatchProcessWorker<NodeDescription> {
			private long latestTime;

			public PersonWorker(final long latestTime) {
				this.latestTime = latestTime;
			}

			public long getLatestTime() {
				return this.latestTime;
			}

			public String getIdentifier(NodeDescription entry) {
				return entry.getSourceId();
			}

			public void process(final NodeDescription person) throws Throwable {
				// Make a mutable copy of the person properties, since they get
				// written back to by person service
				HashMap<QName, Serializable> personProperties = new HashMap<QName, Serializable>(
						person.getProperties());
				String personName = (String) personProperties.get(ContentModel.PROP_USERNAME);
				String ou = (String) personProperties.get(ContentModel.PROP_ORGANIZATION);
				if (SEIPTenantIntegration.isValidTenant(ou) && !personName.endsWith(ou)) {
					personName += ("@" + ou);
					person.getProperties().put(ContentModel.PROP_USERNAME, personName);
				}
				final String personFullName = personName;
				logger.debug("Check user: " + personFullName);
				String tenantDomain = SEIPTenantIntegration.getTenantId(personFullName);
				if (tenantDomain != null && !tenantDomain.isEmpty()) {
					if (!isTenantEnabled(tenantDomain)) {
						logger.debug("Tenant is missing/disabled for user: " + personFullName);
						return;
					}
					logger.debug("Process user: " + personFullName);
					AuthenticationUtil.runAs(new RunAsWork<Void>() {
						@Override
						public Void doWork() throws Exception {
							processInTenantMode(personFullName, person);
							return null;
						}
					}, SEIPTenantIntegration.getSystemUserByTenantId(tenantDomain));
				} else {
					logger.debug("Process user: " + personFullName);
					processInTenantMode(personFullName, person);
				}
			}

			private void processInTenantMode(String personName, NodeDescription person) {
				HashMap<QName, Serializable> personProperties = person.getProperties();
				// Make a mutable copy of the person properties, since they get
				// written back to by person service

				Set<String> zones = TenantChainingUserRegistrySynchronizer.this.authorityService
						.getAuthorityZones(personName);
				if (zones == null) {
					// The person did not exist at all
					if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
						TenantChainingUserRegistrySynchronizer.logger.debug("Creating user '" + personName + "'");
					}

					TenantChainingUserRegistrySynchronizer.this.personService.createPerson(personProperties, zoneSet);
				} else if (zones.contains(zoneId)) {
					// The person already existed in this zone: update the
					// person
					if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
						TenantChainingUserRegistrySynchronizer.logger.debug("Updating user '" + personName + "'");
					}

					TenantChainingUserRegistrySynchronizer.this.personService.setPersonProperties(personName,
							personProperties, false);
				} else {
					// Check whether the user is in any of the authentication
					// chain zones
					Set<String> intersection = new TreeSet<String>(zones);
					intersection.retainAll(allZoneIds);
					// Check whether the user is in any of the higher priority
					// authentication chain zones
					Set<String> visited = new TreeSet<String>(intersection);
					visited.retainAll(visitedZoneIds);
					if (visited.size() > 0) {
						// A person that exists in a different zone with higher
						// precedence - ignore
						return;
					}

					else if (!allowDeletions || intersection.isEmpty()) {
						// The person exists, but in a different zone. Either
						// deletions are disallowed or the zone is
						// not in the authentication chain. May be due to
						// upgrade or zone changes. Let's re-zone them
						if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.warn("Updating user '" + personName
									+ "'. This user will in future be assumed to originate from user registry '" + zone
									+ "'.");
						}
						updateAuthorityZones(personName, zones, zoneSet);
						TenantChainingUserRegistrySynchronizer.this.personService.setPersonProperties(personName,
								personProperties, false);
					} else {
						// The person existed, but in a zone with lower
						// precedence
						if (TenantChainingUserRegistrySynchronizer.logger.isWarnEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.warn("Recreating occluded user '" + personName
									+ "'. This user was previously created through synchronization with a lower priority user registry.");
						}
						TenantChainingUserRegistrySynchronizer.this.personService.deletePerson(personName);
						TenantChainingUserRegistrySynchronizer.this.personService.createPerson(personProperties,
								zoneSet);
					}
				}

				// Maintain association deletions and creations in one shot
				// (safe to do this with persons as we can't
				// create cycles)
				groupAnalyzer.maintainAssociationDeletions(personName);
				groupAnalyzer.maintainAssociationCreations(personName);

				synchronized (this) {
					// Maintain the last modified date
					Date personLastModified = person.getLastModified();
					if (personLastModified != null) {
						this.latestTime = Math.max(this.latestTime, personLastModified.getTime());
					}
				}
			}
		}

		PersonWorker persons = new PersonWorker(lastModifiedMillis);
		int personProcessedCount = personProcessor.process(persons, splitTxns);

		// Process those associations to persons who themselves have not been
		// updated
		groupAnalyzer.finalizeAssociations(userRegistry, splitTxns);

		// Only now that the whole tree has been processed is it safe to persist
		// the last modified dates
		long latestTime = groupAnalyzer.getLatestTime();
		if (latestTime != -1) {
			setMostRecentUpdateTime(TenantChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId,
					latestTime, splitTxns);
		}
		latestTime = persons.getLatestTime();
		if (latestTime != -1) {
			setMostRecentUpdateTime(TenantChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId,
					latestTime, splitTxns);
		}

		// Delete authorities if we have complete information for the zone
		Set<String> deletionCandidates = groupAnalyzer.getDeletionCandidates();
		if (isFullSync && allowDeletions && !deletionCandidates.isEmpty()) {
			BatchProcessor<String> authorityDeletionProcessor = new BatchProcessor<String>(zone + " Authority Deletion",
					this.transactionService.getRetryingTransactionHelper(), deletionCandidates, this.workerThreads, 10,
					this.applicationEventPublisher, TenantChainingUserRegistrySynchronizer.logger,
					this.loggingInterval);
			class AuthorityDeleter extends BaseBatchProcessWorker<String> {
				private int personProcessedCount;
				private int groupProcessedCount;

				public int getPersonProcessedCount() {
					return this.personProcessedCount;
				}

				public int getGroupProcessedCount() {
					return this.groupProcessedCount;
				}

				public String getIdentifier(String entry) {
					return entry;
				}

				public void process(String authority) throws Throwable {
					if (AuthorityType.getAuthorityType(authority) == AuthorityType.USER) {
						if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.debug("Deleting user '" + authority + "'");
						}
						TenantChainingUserRegistrySynchronizer.this.personService.deletePerson(authority);
						synchronized (this) {
							this.personProcessedCount++;
						}
					} else {
						if (TenantChainingUserRegistrySynchronizer.logger.isDebugEnabled()) {
							TenantChainingUserRegistrySynchronizer.logger.debug(
									"Deleting group '" + TenantChainingUserRegistrySynchronizer.this.authorityService
											.getShortName(authority) + "'");
						}
						TenantChainingUserRegistrySynchronizer.this.authorityService.deleteAuthority(authority);
						synchronized (this) {
							this.groupProcessedCount++;
						}
					}
				}
			}
			AuthorityDeleter authorityDeleter = new AuthorityDeleter();
			authorityDeletionProcessor.process(authorityDeleter, splitTxns);
			groupProcessedCount += authorityDeleter.getGroupProcessedCount();
			personProcessedCount += authorityDeleter.getPersonProcessedCount();
		}

		// Remember we have visited this zone
		visitedZoneIds.add(zoneId);

		if (TenantChainingUserRegistrySynchronizer.logger.isInfoEnabled()) {
			TenantChainingUserRegistrySynchronizer.logger
					.info("Finished synchronizing users and groups with user registry '" + zone + "'");
			TenantChainingUserRegistrySynchronizer.logger
					.info(personProcessedCount + " user(s) and " + groupProcessedCount + " group(s) processed");
		}
	}

	private boolean isTenantEnabled(String tenantDomain) {
		TenantEntity tenant = null;
		return (tenant = tenantAdminDAO.getTenant(tenantDomain)) != null && tenant.getEnabled().booleanValue();
	}

	/**
	 * Gets the persisted most recent update time for a label and zone.
	 * 
	 * @param label
	 *            the label
	 * @param zoneId
	 *            the zone id
	 * @return the most recent update time in milliseconds
	 */
	private long getMostRecentUpdateTime(final String label, final String zoneId, boolean splitTxns) {
		return this.transactionService.getRetryingTransactionHelper()
				.doInTransaction(new RetryingTransactionCallback<Long>() {
					public Long execute() throws Throwable {
						Long updateTime = (Long) TenantChainingUserRegistrySynchronizer.this.attributeService
								.getAttribute(TenantChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, label,
										zoneId);
						return updateTime == null ? -1 : updateTime;
					}
				}, true, splitTxns);
	}

	/**
	 * Persists the most recent update time for a label and zone.
	 * 
	 * @param label
	 *            the label
	 * @param zoneId
	 *            the zone id
	 * @param lastModifiedMillis
	 *            the update time in milliseconds
	 * @param splitTxns
	 *            Can the modifications to Alfresco be split across multiple
	 *            transactions for maximum performance? If <code>true</code>,
	 *            the attribute is persisted in a new transaction for increased
	 *            performance and reliability.
	 */
	private void setMostRecentUpdateTime(final String label, final String zoneId, final long lastModifiedMillis,
			boolean splitTxns) {
		this.transactionService.getRetryingTransactionHelper()
				.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
					public Object execute() throws Throwable {
						TenantChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
								Long.valueOf(lastModifiedMillis),
								TenantChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, label, zoneId);
						return null;
					}
				}, false, splitTxns);
	}

	/**
	 * Gets the default set of zones to set on a person or group belonging to
	 * the user registry with the given zone ID. We add the default zone as well
	 * as the zone corresponding to the user registry so that the users and
	 * groups are visible in the UI.
	 * 
	 * @param zoneId
	 *            the zone id
	 * @return the zone set
	 */
	private Set<String> getZones(final String zoneId) {
		Set<String> zones = new HashSet<String>(5);
		zones.add(AuthorityService.ZONE_APP_DEFAULT);
		zones.add(zoneId);
		return zones;
	}

	/**
	 * Modifies an authority's zone set from oldZones to newZones in the most
	 * efficient manner (avoiding unnecessary reindexing cost).
	 * 
	 * @param authorityName
	 * @param oldZones
	 * @param newZones
	 */
	private void updateAuthorityZones(String authorityName, Set<String> oldZones, final Set<String> newZones) {
		Set<String> zonesToRemove = new HashSet<String>(oldZones);
		zonesToRemove.removeAll(newZones);
		// Let's keep the authority in the alfresco auth zone if it was already
		// there. Otherwise we may have to
		// regenerate all paths to this authority from site groups, which could
		// be very expensive!
		zonesToRemove.remove(AuthorityService.ZONE_AUTH_ALFRESCO);
		if (!zonesToRemove.isEmpty()) {
			this.authorityService.removeAuthorityFromZones(authorityName, zonesToRemove);
		}
		Set<String> zonesToAdd = new HashSet<String>(newZones);
		zonesToAdd.removeAll(oldZones);
		if (!zonesToAdd.isEmpty()) {
			this.authorityService.addAuthorityToZones(authorityName, zonesToAdd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.extensions.surf.util.AbstractLifecycleBean#
	 * onBootstrap(org.springframework.context. ApplicationEvent)
	 */
	@Override
	protected void onBootstrap(ApplicationEvent event) {
		// Do an initial differential sync on startup, using transaction
		// splitting. This ensures that on the very
		// first startup, we don't have to wait for a very long login operation
		// to trigger the first sync!

		synchronize(false, false, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.extensions.surf.util.AbstractLifecycleBean#
	 * onShutdown(org.springframework.context. ApplicationEvent)
	 */
	@Override
	protected void onShutdown(ApplicationEvent event) {
	}

	protected abstract class BaseBatchProcessWorker<T> implements BatchProcessWorker<T> {
		public final void beforeProcess() throws Throwable {
			// Disable rules
			TenantChainingUserRegistrySynchronizer.this.ruleService.disableRules();
			// Authentication
			AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
		}

		public final void afterProcess() throws Throwable {
			// Enable rules
			TenantChainingUserRegistrySynchronizer.this.ruleService.enableRules();
			// Clear authentication
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}
}
