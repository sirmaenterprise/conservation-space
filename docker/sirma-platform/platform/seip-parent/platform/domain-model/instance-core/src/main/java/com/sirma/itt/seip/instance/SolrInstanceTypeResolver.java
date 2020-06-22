package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.INSTANCE_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.URI;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.VirtualDb;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchService;

/**
 * Extension for instance type resolving that uses solr search to determine the instance type. This should be the first
 * extension that should be run.
 *
 * @author BBonev
 */
@Extension(target = InstanceTypeResolver.TARGET_NAME, order = 10)
public class SolrInstanceTypeResolver implements InstanceTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * The number of items to be search in a single query
	 */
	private static final int FRAGMENT_SIZE = 1000;
	private static final String PROJECTION = URI + "," + INSTANCE_TYPE + "," + RDF_TYPE;

	@Inject
	private SearchService searchService;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private InstanceService instanceService;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private InstanceTypes instanceTypes;
	@Inject
	@VirtualDb
	private DbDao dbDao;

	@Override
	public Optional<InstanceType> resolve(Serializable id) {
		if (id == null || InstanceVersionService.isVersion(id)) {
			return Optional.empty();
		}

		List<Instance> list = resolve(buildQuery(Collections.singletonList(id))).get();
		if (CollectionUtils.isEmpty(list)) {
			return Optional.empty();
		}
		Instance instance = list.get(0);
		resolveInstanceType(instance);
		return Optional.ofNullable(instance.type());
	}

	@Override
	public Optional<InstanceReference> resolveReference(Serializable id) {
		if (id == null || InstanceVersionService.isVersion(id)) {
			return Optional.empty();
		}

		List<Instance> list = resolve(buildQuery(Collections.singletonList(id))).get();
		if (CollectionUtils.isEmpty(list)) {
			Instance virtual = dbDao.find(Instance.class, id);
			if (virtual != null) {
				return Optional.of(toReference(virtual));
			}
			return Optional.empty();
		}
		Instance instance = list.get(0);
		// clear the cached instance that is not valid so that when the refere.toInstance will load clean instance
		return Optional.of(toReference(instance));
	}

	@Override
	public Map<Serializable, InstanceType> resolve(Collection<Serializable> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyMap();
		}

		Collection<Serializable> filteredIds = filter(ids);
		if (CollectionUtils.isEmpty(filteredIds)) {
			return Collections.emptyMap();
		}

		Collection<Instance> list = resolveFragmented(filteredIds);
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyMap();
		}
		return list
				.stream()
					.filter(Objects::nonNull)
					.map(this::resolveInstanceType)
					.filter(instance -> instance.type() != null)
					.collect(Collectors.toMap(Instance::getId, Instance::type));
	}

	@Override
	public <S extends Serializable> Collection<InstanceReference> resolveReferences(Collection<S> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}

		Collection<Serializable> filteredIds = filter(ids);
		if (CollectionUtils.isEmpty(filteredIds)) {
			return Collections.emptyList();
		}

		Collection<Instance> list = resolveFragmented(filteredIds);
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}

		return CollectionUtils.transformToList(list, this::toReference);
	}

	private InstanceReference toReference(Instance inst) {
		return Resettable.reset(resolveInstanceType(inst).toReference());
	}

	@Override
	public <S extends Serializable> Collection<Instance> resolveInstances(Collection<S> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}

		List<Serializable> filteredIds = filter(ids);
		if (CollectionUtils.isEmpty(filteredIds)) {
			return Collections.emptyList();
		}

		// no need to call solr to fetch instance data, we need the instance data anyway
		Collection<Instance> instances = instanceService.loadByDbId(filteredIds);
		instances.forEach(this::resolveInstanceType);
		return instances;
	}

	@Override
	public <S extends Serializable> Map<S, Boolean> exist(Collection<S> identifiers) {
		if (CollectionUtils.isEmpty(identifiers)) {
			return Collections.emptyMap();
		}

		Collection<Serializable> filteredIds = filter(identifiers);
		if (CollectionUtils.isEmpty(filteredIds)) {
			return Collections.emptyMap();
		}

		Collection<Instance> list = resolveFragmented(filteredIds);
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyMap();
		}

		Set<Serializable> found = list.stream().map(Instance::getId).collect(Collectors.toSet());
		return identifiers.stream().collect(Collectors.toMap(Function.identity(), found::contains, (k1, k2) -> k1));
	}

	// such method could be defined in the interface and called before calling the actual resolver
	// TODO when optimising chaining resolver
	private static List<Serializable> filter(Collection<? extends Serializable> ids) {
		return ids.stream().filter(id -> !InstanceVersionService.isVersion(id)).collect(Collectors.toList());
	}

	private Collection<Instance> resolveFragmented(Collection<? extends Serializable> ids) {
		return FragmentedWork.doWorkWithResult(ids, FRAGMENT_SIZE, part -> resolve(buildQuery(part)).get());
	}

	private Supplier<List<Instance>> resolve(Supplier<String> querySupplier) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setDialect(SearchDialects.SOLR);
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setProjection(PROJECTION);
		arguments.setMaxSize(Integer.MAX_VALUE);
		arguments.setPageSize(Integer.MAX_VALUE);
		return () -> {
			arguments.setStringQuery(querySupplier.get());
			if (StringUtils.isBlank(arguments.getStringQuery())) {
				// query is not valid
				return Collections.emptyList();
			}
			searchService.search(Instance.class, arguments);
			return arguments.getResult();
		};
	}

	private Supplier<String> buildQuery(Collection<? extends Serializable> ids) {
		return () -> {
			String idQuery = ids
					.stream()
						.map(id -> typeConverter.convert(Uri.class, id))
						.filter(Objects::nonNull)
						.map(Object::toString)
						.map(searchService.escapeForDialect(SearchDialects.SOLR).andThen(uri -> "\"" + uri + "\""))
						.collect(Collectors.joining(" OR "));
			if (StringUtils.isBlank(idQuery)) {
				// if the passed ids are all invalid we will return null and not an invalid query id:()
				return null;
			}
			return URI + ":(" + idQuery + ")";
		};
	}

	@SuppressWarnings("unchecked")
	private Instance resolveInstanceType(Instance instance) {
		if (instance.type() != null) {
			return instance;
		}
		Serializable serializable = instance.get(RDF_TYPE);
		String semanticClass;
		if (serializable instanceof Collection) {
			semanticClass = semanticDefinitionService.getMostConcreteClass((Collection<String>) serializable);
		} else {
			semanticClass = Objects.toString(serializable, null);
		}
		instance.addIfNotNull(SEMANTIC_TYPE, semanticClass);
		Optional<InstanceType> type = instanceTypes.from(semanticClass);
		if (type.isPresent()) {
			instance.setType(type.get());
		} else {
			LOGGER.warn("Could not load type for: {} of types {}", instance.getId(), serializable);
		}
		return instance;
	}

}
