package com.sirma.itt.seip.permissions.sync.batch;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;

/**
 * Helper class to fetch and store the role mapping from the semantic model
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/06/2017
 */
class PermissionSyncUtil {

	private static final String ROLE_MAPPING = "SELECT ?instance ?sepRoleId WHERE {\n"
			+ "    ?instance skos:inScheme conc:SecurityRoleTypes ;\n"
			+ "              skos:exactMatch ?sepRoleUri .\n"
			+ "    ?sepRoleUri skos:prefLabel ?sepRoleId ;\n"
			+ "                skos:inScheme conc:SepBaseRoles ;\n"
			+ "                skos:altLabel ?sepRolePriority .\n"
			+ "}";

	@Inject
	private SearchService searchService;

	@Inject
	private RoleService roleService;

	private Map<String, String> roleMapping;
	private Set<String> managerRoleTypes;

	/**
	 * Returns the role mapping between the defined internal roles and the semantic role types
	 *
	 * @return map with keys Sep role identifiers (CONSUMER) and the semantic role types as values
	 */
	Map<String, String> getRoleTypesMapping() {
		if (roleMapping == null) {
			try (Stream<ResultItem> stream = searchService.stream(prepareSearchArguments(ROLE_MAPPING, false)
					, ResultItemTransformer.asIs())) {
				roleMapping = stream
						.filter(item -> item.hasValue("sepRoleId") && item.hasValue("instance"))
						.collect(toMap(item -> item.getString("sepRoleId"), item -> item.getString("instance")));
			}
		}
		return roleMapping;
	}

	/**
	 * Check if the given semantic role type is manager role
	 *
	 * @param semanticRoleType the role to check
	 * @return true if it's manager role
	 */
	public boolean isManagerRoleType(String semanticRoleType) {
		return getManagerRoleTypes().contains(semanticRoleType);
	}

	private Set<String> getManagerRoleTypes() {
		if (managerRoleTypes == null) {
			managerRoleTypes = getRoleTypesMapping()
					.entrySet()
					.stream()
					.filter(e -> roleService.isManagerRole(e.getKey()))
					.map(Map.Entry::getValue)
					.collect(toSet());
		}
		return managerRoleTypes;
	}

	/**
	 * Prepare search arguments that fetch all data for the given query with option to enabled/disable the inferred
	 * statements
	 *
	 * @param query the query to set for execution
	 * @param includeInferred if inferred statements should be included or not
	 * @return the search arguments that can be used for query execution
	 */
	SearchArguments<CommonInstance> prepareSearchArguments(String query, boolean includeInferred) {
		SearchArguments<CommonInstance> rolesFilter = searchService.getFilter(query, CommonInstance.class, null);
		rolesFilter.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, includeInferred);
		rolesFilter.setDialect(SearchDialects.SPARQL);
		rolesFilter.setPageSize(0);
		rolesFilter.setMaxSize(0);
		rolesFilter.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		return rolesFilter;
	}

}
