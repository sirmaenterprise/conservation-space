package com.sirma.itt.emf.definition.compile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.TemplateDefinition;
import com.sirma.itt.emf.definition.load.TemplateDefinitionCompilerCallback;
import com.sirma.itt.emf.definition.model.DefinitionTemplateHolder;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.dozer.DozerMapper;

/**
 * Default implementation of the {@link DefinitionCompiler}. The implementation navigates between
 * different algorithms.
 *
 * @author BBonev
 */
public class ChainingDefinitionCompiler implements DefinitionCompiler {
	/** The logger. */
	@Inject
	protected Logger LOGGER;
	/** The debug. */
	protected boolean debug;
	/** The trace. */
	protected boolean trace;

	@Inject
	@Definition
	private DefinitionCompilerAlgorithm definitionCompiler;

	@Inject
	@TemplateDefinition
	private DefinitionCompilerAlgorithm templateDefinitionCompiler;

	@Inject
	private DozerMapper mapper;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		debug = LOGGER.isDebugEnabled();
		trace = LOGGER.isTraceEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends TopLevelDefinition> List<T> compileDefinitions(
			DefinitionCompilerCallback<T> callback, boolean persist) {
		// handle template definition callback
		if (callback instanceof TemplateDefinitionCompilerCallback) {
			TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>> templateCallback = (TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>>) callback;
			templateDefinitionCompiler.prepare(templateCallback);
			List<TopLevelDefinition> list = templateDefinitionCompiler.loadFiles(templateCallback);

			// if hybrid compilation is supported then we try to separate the definitions and call
			// the proper compilers
			if (templateCallback.isHybridDefinitionsSupported()) {
				Pair<List<TopLevelDefinition>, List<TopLevelDefinition>> pair = templateCallback
						.filerStandaloneDefinitions(list);
				List<TopLevelDefinition> templates = pair.getFirst();
				List<TopLevelDefinition> compiledTemplates = templateDefinitionCompiler.compile(
						templates, templateCallback, persist);

				List<TopLevelDefinition> standalone = pair.getSecond();
				DefinitionCompilerCallback<TopLevelDefinition> otherCallback = templateCallback
						.getOtherCallback();
				if ((standalone != null) && !standalone.isEmpty() && (otherCallback != null)) {

					// start compiler preparation
					definitionCompiler.prepare(otherCallback);

					// if the class of the other compiler is different we will try to convert the
					// definitions first
					if (!standalone.get(0).getClass().equals(otherCallback.getDefinitionClass())) {
						List<TopLevelDefinition> convertedList = new ArrayList<TopLevelDefinition>(
								standalone.size());
						for (TopLevelDefinition topLevelDefinition : standalone) {
							TopLevelDefinition converted = mapper.getMapper().map(
									topLevelDefinition, otherCallback.getDefinitionClass());
							converted.setContainer(topLevelDefinition.getContainer());
							convertedList.add(converted);
						}
						standalone = convertedList;
					}
					List<TopLevelDefinition> toplevelDefinitions = definitionCompiler.compile(standalone, otherCallback, persist);
					List<TopLevelDefinition> result = new ArrayList<>(compiledTemplates.size() + toplevelDefinitions.size());
					result.addAll(compiledTemplates);
					result.addAll(toplevelDefinitions);
					return (List<T>) result;
				}
				return (List<T>) compiledTemplates;
			}
			return (List<T>) templateDefinitionCompiler.compile(list, templateCallback, persist);
		}
		definitionCompiler.prepare((DefinitionCompilerCallback<TopLevelDefinition>) callback);
		List<TopLevelDefinition> list = definitionCompiler
				.loadFiles((DefinitionCompilerCallback<TopLevelDefinition>) callback);
		return (List<T>) definitionCompiler.compile(list,
				(DefinitionCompilerCallback<TopLevelDefinition>) callback, persist);

	}

}
