package com.sirma.itt.emf.solr.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.db.SolrDb;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.instance.relation.AbstractLinkService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * {@link LinkService} implementation to that operates via Solr.
 *
 * @author BBonev
 */
@SolrDb
@ApplicationScoped
public class SolrLinkServiceImpl extends AbstractLinkService {

	private static final long serialVersionUID = 6929822519796474124L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrLinkServiceImpl.class);

	@Inject
	@SemanticDb
	private LinkService semanticLinkService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private SearchService searchService;

	@Inject
	private SolrSearchConfiguration searchConfiguration;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	private static final Set<String> SUPPORTED_LINKS = new HashSet<>(
			Arrays.asList(LinkConstants.PART_OF_URI, LinkConstants.HAS_CHILD_URI, LinkConstants.TREE_CHILD_TO_PARENT,
					LinkConstants.TREE_PARENT_TO_CHILD, LinkConstants.HAS_ATTACHMENT));

	private static final Map<String, String> LINK_TO_SOLR_PROPERTY = new HashMap<>();

	static {
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.TREE_CHILD_TO_PARENT, "emfParentOf");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.TREE_PARENT_TO_CHILD, "emfHasParent");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.PART_OF_URI, "hasChild");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.HAS_CHILD_URI, "partOfRelation");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.HAS_ATTACHMENT, "hasAttachment");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.IS_ATTACHED_TO, "isAttachedTo");
	}

	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		if (isSupported(linkId)) {
			return getLinksInternal(null, to, linkId);
		}
		return semanticLinkService.getSimpleLinksTo(to, linkId);
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		if (isSupported(linkId)) {
			return getLinksInternal(from, null, linkId);
		}
		return semanticLinkService.getSimpleLinks(from, linkId);
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, Set<String> linkIds) {
		return semanticLinkService.getSimpleLinks(from, linkIds);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		if (isSupported(linkId)) {
			return getLinksInternal(null, to, linkId);
		}
		return semanticLinkService.getLinksTo(to, linkId);
	}

	/**
	 * Checks if the given link identifier is supported by the implementation.
	 *
	 * @param linkId
	 *            the link id
	 * @return true, if is supported
	 */
	private boolean isSupported(String linkId) {
		return linkId != null && SUPPORTED_LINKS.contains(shrinkLinkIdentifier(linkId));
	}

	/**
	 * Gets the links internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return the links internal
	 */
	private List<LinkReference> getLinksInternal(InstanceReference from, InstanceReference to, String linkId) {
		return getLinksInternal(from, to, linkId, null, Integer.MAX_VALUE, 0, null);
	}

	@Override

	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		if (arguments == null) {
			return null;
		}
		if (arguments.getFrom() == null && arguments.getTo() == null || arguments.getLinkId() == null) {
			LOGGER.warn("Insufficient arguments from[{}], to[{}], linkId[{}]", arguments.getFrom(), arguments.getTo(),
					arguments.getLinkId());
			arguments.setResult(Collections.<LinkInstance> emptyList());
			return arguments;
		}

		getLinksInternal(arguments.getFrom(), arguments.getTo(), arguments.getLinkId(), null, arguments.getPageSize(),
				arguments.getPageNumber(), arguments);

		return arguments;
	}

	/**
	 * Gets the links internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param typeFilter
	 *            the type filter
	 * @param pageSize
	 *            the page size
	 * @param page
	 *            the page
	 * @param arguments
	 *            source argument to place the results in
	 * @return the links internal
	 */
	private List<LinkReference> getLinksInternal(InstanceReference from, InstanceReference to, String linkId,
			String typeFilter, int pageSize, int page, LinkSearchArguments arguments) {

		Query query = createQuery(from, to, linkId);
		if (query == null) {
			LOGGER.warn("Could not build solr query - missing arguments");
			return Collections.emptyList();
		}
		SearchArguments<Instance> solr = new SearchArguments<>();
		solr.setPageSize(pageSize);
		solr.setPageNumber(page);
		solr.setSkipCount(page > 0 ? (page - 1) * pageSize : 0);
		solr.setDialect(SearchDialects.SOLR);
		solr.setPermissionsType(QueryResultPermissionFilter.NONE);
		solr.setProjection(searchConfiguration.getRelationsRequestFields().get());

		if (arguments != null) {
			if (arguments.getQuery() != null) {
				query = query.and(arguments.getQuery());
			}
			solr.addSorter(arguments.getFirstSorter());
		}
		solr.setQuery(query);

		searchService.searchAndLoad(SearchInstance.class, solr);

		if (arguments != null) {
			buildInstanceResult(from, to, linkId, solr, arguments);
			return Collections.emptyList();
		}

		return buildReferenceResult(from, to, linkId, solr.getResult());
	}

	/**
	 * Builds the reference result.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param instances
	 *            the instances
	 * @return the list
	 */
	private List<LinkReference> buildReferenceResult(InstanceReference from, InstanceReference to, String linkId,
			List<Instance> instances) {

		List<LinkReference> result = new ArrayList<>(instances.size());
		String linkIdentifier = shrinkLinkIdentifier(linkId);

		for (Instance instance : instances) {
			LinkReference reference = new LinkReference();
			reference.setIdentifier(linkIdentifier);
			if (from == null) {
				reference.setFrom(instance.toReference());
			} else {
				reference.setFrom(from);
			}
			if (to == null) {
				reference.setTo(instance.toReference());
			} else {
				reference.setTo(to);
			}
			result.add(reference);
		}

		return result;
	}

	/**
	 * Builds the instance result.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param solr
	 *            the solr
	 * @param arguments
	 *            the arguments
	 */
	private void buildInstanceResult(InstanceReference from, InstanceReference to, String linkId,
			SearchArguments<Instance> solr, LinkSearchArguments arguments) {

		List<Instance> instances = solr.getResult();
		if (instances == null) {
			arguments.setResult(Collections.<LinkInstance> emptyList());
			arguments.setTotalItems(0);
			return;
		}
		List<LinkInstance> result = new ArrayList<>(instances.size());
		Instance fromInstance = from == null ? null : from.toInstance();
		Instance toInstance = to == null ? null : to.toInstance();

		String linkIdentifier = shrinkLinkIdentifier(linkId);
		for (Instance instance : instances) {
			LinkInstance linkInstance = new LinkInstance();
			linkInstance.setIdentifier(linkIdentifier);
			if (fromInstance == null) {
				linkInstance.setFrom(instance);
			} else {
				linkInstance.setFrom(fromInstance);
			}
			if (toInstance == null) {
				linkInstance.setTo(instance);
			} else {
				linkInstance.setTo(toInstance);
			}

			result.add(linkInstance);
		}

		arguments.setResult(result);
		arguments.setTotalItems(solr.getTotalItems());
	}

	/**
	 * Creates the query.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return the string
	 */
	private Query createQuery(InstanceReference from, InstanceReference to, String linkId) {
		Query query = null;
		String uri = null;
		if (from != null) {
			uri = namespaceRegistryService.buildFullUri(from.getIdentifier());
			if (to != null) {
				query = new Query(DefaultProperties.URI, "\"" + uri + "\"", true);
			}
		}
		if (to != null) {
			uri = namespaceRegistryService.buildFullUri(to.getIdentifier());
			if (from != null && query != null) {
				String linkIdentifier = shrinkLinkIdentifier(linkId);
				String solrProperty = LINK_TO_SOLR_PROPERTY.get(linkIdentifier);
				query = query.and(solrProperty, "\"" + uri + "\"").end();
			}
		}
		if (query == null && uri != null) {
			String linkIdentifier = shrinkLinkIdentifier(linkId);
			String solrProperty = LINK_TO_SOLR_PROPERTY.get(linkIdentifier);
			query = new Query(solrProperty, "\"" + uri + "\"");
		}
		return query;
	}

	@Override

	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {
		return semanticLinkService.link(from, to, mainLinkId, reverseLinkId, properties);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		return semanticLinkService.linkSimple(from, to, linkId);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		return semanticLinkService.linkSimple(from, to, linkId, reverseId);
	}

	@Override
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		return semanticLinkService.linkSimple(from, tos, linkId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		semanticLinkService.unlinkSimple(from, linkId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		semanticLinkService.unlinkSimple(from, to, linkId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		semanticLinkService.unlinkSimple(from, to, linkId, reverseId);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		return semanticLinkService.getLinks(from, linkId);
	}

	@Override
	public LinkInstance getLinkInstance(Serializable id) {
		return semanticLinkService.getLinkInstance(id);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		return semanticLinkService.getLinks(from);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return semanticLinkService.getLinks(from, linkIds);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return semanticLinkService.getLinksTo(to);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		return semanticLinkService.getLinksTo(to, linkIds);
	}

	@Override
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		return semanticLinkService.removeLinksFor(instance, linkIds);
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to) {
		return semanticLinkService.unlink(from, to);
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid) {
		return semanticLinkService.unlink(from, to, linkId, reverseLinkid);
	}

	@Override
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		return semanticLinkService.updateLinkProperties(id, properties);
	}

	@Override
	public void removeLinkById(Serializable linkDbId) {
		semanticLinkService.removeLinkById(linkDbId);
	}

	@Override
	public void removeLink(LinkReference instance) {
		semanticLinkService.removeLink(instance);
	}

	@Override
	public LinkReference getLinkReference(Serializable id) {
		return semanticLinkService.getLinkReference(id);
	}

	@Override
	public boolean removeLinksFor(InstanceReference reference) {
		return semanticLinkService.removeLinksFor(reference);
	}

	@Override
	public void removeLink(LinkInstance instance) {
		semanticLinkService.removeLink(instance);
	}

	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		return semanticLinkService.isLinked(from, to, linkId);
	}

	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		return semanticLinkService.isLinkedSimple(from, to, linkId);
	}

	@Override
	protected void removeLinkInternal(LinkReference second) {
		// nothing to do here
	}

	@Override
	protected LinkReference getLinkReferenceById(Serializable id, boolean loadProperties) {
		return null;
	}

	@Override
	protected String shrinkLinkIdentifier(String identifier) {
		return namespaceRegistryService.getShortUri(identifier);
	}

	@Override
	protected String expandLinkIdentifier(String identifier) {
		return namespaceRegistryService.buildFullUri(identifier);
	}

	@Override
	protected List<LinkReference> getLinksInternal(Object from, Object to, Collection<String> linkids) {
		return Collections.emptyList();
	}

	@Override
	protected Pair<Serializable, Serializable> linkInternal(Object from, Object to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		return semanticLinkService.link((InstanceReference) from, (InstanceReference) to, mainLinkId, reverseLinkId,
				properties);
	}

	@Override
	protected String getMiddleLevelCacheName() {
		return null;
	}

	@Override
	protected String getTopLevelCacheName() {
		return null;
	}

	@Override
	protected boolean updatePropertiesInternal(Serializable id, Map<String, Serializable> properties,
			Map<String, Serializable> oldProperties) {
		return false;
	}

	@Override
	protected String getReverseLinkType(String relationType) {
		PropertyInstance relation = semanticDefinitionService.getRelation(relationType);
		if (relation != null) {
			return relation.getInverseRelation();
		}
		return null;
	}
}
