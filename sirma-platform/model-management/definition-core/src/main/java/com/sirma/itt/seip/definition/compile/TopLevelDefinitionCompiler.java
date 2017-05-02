/*
 *
 */
package com.sirma.itt.seip.definition.compile;

import static com.sirma.itt.seip.definition.util.DefinitionIdentityUtil.createDefinitionId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.serialization.SerializationHelper;

/**
 * Definition compiler implementation that handles regular top level definitions.
 *
 * @author BBonev
 */
@Definition
public class TopLevelDefinitionCompiler extends AbstractDefinitionCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopLevelDefinitionCompiler.class);
	private GenericAsyncTask task;

	@Inject
	private DefinitionCompilerHelper helper;

	@Inject
	private SerializationHelper serializationHelper;

	@Override
	public void prepare(DefinitionCompilerCallback<TopLevelDefinition> callback) {
		// only start cache warm up if not a template
		if (!(callback instanceof TemplateDefinitionCompilerCallback)) {
			task = new CacheWarmUpTask(callback);
			submitTask(task);
		} else {
			if (task != null && task.isDone()) {
				task = null;
			}
		}
	}

	@Override
	public List<TopLevelDefinition> compile(List<TopLevelDefinition> loadedDefinitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, boolean persist) {

		if (loadedDefinitions.isEmpty()) {
			LOGGER.warn("No valid {} definitions to load", callback.getCallbackName());
			return Collections.emptyList();
		}

		Map<String, TopLevelDefinition> definitions = new LinkedHashMap<>((int) (loadedDefinitions.size() * 1.2), 0.9f);
		Set<String> duplicateDefinitions = new LinkedHashSet<>();
		// the list of base definitions will never be persisted, at least not as
		// they are but as copies in everyone of the sites
		Map<String, TopLevelDefinition> baseDefinitions = new LinkedHashMap<>();
		// the list of all concrete containers with enabled CMF

		beforeDefinitionProcess(loadedDefinitions, callback, definitions, duplicateDefinitions, baseDefinitions);
		definitions.keySet().removeAll(duplicateDefinitions);

		// for all containers add a copy of the base definitions
		copyBaseDefinitionsToLocalContainers(baseDefinitions, definitions);

		waitForCacheWarmUp(task);

		// first we collect the root definitions
		Map<String, TopLevelDefinition> rootDefinitionsMapping = collectRootDefinitions(loadedDefinitions, callback,
				definitions);

		// compile root definitions
		List<TopLevelDefinition> list = compileDefinitions(rootDefinitionsMapping, callback);
		if (list.isEmpty()) {
			LOGGER.warn("Failed to compile the root {} definitions: {}", callback.getCallbackName(),
					rootDefinitionsMapping.keySet());
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
			Set<String> set = new HashSet<>(failed);
			rootDefinitionsMapping.keySet().removeAll(set);
			definitions.keySet().removeAll(set);
		}

		// now we can compile all definitions definitions
		list = compileDefinitions(definitions, callback);
		if (list.isEmpty()) {
			LOGGER.warn("Failed to compile any {} definitions: {}", callback.getCallbackName(), definitions.keySet());
			return Collections.emptyList();
		}
		// and persist them if needed
		failed = prepareForPersistAndValidate(list, persist, callback);
		// remove invalid definitions
		if (!failed.isEmpty()) {
			// we remove all failed definitions if any
			definitions.keySet().removeAll(new HashSet<>(failed));
		}

		LOGGER.info("Finished {} definitions loading", callback.getCallbackName());

		return new ArrayList<>(definitions.values());
	}

	/**
	 * Collect root definitions.
	 *
	 * @param loadedDefinitions
	 *            the loaded definitions
	 * @param callback
	 *            the callback
	 * @param definitions
	 *            the definitions
	 * @return the map
	 */
	private Map<String, TopLevelDefinition> collectRootDefinitions(List<TopLevelDefinition> loadedDefinitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, Map<String, TopLevelDefinition> definitions) {
		Map<String, TopLevelDefinition> rootDefinitionsMapping = new LinkedHashMap<>(
				(int) (loadedDefinitions.size() * 1.2), 0.9f);
		for (Entry<String, TopLevelDefinition> entry : definitions.entrySet()) {
			Collection<String> dependencies = callback.getDependencies(entry.getValue());
			if (dependencies.isEmpty()) {
				rootDefinitionsMapping.put(entry.getKey(), entry.getValue());
			}
		}
		LOGGER.debug("Found root definitions {}", rootDefinitionsMapping.keySet());
		return rootDefinitionsMapping;
	}

	/**
	 * Before definition process.
	 *
	 * @param loadedDefinitions
	 *            the loaded definitions
	 * @param callback
	 *            the callback
	 * @param definitions
	 *            the definitions
	 * @param duplicateDefinitions
	 *            the duplicate definitions
	 * @param baseDefinitions
	 *            the base definitions
	 */
	private static void beforeDefinitionProcess(List<TopLevelDefinition> loadedDefinitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, Map<String, TopLevelDefinition> definitions,
			Set<String> duplicateDefinitions, Map<String, TopLevelDefinition> baseDefinitions) {
		// prepare fields for compilation
		for (TopLevelDefinition definitionImpl : loadedDefinitions) {
			callback.normalizeFields(definitionImpl);
			if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(definitionImpl.getContainer())) {
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
				String message = "Found same definition id='" + defId + "' in 2 different files: " + existing.getDmsId()
						+ " and in " + definitionImpl.getDmsId() + ". Skipping both definitions!";
				ValidationLoggingUtil.addErrorMessage(message);
				LOGGER.warn(message);
			}
		}
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
	@SuppressWarnings("unchecked")
	private <D> D createCopy(D object) {
		return (D) serializationHelper.deserialize(serializationHelper.serialize(object));
	}

	@Override
	public List<TopLevelDefinition> loadFiles(DefinitionCompilerCallback<TopLevelDefinition> callback) {
		List<FileDescriptor> definitionDescriptors = callback.getDefinitions();
		if (definitionDescriptors.isEmpty()) {
			LOGGER.info("No {} definitions!", callback.getCallbackName());
			return Collections.emptyList();
		}
		LOGGER.info("Found {} {} definitions.", definitionDescriptors.size(), callback.getCallbackName());

		List<TopLevelDefinition> loadedDefinitions = helper.loadFiles(definitionDescriptors,
				callback.getDefinitionClass(), callback.getMappingClass(), callback.getXmlValidationType(), true,
				callback);
		return loadedDefinitions;
	}
}
