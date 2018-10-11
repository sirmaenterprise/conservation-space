package com.sirma.itt.seip.instance.revision;

import java.util.Collection;
import java.util.Optional;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Service responsible for working and managing instance revisions. Revisions a instance copies/snapshots of the
 * instance properties and content. To create new revision a publish operation must be performed.
 *
 * @author BBonev
 */
public interface RevisionService {

	/**
	 * Publish operation that creates new revision for the given instance. The created revision is returned by the
	 * method. Note that if the given instance is a revision the method does nothing and returns <code>null</code>
	 * instance. Also if the given instance is not allow for revisioning the method also does nothing and returns
	 * <code>null</code>.
	 *
	 * @param instanceToPublish
	 *            the instance to publish
	 * @param operation
	 *            the concrete operation that triggers the publish
	 * @return the published revision or <code>null</code> if the given instance is revision or is not
	 *         configured/allowed for revision
	 */
	default Instance publish(Instance instanceToPublish, Operation operation) {
		return publish(new PublishInstanceRequest(instanceToPublish, operation, null, null));
	}

	/**
	 * Publish the main instance and the associated instances identified by the given request.
	 *
	 * @param publishRequest
	 *            the publish request
	 * @return the main published instance
	 * @see PublishInstanceRequest
	 */
	Instance publish(PublishInstanceRequest publishRequest);

	/**
	 * Gets the last revision it could be approved or rejected.
	 *
	 * @param instance
	 *            the instance
	 * @return the last revision
	 */
	Instance getLastRevision(Instance instance);

	/**
	 * Checks if is revision supported for the given instance
	 *
	 * @param instanceToCheck
	 *            the instance to check
	 * @return true, if revisioning is supported
	 */
	boolean isRevisionSupported(Instance instanceToCheck);

	/**
	 * Gets the revisions for the given instance represented by the given {@link InstanceReference}.
	 *
	 * @param <I>
	 *            the generic type
	 * @param reference
	 *            the reference to look for
	 * @param ascending
	 *            specify order type
	 * @return the revisions or empty collection if not revision are found
	 */
	<I extends Instance> Collection<I> getRevisions(InstanceReference reference, boolean ascending);

	/**
	 * Checks if the current instance is revision or not. If the instance does not support revisions then
	 * <code>false</code> is returned.
	 *
	 * @param instanceToCheck
	 *            the instance to check
	 * @return true, if is revision
	 */
	boolean isRevision(Instance instanceToCheck);

	/**
	 * Gets the original instance for the given revision. The method accepts a revision instance. If the current
	 * instance is not a revision then the method may return the same instance
	 *
	 * @param revision
	 *            the revision
	 * @return the original instance of the given revision
	 */
	Instance getRevisionOf(Instance revision);

	/**
	 * <ol>
	 *     <li>Fetches name of context where <code>revision</code> have to exist. It is described in definition of <code>revision</code>.</li>
	 *     <li>Searches for instance with that name. If found return it, otherwise create it.</li>
	 * </ol>
	 * @param revision - the revision.
	 * @return return context instance where revision have to be exist.
	 */
	Optional<Instance> getOrCreateContextForRevision(Instance revision);

	/**
	 * Creates contexts for revisions.
	 */
	void createRevisionsContexts();
}
