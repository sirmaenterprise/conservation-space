package com.sirma.sep.content;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.io.BufferedInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Properties;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.batch.ContentInfoMatcher;
import com.sirma.sep.content.type.MimeTypeResolver;

/**
 * DAO for loading and persisting {@link ContentEntity}s.
 *
 * @author BBonev
 */
@Singleton
public class ContentEntityDao {
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
	private final TransactionSupport transactionSupport;

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
			IdResolver idResolver, ContentDigestProvider digestProvider, TransactionSupport transactionSupport) {
		this.idManager = idManager;
		this.mimeTypeResolver = mimeTypeResolver;
		this.dbDao = dbDao;
		resolver = idResolver;
		contentDigestProvider = digestProvider;
		this.transactionSupport = transactionSupport;
	}

	/**
	 * Assign content to instance by setting the given instance id to the given content id if not already set
	 *
	 * @param contentId
	 *            the content id
	 * @param instanceId
	 *            the instance id
	 * @param version
	 *            the version of the content
	 * @return true, if successful
	 */
	boolean assignContentToInstance(String contentId, Serializable instanceId, int version) {
		return dbDao.executeUpdate(ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY,
				Arrays.asList(new Pair<>("instanceId", instanceId), new Pair<>("id", contentId),
						new Pair<>("version", version))) == 1;
	}

	/**
	 * Gets the or create import entity.
	 *
	 * @param contentImport
	 *            the content import
	 * @return the or create import entity
	 */
	ContentEntity importEntity(ContentImport contentImport) {
		// versionable forces to import new record, not to override/update existent one
		ContentEntity entity = getOrCreateEntity(contentImport.getInstanceId(), contentImport.setVersionable(true));
		entity.merge(contentImport);
		if (entity.getMimeType() == null) {
			if (contentImport.getContent() != null) {
				entity.setMimeType(detectMimeTypeFromContent(contentImport));
			} else {
				entity.setMimeType(MediaType.APPLICATION_OCTET_STREAM);
			}
		}

		persistEntity(entity, false);
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
	List<ContentEntity> getContentsForInstance(Serializable id, Collection<String> contentsToSkip) {
		if (StringUtils.isBlank(Objects.toString(id, null))) {
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
	ContentEntity getOrCreateEntity(Serializable instance, Content content) {
		Serializable instanceId = resolver.resolve(instance).orElse(null);
		ContentEntity entity = getEntity(instanceId, content.getPurpose());
		String contentId = content.getContentId();
		if (entity == null) {
			entity = createNewEntity(0, contentId);
		} else if (content.isVersionable()) {
			int incrementedVersion = Math.incrementExact(entity.getVersion());
			String oldRemoteSource = entity.getRemoteSourceName();
			entity = createNewEntity(incrementedVersion, contentId);
			// copy the remote source name so that the new version is uploaded in the same store without any
			// additional checks
			if (content.isContentStoreEnforcedOnVersionUpdate()) {
				entity.setRemoteSourceName(oldRemoteSource);
			}
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
	ContentEntity getUniqueContent(Serializable instance, Content content) {
		String digest = contentDigestProvider.digest(content);
		ContentEntity entity = getEntityByChecksum(digest);
		if (entity == null) {
			entity = createNewEntity(0, content.getContentId());
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
	ContentEntity getEntityByChecksum(String digest) {
		if (StringUtils.isBlank(digest)) {
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

	/**
	 * Creates new entity and set version for the content. If content id was preset, it will be used instead of
	 * generating new one.
	 */
	private ContentEntity createNewEntity(int version, String contentId) {
		ContentEntity entity = new ContentEntity();
		setContentId(contentId, entity);
		entity.setVersion(version);
		return entity;
	}

	private void setContentId(String contentId, ContentEntity entity) {
		if (StringUtils.isBlank(contentId)) {
			idManager.generateStringId(entity, true);
		} else {
			entity.setId(contentId);
			idManager.register(entity);
		}
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
			// reset the mime type, if it is some of the invalid ones, but there is no content so we could get default
			mimeType = null;
			if (content.getContent() != null) {
				mimeType = mimeTypeResolver.getMimeType(new BufferedInputStream(content.getContent().getInputStream()),
						content.getName());
			}
		}
		return getOrDefault(mimeType, MediaType.APPLICATION_OCTET_STREAM);
	}

	protected static boolean isNotValidMimeType(String mimeType) {
		return StringUtils.isBlank(mimeType) || INVALID_MIMETYPES.contains(mimeType);
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
	void persistEntity(ContentEntity entity, boolean isNew) {
		dbDao.saveOrUpdate(entity);
		if (isNew) {
			idManager.unregister(entity);
		}
	}

	/**
	 * Update existing entity in transaction if not already started. Mainly used when the update happens during
	 * read operations like metadata retrieval.
	 *
	 * @param entity
	 *            the entity
	 */
	void updateEntityInTx(ContentEntity entity) {
		transactionSupport.invokeInNewTx(() -> dbDao.saveOrUpdate(entity));
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
	List<ContentEntity> getContentForIds(Set<Serializable> ids, String type) {
		// if passed null we can only fetch content ids so any value here is valid
		String contentType = type == null ? "ANY" : type;

		return dbDao.fetchWithNamed(ContentEntity.QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE_KEY,
				Arrays.asList(new Pair<>("id", ids), new Pair<>("purpose", contentType)));
	}

	/**
	 * Retrieve part of the store contents. The entries will be returned ordered based on their insertion order.
	 *
	 * @param storeName the store name to retrieve
	 * @param offset the number of items to skip from the first added item in the store
	 * @param page the number of items to return
	 * @return the loaded entities
	 */
	List<ContentEntity> getStoreContents(String storeName, int offset, int page) {
		Objects.requireNonNull(storeName, "Store name is required parameter");

		return dbDao.fetchWithNamed(ContentEntity.QUERY_CONTENTS_BY_STORE_NAME_KEY,
				Collections.singletonList(new Pair<>("storeName", storeName)), offset, page);
	}

	/**
	 * Fetch entities that use the given content identified by the given remote id from the given store
	 *
	 * @param remoteSourceName the store name
	 * @param remoteId the remote id from the given store
	 * @return the list of entities that reuse the content
	 */
	public List<ContentEntity> getEntityByRemoteId(String remoteSourceName, String remoteId) {
		Objects.requireNonNull(remoteSourceName, "Store name is required parameter");
		Objects.requireNonNull(remoteId, "Remote id is required parameter");

		return dbDao.fetchWithNamed(ContentEntity.QUERY_CONTENTS_BY_STORE_NAME_AND_REMOTE_ID_KEY,
				Arrays.asList(new Pair<>("storeName", remoteSourceName), new Pair<>("remoteId", remoteId)));
	}

	/**
	 * Query content identifiers that match the given content selector
	 *
	 * @param contentSelector the content selector that carry content query information.
	 * @return a collection if found content identifiers
	 */
	public Collection<String> getContentIdBy(ContentInfoMatcher contentSelector) {
		// TODO: the query is limited by the number of arguments passed. If it exceeds Short.MAX_VALUE it will have problems
		// we should add checks to limit the allowed searched instances/contents or handle it, but it's too custom.
		// for now this should do

		List<Pair<String, Object>> args = new ArrayList<>();
		String query = "select id from ContentEntity";

		String purposePredicate = createPredicate("purpose", contentSelector.getPurpose(), args);
		String storePredicate = createPredicate("remoteSourceName", contentSelector.getStoreName(), args);
		String contentIds = createPredicate("id", contentSelector.getContentIds(), args);
		String instanceIds = createPredicate("instanceId", contentSelector.getInstanceIds(), args);
		String instancePattern = createPredicate("instanceId", contentSelector.getInstanceIdPattern(), args);

		query += appendQueryPredicates(purposePredicate, storePredicate, contentIds, instanceIds, instancePattern);

		return dbDao.fetch(query, args);
	}

	private String appendQueryPredicates(String... predicates) {
		if (predicates == null || predicates.length == 0) {
			return "";
		}
		String query = Arrays.stream(predicates).filter(Objects::nonNull).collect(Collectors.joining(" and "));
		if (StringUtils.isBlank(query)) {
			return query;
		}
		return " where " + query;
	}

	private String createPredicate(String propertyName, Object selector, List<Pair<String, Object>> args) {
		if (selector == null || (selector instanceof Collection && ((Collection) selector).isEmpty())) {
			return null;
		}
		String operator = " = ";
		String variableName = ":" + propertyName;
		if (isWildcard(selector)) {
			operator = " like ";
		} else if (selector instanceof Collection) {
			operator = " in ";
			variableName = "(:" + propertyName + ")";
		}
		args.add(new Pair<>(propertyName, selector));
		return propertyName + operator + variableName;
	}

	private boolean isWildcard(Object value) {
		return value instanceof String && (value.toString().contains("%") || value.toString().contains("?"));
	}
}
