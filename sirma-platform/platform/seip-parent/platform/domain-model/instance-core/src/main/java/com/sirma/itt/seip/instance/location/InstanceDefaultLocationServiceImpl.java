package com.sirma.itt.seip.instance.location;

import static com.sirma.itt.seip.instance.relation.LinkConstants.IS_DEFAULT_LOCATION;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.instance.location.InstanceDefaultLocationService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.search.SearchService;

/**
 * Concrete implementation for the InstanceDefaultLocationService. Contains logic for adding, removing and updating
 * default locations for definitions. The locations are set by link between the definitions and the instances. This link
 * is created in the semantic. For the retrieving is used custom SPARQL query, executed by search service.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class InstanceDefaultLocationServiceImpl implements InstanceDefaultLocationService {

	private static final String LOCATION_PARAM = "locationParam";

	private static final String DEFAULT_LOCATIONS_QUERY = "instanceQueries/getDefaultLocations";

	@Inject
	@SemanticDb
	private LinkService semanticLinkService;

	@Inject
	private SearchService searchService;

	/**
	 * Uses semantic link service to create the link between the keys and their value for each entry in the passed map.
	 */
	@Override
	public void addDefaultLocations(Map<InstanceReference, InstanceReference> defaultLocations) {
		if (CollectionUtils.isNotEmpty(defaultLocations)) {
			defaultLocations.forEach(this::createSimpleLinkForDefaultLocation);
		}
	}

	/**
	 * Executes custom SPARQL query using search service, which retrieves the default location for the passed definition
	 * id (if any), plus the projects for which the user have permission. The there are no results after the search this
	 * method will return empty collection.
	 */
	@Override
	public Collection<? extends Instance> retrieveLocations(String definitionId) {
		Context<String, Object> context = new Context<>(1);
		context.put(LOCATION_PARAM, definitionId);
		SearchArguments<SearchInstance> arguments = searchService.getFilter(DEFAULT_LOCATIONS_QUERY,
				SearchInstance.class, context);
		arguments.setPermissionsType(QueryResultPermissionFilter.WRITE);
		searchService.searchAndLoad(Instance.class, arguments);
		if (CollectionUtils.isEmpty(arguments.getResult())) {
			return Collections.emptyList();
		}
		return arguments.getResult();
	}

	@Override
	public Collection<InstanceReference> retrieveOnlyDefaultLocations(InstanceReference reference) {
		if (reference == null) {
			return Collections.emptyList();
		}

		return new LinkIterable<>(semanticLinkService.getSimpleLinksTo(reference, IS_DEFAULT_LOCATION), true);
	}

	/**
	 * Updates definitions default locations. First removes the old link, if there is one and then creates the new link.
	 * The links are created and removed from the semantic DB.
	 */
	@Override
	public void updateDefaultLocations(Map<InstanceReference, InstanceReference> defaultLocations) {
		if (CollectionUtils.isNotEmpty(defaultLocations)) {
			defaultLocations.forEach((k, v) -> {
				unlinkSimpleDefaultLocation(k, v);
				createSimpleLinkForDefaultLocation(k, v);
			});
		}
	}

	/**
	 * Removes definitions default locations. Uses semantic link service to remove the locations for the given
	 * definition.
	 */
	@Override
	public void removeDefaultLocations(Collection<InstanceReference> definitionsReferences) {
		if (CollectionUtils.isNotEmpty(definitionsReferences)) {
			definitionsReferences.forEach(location -> {
				List<LinkReference> links = semanticLinkService.getSimpleLinksTo(location, IS_DEFAULT_LOCATION);
				unlinkSimpleDefaultLocation(links.get(0).getTo(), links.get(0).getFrom());
			});
		}
	}

	/**
	 * Creates simple link between the definition reference and the passed instance reference. The used link is
	 * {@link LinkConstants#IS_DEFAULT_LOCATION}. The link is created in the semantic DB.
	 *
	 * @param definitionReference
	 *            the definition reference to which will be assigned location
	 * @param instanceLocationReference
	 *            the instance reference which will be the default location for the instances with given definition
	 */
	private void createSimpleLinkForDefaultLocation(InstanceReference definitionReference,
			InstanceReference instanceLocationReference) {
		semanticLinkService.linkSimple(instanceLocationReference, definitionReference, IS_DEFAULT_LOCATION);
	}

	/**
	 * Removes simple link {@link LinkConstants#IS_DEFAULT_LOCATION} for the passed instance references. The link is
	 * removed from the semantic DB.
	 *
	 * @param definitionReference
	 *            the definition reference, which will be unlinked
	 * @param instanceLocationReference
	 *            the instance reference, which will be unlinked
	 */
	private void unlinkSimpleDefaultLocation(InstanceReference definitionReference,
			InstanceReference instanceLocationReference) {
		semanticLinkService.unlinkSimple(instanceLocationReference, definitionReference, IS_DEFAULT_LOCATION);
	}

}
