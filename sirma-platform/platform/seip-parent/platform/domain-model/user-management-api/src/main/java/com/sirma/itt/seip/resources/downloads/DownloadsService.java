/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Contains logic related to the downloads functionality. There are method for adding given instance to and removing it
 * form user downloads. The user can be passed or extracted. There also is a method, that updates the instance headers
 * download icon, when pages are loaded and headers are shown.
 *
 * @author A. Kunchev
 */
public interface DownloadsService {

	/**
	 * Adds instance to the current logged user downloads. This is done by creating the relation between the user and
	 * the instance that is passed. The link that is created is simple. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD} and it is created from the user to
	 * the instance. This method extracts the user, if you want to pass it instead, use the overloaded method
	 * {@link #add(InstanceReference, InstanceReference)}.
	 * <p>
	 * This method fires AddedToDownloadsEvent, when the relation was created successfully.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be marked for download for the current user
	 * @return true if the link is created successfully, false otherwise
	 */
	boolean add(InstanceReference instanceReference);

	/**
	 * Adds instance to the passed user downloads. This is done by creating the relation between the user and the
	 * instance that is passed. The created link is simple. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD} and it is created from the user to
	 * the instance. This method accepts instance reference, that represents the user for whom the the instance is added
	 * for download.
	 * <p>
	 * This method fires AddedToDownloadsEvent, when the relation was created successfully.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be marked for download for the user
	 * @param userInstanceReference
	 *            the user instance reference
	 * @return true if the link is created successfully, false otherwise
	 */
	boolean add(InstanceReference instanceReference, InstanceReference userInstanceReference);

	/**
	 * Removes instance from the current logged user downloads. This is done by removing the simple link, that is
	 * created, when the instance is added to the user downloads. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD}. This method extracts current
	 * logged user, if you want to pass it instead, use the overloaded method
	 * {@link #remove(InstanceReference, InstanceReference)}.
	 * <p>
	 * This method fires DeletedFromDownloadsEvent.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be removed from the user downloads
	 */
	void remove(InstanceReference instanceReference);

	/**
	 * Removes passed instance from downloads for passed user. This is done by removing the simple link, which is
	 * created, when the instance is added to the user downloads. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD}. This method accepts instance
	 * reference, that represents the user for whom the instance is removed from downloads.
	 * <p>
	 * This method fires DeletedFromDownloadsEvent.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be removed from the user downloads
	 * @param userInstanceReference
	 *            the user instance reference
	 */
	void remove(InstanceReference instanceReference, InstanceReference userInstanceReference);

	/**
	 * Removes all marked for download instances from the currently logged user downloads. First all the instances are
	 * extracted, then their relations to the user is removed. The id of the removed relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD}. The user is extracted from the
	 * current session.
	 * <p>
	 * This method fires DeletedAllFromDownloadsEvent, if relation was removed successfully.
	 *
	 * @return <b>TRUE</b> if the operation is successful, <b>FALSE</b> otherwise
	 */
	boolean removeAll();

	/**
	 * Removes all marked for download instances from the passed user downloads. First all the instances are extracted,
	 * then their relations to the user is removed. The id of the removed relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD}. This method accepts instance
	 * reference, that represents the user for whom the instances are removed from downloads.
	 * <p>
	 * This method fires DeletedAllFromDownloadsEvent, if relation was removed successfully.
	 *
	 * @param userInstanceReference
	 *            the user instance reference
	 * @return <b>TRUE</b> if the operation is successful, <b>FALSE</b> otherwise
	 */
	boolean removeAll(InstanceReference userInstanceReference);

	/**
	 * Get all instances marked for download for the currently logged user. Extract all instances, which have relation
	 * of type {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD} to the current user. The
	 * user is extracted from the session.
	 *
	 * @return collection of instances marked for download for the currently logged user
	 */
	Collection<InstanceReference> getAll();

	/**
	 * Get all instances marked for download for the given user. Extract all instances that have relation of type
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#MARKED_FOR_DOWNLOAD} to the user.
	 *
	 * @param userInstanceReference
	 *            the user instance reference
	 * @return collection of instances marked for download for the given user
	 */
	Collection<InstanceReference> getAll(InstanceReference userInstanceReference);

	/**
	 * Updates download icon in the headers for single instance. First extract all instances marked for download for the
	 * current logged user, then checks, if the passed instance is contained by the extracted instances for download for
	 * the user. If it is, then the passed instance is updated and returned, if not it is just returned.
	 * 
	 * @param <I>
	 *            instance type
	 * @param instance
	 *            the instance, which will be updated
	 * @return updated instance or the same instance, if it is not in downloads for the current user
	 */
	<I extends Instance> I updateDownloadStateForInstance(I instance);

	/**
	 * Updates the download icon in headers of the passed instances. First extract all instances marked for download for
	 * the current logged user, then filters the passed instances. If they are in the used downloads, then their headers
	 * download icon are updated. This is done by replacing CSS class in the span element, which represents the icon.
	 * The headers are extracted from the instance properties and after the update, they are set back.
	 * 
	 * @param <I>
	 *            instance type
	 * @param instances
	 *            the collection of instances that will be processed
	 */
	<I extends Instance> void updateDownloadStateForInstances(Collection<I> instances);

	/**
	 * Updates the download icon in headers of the passed instances. First extract all instances marked for download for
	 * the passed user, then filters the passed instances. If they are in the used downloads, then their headers
	 * download icon are updated. This is done by replacing CSS class in the span element, which represents the icon.
	 * The headers are extracted from the instance properties and after the update, they are set back.
	 * 
	 * @param <I>
	 *            instance type
	 * @param instances
	 *            the collection of instances that will be processed
	 * @param userInstanceReference
	 *            the user instance reference
	 */
	<I extends Instance> void updateDownloadStateForInstances(Collection<I> instances,
			InstanceReference userInstanceReference);

	/**
	 * Creates archive (zip file) from the documents, which are marked for download from the current user. Keep in mind
	 * that you should clean up the archive after you finish your work with it.
	 *
	 * @return the id of the created archive or null if there are no documents for download
	 * @see com.sirma.itt.seip.resources.downloads.DownloadsAdapterService
	 */
	String createArchive();

	/**
	 * Gets the given archive status. This service will return JSON object, which contains the information about the
	 * archivation process for the archive.
	 *
	 * <pre>
	 * <b>Object information example:</b>
	 * {
	 *  "status": "IN_PROGRESS",
	 *  "done": "4371",
	 *  "total": "6245",
	 *  "filesAdded": "4",
	 *  "totalFiles": "6"
	 * }
	 * <b>Possible statuses:</b>
	 * PENDING                   - The archiving hasn't started yet.
	 * IN_PROGRESS               - The archive is not ready for download.
	 * DONE                      - The archiving is complete and the archive can be downloaded.
	 * MAX_CONTENT_SIZE_EXCEEDED - The file size is too large to be zipped up.
	 * CANCELLED                 - The archiving was stopped or the archive was deleted.
	 * </pre>
	 *
	 * @param archiveId
	 *            the DMS id of the archive, which status will be checked
	 * @return the status of the archive or null if the passed id is null or empty
	 * @see com.sirma.itt.seip.resources.downloads.DownloadsAdapterService
	 */
	String getArchiveStatus(String archiveId);

	/**
	 * Removes given archive from DMS. This method should be called after the archive processing is done and there is no
	 * need for it any more. This method can be used to stop the archive creation or to delete created archive. The
	 * method returns JSON object with following format:
	 * <p>
	 *
	 * <pre>
	 * { "status":"CANCELLED" }
	 * </pre>
	 *
	 * @param archiveId
	 *            the DMS id of the archive, which will be cancelled or deleted
	 * @return JSON object with the status of the archive or null if the id is null or empty
	 * @see com.sirma.itt.seip.resources.downloads.DownloadsAdapterService
	 */
	String removeArchive(String archiveId);

	/**
	 * Gets the URL for the completed archive, from which the archive can be downloaded. This method should be called
	 * only, when the status of the archive is "DONE", which means that the archivation is complete.
	 *
	 * @param archiveId
	 *            the DMS id of the archive, which download URL will be returned
	 * @return the download URL of the archive or null if the id is null or empty
	 * @see com.sirma.itt.seip.resources.downloads.DownloadsAdapterService
	 */
	String getArchiveLink(String archiveId);

}
