package com.sirma.sep.content;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sirma.sep.content.event.ContentUpdatedEvent;

/**
 * InstanceContentService provides means to assign multiple contents to a single instance and provide access to them.
 * Instances now could have a view and it's separate from the actual content of the instance (documents for example).
 * Also each instance could have more than one view or primary content. These additional contents are identified by the
 * type parameters of the save and load methods.
 *
 * @author BBonev
 */
public interface InstanceContentService {

	/**
	 * Saves and associates the content to the given instance. If the instance has a content associated for the given
	 * {@link Content#getPurpose()} then it will be updated.
	 *
	 * @param instance
	 *            the instance to associate the content to
	 * @param content
	 *            the content to add/store/update
	 * @return the content info containing all known information about the referenced file
	 */
	ContentInfo saveContent(Serializable instance, Content content);

	/**
	 * Update a specific content identified by the given content identifier. If the identifier is <code>null</code> then
	 * an exception will be thrown. This method can update old content version by providing the content id of the
	 * version. This method will ignore the value returned by the {@link Content#isVersionable()} method. This method
	 * will not create any new versions but will trigger the event {@link ContentUpdatedEvent}
	 *
	 * @param contentId
	 *            the identifier of the content that need to be updated
	 * @param instance
	 *            an optional instance to use for event notifications. Possible values are
	 *            {@code com.sirma.itt.seip.domain.instance.Instance},
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference} or instance id
	 * @param content
	 *            the content that should replace the current content represented by the given content identifier.
	 * @return the content info containing all known information about the referenced file after the update
	 */
	ContentInfo updateContent(String contentId, Serializable instance, Content content);

	/**
	 * Copy the content identified by the given identifier and assign it to the given instance. The content should be
	 * copied synchronously. If the original content is not found then this method does nothing. The method
	 * {@link ContentInfo#exists()} should return <code>true</code> on successful operation.
	 *
	 * @param instance
	 *            the target instance that need the given content to be copied to
	 * @param contentId
	 *            the content identifier to use to load the content to copy
	 * @return the content info that will identify the new content assigned to the new instance.
	 */
	ContentInfo copyContent(Serializable instance, String contentId);

	/**
	 * Copy the content identified by the given identifier and assign it to the given instance. The content should be
	 * copied asynchronously. The returned {@link ContentInfo#exists()} will report <code>false</code> until the copy is
	 * complete.
	 *
	 * @param instance
	 *            the target instance that need the given content to be copied to
	 * @param contentId
	 *            the content identifier to use to load the content to copy
	 * @return the content info that will identify the new content assigned to the new instance.
	 */
	ContentInfo copyContentAsync(Serializable instance, String contentId);

	/**
	 * Saves and associates the given {@link Content} definitions to the given instance. If any of the contents is
	 * already associated it will be updated.
	 *
	 * @param instance
	 *            the instance to associate to
	 * @param content
	 *            the collection of contents to associate.
	 * @return the list of content info for each save/updated Content passed in the same order as they were received
	 */
	List<ContentInfo> saveContent(Serializable instance, List<Content> content);

	/**
	 * Gets the content for a given {@code com.sirma.itt.seip.domain.instance.Instance},
	 * {@code com.sirma.itt.seip.domain.instance.InstanceReference}, instance id or content identifier and content type.
	 *
	 * @param identifier
	 *            the identifier to be used for fetching the content. Possible values are
	 *            {@code com.sirma.itt.seip.domain.instance.Instance},
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference}, instance id or content id.
	 * @param purpose
	 *            the content purpose to use to distinguish the contents. This parameter is required if for identifier
	 *            is passed one of the values: {@code com.sirma.itt.seip.domain.instance.Instance},
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference} or instance id.
	 * @return the content or {@link ContentInfo#DO_NOT_EXIST} if not found or identifier is <code>null</code>.
	 */
	ContentInfo getContent(Serializable identifier, String purpose);

	/**
	 * Gets the preview of the content identified for a given {@code com.sirma.itt.seip.domain.instance.Instance},
	 * {@code com.sirma.itt.seip.domain.instance.InstanceReference}, instance id or content identifier and content type.
	 *
	 * @param identifier
	 *            the identifier to be used for fetching the content. Possible values are
	 *            {@code com.sirma.itt.seip.domain.instance.Instance},
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference}, instance id or content id.
	 * @param purpose
	 *            the content purpose to use to distinguish the contents. This parameter is required if for identifier
	 *            is passed one of the values: {@code com.sirma.itt.seip.domain.instance.Instance},
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference} or instance id.
	 * @return the content or {@link ContentInfo#DO_NOT_EXIST} if not found or identifier is <code>null</code>.
	 */
	ContentInfo getContentPreview(Serializable identifier, String purpose);

	/**
	 * Batch get content information for multiple instances and a single type.
	 *
	 * @param identifiers
	 *            the identifiers that need to be fetched. The elements of the source collection could be of type
	 *            {@code com.sirma.itt.seip.domain.instance.Instance},
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference}, instance ids and content ids.
	 * @param purpose
	 *            the type of the content relative to the instances.
	 * @return a collection of found contents, never <code>null</code>
	 */
	Collection<ContentInfo> getContent(Collection<? extends Serializable> identifiers, String purpose);

	/**
	 * Gets content associated with the given instance identifier, filtered by content purpose. If the filter collection
	 * is empty or <code>null</code> all found contents will be returned. If no content is associated then empty
	 * collection will be returned. If the given identifier is not instance identifier then empty collection will be
	 * returned.
	 *
	 * @param identifier
	 *            the instance identifier or {@code com.sirma.itt.seip.domain.instance.Instance} or
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference}.
	 * @param contentsToSkip
	 *            collection of content purposes, used to filter results
	 * @return collection of content info associated with the given instance or empty collection if nothing is
	 *         associated or all of the found are filtered
	 */
	Collection<ContentInfo> getContentsForInstance(Serializable identifier, Collection<String> contentsToSkip);

	/**
	 * Gets all content associated with the given instance identifier. If no content is associated then empty collection
	 * will be returned. If the given identifier is not instance identifier then empty collection will be returned.
	 *
	 * @param identifier
	 *            the instance identifier or {@code com.sirma.itt.seip.domain.instance.Instance} or
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference}.
	 * @return the all content info associated with the given instance or empty collection if nothing is associated,
	 *         yet.
	 */
	default Collection<ContentInfo> getAllContent(Serializable identifier) {
		return getContentsForInstance(identifier, null);
	}

	/**
	 * Delete all content for the given instance identifier.
	 *
	 * @param identifier
	 *            the identifier or {@code com.sirma.itt.seip.domain.instance.Instance} or
	 *            {@code com.sirma.itt.seip.domain.instance.InstanceReference}.
	 * @return <code>true</code>, if all found content is successfully deleted or no content is found at all.
	 *         <code>false</code> if any of the found content failed to be deleted.
	 */
	boolean deleteAllContentForInstance(Serializable identifier);

	/**
	 * Delete content identified by the given identifier and content purpose. The identifier could be a content
	 * identifier, instance id, {@code com.sirma.itt.seip.domain.instance.Instance} or
	 * {@code com.sirma.itt.seip.domain.instance.InstanceReference}.
	 * <p>
	 * Possible uses:
	 *
	 * <pre>
	 * {@code
	 * instanceContentService.deleteContent(contentId, null);
	 * instanceContentService.deleteContent(instanceId, contentType);
	 * instanceContentService.deleteContent(instance, contentType);
	 * instanceContentService.deleteContent(instanceReference, contentType);
	 * }
	 * </pre>
	 *
	 * @param identifier
	 *            the identifier of the content that need to be deleted or instance identifier.
	 * @param purpose
	 *            the purpose of the content relative to the instances.
	 * @return true, if the content is found and successfully deleted and <code>false</code> if not found or could not
	 *         be deleted.
	 */
	boolean deleteContent(Serializable identifier, String purpose);

	/**
	 * Schedules content delete after specific time period. The task that executes the delete is persistent and it will
	 * be executed, even if the application is restarted. The amount of retries are two in case that the first time
	 * fails. Task execution is asynchronous.
	 *
	 * @param identifier
	 *            the id of the instance, which content will be deleted. This identifier could be passed as instance id,
	 *            instance or instance reference. The id will be resolved by internal logic. Required
	 * @param purpose
	 *            the purpose of the content that should be deleted. Required
	 * @param delay
	 *            the amount of delay before the actual delete
	 * @param timeUnit
	 *            the {@link TimeUnit} for the delay. Required
	 */
	void deleteContent(Serializable identifier, String purpose, int delay, TimeUnit timeUnit);

	/**
	 * Import the content described by the given {@link ContentImport} instance. After successful call to this method
	 * and transaction commit, the content should be accessible via the methods
	 * {@link #getContent(Serializable, String)} and {@link #getContent(Collection, String)}
	 *
	 * @param contentImport
	 *            the content to import
	 * @return the content id under with the given {@link ContentImport} is added into the system or <code>null</code>
	 *         if could not be added or <code>null</code> is passed
	 */
	String importContent(ContentImport contentImport);

	/**
	 * Batch import content information.
	 *
	 * @param contentImports
	 *            the content imports
	 * @return the list with the content ids of the imported content instances in the same order in one they are passed.
	 *         A content id may be <code>null</code> if the content fail to import. Empty collection if the input is
	 *         <code>null</code> or empty collection.
	 * @see #importContent(ContentImport)
	 */
	List<String> importContent(List<ContentImport> contentImports);

	/**
	 * Assign content to instance. This sets the given instance to the content identified by the given id. If the
	 * content is already assign to other instance this operation will not succeed and will return <code>false</code>.
	 * <p>
	 * This method should be used in cases when the content is uploaded before the instance is created. Before this
	 * assignment the content will be retrievable only by it's content id.
	 * <p>
	 * If there are contents with the same purpose the new content will be assigned with incremented version.
	 *
	 * @param contentId
	 *            the content id
	 * @param instanceId
	 *            the instance id
	 * @param purpose
	 *            used to check if there are content with the same purpose for that instance
	 * @return true, if successful
	 */
	boolean assignContentToInstance(String contentId, Serializable instanceId, String purpose);
}
