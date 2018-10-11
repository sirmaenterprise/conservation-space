package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.RelationalDb;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.relation.AbstractLinkService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.DefaultPrimaryStateTypeImpl;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Default service for {@link LinkService}. The service allows creation and retrieval of links of various types for
 * source and target.
 * <p>
 * TODO: added removing of properties when deleting links!
 *
 * @author BBonev
 */
@ApplicationScoped
@RelationalDb
public class LinkServiceImpl extends AbstractLinkService {

	private static final PrimaryStates IN_PROGRESS = new DefaultPrimaryStateTypeImpl(PrimaryStates.IN_PROGRESS_KEY);

	private static final long serialVersionUID = 968290722625442284L;

	private static final Set<String> ALL_LINKS = new HashSet<>(1, 1f);

	private static final String NO_LINK = "$NO_LINK$";

	@Inject
	private TypeConverter converter;

	@Inject
	private DbDao dbDao;

	@Inject
	private PropertiesService propertiesService;

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkServiceImpl.class);

	@Inject
	private StateService stateService;

	@Inject
	private DatabaseIdManager idManager;

	/**
	 * Link internal. Links from instance to instance using the given primary and reverse link ids if present and the
	 * given properties for both.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverseId
	 *            the reverse id
	 * @param properties
	 *            the properties
	 * @return true, if successful
	 */
	@Override
	protected Pair<Serializable, Serializable> linkInternal(Object from, Object to, String linkId, String reverseId,
			Map<String, Serializable> properties) {
		if (StringUtils.isBlank(linkId) || from == null || to == null) {
			return Pair.nullPair();
		}
		Map<String, Serializable> localProps = properties;
		if (localProps == null) {
			localProps = new HashMap<>(1);
		}

		Pair<Serializable, Serializable> ids = new Pair<>(null, null);
		try {
			// remove any other link if exists
			unlinkInternal(from, to, linkId, reverseId);
			// enable custom configuration
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();

			// first we save the forward direction
			InstanceReference fromRef = convertToReference(from);
			InstanceReference toRef = convertToReference(to);
			LinkEntity entity = createLinkEntity(fromRef, toRef, linkId, true);
			entity = dbDao.saveOrUpdate(entity);
			ids.setFirst(entity.getId());
			// if the properties a missing then no need to create instance only to save non
			// existing properties
			if (!localProps.isEmpty()) {
				propertiesService.saveProperties(createInstance(fromRef, toRef, entity, localProps));
			}

			// then if needed save the reverse direction
			if (StringUtils.isNotBlank(reverseId)) {
				LinkEntity reverseEntity = createLinkEntity(toRef, fromRef, reverseId, false);
				// update reverse link
				reverseEntity.setReverse(entity.getId());
				reverseEntity = dbDao.saveOrUpdate(reverseEntity);
				// update the reverse link
				entity.setReverse(reverseEntity.getId());
				ids.setSecond(reverseEntity.getId());
				if (!localProps.isEmpty()) {
					propertiesService.saveProperties(createInstance(toRef, fromRef, reverseEntity, localProps));
				}
			}
		} finally {
			// clear the configuration
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
		}
		return ids;
	}

	/**
	 * Creates the link entity.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param primary
	 *            the bidirectional
	 * @return the link entity
	 */
	private LinkEntity createLinkEntity(InstanceReference from, InstanceReference to, String linkId, boolean primary) {
		LinkEntity entity = new LinkEntity();
		idManager.generateStringId(entity, false);
		entity.setPrimary(primary);
		entity.setIdentifier(linkId);
		entity.setFrom((LinkSourceId) from);
		entity.setTo((LinkSourceId) to);
		return entity;
	}

	/**
	 * Convert to link source.
	 *
	 * @param instance
	 *            the instance
	 * @return the link source id
	 */
	private InstanceReference convertToReference(Object instance) {
		if (instance instanceof InstanceReference) {
			return (InstanceReference) instance;
		} else if (instance instanceof Instance) {
			return ((Instance) instance).toReference();
		}
		// fail safe of other undefined cases
		return converter.convert(InstanceReference.class, instance);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		if (from == null || linkIds == null || linkIds.isEmpty()) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, null, null, linkIds, true);
	}

	/**
	 * Gets the links internal.
	 *
	 * @param from
	 *            the beginning of the link
	 * @param to
	 *            the end of the link
	 * @param linkId
	 *            the link id
	 * @param linkIds
	 *            the link ids
	 * @param loadProperties
	 *            the load properties
	 * @return the links internal
	 */
	private List<LinkReference> getLinksInternal(Object from, Object to, String linkId, Collection<String> linkIds,
			boolean loadProperties) {
		InstanceReference fromRef = convertToReference(from);
		InstanceReference toRef = convertToReference(to);

		Collection<String> links = new LinkedList<>();
		if (linkIds != null) {
			links.addAll(linkIds);
		}
		if (linkId != null) {
			links.add(linkId);
		}
		List<Pair<String, Object>> args = new ArrayList<>(5);
		// fetch all links for the given source
		String query = null;
		if (!links.isEmpty()) {
			args.add(new Pair<>("identifier", links));
		}
		if (fromRef != null) {
			query = links.isEmpty() ? LinkEntity.QUERY_LINK_BY_SRC_KEY : LinkEntity.QUERY_LINK_BY_SRC_AND_IDS_KEY;
			args.add(new Pair<>("fromId", fromRef.getId()));
			args.add(new Pair<>("fromType", fromRef.getReferenceType().getId()));
		}
		if (toRef != null) {
			query = links.isEmpty() ? LinkEntity.QUERY_LINK_BY_TARGET_KEY : LinkEntity.QUERY_LINK_BY_TARGET_AND_IDS_KEY;
			args.add(new Pair<>("toId", toRef.getId()));
			args.add(new Pair<>("toType", toRef.getReferenceType().getId()));
		}
		if (fromRef != null && toRef != null) {
			query = links.isEmpty() ? LinkEntity.QUERY_LINK_BY_TARGET_AND_SOURCE_KEY
					: LinkEntity.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY;
		}
		if (query == null) {
			return Collections.emptyList();
		}
		List<LinkEntity> list = dbDao.fetchWithNamed(query, args);

		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<LinkReference> linkInstances = new ArrayList<>(list.size());
		// load and convert link entities to instances and group them by linkId and link target
		// types
		for (LinkEntity linkEntity : list) {
			LinkReference linkInstance = convertToReference(linkEntity);
			if (linkInstance == null) {
				// we have invalid To link end, nothing to do
				continue;
			}
			linkInstances.add(linkInstance);
		}
		if (loadProperties) {
			// load properties for all links
			propertiesService.loadProperties(linkInstances);
			// update the state if missing
			String state = stateService.getState(IN_PROGRESS, ObjectTypes.LINK_REFERENCE);
			for (LinkReference linkReference : linkInstances) {
				if (linkReference.getProperties() != null
						&& !linkReference.getProperties().containsKey(DefaultProperties.STATUS)) {
					linkReference.getProperties().put(DefaultProperties.STATUS, state);
				}
			}
		}
		return linkInstances;
	}

	/**
	 * Convert the given entity to link instance and adds it the the given ma * ng if *
	 *
	 * @param linkEntity
	 *            the link entity to convert
	 * @return the link instance
	 */
	private static LinkReference convertToReference(LinkEntity linkEntity) {
		LinkReference instance = new LinkReference();
		instance.setPrimary(linkEntity.getPrimary());
		instance.setId(linkEntity.getId());
		instance.setIdentifier(linkEntity.getIdentifier());
		instance.setReverse(linkEntity.getReverse());
		instance.setFrom(linkEntity.getFrom());
		instance.setTo(linkEntity.getTo());
		return instance;
	}

	/**
	 * Creates a {@link LinkInstance} using the given from and to arguments * enti * .
	 *
	 * @param from
	 *            the from from element to set, no need to convert it from DB model
	 * @param to
	 *            the to
	 * @param linkEntity
	 *            the link entity to convert
	 * @param properties
	 *            the properties
	 * @return the link instance
	 */
	private static LinkReference createInstance(InstanceReference from, InstanceReference to, LinkEntity linkEntity,
			Map<String, Serializable> properties) {
		LinkReference instance = new LinkReference();
		instance.setPrimary(linkEntity.getPrimary());
		instance.setId(linkEntity.getId());
		instance.setIdentifier(linkEntity.getIdentifier());
		instance.setFrom(from);
		instance.setTo(to);
		if (properties != null) {
			instance.setProperties(properties);
		}
		return instance;
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverse) {
		if (from == null || to == null) {
			return false;
		}
		String firstLink = StringUtils.isBlank(linkId) ? NO_LINK : linkId;
		String secondLink = StringUtils.isBlank(reverse) ? NO_LINK : reverse;
		return unlinkInternal(from, to, firstLink, secondLink);
	}

	/**
	 * Unlink internal. Removes the link for the given source and target link ends. If the linkId is not present then
	 * all links will be removed. If needed the reverse link can be removed also.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverse
	 *            the reverse
	 * @return true, if removed something
	 */
	private boolean unlinkInternal(Object from, Object to, String linkId, String reverse) {
		InstanceReference fromId = convertToReference(from);
		InstanceReference toId = convertToReference(to);

		int unlinked = unlinkInternal(fromId, toId, linkId);
		// if source and destination are the same instance, no need to call it again. it was handled
		// in the previous call
		if (StringUtils.isNotBlank(reverse) && fromId != null && !fromId.equals(toId)) {
			unlinked += unlinkInternal(toId, fromId, reverse);
		}
		LOGGER.debug("Removed {} links for: from {} to {} with type {}|{}", unlinked, fromId, toId, linkId, reverse);
		return unlinked > 0;
	}

	/**
	 * Unlink internal. Executes the queries for unlinking. If linkId is not present then all links will be removed that
	 * starts from the first id and point to the second id
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param linkId
	 *            the link id
	 * @return the int
	 */
	private int unlinkInternal(InstanceReference fromId, InstanceReference toId, String linkId) {
		if (NO_LINK.equals(linkId)) {
			return 0;
		}
		List<LinkReference> loadedLinks = new LinkedList<>();
		if (fromId != null && fromId.equals(toId)) {
			List<LinkReference> list = getLinksInternal(fromId, null, linkId, null, false);
			loadedLinks.addAll(list);
			notifyForDeletedLinks(list);
			list = getLinksInternal(null, toId, linkId, null, false);
			loadedLinks.addAll(list);
			notifyForDeletedLinks(list);
		} else {
			List<LinkReference> list = getLinksInternal(fromId, toId, linkId, null, false);
			loadedLinks.addAll(list);
			notifyForDeletedLinks(list);
		}
		// update the cache

		for (LinkReference linkReference : loadedLinks) {
			// clear properties
			propertiesService.removeProperties(linkReference, linkReference);
			dbDao.delete(LinkEntity.class, linkReference.getId());
		}

		return loadedLinks.size();
	}

	/**
	 * Notify for deleted links.
	 *
	 * @param list
	 *            the list
	 */
	private void notifyForDeletedLinks(List<LinkReference> list) {
		for (LinkReference linkReference : list) {
			Serializable id = linkReference.getId();
			addDeletedLinkId(id);
		}
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		if (to == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(null, to, null, ALL_LINKS, true);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		if (to == null || StringUtils.isBlank(linkId)) {
			return CollectionUtils.emptyList();
		}
		return getLinksInternal(null, to, linkId, null, true);
	}

	@Override
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		boolean valid = true;
		for (String string : linkIds) {
			valid |= unlink(instance, instance, string, null);
		}
		return valid;
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		Pair<Serializable, Serializable> pair = link(from, to, encodeSimpleLink(linkId),
				encodeSimpleLink(getReverseLinkType(linkId)), Collections.emptyMap());
		return pair.getFirst() != null && pair.getSecond() != null;
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		if (reverseId == null) {
			return linkSimple(from, to, linkId);
		}
		Pair<Serializable, Serializable> pair = link(from, to, encodeSimpleLink(linkId), encodeSimpleLink(reverseId),
				Collections.emptyMap());
		return pair.getFirst() != null && pair.getSecond() != null;
	}

	/**
	 * Gets the reverse link type.
	 *
	 * @param relationType
	 *            the relation type
	 * @return the reverse link type
	 */
	@Override
	protected String getReverseLinkType(String relationType) {
		if (relationType.endsWith("partOf")) {
			return InstanceContextService.HAS_CHILD_URI;
		} else if (relationType.endsWith("hasChild")) {
			return InstanceContextService.PART_OF_URI;
		} else if (relationType.endsWith("processes")) {
			return LinkConstants.PROCESSED_BY;
		} else if (relationType.endsWith("processedBy")) {
			return LinkConstants.PROCESSES;
		}
		return relationType;
	}

	/**
	 * Encode simple link.
	 *
	 * @param id
	 *            the id
	 * @return the string
	 */
	private static String encodeSimpleLink(String id) {
		if (id == null) {
			return null;
		}
		return id + "-simple";
	}

	/**
	 * Decode simple link.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the string
	 */
	private static String decodeSimpleLink(String identifier) {
		if (identifier == null) {
			return null;
		}
		if (identifier.endsWith("-simple")) {
			return identifier.substring(0, identifier.length() - 7);
		}
		return identifier;
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		List<LinkReference> links = getLinks(from, encodeSimpleLink(linkId));
		for (LinkReference reference : links) {
			reference.setIdentifier(decodeSimpleLink(reference.getIdentifier()));
		}
		return links;
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, Set<String> linkIds) {
		List<LinkReference> links = new LinkedList<>();
		for (String linkId : linkIds) {
			links.addAll(getSimpleLinks(from, linkId));
		}
		return links;
	}

	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		if (from == null) {
			return;
		}
		unlinkInternal(from, from, encodeSimpleLink(linkId), encodeSimpleLink(getReverseLinkType(linkId)));
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		unlinkInternal(from, to, encodeSimpleLink(linkId), encodeSimpleLink(getReverseLinkType(linkId)));
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		if (reverseId == null) {
			unlinkInternal(from, to, encodeSimpleLink(linkId), encodeSimpleLink(getReverseLinkType(linkId)));
		} else {
			unlinkInternal(from, to, encodeSimpleLink(linkId), encodeSimpleLink(reverseId));
		}
	}

	@Override
	protected List<LinkReference> getLinksInternal(Object from, Object to, Collection<String> linkids) {
		return getLinksInternal(from, to, null, linkids, true);
	}

	@Override
	protected String shrinkLinkIdentifier(String identifier) {
		// no need to do anything
		return identifier;
	}

	@Override
	protected String expandLinkIdentifier(String identifier) {
		return identifier;
	}

	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		List<LinkReference> linksTo = getLinksTo(to, linkId);
		for (LinkReference reference : linksTo) {
			reference.setIdentifier(decodeSimpleLink(reference.getIdentifier()));
		}
		return linksTo;
	}
}
