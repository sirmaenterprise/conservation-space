package com.sirma.itt.emf.definition.compile;

import static com.sirma.itt.emf.definition.DefinitionIdentityUtil.createDefinitionId;
import static com.sirma.itt.emf.definition.DefinitionIdentityUtil.createParentDefinitionId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.TemplateDefinition;
import com.sirma.itt.emf.definition.load.TemplateDefinitionCompilerCallback;
import com.sirma.itt.emf.definition.model.DefinitionTemplateHolder;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.util.ValidationLoggingUtil;

/**
 * Definition compiler implementation that handles template definitions.
 * 
 * @author BBonev
 */
@TemplateDefinition
public class TemplateDefinitionCompiler extends AbstractDefinitionCompiler implements
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
		if ((task != null) && task.isDone()) {
			task = null;
		}

		task = new CacheWarmUpTask(callback);
		pool.submit(task);
	}

	/**
	 * Check and convert.
	 * 
	 * @param callback
	 *            the callback
	 * @return the template definition compiler callback
	 */
	@SuppressWarnings("unchecked")
	private TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>> checkAndConvert(
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (callback instanceof TemplateDefinitionCompilerCallback) {
			return (TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>>) callback;
		}
		return null;
	}

	@Override
	public List<TopLevelDefinition> loadFiles(
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
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
		if ((definitions == null) || definitions.isEmpty()) {
			LOGGER.info("No " + callback.getCallbackName() + " template definitions!");
			return Collections.emptyList();
		}
		LOGGER.info("Found " + definitions.size() + " " + callback.getCallbackName()
				+ " template definitions.");

		// convert and validate source files
		List<DefinitionTemplateHolder<TopLevelDefinition>> loadedDefinitions = helper.loadFiles(definitions,
				callback.getTemplateClass(), callback.getMappingClass(),
				callback.getXmlValidationType(), false, callback);

		List<TopLevelDefinition> result = new LinkedList<TopLevelDefinition>();

		for (DefinitionTemplateHolder<TopLevelDefinition> templatesDefinition : loadedDefinitions) {
			if ((templatesDefinition == null) || (templatesDefinition.getTemplates() == null)) {
				LOGGER.warn("Failed to parse definition");
				continue;
			}

			if (templatesDefinition.getTemplates().isEmpty()) {
				String message = "No template definitions found in: "
						+ templatesDefinition.getDmsId();
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				continue;
			}

			List<TopLevelDefinition> templates = templatesDefinition.getTemplates();
			// copied container information from the holder definition
			for (TopLevelDefinition topLevelDefinition : templates) {
				topLevelDefinition.setContainer(templatesDefinition.getContainer());
				topLevelDefinition.setDmsId(templatesDefinition.getDmsId());
			}
			result.addAll(templates);
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
		for (TopLevelDefinition template : definitions) {
			if (trace) {
				LOGGER.trace("Processing template definition: " + template);
			}
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

		// we do this here and not above at the first pass because we may miss
		// overridden definition
		copyBaseDefinitionsToLocalContainers(baseDefinitions, templateDefinitions);

		// check if we have resolved all dependences
		Set<String> tmp = new HashSet<String>(unresolvedIds.keySet());
		tmp.retainAll(resolvedIds);
		unresolvedIds.keySet().removeAll(tmp);
		// remove unresolved dependencies
		resolvedIds.removeAll(new HashSet<String>(unresolvedIds.values()));

		if (!unresolvedIds.isEmpty()) {
			String message = "Cannot resolve parent dependences " + unresolvedIds.keySet()
					+ ". Dependent definitions will not be saved: " + unresolvedIds.values();
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
		}

		try {
			if ((task != null) && !task.isDone()) {
				// wait for the cache to warm up
				task.get();
			}
		} catch (Exception e) {
			LOGGER.warn("Exception during waitng for cache warmup. Will continue anyway", e);
		}

		List<TopLevelDefinition> list = compileDefinitions(templateDefinitions, callback);

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
				String message = "Found errors while validating " + definition.getIdentifier()
						+ ". Skipping it!";
				LOGGER.error("\n=======================================================================\n"
						+ message
						+ "\n=======================================================================");
				ValidationLoggingUtil.addErrorMessage(message);
				it.remove();
				continue;
			}

			if (persist) {
				if (debug) {
					LOGGER.debug("Saving " + callback.getCallbackName() + " template definition: "
							+ definition.getIdentifier());
				}
				// check for previous definition if so and there are no changes we do nothing
				TopLevelDefinition oldDefinition = callback
						.findTemplateInSystem(DefinitionIdentityUtil.createDefinitionId(definition));
				if (oldDefinition != null) {
					if (mutableDictionaryService.isDefinitionEquals(definition, oldDefinition)) {
						// nothing to do
						continue;
					}
				}
				// if there are changes then we need to persist it all later we will delete the old
				// definition if any
				callback.saveTemplateProperties(definition, null);

				// dbDao.executeInTransaction(new
				// SaveTemplateCallable<TopLevelDefinition>(definition,
				// callback));
				callback.saveTemplate(definition);
			}
		}
		return list;
	}
}
