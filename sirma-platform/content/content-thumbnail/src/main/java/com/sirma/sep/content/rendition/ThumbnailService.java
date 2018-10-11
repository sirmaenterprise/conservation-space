package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Service responsible for thumbnail synchronization and retrieval for the registered instances. The service should try
 * to retrieve a thumbnail for the source system for up to configured times. After the exceeding the configured time the
 * instance could be checked again for thumbnail only if registered again via
 * {@link ThumbnailService#register(Instance)} or rescheduled via {@link ThumbnailService#scheduleCheck(Collection)}
 * method.
 *
 * @author BBonev
 */
public interface ThumbnailService {
	String MAX_RETRIES = "MAX_RETRIES";

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The thumbnail
	 * source and target will be the same instance.
	 *
	 * @param instance
	 *            the instance to schedule
	 */
	void register(Instance instance);

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance.
	 *
	 * @param target
	 *            the target instance to assign the thumbnail to
	 * @param thumbnailSource
	 *            the instance used for thumbnail source
	 */
	void register(Instance target, Instance thumbnailSource);

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance. When retrieved the thumbnail will be
	 * assign to the target instance for the given purpose
	 *
	 * @param target
	 *            the target instance to assign the thumbnail to
	 * @param thumbnailSource
	 *            the instance used for thumbnail source
	 * @param purpose
	 *            the purpose to assign for the given thumbnail when fetched
	 */
	void register(Instance target, Instance thumbnailSource, String purpose);

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance. When retrieved the thumbnail will be
	 * assign to the target instance for the given purpose.
	 *
	 * @param target
	 *            reference to the target instance to assign the thumbnail to
	 * @param thumbnailSource
	 *            the instance used for thumbnail source
	 * @param purpose
	 *            the purpose to assign for the given thumbnail when fetched (Not required, if missing default value
	 *            will be used)
	 */
	void register(InstanceReference target, Instance thumbnailSource, String purpose);

	/**
	 * Transfers current thumbnail from source instance to target instance. The thumbnail instance is extracted by using
	 * the DB record for the source instance. Then new record is inserted for the target instance, with the same
	 * thumbnail as source instance. If the source instance doesn't have thumbnail, the method will do nothing.
	 *
	 * @param target
	 *            the instance on which the thumbnail will be transfer
	 * @param thumbnailSource
	 *            the instance from, which will be extract the thumbnail
	 */
	void copyThumbnailFromSource(InstanceReference target, InstanceReference thumbnailSource);

	/**
	 * Schedule thumbnail check for the given list of ids if registered at all. The method should reschedule only
	 * instance that are registered but does not have a thumbnails due to failed retrieval or non applicable/existent
	 * thumbnails.
	 *
	 * @param <S>
	 *            the generic key type
	 * @param ids
	 *            the list instance ids to reschedule for download
	 */
	<S extends Serializable> void scheduleCheck(Collection<S> ids);

	/**
	 * Register thumbnail for an instance identified by the given reference via external source. If the instance
	 * specified by the given reference has been registered then the specified thumbnail will override the fetched if
	 * any or will stop the retries to fetch any.
	 *
	 * @param reference
	 *            an instance reference to register a thumbnail for
	 * @param thumbnail
	 *            a thumbnail to register for to the instance
	 */
	void addThumbnail(InstanceReference reference, String thumbnail);

	/**
	 * Register thumbnail for an instance identified by the given reference via external source. If the instance
	 * specified by the given reference has been registered then the specified thumbnail will override the fetched if
	 * any or will stop the retries to fetch any.
	 *
	 * @param reference
	 *            an instance reference to register a thumbnail for
	 * @param thumbnail
	 *            a thumbnail to register for to the instance
	 * @param purpose
	 *            is any specific purpose for the thumbnail. The specified thumbnail will only be returned if requested
	 *            for the purpose. The default purpose value is <code>null</code> (no value)
	 */
	void addThumbnail(InstanceReference reference, String thumbnail, String purpose);

	/**
	 * Delete thumbnail by source instance. All entries there the source instance is used for thumbnail and the
	 * thumbnail will be removed from the database.
	 *
	 * @param sourceInstanceId
	 *            the source instance id
	 */
	void deleteThumbnail(Serializable sourceInstanceId);

	/**
	 * Removes the thumbnail for the given instance. If the purpose is not passed then all thumbnails for the instance
	 * will be removed.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param purpose
	 *            the purpose. If default thumbnail should be removed then {@link RenditionService#DEFAULT_PURPOSE} must
	 *            be passed for purpose.
	 */
	void removeThumbnail(Serializable instanceId, String purpose);

}
