package com.sirma.itt.seip.instance.relation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;

/**
 * The LinkReferencesDeletionService is schedule wrapper, that deletes a link instances provided as argument in the
 * context.
 * @deprecated should not be used, left only for backward compatibility
 */
@Deprecated
@ApplicationScoped
@Named(LinkReferencesDeletionService.BEAN_ID)
public class LinkReferencesDeletionService extends SchedulerActionAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinkReferencesDeletionService.class);
	public static final String BEAN_ID = "LinkReferencesDeletionService";

	/** Key which holds Collection of LinkInstance. */
	public static final String DELEATABLE = "deleatable";

	/** The OPERATION as string value */
	public static final String OPERATION = "operation";

	/** Is delete a permanent? */
	public static final String PERMANENT = "permanent";

	/** Provide some custom message. */
	public static final String CUSTOM_ERROR = "custom_error";

	@Inject
	private InstanceService instanceService;

	@Inject
	private LinkService linkService;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		List<LinkReference> failed = new LinkedList<>();
		@SuppressWarnings("unchecked")
		List<LinkReference> links = (List<LinkReference>) context.get(DELEATABLE);
		LOGGER.debug("Executing scheduled deletion of {} links!", links.size());
		Map<Serializable, LinkReference> mapping = generateMappingForReferences(links);
		List<LinkInstance> convertToLinkInstance = linkService.convertToLinkInstance(links);
		Operation operation = new Operation(context.getIfSameType(OPERATION, String.class));
		Boolean permanent = context.getIfSameType(PERMANENT, Boolean.class, Boolean.FALSE);
		for (LinkInstance linkInstance : convertToLinkInstance) {
			Instance child = null;
			try {
				child = linkInstance.getTo();
				if (child != null) {
					if (Operation.isUserOperationAs(operation, ActionTypeConstants.DELETE)) {
						instanceService.delete(child, operation, permanent);
					} else if (Operation.isUserOperationAs(operation, ActionTypeConstants.STOP)) {
						instanceService.cancel(child);
					}
				} else {
					LOGGER.error("Instance is null for association: {}! Deletion would be skipped for this item!", linkInstance);
				}
			} catch (Exception e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Failed to delete instance {}. Will try again later.",
							child != null ? child.getId() : "null", e);
				} else {
					LOGGER.warn("Failed to delete instance {}. Will try again later. The error was {}",
							child != null ? child.getId() : "null", e.getMessage());
				}
				failed.add(mapping.get(linkInstance.getId()));
			}
		}
		if (!failed.isEmpty()) {
			// update context
			context.put(DELEATABLE, (Serializable) failed);
			if (context.containsKey(CUSTOM_ERROR)) {
				throw new EmfRuntimeException(context.get(CUSTOM_ERROR).toString());
			}
			throw new EmfRuntimeException("There are problems during deletion of items! Action is rescheduled!");
		}

	}

	/**
	 * Generate mapping internal between the reference and its id for faster retrieval.
	 *
	 * @param links
	 *            are the list of {@link LinkReference}
	 * @return the mapping by id
	 */
	private static Map<Serializable, LinkReference> generateMappingForReferences(List<LinkReference> links) {
		Map<Serializable, LinkReference> mapping = CollectionUtils.createLinkedHashMap(links.size());
		for (LinkReference linkReference : links) {
			mapping.put(linkReference.getId(), linkReference);
		}
		return mapping;
	}
}
