package com.sirma.itt.cmf.services.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.entity.DraftEntity;
import com.sirma.itt.cmf.beans.entity.DraftEntityId;
import com.sirma.itt.cmf.beans.model.DraftInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * The draft service impl of {@link DraftService}
 *
 * @author bbanchev
 */
@Stateless
public class DraftServiceImpl implements DraftService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DraftServiceImpl.class);

	/** The Constant VERSIONABLE_PROPERTIES. */
	private static final Set<String> VERSIONABLE_PROPERTIES = new HashSet<String>();
	static {
		// TODO from config
		VERSIONABLE_PROPERTIES.add(DocumentProperties.TITLE);
		VERSIONABLE_PROPERTIES.add(DocumentProperties.NAME);
	}

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DraftInstance> getDrafts(Instance instance) {
		List<DraftEntity> fetchWithNamed = fetch(instance, null);
		List<DraftInstance> instances = new ArrayList<>(fetchWithNamed.size());
		for (DraftEntity draftEntry : fetchWithNamed) {
			instances.add(convert(draftEntry, null));
		}
		return instances;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DraftInstance getDraft(Instance instance, User user) {
		List<DraftEntity> fetchWithNamed = fetch(instance, user);
		if (fetchWithNamed.size() > 0) {
			return convert(fetchWithNamed.get(0), user);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DraftInstance create(Instance instance, User user) {
		if ((instance == null) || (instance.getId() == null) || (user == null)
				|| (user.getId() == null)) {
			return null;
		}
		// List<DraftEntity> fetch = fetch(instance, user);
		// if (fetch != null && fetch.size() == 1) {
		// return convert(fetch.get(0), user);
		// }
		DraftEntity createdEntity = createEntity(instance, user);
		// store only the needed props
		Map<String, Serializable> properties = new HashMap<>(instance.getProperties());
		properties.keySet().retainAll(versionableProperties());
		JSONObject jsonProperties = new JSONObject(properties);
		createdEntity.setProperties(jsonProperties.toString());
		// get the content
		if (instance.getProperties().containsKey(DocumentProperties.CONTENT)) {
			Serializable saved = instance.getProperties().get(DocumentProperties.CONTENT);
			if (saved instanceof String) {
				createdEntity.setContent(saved.toString());
			}
		} else if (instance.getProperties().containsKey(DocumentProperties.FILE_LOCATOR)) {
			// download the data
			Serializable saved = instance.getProperties().get(DocumentProperties.FILE_LOCATOR);
			if (saved instanceof FileDescriptor) {
				try {
					InputStream download = ((FileDescriptor) saved).getInputStream();
					byte[] byteArray = IOUtils.toByteArray(download);
					createdEntity.setContent(new String(byteArray));
					byteArray = null;
				} catch (Exception e) {
					LOGGER.error("Error during draft creation - content is not extracted!", e);
				}
			}
		}
		// save the draft entry with current date.
		createdEntity.setCreated(new Date());
		DraftEntity saved = dbDao.saveOrUpdate(createdEntity);
		return convert(saved, user);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DraftInstance> delete(Instance instance) {
		List<DraftEntity> drafts = fetch(instance, null);
		List<DraftInstance> deleted = new ArrayList<>(drafts.size());
		for (DraftEntity draftEntity : drafts) {
			deleted.add(convert(draftEntity, null));
			dbDao.delete(DraftEntity.class, draftEntity.getId());
		}
		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DraftInstance delete(Instance instance, User user) {
		if (user == null) {
			throw new EmfRuntimeException("Missing required argument for draft creator!");
		}
		List<DraftEntity> draftEntities = fetch(instance, user);
		if (draftEntities.size() == 1) {
			DraftEntity draftEntity = draftEntities.get(0);
			DraftInstance converted = convert(draftEntity, user);
			dbDao.delete(DraftEntity.class, draftEntity.getId());
			return converted;
		}
		return null;
	}

	/**
	 * Convert a draft entity to a {@link DraftInstance} with the needed properties set
	 *
	 * @param draftEntity
	 *            the draft entity
	 * @param user
	 *            the user provided as performance optimization. If null the user is retrieved from
	 *            the entity
	 * @return the draft instance converted
	 */
	private DraftInstance convert(DraftEntity draftEntity, User user) {
		try {
			DraftInstance draftInstance = new DraftInstance();
			draftInstance.setCreatedOn(draftEntity.getCreated());
			draftInstance.setDraftContent(draftEntity.getContent());

			JSONObject jsonObject = JsonUtil.toJsonObject(draftEntity.getProperties());
			Map<String, Serializable> converted = new HashMap<>(jsonObject.length());
			Iterator<?> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String key = keys.next().toString();
				Object value = jsonObject.get(key);
				if (value instanceof Serializable) {
					converted.put(key.toString(), (Serializable) value);
				}
			}
			draftInstance.setDraftProperties(converted);
			if (user != null) {
				draftInstance.setCreator(user);
			} else {
				draftInstance.setCreator((User) resourceService.getResource(draftEntity.getId()
						.getUserId()));
			}
			draftInstance.setInstanceId(draftEntity.getId().getInstanceId());
			return draftInstance;
		} catch (Exception e) {
			LOGGER.error("Error during draft retrieve!", e);
		}
		return null;
	}

	/**
	 * Fetch the draft instances from db/cache and return them as list. If user is null all drafts
	 * for the given instance are returned.
	 *
	 * @param instance
	 *            the instance to fetch for
	 * @param user
	 *            the user to fetch for. Might be null
	 * @return the list of {@link DraftEntity}
	 */
	private List<DraftEntity> fetch(Instance instance, User user) {
		List<Pair<String, Object>> params = new ArrayList<>(user == null ? 1 : 2);
		params.add(new Pair<String, Object>("uri", instance.getId()));
		if (user != null) {
			params.add(new Pair<String, Object>("userId", user.getId()));
			return dbDao.fetchWithNamed(
					DbQueryTemplates.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER_KEY, params);
		} else {
			return dbDao.fetchWithNamed(DbQueryTemplates.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_KEY,
					params);
		}
	}

	/**
	 * Creates the entity internally and populates the needed properties.
	 *
	 * @param instance
	 *            the instance
	 * @param user
	 *            the user
	 * @return the draft entity created
	 */
	private DraftEntity createEntity(Instance instance, User user) {
		DraftEntity draftEntity = new DraftEntity();
		draftEntity.setId(new DraftEntityId());
		draftEntity.getId().setUserId(user.getId().toString());
		draftEntity.getId().setInstanceId(instance.getId().toString());
		return draftEntity;
	}

	/**
	 * Versionable properties to store in the draft
	 *
	 * @return the collection
	 */
	private Collection<String> versionableProperties() {
		return VERSIONABLE_PROPERTIES;
	}
	//
	// /**
	// * On delete document event handling. Remove all drafts.
	// *
	// * @param event
	// * the event
	// */
	// public void onDeleteDocument(@Observes BeforeDocumentDeleteEvent event) {
	// delete(event.getInstance());
	// }

}
