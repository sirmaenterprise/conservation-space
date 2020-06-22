package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Service responsible for thumbnail assign and generation of the registered instances. The service should try
 * to retrieve a thumbnail from the source system for up to configured times. After the exceeding the configured time the
 * instance could be checked again for thumbnail only if registered again via
 * {@link ThumbnailService#register(Instance)} or rescheduled via {@link ThumbnailService#scheduleCheck(Collection)}
 * method. <p>
 * The service supports multiple thumbnail types defined in {@link ThumbnailType}.<br>
 * For thumbnail loading the {@link RenditionService} should be used.
 *
 * @author BBonev
 */
public interface ThumbnailService {
	String MAX_RETRIES = "MAX_RETRIES";

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The thumbnail
	 * source and target will be the same instance. This is mainly useful for uploaded instance that can be converted to
	 * image and have it used as thumbnail.<br>
	 * <b>The method will update only {@link ThumbnailType#SELF} thumbnail.</b>
	 *
	 * @param instance the instance to register or schedule
	 */
	default void register(Instance instance) {
		if (instance != null) {
			register(instance.getId());
		}
	}

	/**
	 * Register instance represented by it's id for thumbnail check and synchronization. If method is called again
	 * for instance with a thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check.
	 * The thumbnail source and target will be the same instance. This is mainly useful for uploaded instance that can
	 * be converted to image and have it used as thumbnail.<br>
	 * <b>The method will update only {@link ThumbnailType#SELF} thumbnail.</b>
	 *
	 * @param instanceId the instance id to register or schedule
	 */
	default void register(Serializable instanceId) {
		register(instanceId, instanceId);
	}
  
	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance. When retrieved the thumbnail will be
	 * assign to the target instance for the given purpose. The purpose of the method is to assign a different thumbnail
	 * to an instance because one of the following<ul>
	 * <li>the instance is not uploaded</li>
	 * <li>the instance does not support thumbnail generation</li>
	 * <li>want to override the default instance thumbnail</li>
	 * </ul>
	 *
	 * @param target the instance to assign the thumbnail to
	 * @param thumbnailSource the image to use as thumbnail
	 * @deprecated Replaced by {@link #register(Serializable, Serializable)}
	 */
	@Deprecated
	default void register(Instance target, Instance thumbnailSource) {
		Serializable targetId = null;
		Serializable thumbnailSourceId = null;
		if (target != null) {
			targetId = target.getId();
		}
		if (thumbnailSource  != null) {
			thumbnailSourceId = thumbnailSource.getId();
		}
		register(targetId, thumbnailSourceId);
	}

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance. When retrieved the thumbnail will be
	 * assign to the target instance for the given purpose. The purpose of the method is to assign a different thumbnail
	 * to an instance because one of the following:<ul>
	 * <li>the instance is not uploaded</li>
	 * <li>the instance does not support thumbnail generation</li>
	 * <li>want to override the default instance thumbnail</li>
	 * </ul>
	 * <br>
	 * <b>The method will create/update the {@link ThumbnailType#ASSIGNED} thumbnail if the thumbnail source id is
	 * different from the target instance otherwise the {@link ThumbnailType#SELF} thumbnail will be updated.</b>
	 *
	 * @param targetId reference to the target instance to assign the thumbnail to
	 * @param thumbnailSourceId the instance used for thumbnail source
	 */
	default void register(Serializable targetId, Serializable thumbnailSourceId) {
		register(targetId, thumbnailSourceId, (ThumbnailType) null);
	}

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance. When retrieved the thumbnail will be
	 * assign to the target instance for the given purpose. <br>
	 * The purpose of the method is to assign a different thumbnail
	 * to an instance because one of the following<ul>
	 * <li>the instance is not uploaded</li>
	 * <li>the instance does not support thumbnail generation</li>
	 * <li>want to override the default instance thumbnail</li>
	 * </ul>
	 * The added purpose parameter could be used to assign multiple thumbnails to an instance (like small and big thumbnail)<br>
	 * If not passed the purpose will be calculated by the following rules <ul>
	 *   <li>If both instance ids are equal then purpose={@link ThumbnailType#SELF}</li>
	 *   <li>If instances are different then purpose={@link ThumbnailType#ASSIGNED}</li>
	 * </ul>
	 *
	 * @param targetId reference to the target instance to assign the thumbnail to
	 * @param thumbnailSourceId the instance used for thumbnail source
	 * @param purpose the purpose to assign for the given thumbnail when fetched (Not required, if missing the purpose will be calculated)
	 * @deprecated use {@link #register(Serializable, Serializable, ThumbnailType)}. Left for compatibility and will be
	 * removed in future versions
	 */
	@Deprecated
	default void register(Serializable targetId, Serializable thumbnailSourceId, String purpose) {
		ThumbnailType type = purpose == null ? null : ThumbnailType.valueOf(purpose.toUpperCase());
		register(targetId, thumbnailSourceId, type);
	}

	/**
	 * Register instance for thumbnail check and synchronization. If method is called again for instance with a
	 * thumbnail the old thumbnail will be cleared and the instance will be scheduled for new check. The method allows
	 * to specify the thumbnails source to be different from the target instance. When retrieved the thumbnail will be
	 * assign to the target instance for the given purpose. <br>
	 * The purpose of the method is to assign a different thumbnail
	 * to an instance because one of the following<ul>
	 * <li>the instance is not uploaded</li>
	 * <li>the instance does not support thumbnail generation</li>
	 * <li>want to override the default instance thumbnail</li>
	 * </ul>
	 * The added purpose parameter could be used to assign multiple thumbnails to an instance (like small and big thumbnail)<br>
	 * If not passed the purpose will be calculated by the following rules <ul>
	 *   <li>If both instance ids are equal then purpose={@link ThumbnailType#SELF}</li>
	 *   <li>If instances are different then purpose={@link ThumbnailType#ASSIGNED}</li>
	 * </ul>
	 *
	 * @param targetId reference to the target instance to assign the thumbnail to
	 * @param thumbnailSourceId the instance used for thumbnail source
	 * @param purpose the purpose to assign for the given thumbnail when fetched (Not required, if missing the purpose will be calculated)
	 */
	void register(Serializable targetId, Serializable thumbnailSourceId, ThumbnailType purpose);

	/**
	 * Schedule thumbnail check for the given list of ids if registered at all. The method should reschedule only
	 * instance that are registered but does not have a thumbnails due to failed retrieval or non applicable/existent
	 * thumbnails.
	 *
	 * @param <S> the generic key type
	 * @param ids the list instance ids to reschedule for download
	 */
	<S extends Serializable> void scheduleCheck(Collection<S> ids);

	/**
	 * Register thumbnail represented as base64 text for an instance. If the instance specified by the given reference
	 * has been registered then the specified thumbnail will override the fetched if any or will stop the retries
	 * to fetch any.
	 *
	 * @param id an instance id to register a thumbnail for
	 * @param thumbnail a thumbnail to register for to the instance
	 * @deprecated use {@link #addAssignedThumbnail(Serializable, String)} or {@link #addSelfThumbnail(Serializable, String)}
	 */
	@Deprecated
	default void addThumbnail(Serializable id, String thumbnail) {
		addSelfThumbnail(id, thumbnail);
	}

	/**
	 * Adds or updates an externally fetched thumbnail, represented as base64 text, for the given instance. The thumbnail
	 * will be added for representation for the {@link ThumbnailType#ASSIGNED} type.
	 *
	 * @param id an instance id to register a thumbnail for
	 * @param thumbnail a thumbnail to register for to the instance
	 */
	default void addAssignedThumbnail(Serializable id, String thumbnail) {
		addThumbnail(id, thumbnail, ThumbnailType.ASSIGNED);
	}

	/**
	 * Adds or updates an externally fetched thumbnail, represented as base64 text, for the given instance. The thumbnail
	 * will be added for representation for the {@link ThumbnailType#SELF} type.
	 *
	 * @param id an instance id to register a thumbnail for
	 * @param thumbnail a thumbnail to register for to the instance
	 */
	default void addSelfThumbnail(Serializable id, String thumbnail) {
		addThumbnail(id, thumbnail, ThumbnailType.SELF);
	}

	/**
	 * Register thumbnail represented as base64 text for an instance. If the instance specified by the given reference
	 * has been registered then the specified thumbnail will override the fetched if any or will stop the retries
	 * to fetch any. The method allows setting a concrete instance thumbnail purse as well.
	 *
	 * @param targetId an instance id to register a thumbnail for
	 * @param thumbnail a thumbnail to register for to the instance
	 * @param purpose is any specific purpose for the thumbnail. The specified thumbnail will only be returned if requested
	 * for the purpose. The default purpose value is <code>null</code> (no value)
	 * @deprecated use {@link #addThumbnail(Serializable, String, ThumbnailType)}
	 */
	@Deprecated
	default void addThumbnail(Serializable targetId, String thumbnail, String purpose) {
		ThumbnailType type = purpose == null ? null : ThumbnailType.valueOf(purpose.toUpperCase());
		addThumbnail(targetId, thumbnail, type);
	}

	/**
	 * Register thumbnail represented as base64 text for an instance. If the instance specified by the given reference
	 * has been registered then the specified thumbnail will override the fetched if any or will stop the retries
	 * to fetch any. The method allows setting a concrete instance thumbnail purse as well.
	 *
	 * @param targetId an instance id to register a thumbnail for
	 * @param thumbnail a thumbnail to register for to the instance
	 * @param purpose is any specific purpose for the thumbnail. The specified thumbnail will only be returned if requested
	 * for the purpose.
	 */
	void addThumbnail(Serializable targetId, String thumbnail, ThumbnailType purpose);

	/**
	 * Deletes the thumbnail of the given instance and all entries where the given instance is used for thumbnail.
	 * The method should be called when deleting an instance.
	 *
	 * @param instanceId the instance id of the thumbnail to remove.
	 */
	void deleteThumbnail(Serializable instanceId);

	/**
	 * Removes the thumbnail for the given instance. If the purpose is not passed then all thumbnails for the instance
	 * will be removed.
	 *
	 * @param instanceId the instance id
	 * @param purpose the purpose.
	 * @deprecated replaced by {@link #removeThumbnail(Serializable, ThumbnailType)}. Could also use
	 * {@link #removeSelfThumbnail(Serializable)} or {@link #removeAssignedThumbnail(Serializable)}. Left for
	 * compatibility and will be removed in future versions.
	 */
	@Deprecated
	default boolean removeThumbnail(Serializable instanceId, String purpose) {
		ThumbnailType type = purpose == null ? null : ThumbnailType.valueOf(purpose.toUpperCase());
		return removeThumbnail(instanceId, type);
	}

	/**
	 * Removes the thumbnail value for the given instance and purpose. If the instance does not have a thumbnail of for
	 * the given purpose then the method does nothing.
	 *
	 * @param instanceId the instance id to delete it's thumbnail
	 * @param purpose the concrete thumbnail purpose to delete. If null then the {@link ThumbnailType#SELF} will be deleted.
	 * @return true if any thumbnail is found and removed for the given instance.
	 */
	boolean removeThumbnail(Serializable instanceId, ThumbnailType purpose);

	/**
	 * Removes the {@link ThumbnailType#SELF} thumbnail of the given instance. If the Instance has other thumbnails they
	 * will be left intact.
	 *
	 * @param instanceId the if of the instance to remove it's own thumbnail
	 */
	default void removeAssignedThumbnail(Serializable instanceId) {
		boolean successful = removeThumbnail(instanceId, ThumbnailType.ASSIGNED);
		if (!successful) {
			removeThumbnail(instanceId, ThumbnailType.DEFAULT);
		}
	}

	/**
	 * Removes the {@link ThumbnailType#ASSIGNED} thumbnail of the given instance. If the Instance has other thumbnails
	 * they will be left intact.
	 *
	 * @param instanceId the if of the instance to remove it's assigned thumbnail
	 */
	default void removeSelfThumbnail(Serializable instanceId) {
		boolean successful = removeThumbnail(instanceId, ThumbnailType.SELF);
		if (!successful) {
			removeThumbnail(instanceId, ThumbnailType.DEFAULT);
		}
	}
}
