package com.sirma.itt.seip.instance.relation;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Basic {@link LinkService} implementation for some of the methods.
 *
 * @author BBonev
 */
public abstract class AbstractLinkService implements LinkService, Serializable {

	private static final int MAX_DELETED_LINKS = 2048;

	private static final long serialVersionUID = -1422766464513563845L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLinkService.class);

	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	protected DefinitionService definitionService;

	@Inject
	protected SecurityContext securityContext;

	/**
	 * Lock for synchronizing the access to the deleted links set
	 */
	private static final Lock DELETED_LINKS_LOCK = new ReentrantLock();

	/**
	 * Set that holds all recently deleted links. This is needed due to the cache organization and not to clear all of
	 * it on link removal.
	 */
	private static final Set<Serializable> DELETED_LINKS = CollectionUtils.createLinkedHashSet(MAX_DELETED_LINKS);

	@Override
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {
		if (from == null || to == null) {
			LOGGER.warn("Cannot link null instances!");
			return Pair.NULL_PAIR;
		}
		Map<String, Serializable> map = properties;
		if (map == null) {
			map = Collections.emptyMap();
		}

		return linkAndRegisterToCache(from.toReference(), to.toReference(), mainLinkId, reverseLinkId, map);
	}

	@Override
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		if (from == null || to == null) {
			LOGGER.warn("Cannot link null instances!");
			return Pair.NULL_PAIR;
		}
		return linkAndRegisterToCache(from, to, mainLinkId, reverseLinkId, properties);
	}

	/**
	 * Link and register to cache.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param mainLinkId
	 *            the main link id
	 * @param reverseLinkId
	 *            the reverse link id
	 * @param properties
	 *            the properties
	 * @return the pair
	 */
	protected Pair<Serializable, Serializable> linkAndRegisterToCache(InstanceReference from, InstanceReference to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties) {
		Pair<Serializable, Serializable> pair = linkInternal(from, to, mainLinkId, reverseLinkId, properties);
		if (pair == null) {
			LOGGER.warn("Link {} between {} and {} was NOT created!", mainLinkId, from, to);
			return null;
		}
		if (pair.getFirst() != null) {
			LOGGER.trace("Forward link {} was created between {} with id={} to {}. Updating cache.", mainLinkId,
					from.getId(), pair.getFirst(), to.getId());
		}
		if (pair.getSecond() != null) {
			LOGGER.trace("Reverse link {} was created between {} with id={} to {}. Updating cache.", reverseLinkId,
					to.getId(), pair.getSecond(), from.getId());
		}

		return pair;
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return getLinksInternal(null, to, Collections.singletonList(linkId));
	}

	@Override
	public LinkInstance convertToLinkInstance(LinkReference source) {
		if (source == null) {
			return null;
		}
		LinkInstance instance = new LinkInstance();
		instance.setId(source.getId());
		instance.setIdentifier(source.getIdentifier());
		instance.setPrimary(source.getPrimary());
		instance.setReverse(source.getReverse());
		instance.setProperties(source.getProperties());

		instance.setFrom(source.getFrom().toInstance());
		instance.setTo(source.getTo().toInstance());
		return instance;
	}

	@Override
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source) {
		List<LinkInstance> instanceLinks = new ArrayList<>(source.size());
		// hold references to the instances that will be filled with data
		Collection<Instance> instances = new ArrayList<>(instanceLinks.size() * 2);

		for (LinkReference linkReference : source) {
			LinkInstance linkInstance = linkReference.toLinkInstance();
			instanceLinks.add(linkInstance);

			instances.add(linkInstance.getFrom());
			instances.add(linkInstance.getTo());
		}

		instanceLoadDecorator.decorateResult(instances);
		return instanceLinks;
	}

	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		if (from == null || to == null || linkId == null) {
			return false;
		}
		List<LinkReference> references = getLinksInternal(from, linkId);
		for (LinkReference linkReference : references) {
			if (linkReference.getTo().equals(to)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		if (from == null || to == null || linkId == null) {
			return false;
		}
		List<LinkReference> references = getSimpleLinks(from, linkId);
		for (LinkReference linkReference : references) {
			if (linkReference.getTo().equals(to)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		if (from == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, null);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		if (from == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, linkId);
	}

	private List<LinkReference> getLinksInternal(InstanceReference from, String linkId) {
		if (linkId == null) {
			return getLinksInternal(from, null, Collections.emptyList());
		}
		return getLinksInternal(from, null, Collections.singletonList(linkId));
	}

	@Override
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		if (arguments == null) {
			return null;
		}
		// same basic implementation for the method using the existing methods
		List<LinkReference> result = Collections.emptyList();
		if (arguments.getFrom() != null) {
			if (arguments.getLinkId() != null) {
				result = getLinks(arguments.getFrom(), arguments.getLinkId());
			} else {
				result = getLinks(arguments.getFrom());
			}
		} else if (arguments.getTo() != null) {
			if (arguments.getLinkId() != null) {
				result = getLinksTo(arguments.getTo(), arguments.getLinkId());
			} else {
				result = getLinksTo(arguments.getTo());
			}
		}
		arguments.setResult(convertToLinkInstance(result));
		arguments.setTotalItems(result.size());
		return arguments;
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance, Predicate<String> relationFilter) {
		if (instance == null) {
			return Collections.emptyList();
		}

		DefinitionModel definition = definitionService.getInstanceDefinition(instance);
		if (definition == null) {
			LOGGER.debug("Could not load definition model for instance with id [{}] and definition id [{}]",
					instance.getId(), instance.getIdentifier());
			return Collections.emptyList();
		}
		// will load only non system fields of type URI that have a valid uri and are not filtered
		Set<String> linkIds = definition
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.filter(p -> relationFilter.test(p.getIdentifier()))
					.map(PropertyDefinition.resolveUri())
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

		if (isEmpty(linkIds)) {
			return Collections.emptyList();
		}

		return getSimpleLinks(instance.toReference(), linkIds);
	}

	/**
	 * Gets the reverse link type. If no reverse link is defined in semantic model no reverse link will be created
	 *
	 * @param relationType
	 *            the relation type
	 * @return the reverse link type
	 */
	protected abstract String getReverseLinkType(String relationType);

	/**
	 * Adds the deleted link id.
	 *
	 * @param linkDbId
	 *            the link db id
	 */
	protected void addDeletedLinkId(Serializable linkDbId) {
		if (linkDbId == null) {
			return;
		}
		DELETED_LINKS_LOCK.lock();
		try {
			if (DELETED_LINKS.size() == MAX_DELETED_LINKS) {
				// delete the first element
				DELETED_LINKS.remove(DELETED_LINKS.iterator().next());
			}
			DELETED_LINKS.add(linkDbId);
		} finally {
			DELETED_LINKS_LOCK.unlock();
		}
	}

	/**
	 * Normalize link identifier.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the string
	 */
	protected abstract String shrinkLinkIdentifier(String identifier);

	/**
	 * Expand link identifier.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the string
	 */
	protected abstract String expandLinkIdentifier(String identifier);

	/**
	 * Gets the links internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkids
	 *            the linkids
	 * @return the links internal
	 */
	protected abstract List<LinkReference> getLinksInternal(Object from, Object to, Collection<String> linkids);

	/**
	 * Link internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param mainLinkId
	 *            the main link id
	 * @param reverseLinkId
	 *            the reverse link id
	 * @param properties
	 *            the properties
	 * @return the pair
	 */
	protected abstract Pair<Serializable, Serializable> linkInternal(Object from, Object to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties);

	/**
	 * Checks if the given link id is deleted.
	 *
	 * @param id
	 *            the id
	 * @return true, if is deleted
	 */
	protected boolean isDeleted(Serializable id) {
		DELETED_LINKS_LOCK.lock();
		try {
			return DELETED_LINKS.contains(id);
		} finally {
			DELETED_LINKS_LOCK.unlock();
		}
	}
}
