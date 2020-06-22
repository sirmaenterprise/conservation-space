package com.sirma.itt.emf.semantic.resources;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.event.AttachedChildToResourceEvent;
import com.sirma.itt.seip.resources.event.ResourceAddedEvent;
import com.sirma.itt.seip.resources.event.ResourceSynchronizationRequredEvent;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.tasks.Schedule;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Observer that listens for new resources or modifications to update the semantic database.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ResourceSynchronizationObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSynchronizationObserver.class);

	private static final String DESCRIPTION = "dcterms:description";

	@Inject
	@SemanticDb
	private DbDao dbDao;
	@Inject
	private javax.enterprise.inject.Instance<RepositoryConnection> repositoryConnection;
	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private SynchronizationRunner synchronizationRunner;

	/**
	 * Listens for newly added resources.
	 *
	 * @param event the event
	 */
	public void onNewResource(@Observes ResourceAddedEvent event) {
		updateName(event.getInstance());
		setSemanticType(event.getInstance());
	}

	/**
	 * Sets the semantic type.
	 *
	 * @param resource the new semantic type
	 */
	private void setSemanticType(Resource resource) {
		Serializable semanticType = resource.getOrCreateProperties().computeIfAbsent(DefaultProperties.SEMANTIC_TYPE,
				key -> {
					String type = EmfUser.class.getName();
					if (resource.getType() == ResourceType.SYSTEM) {
						type = EmfResource.class.getName();
					} else if (resource.getType() == ResourceType.GROUP) {
						type = EmfGroup.class.getName();
					}
					DataTypeDefinition definition = definitionService.getDataTypeDefinition(type);
					if (definition != null && definition.getFirstUri() != null) {
						return typeConverter.convert(Uri.class, definition.getFirstUri());
					}
					return null;
				});

		// if the semantic type is resolved and we does not have a type filled in the instance resolve it
		if (resource.type() == null && semanticType != null) {
			ClassInstance classInstance = semanticDefinitionService.getClassInstance(semanticType.toString());
			if (classInstance != null) {
				resource.setType(classInstance.type());
			} else {
				LOGGER.warn("Could not resolve valid type for resource {} -> {}", resource.getId(), semanticType);
			}
		}
	}

	/**
	 * To synch the user to group. Note that this event may not be fired anywhere in the platform. It's replaced by the
	 * method {@link #runGroupMembersSynchronization()}
	 *
	 * @param event the event
	 * @deprecated Does not seep this is called as the event is no longer fired for resource instances as is
	 */
	@Deprecated
	public void onResourceAddedToGroup(@Observes AttachedChildToResourceEvent event) {
		if (event.getInstance() == null || event.getInstance().getId() == null || event.getChild() == null
				|| event.getChild().getId() == null) {
			return;
		}
		IRI subject = registryService.buildUri(event.getInstance().getId().toString());
		IRI object = registryService.buildUri(event.getChild().getId().toString());
		try {
			repositoryConnection.get().add(subject, Proton.HAS_MEMBER, object, registryService.getDataGraph());
		} catch (RepositoryException e) {
			LOGGER.warn("Failed to sync group {} to member {} relation ", event.getInstance().getId(),
					event.getChild().getId(), e);
		}
	}

	/**
	 * Listens for forced resource synchronizations.
	 *
	 * @param event the event
	 */
	public void onForcedSynchronization(@Observes ResourceSynchronizationRequredEvent event) {
		runResourceSynchronization();
		runGroupMembersSynchronization();
	}

	/**
	 * Executes synchronization of users and groups members to semantic database
	 */
	@Schedule(identifier = "SEMANTIC_RESOURCE_SYNCHRONIZATION", system = false)
	@ConfigurationPropertyDefinition(name = "semantic.user.sync.schedule", defaultValue = "0 5/15 * ? * *",
			sensitive = true, system = true,
			label = "Cron expression for synchronizyng user and groups to semantic database")
	public void runResourceSynchronization() {
		synchronizationRunner.runSynchronization(SeipToSemanticResourceSynchronizationConfig.NAME);
	}

	/**
	 * Executes synchronization of group members to semantic database
	 */
	@Schedule(identifier = "SEMANTIC_GROUP_MEMBERS_SYNCHRONIZATION", system = false)
	@ConfigurationPropertyDefinition(name = "semantic.groupMembers.sync.schedule", defaultValue = "0 6/15 * ? * *",
			sensitive = true, system = true,
			label = "Cron expression for synchronizyng group members to semantic database")
	public void runGroupMembersSynchronization() {
		synchronizationRunner.runSynchronization(SeipToSemanticGroupMembersSynchronizationConfig.NAME);
	}

	/**
	 * Copies the name of the resource in new property - description. The reason for this - the name of resource to be
	 * searched case insensitively - CMF-16875.
	 */
	private static void updateName(Resource resource) {
		if (resource == null) {
			return;
		}
		resource.add(DESCRIPTION, resource.getName());
	}
}
