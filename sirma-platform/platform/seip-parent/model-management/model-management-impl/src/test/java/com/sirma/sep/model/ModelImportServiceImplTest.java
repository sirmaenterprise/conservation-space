package com.sirma.sep.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.template.TemplateImportService;
import com.sirma.itt.seip.testutil.fakes.TempFileProviderFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.file.ArchiveUtil;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMDefinitionImportService;
import com.sirmaenterprise.sep.roles.PermissionsImportService;

public class ModelImportServiceImplTest {

	@InjectMocks
	ModelImportServiceImpl modelImportService;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Spy
	private TempFileProvider tempFileProvider;

	@Mock
	private DefinitionImportService definitionImportService;

	@Mock
	private TemplateImportService templateImportService;

	@Mock
	private BPMDefinitionImportService bPMDefinitionImportService;

	@Mock
	private PermissionsImportService permissionsImportService;

	@Mock
	private EventService eventService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	private static final String DEFINITION_DIR = "definition";

	private static final String TEMPLATE_DIR = "template";

	private Map<String, InputStream> importMapping;

	private Set<String> lastlyImportedDefinitionFiles;
	private Set<String> lastlyImportedTemplateFiles;
	private Set<String> lastlyImportedBPMNFiles;
	private Set<String> lastlyImportedPermissionFiles;


	@Test
	public void should_Import_All_Models_From_A_Valid_Archive() {
		withFiles("correctModels.zip");

		modelImportService.importModel(importMapping);

		verifyBPMNsImported("WF1.bpmn");
		verifyDefinitionsImported("genericCase.xml", "genericObjectDefinition.xml", "genericProject.xml");
		verifyRolesImported("consumer.xml", "contributor.xml");
		verifyTemplatesImported("email_template.xml", "helptemplate.xml");
		verifyModelsImportCompleteEventIsFired();
	}

	@Test
	public void should_Import_All_Models_When_Provided_A_Valid_Archive_Plus_NotArchived_Files() {
		withFiles("correctModels.zip", "manager.xml", "projectDefinition.xml", "testTemplate.xml", "WF2.bpmn");

		modelImportService.importModel(importMapping);

		verifyBPMNsImported("WF1.bpmn", "WF2.bpmn");
		verifyDefinitionsImported("genericCase.xml", "genericObjectDefinition.xml", "genericProject.xml",
				"projectDefinition.xml");
		verifyRolesImported("consumer.xml", "contributor.xml", "manager.xml");
		verifyTemplatesImported("email_template.xml", "helptemplate.xml", "testTemplate.xml");
		verifyModelsImportCompleteEventIsFired();
	}

	@Test
	public void should_Import_Not_Archived_Model_Files() {
		withFiles("manager.xml", "projectDefinition.xml", "testTemplate.xml", "WF2.bpmn");

		modelImportService.importModel(importMapping);

		verifyBPMNsImported("WF2.bpmn");
		verifyDefinitionsImported("projectDefinition.xml");
		verifyRolesImported("manager.xml");
		verifyTemplatesImported("testTemplate.xml");
		verifyModelsImportCompleteEventIsFired();
	}

	@Test
	public void should_Detect_Error_When_Provided_An_Archive_With_Invalid_XML_Files() {
		withFiles("modelsInvalidRootTag.zip");
		verifyInvalid(modelImportService.importModel(importMapping));
		verifyModelsImportCompleteEventIsNotFired();
	}

	@Test
	public void should_Detect_Error_When_Provided_An_Archive_With_No_Files_In_It() {
		withFiles("noModels.zip");
		verifyInvalid(modelImportService.importModel(importMapping));
		verifyModelsImportCompleteEventIsNotFired();
	}

	@Test
	public void should_Detect_Error_When_Provided_Invalid_Type_Of_Archive() {
		withFiles("tarArchive.tar");
		verifyInvalid(modelImportService.importModel(importMapping));
		verifyModelsImportCompleteEventIsNotFired();
	}

	@Test
	public void should_Detect_Error_When_Provided_Not_Archived_Invalid_Files() {
		withFiles("manager.xml", "codelists.xlsx");
		verifyInvalid(modelImportService.importModel(importMapping));
		verifyModelsImportCompleteEventIsNotFired();
	}

	@Test
	public void should_Detect_Error_When_Provided_A_Valid_Archive_But_Invalid_NotArchived_Files() {
		withFiles("correctModels.zip", "codelists.xlsx");
		verifyInvalid(modelImportService.importModel(importMapping));
		verifyModelsImportCompleteEventIsNotFired();
	}

	@Test
	public void should_Detect_Error_When_Provided_An_XML_File_With_Unsupported_Root_Tag() {
		withFiles("manager.xml", "invalidRootTag.xml");
		verifyInvalid(modelImportService.importModel(importMapping));
		verifyModelsImportCompleteEventIsNotFired();
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_If_Empty_Export_Request_Passed() {
		ModelExportRequest request = new ModelExportRequest();
		modelImportService.exportModel(request);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_No_Models_Were_Found_For_Export() {
		withExistingDefinitions(new String[0]);
		withExistingTemplates(new String[0]);

		ModelExportRequest request = new ModelExportRequest();
		request.setAllTemplates(true);
		request.setAllDefinitions(true);
		modelImportService.exportModel(request);
	}

	@Test
	public void should_Export_Single_Definition_As_Xml() throws IOException {
		List<File> existing = withExistingDefinitions("projectDefinition.xml");
		List<String> requestIds = Arrays.asList("defId");

		ModelExportRequest request = new ModelExportRequest();
		request.setDefinitions(requestIds);
		File exported = modelImportService.exportModel(request);

		verifyDefinitionIdsRequested(requestIds);
		verifyXmlFileExported(existing.get(0), exported);
	}

	@Test
	public void should_Export_Single_Template_As_Xml() throws IOException {
		List<File> existing = withExistingTemplates("testTemplate.xml");
		List<String> requestIds = Arrays.asList("templateId");

		ModelExportRequest request = new ModelExportRequest();
		request.setTemplates(requestIds);
		File exported = modelImportService.exportModel(request);

		verifyTemplateIdsRequested(requestIds);
		verifyXmlFileExported(existing.get(0), exported);
	}

	@Test
	public void should_Export_Requested_Definitions_Archived() throws IOException {
		List<File> existing = withExistingDefinitions("projectDefinition.xml", "caseDefinition.xml",
				"taskDefinition.xml");
		List<String> requestIds = Arrays.asList("projectDef", "caseDef", "taskDef");

		ModelExportRequest request = new ModelExportRequest();
		request.setDefinitions(requestIds);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "definitions.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyDefinitionIdsRequested(requestIds);
		verifyNoDirectoriesArePresent(unzipDirectory);
		verifyDirContainsFiles(unzipDirectory, existing);
	}

	@Test
	public void should_Export_All_Definitions_Archived() throws IOException {
		List<File> existing = withExistingDefinitions("projectDefinition.xml", "caseDefinition.xml",
				"taskDefinition.xml");

		ModelExportRequest request = new ModelExportRequest();
		request.setAllDefinitions(true);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "definitions.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyNoDirectoriesArePresent(unzipDirectory);
		verifyDirContainsFiles(unzipDirectory, existing);
	}

	@Test
	public void should_Export_Requested_Templates_Archived() throws IOException {
		List<File> existing = withExistingTemplates("testTemplate.xml", "testTemplate2.xml");
		List<String> requestIds = Arrays.asList("template1", "template2");

		ModelExportRequest request = new ModelExportRequest();
		request.setTemplates(requestIds);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "templates.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyTemplateIdsRequested(requestIds);
		verifyNoDirectoriesArePresent(unzipDirectory);
		verifyDirContainsFiles(unzipDirectory, existing);
	}

	@Test
	public void should_Export_All_Templates_Archived() throws IOException {
		List<File> existing = withExistingTemplates("testTemplate.xml", "testTemplate2.xml");

		ModelExportRequest request = new ModelExportRequest();
		request.setAllTemplates(true);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "templates.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyNoDirectoriesArePresent(unzipDirectory);
		verifyDirContainsFiles(unzipDirectory, existing);
	}

	@Test
	public void should_Export_Requested_Definitions_And_Templates_Put_In_Folders_And_Archived() throws IOException {
		List<File> existingDefinitions = withExistingDefinitions("projectDefinition.xml", "caseDefinition.xml",
				"taskDefinition.xml");
		List<File> existingTemplates = withExistingTemplates("testTemplate.xml", "testTemplate2.xml");
		List<String> requestDefIds = Arrays.asList("projectDef", "caseDef", "taskDef");
		List<String> requestTemplateIds = Arrays.asList("template1", "template2");

		ModelExportRequest request = new ModelExportRequest();
		request.setDefinitions(requestDefIds);
		request.setTemplates(requestTemplateIds);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "models.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyDefinitionIdsRequested(requestDefIds);
		verifyTemplateIdsRequested(requestTemplateIds);
		verifyExactDirectoriesArePresent(unzipDirectory, TEMPLATE_DIR, DEFINITION_DIR);

		File definitionDir = getSubDirectory(unzipDirectory, DEFINITION_DIR);
		verifyDirContainsFiles(definitionDir, existingDefinitions);

		File templateDir = getSubDirectory(unzipDirectory, TEMPLATE_DIR);
		verifyDirContainsFiles(templateDir, existingTemplates);
	}

	@Test
	public void should_Export_All_Definitions_And_All_Templates_Put_In_Folders_And_Archived() throws IOException {
		List<File> existingDefinitions = withExistingDefinitions("projectDefinition.xml", "caseDefinition.xml",
				"taskDefinition.xml");
		List<File> existingTemplates = withExistingTemplates("testTemplate.xml", "testTemplate2.xml");

		ModelExportRequest request = new ModelExportRequest();
		request.setAllDefinitions(true);
		request.setAllTemplates(true);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "models.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyExactDirectoriesArePresent(unzipDirectory, TEMPLATE_DIR, DEFINITION_DIR);

		File definitionDir = getSubDirectory(unzipDirectory, DEFINITION_DIR);
		verifyDirContainsFiles(definitionDir, existingDefinitions);

		File templateDir = getSubDirectory(unzipDirectory, TEMPLATE_DIR);
		verifyDirContainsFiles(templateDir, existingTemplates);
	}

	@Test
	public void should_Export_All_Definitions_And_Requested_Templates_Put_In_Folders_And_Archived() throws IOException {
		List<File> existingDefinitions = withExistingDefinitions("projectDefinition.xml", "caseDefinition.xml",
				"taskDefinition.xml");
		List<File> existingTemplates = withExistingTemplates("testTemplate.xml", "testTemplate2.xml");
		List<String> requestTemplateIds = Arrays.asList("template1", "template2");

		ModelExportRequest request = new ModelExportRequest();
		request.setAllDefinitions(true);
		request.setTemplates(requestTemplateIds);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "models.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyTemplateIdsRequested(requestTemplateIds);
		verifyExactDirectoriesArePresent(unzipDirectory, TEMPLATE_DIR, DEFINITION_DIR);

		File definitionDir = getSubDirectory(unzipDirectory, DEFINITION_DIR);
		verifyDirContainsFiles(definitionDir, existingDefinitions);

		File templateDir = getSubDirectory(unzipDirectory, TEMPLATE_DIR);
		verifyDirContainsFiles(templateDir, existingTemplates);
	}

	@Test
	public void should_Export_Requested_Definitions_And_All_Templates_Put_In_Folders_And_Archived() throws IOException {
		List<File> existingDefinitions = withExistingDefinitions("projectDefinition.xml", "caseDefinition.xml",
				"taskDefinition.xml");
		List<File> existingTemplates = withExistingTemplates("testTemplate.xml", "testTemplate2.xml");
		List<String> requestDefIds = Arrays.asList("projectDef", "caseDef", "taskDef");

		ModelExportRequest request = new ModelExportRequest();
		request.setDefinitions(requestDefIds);
		request.setAllTemplates(true);
		File exported = modelImportService.exportModel(request);

		verifyFileExportedWithName(exported, "models.zip");
		File unzipDirectory = unzipToNewTempDir(exported);

		verifyDefinitionIdsRequested(requestDefIds);
		verifyExactDirectoriesArePresent(unzipDirectory, TEMPLATE_DIR, DEFINITION_DIR);

		File definitionDir = getSubDirectory(unzipDirectory, DEFINITION_DIR);
		verifyDirContainsFiles(definitionDir, existingDefinitions);

		File templateDir = getSubDirectory(unzipDirectory, TEMPLATE_DIR);
		verifyDirContainsFiles(templateDir, existingTemplates);
	}

	@Before
	public void init() {
		tempFileProvider = new TempFileProviderFake(tempFolder.getRoot());
		MockitoAnnotations.initMocks(this);
		importMapping = new HashMap<>();
		lastlyImportedDefinitionFiles = new HashSet<>();
		lastlyImportedTemplateFiles = new HashSet<>();
		lastlyImportedBPMNFiles = new HashSet<>();
		lastlyImportedPermissionFiles = new HashSet<>();
	}

	@After
	public void cleanUp() {
		importMapping.entrySet().stream().forEach(entry -> {
			IOUtils.closeQuietly(entry.getValue());
		});
	}

	private File unzipToNewTempDir(File exported) throws IOException {
		File unzipDirectory = tempFolder.newFolder();
		ArchiveUtil.unZip(exported, unzipDirectory);
		return unzipDirectory;
	}

	private void verifyDefinitionsImported(String... fileNames) {
		Set<String> expectedFiles = new HashSet<>(Arrays.asList(fileNames));
		assertEquals(expectedFiles, lastlyImportedDefinitionFiles);
	}

	private void verifyTemplatesImported(String... fileNames) {
		Set<String> expectedFiles = new HashSet<>(Arrays.asList(fileNames));
		assertEquals(expectedFiles, lastlyImportedTemplateFiles);
	}

	private void verifyBPMNsImported(String... fileNames) {
		Set<String> expectedFiles = new HashSet<>(Arrays.asList(fileNames));
		assertEquals(expectedFiles, lastlyImportedBPMNFiles);
	}

	private void verifyRolesImported(String... fileNames) {
		Set<String> expectedFiles = new HashSet<>(Arrays.asList(fileNames));
		assertEquals(expectedFiles, lastlyImportedPermissionFiles);
	}

	private void verifyErrorsDetected(List<String> errors, int numberOfExpectedErrors) {
		verifyInvalid(errors);
		assertEquals(numberOfExpectedErrors, errors.size());
	}

	private void verifyInvalid(List<String> errors) {
		assertTrue(errors.size() > 0);
		verify(definitionImportService, never()).importDefinitions(any());
		verify(templateImportService, never()).importTemplates(any());
		verify(bPMDefinitionImportService, never()).importDefinitions(any());
		verify(permissionsImportService, never()).importPermissions(any());
	}

	private void verifyTemplateIdsRequested(List<String> ids) {
		verify(templateImportService).exportTemplates(eq(ids));
	}

	private void verifyDefinitionIdsRequested(List<String> ids) {
		verify(definitionImportService).exportDefinitions(eq(ids));
	}

	private static void verifyFileExportedWithName(File file, String expectedName) {
		assertEquals("Exported file was not named as expected", expectedName, file.getName());
	}

	private static void verifyExactDirectoriesArePresent(File parent, String... dirNames) {
		Set<String> existingDirs = Stream
					.of(parent.listFiles())
					.filter(File::isDirectory)
					.map(File::getName)
					.collect(Collectors.toSet());
		assertEquals("The number of existing directories was not as expected", dirNames.length, existingDirs.size());
		assertEquals(new HashSet<>(Arrays.asList(dirNames)), existingDirs);
	}

	private static void verifyNoDirectoriesArePresent(File parent) {
		Set<File> existingDirs = Stream
								.of(parent.listFiles())
								.filter(File::isDirectory)
								.collect(Collectors.toSet());
		assertTrue("Exported directories were detected. There shouldn't be.", existingDirs.isEmpty());
	}

	private static void verifyXmlFileExported(File expected, File actual) throws IOException {
		assertTrue("Exported file was not xml", actual.getName().toLowerCase().endsWith(".xml"));
		assertEquals(expected.getName(), actual.getName());

		String fileContentActual = FileUtils.readFileToString(actual);
		String fileContentExpected = FileUtils.readFileToString(expected);
		assertEquals(fileContentExpected, fileContentActual);
	}

	private static void verifyDirContainsFiles(File dir, List<File> files) throws IOException {
		Map<String, File> actualFiles = Stream
					.of(dir.listFiles())
					.filter(File::isFile)
					.collect(CollectionUtils.toIdentityMap(File::getName));

		assertEquals("The number of exported files in directory " + dir.getName() + " was not as expected.",
				files.size(),
				actualFiles.size());

		for (File fileExpected : files) {
			File existing = actualFiles.get(fileExpected.getName());
			assertNotNull("Expected file with name " + fileExpected.getName() + " was not found in the exported files",
					existing);
			String fileContentActual = FileUtils.readFileToString(existing);
			String fileContentExpected = FileUtils.readFileToString(fileExpected);

			assertEquals("Exported file " + fileExpected.getName() + " didn't have the expected xml content.",
					fileContentExpected,
					fileContentActual);
		}
	}

	private List<File> withExistingDefinitions(String... fileNames) {
		if (fileNames.length == 0) {
			when(definitionImportService.exportAllDefinitions()).thenReturn(Collections.emptyList());
			when(definitionImportService.exportDefinitions(anyList())).thenReturn(Collections.emptyList());
			return Collections.emptyList();
		}

		List<File> existingFiles = Stream
					.of(fileNames)
					.map(ModelImportServiceImplTest::loadFromResourceFolder)
					.collect(Collectors.toList());

		when(definitionImportService.exportAllDefinitions()).thenReturn(existingFiles);
		when(definitionImportService.exportDefinitions(anyList())).thenReturn(existingFiles);
		return existingFiles;
	}

	private List<File> withExistingTemplates(String... fileNames) {
		if (fileNames.length == 0) {
			when(templateImportService.exportAllTemplates()).thenReturn(Collections.emptyList());
			when(templateImportService.exportTemplates(anyList())).thenReturn(Collections.emptyList());
			return Collections.emptyList();
		}

		List<File> existingFiles = Stream
				.of(fileNames)
					.map(ModelImportServiceImplTest::loadFromResourceFolder)
					.collect(Collectors.toList());

		when(templateImportService.exportAllTemplates()).thenReturn(existingFiles);
		when(templateImportService.exportTemplates(anyList())).thenReturn(existingFiles);
		return existingFiles;
	}

	private static File getSubDirectory(File parentDirectory, String directoryName) {
		return Arrays
				.asList(parentDirectory.listFiles())
					.stream()
					.filter(File::isDirectory)
					.filter(directory -> directoryName.equals(directory.getName()))
					.findFirst()
					.orElse(null);
	}

	private void withFiles(String... fileNames) {
		for (String fileName : fileNames) {
			File file = loadFromResourceFolder(fileName);
			try {
				importMapping.put(fileName, FileUtils.openInputStream(file));
			} catch (IOException e) {
				throw new EmfApplicationException("Failed to open stream to file " + fileName, e);
			}
		}
		captureFilesPassedForImport();
	}

	private static File loadFromResourceFolder(String fileName) {
		return new File(ModelImportServiceImplTest.class
				.getClassLoader()
					.getResource(
							ModelImportServiceImplTest.class.getPackage().getName().replace('.', '/') + "/" + fileName)
					.getFile());
	}

	/**
	 * Since the temp import directories are deleted after execution of import, it is required to check their contents
	 * at an earlier moment, before being deleted. This method captures the invocation of each import service and at
	 * that moment, reads the contents of the passed directory and writes it as a Set - as a private field in this test
	 * class so that it is preserved and asserted later.
	 */
	private void captureFilesPassedForImport() {
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				Path path = (Path) invocation.getArguments()[0];
				lastlyImportedDefinitionFiles = readFileNamesAsSet(path.toString());
				return null;
			}
		}).when(definitionImportService).importDefinitions(any());

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				String path = (String) invocation.getArguments()[0];
				lastlyImportedTemplateFiles = readFileNamesAsSet(path);
				return null;
			}
		}).when(templateImportService).importTemplates(any());

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				String path = (String) invocation.getArguments()[0];
				lastlyImportedPermissionFiles = readFileNamesAsSet(path);
				return null;
			}
		}).when(permissionsImportService).importPermissions(any());

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				String path = (String) invocation.getArguments()[0];
				lastlyImportedBPMNFiles = readFileNamesAsSet(path);
				return null;
			}
		}).when(bPMDefinitionImportService).importDefinitions(any());
	}

	private static Set<String> readFileNamesAsSet(String directoryPath) {
		return FileUtil
				.loadFromPath(directoryPath)
				.stream()
				.map(File::getName)
				.collect(Collectors.toSet());
	}

	private void verifyModelsImportCompleteEventIsFired() {
		verify(eventService).fire(Matchers.any(ModelImportCompleted.class));
	}

	private void verifyModelsImportCompleteEventIsNotFired() {
		verify(eventService, Mockito.never()).fire(Matchers.any(ModelImportCompleted.class));
	}
}
