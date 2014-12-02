package com.sirma.itt.emf.instance.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Observer that populates links for the parent-child relations.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AutolinkObserver {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/**
	 * Listens for after instance persist events to link the parent to child object links.
	 *
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	public <I extends Instance> void onAfterInstanceCreated(
			@Observes AfterInstancePersistEvent<I, ?> event) {
		if (RuntimeConfiguration.isSet(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS,
				RuntimeConfigurationProperties.DISABLE_AUTOMATIC_PARENT_CHILD_LINKS)) {
			return;
		}
		Instance instance = event.getInstance();

		InstanceReference parentReference = getParentReference(instance);
		if (parentReference != null) {
			createParentToChildLink(parentReference, instance.toReference(), true);
		}
	}

	/**
	 * Fetches the parent reference of the given instances
	 * 
	 * @param instance
	 *            the instance
	 * @return the parent reference
	 */
	private InstanceReference getParentReference(Instance instance) {
		InstanceReference parentReference = null;
		if (instance instanceof OwnedModel) {
			// use the reference with priority because for non synchronized parent instance there is
			// not instance, yet.
			parentReference = ((OwnedModel) instance).getOwningReference();
			// this could be null and to have an owning instance for objects and multi attached
			// documents
			if (parentReference == null) {
				Instance owningInstance = ((OwnedModel) instance).getOwningInstance();
				if (owningInstance != null) {
					parentReference = owningInstance.toReference();
				}
			}
		}
		return parentReference;
	}

	/**
	 * Creates parent to child link that is marked as created by system
	 * 
	 * @param parent
	 *            the parent
	 * @param child
	 *            the instance
	 * @param skipExistsCheck
	 *            if check for existence before creating link
	 */
	private void createParentToChildLink(InstanceReference parent, InstanceReference child, boolean skipExistsCheck) {
		// first check if we already has the relation not to create it again
		if (skipExistsCheck || !linkService.isLinked(parent, child, LinkConstants.PARENT_TO_CHILD)) {
			linkService.link(parent, child, LinkConstants.PARENT_TO_CHILD,
					LinkConstants.CHILD_TO_PARENT, LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
		}
		// create parent to child links for tree
		linkService.linkSimple(parent, child, LinkConstants.TREE_PARENT_TO_CHILD,
				LinkConstants.TREE_CHILD_TO_PARENT);
	}

	/**
	 * When instance is attached to other then both should be linked together.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceAttachedEvent(@Observes InstanceAttachedEvent<Instance> event) {
		if (event.getInstance() instanceof Resource) {
			return;
		}
		createParentToChildLink(event.getInstance().toReference(), event.getChild().toReference(),
				true);

		linkService.linkSimple(event.getChild().toReference(), event.getInstance().toReference(),
				LinkConstants.PART_OF_URI);
	}

	/**
	 * When instance is detached from other then both should not be linked together.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceDetachedEvent(@Observes InstanceDetachedEvent<Instance> event) {
		InstanceReference parent = event.getInstance().toReference();
		InstanceReference child = event.getChild().toReference();
		linkService.unlink(parent, child, LinkConstants.PARENT_TO_CHILD,
				LinkConstants.CHILD_TO_PARENT);

		linkService.unlinkSimple(child, parent, LinkConstants.PART_OF_URI);

		linkService.unlinkSimple(parent, child, LinkConstants.TREE_PARENT_TO_CHILD,
				LinkConstants.TREE_CHILD_TO_PARENT);
	}

	/**
	 * Method that removes all links for instance upon instance deletion.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceDeleted(@Observes AfterInstanceDeleteEvent<Instance, ?> event) {
		linkService.removeLinksFor(event.getInstance().toReference());
	}

}
