package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Used to collect all {@link InstanceTypeResolver} implementations and call them. This class is skipped, because it is
 * not registered as extension.
 *
 * @author A. Kunchev
 */
// TODO Could be optimized later
@ApplicationScoped
public class ChainingInstanceTypeResolver implements InstanceTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(InstanceTypeResolver.TARGET_NAME)
	private Plugins<InstanceTypeResolver> resolvers;

	@Override
	public Optional<InstanceType> resolve(Serializable id) {
		return callOptionalResolvers(resolver -> resolver.resolve(id));
	}

	@Override
	public Optional<InstanceReference> resolveReference(Serializable id) {
		return callOptionalResolvers(resolver -> resolver.resolveReference(id));
	}

	@Override
	public Map<Serializable, InstanceType> resolve(Collection<Serializable> ids) {
		Map<Serializable, InstanceType> result = new HashMap<>(ids.size());
		for (InstanceTypeResolver resolver : resolvers) {
			result.putAll(resolver.resolve(ids));
		}

		return result;
	}

	@Override
	public <S extends Serializable> Collection<InstanceReference> resolveReferences(Collection<S> ids) {
		Collection<InstanceReference> results = callCollectionResolvers(resolver -> resolver.resolveReferences(ids));
		return restoreOrder(ids, results);
	}

	@Override
	public <S extends Serializable> Collection<Instance> resolveInstances(Collection<S> ids) {
		Collection<Instance> results = callCollectionResolvers(resolver -> resolver.resolveInstances(ids));
		return restoreOrder(ids, results);
	}

	@Override
	public <S extends Serializable> Map<S, Boolean> exist(Collection<S> identifiers) {
		return callMapResolvers(resolver -> resolver.exist(identifiers));
	}

	private static <T> Collection<T> restoreOrder(Collection<? extends Serializable> originalOrder,
			Collection<T> list) {
		if (isEmpty(list)) {
			// nothing to restore
			return createHashSet(0);
		}

		Collection<T> result = list;
		if (originalOrder instanceof List) {
			result = new ArrayList<>(list.size());
			Map<Serializable, T> mapping = buildIdsMap(list);
			for (Serializable id : originalOrder) {
				addNonNullValue(result, mapping.get(id));
			}
			// this may happen if the ids are in some different format and are not directly mappable
			if (result.isEmpty()) {
				LOGGER.warn("Could not restore order for instance resolving");
				result = list;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> Map<Serializable, T> buildIdsMap(Collection<T> list) {
		Map<Serializable, T> mapping = createHashMap(list.size());
		for (T element : list) {
			if (element instanceof Instance) {
				Instance instance = (Instance) element;
				mapping.put(instance.getId(), (T) instance);
			} else if (element instanceof InstanceReference) {
				InstanceReference reference = (InstanceReference) element;
				mapping.put(reference.getId(), (T) reference);
			}
		}
		return mapping;
	}

	private <T> Optional<T> callOptionalResolvers(Function<InstanceTypeResolver, Optional<T>> resolverFunc) {
		for (InstanceTypeResolver resolver : resolvers) {
			Optional<T> type = resolverFunc.apply(resolver);
			if (type.isPresent()) {
				return type;
			}
		}

		return Optional.empty();
	}

	private <T> Collection<T> callCollectionResolvers(Function<InstanceTypeResolver, Collection<T>> resolverFunc) {
		Collection<T> result = new LinkedList<>();
		for (InstanceTypeResolver resolver : resolvers) {
			result.addAll(resolverFunc.apply(resolver));
		}

		return result;
	}

	private <K, V> Map<K, V> callMapResolvers(Function<InstanceTypeResolver, Map<K, V>> resolverFunc) {
		Map<K, V> result = new LinkedHashMap<>();
		for (InstanceTypeResolver resolver : resolvers) {
			result.putAll(resolverFunc.apply(resolver));
		}

		return result;
	}
}
