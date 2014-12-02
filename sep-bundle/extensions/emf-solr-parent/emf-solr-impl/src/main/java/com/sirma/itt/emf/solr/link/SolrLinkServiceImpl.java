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

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.db.SolrDb;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.AbstractLinkService;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkSearchArguments;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.solr.configuration.SolrConfigurationProperties;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * {@link LinkService} implementation to that operates via Solr.
 * 
 * @author BBonev
 */
@SolrDb
@ApplicationScoped
public class SolrLinkServiceImpl extends AbstractLinkService {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6929822519796474124L;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SolrLinkServiceImpl.class);
	/** The semantic link service. */
	@Inject
	@SemanticDb
	private LinkService semanticLinkService;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The search service. */
	@Inject
	private SearchService searchService;

	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_CONFIG_LINKS_FL, defaultValue = SolrQueryConstants.FIELD_NAME_INSTANCE_ID
			+ ","
			+ SolrQueryConstants.FIELD_NAME_INSTANCE_TYPE
			+ ","
			+ DefaultProperties.HEADER_COMPACT
			+ ","
			+ DefaultProperties.MIMETYPE
			+ ","
			+ DefaultProperties.PURPOSE)
	private String linkProjection;

	/** The Constant SUPPORTED_LINKS. */
	private static final Set<String> SUPPORTED_LINKS = new HashSet<String>(Arrays.asList(
			LinkConstants.PART_OF_URI, LinkConstants.HAS_CHILD_URI,
			LinkConstants.TREE_CHILD_TO_PARENT, LinkConstants.TREE_PARENT_TO_CHILD));

	private static final Map<String, String> LINK_TO_SOLR_PROPERTY = new HashMap<String, String>();
	static {
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.TREE_CHILD_TO_PARENT, "emfParentOf");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.TREE_PARENT_TO_CHILD, "emfHasParent");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.PART_OF_URI, "hasChild");
		LINK_TO_SOLR_PROPERTY.put(LinkConstants.HAS_CHILD_URI, "partOfRelation");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		if (isSupported(linkId)) {
			return getLinksInternal(null, to, linkId);
		}
		return semanticLinkService.getSimpleLinksTo(to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		if (isSupported(linkId)) {
			return getLinksInternal(from, null, linkId);
		}
		return semanticLinkService.getSimpleLinks(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		if (isSupported(linkId)) {
			return getLinksInternal(null, to, linkId);
		}
		return semanticLinkService.getLinksTo(to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from, String linkId,
			Class<? extends Instance> toFilter) {
		if (isSupported(linkId) && (toFilter != null)) {
			DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(toFilter
					.getName());
			if (typeDefinition != null) {
				return getLinksInternal(from, null, linkId, typeDefinition.getFirstUri(),
						Integer.MAX_VALUE, 0, null);
			}
		}
		return semanticLinkService.getLinks(from, linkId, toFilter);
	}

	/**
	 * Checks if the given link identifier is supported by the implementation.
	 * 
	 * @param linkId
	 *            the link id
	 * @return true, if is supported
	 */
	private boolean isSupported(String linkId) {
		return (linkId != null) && SUPPORTED_LINKS.contains(shrinkLinkIdentifier(linkId));
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
	private List<LinkReference> getLinksInternal(InstanceReference from, InstanceReference to,
			String linkId) {
		return getLinksInternal(from, to, linkId, null, Integer.MAX_VALUE, 0, null);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		if (arguments == null) {
			return null;
		}
		if (((arguments.getFrom() == null) && (arguments.getTo() == null))
				|| (arguments.getLinkId() == null)) {
			LOGGER.warn("Insufficient arguments from[{}], to[{}], linkId[{}]", arguments.getFrom(),
					arguments.getTo(), arguments.getLinkId());
			arguments.setResult(Collections.<LinkInstance> emptyList());
			return arguments;
		}

		getLinksInternal(arguments.getFrom(), arguments.getTo(), arguments.getLinkId(), null,
				arguments.getMaxSize(), arguments.getPageNumber(), arguments);

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
	private List<LinkReference> getLinksInternal(InstanceReference from, InstanceReference to,
			String linkId, String typeFilter, int pageSize, int page, LinkSearchArguments arguments) {

		String query = createQuery(from, to, linkId);
		if (query == null) {
			LOGGER.warn("Could not build solr query - missing arguments");
			return Collections.emptyList();
		}
		SearchArguments<Instance> solr = new SearchArguments<Instance>();
		solr.setPageSize(pageSize);
		solr.setPageNumber(page);
		solr.setSkipCount(page > 0 ? (page - 1) * pageSize : 0);
		solr.setStringQuery(query);
		solr.setDialect(SearchDialects.SOLR);
		solr.setProjection(linkProjection);

		searchService.search(SearchInstance.class, solr);

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
	private List<LinkReference> buildReferenceResult(InstanceReference from, InstanceReference to,
			String linkId, List<Instance> instances) {

		List<LinkReference> result = new ArrayList<LinkReference>(instances.size());
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
		List<LinkInstance> result = new ArrayList<LinkInstance>(instances.size());
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
	private String createQuery(InstanceReference from, InstanceReference to, String linkId) {
		String query = null;
		String uri = null;
		if (from != null) {
			uri = namespaceRegistryService.buildFullUri(from.getIdentifier());
		} else if (to != null) {
			uri = namespaceRegistryService.buildFullUri(to.getIdentifier());
		}
		if (uri != null) {
			String linkIdentifier = shrinkLinkIdentifier(linkId);
			String solrProperty = LINK_TO_SOLR_PROPERTY.get(linkIdentifier);
			query = solrProperty + ": \"" + uri + "\"";
		}
		return query;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		return semanticLinkService.link(from, to, mainLinkId, reverseLinkId, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		return semanticLinkService.linkSimple(from, to, linkId);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		return semanticLinkService.linkSimple(from, to, linkId, reverseId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		return semanticLinkService.linkSimple(from, tos, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		semanticLinkService.unlinkSimple(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		semanticLinkService.unlinkSimple(from, to, linkId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return semanticLinkService.getLinks(from, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return semanticLinkService.getLinksTo(to);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		return semanticLinkService.getLinksTo(to, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		return semanticLinkService.removeLinksFor(instance, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean unlink(InstanceReference from, InstanceReference to) {
		return semanticLinkService.unlink(from, to);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId,
			String reverseLinkid) {
		return semanticLinkService.unlink(from, to, linkId, reverseLinkid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		return semanticLinkService.updateLinkProperties(id, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLinkById(Serializable linkDbId) {
		semanticLinkService.removeLinkById(linkDbId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLink(LinkReference instance) {
		semanticLinkService.removeLink(instance);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeLinkInternal(LinkReference second) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LinkReference getLinkReferenceById(Serializable id, boolean loadProperties) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String shrinkLinkIdentifier(String identifier) {
		return namespaceRegistryService.getShortUri(identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String expandLinkIdentifier(String identifier) {
		return namespaceRegistryService.buildFullUri(identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<LinkReference> getLinksInternal(Object from, Object to,
			Collection<String> linkids) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Serializable, Serializable> linkInternal(Object from, Object to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties) {
		return semanticLinkService.link((InstanceReference) from, (InstanceReference) to,
				mainLinkId, reverseLinkId, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMiddleLevelCacheName() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTopLevelCacheName() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean updatePropertiesInternal(Serializable id,
			Map<String, Serializable> properties, Map<String, Serializable> oldProperties) {
		return false;
	}

}
