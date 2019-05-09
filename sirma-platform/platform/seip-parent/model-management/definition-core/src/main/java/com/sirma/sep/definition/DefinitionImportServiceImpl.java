package com.sirma.sep.definition;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
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
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.domain.validation.ValidationReport;
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
	public DefinitionValidationResult validate(Path newDefinitionsDirectory) {
		Objects.requireNonNull(newDefinitionsDirectory);

		ValidationReport validationReport = new ValidationReport();

		List<GenericDefinitionImpl> definitions = Collections.emptyList();

		try {
			LOGGER.debug("Begin validating definitions before import");

			definitionsDirectoryProcessor.prepareDefinitionsDirectory(newDefinitionsDirectory);

			List<ParsedDefinition> parsedDefinitions = new LinkedList<>();

			List<ValidationMessage> parseErrors = definitionsDirectoryProcessor.parseDefinitions(newDefinitionsDirectory, false,
					parsedDefinitions);
			validationReport.addMessages(parseErrors);

			List<ValidationMessage> duplicateLabelErrors = checkForDuplicateIds(parsedDefinitions, ParsedDefinition::getLabels, "<label>");
			validationReport.addMessages(duplicateLabelErrors);

			List<ValidationMessage> duplicateFiltersErrors = checkForDuplicateIds(parsedDefinitions, ParsedDefinition::getFilters,
					"<filter>");
			validationReport.addMessages(duplicateFiltersErrors);

			definitions = parsedDefinitions.stream()
					.map(ParsedDefinition::getDefinition)
					.collect(Collectors.toList());

			// Pre compile static validation
			List<ValidationMessage> definitionErrors = validationService.validateDefinitions(definitions);
			validationReport.addMessages(definitionErrors);

			definitionCompiler.compile(definitions);

			List<ValidationMessage> validationErrors = validationService.validateCompiledDefinitions(definitions);
			validationReport.addMessages(validationErrors);
		} catch (DefinitionValidationException e) {
			validationReport.addErrors(new LinkedList<>(e.getErrors()));
		}

		return new DefinitionValidationResult(validationReport, new LinkedList<>(definitions));
	}

	@Override
	public void importDefinitions(Path newDefinitionsDirectory) {
		LOGGER.info("Begin definition import");

		List<ParsedDefinition> parseDefinitions = new LinkedList<>();

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
		Map<String, GenericDefinitionImpl> index = definitions.stream()
				.collect(Collectors.toMap(GenericDefinitionImpl::getIdentifier, Function.identity()));

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
				definition.setRevision(existingDefinition.getRevision() + 1L);
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
		LOGGER.info("Detected definition identifier change from {} to {}. The old definition was removed {}successfully",
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

	private List<ValidationMessage> checkForDuplicateIds(List<ParsedDefinition> parseDefinitions,
			Function<ParsedDefinition, List<? extends Identity>> mapper, String objectKind) {

		// Label/Filter ID -> definition IDs
		Map<String, Set<String>> labels = new HashMap<>();

		for (ParsedDefinition parsedDefinition : parseDefinitions) {
			for (Identity label : mapper.apply(parsedDefinition)) {
				Set<String> definitionsIds = labels.computeIfAbsent(label.getIdentifier(), id -> new HashSet<>());
				definitionsIds.add(parsedDefinition.getDefinition().getIdentifier());
			}
		}

		DefinitionImportMessageBuilder messageBuilder = new DefinitionImportMessageBuilder();

		// Using the first definition to map the validation message
		labels.entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() > 1)
				.forEach(entry -> messageBuilder.duplicatedLabels(entry.getValue().iterator().next(), entry.getKey(), objectKind,
						entry.getValue()));

		return messageBuilder.getMessages();
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
		File tempDir = tempFileProvider.createUniqueTempDir("definitionsExport");
		List<DefinitionContent> existingContents = dbDao
				.fetchWithNamed(DefinitionContent.FETCH_CONTENT_OF_ALL_DEFINITIONS_KEY, Collections.emptyList());
		return existingContents
					.stream()
					.map(content -> toXmlFile(content.getFileName(), content.getContent(), tempDir))
					.collect(Collectors.toList());
	}

	@Override
	public List<File> exportDefinitions(List<String> ids) {
		File tempDir = tempFileProvider.createUniqueTempDir("definitionsExport");
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
