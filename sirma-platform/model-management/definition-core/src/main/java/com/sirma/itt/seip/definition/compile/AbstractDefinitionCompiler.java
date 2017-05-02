package com.sirma.itt.seip.definition.compile;

import static com.sirma.itt.seip.definition.util.DefinitionIdentityUtil.createDefinitionId;
import static com.sirma.itt.seip.definition.util.DefinitionIdentityUtil.createParentDefinitionId;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.MutableDictionaryService;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerCallback.DefinitionReferenceResolver;
import com.sirma.itt.seip.definition.util.DefinitionIdentityUtil;
import com.sirma.itt.seip.definition.util.DefinitionSorterCallback;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.Mergeable;
import com.sirma.itt.seip.domain.definition.ReferenceDefinitionModel;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Base class that provides common functions for implementing a definition compiler algoritms.
 *
 * @author BBonev
 */
public abstract class AbstractDefinitionCompiler implements DefinitionCompilerAlgorithm {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final DefinitionSorterCallback<TopLevelDefinition> REFERENCE_DEFINITION_SORTER = new ReferenceDefinitionSorter();
	private static final DefinitionSorterCallback<TopLevelDefinition> PARENT_DEFINITION_SORTER = new ParentChildDefinitionSorter();
	/** The mutable dictionary service. */
	@Inject
	protected MutableDictionaryService mutableDictionaryService;
	/** The definition management service. */
	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	protected DefinitionCompilerHelper compilerHelper;

	@Inject
	protected DictionaryService dictionaryService;

	/**
	 * Submit task.
	 *
	 * @param task
	 *            the task
	 */
	protected void submitTask(GenericAsyncTask task) {
		compilerHelper.submitTask(task);
	}

	/**
	 * Prepare for persist and validate.
	 *
	 * @param list
	 *            the list
	 * @param persist
	 *            the persist
	 * @param callback
	 *            the callback
	 * @return the list
	 */
	protected List<String> prepareForPersistAndValidate(List<?> list, boolean persist,
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
		List<String> result = new LinkedList<>();
		for (Iterator<?> it = list.iterator(); it.hasNext();) {
			Object object = it.next();
			if (object instanceof TopLevelDefinition) {
				TopLevelDefinition definition = (TopLevelDefinition) object;
				// update field paths
				callback.normalizeFields(definition);

				callback.prepareForPersist(definition);

				// // update fields again, if they are modified on prepare
				callback.normalizeFields(definition);

				if (definition instanceof BidirectionalMapping) {
					BidirectionalMapping bidirectional = (BidirectionalMapping) definition;
					bidirectional.initBidirection();
				}

				// validate the definition
				if (!callback.validateCompiledDefinition(definition)) {
					String message = "Found errors while validating " + definition.getIdentifier() + ". Skipping it!";
					LOGGER.error("\n=======================================================================\n"
							+ message + "\n=======================================================================");
					ValidationLoggingUtil.addErrorMessage(message);
					it.remove();
					// we have failed to persist this definition
					result.add(createDefinitionId(definition));
				} else if (persist) {
					// if the definition is valid and we want to persist it then we do it
					persistDefinition(definition, callback);
				}
			}
		}
		return result;
	}

	/**
	 * Clone the given definition.
	 *
	 * @param src
	 *            the source definition to copy from. The definition should be
	 * @return the definition copy {@link Mergeable}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected TopLevelDefinition cloneDefinition(TopLevelDefinition src) {
		TopLevelDefinition copy = org.dozer.util.ReflectionUtils.newInstance(src.getClass());
		if (copy instanceof Mergeable && src instanceof Mergeable) {
			((Mergeable) copy).mergeFrom(src);
		}
		return copy;
	}

	/**
	 * Persist the list of valid case definitions.
	 *
	 * @param <V>
	 *            the value type
	 * @param definition
	 *            the definition
	 * @param callback
	 *            the callback
	 */
	protected <V extends TopLevelDefinition> void persistDefinition(V definition,
			DefinitionCompilerCallback<V> callback) {

		String definitionId = definition.getIdentifier();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing compiled definition: {}", definitionId);
		} else {
			LOGGER.trace("Processing compiled definition: {}", definition);
		}

		// added container check - should not compare 2 definitions
		// that are from 2 different containers(sites)
		V existing = callback.findTemplateInSystem(definitionId);
		boolean equals = false;
		if (existing != null) {
			LOGGER.info("There is an existing definition with ID: {} - checking if update is needed",
					definitionId);
			equals = mutableDictionaryService.isDefinitionEquals(definition, existing);
			// if the definitions differ we increase revision number
			// otherwise do nothing
			if (!equals) {
				definition.setRevision(existing.getRevision().longValue() + 1L);
				LOGGER.info("Definitions are not equal. Increasing revision to {}", definition.getRevision());
			} else {
				LOGGER.debug("Definitions are equal. Nothing is changed");
				return;
			}
		}
		if (!equals) {
			// update the revision of all properties
			callback.setPropertyRevision(definition);
		}

		callback.saveTemplateProperties(definition, existing);

		if (!equals) {
			callback.saveTemplate(definition);
		}
	}

	/**
	 * Copy base definitions to local containers.
	 *
	 * @param baseDefinitions
	 *            the base definitions
	 * @param targetDefinitions
	 *            the target definitions
	 */
	protected void copyBaseDefinitionsToLocalContainers(Map<String, TopLevelDefinition> baseDefinitions,
			Map<String, TopLevelDefinition> targetDefinitions) {
		if (baseDefinitions.isEmpty()) {
			return;
		}
		Set<String> enabledContainers = getEnabledContainers();
		LOGGER.debug("Copying {} to {}", baseDefinitions.keySet(), enabledContainers);
		for (String containerId : enabledContainers) {
			for (Entry<String, TopLevelDefinition> entry : baseDefinitions.entrySet()) {
				// add a copy only if there is no overridden entry
				// local entries are with higher priority
				if (targetDefinitions.containsKey(entry.getKey() + DefinitionIdentityUtil.SEPARATOR + containerId)) {
					LOGGER.trace("Found {} in {}. Skipping it", entry.getKey(), containerId);
					continue;
				}
				LOGGER.trace("Adding {} to {}", entry.getKey(), containerId);
				TopLevelDefinition clone = cloneDefinition(entry.getValue());
				// set the new container and add to the rest of definitions
				clone.setContainer(containerId);
				targetDefinitions.put(createDefinitionId(clone), clone);
			}
		}
	}

	/**
	 * Gets the enabled containers.
	 *
	 * @return the enabled containers
	 */
	protected Set<String> getEnabledContainers() {
		return definitionManagementService.getEnabledEmfContainers();
	}

	/**
	 * Compile case definitions. Propagates all parent data to their children without overriding it.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the cases
	 * @param callback
	 *            the callback
	 * @return the list if valid definitions
	 */
	protected <E extends TopLevelDefinition> List<TopLevelDefinition> compileDefinitions(Map<String, E> definitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (definitions.isEmpty()) {
			return Collections.emptyList();
		}

		CompilationProcessHolder<E> holder = new CompilationProcessHolder<>(definitions, dictionaryService);

		for (Entry<String, E> entry : definitions.entrySet()) {
			LOGGER.trace("Started processing of {}", entry.getKey());
			if (!holder.canProcess(entry.getKey())) {
				LOGGER.trace("Will not process {} due to: already processed or found errors.", entry.getKey());
				continue;
			}
			E impl = entry.getValue();

			if (impl instanceof BidirectionalMapping) {
				((BidirectionalMapping) impl).initBidirection();
			}

			// if we have a parent we start validating it
			if (impl.getParentDefinitionId() != null) {
				mergeWithParent(definitions, callback, entry, holder);
			}
			// for top level definitions we merge them only with themselves to
			// expand references
			int oldErrors = holder.errorCount();

			merge(impl, null, holder, callback);

			holder.handled(entry, oldErrors);
		}

		logCompilationErrors(holder);
		return holder.getResult();
	}

	/**
	 * Merge with parent.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @param callback
	 *            the callback
	 * @param entry
	 *            the entry
	 * @param holder
	 *            the holder
	 */
	private <E extends TopLevelDefinition> void mergeWithParent(Map<String, E> definitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, Entry<String, E> entry,
			CompilationProcessHolder<E> holder) {

		E impl = entry.getValue();
		String parentId = createParentDefinitionId(impl);
		// first check if the parent exists at all
		if (!definitions.containsKey(parentId)) {
			LOGGER.trace("Parent definition {} not found in current session!", parentId);
			holder.addUnresolved(entry.getKey(), parentId);
			return;
		}
		// check if we have errors in that parent or is not resolved
		// properly
		if (holder.isFailed(parentId)) {
			String id = createDefinitionId(impl);
			holder.addError(id, impl);
			String message = "Found errors in parent definition " + parentId + " for : " + id + ". Skipping it";
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
			return;
		}

		E parent = definitions.get(parentId);

		int oldErrors = holder.errorCount();
		merge(impl, parent, holder, callback);

		holder.handled(entry, oldErrors);

		return;
	}

	/**
	 * Log compilation errors.
	 *
	 * @param <E>
	 *            the element type
	 * @param holder
	 *            the holder
	 */
	private <E extends TopLevelDefinition> void logCompilationErrors(CompilationProcessHolder<E> holder) {

		Map<String, String> unresolved = holder.unresolved;
		Map<String, E> errors = holder.errors;

		if (!errors.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			String str = "Found errors in the following definitions: ";
			builder.append(str);
			LOGGER.error(str);
			for (E definition : errors.values()) {
				str = "  >> " + definition.getIdentifier();
				LOGGER.error(str);
				builder.append("\n").append(str);
				LOGGER.trace("  >> {}", definition);
			}
			LOGGER.info("NOTE: To see the complete broken definitions enable TRACE logging!");
			LOGGER.error("<<");
			builder.append("\n").append("<<");
			ValidationLoggingUtil.addErrorMessage(builder.toString());
		}

		if (!unresolved.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			String message = "Failed to resolve dependences for definitions: ";
			builder.append(message);
			LOGGER.error(message);
			for (Entry<String, String> entry : unresolved.entrySet()) {
				message = "  >> definition \"" + entry.getKey() + "\" with parent \"" + entry.getValue() + "\"";
				LOGGER.error(message);
				builder.append("\n").append(message);
			}
			LOGGER.error("<<");
			builder.append("\n").append("<<");
			ValidationLoggingUtil.addErrorMessage(builder.toString());
		}
	}

	/**
	 * Sorts the given definitions from parents to children.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @return the unresolved dependences
	 */
	protected <E> Map<String, String> sortDefinitions(Map<String, TopLevelDefinition> definitions) {

		Map<String, String> unresolved = new LinkedHashMap<>();

		unresolved.putAll(DefinitionUtil.sortDefinitionsUsingSorter(definitions, PARENT_DEFINITION_SORTER));
		unresolved.putAll(DefinitionUtil.sortDefinitionsUsingSorter(definitions, REFERENCE_DEFINITION_SORTER));

		LOGGER.debug("After sorting of definitions: {}", definitions.keySet());
		if (!unresolved.isEmpty()) {
			String message = "Failed to resolve dependences for definitions: ";
			StringBuilder builder = new StringBuilder();
			LOGGER.error(message);
			builder.append(message);
			for (Entry<String, String> entry : unresolved.entrySet()) {
				message = "  >> definition \"" + entry.getKey() + "\" with parent \"" + entry.getValue() + "\"";
				LOGGER.error(message);
				builder.append("\n").append(message);
			}
			LOGGER.error("<<");
			builder.append("\n").append("<<");
			ValidationLoggingUtil.addErrorMessage(builder.toString());
		}
		return unresolved;
	}

	/**
	 * Wait for cache warm up.
	 *
	 * @param task
	 *            the task
	 */
	protected void waitForCacheWarmUp(ForkJoinTask<?> task) {
		try {
			if (task != null && !task.isDone()) {
				// wait for the cache to warm up
				task.get();
			}
		} catch (Exception e) {
			LOGGER.warn("Exception during waitng for cache warmup. Will continue anyway", e);
		}
	}

	/**
	 * Merge into the target definition the given source definition. Updates the document reference IDs.
	 *
	 * @param <E>
	 *            the element type
	 * @param target
	 *            the target
	 * @param src
	 *            the src
	 * @param holder
	 *            the holder
	 * @param callback
	 *            the callback
	 * @return the case definition impl
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <E extends TopLevelDefinition> E merge(E target, E src, CompilationProcessHolder<E> holder,
			DefinitionCompilerCallback<TopLevelDefinition> callback) {

		// do not merge top level definitions
		if (target != null && src != null) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Merging definition {}  with {}", createDefinitionId(target),
						createDefinitionId(src));
				LOGGER.trace("BEFORE: {}", target);
			}
			if (target instanceof Mergeable && src instanceof Mergeable) {
				((Mergeable) target).mergeFrom(src);
			}
			LOGGER.trace("AFTER : {}" + target);
		}

		LOGGER.trace("Updating refernces after the first merge");
		// add passing the current context to the method so it could resolve the dependencies
		// that are not persisted, yet
		if (!callback.updateReferences(target, holder)) {
			String id = createDefinitionId(target);
			holder.addError(id, target);
			String message = "Found missing reference dependency for definition: " + id + ". Skipping it.";
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
			return target;
		}
		return target;
	}

	/**
	 * The Enum ErrorResolution.
	 */
	static enum ErrorResolution {
		/** The fix. */
		FIX, /** The ignore. */
		IGNORE, /** The warn. */
		WARN, /** The error. */
		ERROR;
	}

	/**
	 * Asynchronous task for cache warm up.
	 *
	 * @author BBonev
	 */
	class CacheWarmUpTask extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -2161232307727949681L;
		/** The callback. */
		private DefinitionCompilerCallback<?> callback;

		/**
		 * Instantiates a new cache warm up task.
		 *
		 * @param callback
		 *            the callback
		 */
		public CacheWarmUpTask(DefinitionCompilerCallback<?> callback) {
			this.callback = callback;
		}

		@Override
		protected boolean executeTask() throws Exception {
			TimeTracker tracker = new TimeTracker().begin();
			// asynch executing of the method warmUpCache
			callback.warmUpCache();

			LOGGER.debug("Definition cache warm up for " + callback.getCallbackName() + " took "
					+ tracker.stopInSeconds() + " s");
			return true;
		}
	}

	/**
	 * Sorter callback implementation to handle basic parent/child inheritance when sorting
	 *
	 * @author BBonev
	 */
	static class ParentChildDefinitionSorter implements DefinitionSorterCallback<TopLevelDefinition> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String createDefinitionId(TopLevelDefinition definition) {
			return DefinitionIdentityUtil.createDefinitionId(definition);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String createParentDefinitionId(TopLevelDefinition definition) {
			return DefinitionIdentityUtil.createParentDefinitionId(definition);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getParentDefinitionId(TopLevelDefinition topLevelDefinition) {
			return topLevelDefinition.getParentDefinitionId();
		}

		@Override
		public String toString() {
			return "parent-child callback";
		}

		@Override
		public String getId(TopLevelDefinition definition) {
			return definition.getIdentifier();
		}
	}

	/**
	 * Sorter callback implementation to handle reference inheritance when sorting
	 *
	 * @author BBonev
	 */
	static class ReferenceDefinitionSorter implements DefinitionSorterCallback<TopLevelDefinition> {

		/**
		 * Gets the parent definition id.
		 *
		 * @param topLevelDefinition
		 *            the top level definition
		 * @return the parent definition id
		 */
		@Override
		public String getParentDefinitionId(TopLevelDefinition topLevelDefinition) {
			if (topLevelDefinition instanceof ReferenceDefinitionModel) {
				return ((ReferenceDefinitionModel) topLevelDefinition).getReferenceId();
			}
			return null;
		}

		/**
		 * Creates the definition id.
		 *
		 * @param definition
		 *            the definition
		 * @return the string
		 */
		@Override
		public String createDefinitionId(TopLevelDefinition definition) {
			return DefinitionIdentityUtil.createDefinitionId(definition);
		}

		/**
		 * Creates the parent definition id.
		 *
		 * @param definition
		 *            the definition
		 * @return the string
		 */
		@Override
		public String createParentDefinitionId(TopLevelDefinition definition) {
			if (definition instanceof ReferenceDefinitionModel) {
				return DefinitionIdentityUtil.createReferenceDefinitionId((ReferenceDefinitionModel) definition);
			}
			return null;
		}

		@Override
		public String toString() {
			return "Reference-definition callback";
		}

		@Override
		public String getId(TopLevelDefinition definition) {
			return definition.getIdentifier();
		}
	}

	/**
	 * Helper class/DTO for definition compilation. Provides methods for accessing the data during compilation.
	 *
	 * @param <E>
	 *            the element type
	 */
	private class CompilationProcessHolder<E extends TopLevelDefinition> implements DefinitionReferenceResolver {
		/** The currently processed definitions. */
		final Map<String, E> definitions;
		final Map<String, String> unresolved;
		final DictionaryService dictionary;
		final Set<String> handled = new HashSet<>();
		final Map<String, E> errors = new LinkedHashMap<>();
		final List<TopLevelDefinition> result = new LinkedList<>();

		/**
		 * Instantiates a new compilation process holder.
		 *
		 * @param definitions
		 *            the definitions
		 * @param dictionaryService
		 *            the dictionary service
		 */
		@SuppressWarnings("unchecked")
		public CompilationProcessHolder(Map<String, E> definitions, DictionaryService dictionaryService) {
			this.definitions = definitions;
			dictionary = dictionaryService;
			unresolved = sortDefinitions((Map<String, TopLevelDefinition>) this.definitions);
		}

		/**
		 * Resolve definition from the currently processing definitions.
		 *
		 * @param identifier
		 *            the identifier
		 * @return the from processing
		 */
		public E getFromProcessing(String identifier) {
			return definitions.get(identifier);
		}

		/**
		 * Checks if given definition id could be processed. It's could be processed if is not already handled and is
		 * not marked with erros.
		 *
		 * @param key
		 *            the key
		 * @return true, if can be processed
		 */
		boolean canProcess(String key) {
			return !(handled.contains(key) || errors.containsKey(key));
		}

		/**
		 * Checks if the given definition id is processed with errors or has unresolved dependencies.
		 *
		 * @param key
		 *            the key
		 * @return true, if is marked for failed
		 */
		boolean isFailed(String key) {
			return errors.containsKey(key) || unresolved.containsKey(key);
		}

		/**
		 * Mark the given entry as successfully handled if the error count is the same as provided
		 *
		 * @param entry
		 *            the entry
		 * @param oldErrorCount
		 *            the old error count to check
		 */
		void handled(Entry<String, E> entry, int oldErrorCount) {
			if (oldErrorCount == errorCount()) {
				handled.add(entry.getKey());
				getResult().add(entry.getValue());
			}
		}

		/**
		 * Returns the current error count.
		 *
		 * @return the error count
		 */
		int errorCount() {
			return errors.size();
		}

		/**
		 * Register an error
		 *
		 * @param key
		 *            the key
		 * @param failed
		 *            the failed
		 */
		void addError(String key, E failed) {
			errors.put(key, failed);
		}

		/**
		 * Registers that a definition depends on other definition that is not resolvable.
		 *
		 * @param key
		 *            the key
		 * @param failed
		 *            the failed
		 */
		void addUnresolved(String key, String failed) {
			unresolved.put(key, failed);
		}

		/**
		 * Returns the final result from the compilation
		 *
		 * @return the result
		 */
		public List<TopLevelDefinition> getResult() {
			return result;
		}

		@Override
		public <V extends TopLevelDefinition> V resolve(Class<V> type, String identifier) {
			E processing = getFromProcessing(identifier);
			if (processing != null && type.isInstance(processing)) {
				return type.cast(processing);
			}
			return type.cast(dictionary.find(identifier));
		}
	}
}
