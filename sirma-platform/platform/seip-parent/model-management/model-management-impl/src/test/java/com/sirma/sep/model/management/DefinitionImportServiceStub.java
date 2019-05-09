package com.sirma.sep.model.management;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.jaxb.Label;
import com.sirma.itt.seip.definition.jaxb.LabelValue;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.model.management.stubs.LabelServiceStub;
import com.sirma.sep.xml.JAXBHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;

/**
 * Stubs the definition export functionality in a {@link DefinitionImportService} {@link org.mockito.Mock}.
 *
 * @author Mihail Radkov
 */
public class DefinitionImportServiceStub {

	private final LabelServiceStub labelServiceStub;
	private final Map<String, File> filesToExport;
	private final Map<String, File> clonedFiles;
	private final File tempDirectory;

	public DefinitionImportServiceStub(DefinitionImportService definitionImportService, LabelServiceStub labelServiceStub) {
		this.labelServiceStub = labelServiceStub;

		filesToExport = new LinkedHashMap<>();
		clonedFiles = new LinkedHashMap<>();
		try {
			tempDirectory = Files.createTempDirectory("models-copy-").toFile();
			tempDirectory.deleteOnExit();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		when(definitionImportService.exportAllDefinitions()).thenAnswer(invocation -> {
			cloneFilesIfAbsent(filesToExport.keySet());
			return new ArrayList(clonedFiles.values());
		});

		when(definitionImportService.exportDefinitions(anyList())).thenAnswer(invocation -> {
			List<String> identifiers = (List<String>) invocation.getArguments()[0];
			// Append .xml for easy look up
			Set<String> fileNames = identifiers.stream().map(id -> id + ".xml").collect(Collectors.toSet());
			cloneFilesIfAbsent(fileNames);
			return clonedFiles.values().stream().filter(file -> fileNames.contains(file.getName())).collect(Collectors.toList());
		});
	}

	private void cloneFilesIfAbsent(Set<String> files) {
		files.forEach(
				file -> clonedFiles.compute(file, (k, copy) -> copy != null && copy.exists() ? copy : cloneFile(filesToExport.get(file))));
	}

	/**
	 * Reset the stored definitions
	 */
	public void reset() {
		filesToExport.clear();
		clonedFiles.clear();
	}

	/**
	 * Uses the provided definition XML file path to configure the stub to provide it as {@link File} during export via
	 * {@link DefinitionImportService#exportAllDefinitions()}.
	 * <p>
	 * The file is cloned in case any service dispose of it after processing. To clean after testing use {@link #clear()}
	 * <p>
	 * Additionally any labels defined in the definition XML will be available via {@link com.sirma.itt.seip.definition.label.LabelService}.
	 *
	 * @param definitionXml the file path to a definition  xml to be used as test data
	 * @return a clone {@link File} of the originally provided XML
	 */
	public File withDefinition(String definitionXml) {
		File file = loadFile(definitionXml);
		filesToExport.put(file.getName(), file);

		File clone = cloneFile(file);
		clonedFiles.put(clone.getName(), clone);

		stubLabels(file);

		return clone;
	}

	private File loadFile(String fileName) {
		// The exported files are deleted after models calculation -> need a copy
		try {
			return new File(getClass().getResource(fileName).toURI());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private File cloneFile(File file) {
		try {
			File copy = new File(tempDirectory, file.getName());
			Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return copy;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void stubLabels(File definitionXml) {
		Definition definition = JAXBHelper.load(definitionXml, Definition.class);
		if (definition.getLabels() != null && CollectionUtils.isNotEmpty(definition.getLabels().getLabel())) {
			definition.getLabels()
					.getLabel()
					.stream()
					.map(label -> DefinitionImportServiceStub.toLabelDefinition(definition.getId(), label))
					.forEach(labelServiceStub::withLabelDefinition);
		}
	}

	private static LabelDefinition toLabelDefinition(String definedIn, Label label) {
		String labelId = label.getId();
		Map<String, String> labels = label.getValue().stream().collect(Collectors.toMap(LabelValue::getLang, LabelValue::getValue));
		return LabelServiceStub.build(labelId, definedIn, labels);
	}

	public void clear() {
		FileUtils.deleteQuietly(tempDirectory);
	}
}
