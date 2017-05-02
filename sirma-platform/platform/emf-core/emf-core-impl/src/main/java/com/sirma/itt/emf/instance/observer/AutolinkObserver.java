package com.sirma.itt.emf.instance.observer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.resources.Resource;

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

	private static final Set<String> UNSUPPORTED_OPERATIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(ActionTypeConstants.ADD_LIBRARY)));

	/**
	 * Listens for after instance persist events to link the parent to child object links.
	 *
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	public <I extends Instance> void onAfterInstanceCreated(@Observes AfterInstancePersistEvent<I, ?> event) {
		if (Options.DISABLE_AUTOMATIC_PARENT_CHILD_LINKS.isEnabled()) {
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
	private static InstanceReference getParentReference(Instance instance) {
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
			linkService.link(parent, child, LinkConstants.PARENT_TO_CHILD, LinkConstants.CHILD_TO_PARENT,
					LinkConstants.getDefaultSystemProperties());
		}
		// create parent to child links for tree
		linkService.linkSimple(parent, child, LinkConstants.TREE_PARENT_TO_CHILD, LinkConstants.TREE_CHILD_TO_PARENT);
	}

	/**
	 * When instance is attached to other then both should be linked together.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceAttachedEvent(@Observes InstanceAttachedEvent<? extends Instance> event) {
		// If operation is add library we do not need a link for the tree, so no need to add
		// parent-child relation.
		final String operationId = event.getOperationId();
		if (event.getInstance() instanceof Resource || UNSUPPORTED_OPERATIONS.contains(operationId)) {
			return;
		}
		InstanceReference parentRef = event.getInstance().toReference();
		InstanceReference childRef = event.getChild().toReference();

		// create parent to child links for tree
		linkService.linkSimple(parentRef, childRef, LinkConstants.TREE_PARENT_TO_CHILD,
				LinkConstants.TREE_CHILD_TO_PARENT);
	}

	/**
	 * When instance is detached from other then both should not be linked together.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceDetachedEvent(@Observes InstanceDetachedEvent<? extends Instance> event) {
		InstanceReference parent = event.getInstance().toReference();
		InstanceReference child = event.getChild().toReference();

		linkService.unlinkSimple(parent, child, LinkConstants.TREE_PARENT_TO_CHILD, LinkConstants.TREE_CHILD_TO_PARENT);
	}

}
