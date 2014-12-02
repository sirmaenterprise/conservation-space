package com.sirma.itt.cmf.services.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;

/**
 * Observer that populates links for the parent-child relations between section parent and section
 * children. This is complimentary class of the
 * {@link com.sirma.itt.emf.instance.observer.AutolinkObserver}.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class CmfAutolinkObserver {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/**
	 * Listens for after instance persist events to link the parent to child object links
	 * 
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	public <I extends Instance> void onAfterInstanceCreated(
			@Observes AfterInstancePersistEvent<I, ?> event) {
		if (RuntimeConfiguration.isSet(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS,
				RuntimeConfigurationProperties.DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS)) {
			return;
		}
		Instance instance = event.getInstance();

		Instance parent = InstanceUtil.getDirectParent(instance, true);
		if (parent instanceof SectionInstance) {
			// attach context if any. We had a requirement to link the child to the first context in the
			// tree explicitly
			attachToContext(instance, parent, true);
		}
	}

	/**
	 * Creates parent to child link that is marked as created by system
	 * 
	 * @param from
	 *            the parent
	 * @param to
	 *            the instance
	 * @param skipExistsCheck
	 *            if check for existence before creating link
	 */
	private void createParentToChildLink(Instance from, Instance to, boolean skipExistsCheck) {
		// first check if we already has the relation not to create it again
		if (skipExistsCheck
				|| !linkService.isLinked(from.toReference(), to.toReference(),
				LinkConstants.PARENT_TO_CHILD)) {
			linkService.link(from, to, LinkConstants.PARENT_TO_CHILD,
					LinkConstants.CHILD_TO_PARENT, LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
		}
	}

	/**
	 * Attach the given new child to the first parent context different from the given direct parent
	 * if any
	 * 
	 * @param childToAttach
	 *            the child to attach
	 * @param directParent
	 *            the direct parent
	 * @param skipExistsCheck
	 *            if check for existence before creating link
	 */
	private void attachToContext(Instance childToAttach, Instance directParent,
			boolean skipExistsCheck) {
		Instance context = InstanceUtil.getParentContext(childToAttach, true);
		if ((context != null) && !context.equals(directParent)) {
			createParentToChildLink(context, childToAttach, skipExistsCheck);
		}
	}

	/**
	 * When instance is attached to other then both should be linked together.
	 * 
	 * @param event
	 *            the event
	 */
	public void onInstanceAttachedEvent(@Observes InstanceAttachedEvent<Instance> event) {
		if (event.getInstance() instanceof SectionInstance) {
			// CMF-7932: removed check that ensures creation of duplicate links between the attached
			// content to a section
			// attach to the context instance
			attachToContext(event.getChild(), event.getInstance(), true);
		}
	}

	/**
	 * When instance is detached from another instance. The observer handles he special case for
	 * section content
	 * 
	 * @param event
	 *            the event
	 */
	public void onInstanceDetachedEvent(@Observes InstanceDetachedEvent<Instance> event) {
		if (event.getInstance() instanceof SectionInstance) {
			Instance instance = InstanceUtil.getParentContext(event.getInstance());
			int count = 0;
			for (SectionInstance section : ((CaseInstance) instance).getSections()) {
				if (section.getContent().contains(event.getChild())) {
					count++;
				}
				// if we have at least one document in some section we are done
				if (count >= 1) {
					return;
				}
			}
			if (count == 0) {
				// if the instance is not attached more than once we remove the link
				linkService.unlink(instance.toReference(), event.getChild().toReference(),
						LinkConstants.PARENT_TO_CHILD, LinkConstants.CHILD_TO_PARENT);
			}
		}
	}

}
