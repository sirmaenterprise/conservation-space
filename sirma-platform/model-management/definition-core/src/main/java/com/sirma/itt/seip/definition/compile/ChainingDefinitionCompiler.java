package com.sirma.itt.seip.definition.compile;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.compile.Definition;
import com.sirma.itt.seip.definition.compile.DefinitionCompiler;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerAlgorithm;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerCallback;
import com.sirma.itt.seip.definition.compile.TemplateDefinition;
import com.sirma.itt.seip.definition.compile.TemplateDefinitionCompilerCallback;
import com.sirma.itt.seip.domain.definition.DefinitionTemplateHolder;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * Default implementation of the {@link DefinitionCompiler}. The implementation navigates between different algorithms.
 *
 * @author BBonev
 */
public class ChainingDefinitionCompiler implements DefinitionCompiler {

	@Inject
	@Definition
	private DefinitionCompilerAlgorithm definitionCompiler;

	@Inject
	@TemplateDefinition
	private DefinitionCompilerAlgorithm templateDefinitionCompiler;

	@Inject
	private ObjectMapper mapper;

	@Override
	@SuppressWarnings("unchecked")
	public <T extends TopLevelDefinition> List<T> compileDefinitions(DefinitionCompilerCallback<T> callback,
			boolean persist) {
		// handle template definition callback
		if (callback instanceof TemplateDefinitionCompilerCallback) {
			TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>> templateCallback = (TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>>) callback;
			templateDefinitionCompiler.prepare(templateCallback);
			List<TopLevelDefinition> list = templateDefinitionCompiler.loadFiles(templateCallback);

			// if hybrid compilation is supported then we try to separate the definitions and call
			// the proper compilers
			if (templateCallback.isHybridDefinitionsSupported()) {
				return compileHybridDefinitions(persist, templateCallback, list);
			}
			return (List<T>) templateDefinitionCompiler.compile(list, templateCallback, persist);
		}
		definitionCompiler.prepare((DefinitionCompilerCallback<TopLevelDefinition>) callback);
		List<TopLevelDefinition> list = definitionCompiler
				.loadFiles((DefinitionCompilerCallback<TopLevelDefinition>) callback);
		return (List<T>) definitionCompiler.compile(list, (DefinitionCompilerCallback<TopLevelDefinition>) callback,
				persist);

	}

	@SuppressWarnings("unchecked")
	private <T extends TopLevelDefinition> List<T> compileHybridDefinitions(boolean persist,
			TemplateDefinitionCompilerCallback<TopLevelDefinition, DefinitionTemplateHolder<TopLevelDefinition>> templateCallback,
			List<TopLevelDefinition> list) {
		Pair<List<TopLevelDefinition>, List<TopLevelDefinition>> pair = templateCallback
				.filerStandaloneDefinitions(list);
		List<TopLevelDefinition> templates = pair.getFirst();
		List<TopLevelDefinition> compiledTemplates = templateDefinitionCompiler.compile(templates, templateCallback,
				persist);

		List<TopLevelDefinition> standalone = pair.getSecond();
		DefinitionCompilerCallback<TopLevelDefinition> otherCallback = templateCallback.getOtherCallback();
		if (standalone != null && !standalone.isEmpty() && otherCallback != null) {

			return processStandaloneDefinitions(persist, compiledTemplates, standalone, otherCallback);
		}
		return (List<T>) compiledTemplates;
	}

	@SuppressWarnings("unchecked")
	private <T extends TopLevelDefinition> List<T> processStandaloneDefinitions(boolean persist,
			List<TopLevelDefinition> compiledTemplates, List<TopLevelDefinition> standalone,
			DefinitionCompilerCallback<TopLevelDefinition> otherCallback) {
		// start compiler preparation
		definitionCompiler.prepare(otherCallback);

		// if the class of the other compiler is different we will try to convert the
		// definitions first
		List<TopLevelDefinition> toProcess = standalone;
		if (!standalone.get(0).getClass().equals(otherCallback.getDefinitionClass())) {
			List<TopLevelDefinition> convertedList = new ArrayList<>(standalone.size());
			for (TopLevelDefinition topLevelDefinition : standalone) {
				TopLevelDefinition converted = mapper.map(topLevelDefinition, otherCallback.getDefinitionClass());
				converted.setContainer(topLevelDefinition.getContainer());
				convertedList.add(converted);
			}
			toProcess = convertedList;
		}
		List<TopLevelDefinition> toplevelDefinitions = definitionCompiler.compile(toProcess, otherCallback, persist);
		List<TopLevelDefinition> result = new ArrayList<>(compiledTemplates.size() + toplevelDefinitions.size());
		result.addAll(compiledTemplates);
		result.addAll(toplevelDefinitions);
		return (List<T>) result;
	}

}
