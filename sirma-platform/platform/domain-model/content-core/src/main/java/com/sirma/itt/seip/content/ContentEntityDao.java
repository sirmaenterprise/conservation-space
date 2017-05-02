package com.sirma.itt.seip.content;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.io.BufferedInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Properties;
import com.sirma.itt.seip.content.type.MimeTypeResolver;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;

/**
 * DAO for loading and persisting {@link ContentEntity}s.
 *
 * @author BBonev
 */
@Singleton
class ContentEntityDao {
	// some browsers return application/unknown or application/x-download for mimetypes not recognized by the OS
	// here are generally mimetypes that are considered as not valid or not accurate and we will try to reevaluate
	// them using our detectors
	private static final Set<String> INVALID_MIMETYPES = new HashSet<>(
			Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, "application/unknown", "application/x-download"));

	private final DatabaseIdManager idManager;
	private final MimeTypeResolver mimeTypeResolver;
	private final DbDao dbDao;
	private final IdResolver resolver;
	private final ContentDigestProvider contentDigestProvider;

	/**
	 * Instantiates a new content entity dao.
	 *
	 * @param idManager
	 *            the id manager
	 * @param mimeTypeResolver
	 *            the mime type resolver
	 * @param dbDao
	 *            the db dao
	 * @param idResolver
	 *            the id resolver to use
	 * @param digestProvider
	 *            the provider to use
	 */
	@Inject
	public ContentEntityDao(DatabaseIdManager idManager, MimeTypeResolver mimeTypeResolver, DbDao dbDao,
			IdResolver idResolver, ContentDigestProvider digestProvider) {
		this.idManager = idManager;
		this.mimeTypeResolver = mimeTypeResolver;
		this.dbDao = dbDao;
		resolver = idResolver;
		contentDigestProvider = digestProvider;
	}

	/**
	 * Assign content to instance by setting the given instance id to the given content id if not already set
	 *
	 * @param contentId
	 *            the content id
	 * @param instanceId
	 *            the instance id
	 * @return true, if successful
	 */
	public boolean assignContentToInstance(String contentId, Serializable instanceId) {
		return dbDao.executeUpdate(ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY,
				Arrays.asList(new Pair<>("instanceId", instanceId), new Pair<>("id", contentId))) == 1;
	}

	/**
	 * Gets the or create import entity.
	 *
	 * @param contentImport
	 *            the content import
	 * @return the or create import entity
	 */
	public ContentEntity importEntity(ContentImport contentImport) {
		// versionable forces to import new record, not to override/update existent one
		ContentEntity entity = getOrCreateEntity(contentImport.getInstanceId(), contentImport.setVersionable(true));
		boolean isNew = false;
		if (entity == null) {
			isNew = true;
			entity = new ContentEntity();
			idManager.generateStringId(entity, false);
		}
		entity.merge(contentImport);

		if (entity.getMimeType() == null) {
			if (contentImport.getContent() != null) {
				entity.setMimeType(detectMimeTypeFromContent(contentImport));
			} else {
				entity.setMimeType(MediaType.APPLICATION_OCTET_STREAM);
			}
		}

		persistEntity(entity, isNew);
		return entity;
	}

	/**
	 * Delete entity.
	 *
	 * @param contentEntity
	 *            the content entity
	 * @return true, if successful
	 */
	public boolean delete(ContentEntity contentEntity) {
		if (contentEntity != null) {
			return dbDao.delete(ContentEntity.class, contentEntity.getId()) > 0;
		}
		return false;
	}

	/**
	 * Gets {@link ContentEntity}s for instance id, filtered by purposes. If the skip collection is empty or
	 * <code>null</code>, all contents associated with the instance will be returned.
	 *
	 * @param id
	 *            the id
	 * @param contentsToSkip
	 *            collection of content purposes which will be used to filter the results. Could be <code>null</code> or
	 *            empty for extracting all contents
	 * @return contents associated with the specific instance
	 */
	public List<ContentEntity> getContentsForInstance(Serializable id, Collection<String> contentsToSkip) {
		if (StringUtils.isNullOrEmpty(Objects.toString(id, null))) {
			return emptyList();
		}

		List<ContentEntity> results = dbDao.fetchWithNamed(ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE_KEY,
				Arrays.asList(new Pair<>("instanceId", id)));

		if (isNotEmpty(contentsToSkip)) {
			return results.stream().filter(contentFilter(contentsToSkip)).collect(Collectors.toList());
		}

		return results;
	}

	private static Predicate<ContentEntity> contentFilter(Collection<String> purposes) {
		return entity -> !purposes.stream().anyMatch(purpose -> entity.getPurpose().contains(purpose));
	}

	/**
	 * Gets the or create entity for the given instance and content
	 *
	 * @param instance
	 *            the instance
	 * @param content
	 *            the content
	 * @return the or create entity
	 */
	public ContentEntity getOrCreateEntity(Serializable instance, Content content) {
		Serializable instanceId = resolver.resolve(instance).orElse(null);
		ContentEntity entity = getEntity(instanceId, content.getPurpose());
		if (entity == null) {
			entity = createNewEntity(0);
		} else if (content.isVersionable()) {
			int incrementedVersion = Math.incrementExact(entity.getVersion());
			String oldRemoteSource = entity.getRemoteSourceName();
			entity = createNewEntity(incrementedVersion);
			// copy the remote source name so that the new version is uploaded in the same store without any
			// additional checks
			entity.setRemoteSourceName(oldRemoteSource);
		}
		entity.setInstanceId(Objects.toString(instanceId, null));

		fillEntityMetadata(entity, content, instance);
		return entity;
	}

	/**
	 * Get or create entity for unique content. The method will calculate a digest using the installed
	 * {@link ContentDigestProvider}s
	 *
	 * @param instance
	 *            that is assigned to the content
	 * @param content
	 *            the content that should be stored
	 * @return content entity, not null
	 */
	public ContentEntity getUniqueContent(Serializable instance, Content content) {
		String digest = contentDigestProvider.digest(content);
		ContentEntity entity = getEntityByChecksum(digest);
		if (entity == null) {
			entity = createNewEntity(0);
			Serializable instanceId = resolver.resolve(instance).orElse(null);
			entity.setInstanceId(Objects.toString(instanceId, null));
			fillEntityMetadata(entity, content, instance);
			entity.setChecksum(digest);
		}
		return entity;
	}

	/**
	 * Gets the entity for the given digest/checksum
	 *
	 * @param digest
	 *            the digest to look for
	 * @return the entity or <code>null</code> if digest is null or not found
	 */
	public ContentEntity getEntityByChecksum(String digest) {
		if (StringUtils.isNullOrEmpty(digest)) {
			return null;
		}
		List<ContentEntity> list = dbDao.fetchWithNamed(ContentEntity.QUERY_CONTENT_BY_CHECKSUM_KEY,
				Arrays.asList(new Pair<>("checksum", digest)));
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	protected boolean isNewEntity(ContentEntity entity) {
		return !idManager.isPersisted(entity);
	}

	protected void fillEntityMetadata(ContentEntity entity, Content content, Serializable instance) {
		entity.copyFrom(content);
		// fill some defaults if not passed with the Content object
		if (entity.getCharset() == null) {
			entity.setCharset(StandardCharsets.UTF_8.name());
		}
		// we may need to remove this here but they are here for now for backup and backward compatibility
		if (entity.getName() == null) {
			entity.setName(getStringProperty(instance, NAME));
		}
		entity.setMimeType(detectMimeType(entity, content, instance));
	}

	/** Creates new entity and set version for the content. */
	private ContentEntity createNewEntity(int version) {
		ContentEntity entity = new ContentEntity();
		idManager.generateStringId(entity, true);
		entity.setVersion(version);
		return entity;
	}

	private static String getStringProperty(Serializable instance, String propertyName) {
		if (instance instanceof Properties) {
			return ((Properties) instance).getString(propertyName);
		}
		return null;
	}

	protected String detectMimeType(ContentEntity entity, Content content, Serializable instance) {
		String mimeType = entity.getMimeType();
		if (isNotValidMimeType(mimeType)) {
			mimeType = getStringProperty(instance, MIMETYPE);
		}

		// Try to resolve the mime type from the content.
		if (isNotValidMimeType(mimeType) || content.shouldDetectedMimeTypeFromContent()) {
			mimeType = detectMimeTypeFromContent(content);
		}

		// If for some reason mime type resolving fails at this point we try to use initial value.
		if (isNotValidMimeType(mimeType)) {
			mimeType = entity.getMimeType();
		}

		// if we still don't have a valid mime, detection failed and we return a default value.
		return getOrDefault(mimeType, MediaType.APPLICATION_OCTET_STREAM);
	}

	protected String detectMimeTypeFromContent(Content content) {
		// Try to resolve the mime type from the file name.
		String mimeType = mimeTypeResolver.resolveFromName(content.getName());
		// if resolving from file name fails or mime type detection from content is set use the stream to calculate.
		if (isNotValidMimeType(mimeType) || content.shouldDetectedMimeTypeFromContent()) {
			mimeType = mimeTypeResolver.getMimeType(new BufferedInputStream(content.getContent().getInputStream()));
		}
		return getOrDefault(mimeType, MediaType.APPLICATION_OCTET_STREAM);
	}

	protected static boolean isNotValidMimeType(String mimeType) {
		return StringUtils.isNullOrEmpty(mimeType) || INVALID_MIMETYPES.contains(mimeType);
	}

	/**
	 * Gets the entity for the given id and type
	 *
	 * @param instanceId
	 *            the instance id
	 * @param type
	 *            the type
	 * @return the entity
	 */
	public ContentEntity getEntity(Serializable instanceId, String type) {
		if (instanceId == null && type == null) {
			return null;
		}
		List<ContentEntity> list = dbDao.fetchWithNamed(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY,
				Arrays.asList(new Pair<>("id", instanceId), new Pair<>("purpose", type)));
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * Persist entity.
	 *
	 * @param entity
	 *            the entity
	 * @param isNew
	 *            the is new
	 */
	public void persistEntity(ContentEntity entity, boolean isNew) {
		dbDao.saveOrUpdate(entity);
		if (isNew) {
			idManager.unregister(entity);
		}
	}

	/**
	 * Gets the content for the given content or instance ids.
	 *
	 * @param ids
	 *            the ids
	 * @param type
	 *            the type
	 * @return the content for ids
	 */
	public List<ContentEntity> getContentForIds(Set<Serializable> ids, String type) {
		// if passed null we can only fetch content ids so any value here is valid
		String contentType = type == null ? "ANY" : type;

		return dbDao.fetchWithNamed(ContentEntity.QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE_KEY,
				Arrays.asList(new Pair<>("id", ids), new Pair<>("purpose", contentType)));
	}

}