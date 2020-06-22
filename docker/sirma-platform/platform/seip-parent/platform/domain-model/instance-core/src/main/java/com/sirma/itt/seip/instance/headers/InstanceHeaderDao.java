package com.sirma.itt.seip.instance.headers;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * Dao for the {@link HeaderEntity}. It's responsible for the entities persistence and loading.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
class InstanceHeaderDao {
	@Inject
	private DbDao dbDao;

	/**
	 * Finds entity for the given definition if any
	 *
	 * @param definitionId the definition to search for
	 * @return the found entity or empty optional if not found
	 */
	Optional<HeaderEntity> findByDefinitionId(String definitionId) {
		List<HeaderEntity> headers = dbDao.fetchWithNamed(HeaderEntity.QUERY_BY_DEFINITION_ID_KEY,
				Collections.singletonList(new Pair<>("definitionId", definitionId)));
		if (isEmpty(headers)) {
			return Optional.empty();
		}
		return Optional.of(headers.get(0));
	}

	/**
	 * Save the given entity to the database
	 *
	 * @param entity entity to save
	 */
	void persist(HeaderEntity entity) {
		dbDao.saveOrUpdate(entity);
	}
}
