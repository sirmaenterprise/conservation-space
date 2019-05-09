package com.sirma.sep.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.template.TemplateImportService;
import com.sirma.itt.seip.template.TemplateValidationRequest;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.file.ArchiveUtil;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.definition.DefinitionValidationResult;
import com.sirma.sep.threads.ThreadInterrupter;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMDefinitionImportService;
import com.sirmaenterprise.sep.roles.PermissionsImportService;

/**
 * Default implementation of {@link ModelImportService}
 *
 * @author Vilizar Tsonev
 */
public class ModelImportServiceImpl implements ModelImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String DEFINITION = "definition";

	private static final String TEMPLATE = "template";

	private static final String BPMN = "bpmn";

	private static final String PERMISSION = "permission";

	private static final String TEMPLATE_ROOT_TAG = "templateDefinition";

	private static final String ROLES_ROOT_TAG = "roles";

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private DefinitionImportService definitionImportService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private TemplateImportService templateImportService;

	@Inject
	private PermissionsImportService permissionsImportService;

	@Inject
	private BPMDefinitionImportService bPMDefinitionImportService;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private EventService eventService;

	@Inject
	private ContextualLock importLock;

	@Inject
	private ThreadInterrupter threadInterrupter;

	@Override
	public ValidationReport importModel(Map<String, InputStream> files) {
		ModelDirectoriesHolder dirHolder = getDirectoriesHolder();

		try {
			if (!tryLocking()) {
				return new ModelImportValidationBuilder().importTimeout();
			}
			prepareArchivedDefinitions(files, dirHolder);
			prepareNonArchiveFiles(files, dirHolder);

			ValidationReport validationReport = validate(dirHolder);
			if (!validationReport.isValid()) {
				return validationReport;
			}

			doImport(dirHolder);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new ModelImportValidationBuilder().importException(e);
		} finally {
			dirHolder.cleanupAllDirectories();
			unlock();
		}

		return ValidationReport.valid();
	}

	@Override
	public ValidationReport validateModel(Map<String, InputStream> files) {
		ModelDirectoriesHolder dirHolder = getDirectoriesHolder();
		try {
			prepareArchivedDefinitions(files, dirHolder);
			prepareNonArchiveFiles(files, dirHolder);
			return validate(dirHolder);
		} catch (IllegalArgumentException e) {
			return new ValidationReport().addError(e.getMessage());
		} finally {
			dirHolder.cleanupAllDirectories();
		}
	}

	@Override
	public File exportModel(ModelExportRequest request) {
		validateExportRequest(request);

		List<File> definitionFiles = exportDefinitionFiles(request);
		List<File> templateFiles = exportTemplateFiles(request);
		boolean hasDefinitions = !definitionFiles.isEmpty();
		boolean hasTemplates = !templateFiles.isEmpty();
		LOGGER.info("Initiating export of {} model files.", definitionFiles.size() + templateFiles.size());

		// single file from single type => return it as it is (as xml)
		if (definitionFiles.size() == 1 && !hasTemplates) {
			return definitionFiles.get(0);
		} else if (templateFiles.size() == 1 && !hasDefinitions) {
			return templateFiles.get(0);
		}

		// multiple files from single type => archive them flat and return the zip
		if (hasDefinitions && !hasTemplates) {
			return filesToZip(definitionFiles, DEFINITION);
		} else if (hasTemplates && !hasDefinitions) {
			return filesToZip(templateFiles, TEMPLATE);
		}

		// multiple files from both types => group them in directories and zip them
		if (hasDefinitions) {
			return groupInDirectoriesAndZip(definitionFiles, templateFiles);
		}

		throw new EmfApplicationException("No templates or definitions for export were found in the system");
	}

	private ModelDirectoriesHolder getDirectoriesHolder() {
		ModelDirectoriesHolder dirHolder = new ModelDirectoriesHolder();
		File rootDir = tempFileProvider.createUniqueTempDir("modelImportRootDir");
		dirHolder.setRootDir(rootDir);

		dirHolder.setDefinitionsDir(tempFileProvider.createSubDir(rootDir, DEFINITION));
		dirHolder.setTemplatesDir(tempFileProvider.createSubDir(rootDir, TEMPLATE));
		dirHolder.setBpmnDir(tempFileProvider.createSubDir(rootDir, BPMN));
		dirHolder.setPermissionsDir(tempFileProvider.createSubDir(rootDir, PERMISSION));
		return dirHolder;
	}

	private boolean tryLocking() {
		try {
			return importLock.tryLock(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) { // NOSONAR
			// the exception is handled by interrupting the current thread
			threadInterrupter.interruptCurrentThread();
			throw new IllegalStateException(e);
		}
	}

	private void unlock() {
		importLock.unlock();
	}

	private File createUniqueTempDir(String prefix) {
		return tempFileProvider.createUniqueTempDir(prefix);
	}

	private File groupInDirectoriesAndZip(List<File> definitions, List<File> templates) {
		try {
			File modelFilesDir = createUniqueTempDir("modelFilesDir");
			File templatesDir = new File(modelFilesDir, TEMPLATE);
			File definitionsDir = new File(modelFilesDir, DEFINITION);

			for (File definition : definitions) {
				FileUtils.copyFileToDirectory(definition, definitionsDir);
			}
			for (File template : templates) {
				FileUtils.copyFileToDirectory(template, templatesDir);
			}
			LOGGER.debug("Added {} definitions and {} templates to their relevant folders for export",
					definitions.size(), templates.size());
			// the empty zip file is created in a separate dir, because otherwise it will archive itself
			File archiveDir = createUniqueTempDir("modelArchiveDir");
			File zipFile = new File(archiveDir, "models.zip");
			ArchiveUtil.zipFile(modelFilesDir, zipFile);
			return zipFile;
		} catch (IOException e) {
			throw new EmfApplicationException("An error occured while exporting model files.", e);
		}
	}

	private File filesToZip(List<File> modelFiles, String modelsType) {
		try {
			File modelFilesDir = createUniqueTempDir("modelFilesDir");
			for (File file : modelFiles) {
				FileUtils.copyFileToDirectory(file, modelFilesDir);
			}
			LOGGER.debug("Preparing {} {} files for archiving and export", modelFiles.size(), modelsType);
			// the "s" at the end of the file name is because plural is required
			String zipFileName = modelsType + "s.zip";
			// the empty zip file is created in a separate dir, because otherwise it will archive itself
			File archiveDir = createUniqueTempDir("modelArchiveDir");
			File zipFile = new File(archiveDir, zipFileName);
			ArchiveUtil.zipFile(modelFilesDir, zipFile);
			return zipFile;
		} catch (IOException e) {
			throw new EmfApplicationException("An error occured while exporting model files.", e);
		}
	}

	private List<File> exportDefinitionFiles(ModelExportRequest request) {
		if (request.isAllDefinitions()) {
			return definitionImportService.exportAllDefinitions();
		} else if (CollectionUtils.isNotEmpty(request.getDefinitions())) {
			return definitionImportService.exportDefinitions(request.getDefinitions());
		}
		return Collections.emptyList();
	}

	private List<File> exportTemplateFiles(ModelExportRequest request) {
		if (request.isAllTemplates()) {
			return templateImportService.exportAllTemplates();
		} else if (CollectionUtils.isNotEmpty(request.getTemplates())) {
			return templateImportService.exportTemplates(request.getTemplates());
		}
		return Collections.emptyList();
	}

	private static void validateExportRequest(ModelExportRequest request) {
		Objects.requireNonNull(request, "Model export request is not provided.");
		if (!request.isAllDefinitions() && !request.isAllTemplates()
				&& CollectionUtils.isEmpty(request.getDefinitions())
				&& CollectionUtils.isEmpty(request.getTemplates())) {
			throw new IllegalArgumentException(
					"The provided model export request is empty. No models were requested with it.");
		}
	}

	private ValidationReport validate(ModelDirectoriesHolder dirHolder) {
		ValidationReport validationReport = new ValidationReport();

		// these flags will be needed later (in other methods) again, so set them to the dirHolder to avoid checking
		// directories recursively every time
		dirHolder.setHasDefinitions(containsAnyFiles(dirHolder.getDefinitionsDir()));
		dirHolder.setHasTemplates(containsAnyFiles(dirHolder.getTemplatesDir()));
		dirHolder.setHasPermissions(containsAnyFiles(dirHolder.getPermissionsDir()));
		dirHolder.setHasBPMNs(containsAnyFiles(dirHolder.getBpmnDir()));

		if (!dirHolder.hasDefinitions() && !dirHolder.hasTemplates() && !dirHolder.hasPermissions()
				&& !dirHolder.hasBPMNs()) {
			return new ValidationReport().addError(
					"No model files have been provided for import, or the provided archive is effectively empty.");
		}

		List<GenericDefinition> definitions;

		if (dirHolder.hasDefinitions()) {
			DefinitionValidationResult validationResult = definitionImportService.validate(dirHolder.getDefinitionsDir().toPath());
			validationReport.merge(validationResult.getValidationReport());
			definitions = validationResult.getDefinitions();
		} else {
			// Templates must be validated against the definitions, if none are provided for import -> use the available
			definitions = definitionService.getAllDefinitions(GenericDefinition.class);
		}

		if (dirHolder.hasTemplates()) {
			TemplateValidationRequest validationRequest = new TemplateValidationRequest(dirHolder.getTemplatesDir().getAbsolutePath(),
					definitions);
			List<String> templatesValidationErrors = templateImportService.validate(validationRequest);
			validationReport.addErrors(templatesValidationErrors);
		}

		if (dirHolder.hasPermissions()) {
			List<String> permissionsValidationErrors = permissionsImportService.validate(dirHolder.getPermissionsDir().getAbsolutePath());
			validationReport.addErrors(permissionsValidationErrors);
		}

		return validationReport;
	}

	private void doImport(ModelDirectoriesHolder dirHolder) {
		LOGGER.info("Models validation passed without errors. Initiating import.");
		TimeTracker tracker = TimeTracker.createAndStart();

		if (dirHolder.hasDefinitions()) {
			// a new transaction is required because the definitions has to be immediately visible for other transactions
			transactionSupport.invokeInNewTx(() -> definitionImportService.importDefinitions(dirHolder.getDefinitionsDir().toPath()));
		}
		if (dirHolder.hasPermissions()) {
			permissionsImportService.importPermissions(dirHolder.getPermissionsDir().getAbsolutePath());
		}
		if (dirHolder.hasBPMNs()) {
			bPMDefinitionImportService.importDefinitions(dirHolder.getBpmnDir().getAbsolutePath());
		}
		if (dirHolder.hasTemplates()) {
			templateImportService.importTemplates(dirHolder.getTemplatesDir().getAbsolutePath());
		}

		eventService.fire(new ModelImportCompleted());
		LOGGER.info("============ Models successfully imported. Import process took {} sec ============", tracker.stopInSeconds());
	}

	private static void prepareNonArchiveFiles(Map<String, InputStream> files, ModelDirectoriesHolder dirHolder) {
		files.entrySet().stream()
				.filter(entry -> !isZipFile(entry.getKey()))
				.forEach(entry -> materializeImportFile(entry.getKey(), entry.getValue(), dirHolder));
	}

	/**
	 * Recognizes the definition file type (by using the file extension and the root tag if needed) and creates a file
	 * for it in the relevant import directory.
	 */
	private static void materializeImportFile(String fileName, InputStream stream, ModelDirectoriesHolder dirHolder) {
		String content;
		try {
			content = IOUtils.toString(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		if (fileName.toLowerCase().endsWith(".bpmn")) {
			createInDirectory(fileName, content, dirHolder.getBpmnDir());
		} else if (fileName.toLowerCase().endsWith(".xml")) {
			String rootTag = getRootTag(content, fileName);
			if (DEFINITION.equals(rootTag)) {
				createInDirectory(fileName, content, dirHolder.getDefinitionsDir());
			} else if (TEMPLATE_ROOT_TAG.equals(rootTag)) {
				createInDirectory(fileName, content, dirHolder.getTemplatesDir());
			} else if (ROLES_ROOT_TAG.equals(rootTag)) {
				createInDirectory(fileName, content, dirHolder.getPermissionsDir());
			} else {
				throw new IllegalArgumentException(
						"XML file '" + fileName + "' has an unsupported root tag - <" + rootTag + ">");
			}
		} else {
			throw new IllegalArgumentException(
					"File '" + fileName + "' is of unsupported file format.");
		}
	}

	private static void createInDirectory(String fileName, String content, File directory) {
		LOGGER.trace("Creating auto-recognized model file {} in temp directory {}", fileName, directory.getName());
		File file = new File(directory, fileName);
		try {
			FileUtils.write(file, content);
		} catch (IOException e) {
			LOGGER.error("Failed to create temp definition file {} due to {}", fileName, e);
		}
	}

	private void prepareArchivedDefinitions(Map<String, InputStream> files, ModelDirectoriesHolder dirHolder) {
		File unzipDirectory = null;
		try (InputStream archiveStream = files
				.entrySet()
				.stream()
				.filter(entry -> isZipFile(entry.getKey()))
				.findFirst()
				.map(Entry::getValue)
				.orElse(null)) {

			if (archiveStream == null) {
				return;
			}

			LOGGER.debug("Archive file detected during models import. Proceeding with unzip...");
			unzipDirectory = createUniqueTempDir("modelsUnzipDir");
			ArchiveUtil.unZip(archiveStream, unzipDirectory);

			copyToRelevantImportDirectories(unzipDirectory, dirHolder);
		} catch (IOException e) {
			LOGGER.error("Failed to copy unzipped model definitions to their respective import directories", e);
		} finally {
			// deleteQuietly has a param null check, so no need to do it
			FileUtils.deleteQuietly(unzipDirectory);
		}
	}

	private static void copyToRelevantImportDirectories(File unzipDirectory, ModelDirectoriesHolder dirHolder)
			throws IOException {
		List<File> allModelFiles = FileUtil.loadFromPath(unzipDirectory.getAbsolutePath());
		for (File file : allModelFiles) {
			String fileName = file.getName();
			LOGGER.trace("Processing unarchived model file '{}'.", fileName);
			if (fileName.toLowerCase().endsWith(".bpmn")) {
				FileUtils.copyFileToDirectory(file, dirHolder.getBpmnDir());
			} else if (fileName.toLowerCase().endsWith(".xml")) {
				String content = FileUtils.readFileToString(file);
				String rootTag = getRootTag(content, fileName);
				if (DEFINITION.equals(rootTag)) {
					FileUtils.copyFileToDirectory(file, dirHolder.getDefinitionsDir());
				} else if (TEMPLATE_ROOT_TAG.equals(rootTag)) {
					FileUtils.copyFileToDirectory(file, dirHolder.getTemplatesDir());
				} else if (ROLES_ROOT_TAG.equals(rootTag)) {
					FileUtils.copyFileToDirectory(file, dirHolder.getPermissionsDir());
				} else {
					throw new IllegalArgumentException(
							"XML file '" + fileName + "' has an unsupported root tag - <" + rootTag + ">");
				}
			} else {
				LOGGER.debug("File '{}' is of unsupported file format and will not be imported.", fileName);
			}
		}
	}

	private static boolean containsAnyFiles(File directoryPath) {
		return !FileUtil.loadFromPath(directoryPath.getAbsolutePath()).isEmpty();
	}

	private static String getRootTag(String content, String fileName) {
		if (StringUtils.isBlank(content)) {
			throw new IllegalArgumentException("Failed to parse " + fileName + " because it is empty.");
		}
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLStreamReader xmlReader = factory.createXMLStreamReader(
					new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());
			while (xmlReader.hasNext()) {
				if (xmlReader.next() == XMLStreamConstants.START_ELEMENT) {
					return xmlReader.getName().getLocalPart();
				}
			}
		} catch (XMLStreamException e) {
			throw new IllegalArgumentException("Failed to parse " + fileName + " due to: " + e.getMessage());
		}
		return null;
	}

	private static boolean isZipFile(String fileName) {
		return fileName.toLowerCase().endsWith(".zip");
	}

	/**
	 * Unites all definition import directory paths on one place. Removes the need to pass every path as a separate
	 * parameter to the methods involved in the processing & import.
	 */
	private class ModelDirectoriesHolder {

		private File rootDir;
		private File definitionsDir;
		private File templatesDir;
		private File bpmnDir;
		private File permissionsDir;

		// these flags indicate if the temp import directories actually contain any files.
		// Removes the need to recursively check the relevant folders each time
		private boolean hasDefinitions;
		private boolean hasTemplates;
		private boolean hasBPMNs;
		private boolean hasPermissions;

		public void cleanupAllDirectories() {
			FileUtils.deleteQuietly(rootDir);
		}

		public void setRootDir(File rootDir) {
			this.rootDir = rootDir;
		}

		public File getDefinitionsDir() {
			return definitionsDir;
		}

		public void setDefinitionsDir(File definitionsDir) {
			this.definitionsDir = definitionsDir;
		}

		public File getTemplatesDir() {
			return templatesDir;
		}

		public void setTemplatesDir(File templatesDir) {
			this.templatesDir = templatesDir;
		}

		public File getBpmnDir() {
			return bpmnDir;
		}

		public void setBpmnDir(File bpmnDir) {
			this.bpmnDir = bpmnDir;
		}

		public File getPermissionsDir() {
			return permissionsDir;
		}

		public void setPermissionsDir(File permissionsDir) {
			this.permissionsDir = permissionsDir;
		}

		public boolean hasDefinitions() {
			return hasDefinitions;
		}

		public void setHasDefinitions(boolean hasDefinitions) {
			this.hasDefinitions = hasDefinitions;
		}

		public boolean hasTemplates() {
			return hasTemplates;
		}

		public void setHasTemplates(boolean hasTemplates) {
			this.hasTemplates = hasTemplates;
		}

		public boolean hasBPMNs() {
			return hasBPMNs;
		}

		public void setHasBPMNs(boolean hasBPMNs) {
			this.hasBPMNs = hasBPMNs;
		}

		public boolean hasPermissions() {
			return hasPermissions;
		}

		public void setHasPermissions(boolean hasPermissions) {
			this.hasPermissions = hasPermissions;
		}
	}
}
