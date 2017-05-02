package com.sirma.itt.seip.instance.draft;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Concrete implementation of {@link DraftService}. Contains logic for storing, retrieving and deleting specific draft
 * form the DB.
 *
 * @author bbanchev
 * @author A. Kunchev
 */
@ApplicationScoped
public class DraftServiceImpl implements DraftService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DraftServiceImpl.class);

	@Inject
	private DbDao dbDao;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private IdocSanitizer idocSanitizer;

	@Override
	public DraftInstance getDraft(String instanceId, String userId) {
		String user = getUserIfAbsent(userId);
		if (StringUtils.isBlank(instanceId) || StringUtils.isBlank(user)) {
			return null;
		}

		List<DraftEntity> fetchWithNamed = fetch(instanceId, user);
		if (fetchWithNamed.isEmpty()) {
			return null;
		}

		return toDraftInstance(fetchWithNamed.get(0));
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public DraftInstance create(String instanceId, String userId, String content) {
		String user = getUserIfAbsent(userId);
		if (StringUtils.isBlank(instanceId) || StringUtils.isBlank(user)) {
			LOGGER.debug("Missing required argument: instance id - [{}] or user - [{}].", instanceId, user);
			return null;
		}

		DraftEntity createdEntity = createEntity(instanceId, user);
		setDraftContent(createdEntity, content);
		createdEntity.setCreated(new Date());
		DraftEntity saved = dbDao.saveOrUpdate(createdEntity);
		return toDraftInstance(saved);
	}

	private void setDraftContent(DraftEntity createdEntity, String contentAsString) {
		String instanceId = createdEntity.getId().getInstanceId();
		String sanitizedContent = idocSanitizer.sanitize(contentAsString);
		Content content = Content
				.createEmpty()
					.setContent(sanitizedContent, StandardCharsets.UTF_8)
					.setMimeType(MediaType.TEXT_HTML)
					.setName(UUID.randomUUID() + "-instanceDraft.html")
					.setPurpose("draft-" + createdEntity.getId().getUserId());

		// dummy instance just to avoid real instance loading
		Instance instance = new EmfInstance();
		instance.setId(instanceId);
		ContentInfo contentInfo = instanceContentService.saveContent(instance, content);
		if (contentInfo == null) {
			throw new EmfRuntimeException("Failed to save the content for instance with id: " + instanceId);
		}

		createdEntity.setContentId(contentInfo.getContentId());
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public Collection<DraftInstance> delete(String instanceId) {
		if (StringUtils.isBlank(instanceId)) {
			return Collections.emptyList();
		}

		List<DraftEntity> drafts = fetch(instanceId, null);
		List<DraftInstance> deleted = new ArrayList<>(drafts.size());
		for (DraftEntity entity : drafts) {
			DraftInstance draftInstance = deleteInternal(entity);
			deleted.add(draftInstance);
		}

		return deleted;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public DraftInstance delete(String instanceId, String userId) {
		String user = getUserIfAbsent(userId);
		if (StringUtils.isBlank(instanceId) || StringUtils.isBlank(user)) {
			throw new EmfRuntimeException("Missing required argument for draft deletion!");
		}

		List<DraftEntity> draftEntities = fetch(instanceId, user);
		if (draftEntities.isEmpty()) {
			return null;
		}

		return deleteInternal(draftEntities.get(0));
	}

	private DraftInstance deleteInternal(DraftEntity entity) {
		DraftInstance converted = toDraftInstance(entity);
		dbDao.delete(DraftEntity.class, entity.getId());
		instanceContentService.deleteContent(entity.getContentId(), null);
		return converted;
	}

	/**
	 * If the passed id is blank, returns authenticated user id.
	 */
	private String getUserIfAbsent(String userId) {
		if (StringUtils.isBlank(userId)) {
			return (String) securityContext.getAuthenticated().getSystemId();
		}

		return userId;
	}

	/**
	 * Convert a draft entity to a {@link DraftInstance}.
	 *
	 * @param draftEntity
	 *            entity to convert
	 * @return created {@link DraftInstance} or <code>null</code>, if the passed entity is <code>null</code>
	 */
	private static DraftInstance toDraftInstance(DraftEntity draftEntity) {
		if (draftEntity == null) {
			return null;
		}

		DraftInstance draftInstance = new DraftInstance();
		draftInstance.setDraftContentId(draftEntity.getContentId());
		draftInstance.setInstanceId(draftEntity.getId().getInstanceId());
		draftInstance.setCreator(draftEntity.getId().getUserId());
		draftInstance.setCreatedOn(draftEntity.getCreated());
		return draftInstance;
	}

	/**
	 * Fetch the draft instances from db/cache and return them as list. If user is null all drafts for the given
	 * instance are returned.
	 *
	 * @param id
	 *            the id of the instance which draft will be fetched
	 * @param userId
	 *            the id of the user for which draft will be fetched
	 * @return the list of {@link DraftEntity}
	 */
	private List<DraftEntity> fetch(String id, String userId) {
		List<Pair<String, Object>> params = new ArrayList<>();
		params.add(new Pair<String, Object>("uri", id));
		String query = DraftEntity.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_KEY;
		if (userId != null) {
			params.add(new Pair<String, Object>("userId", userId));
			query = DraftEntity.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER_KEY;
		}

		return dbDao.fetchWithNamed(query, params);
	}

	private static DraftEntity createEntity(String instanceId, String userId) {
		DraftEntityId entityId = new DraftEntityId();
		entityId.setUserId(userId);
		entityId.setInstanceId(instanceId);

		DraftEntity draftEntity = new DraftEntity();
		draftEntity.setId(entityId);
		return draftEntity;
	}

}
