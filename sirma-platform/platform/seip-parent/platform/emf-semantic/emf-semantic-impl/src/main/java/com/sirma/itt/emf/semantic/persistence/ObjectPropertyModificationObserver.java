package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkAddedEvent;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkRemovedEvent;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Observer that listens for object property modifications from the {@link LinkService} and triggers cache clear for the
 * referenced instances. The observer collects all changes in the transaction and tries to perform a single clear at the
 * end of the transaction.
 *
 * @author BBonev
 */
@ApplicationScoped
class ObjectPropertyModificationObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceService instanceService;

	@Inject
	private ChangedInstanceBuffer changedBuffer;

	@Inject
	private TransactionSupport transactionSupport;

	void onLinkAdded(@Observes LinkAddedEvent event) {
		LinkReference addedLink = event.getAddedLink();
		if (addChanges(addedLink)) {
			// to access the change buffer it should be in the current transaction context
			transactionSupport.invokeBeforeTransactionCompletion(this::touchUsingBuffer);
		}
	}

	private boolean addChanges(LinkReference link) {
		try {
			changedBuffer.addChange(link.getFrom());
			changedBuffer.addChange(link.getTo());
			return true;
		} catch (ContextNotActiveException e) {
			LOGGER.debug("Context not active. Touch instance from link operation now");
			LOGGER.trace("Context not active. Touch instance now", e);
			touchInstance(link);
			return false;
		}
	}

	@SuppressWarnings("boxing")
	private void touchUsingBuffer() {
		try {
			// get changes from the current transaction and reset the buffer so only the first to enter should trigger
			// update, also remove the target instance from the buffer in order to keep the data in the cache
			Collection<Serializable> changes = changedBuffer.getChangesAndReset();
			if (isEmpty(changes)) {
				return;
			}

			instanceService.touchInstance(changes);
			LOGGER.debug("Touched {} instances after relation change: {}", changes.size(), changes);
		} catch (ContextNotActiveException e) {
			LOGGER.warn("Could not get buffered changes!", e);
		}
	}

	private void touchInstance(LinkReference link) {
		instanceService.touchInstance(Arrays.asList(link.getFrom(), link.getTo()));
	}

	void onLinkRemoved(@Observes LinkRemovedEvent event) {
		LinkReference removedLink = event.getRemovedLink();
		if (addChanges(removedLink)) {
			transactionSupport.invokeBeforeTransactionCompletion(this::touchUsingBuffer);
		}
	}
}
