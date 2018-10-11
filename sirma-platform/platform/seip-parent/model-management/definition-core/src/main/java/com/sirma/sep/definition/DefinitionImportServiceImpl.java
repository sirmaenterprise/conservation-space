package com.sirma.sep.definition;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionInitialization;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.compile.GenericDefinitionCompilerCallback;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.filter.Filter;
import com.sirma.itt.seip.domain.filter.FilterService;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.definition.compile.DefinitionCompiler;
import com.sirma.sep.definition.db.DefinitionContent;
import com.sirma.sep.definition.db.DefinitionEntry;
import com.sirma.sep.definition.fs.DefinitionFilesProcessor;
import com.sirma.sep.definition.fs.DefinitionFilesProcessor.ParsedDefinition;
import com.sirma.sep.definition.validation.DefinitionValidationService;

@Singleton
public class DefinitionImportServiceImpl implements DefinitionImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionFilesProcessor definitionsDirectoryProcessor;

	@Inject
	private DefinitionCompiler definitionCompiler;

	@Inject
	private DefinitionValidationService validationService;

	@Inject
	private FilterService filterService;

	@Inject
	private LabelService labelService;

	@Inject
	@Any
	private GenericDefinitionCompilerCallback compilerCallback;

	@Inject
	private MutableDefinitionService mutableDefinitionService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private DefinitionInitialization definitionInitialization;

	@Inject
	private EventService eventService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private DbDao dbDao;

	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public Collection<String> validate(Path newDefinitionsDirectory) {
		Objects.requireNonNull(newDefinitionsDirectory);

		List<String> errors = new ArrayList<>();

		try {
			LOGGER.debug("Begin validating definitions before import");

			definitionsDirectoryProcessor.prepareDefinitionsDirectory(newDefinitionsDirectory);

			List<ParsedDefinition> parseDefinitions = new ArrayList<>();

			List<String> parseErrors = definitionsDirectoryProcessor.parseDefinitions(newDefinitionsDirectory, false, parseDefinitions);
			errors.addAll(parseErrors);

			List<String> duplicateLabelErrors = checkForDuplicateIds(parseDefinitions, ParsedDefinition::getLabels, "label");
			errors.addAll(duplicateLabelErrors);

			List<String> duplicateFiltersErrors = checkForDuplicateIds(parseDefinitions, ParsedDefinition::getFilters, "filter");
			errors.addAll(duplicateFiltersErrors);

			List<GenericDefinitionImpl> definitions = parseDefinitions.stream()
														.map(ParsedDefinition::getDefinition)
														.collect(Collectors.toList());

			List<String> definitionErrors = validationService.validateDefinitions(definitions);
			errors.addAll(definitionErrors);

			List<String> compilationErrors = definitionCompiler.compile(definitions);
			errors.addAll(compilationErrors);

			List<String> validationErrors = validationService.validateCompiledDefinitions(definitions);
			errors.addAll(validationErrors);
		} catch (DefinitionValidationException e) {
			errors.addAll(e.getErrors());
		}

		return new LinkedHashSet<>(errors);
	}

	@Override
	public void importDefinitions(Path newDefinitionsDirectory) {
		LOGGER.info("Begin definition import");

		List<ParsedDefinition> parseDefinitions = new ArrayList<>();

		definitionsDirectoryProcessor.parseDefinitions(newDefinitionsDirectory, true, parseDefinitions);

		List<GenericDefinitionImpl> definitions = parseDefinitions.stream()
													.map(ParsedDefinition::getDefinition)
													.collect(Collectors.toList());

		LOGGER.info("Start importing of {} definitions", definitions.size());

		definitionCompiler.compile(definitions);

		saveFilters(parseDefinitions);

		saveLabels(parseDefinitions);

		List<GenericDefinitionImpl> affectedDefinitions = getAffectedDefinitions(definitions);

		affectedDefinitions.forEach(this::saveDefinition);

		LOGGER.info("Finished definition importing");

		eventService.fire(new DefinitionsChangedEvent());
	}

	/**
	 * Filters the definitions that are not affected by the current import operation.
	 * Affected are those for which a new import content is provided or a new content is provided for
	 * one of their ancestors.
	 *
	 * @param definitions list of definitions to filter
	 * @return list of only affected definitions
	 */
	private static List<GenericDefinitionImpl> getAffectedDefinitions(List<GenericDefinitionImpl> definitions) {
		Map<String, GenericDefinitionImpl> index = definitions.stream().collect(Collectors.toMap(GenericDefinitionImpl::getIdentifier, Function.identity()));

		return definitions.stream()
				.filter(definition -> definition.getSourceContent() != null || isAncestorChanged(index, definition.getParentDefinitionId()))
				.collect(Collectors.toList());
	}

	private static boolean isAncestorChanged(Map<String, GenericDefinitionImpl> index, String parentDefinitionId) {
		GenericDefinitionImpl ancestor = index.get(parentDefinitionId);

		while (ancestor != null) {
			if (ancestor.getSourceContent() != null) {
				return true;
			}

			ancestor = index.get(ancestor.getParentDefinitionId());
		}

		return false;
	}

	private void saveDefinition(GenericDefinitionImpl definition) {
		String definitionId = definition.getIdentifier();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Processing compiled definition: {}", definition);
		} else {
			LOGGER.debug("Processing compiled definition '{}'", definitionId);
		}

		String sourceFile = definition.getSourceFile();
		String sourceContent = definition.getSourceContent();
		String previousId = definition.getPreviousIdentifier();

		if (sourceFile != null && sourceContent != null) {
			if (previousId != null) {
				removePreviousDefinition(definition);
			}
			DefinitionContent definitionContent = new DefinitionContent();
			definitionContent.setId(definition.getIdentifier());
			definitionContent.setFileName(sourceFile);
			definitionContent.setContent(sourceContent);

			dbDao.saveOrUpdate(definitionContent);
		}

		GenericDefinitionImpl existingDefinition = (GenericDefinitionImpl) definitionService.find(definitionId);
		boolean equals;
		if (existingDefinition != null) {
			equals = mutableDefinitionService.isDefinitionEquals(definition, existingDefinition);

			// if the definitions differ the revision number has to be increased
			if (!equals) {
				definition.setRevision(existingDefinition.getRevision().longValue() + 1L);
				LOGGER.debug("Updating the revision of definition '{}' to '{}'", definitionId, definition.getRevision());
			} else {
				LOGGER.debug("Definition '{}' is not changed. Saving is skipped", definitionId);
				return;
			}
		}

		// update the revision of all properties
		compilerCallback.setPropertyRevision(definition);

		compilerCallback.saveDefinitionProperties(definition, existingDefinition);

		compilerCallback.saveDefinition(definition);
	}

	private void removePreviousDefinition(GenericDefinitionImpl definition) {
		String previousId = definition.getPreviousIdentifier();
		DefinitionContent previousContent = dbDao.find(DefinitionContent.class, previousId);
		// we need to delete the content in new transaction otherwise the database constrain kicks in
		boolean removedSuccessfully = transactionSupport.invokeInNewTx(() -> dbDao.delete(DefinitionContent.class, previousId)) == 1;
		LOGGER.info(
				"Detected definition identifier change from {} to {}. The old definition was removed {}successfully",
				previousId, definition.getIdentifier(), removedSuccessfully ? "" : "un");
		// restore content on failed transaction otherwise it will be lost
		transactionSupport.invokeOnFailedTransactionInTx(() -> dbDao.saveOrUpdate(previousContent));
	}

	private void saveFilters(List<ParsedDefinition> parseDefinitions) {
		List<Filter> allFilters = parseDefinitions.stream()
						.map(ParsedDefinition::getFilters)
						.flatMap(List::stream)
						.collect(Collectors.toList());

		filterService.saveFilters(allFilters);
	}

	private void saveLabels(List<ParsedDefinition> parseDefinitions) {
		List<LabelDefinition> allLabels = parseDefinitions.stream()
			.map(ParsedDefinition::getLabels)
			.flatMap(List::stream)
			.collect(Collectors.toList());

		labelService.saveLabels(allLabels);
	}

	private static List<String> checkForDuplicateIds(List<ParsedDefinition> parseDefinitions,
			Function<ParsedDefinition, List<? extends Identity>> mapper, String objectKind) {

		Map<String, List<String>> labels = new HashMap<>();

		for (ParsedDefinition parsedDefinition : parseDefinitions) {
			for (Identity label : mapper.apply(parsedDefinition)) {
				List<String> definitionsIds = labels.computeIfAbsent(label.getIdentifier(), id -> new ArrayList<>());
				definitionsIds.add(parsedDefinition.getDefinition().getIdentifier());
			}
		}

		return labels.entrySet()
					.stream()
					.filter(entry -> entry.getValue().size() > 1)
					.map(entry -> {
						String message = "Duplicate " + objectKind + " with id '" + entry.getKey() + "' found in definitions "
							+ StringUtils.join(entry.getValue(), ", ");
						LOGGER.debug(message);
						return message;
					})
					.collect(Collectors.toList());
	}

	@Override
	public void initializeDataTypes() {
		definitionInitialization.onStart();
	}

	@Override
	public List<DefinitionInfo> getImportedDefinitions() {
		List<Object[]> rows = dbDao.fetchWithNamed(DefinitionEntry.QUERY_FETCH_IMPORTED_DEFINITIONS_KEY, Collections.emptyList());

		return rows.stream()
				.map(columns -> new DefinitionInfo((String) columns[0], (String) columns[1],
						Short.valueOf((short) 1).equals(columns[4]), (String) columns[2], (Date) columns[3]))
				.collect(Collectors.toList());
	}

	@Override
	public List<File> exportAllDefinitions() {
		File tempDir = tempFileProvider.createTempDir("definitionsExport");
		List<DefinitionContent> existingContents = dbDao
				.fetchWithNamed(DefinitionContent.FETCH_CONTENT_OF_ALL_DEFINITIONS_KEY, Collections.emptyList());
		return existingContents
					.stream()
					.map(content -> toXmlFile(content.getFileName(), content.getContent(), tempDir))
					.collect(Collectors.toList());
	}

	@Override
	public List<File> exportDefinitions(List<String> ids) {
		File tempDir = tempFileProvider.createTempDir("definitionsExport");
		List<DefinitionContent> existingContents = dbDao.fetchWithNamed(
				DefinitionContent.FETCH_CONTENT_BY_DEFINITION_IDS_KEY,
				Collections.singletonList(new Pair<>("ids", ids)));
		return existingContents
					.stream()
					.map(content -> toXmlFile(content.getFileName(), content.getContent(), tempDir))
					.collect(Collectors.toList());
	}

	private static File toXmlFile(String fileName, String content, File parentDir) {
		File file = new File(parentDir, fileName);
		try {
			FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new EmfApplicationException("Failed to write content to temp file " + fileName, e);
		}
		return file;
	}
}
