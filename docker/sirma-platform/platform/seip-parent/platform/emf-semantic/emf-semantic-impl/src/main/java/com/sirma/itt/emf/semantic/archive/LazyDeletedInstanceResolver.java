package com.sirma.itt.emf.semantic.archive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.archive.TransactionIdHolder;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchService;

/**
 * Lazy loaded for dependencies that loads small portion of instances to process. The used query is:
 * {@value #GET_ALL_CHILD_INSTANCES}. The loaded uses the batch size configuration same as
 * 'operations.delete.dependencyBatchSize'. The returned iterator is thread safe for using in multiple threads.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DependencyResolver.TARGET_NAME, order = 10, priority = 10)
public class LazyDeletedInstanceResolver implements DependencyResolver {

	private static final String MISSING_QUERY_ERROR = "Missing query for loading instance dependencies: {}";
	private static final Logger LOGGER = LoggerFactory.getLogger(LazyDeletedInstanceResolver.class);
	private static final String GET_ALL_CHILD_INSTANCES = "instanceQueries/getDeletedInstances";
	private static final String COUNT_ALL_CHILD_INSTANCES = "instanceQueries/countDeletedInstances";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "operations.delete.dependencyBatchSize", defaultValue = "50", sensitive = true, type = Integer.class, label = "The number of dependent instances to process in a single transaction. If this number is too high a transaction timeout could occur")
	private ConfigurationProperty<Integer> dependencyDeleteBatchSize;

	@Inject
	private SearchService searchService;
	@Inject
	private TransactionIdHolder idHolder;
	@Inject
	private InstanceService instanceService;
	@Inject
	private ExpressionsManager evaluator;

	@Override
	public Integer countDependencies(Instance instance) {
		Serializable transactionId = idHolder.getTransactionId();
		Serializable id = instance.getId();
		if (id == null || transactionId == null) {
			return null;
		}

		String query = String.format("${query(%s, objectUri=%s, transactionId=%s).count}", COUNT_ALL_CHILD_INSTANCES,
				id, transactionId);
		return evaluator.evaluate(query, Integer.class);
	}

	@Override
	public int currentBatchSize() {
		return dependencyDeleteBatchSize.get();
	}

	@Override
	public boolean isLazyLoadingSupported() {
		return true;
	}

	@Override
	public Iterator<Instance> resolveDependenciesLazily(Instance parent) {
		SearchArguments<SearchInstance> filter = createContext(parent);
		if (filter == null) {
			LOGGER.warn(MISSING_QUERY_ERROR, GET_ALL_CHILD_INSTANCES);
			return Collections.emptyIterator();
		}
		Integer count = countDependencies(parent);

		return new LazyQueryIterator(filter, count, currentBatchSize());
	}

	/**
	 * Creates the context.
	 *
	 * @param parent
	 *            the parent
	 * @return the search arguments
	 */
	private SearchArguments<SearchInstance> createContext(Instance parent) {
		Context<String, Object> context = new Context<>(2);
		context.put("objectUri", parent.getId());
		context.put("transactionId", idHolder.getTransactionId());
		return searchService.getFilter(GET_ALL_CHILD_INSTANCES, SearchInstance.class, context);
	}

	@Override
	public Collection<Instance> resolveDependencies(Instance parent) {
		SearchArguments<SearchInstance> filter = createContext(parent);
		if (filter == null) {
			LOGGER.warn(MISSING_QUERY_ERROR, GET_ALL_CHILD_INSTANCES);
			return Collections.emptyList();
		}

		return loadElements(filter, -1);
	}

	/**
	 * Load elements.
	 *
	 * @param arguments
	 *            the arguments
	 * @param size
	 *            the size
	 * @return the collection
	 */
	private Collection<Instance> loadElements(SearchArguments<? extends Instance> arguments, int size) {
		Collection<Instance> local = Collections.emptyList();
		arguments.setPageSize(size);
		searchService.search(Instance.class, arguments);
		if (arguments.getResult() != null && !arguments.getResult().isEmpty()) {
			List<Serializable> result = arguments.getResult().stream().map(Entity::getId).collect(Collectors.toList());
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.enable();
			try {
				local = instanceService.loadByDbId(result);
			} finally {
				Options.ALLOW_LOADING_OF_DELETED_INSTANCES.disable();
			}
		}
		return local;
	}

	/**
	 * Iterator implementation that loads it's contexts lazily.
	 *
	 * @author BBonev
	 */
	private class LazyQueryIterator implements Iterator<Instance> {

		private int storeSize = -1;
		private Iterator<Instance> storeIterator;
		private final SearchArguments<Instance> arguments;
		private int returnedInstances = 0;
		private final int batchSize;
		/** The lock object used to synchronize the access to the internal store. */
		private Lock storeLock = new ReentrantLock();
		private Integer expectedCount;

		/**
		 * Instantiates a new lazy query iterator.
		 *
		 * @param <I>
		 *            the generic type
		 * @param arguments
		 *            the arguments
		 * @param count
		 *            the expected count
		 * @param batchSize
		 *            the number of elements the iterator should load on a single query
		 */
		@SuppressWarnings("unchecked")
		<I extends Instance> LazyQueryIterator(SearchArguments<I> arguments, Integer count, Integer batchSize) {
			expectedCount = count;
			this.arguments = (SearchArguments<Instance>) arguments;
			this.batchSize = batchSize;
		}

		@Override
		public boolean hasNext() {
			storeLock.lock();
			try {
				initLocalStore();
				boolean hasNext = storeIterator.hasNext();
				// current store has finished
				// if the store was empty the first time no need to check again
				// if the store size is less that the batch size no need to check again
				if (!hasNext && storeSize > 0 && (storeSize >= batchSize) && checkForMore()) {
					loadNextBatch();
					// check again for data
					hasNext = storeIterator.hasNext();
				}
				return hasNext;
			} finally {
				storeLock.unlock();
			}
		}

		/**
		 * Check for more.
		 *
		 * @return true, if successful
		 */
		private boolean checkForMore() {
			// if we don't now the count we have to check again
			return expectedCount == null || expectedCount != returnedInstances;
		}

		/**
		 * Load next batch.
		 */
		private void loadNextBatch() {
			setStore(loadElements(arguments, batchSize));
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
			storeIterator = data.iterator();
		}

		@Override
		public Instance next() {
			storeLock.lock();
			try {
				initLocalStore();
				returnedInstances++;
				return storeIterator.next();
			} finally {
				storeLock.unlock();
			}
		}

	}

}
