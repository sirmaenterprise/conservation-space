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

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.state.DefaultPrimaryStateTypeImpl;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;

/**
 * Default service for {@link LinkService}. The service allows creation and retrieval of links of
 * various types for source and target.
 * <p>
 * TODO: added removing of properties when deleting links!
 *
 * @author BBonev
 */
@Stateless
@RelationalDb
public class LinkServiceImpl extends AbstractLinkService {

	private static final PrimaryStateType IN_PROGRESS = new DefaultPrimaryStateTypeImpl(
			PrimaryStateType.IN_PROGRESS);
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 968290722625442284L;
	/** The Constant ALL_LINKS. */
	private static final Set<String> ALL_LINKS = new HashSet<String>(1, 1f);

	/** The Constant NO_LINK. */
	private static final String NO_LINK = "$NO_LINK$";

	/** The Constant LINK_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 10000), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store linked objects with a key link type/from instance reference. <br>Minimal value expression: workingInstances*averageLinkTypesPerInstance"))
	public static final String LINK_ENTITY_CACHE = "LINK_ENTITY_CACHE";

	/** The Constant LINK_ENTITY_FULL_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 2000), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the link objects only by instance type ids. The cache will store only the different types of links and not the links itself. <br>Minimal value expression: workingInstances"))
	public static final String LINK_ENTITY_FULL_CACHE = "LINK_ENTITY_FULL_CACHE";
	/** The converter. */
	@Inject
	private TypeConverter converter;
	/** The db dao. */
	@Inject
	private DbDao dbDao;
	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	/** The logger. */
	@Inject
	private Logger logger;
	/** The debug. */
	private boolean debug;

	/** The state service. */
	@Inject
	private StateService stateService;

	/**
	 * Initialize some viriables.
	 */
	@PostConstruct
	public void init() {
		debug = logger.isDebugEnabled();
	}

	/**
	 * Link internal. Links from instance to instance using the given primary and reverse link ids
	 * if present and the given properties for both.
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
	protected Pair<Serializable, Serializable> linkInternal(Object from, Object to, String linkId,
			String reverseId, Map<String, Serializable> properties) {
		if (StringUtils.isNullOrEmpty(linkId) || (from == null) || (to == null)) {
			return Pair.nullPair();
		}
		if (properties == null) {
			properties = new HashMap<String, Serializable>(1);
		}

		Pair<Serializable, Serializable> ids = new Pair<Serializable, Serializable>(null, null);
		try {
			// remove any other link if exists
			unlinkInternal(from, to, linkId, reverseId);
			// enable custom configuration
			RuntimeConfiguration
					.setConfiguration(
							RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION,
							Boolean.TRUE);

			// first we save the forward direction
			InstanceReference fromRef = convertToReference(from);
			InstanceReference toRef = convertToReference(to);
			LinkEntity entity = createLinkEntity(fromRef, toRef, linkId, true);
			entity = dbDao.saveOrUpdate(entity);
			ids.setFirst(entity.getId());
			// if the properties a missing then no need to create instance only to save non
			// existing properties
			if (!properties.isEmpty()) {
				propertiesService.saveProperties(createInstance(fromRef, toRef, entity, properties));
			}

			// then if needed save the reverse direction
			if (StringUtils.isNotNullOrEmpty(reverseId)) {
				LinkEntity reverseEntity = createLinkEntity(toRef, fromRef, reverseId, false);
				// update reverse link
				reverseEntity.setReverse(entity.getId());
				reverseEntity = dbDao.saveOrUpdate(reverseEntity);
				// update the reverse link
				entity.setReverse(reverseEntity.getId());
				ids.setSecond(reverseEntity.getId());
				if (!properties.isEmpty()) {
					propertiesService.saveProperties(createInstance(toRef, fromRef, reverseEntity,
							properties));
				}
			}
		} finally {
			// clear the configuration
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
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
	private LinkEntity createLinkEntity(InstanceReference from, InstanceReference to,
			String linkId, boolean primary) {
		LinkEntity entity = new LinkEntity();
		SequenceEntityGenerator.generateStringId(entity, false);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		if ((from == null) || (linkIds == null) || linkIds.isEmpty()) {
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
	private List<LinkReference> getLinksInternal(Object from, Object to, String linkId,
			Collection<String> linkIds, boolean loadProperties) {
		InstanceReference fromRef = convertToReference(from);
		InstanceReference toRef = convertToReference(to);

		Collection<String> links = new LinkedList<>();
		if (linkIds != null) {
			links.addAll(linkIds);
		}
		if (linkId != null) {
			links.add(linkId);
		}
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(5);
		// fetch all links for the given source
		String query = null;
		if (!links.isEmpty()) {
			args.add(new Pair<String, Object>("identifier", links));
		}
		if (fromRef != null) {
			query = links.isEmpty() ? EmfQueries.QUERY_LINK_BY_SRC_KEY
					: EmfQueries.QUERY_LINK_BY_SRC_AND_IDS_KEY;
			args.add(new Pair<String, Object>("fromId", fromRef.getIdentifier()));
			args.add(new Pair<String, Object>("fromType", fromRef.getReferenceType().getId()));
		}
		if (toRef != null) {
			query = links.isEmpty() ? EmfQueries.QUERY_LINK_BY_TARGET_KEY
					: EmfQueries.QUERY_LINK_BY_TARGET_AND_IDS_KEY;
			args.add(new Pair<String, Object>("toId", toRef.getIdentifier()));
			args.add(new Pair<String, Object>("toType", toRef.getReferenceType().getId()));
		}
		if ((fromRef != null) && (toRef != null)) {
			query = links.isEmpty() ? EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_KEY
					: EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY;
		}
		if (query == null) {
			return Collections.emptyList();
		}
		List<LinkEntity> list = dbDao.fetchWithNamed(query, args);

		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<LinkReference> linkInstances = new ArrayList<LinkReference>(list.size());
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
			String state = stateService.getState(IN_PROGRESS, LinkReference.class);
			for (LinkReference linkReference : linkInstances) {
				if ((linkReference.getProperties() != null)
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
	private LinkReference convertToReference(LinkEntity linkEntity) {
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
	private LinkReference createInstance(InstanceReference from, InstanceReference to,
			LinkEntity linkEntity,
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance) {
		if (instance == null) {
			return false;
		}
		return unlinkInternal(instance, instance, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to) {
		if ((from == null) || (to == null)) {
			return false;
		}
		return unlinkInternal(from, to, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverse) {
		if ((from == null) || (to == null)) {
			return false;
		}
		String firstLink = StringUtils.isNullOrEmpty(linkId) ? NO_LINK : linkId;
		String secondLink = StringUtils.isNullOrEmpty(reverse) ? NO_LINK : reverse;
		return unlinkInternal(from, to, firstLink, secondLink);
	}

	/**
	 * Unlink internal. Removes the link for the given source and target link ends. If the linkId is
	 * not present then all links will be removed. If needed the reverse link can be removed also.
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
		if (StringUtils.isNotNullOrEmpty(reverse) && !fromId.equals(toId)) {
			unlinked += unlinkInternal(toId, fromId, reverse);
		}
		if (debug) {
			logger.debug("Removed " + unlinked + " links for: from " + fromId + " to " + toId
					+ " with type " + linkId + "|" + reverse);
		}
		return unlinked > 0;
	}

	/**
	 * Unlink internal. Executes the queries for unlinking. If linkId is not present then all links
	 * will be removed that starts from the first id and point to the second id
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
		List<LinkReference> loadedLinks = new LinkedList<LinkReference>();
		if (fromId.equals(toId)) {
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
			propertiesService.removeProperties(linkReference, linkReference.getRevision(),
					linkReference);
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

	/**
	 * Removes the properties.
	 *
	 * @param instance
	 *            the instance
	 * @param path
	 *            the path
	 */
	private void removeProperties(Entity<?> instance, PathElement path) {
		propertiesService.removeProperties(instance, 0L, path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to) {
		if (to == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(null, to, null, ALL_LINKS, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		if ((to == null) || StringUtils.isNullOrEmpty(linkId)) {
			return CollectionUtils.emptyList();
		}
		List<LinkReference> list = getLinksInternal(null, to, linkId, null, true);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		if ((to == null) || (linkIds == null) || linkIds.isEmpty()) {
			return Collections.emptyList();
		}
		return getLinksInternal(null, to, null, linkIds, true);
	}

	/**
	 * {@inheritDoc}
	 */
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
				encodeSimpleLink(getReverseLinkType(linkId)),
				Collections.<String, Serializable> emptyMap());
		return (pair.getFirst() != null) && (pair.getSecond() != null);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		if (reverseId == null) {
			return linkSimple(from, to, linkId);
		}
		Pair<Serializable, Serializable> pair = link(from, to, encodeSimpleLink(linkId),
				encodeSimpleLink(reverseId), Collections.<String, Serializable> emptyMap());
		return (pair.getFirst() != null) && (pair.getSecond() != null);
	}

	/**
	 * Gets the reverse link type.
	 * 
	 * @param relationType
	 *            the relation type
	 * @return the reverse link type
	 */
	private String getReverseLinkType(String relationType) {
		if (relationType.endsWith("partOf")) {
			return LinkConstants.HAS_CHILD_URI;
		} else if (relationType.endsWith("hasChild")) {
			return LinkConstants.PART_OF_URI;
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
	private String encodeSimpleLink(String id) {
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
	private String decodeSimpleLink(String identifier) {
		if (identifier == null) {
			return null;
		}
		if (identifier.endsWith("-simple")) {
			return identifier.substring(0, identifier.length() - 7);
		}
		return identifier;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		List<LinkReference> links = getLinks(from, encodeSimpleLink(linkId));
		for (LinkReference reference : links) {
			reference.setIdentifier(decodeSimpleLink(reference.getIdentifier()));
		}
		return links;
	}

	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		unlinkInternal(from, null, encodeSimpleLink(linkId), null);
		unlinkInternal(null, from, encodeSimpleLink(getReverseLinkType(linkId)), null);
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		unlinkInternal(from, to, encodeSimpleLink(linkId),
				encodeSimpleLink(getReverseLinkType(linkId)));
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		if (reverseId == null) {
			unlinkInternal(from, to, encodeSimpleLink(linkId),
					encodeSimpleLink(getReverseLinkType(linkId)));
		} else {
			unlinkInternal(from, to, encodeSimpleLink(linkId), encodeSimpleLink(reverseId));
		}
	}

	@Override
	protected LinkReference getLinkReferenceById(Serializable id, boolean loadProperties) {
		LinkEntity linkEntity = dbDao.find(LinkEntity.class, id);
		if (linkEntity == null) {
			if (debug) {
				logger.debug("No link found in relational DB with id=" + id);
			}
			return null;
		}
		LinkReference reference = convertToReference(linkEntity);
		if (loadProperties) {
			propertiesService.loadProperties(reference);
		}
		return reference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<LinkReference> getLinksInternal(Object from, Object to,
			Collection<String> linkids) {
		List<LinkReference> internal = getLinksInternal(from, to, null, linkids, true);
		return internal;
	}

	@Override
	protected String shrinkLinkIdentifier(String identifier) {
		// no need to do anything
		return identifier;
	}

	@Override
	protected String getTopLevelCacheName() {
		return LINK_ENTITY_FULL_CACHE;
	}

	@Override
	protected String expandLinkIdentifier(String identifier) {
		return identifier;
	}

	@Override
	protected void removeLinkInternal(LinkReference instance) {
		unlinkInternal(instance.getFrom(), instance.getTo(), instance.getIdentifier(), NO_LINK);
		removeProperties(instance, instance);
	}

	@Override
	protected boolean updatePropertiesInternal(Serializable id,
			Map<String, Serializable> properties, Map<String, Serializable> oldProperties) {
		if ((id == null) || (properties == null)) {
			logger.warn("Cannot save properties of non persisted link instance");
			return false;
		}
		LinkReference reference = getLinkReference(id);
		if (reference == null) {
			logger.warn("Link with id=" + id + " was not found!");
			return false;
		}
		reference.getProperties().putAll(properties);
		propertiesService.saveProperties(reference);
		return true;
	}

	@Override
	protected String getMiddleLevelCacheName() {
		return LINK_ENTITY_CACHE;
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
