package com.sirma.sep.definition.fs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.jaxb.FilterDefinition;
import com.sirma.itt.seip.definition.jaxb.Label;
import com.sirma.itt.seip.definition.jaxb.LabelValue;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.model.FilterDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.definition.schema.BaseSchemas;
import com.sirma.itt.seip.domain.filter.Filter;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.sep.definition.DefinitionImportMessageBuilder;
import com.sirma.sep.definition.DefinitionValidationException;
import com.sirma.sep.definition.db.DefinitionContent;
import com.sirma.sep.xml.JAXBHelper;

/**
 * Performs utility operations on definition files.
 *
 * @author Adrian Mitev
 */
public class DefinitionFilesProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String EXISTING_DEFINITIONS_PATH = "__existing__";
	private static final Pattern DEFINITION_ID_SELECTOR = Pattern.compile("<definition\\s.*?id=\"(\\w+)\"\\s?.*?>");

	@Inject
	private DbDao dbDao;

	@Inject
	private ObjectMapper objectMapper;

	/**
	 * Creates a directory that contain the existing definition files overwritten by the definition files that will be
	 * imported in a flat structure (removing the original directory structure). Validates the new definitions for
	 * duplicate file names in different directories.
	 *
	 * @param newDefinitionsDirectory directory containing new definitions for processing
	 * @throws DefinitionValidationException
	 *             when validation fails.
	 */
	public void prepareDefinitionsDirectory(Path newDefinitionsDirectory) {
		if (Files.notExists(newDefinitionsDirectory)) { //NOSONAR
			throw new IllegalArgumentException("Definitions directory does not exist");
		}

		try (Stream<Path> definitionFilesStream = Files.walk(newDefinitionsDirectory)) {
			List<Path> files = definitionFilesStream.filter(Files::isRegularFile)
													.filter(file -> file.toString().toLowerCase().endsWith(".xml"))
													.collect(Collectors.toList());

			if (files.isEmpty()) {
				throw new IllegalArgumentException("No definition files provided");
			}

			LOGGER.debug("Found {} new definition files to import", files.size());

			List<String> errors = findDuplicateFilesNames(newDefinitionsDirectory, files);

			if (!errors.isEmpty()) {
				LOGGER.debug("Found files with duplicate name in different directories - {}", errors);
				throw new DefinitionValidationException(errors);
			}

			Path existingDefinitionsDirectory = newDefinitionsDirectory.resolve(EXISTING_DEFINITIONS_PATH);

			Files.deleteIfExists(existingDefinitionsDirectory);
			Files.createDirectories(existingDefinitionsDirectory);

			fetchExistingDefinitions(existingDefinitionsDirectory);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void fetchExistingDefinitions(Path existingDefinitionFilesDirectory) throws IOException {
		List<DefinitionContent> existingContents = dbDao.fetchWithNamed(DefinitionContent.FETCH_CONTENT_OF_ALL_DEFINITIONS_KEY,
					Collections.emptyList());

		for (DefinitionContent existingContent : existingContents) {
			Files.write(existingDefinitionFilesDirectory.resolve(existingContent.getFileName()), existingContent.getContent().getBytes(StandardCharsets.UTF_8));
		}
	}

	private static List<String> findDuplicateFilesNames(Path baseDir, List<Path> files) {
		return files.stream()
					.collect(Collectors.groupingBy(s -> s.getFileName().toString().toLowerCase()))
					.entrySet()
					.stream()
					.filter(entry -> entry.getValue().size() > 1)
					.map(entry -> entry.getValue().stream()
								  .map(current -> baseDir.relativize(current).toString())
								  .collect(Collectors.joining(", ")))
					.map(duplicateFiles -> "Duplicate file names found: " + duplicateFiles + " in the provided definitions")
					.collect(Collectors.toList());
	}

	/**
	 * Converts the set of existing + new definition files to java objects.
	 * A newly provided file overrides existing file with the same name (only name, not path).
	 * This method should be called only after {@link #prepareDefinitionsDirectory(Path)}.
	 *
	 * @param newDefinitionsDirectory directory where the new definitions are stored.
	 * @param includeContent when true, the source content of the definitions is also set
	 * @param destination list where to store the parsed definitions
	 * @return list of errors found during parsing
	 */
	public List<ValidationMessage> parseDefinitions(Path newDefinitionsDirectory, boolean includeContent, List<ParsedDefinition> destination) {
		Path existingDefinitionsDirectory = newDefinitionsDirectory.resolve(EXISTING_DEFINITIONS_PATH);

		if (Files.notExists(existingDefinitionsDirectory)) { //NOSONAR
			LOGGER.debug("Directory containing existing definitions does not exist");
			throw new IllegalArgumentException("Definitions should be prepared first");
		}

		DefinitionImportMessageBuilder messageBuilder = new DefinitionImportMessageBuilder();

		try (Stream<Path> definitionFilesStream = Files.walk(newDefinitionsDirectory);
				Stream<Path> existingFilesStream = Files.walk(existingDefinitionsDirectory)) {

			Map<String, Path> existingFiles = existingFilesStream
					.filter(Files::isRegularFile)
					.filter(file -> file.toString().toLowerCase().endsWith(".xml"))
					.collect(Collectors.toMap(x -> x.getFileName().toString(), Function.identity()));

			Map<String, Path> newFiles = definitionFilesStream
										.filter(Files::isRegularFile)
										.filter(file -> file.toString().toLowerCase().endsWith(".xml"))
										.filter(path -> !path.startsWith(existingDefinitionsDirectory))
										.collect(Collectors.toMap(x -> x.getFileName().toString(), Function.identity()));

			Map<String, String> changedIdentifiers = checkForChangedIdentifiers(existingFiles, newFiles);
			// override existing files with new files
			existingFiles.putAll(newFiles);

			for (Path definitionFile: existingFiles.values()) {
				List<String> parserErrors = validateDefinitionFile(definitionFile);

				if (!parserErrors.isEmpty()) {
					messageBuilder.xmlParsingFailure(definitionFile.getFileName().toString(), parserErrors);
					continue;
				}

				Definition definition = JAXBHelper.load(definitionFile, Definition.class);

				GenericDefinitionImpl genericDefinition = objectMapper.map(definition, GenericDefinitionImpl.class);

				if (includeContent) {
					String fileName = definitionFile.getFileName().toString();
					boolean isNewDefinitionFile = newFiles.containsKey(fileName);

					if (isNewDefinitionFile) {
						genericDefinition.setSourceFile(fileName);

						String content = new String(Files.readAllBytes(definitionFile), StandardCharsets.UTF_8);
						genericDefinition.setSourceContent(content);

						String previousId = changedIdentifiers.get(definitionFile.getFileName().toString());
						genericDefinition.setPreviousIdentifier(previousId);
					}
				}

				destination.add(new ParsedDefinition(genericDefinition, getLabels(definition), getFilters(definition)));
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return messageBuilder.getMessages();
	}

	private Map<String, String> checkForChangedIdentifiers(Map<String, Path> existingFiles,
			Map<String, Path> newFiles) {

		Set<String> modifiedDefinitions = CollectionUtils.intersection(existingFiles.keySet(), newFiles.keySet());
		Map<String, String> definitionIdChanges = new HashMap<>();
		for (String definitionFileName : modifiedDefinitions) {
			String currentDefinitionId = resolveDefinitionId(existingFiles.get(definitionFileName));
			String newDefinitionId = resolveDefinitionId(newFiles.get(definitionFileName));
			if (!currentDefinitionId.equals(newDefinitionId)) {
				definitionIdChanges.put(definitionFileName, currentDefinitionId);
			}
		}
		return definitionIdChanges;
	}

	private String resolveDefinitionId(Path definitionPath) {
		try (InputStream fileStream = Files.newInputStream(definitionPath, StandardOpenOption.READ)) {
			String definitionContent = IOUtils.toString(fileStream);
			Matcher matcher = DEFINITION_ID_SELECTOR.matcher(definitionContent);
			if (matcher.find()) {
				return matcher.group(1);
			}
			throw new IllegalArgumentException("Detected definition without defined definition identifier in file " + definitionPath);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static List<LabelDefinition> getLabels(Definition definition) {
		List<LabelDefinition> labels;
		if (definition.getLabels() != null) {
			labels = convertLabels(definition.getLabels().getLabel(), definition.getId());
		} else {
			labels = new ArrayList<>();
		}
		return labels;
	}

	private List<Filter> getFilters(Definition definition) {
		List<Filter> filters;
		if (definition.getFilterDefinitions() != null) {
			filters = convertFilter(definition.getFilterDefinitions().getFilter(), definition.getId());
		} else {
			filters = new ArrayList<>();
		}
		return filters;
	}

	private static List<String> validateDefinitionFile(Path definitionFile) {
		return JAXBHelper.validateFile(definitionFile, BaseSchemas.GENERIC_DEFINITION);
	}

	private static List<LabelDefinition> convertLabels(List<Label> labels, String definedIn) {
		return labels.stream()
					.map(current -> {
						LabelImpl label = new LabelImpl();
						label.setIdentifier(current.getId());
						List<LabelValue> values = current.getValue();
						Map<String, String> map = CollectionUtils.createLinkedHashMap(values.size());
						for (LabelValue value : values) {
							map.put(value.getLang(), value.getValue());
						}
						label.setLabels(map);
						label.addDefinedIn(definedIn);
						return label;
					})
					.collect(Collectors.toList());
	}

	private List<Filter> convertFilter(List<FilterDefinition> labels, String definitionId) {
		return labels.stream()
					.map(current -> objectMapper.map(current, FilterDefinitionImpl.class))
					.peek(filter -> filter.addDefinedIn(definitionId))
					.collect(Collectors.toList());
	}

	public static class ParsedDefinition {
		private final GenericDefinitionImpl definition;
		private final List<LabelDefinition> labels;
		private final List<Filter> filters;

		public ParsedDefinition(GenericDefinitionImpl definition, List<LabelDefinition> labels,
				List<Filter> filters) {
			this.definition = definition;
			this.labels = labels;
			this.filters = filters;
		}

		public GenericDefinitionImpl getDefinition() {
			return definition;
		}

		public List<LabelDefinition> getLabels() {
			return labels;
		}

		public List<Filter> getFilters() {
			return filters;
		}
	}

}
