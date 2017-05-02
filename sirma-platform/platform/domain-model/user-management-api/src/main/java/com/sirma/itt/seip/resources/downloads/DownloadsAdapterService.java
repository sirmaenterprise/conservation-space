/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import java.io.Serializable;
import java.util.Collection;

/**
 * This interface extends com.sirma.itt.emf.adapter.CMFAdapterService and it is used to declare methods for creating and
 * processing archives (zip files) from DMS. The communication with the DMS is done be rest services.
 *
 * @author A. Kunchev
 */
public interface DownloadsAdapterService {

	/**
	 * Creates archive (zip file) from the documents, which are marked for download from the current user. Builds
	 * request JSON body and makes service call. Uses afresco rest services to create the archive. <br>
	 * Keep in mind that you should clean up the archive after you finish your work with it.
	 * <p>
	 * <b>DMS request example</b>
	 *
	 * <pre>
	 * method:       POST
	 * url:          http://xxx.xxx.xxx.xxx:XXXX/alfresco/service/api/internal/downloads
	 * Content-Type: application/json
	 * json body:    [{"nodeRef":"workspace://SpacesStore/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"},
	 *                {"nodeRef":"workspace://SpacesStore/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}]
	 * </pre>
	 * <p>
	 * <b>Response example</b>
	 *
	 * <pre>
	 * {
	 *  "status": "success",
	 *  "nodeRef": "workspace://SpacesStore/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
	 * }
	 * </pre>
	 *
	 * @param instances
	 *            the document instances, which will be archived
	 * @return the id of the created archive or null if there are no documents for download
	 */
	String createArchive(Collection<Serializable> instances);

	/**
	 * Gets the given archive status. This service will return JSON object, which contains the information about the
	 * archivation process. Makes call to alfresco rest service.
	 * <p>
	 * <b>DMS request example</b>
	 *
	 * <pre>
	 * method: GET
	 * url:    http://xxx.xxx.xxx.xxx:XXXX/alfresco/service/api/internal/downloads/workspace/SpacesStore/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/status
	 *
	 * <b>Response example:</b>
	 * {
	 *  "status": "IN_PROGRESS",
	 *  "done": "4371",
	 *  "total": "6245",
	 *  "filesAdded": "4",
	 *  "totalFiles": "6"
	 * }
	 *
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
	 */
	String getArchiveStatus(String archiveId);

	/**
	 * Removes given archive from DMS. This method should be called after the archive processing is done and there is no
	 * need for it any more. The method can be used to stop the archive creation or to delete created archive.
	 * <p>
	 * <b>DMS request example</b>
	 *
	 * <pre>
	 * method: DELETE
	 * url:    http://xxx.xxx.xxx.xxx:XXXX/alfresco/service/api/internal/downloads/workspace/SpacesStore/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx*
	 *
	 * <b>Response example</b>
	 * {
	 *  "status":"CANCELLED"
	 * }
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
	String getArchiveURL(String archiveId);

}
