package com.sirma.itt.seip.instance.draft;

import java.util.Collection;

/**
 * Service responsible for the draft instances operation. Defines methods for retrieve, create and delete drafts.
 *
 * @author bbanchev
 */
public interface DraftService {

	/**
	 * Gets the draft for given user and instance.
	 *
	 * @param instanceId
	 *            the instance id of searched instance draft
	 * @param userId
	 *            the id of the draft creator
	 * @return the draft or null if not found
	 */
	DraftInstance getDraft(String instanceId, String userId);

	/**
	 * Creates a new draft for given instance and user. Only one such entry should be possible. Multiple invocation
	 * should store only one entry. If the user id is not passed, the method will try to extract it from the currently
	 * authenticated.
	 *
	 * @param instanceId
	 *            the id of the instance for which should be created draft
	 * @param userId
	 *            the id of the draft creator
	 * @param content
	 *            the content of the instance that should be saved
	 * @return the created draft instance or null if the user is missing
	 */
	DraftInstance create(String instanceId, String userId, String content);

	/**
	 * Removes a draft for a instance and specific user. If the user is missing, currently authenticated will be used.
	 *
	 * @param instanceId
	 *            the id of the instance which draft should be removed
	 * @param userId
	 *            the id of the draft creator
	 * @return the deleted instance or null if nothing is deleted
	 */
	DraftInstance delete(String instanceId, String userId);

	/**
	 * Delete all drafts for given instance.
	 * <p>
	 * <b>NOTE - This method will delete only the draft entries, not the stored contents.</b>
	 *
	 * @param instanceId
	 *            the id of the instance which drafts should be removed
	 * @return deleted draft instances
	 */
	Collection<DraftInstance> delete(String instanceId);

}
