package com.sirma.itt.emf.semantic.library;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Semantic implementation of LibraryProvider. Provides logic for initializing the libraries, retrieving their
 * description from the Semantic Repository and caching the results. The libraries are initialized when new definitions
 * are uploaded or the cache is cleared
 *
 * @author kirq4e
 */
@ApplicationScoped
public class SemanticLibraryProvider implements LibraryProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLibraryProvider.class);

	@Inject
	private AuthorityService authorityService;
	@Inject
	private SearchService searchService;
	@Inject
	private UserPreferences userPreferences;

	@Override
	public List<Instance> getLibraries(String forAction) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setStringQuery(SemanticQueries.QUERY_LIBRARIES_AS_OBJECTS.getQuery());
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.getArguments().put("lang", userPreferences.getLanguage());
		arguments.setMaxSize(-1);
		arguments.setPageSize(-1);
		searchService.searchAndLoad(Instance.class, arguments);

		List<Instance> libraries = arguments.getResult();
		return filterLibrariesForAction(libraries, forAction);
	}

	private <I extends Instance> List<I> filterLibrariesForAction(List<I> libraries, String forAction) {
		if (CollectionUtils.isEmpty(libraries)) {
			return libraries;
		}

		// admin has access to all
		if (authorityService.isAdminOrSystemUser()) {
			return libraries;
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Semantic classess: {}", libraries);
		}
		List<I> result = new LinkedList<>();
		for (I classInstance : libraries) {
			if (authorityService.isActionAllowed(classInstance, forAction, null)) {
				result.add(classInstance);
			} else {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Skipped not allowed: {}", classInstance);
				}
			}
		}

		return result;
	}

}
