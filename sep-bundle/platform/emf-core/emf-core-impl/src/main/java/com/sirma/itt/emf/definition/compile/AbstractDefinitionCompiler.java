package com.sirma.itt.emf.definition.compile;

import static com.sirma.itt.emf.definition.DefinitionIdentityUtil.createDefinitionId;
import static com.sirma.itt.emf.definition.DefinitionIdentityUtil.createParentDefinitionId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.ReferenceDefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.ValidationLoggingUtil;

/**
 * Base class that provides common functions for implementing a definition compiler algoritms.
 *
 * @author BBonev
 */
public abstract class AbstractDefinitionCompiler implements DefinitionCompilerAlgorithm {

	/** The Constant REFERENCE_DEFINITION_SORTER. */
	private static final DefinitionSorterCallback REFERENCE_DEFINITION_SORTER = new ReferenceDefinitionSorter();

	/** The Constant PARENT_DEFINITION_SORTER. */
	private static final DefinitionSorterCallback PARENT_DEFINITION_SORTER = new ParentChildDefinitionSorter();
	/** The logger. */
	@Inject
	protected Logger LOGGER;
	/** The debug. */
	protected boolean debug;
	/** The trace. */
	protected boolean trace;
	/** The mutable dictionary service. */
	@Inject
	protected MutableDictionaryService mutableDictionaryService;
	/** The definition management service. */
	@Inject
	private DefinitionManagementService definitionManagementService;

	/** Pool for the available processors. */
	protected static ForkJoinPool pool;

	/** The initialize default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.INITIALIZE_DEFAULT_CONTAINER_ONLY, defaultValue = "false")
	private Boolean initializeDefaultContainer;

	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;

	static {
		// Create pool size with double the threads per processor
		pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() << 1);
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
		List<String> result = new LinkedList<String>();
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
					String message = "Found errors while validating " + definition.getIdentifier()
							+ ". Skipping it!";
					LOGGER.error("\n=======================================================================\n"
							+ message
							+ "\n=======================================================================");
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
		if ((copy instanceof Mergeable) && (src instanceof Mergeable)) {
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

		String definitionId = createDefinitionId(definition);
		if (debug) {
			LOGGER.debug("Processing compiled definition: " + definitionId);
		} else if (trace) {
			LOGGER.trace("Processing compiled definition: " + definition);
		}

		// added container check - should not compare 2 definitions
		// that are from 2 different containers(sites)
		V existing = callback.findTemplateInSystem(definitionId);
		boolean equals = false;
		if (existing != null) {
			LOGGER.info("There is an existing definition with ID: " + definitionId
					+ " - checking if update is needed");
			equals = mutableDictionaryService.isDefinitionEquals(definition, existing);
			// if the definitions differ we increase revision number
			// otherwise do nothing
			if (!equals) {
				definition.setRevision(existing.getRevision() + 1);
				LOGGER.info("Definitions are not equal. Increasing revision to "
						+ definition.getRevision());
			} else {
				if (debug) {
					LOGGER.debug("Definitions are equal. Nothing is changed");
				}
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
	protected void copyBaseDefinitionsToLocalContainers(
			Map<String, TopLevelDefinition> baseDefinitions,
			Map<String, TopLevelDefinition> targetDefinitions) {
		if (baseDefinitions.isEmpty()) {
			return;
		}
		Set<String> enabledContainers = getEnabledContainers();
		if (debug) {
			LOGGER.debug("Copying " + baseDefinitions.keySet() + " to " + enabledContainers);
		}
		for (String containerId : enabledContainers) {
			for (Entry<String, TopLevelDefinition> entry : baseDefinitions.entrySet()) {
				// add a copy only if there is no overridden entry
				// local entries are with higher priority
				if (targetDefinitions.containsKey(entry.getKey() + DefinitionIdentityUtil.SEPARATOR
						+ containerId)) {
					if (trace) {
						LOGGER.trace("Found " + entry.getKey() + " in " + containerId
								+ ". Skipping it");
					}
					continue;
				}
				if (trace) {
					LOGGER.trace("Adding " + entry.getKey() + " to " + containerId);
				}
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
		// by default we accept all definitions
		if (Boolean.FALSE.equals(initializeDefaultContainer)) {
			return definitionManagementService.getEnabledEmfContainers();
		}
		return new HashSet<String>(Arrays.asList(defaultContainer));
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
	protected <E extends TopLevelDefinition> List<TopLevelDefinition> compileDefinitions(
			Map<String, E> definitions, DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (definitions.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, String> unresolved = sortDefinitions(definitions);

		List<TopLevelDefinition> result = new LinkedList<TopLevelDefinition>();
		Set<String> handled = new HashSet<String>();

		Map<String, E> errors = new LinkedHashMap<String, E>();
		for (Entry<String, E> entry : definitions.entrySet()) {
			if (trace) {
				LOGGER.trace("Started processing of " + entry.getKey());
			}
			if (handled.contains(entry.getKey()) || errors.containsKey(entry.getKey())
			/* || unresolved.containsValue(entry.getKey()) */) {
				if (trace) {
					LOGGER.trace("Will not process " + entry.getKey()
							+ " due to: already processed or found errors.");
				}
				continue;
			}
			E impl = entry.getValue();

			if (impl instanceof BidirectionalMapping) {
				((BidirectionalMapping) impl).initBidirection();
			}

			String parentId = createParentDefinitionId(impl);
			// if we have a parent we start validating it
			if (impl.getParentDefinitionId() != null) {
				// first check if the parent exists at all
				if (!definitions.containsKey(parentId)) {
					if (trace) {
						LOGGER.trace("Parent definition " + parentId
								+ " not found in current session!");
					}
					unresolved.put(entry.getKey(), parentId);
					continue;
				}
				// check if we have errors in that parent or is not resolved
				// properly
				if (errors.containsKey(parentId) || unresolved.containsKey(parentId)) {
					String id = createDefinitionId(impl);
					errors.put(id, impl);
					String message = "Found errors in parent definition " + parentId + " for : "
							+ id + ". Skipping it";
					LOGGER.error(message);
					ValidationLoggingUtil.addErrorMessage(message);
					continue;
				}

				E parent = definitions.get(parentId);

				int oldErrors = errors.size();
				merge(impl, parent, errors, callback);
				if (oldErrors == errors.size()) {
					handled.add(entry.getKey());
					result.add(impl);
				}
				continue;
			}
			// for top level definitions we merge them only with themselves to
			// expand references
			int oldErrors = errors.size();
			merge(impl, impl, errors, callback);
			if (oldErrors == errors.size()) {
				handled.add(entry.getKey());
				result.add(impl);
			}
		}

		if (!errors.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			String str = "Found errors in the following definitions: ";
			builder.append(str);
			LOGGER.error(str);
			for (E definition : errors.values()) {
				str = "  >> " + definition.getIdentifier();
				LOGGER.error(str);
				builder.append("\n").append(str);
				if (trace) {
					LOGGER.trace("  >> " + definition);
				}
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
				message = "  >> definition \"" + entry.getKey() + "\" with parent \""
						+ entry.getValue() + "\"";
				LOGGER.error(message);
				builder.append("\n").append(message);
			}
			LOGGER.error("<<");
			builder.append("\n").append("<<");
			ValidationLoggingUtil.addErrorMessage(builder.toString());
		}
		return result;
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
	protected <E extends TopLevelDefinition> Map<String, String> sortDefinitions(
			Map<String, E> definitions) {

		Map<String, String> unresolved = new LinkedHashMap<>();

		unresolved.putAll(sortDefinitions(definitions, PARENT_DEFINITION_SORTER));
		unresolved.putAll(sortDefinitions(definitions, REFERENCE_DEFINITION_SORTER));

		if (debug) {
			LOGGER.debug("After sorting of definitions: " + definitions.keySet());
		}
		if (!unresolved.isEmpty()) {
			String message = "Failed to resolve dependences for definitions: ";
			StringBuilder builder = new StringBuilder();
			LOGGER.error(message);
			builder.append(message);
			for (Entry<String, String> entry : unresolved.entrySet()) {
				message = "  >> definition \"" + entry.getKey() + "\" with parent \""
						+ entry.getValue() + "\"";
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
	 * Sort definitions.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @param sorter
	 *            the sorter to use
	 * @return the map
	 */
	protected <E extends TopLevelDefinition> Map<String, String> sortDefinitions(
			Map<String, E> definitions, DefinitionSorterCallback sorter) {
		if (debug) {
			LOGGER.debug("Started sorting using " + sorter + " of definitions: "
					+ definitions.keySet());
		}
		Map<String, String> unresolved = new HashMap<String, String>();

		Map<String, E> result = new LinkedHashMap<String, E>();
		for (Entry<String, E> entry : definitions.entrySet()) {
			if (trace) {
				LOGGER.trace("Processing definition " + entry.getKey());
			}
			if (sorter.getParentDefinitionId(entry.getValue()) == null) {
				if (trace) {
					LOGGER.trace("Definition " + entry.getKey()
							+ " does not have parent. Added to result.");
				}
				result.put(entry.getKey(), entry.getValue());
			} else {
				if (trace) {
					LOGGER.trace("Definition " + entry.getKey() + " have parent "
							+ sorter.createParentDefinitionId(entry.getValue()));
				}
				if (EqualsHelper.nullSafeEquals(entry.getValue().getIdentifier(),
						sorter.getParentDefinitionId(entry.getValue()), true)) {
					ValidationLoggingUtil
							.addErrorMessage("A definition cannot reference to itself via parentId. Definition ID="
									+ sorter.createParentDefinitionId(entry.getValue()));
					unresolved.put(entry.getValue().getIdentifier(), entry.getValue().getIdentifier());
					continue;
				}
				if (!result.containsKey(entry.getKey())) {
					E e = entry.getValue();

					Map<String, E> defTree = new LinkedHashMap<String, E>();
					defTree.put(sorter.createDefinitionId(e), e);

					while (sorter.getParentDefinitionId(e) != null) {
						String uniqueParentDef = sorter.createParentDefinitionId(e);
						if (trace) {
							LOGGER.trace("Checking parent " + uniqueParentDef);
						}
						if (!definitions.containsKey(uniqueParentDef)) {
							// error - no parent definition for definition 'e'
							unresolved.put(sorter.createDefinitionId(e), uniqueParentDef);
							if (trace) {
								LOGGER.trace("Parent definition not found " + uniqueParentDef
										+ " added to unresoled list.");
							}
							break;
						}
						E parent = definitions.get(uniqueParentDef);
						if (trace) {
							LOGGER.trace("Added parent for sorting "
									+ sorter.createDefinitionId(parent));
						}
						defTree.put(sorter.createDefinitionId(parent), parent);
						e = parent;
					}

					// here if we have more then 2 elements we need to
					// prepare them for the second pass
					if (!defTree.isEmpty()) {
						List<String> keys = new ArrayList<String>(defTree.keySet());
						if (trace) {
							LOGGER.trace("Hierarchy need sorting and second time processing. Updating second pass list with "
									+ keys);
						}
						// reverse the order and add them for second pass
						Collections.reverse(keys);
						for (String key : keys) {
							result.put(key, defTree.get(key));
						}
					} else {
						if (trace) {
							LOGGER.trace("No valid parents found for definition " + entry.getKey());
						}
					}
				} else {
					if (trace) {
						LOGGER.trace("Definition " + entry.getKey() + " already processed.");
					}
				}
			}
		}

		// updating result
		definitions.clear();
		definitions.putAll(result);
		return unresolved;
	}

	/**
	 * Merge into the target definition the given source definition. Updates the document reference
	 * IDs.
	 *
	 * @param <E>
	 *            the element type
	 * @param target
	 *            the target
	 * @param src
	 *            the src
	 * @param errors
	 *            the errors
	 * @param callback
	 *            the callback
	 * @return the case definition impl
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <E extends TopLevelDefinition> E merge(E target, E src, Map<String, E> errors,
			DefinitionCompilerCallback<TopLevelDefinition> callback) {

		// do not merge top level definitions
		if (target != src) {
			if (trace) {
				LOGGER.trace("Merging definition " + createDefinitionId(target) + "  with "
						+ createDefinitionId(src));
				LOGGER.trace("BEFORE: " + target);
			}
			if ((target instanceof Mergeable) && (src instanceof Mergeable)) {
				((Mergeable) target).mergeFrom(src);
			}
			if (trace) {
				LOGGER.trace("AFTER : " + target);
			}
		}

		if (trace) {
			LOGGER.trace("Updating refernces after the first merge");
		}
		// FIXME: add passing the current context to the method so it could resolve the dependencies
		// that are not persisted, yet
		if (!callback.updateReferences(target)) {
			String id = createDefinitionId(target);
			errors.put(id, target);
			String message = "Found missing reference dependency for definition: " + id
					+ ". Skipping it.";
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
		FIX,
		/** The ignore. */
		IGNORE,
		/** The warn. */
		WARN,
		/** The error. */
		ERROR;
	}

	/**
	 * Asynchronous task for cache warm up.
	 *
	 * @author BBonev
	 */
	class CacheWarmUpTask extends RecursiveTask<Void> {

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

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Void compute() {
			TimeTracker tracker = new TimeTracker().begin();
			// asynch executing of the method warmUpCache
			callback.warmUpCache();

			LOGGER.debug("Definition cache warm up for " + callback.getCallbackName() + " took "
					+ tracker.stopInSeconds() + " s");
			return null;
		}
	}

	/**
	 * Callback interface used to provide information for definition while performing sort
	 * algorithm.
	 *
	 * @author BBonev
	 */
	public interface DefinitionSorterCallback {

		/**
		 * Gets the parent definition id.
		 *
		 * @param topLevelDefinition
		 *            the top level definition
		 * @return the parent definition id
		 */
		String getParentDefinitionId(TopLevelDefinition topLevelDefinition);

		/**
		 * Creates the definition id.
		 *
		 * @param definition
		 *            the definition
		 * @return the string
		 */
		String createDefinitionId(TopLevelDefinition definition);

		/**
		 * Creates the parent definition id.
		 *
		 * @param definition
		 *            the definition
		 * @return the string
		 */
		String createParentDefinitionId(TopLevelDefinition definition);
	}

	/**
	 * Sorter callback implementation to handle basic parent/child inheritance when sorting
	 *
	 * @author BBonev
	 */
	static class ParentChildDefinitionSorter implements DefinitionSorterCallback {


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
	}

	/**
	 * Sorter callback implementation to handle reference inheritance when sorting
	 *
	 * @author BBonev
	 */
	static class ReferenceDefinitionSorter implements DefinitionSorterCallback {

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
				return DefinitionIdentityUtil
						.createReferenceDefinitionId((ReferenceDefinitionModel) definition);
			}
			return null;
		}

		@Override
		public String toString() {
			return "Reference-definition callback";
		}

	}
}
