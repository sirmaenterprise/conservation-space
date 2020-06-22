package com.sirma.itt.seip.instance.archive.properties;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.Instance;
import  com.sirma.itt.seip.instance.archive.properties.entity.ArchivedJsonPropertiesEntity;
import com.sirma.itt.seip.instance.properties.RelationalNonPersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Provides CRUD operations for version properties.
 *
 * @author A. Kunchev
 */
// TODO cache something to improve performance
@ApplicationScoped
public class ArchivedPropertiesDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int FRAGMENT_SIZE = 16384;

	@Inject
	private DbDao dbDao;

	@Inject
	private ArchivedPropertiesConverter archivedPropertiesConverter;

	@Inject
	@ExtensionPoint(value = RelationalNonPersistentPropertiesExtension.TARGET_NAME)
	private Plugins<RelationalNonPersistentPropertiesExtension> nonPersistentProperties;
	private Set<String> forbiddenProperties = new HashSet<>(50);

	/**
	 * Initializes the forbiddenProperties by collecting all properties provided by
	 * {@link RelationalNonPersistentPropertiesExtension}s.
	 */
	@PostConstruct
	void initForbiddenProperties() {
		nonPersistentProperties.stream()
					.map(RelationalNonPersistentPropertiesExtension::getNonPersistentProperties)
					.forEach(forbiddenProperties::addAll);
	}

	/**
	 * Persists properties of given version.<br>
	 * To avoid merging process as it isn't needed, first the old properties will be deleted for that version and then
	 * the new properties will be stored.
	 *
	 * @param version which properties should be stored
	 */
	public <I extends Instance> void persist(I version) {
		version.removeProperties(forbiddenProperties);
		Map<String, Serializable> properties = archivedPropertiesConverter.toPersistent(version);
		dbDao.saveOrUpdate(new ArchivedJsonPropertiesEntity(version.getId().toString(), properties));
	}

	/**
	 * Loads properties of single version. The properties are directly added to the version.
	 * <p>
	 * Note that the passed version properties will be modified after method is called.
	 * </p>
	 *
	 * @param version which properties should be loaded
	 */
	public <I extends Instance> void load(I version) {
		ArchivedJsonPropertiesEntity entity = dbDao.find(ArchivedJsonPropertiesEntity.class, version.getId());

		if (entity == null) {
			LOGGER.debug("No properties were found for instance - {}.", version.getId());
			return;
		}

		Map<String, Serializable> properties = archivedPropertiesConverter.toInstanceProperties(entity.getProperties());
		version.addAllProperties(properties);
	}

	/**
	 * Loads properties of the passed versions. The properties are directly added to the versions, when they are loaded.
	 * <p>
	 * Note that the passed versions properties will be modified after method is called.
	 * </p>
	 *
	 * @param versions which properties should be load.
	 */
	public <I extends Instance> void load(Collection<I> versions) {
		Map<Serializable, Instance> versionsMapping = createIdMapping(versions);
		Collection<ArchivedJsonPropertiesEntity> entities = FragmentedWork.doWorkWithResult(versionsMapping.keySet(),
				FRAGMENT_SIZE, this::batchLoadInternal);

		if (CollectionUtils.isEmpty(entities)) {
			LOGGER.debug("No properties were found for instances - {}.", versionsMapping.keySet());
			return;
		}

		entities.forEach(setPropertiesToVersions(versionsMapping));
	}

	private static <I extends Instance> Map<Serializable, Instance> createIdMapping(Collection<I> versions) {
		return versions.stream().collect(Collectors.toMap(Instance::getId, Function.identity()));
	}

	private List<ArchivedJsonPropertiesEntity> batchLoadInternal(Collection<Serializable> versionIds) {
		return dbDao.fetchWithNamed(ArchivedJsonPropertiesEntity.QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY,
				Collections.singletonList(new Pair<>("versionIds", versionIds)));
	}

	private Consumer<ArchivedJsonPropertiesEntity> setPropertiesToVersions(Map<Serializable, Instance> versions) {
		return entity -> versions.computeIfPresent(entity.getId(), (id, version) -> {
			version.addAllProperties(archivedPropertiesConverter.toInstanceProperties(entity.getProperties()));
			return version;
		});
	}

	/**
	 * Deletes {@link ArchivedJsonPropertiesEntity} by version id.
	 *
	 * @param versionId of the instance which properties should be deleted
	 */
	public void delete(Serializable versionId) {
		dbDao.delete(ArchivedJsonPropertiesEntity.class, versionId);
	}
}