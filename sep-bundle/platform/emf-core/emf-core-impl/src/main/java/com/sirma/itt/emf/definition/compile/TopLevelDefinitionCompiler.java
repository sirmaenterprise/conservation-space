/*
 *
 */
package com.sirma.itt.emf.definition.compile;

import static com.sirma.itt.emf.definition.DefinitionIdentityUtil.createDefinitionId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.TemplateDefinitionCompilerCallback;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.serialization.SerializationUtil;
import com.sirma.itt.emf.util.ValidationLoggingUtil;

/**
 * Definition compiler implementation that handles regular top level definitions.
 *
 * @author BBonev
 */
@Definition
public class TopLevelDefinitionCompiler extends AbstractDefinitionCompiler implements
		DefinitionCompilerAlgorithm {

	private ForkJoinTask<?> task;

	@Inject
	private DefinitionCompilerHelper helper;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		debug = LOGGER.isDebugEnabled();
		trace = LOGGER.isTraceEnabled();
	}

	@Override
	public void prepare(DefinitionCompilerCallback<TopLevelDefinition> callback) {
		// only start cache warm up if not a template
		if (!(callback instanceof TemplateDefinitionCompilerCallback)) {
			task = new CacheWarmUpTask(callback);
			pool.submit(task);
		} else {
			if ((task != null) && task.isDone()) {
				task = null;
			}
		}
	}

	@Override
	public List<TopLevelDefinition> compile(List<TopLevelDefinition> loadedDefinitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, boolean persist) {

		if (loadedDefinitions.isEmpty()) {
			LOGGER.warn("No valid " + callback.getCallbackName() + " definitions to load");
			return Collections.emptyList();
		}

		Map<String, TopLevelDefinition> definitions = new LinkedHashMap<String, TopLevelDefinition>(
				(int) (loadedDefinitions.size() * 1.2), 0.9f);
		Set<String> duplicateDefinitions = new LinkedHashSet<String>();
		// the list of base definitions will never be persisted, at least not as
		// they are but as copies in everyone of the sites
		Map<String, TopLevelDefinition> baseDefinitions = new LinkedHashMap<String, TopLevelDefinition>();
		// the list of all concrete containers with enabled CMF

		// prepare fields for compilation
		for (TopLevelDefinition definitionImpl : loadedDefinitions) {
			callback.normalizeFields(definitionImpl);
			if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(definitionImpl
					.getContainer())) {
				baseDefinitions.put(definitionImpl.getIdentifier(), definitionImpl);
				continue;
			}
			String defId = createDefinitionId(definitionImpl);
			TopLevelDefinition existing = definitions.get(defId);
			if (existing == null) {
				definitions.put(defId, definitionImpl);
			} else {
				// later will skip all definitions that have duplicates
				duplicateDefinitions.add(defId);
				String message = "Found same definition id='" + defId
						+ "' in 2 different files: " + existing.getDmsId() + " and in "
						+ definitionImpl.getDmsId() + ". Skipping both definitions!";
				ValidationLoggingUtil.addErrorMessage(message);
				LOGGER.warn(message);
			}
		}
		definitions.keySet().removeAll(duplicateDefinitions);

		// for all containers add a copy of the base definitions
		copyBaseDefinitionsToLocalContainers(baseDefinitions, definitions);

		try {
			if ((task != null) && !task.isDone()) {
				// wait for the cache to warm up
				task.get();
			}
		} catch (Exception e) {
			LOGGER.warn("Exception during waitng for cache warmup. Will continue anyway", e);
		}

		// first we collect the root definitions
		Map<String, TopLevelDefinition> rootDefinitionsMapping = new LinkedHashMap<String, TopLevelDefinition>(
				(int) (loadedDefinitions.size() * 1.2), 0.9f);
		for (Entry<String, TopLevelDefinition> entry : definitions.entrySet()) {
			if (entry.getValue().getParentDefinitionId() == null) {
				rootDefinitionsMapping.put(entry.getKey(), entry.getValue());
			}
		}

		// compile root definitions
		List<TopLevelDefinition> list = compileDefinitions(rootDefinitionsMapping, callback);
		if (list.isEmpty()) {
			LOGGER.warn("Failed to compile the root " + callback.getCallbackName()
					+ " definitions: " + rootDefinitionsMapping.keySet());
			return Collections.emptyList();
		}

		// IMPORTANT
		// create copy of the root definitions to perform a dry run for validation and pre
		// processing. We does not modify the originals because they should be in their initial
		// state when start merging. This is does due to the fact when calling
		// prepareForPersistAndValidate performs a logic that removes duplicate fields from the
		// definition but in the case of workflow definitions this is a bad thing.
		List<TopLevelDefinition> copy = createCopy(list);
		// we validate root definitions so later we can propagate changes to all other definitions
		List<String> failed = prepareForPersistAndValidate(copy, false, callback);
		// remove invalid definitions
		if (!failed.isEmpty()) {
			// we remove all failed definitions if any
			Set<String> set = new HashSet<String>(failed);
			rootDefinitionsMapping.keySet().removeAll(set);
			definitions.keySet().removeAll(set);
		}

		// now we can compile all definitions definitions
		list = compileDefinitions(definitions, callback);
		if (list.isEmpty()) {
			LOGGER.warn("Failed to compile any " + callback.getCallbackName() + " definitions: "
					+ definitions.keySet());
			return Collections.emptyList();
		}
		// and persist them if needed
		failed = prepareForPersistAndValidate(list, persist, callback);
		// remove invalid definitions
		if (!failed.isEmpty()) {
			// we remove all failed definitions if any
			definitions.keySet().removeAll(new HashSet<String>(failed));
		}

		List<Pair<String, String>> instances = mutableDictionaryService
				.removeDefinitionsWithoutInstances(definitions.keySet());
		LOGGER.warn("Removed the following definitions that does not have instances: " + instances);

		LOGGER.info("Finished " + callback.getCallbackName() + " definitions loading");

		return new ArrayList<>(definitions.values());
	}

	/**
	 * Creates the copy.
	 *
	 * @param <D>
	 *            the generic type
	 * @param object
	 *            the object
	 * @return the d
	 */
	private <D> D createCopy(D object) {
		return SerializationUtil.copy(object);
	}

	@Override
	public List<TopLevelDefinition> loadFiles(
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
		List<FileDescriptor> definitionDescriptors = callback.getDefinitions();
		if (definitionDescriptors.isEmpty()) {
			LOGGER.info("No " + callback.getCallbackName() + " definitions!");
			return Collections.emptyList();
		}
		LOGGER.info("Found " + definitionDescriptors.size() + " " + callback.getCallbackName()
				+ " definitions.");

		List<TopLevelDefinition> loadedDefinitions = helper.loadFiles(definitionDescriptors,
				callback.getDefinitionClass(), callback.getMappingClass(),
				callback.getXmlValidationType(), true, callback);
		return loadedDefinitions;
	}

}
