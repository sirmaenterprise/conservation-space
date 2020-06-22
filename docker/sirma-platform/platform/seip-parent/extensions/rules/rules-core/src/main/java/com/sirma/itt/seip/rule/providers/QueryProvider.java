package com.sirma.itt.seip.rule.providers;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Configurable;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.rule.model.RuleQueryConfig;
import com.sirma.itt.seip.search.SearchService;

/**
 * Generic query provider that runs a query based on configuration. The resolver could use properties from the current
 * instance to call the query if needed.
 *
 * @author BBonev
 */
@Named(QueryProvider.NAME)
public class QueryProvider implements DependencyResolver, Configurable {

	public static final String LOAD_FULL_INSTANCES = "loadFullInstances";
	public static final String BATCH_LOAD_SIZE = "batchLoadSize";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String NAME = "queryProvider";
	private Boolean loadFullInstances = Boolean.FALSE;
	private int batchLoadSize = 100;

	@Inject
	private ExpressionsManager expressionsManager;
	@Inject
	protected SearchService searchService;
	@Inject
	private InstanceService instanceService;
	@Inject
	private InstanceTypes instanceTypes;
	private RuleQueryConfig queryConfig;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		queryConfig = RuleQueryConfig.parse(configuration);
		if (queryConfig == null) {
			return false;
		}
		loadFullInstances = configuration.getIfSameType(LOAD_FULL_INSTANCES, Boolean.class, Boolean.FALSE);
		batchLoadSize = configuration.getIfSameType(BATCH_LOAD_SIZE, Integer.class, 100);
		return true;
	}

	@Override
	public boolean isLazyLoadingSupported() {
		return true;
	}

	@Override
	public Integer countDependencies(Instance instance) {
		return null;
	}

	@Override
	public Iterator<Instance> resolveDependenciesLazily(Instance parent) {
		SearchArguments<? extends Instance> arguments = queryConfig.buildSearchArguments(parent, searchService,
				expressionsManager);
		if (arguments == null) {
			return Collections.emptyIterator();
		}
		// TODO refactor for JAVA 8 with method reference
		return new LazyIterator(queryConfig.beforeQueryExecute(arguments), batchLoadSize, loadFullInstances,
				searchService, instanceService, instanceTypes);
	}

	@Override
	public Collection<Instance> resolveDependencies(Instance parent) {
		return searchForInstances(parent);
	}

	/**
	 * Search for instances.
	 *
	 * @return the collection
	 */
	@SuppressWarnings("unchecked")
	private Collection<Instance> searchForInstances(Instance source) {
		SearchArguments<? extends Instance> arguments = queryConfig.buildSearchArguments(source, searchService,
				expressionsManager);
		if (arguments == null) {
			LOGGER.warn("Could not find query {} to run!", queryConfig.getQuery());
			return Collections.emptyList();
		}
		searchService.searchAndLoad(Instance.class, queryConfig.beforeQueryExecute(arguments));
		return (Collection<Instance>) arguments.getResult();
	}

	@Override
	public String toString() {
		return new StringBuilder(64).append("QueryProvider[").append(queryConfig.getQuery()).append("]").toString();
	}

	/**
	 * Iterator that fetches only the given number of elements per batch.
	 *
	 * @author BBonev
	 */
	static class LazyIterator implements Iterator<Instance> {
		private int storeSize = -1;
		private int usedFromStore = 0;
		private Iterator<Instance> storeIterator;
		private final SearchArguments<Instance> arguments;
		private int returnedInstances = 0;
		private final int batchSize;
		/** The lock object used to synchronize the access to the internal store. */
		private Lock storeLock = new ReentrantLock();

		private final SearchService searchService;
		private final InstanceService instanceService;
		private final Boolean loadFull;
		private InstanceTypes instanceTypes;

		/**
		 * Instantiates a new lazy query iterator.
		 *
		 * @param <I>
		 *            the generic type
		 * @param arguments
		 *            the arguments
		 * @param batchSize
		 *            the number of elements the iterator should load on a single query
		 * @param loadFullInstances
		 *            the load full instances
		 * @param searchService
		 *            the search service
		 * @param instanceService
		 *            the instance service
		 * @param instanceTypes
		 *            the instance types
		 */
		@SuppressWarnings("unchecked")
		<I extends Instance> LazyIterator(SearchArguments<I> arguments, int batchSize, Boolean loadFullInstances,
				SearchService searchService, InstanceService instanceService,
				InstanceTypes instanceTypes) {
			loadFull = loadFullInstances;
			this.searchService = searchService;
			this.instanceService = instanceService;
			this.instanceTypes = instanceTypes;
			this.arguments = (SearchArguments<Instance>) arguments;
			arguments.setPageSize(batchSize);
			arguments.setSkipCount(0);
			this.batchSize = batchSize;
		}

		@Override
		public boolean hasNext() {
			storeLock.lock();
			try {
				initLocalStore();
				return storeIterator.hasNext() || checkForMore();
			} finally {
				storeLock.unlock();
			}
		}

		/**
		 * Check for more data to load.
		 *
		 * @return true, if there is data for processing and <code>false</code> if we reached the end.
		 */
		private boolean checkForMore() {
			// current store has finished
			// if the store was empty the first time no need to check again
			// if the store size is less that the batch size no need to check again
			if (storeSize > 0 && storeSize >= batchSize) {
				loadNextBatch();
				return storeSize > 0;
			}
			return false;
		}

		/**
		 * Load next batch.
		 */
		private void loadNextBatch() {
			setStore(loadElements());
		}

		/**
		 * Inits the local store.
		 */
		private void initLocalStore() {
			if (storeIterator != null) {
				return;
			}
			loadNextBatch();
		}

		/**
		 * Sets the store.
		 *
		 * @param data
		 *            the new store
		 */
		private void setStore(Collection<Instance> data) {
			storeSize = data.size();
			storeIterator = null;
			storeIterator = data.iterator();
			usedFromStore = 0;
		}

		@Override
		public Instance next() {
			storeLock.lock();
			try {
				initLocalStore();
				if (usedFromStore < storeSize || usedFromStore == storeSize && checkForMore()) {
					returnedInstances++;
					usedFromStore++;
					return storeIterator.next();
				}
				throw new NoSuchElementException();
			} finally {
				storeLock.unlock();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove anything.");
		}

		private Collection<Instance> loadElements() {
			Collection<Instance> local = Collections.emptyList();
			// clear the old result and allow for gc
			arguments.setResult(null);
			// skip the already processed instances
			arguments.setSkipCount(returnedInstances);
			arguments.setPageNumber(returnedInstances / batchSize + 1);
			searchService.search(Instance.class, arguments);
			if (CollectionUtils.isNotEmpty(arguments.getResult())) {
				List<Serializable> found = arguments.getResult().stream().map(Entity::getId).collect(Collectors.toList());
				if (loadFull) {
					local = instanceService.loadByDbId(found);
				} else {
					local = arguments.getResult();
				}
			}
			instanceTypes.resolveTypes(local);
			return local;
		}
	}

}
