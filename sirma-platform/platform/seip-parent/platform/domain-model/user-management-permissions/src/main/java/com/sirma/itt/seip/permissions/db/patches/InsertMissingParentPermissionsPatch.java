package com.sirma.itt.seip.permissions.db.patches;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.permissions.role.EntityPermission;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Custom patch that inserts missing parent permissions for Libraries, Users and Groups.
 * These instances are considered as not eligible for inheritance but should be present in the database but with
 * disabled parent inheritance.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/09/2017
 */
public class InsertMissingParentPermissionsPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String QUERY_BY_TYPE = "select distinct ?instance where {\n"
			+ "    ?instance ptop:partOf ?parent .\n"
			+ "}";
	static final String QUERY_LIBRARIES = "select ?instance where {\n"
			+ "    ?instance emf:isPartOfObjectLibrary \"true\"^^xsd:boolean.\n"
			+ "}";

	private SearchService searchService;
	private NamespaceRegistryService registryService;
	private DbDao dbDao;

	@Override
	public void setUp() throws SetupException {
		searchService = CDI.instantiateDefaultBean(SearchService.class, CDI.getCachedBeanManager());
		registryService = CDI.instantiateDefaultBean(NamespaceRegistryService.class, CDI.getCachedBeanManager());
		dbDao = CDI.instantiateDefaultBean(DbDao.class, CDI.getCachedBeanManager());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		Set<String> libraries;
		try (Stream<Serializable> stream = streamInstancesForType(QUERY_LIBRARIES, null)) {
			libraries = stream.map(Object::toString).map(registryService::getShortUri).collect(Collectors.toSet());
		}

		String[] userAndGroup = { EMF.USER.toString(), EMF.GROUP.toString() };

		Stream.concat(libraries.stream(), Stream.of(userAndGroup)).forEach(type -> {
			try (Stream<Serializable> stream = streamInstancesForType(QUERY_BY_TYPE, type)) {
				Set<Serializable> remaining = (Set<Serializable>) stream.reduce(new HashSet<>(),
						(accumulated, newValue) -> accumulateInstanceIdsAndUpdate((Set<Serializable>) accumulated, newValue, type));
				writeChangesInDb(remaining, registryService.getShortUri(type));
			}
		});
		LOGGER.info("Finished processing in {} ms", tracker.stop());
	}

	@SuppressWarnings("unchecked")
	private Serializable accumulateInstanceIdsAndUpdate(Set<Serializable> accumulatedSet, Serializable newValue,
			String type) {
		if (newValue instanceof Set) {
			// this can happen if for some reason the current lambda is used as reduce combiner
			accumulatedSet.addAll((Collection<? extends Serializable>) newValue);
		} else {
			accumulatedSet.add(newValue);
		}
		if (accumulatedSet.size() >= 1024) {
			writeChangesInDb(accumulatedSet, registryService.getShortUri(type));
			accumulatedSet.clear();
		}
		return (Serializable) accumulatedSet;
	}

	private void writeChangesInDb(Set<Serializable> accumulated, String parent) {
		if (accumulated.isEmpty()) {
			return;
		}
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<>("targetId", accumulated));
		args.add(new Pair<>("parentId", parent));
		dbDao.executeUpdateInNewTx(EntityPermission.QUERY_UPDATE_PARENT_FOR_TARGET_KEY, args);
		dbDao.executeUpdateInNewTx(EntityPermission.QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET_KEY, args);
	}

	private Stream<Serializable> streamInstancesForType(String query, String type) {
		return searchService.stream(buildArguments(query, type), ResultItemTransformer.asSingleValue("instance"));
	}

	private static SearchArguments<Instance> buildArguments(String query, String type) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arguments.setMaxSize(-1);
		arguments.setPageSize(-1);
		arguments.setStringQuery(query);
		arguments.setDialect(SearchDialects.SPARQL);
		addNonNullValue(arguments.getArguments(), "parent", type);
		arguments.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, Boolean.FALSE);
		return arguments;
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished filling the missing parent permissions";
	}
}
