package com.sirma.cmf.web.entity.dispatcher;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.SerializedDocumentContext;
import com.sirma.cmf.web.navigation.EmfNavigationSession;
import com.sirma.cmf.web.navigation.NavigationPoint;
import com.sirma.cmf.web.navigation.SessionHandlerProviderFactory;
import com.sirma.itt.emf.instance.InstanceContextInitializer;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.LinkIterable;

/**
 * Responsible for restoring of context hierarchy of a given instance.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class InstanceContextInitializerImpl extends Action implements InstanceContextInitializer {

	@Inject
	private LinkService linkService;

	@Override
	public void restoreHierarchy(Instance selectedInstance, Instance context) {
		TimeTracker timeTracker = TimeTracker.createAndStart();
		Instance contextInstance = null;
		if (context == null) {
			contextInstance = findContextInstance(selectedInstance);
		} else {
			contextInstance = context;
		}
		// no current instance
		if (contextInstance == null) {
			// Check selected instance parents
			Instance owningInstance = ((OwnedModel) selectedInstance).getOwningInstance();
			if ((owningInstance == null) || !(owningInstance instanceof InstanceContext)) {
				Collection<InstanceReference> immediateParents = getImmediateParents(selectedInstance
						.toReference());
				// If selected instance has no parents, then open the instance with no context, or
				// If selected instance has more than one parent, then open the instance with no
				// context
				if (immediateParents.isEmpty() || (immediateParents.size() > 1)) {
					return;
				}
				// If selected instance has a single parent, then open the instance in context of
				// that parent
				log.debug("Open [" + selectedInstance.getClass().getSimpleName()
						+ "] in its only context");
				List<InstanceReference> parentPath = new LinkedList<>();
				walkHierarchy(immediateParents.iterator().next().getIdentifier(), parentPath,
						immediateParents);
				// restore context using parentPath.
				linkParents((OwnedModel) selectedInstance, parentPath);
			}
			// If selected instance has a single parent, then open the instance in context of
			// that parent
			else {
				log.debug("Open [" + selectedInstance.getClass().getSimpleName()
						+ "] in its only [" + owningInstance.getClass().getSimpleName()
						+ "] context");
			}
		}
		// If there is a current context instance, then:
		else {
			Collection<InstanceReference> immediateParents = getImmediateParents(selectedInstance
					.toReference());
			// If no parents exists for the selected instance, then open it with no context
			if (immediateParents.isEmpty()) {
				log.debug("Open [" + selectedInstance.getClass().getSimpleName()
						+ "] with no context");
				return;
			}
			// Else recursively walk up the parents hierarchy to find if the current context
			// instance is in the selected instance hierarchy. During traversing, we should
			// keep track of the found parent path (a list with parent path hierarchy) in
			// order to properly restore the selected instance hierarchy.
			List<InstanceReference> parentPath = new LinkedList<>();
			Serializable id = contextInstance.getId();
			Instance actualContextInstance = InstanceUtil.getContext(contextInstance, true);
			if (actualContextInstance != null) {
				id = actualContextInstance.getId();
			}
			walkHierarchy(id, parentPath, immediateParents);
			// restore context using parentPath.
			linkParents((OwnedModel) selectedInstance, parentPath);
		}
		double stopInSeconds = timeTracker.stopInSeconds();
		log.debug("Restored [" + selectedInstance.getClass().getSimpleName() + "] hierarchy for ["
				+ ((contextInstance == null) ? null : contextInstance.getClass().getSimpleName())
				+ "] context took " + stopInSeconds + " s");
	}

	/**
	 * Find context instance.
	 *
	 * @param selectedInstance
	 *            the selected instance
	 * @return the instance reference
	 */
	private Instance findContextInstance(Instance selectedInstance) {
		EmfNavigationSession emfSessionHandler = SessionHandlerProviderFactory
				.getSessionHandlerProvider().getEmfSessionHandler();
		NavigationPoint navigationPoint = emfSessionHandler.getTrailPoint();
		Instance currentInstance = getContextFromNavigationPoint(navigationPoint);
		// 1. Clicking again on a link that opens same instance as current opened, should force
		// loading one navigation point before the last one in order to restore proper context. For
		// example: open case -> open object from case -> click on object header link -> should
		// properly load object within the same context.
		// 2. If given instance is opened trough a search page for example where the document
		// context is empty and we cannot restore current instance from navigation point
		if ((currentInstance != null) && selectedInstance.getId().equals(currentInstance.getId())) {
			Instance actualContextInstance = InstanceUtil.getContext(selectedInstance, true);
			if (actualContextInstance != null) {
				currentInstance = actualContextInstance;
			}
			// navigationPoint = emfSessionHandler.getOverTrailPoint();
			// currentInstanceReference = getContextFromNavigationPoint(navigationPoint);
		}
		return currentInstance;
	}

	/**
	 * Get the context instance from navigation point.
	 *
	 * @param navigationPoint
	 *            the navigation point
	 * @return the context from navigation point
	 */
	private Instance getContextFromNavigationPoint(NavigationPoint navigationPoint) {
		Instance currentInstance = null;
		if (navigationPoint != null) {
			SerializedDocumentContext storedDocumentContext = navigationPoint
					.getSerializedDocumentContext();
			if (storedDocumentContext != null) {
				String currentInstanceType = (String) storedDocumentContext.getContext().get(
						DocumentContext.CURRENT_INSTANCE);
				currentInstance = (Instance) storedDocumentContext.getContext().get(
						currentInstanceType);
			}
		}
		return currentInstance;
	}

	/**
	 * Rebuild instance hierarchy using provided parent path list by setting owning instance for
	 * every parent path instance.
	 *
	 * @param ownedInstance
	 *            the owned instance
	 * @param parentPath
	 *            the parent path
	 */
	private void linkParents(OwnedModel ownedInstance, List<InstanceReference> parentPath) {
		Collections.reverse(parentPath);
		OwnedModel currentParent = ownedInstance;
		for (InstanceReference instanceReference : parentPath) {
			Instance current = instanceReference.toInstance();
			currentParent.setOwningInstance(current);
			currentParent = (OwnedModel) current;
		}
	}

	/**
	 * Walk instance hierarchy using semantic relations.
	 *
	 * @param id
	 *            the selected instance id
	 * @param parentPath
	 *            a list in which the parent instances should be stored
	 * @param immediateParents
	 *            the a list with immediate parents for current instance
	 * @return true, if path is restored successfully
	 */
	private boolean walkHierarchy(Serializable id, List<InstanceReference> parentPath,
			Collection<InstanceReference> immediateParents) {
		// Get a parent instance. Check if is same as the context instance.
		for (InstanceReference reference : immediateParents) {
			String identifier = reference.getIdentifier();
			// If is same, then load selected instance in that context using stored parent
			// path list from the current recursion.
			// Continue until we find instance that is same as current instance context or has no
			// parents at all.
			if (id.equals(identifier)) {
				parentPath.add(reference);
				return true;
			}
		}
		// a second iteration to walk up the hierarchy
		for (InstanceReference reference : immediateParents) {
			Collection<InstanceReference> parents = getImmediateParents(reference);
			if (!parents.isEmpty()) {
				if (walkHierarchy(id, parentPath, parents)) {
					parentPath.add(reference);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Fetch immediate parents for current instance.
	 *
	 * @param reference
	 *            the reference for which to fetch parents
	 * @return the immediate parents
	 */
	private Collection<InstanceReference> getImmediateParents(InstanceReference reference) {
		return new LinkIterable<>(linkService.getSimpleLinks(reference,
				LinkConstants.TREE_CHILD_TO_PARENT), false);
	}
}
