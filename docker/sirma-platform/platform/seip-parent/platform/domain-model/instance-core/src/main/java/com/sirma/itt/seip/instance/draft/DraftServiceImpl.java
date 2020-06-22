package com.sirma.itt.seip.instance.draft;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Concrete implementation of {@link DraftService}. Contains logic for storing, retrieving and deleting specific draft
 * form the DB.
 *
 * @author bbanchev
 * @author A. Kunchev
 */
@ApplicationScoped
public class DraftServiceImpl implements DraftService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String DRAFT_PURPOSE_PREFIX = "draft-";

	private static final int DELAY_BEFORE_DRAFT_DELETE = 1;

	@Inject
	private DbDao dbDao;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private InstanceContentService instanceContentService;

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

		DraftEntity draftEntity = fetchWithNamed.get(0);

		ContentInfo contentInfo = instanceContentService.getContent(draftEntity.getContentId(), null);
		if (!contentInfo.exists()) {
			LOGGER.warn("Missing draft content: instance id - [{}], content id - [{}] user - [{}].", instanceId,
					draftEntity.getContentId(), user);
			return null;
		}

		return toDraftInstance(draftEntity);
	}

	@Override
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
		Content content = Content
				.createEmpty()
					.setContent(contentAsString, StandardCharsets.UTF_8)
					.setMimeType(MediaType.TEXT_HTML)
					.setName(UUID.randomUUID() + "-instanceDraft.html")
					.setPurpose(DRAFT_PURPOSE_PREFIX + createdEntity.getId().getUserId())
					.setView(true); // enables sanitizing

		// passed just id to force storing in localStore instead of alfresco
		ContentInfo contentInfo = instanceContentService.saveContent(instanceId, content);
		if (!contentInfo.exists()) {
			throw new EmfRuntimeException("Failed to save the content for instance with id: " + instanceId);
		}

		createdEntity.setContentId(contentInfo.getContentId());
	}

	@Override
	public Collection<DraftInstance> delete(String instanceId) {
		if (StringUtils.isBlank(instanceId)) {
			return Collections.emptyList();
		}

		return fetch(instanceId, null)
				.stream()
					.filter(Objects::nonNull)
					.map(this::deleteInternal)
					.collect(Collectors.toList());
	}

	@Override
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
		if (entity == null) {
			return null;
		}
		DraftInstance converted = toDraftInstance(entity);
		DraftEntityId entityId = entity.getId();
		if (dbDao.delete(DraftEntity.class, entityId) != 0) {
			String purpose = DRAFT_PURPOSE_PREFIX + entityId.getUserId();
			instanceContentService.deleteContent(entityId.getInstanceId(), purpose, DELAY_BEFORE_DRAFT_DELETE, SECONDS);
			return converted;
		}

		return null;
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
