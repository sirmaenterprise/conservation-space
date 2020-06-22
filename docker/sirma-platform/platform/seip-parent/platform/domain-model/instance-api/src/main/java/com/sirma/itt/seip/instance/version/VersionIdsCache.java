package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primary used for optimization of the versioning process. The cache stores already versioned ids during single
 * instance processing(object properties and widgets versioning). It will use specified function to retrieving the
 * version ids only, if there are not currently present.
 *
 * @author A. Kunchev
 * @see ProcessObjectPropertiesVersionStep
 * @see ScheduleVersionContentCreate
 */
public class VersionIdsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int INITIAL_CACHE_CAPACITY = 256;
	private final Map<Serializable, Serializable> cache;
	private final Date versionDate;
	private final BiFunction<Collection<Serializable>, Date, Map<Serializable, Serializable>> retrieveMissing;

	public VersionIdsCache(Date versionDate,
			BiFunction<Collection<Serializable>, Date, Map<Serializable, Serializable>> retrieveMissing) {
		cache = createHashMap(INITIAL_CACHE_CAPACITY);
		this.versionDate = requireNonNull(versionDate, "Version date is required!");
		this.retrieveMissing = requireNonNull(retrieveMissing, "The retrieving function is required!").andThen(map -> {
			LOGGER.trace("Retrieved and cached version ids for - {}", map.keySet());
			cache.putAll(map);
			return map;
		});
	}

	/**
	 * Retrieves the version id for the passed ids. Uses the internal cache to get already versioned ids. If there are
	 * ids which version ids are not found in the cache, the method will use the function for retrieving to collect them
	 * and populate the cache.
	 *
	 * @param ids to version
	 * @return {@link Map} where the key is the original instance id and the value is the versioned id
	 */
	public Map<Serializable, Serializable> getVersioned(Collection<Serializable> ids) {
		if (cache.isEmpty()) {
			return retrieveMissing.apply(ids, versionDate);
		}

		Map<Serializable, Serializable> versioned = ids.stream().filter(cache::containsKey).collect(toMap(identity(), cache::get));
		Set<Serializable> cached = versioned.keySet();
		if (cached.containsAll(ids)) {
			return versioned;
		}

		Set<Serializable> copy = new HashSet<>(ids);
		copy.removeAll(cached);
		versioned.putAll(retrieveMissing.apply(copy, versionDate));
		return versioned;
	}
}
