package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.transformToList;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension for instance type resolving that uses relational DB and more precisely archived tables to determine the
 * instance type. This should be run after solr resolving.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceTypeResolver.TARGET_NAME, enabled = true, order = 20)
public class VersionInstanceTypeResolver implements InstanceTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private InstanceTypes instanceTypes;

	@Override
	public Optional<InstanceType> resolve(Serializable id) {
		return resolveFromInstanceInternal(id, Instance::type);
	}

	@Override
	public Optional<InstanceReference> resolveReference(Serializable id) {
		return resolveFromInstanceInternal(id, Instance::toReference);
	}

	private <R> Optional<R> resolveFromInstanceInternal(Serializable id, Function<Instance, R> toApply) {
		if (id == null || !InstanceVersionService.isVersion(id)) {
			return Optional.empty();
		}

		Instance instance = instanceVersionService.loadVersion(id);
		if (instance == null) {
			return Optional.empty();
		}

		resolveType(instance);
		return Optional.ofNullable(toApply.apply(instance));
	}

	@Override
	public Map<Serializable, InstanceType> resolve(Collection<Serializable> ids) {
		if (isEmpty(ids)) {
			return Collections.emptyMap();
		}

		Collection<Serializable> filtered = filter(ids);
		if (filtered.isEmpty()) {
			return Collections.emptyMap();
		}

		Collection<Instance> versions = instanceVersionService.loadVersionsById(filtered);
		if (versions.isEmpty()) {
			return Collections.emptyMap();
		}

		return versions
				.stream()
					.filter(Objects::nonNull)
					.map(this::resolveType)
					.filter(instance -> instance.type() != null)
					.collect(Collectors.toMap(Instance::getId, Instance::type));
	}

	@Override
	public <S extends Serializable> Collection<InstanceReference> resolveReferences(Collection<S> ids) {
		if (isEmpty(ids)) {
			return Collections.emptyList();
		}

		Collection<S> filtered = filter(ids);
		if (filtered.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Instance> foundInstances = instanceVersionService.loadVersionsById(filtered);
		if (foundInstances.isEmpty()) {
			return Collections.emptyList();
		}

		return transformToList(foundInstances, Instance::toReference);
	}

	@Override
	public <S extends Serializable> Collection<Instance> resolveInstances(Collection<S> ids) {
		if (isEmpty(ids)) {
			return Collections.emptyList();
		}

		Collection<S> filtered = filter(ids);
		if (filtered.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Instance> results = instanceVersionService.loadVersionsById(filtered);
		results.forEach(this::resolveType);
		return results;
	}

	@Override
	public <S extends Serializable> Map<S, Boolean> exist(Collection<S> identifiers) {
		if (isEmpty(identifiers)) {
			return Collections.emptyMap();
		}

		Collection<S> filtered = filter(identifiers);
		if (filtered.isEmpty()) {
			return Collections.emptyMap();
		}

		return instanceVersionService.exits(filtered);
	}

	// such method could be defined in the interface and called before calling the actual resolver
	// TODO when optimising chaining resolver
	private static <S extends Serializable> Collection<S> filter(Collection<S> ids) {
		return ids.stream().filter(InstanceVersionService::isVersion).collect(Collectors.toList());
	}

	private Instance resolveType(Instance instance) {
		if (instance.type() != null) {
			return instance;
		}

		Serializable rdfType = instance.get(SEMANTIC_TYPE);
		Optional<InstanceType> type = instanceTypes.from(rdfType);
		if (type.isPresent()) {
			instance.setType(type.get());
		} else {
			LOGGER.warn("Could not load type for: {} of types {}", instance.getId(), rdfType);
		}
		return instance;
	}

}
