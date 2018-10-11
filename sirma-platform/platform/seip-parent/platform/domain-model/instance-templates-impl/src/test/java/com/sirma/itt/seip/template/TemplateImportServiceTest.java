package com.sirma.itt.seip.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.matchers.CompareMatcher;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.dozer.TemplateDozerProvider;
import com.sirma.itt.seip.testutil.fakes.TempFileProviderFake;
import com.sirma.itt.seip.time.TimeTracker;


/**
 * Tests {@link TemplateImportServiceImpl}.
 *
 * @author Vilizar Tsonev
 */
public class TemplateImportServiceTest {

	@InjectMocks
	private TemplateImportServiceImpl templateImportServiceImpl;

	@Spy
	private TempFileProvider tempFileProvider;

	@Spy
	private ObjectMapper mapper = new DozerObjectMapper(
			new DozerBeanMapper(Collections.singletonList(TemplateDozerProvider.DOZER_TEMPLATE_MAPPING_XML)));

	@Mock
	private TemplateServiceImpl templateaService;

	@Mock
	private EventService eventService;

	@Mock
	private Statistics statistics;

	@Mock
	private TemplateDao templateDao;

	@Mock
	private TypeConverter typeConverter;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void should_Allow_Valid_Templates() throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyValid(templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Detect_Errors_In_Files_With_Invalid_Format() throws IOException {
		withLocalTemplateFiles("textile_text_format.txt", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(1, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Detect_Errors_In_Templates_Having_Ids_With_Invalid_Characters() throws IOException {
		withLocalTemplateFiles("template_id_invalid_characters.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(2, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Allow_Templates_To_Have_Ids_With_Unicode_Word_Characters() throws IOException {
		withLocalTemplateFiles("template_id_in_chinese.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyValid(templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Detect_Errors_In_Files_With_Malformed_Xml_Structure() throws IOException {
		withLocalTemplateFiles("helptemplate_missing_fields_tag.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(1, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Detect_Multiple_Errors_In_Files_With_Several_Missing_Fields() throws IOException {
		withLocalTemplateFiles("missing_title_content_and_purpose.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(3, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Detect_Errors_In_Files_With_Missing_Field() throws IOException {
		withLocalTemplateFiles("helptemplate_missing_title_field.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(1, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_DetectError_When_NewlyImported_Templates_Have_Duplicate_Ids() throws IOException {
		withLocalTemplateFiles("second_textilestemplate_valid.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		Template mailTemplate = new Template();
		mailTemplate.setId("email_workflow_complete");
		mailTemplate.setForType("emailTemplate");
		mailTemplate.setTitle("Workflow Completed");
		mailTemplate.setPrimary(false);
		mailTemplate.setContent("Email content");
		withExistingActiveTemplates(mailTemplate);

		verifyErrorsDetected(1, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Perform_Validation_Over_All_Existing_And_Newly_Imported_Templates() throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		Template help = new Template();
		help.setId("helptemplate");
		help.setTitle("Help Template");
		help.setForType("userHelp");
		help.setPurpose("creatable");
		help.setPrimary(true);
		help.setCorrespondingInstance("emf:6fbb45d7-28a0-43cd-95bc-563b556e5332");
		help.setRule("department == \"ENG\" && functional == \"EDG\" && filterCodelist == \"AD210001\"");
		help.setContent("Help template sample content");

		Template textile = new Template();
		textile.setId("basetextilestemplate");
		textile.setTitle("Base Textiles Template");
		textile.setForType("CO1002");
		textile.setPurpose("creatable");
		textile.setPrimary(true);
		textile.setCorrespondingInstance("emf:28e8ccca-386a-42d8-ba23-8d24bfcb515b");
		textile.setContent("Sample content");

		Template testTemplate = new Template();
		testTemplate.setId("test");
		testTemplate.setTitle("test");
		testTemplate.setForType("CO1002");
		testTemplate.setPurpose("creatable");
		testTemplate.setPrimary(false);
		testTemplate.setCorrespondingInstance("emf:28e8ccca-386a-42d8-ba23-8d24bfcb5000");
		testTemplate.setContent("Test content");

		// if the templates for import already exist, they should just be overwritten and should not concern the
		// validation
		withExistingActiveTemplates(help, textile, testTemplate);

		verifyValid(templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_NotAllowImportingNewTemplate_When_PrimaryTemplateForTheSameGroupAlreadyExists()
			throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");

		// there is existing in the RDB template for the same type/purpose/rule. So after the merge, error should be
		// detected
		Template anotherHelpTemplate = new Template();
		anotherHelpTemplate.setId("helptemplate2");
		anotherHelpTemplate.setTitle("Help Template2");
		anotherHelpTemplate.setForType("userHelp");
		anotherHelpTemplate.setPurpose("creatable");
		anotherHelpTemplate.setPrimary(true);
		anotherHelpTemplate.setRule("department == \"ENG\" && functional == \"EDG\" && filterCodelist == \"AD210001\"");
		anotherHelpTemplate.setContent("Help template sample content");
		withExistingActiveTemplates(anotherHelpTemplate);

		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(1, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_NotAllowImportingNewTemplate_When_TemplateForTheSameModelingInstanceAlreadyExists()
			throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");

		// there is existing in the RDB template for the same type/purpose/rule. So after the merge, error should be
		// detected
		Template anotherHelpTemplate = new Template();
		anotherHelpTemplate.setId("helptemplate2");
		anotherHelpTemplate.setTitle("Help Template2");
		anotherHelpTemplate.setForType("userHelp");
		anotherHelpTemplate.setPurpose("creatable");
		anotherHelpTemplate.setPrimary(false);
		anotherHelpTemplate.setCorrespondingInstance("emf:6fbb45d7-28a0-43cd-95bc-563b556e5332");
		anotherHelpTemplate.setRule("department == \"ENG\" && functional == \"EDG\" && filterCodelist == \"AD210001\"");
		anotherHelpTemplate.setContent("Help template sample content");
		withExistingActiveTemplates(anotherHelpTemplate);

		String folderPath = tempFolder.getRoot().getAbsolutePath();

		verifyErrorsDetected(1, templateImportServiceImpl.validate(folderPath));
	}

	@Test
	public void should_Correctly_Import_Templates_From_The_File_System() throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getRoot().getAbsolutePath();

		Template help = new Template();
		help.setId("helptemplate");
		help.setTitle("Help Template");
		help.setForType("userHelp");
		help.setPurpose("creatable");
		help.setPrimary(true);
		help.setCorrespondingInstance("emf:6fbb45d7-28a0-43cd-95bc-563b556e5332");
		help.setRule("department == \"ENG\" && functional == \"EDG\" && filterCodelist == \"AD210001\"");
		help.setContent("Help template sample content");

		Template textile = new Template();
		textile.setId("basetextilestemplate");
		textile.setTitle("Base Textiles Template");
		textile.setForType("CO1002");
		textile.setPurpose("creatable");
		textile.setPrimary(true);
		textile.setCorrespondingInstance("emf:28e8ccca-386a-42d8-ba23-8d24bfcb515b");
		textile.setContent("Sample content");

		templateImportServiceImpl.importTemplates(folderPath);

		verifyTemplatesSaved(Arrays.asList(help, textile));
	}

	@Test
	public void should_Export_All_Templates() throws IOException {
		Template help = new Template();
		help.setId("helptemplate");
		help.setTitle("Help Template");
		help.setForType("userHelp");
		help.setPurpose("creatable");
		help.setPrimary(true);
		help.setCorrespondingInstance("emf:6fbb45d7-28a0-43cd-95bc-563b556e5332");
		help.setRule("department == \"ENG\" && functional == \"EDG\" && filterCodelist == \"AD210001\"");
		help.setContent("Help template sample content");

		Template textile = new Template();
		textile.setId("basetextilestemplate");
		textile.setTitle("Base Textiles Template");
		textile.setForType("CO1002");
		textile.setPurpose("creatable");
		textile.setPrimary(true);
		textile.setCorrespondingInstance("emf:28e8ccca-386a-42d8-ba23-8d24bfcb515b");
		textile.setContent("Sample content");

		Template mailTemplate = new Template();
		mailTemplate.setId("email_workflow_complete");
		mailTemplate.setForType("emailTemplate");
		mailTemplate.setTitle("Workflow Completed");
		mailTemplate.setPrimary(false);
		mailTemplate.setContent("Email content");

		withExistingActiveTemplates(help, textile, mailTemplate);

		List<File> resultFiles = templateImportServiceImpl.exportAllTemplates();

		verifyFilesExported(resultFiles, "helptemplate_valid.xml", "textilestemplate_valid.xml",
				"email_workflow_complete.xml");
	}

	@Test
	public void should_Export_Requested_Templates() throws IOException {
		Template help = new Template();
		help.setId("helptemplate");

		Template randomTemplate = new Template();
		randomTemplate.setId("randomTemplate");

		Template textile = new Template();
		textile.setId("basetextilestemplate");
		textile.setTitle("Base Textiles Template");
		textile.setForType("CO1002");
		textile.setPurpose("creatable");
		textile.setPrimary(true);
		textile.setCorrespondingInstance("emf:28e8ccca-386a-42d8-ba23-8d24bfcb515b");
		textile.setContent("Sample content");

		Template mailTemplate = new Template();
		mailTemplate.setId("email_workflow_complete");
		mailTemplate.setForType("emailTemplate");
		mailTemplate.setTitle("Workflow Completed");
		mailTemplate.setPrimary(false);
		mailTemplate.setContent("Email content");

		withExistingActiveTemplates(help, textile, mailTemplate, randomTemplate);

		List<File> resultFiles = templateImportServiceImpl
				.exportTemplates(Arrays.asList("basetextilestemplate", "email_workflow_complete"));

		verifyFilesExported(resultFiles, "textilestemplate_valid.xml", "email_workflow_complete.xml");
	}

	@Test
	public void should_ExportTemplates_With_FileNames_Corresponding_To_Their_Ids() throws IOException {
		Template help = new Template();
		help.setId("helptemplate");

		Template textile = new Template();
		textile.setId("basetextilestemplate");

		Template testTemplate = new Template();
		testTemplate.setId("testtemplate123");
		withExistingActiveTemplates(help, textile, testTemplate);

		verifyFileNamesExported(templateImportServiceImpl.exportAllTemplates(), "helptemplate.xml",
				"basetextilestemplate.xml", "testtemplate123.xml");
	}

	@Before
	public void init() {
		tempFileProvider = new TempFileProviderFake(tempFolder.getRoot());
		MockitoAnnotations.initMocks(this);
		when(statistics.createTimeStatistics(any(), anyString())).thenReturn(TimeTracker.createAndStart());
		when(typeConverter.convert(eq(String.class), any(Object.class))).then(a -> {
			Object arg = a.getArgumentAt(1, Object.class);
			if (arg == null) {
				return arg;
			}
			return arg.toString();
		});
	}

	private static void verifyFilesExported(List<File> actual, String... expectedFileNames) throws IOException {
		List<File> expectedFiles = Stream
					.of(expectedFileNames)
					.map(fileName -> loadFile(fileName))
					.collect(Collectors.toList());
		assertEquals("The number of exported template files is different than expected", expectedFiles.size(),
				actual.size());

		for (int i = 0; i < actual.size(); i++) {
			String fileContentActual = FileUtils.readFileToString(actual.get(i));
			String fileContentExpected = FileUtils.readFileToString(expectedFiles.get(i));
			assertThat(fileContentActual,
					CompareMatcher
							.isSimilarTo(fileContentExpected)
								.ignoreWhitespace()
								.normalizeWhitespace()
								.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes)));
		}
	}

	private static void verifyFileNamesExported(List<File> actual, String... expectedFileNames) throws IOException {
		Set<String> actualFileNames = actual
				.stream()
				.map(File::getName)
				.collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList(expectedFileNames)), actualFileNames);
	}

	private void verifyTemplatesSaved(List<Template> expectedTemplates) {
		ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
		verify(templateaService, times(expectedTemplates.size())).saveOrUpdateImportedTemplate(captor.capture());

		List<Template> actualSavedTemplates = captor.getAllValues();

		assertTrue(expectedTemplates.containsAll(actualSavedTemplates));
	}

	private static void verifyErrorsDetected(int errorsCount, List<String> errors) {
		assertEquals(errorsCount, errors.size());
	}

	private static void verifyValid(List<String> errors) {
		assertTrue(errors.isEmpty());
	}

	private void withExistingActiveTemplates(Template... templates) {
		when(templateDao.getAllTemplates()).thenReturn(Arrays.asList(templates));
		for (Template template : templates) {
			when(templateaService.getTemplate(eq(template.getId()))).thenReturn(template);
			when(templateaService.getContent(eq(template.getId()))).thenReturn(template.getContent());
		}
	}

	private void withLocalTemplateFiles(String fileName1, String fileName2) throws IOException {
		if (StringUtils.isNotBlank(fileName1)) {
			createTempFileFrom(fileName1);
		}
		if (StringUtils.isNotBlank(fileName2)) {
			createTempFileFrom(fileName2);
		}
	}

	private static File loadFile(String fileName) {
		return new File(TemplateImportServiceTest.class
				.getClassLoader()
					.getResource(
							TemplateImportServiceTest.class.getPackage().getName().replace('.', '/') + "/" + fileName)
					.getFile());
	}

	private File createTempFileFrom(String fileName) throws IOException {
		File originalFile = new File(TemplateImportServiceTest.class
				.getClassLoader()
					.getResource(TemplateImportServiceTest.class.getPackage().getName().replace('.', '/') + "/"
							+ fileName)
					.getFile());

		// since the TemplateImportService cleans up the files after use, create temporary files for the test
		File temporaryTestFile = tempFolder.newFile(fileName);
		FileUtils.copyFile(originalFile, temporaryTestFile);
		return temporaryTestFile;
	}
}
