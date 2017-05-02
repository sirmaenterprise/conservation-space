package com.sirma.itt.seip.definition.compile;

import static com.sirma.itt.seip.definition.util.DefinitionIdentityUtil.createDefinitionId;
import static com.sirma.itt.seip.definition.util.DefinitionIdentityUtil.createParentDefinitionId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.DefinitionTemplateHolder;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Definition compiler implementation that handles template definitions.
 *
 * @author BBonev
 */
@TemplateDefinition
public class TemplateDefinitionCompiler extends AbstractDefinitionCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateDefinitionCompiler.class);
	private GenericAsyncTask task;

	@Inject
	private DefinitionCompilerHelper helper;

	@Override
	public void prepare(DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (task != null && task.isDone()) {
			task = null;
		}

		task = new CacheWarmUpTask(callback);
		submitTask(task);
	}

	/**
	 * Check and convert.
	 *
	 * @param callback
	 *            the callback
	 * @return the template definition compiler callback
	 */
	@SuppressWarnings("unchecked")
	private static TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>> checkAndConvert(
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (callback instanceof TemplateDefinitionCompilerCallback) {
			return (TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>>) callback;
		}
		return null;
	}

	@Override
	public List<TopLevelDefinition> loadFiles(DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (callback instanceof TemplateDefinitionCompilerCallback) {
			return loadFilesInternal(checkAndConvert(callback));
		}
		LOGGER.warn("Called template definition compiler with " + callback.getCallbackName()
				+ " definition compiler that is not for template processing. Nothing is processed.");
		return Collections.emptyList();
	}

	/**
	 * Load files internal.
	 *
	 * @param callback
	 *            the callback
	 * @return the list
	 */
	private List<TopLevelDefinition> loadFilesInternal(
			TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>> callback) {
		List<FileDescriptor> definitions = callback.getDefinitions();
		if (definitions == null || definitions.isEmpty()) {
			LOGGER.info("No {} template definitions!", callback.getCallbackName());
			return Collections.emptyList();
		}
		LOGGER.info("Found {} {} template definitions.", definitions.size(), callback.getCallbackName());

		// convert and validate source files
		List<DefinitionTemplateHolder<TopLevelDefinition>> loadedDefinitions = helper.loadFiles(definitions,
				callback.getTemplateClass(), callback.getMappingClass(), callback.getXmlValidationType(), false,
				callback);

		List<TopLevelDefinition> result = new LinkedList<>();

		for (DefinitionTemplateHolder<TopLevelDefinition> templatesDefinition : loadedDefinitions) {
			if (templatesDefinition == null || templatesDefinition.getTemplates() == null) {
				LOGGER.warn("Failed to parse definition");
				continue;
			}

			if (templatesDefinition.getTemplates().isEmpty()) {
				String message = "No template definitions found in: " + templatesDefinition.getDmsId();
				LOGGER.error(message);
				ValidationLoggingUtil.addWarningMessage(message);
			} else {
				List<TopLevelDefinition> templates = templatesDefinition.getTemplates();
				// copied container information from the holder definition
				for (TopLevelDefinition topLevelDefinition : templates) {
					topLevelDefinition.setContainer(templatesDefinition.getContainer());
					topLevelDefinition.setDmsId(templatesDefinition.getDmsId());
				}
				result.addAll(templates);
			}
		}
		return result;
	}

	@Override
	public List<TopLevelDefinition> compile(List<TopLevelDefinition> definitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, boolean persist) {
		Set<String> resolvedIds = new HashSet<String>();
		Map<String, String> unresolvedIds = new LinkedHashMap<String, String>();

		Map<String, TopLevelDefinition> templateDefinitions = new LinkedHashMap<String, TopLevelDefinition>();
		Map<String, TopLevelDefinition> baseDefinitions = new LinkedHashMap<String, TopLevelDefinition>();

		// collect information for the definition so we can check
		// dependences before persisting
		beforeDefinitionProcess(definitions, resolvedIds, unresolvedIds, templateDefinitions, baseDefinitions);

		// we do this here and not above at the first pass because we may miss
		// overridden definition
		copyBaseDefinitionsToLocalContainers(baseDefinitions, templateDefinitions);

		// check if we have resolved all dependences
		Set<String> tmp = new HashSet<>(unresolvedIds.keySet());
		tmp.retainAll(resolvedIds);
		unresolvedIds.keySet().removeAll(tmp);
		// remove unresolved dependencies
		resolvedIds.removeAll(new HashSet<>(unresolvedIds.values()));

		if (!unresolvedIds.isEmpty()) {
			String message = "Cannot resolve parent dependences " + unresolvedIds.keySet()
					+ ". Dependent definitions will not be saved: " + unresolvedIds.values();
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
		}

		waitForCacheWarmUp(task);

		List<TopLevelDefinition> list = compileDefinitions(templateDefinitions, callback);

		persistDefinitions(callback, persist, list);
		return list;
	}

	/**
	 * Before definition process. Collect information for the definition so we can check dependences before persisting
	 *
	 * @param definitions
	 *            the definitions
	 * @param resolvedIds
	 *            the resolved ids
	 * @param unresolvedIds
	 *            the unresolved ids
	 * @param templateDefinitions
	 *            the template definitions
	 * @param baseDefinitions
	 *            the base definitions
	 */
	private static void beforeDefinitionProcess(List<TopLevelDefinition> definitions, Set<String> resolvedIds,
			Map<String, String> unresolvedIds, Map<String, TopLevelDefinition> templateDefinitions,
			Map<String, TopLevelDefinition> baseDefinitions) {
		for (TopLevelDefinition template : definitions) {
			LOGGER.trace("Processing template definition: {}", template);
			if (template.getContainer() == null) {
				// add the base definition to separate list we will handle
				// it later
				baseDefinitions.put(template.getIdentifier(), template);
				continue;
			}

			String identifier = createDefinitionId(template);
			resolvedIds.add(identifier);
			if (template.getParentDefinitionId() != null) {
				String parentDefinitionId = createParentDefinitionId(template);
				if (!resolvedIds.contains(parentDefinitionId)) {
					unresolvedIds.put(parentDefinitionId, identifier);
				}
			}
			templateDefinitions.put(identifier, template);
		}
	}

	/**
	 * Persist definitions.
	 *
	 * @param callback
	 *            the callback
	 * @param persist
	 *            the persist
	 * @param list
	 *            the list
	 */
	private void persistDefinitions(DefinitionCompilerCallback<TopLevelDefinition> callback, boolean persist,
			List<TopLevelDefinition> list) {
		// persist all resolvable definitions
		for (Iterator<TopLevelDefinition> it = list.iterator(); it.hasNext();) {
			TopLevelDefinition definition = it.next();

			// update field definition, without setting path and NS
			if (definition instanceof BidirectionalMapping) {
				((BidirectionalMapping) definition).initBidirection();
			}
			callback.normalizeFields(definition);
			callback.setPropertyRevision(definition);

			callback.prepareForPersist(definition);

			// update fields again, if they are modified on prepare
			callback.normalizeFields(definition);

			// validate the definition
			if (!callback.validateCompiledDefinition(definition)) {
				String message = "Found errors while validating " + definition.getIdentifier() + ". Skipping it!";
				LOGGER.error(
						"\n=======================================================================\n{}\n=======================================================================",
						message);
				ValidationLoggingUtil.addErrorMessage(message);
				it.remove();
			} else if (persist) {
				LOGGER.debug("Saving {} template definition: {}", callback.getCallbackName(),
						definition.getIdentifier());
				// check for previous definition if so and there are no changes we do nothing
				TopLevelDefinition oldDefinition = callback.findTemplateInSystem(definition.getIdentifier());
				if (!(oldDefinition != null
						&& mutableDictionaryService.isDefinitionEquals(definition, oldDefinition))) {
					// if there are changes then we need to persist it all later we will delete the
					// old definition if any
					callback.saveTemplateProperties(definition, null);
					callback.saveTemplate(definition);
				}
			}
		}
	}
}
