package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.instance.archive.ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_ID_KEY;
import static com.sirma.itt.seip.instance.archive.ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY;
import static com.sirma.itt.seip.instance.archive.ArchivedEntity.QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID_KEY;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.archive.ArchivedEntity;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * Provides direct access to database for retrieving and storing archived entities. Primary used for storing and
 * retrieving entities representing instances versions.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class VersionDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ID_QUERY_PARAM_KEY = "id";
	private static final String IDS_QUERY_PARAM_KEY = "ids";
	private static final String VERSION_DATE_QUERY_PARAM_KEY = "versionDate";

	/** Used to filter deleted entities. */
	private static final Predicate<ArchivedEntity> ONLY_VERSION_ENTITIES = entity -> entity.getCreatedOn() != null
			&& entity.getTransactionId() == null;
	private static final int FRAGMENT_SIZE = 16384;

	@Inject
	private DbDao dbDao;

	@Inject
	@InstanceType(type = ObjectTypes.ARCHIVED)
	private InstanceDao archivedInstanceDao;

	@Inject
	private ObjectMapper objectMapper;

	/**
	 * Persists the passed instance in the archived store, from where it could be loaded later. The instance is saved
	 * with additional parameters, which will help for later retrieving. The instance is saved with all of its current
	 * properties.
	 * <p>
	 * The instance should contain property {@link VersionProperties#VERSION_CREATED_ON}, so that the version is stored
	 * correctly.
	 *
	 * @param instance the version instance that should be stored
	 * @return persisted version instance
	 */
	public ArchivedInstance persistVersion(Instance instance) {
		if (instance == null) {
			throw new EmfRuntimeException("Could not persist version for null instance.");
		}

		ArchivedInstance version = (ArchivedInstance) instance;
		archivedInstanceDao.persistChanges(version);
		LOGGER.debug("Verion [{}] for instance with id - {} is stored.", version.getVersion(), instance.getId());
		return version;
	}

	/**
	 * Retrieves the count of the all stored versions for the instance with specific id.
	 *
	 * @param targetId the id of the instance which version should be counted
	 * @return the number of the found versions for the specific instance or <code>0</code> if versions are not found
	 */
	public int getVersionsCount(Serializable targetId) {
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>(ID_QUERY_PARAM_KEY, targetId));
		List<Long> results = dbDao.fetchWithNamed(QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID_KEY, params);

		if (results.isEmpty()) {
			return 0;
		}

		return results.get(0).intValue();
	}

	/**
	 * Retrieves all stored instances(versions) for the given instance id. When the entities are extracted, they are
	 * converted to {@link ArchivedInstance} and then returned. Note that the returned instances are not with loaded
	 * properties.
	 *
	 * @param targetId the id of instance which version should be retrieved
	 * @param skip the size of the results that should be skipped
	 * @param limit the limit of the results that should be returned. Pass <code>-1</code> to get all results
	 * @return collection of found versions
	 */
	public Collection<ArchivedInstance> findVersionsByTargetId(Serializable targetId, int skip, int limit) {
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>(ID_QUERY_PARAM_KEY, targetId));
		List<ArchivedEntity> results = dbDao.fetchWithNamed(QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY, params, skip,
				limit);
		return convertEntitiesToInstances(results);
	}

	private Collection<ArchivedInstance> convertEntitiesToInstances(Collection<ArchivedEntity> results) {
		if (results.isEmpty()) {
			return createHashSet(0);
		}

		return results
				.stream()
					.filter(ONLY_VERSION_ENTITIES)
					.map(entity -> objectMapper.map(entity, ArchivedInstance.class))
					.collect(Collectors.toList());
	}

	/**
	 * Retrieves specific version for instance with the given id. When the entity is extracted, it is converted to
	 * {@link ArchivedInstance} and then returned. The returned instance is without loaded properties.
	 *
	 * @param id the id of instance which version should be retrieved
	 * @return specific instance version represented as {@link ArchivedInstance} or null if there is no version with
	 *         this id
	 */
	public Optional<ArchivedInstance> findVersionById(Serializable id) {
		Collection<ArchivedEntity> result = extractEntitiesById(Collections.singletonList(id));
		return result.stream()
				.filter(Objects::nonNull)
				.map(entity -> objectMapper.map(entity, ArchivedInstance.class))
				.findFirst();
	}

	private Collection<ArchivedEntity> extractEntitiesById(Collection<? extends Serializable> ids) {
		// some times we receive way to may instance to check so we do a separate queries to overcome the problem
		return FragmentedWork.doWorkWithResult(ids, FRAGMENT_SIZE, part -> dbDao.fetchWithNamed(QUERY_ARCHIVED_ENTITIES_BY_ID_KEY,
				Collections.singletonList(new Pair<>(IDS_QUERY_PARAM_KEY, part))));
	}

	/**
	 * Retrieves versions ids and targetIds for set of target ids and created on date. The result of this method will be
	 * map containing as keys target ids and values id of found entities for version instances that are created before
	 * given date. If there are multiple entities with the same target id, they are filtered and the first that is with
	 * nearest date will be returned. This way we could guaranty that the correct version will be returned in the
	 * result.
	 *
	 * @param targetIds collection of target ids, which version records will be searched
	 * @param date the date that will be used to filter the versions
	 * @return map containing as keys target ids and values the ids of the version entries. Could return empty map if no
	 *         entities were found
	 */
	public Map<Serializable, Serializable> findVersionIdsByTargetIdAndDate(Collection<Serializable> targetIds,
			Serializable date) {
		if (isEmpty(targetIds) || date == null) {
			return createHashMap(0);
		}

		// some times we receive way to may instance to check so we do a separate queries to overcome the problem
		Collection<Object[]> result = FragmentedWork.doWorkWithResult(new HashSet<>(targetIds), FRAGMENT_SIZE,
				part -> queryVersionIdsByTargetIdAndDateInternal(part, date));
		return result.stream()
				// merge function is added just in case there are duplicated entities (somehow)
				.collect(Collectors.toMap(row -> (Serializable) row[0], row -> (Serializable) row[1], (k1, k2) -> k2));
	}

	private List<Object[]> queryVersionIdsByTargetIdAndDateInternal(Collection<Serializable> targetIds,
			Serializable date) {
		List<Pair<String, Object>> params = new ArrayList<>(2);
		// in case there are duplicated ids, we are using set to remove them
		params.add(new Pair<>(IDS_QUERY_PARAM_KEY, targetIds));
		params.add(new Pair<>(VERSION_DATE_QUERY_PARAM_KEY, date));

		// the result is 2 columns where the first is the target id and the second is the version id
		return dbDao
				.fetchWithNamed(ArchivedEntity.QUERY_LAST_VERSION_ID_BY_TARGET_ID_AND_CREATED_ON_DATE_KEY, params);
	}

	/**
	 * Retrieves version instances by their unique id. When the entities are extracted, they are converted to
	 * {@link ArchivedInstance}s and then returned. The returned instances are without loaded properties.
	 *
	 * @param ids the ids of the instances that should be extracted
	 * @return found {@link ArchivedInstance}s or empty collection
	 */
	public Collection<ArchivedInstance> findVersionsById(Collection<? extends Serializable> ids) {
		Collection<ArchivedEntity> results = extractEntitiesById(ids);
		return convertEntitiesToInstances(results);
	}

	/**
	 * Check if the given ids belongs to created versions
	 *
	 * @param ids the collection of identifiers to check
	 * @param <S> the identifier type
	 * @return a mapping of all ids as keys and value if they exist or not as versions
	 */
	<S extends Serializable> Map<S, Boolean> exits(Collection<S> ids) {
		Set<Serializable> found = extractEntitiesById(ids).stream().map(ArchivedEntity::getId).collect(Collectors.toSet());
		return ids.stream().collect(Collectors.toMap(Function.identity(), found::contains, (k1, k2) -> k1));
	}
}
