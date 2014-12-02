package com.sirma.itt.emf.semantic.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.event.resource.ResourcesChangedEvent;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.event.AttachedChildToResourceEvent;
import com.sirma.itt.emf.resources.event.ResourceAddedEvent;
import com.sirma.itt.emf.resources.event.ResourceSynchronizationRequredEvent;
import com.sirma.itt.emf.resources.event.ResourceUpdatedEvent;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Observer that listens for new resources or modifications to update the semantic database.
 *
 * @author BBonev
 */
@Stateless
public class ResourceSynchronizationObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ResourceSynchronizationObserver.class);
	/** The db dao. */
	@Inject
	@SemanticDb
	private DbDao dbDao;

	/** The repository connection. */
	@Inject
	private javax.enterprise.inject.Instance<RepositoryConnection> repositoryConnection;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The registry service. */
	@Inject
	private NamespaceRegistryService registryService;

	/** The lock. */
	private static ReentrantLock lock = new ReentrantLock();

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The context name. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_CONTEXT_NAME, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework")
	private String contextName;
	/** The context. */
	private URI context;

	/**
	 * Listens for newly added resources.
	 *
	 * @param event
	 *            the event
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onNewResource(@Observes ResourceAddedEvent event) {
		dbDao.saveOrUpdate(cleanProperties(event.getInstance()));
	}

	/**
	 * Listens for updated resources.
	 *
	 * @param event
	 *            the event
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onResourceUpdate(@Observes ResourceUpdatedEvent event) {
		dbDao.saveOrUpdate(cleanProperties(event.getInstance()),
				cleanProperties(event.getOldInstance()));
	}

	/**
	 * To synch the user to group.
	 *
	 * @param event
	 *            the event
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onResourceAddedToGroup(@Observes AttachedChildToResourceEvent event) {
		if (event.getInstance() == null || event.getInstance().getId() == null
				|| event.getChild() == null || event.getChild().getId() == null) {
			return;
		}
		URI subject = valueFactory.createURI(registryService.buildFullUri(event.getInstance()
				.getId().toString()));
		URI object = valueFactory.createURI(registryService.buildFullUri(event.getChild().getId()
				.toString()));
		try {
			repositoryConnection.get().add(subject, Proton.HAS_MEMBER, object, getContext());
		} catch (RepositoryException e) {
			LOGGER.warn("Failed to sync group {} to member {} relation ", event.getInstance()
					.getId(), event.getChild().getId(), e);
		}
	}

	/**
	 * Assigns the resources to the provided instances as its members. Uses the
	 * {@link EMF#HAS_ASSIGNEE} predicate
	 *
	 * @param event
	 *            the event with data
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onMembersChanged(@Observes ResourcesChangedEvent event) {
		Instance membersOf = event.getInstance();
		List<Resource> newMembers = new ArrayList<>(event.getNewResources());
		List<Resource> oldMembers = new ArrayList<>(event.getOldResources());
		URI subject = valueFactory.createURI(registryService.buildFullUri(membersOf.getId()
				.toString()));
		try {
			// remove the old first
			oldMembers.removeAll(newMembers);
			Model model = new LinkedHashModel(oldMembers.size());
			for (Resource resource : oldMembers) {
				URI object = valueFactory.createURI(registryService.buildFullUri(resource.getId()
						.toString()));
				Statement addStatement = valueFactory.createStatement(subject, EMF.HAS_ASSIGNEE,
						object, context);
				model.add(addStatement);
			}
			RepositoryConnection connection = repositoryConnection.get();
			if (model.size() > 0) {
				connection.remove(model, getContext());
			}
			// add the new resources
			newMembers.removeAll(event.getOldResources());
			model = new LinkedHashModel(newMembers.size());
			for (Resource resource : newMembers) {
				URI object = valueFactory.createURI(registryService.buildFullUri(resource.getId()
						.toString()));
				Statement addStatement = valueFactory.createStatement(subject, EMF.HAS_ASSIGNEE,
						object, context);
				model.add(addStatement);
			}
			if (model.size() > 0) {
				connection.add(model, getContext());
			}
		} catch (RepositoryException e) {
			LOGGER.warn("Failed to sync members of {}! NEW members {} OLD members {} ", event
					.getInstance().getId(), event.getNewResources(), event.getOldResources(), e);
		}
	}

	/**
	 * Cleans null properties.
	 *
	 * @param instance
	 *            the instance
	 * @return the resource
	 */
	private Resource cleanProperties(Resource instance) {
		if (instance == null) {
			return null;
		}
		return PropertiesUtil.cleanNullProperties(instance);
	}

	/**
	 * Listens for forced resource synchronizations.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	public void onForcedSynchronization(@Observes ResourceSynchronizationRequredEvent event) {
		if (!event.isForced()) {
			return;
		}
		if (lock.isLocked()) {
			return;
		}
		lock.lock();
		try {
			runSynchronization();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Perform the synchronization.
	 */
	private void runSynchronization() {
		List<Resource> resources = resourceService.getAllResources(ResourceType.USER, null);
		synchResourcesList(resources);
		resources = resourceService.getAllResources(ResourceType.GROUP, null);
		synchResourcesList(resources);

	}

	/**
	 * Synch resources to DB.
	 *
	 * @param resources
	 *            the resources
	 */
	private void synchResourcesList(List<Resource> resources) {
		for (Resource resource : resources) {
			dbDao.saveOrUpdate(resource);
		}
	}

	/**
	 * Gets the context URI.
	 *
	 * @return the context URI
	 */
	private URI getContext() {
		if (context == null) {
			context = valueFactory.createURI(contextName);
		}
		return context;
	}

}
