package com.sirma.itt.seip.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.matchers.CompareMatcher;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.dozer.TemplateDozerProvider;
import com.sirma.itt.seip.testutil.fakes.TempFileProviderFake;

/**
 * Tests {@link TemplateImportServiceImpl}.
 *
 * @author Vilizar Tsonev
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ TemplateImportServiceImpl.class, TemplateDozerProvider.class })
@AdditionalPackages({ TypeConverter.class })
@AdditionalClasspaths({ ObjectMapper.class, Extension.class, EventService.class })
public class TemplateImportServiceTest {

	@Produces
	private Statistics statistics = NoOpStatistics.INSTANCE;

	@Produces
	private TempFileProvider tempFileProvider = new TempFileProviderFake();

	@Produces
	@Mock
	private CodelistService codelistService;

	@Produces
	@Mock
	private TemplateServiceImpl templateService;

	@Produces
	@Mock
	private TemplateDao templateDao;

	@Produces
	@Mock
	private SystemConfiguration systemConfiguration;

	private File tempFolder;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private DefaultTypeConverter defaultTypeConverter;

	@Inject
	private TemplateImportServiceImpl templateImportService;

	private List<GenericDefinition> availableDefinitions;

	@Test
	public void should_Allow_Valid_Templates() throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");
		verifyValid();
	}

	@Test
	public void should_Detect_Errors_In_Files_With_Invalid_Format() throws IOException {
		withLocalTemplateFiles("textile_text_format.txt", "textilestemplate_valid.xml");
		verifyErrorsDetected(1);
	}

	@Test
	public void should_Detect_Errors_In_Templates_Having_Ids_With_Invalid_Characters() throws IOException {
		withLocalTemplateFiles("template_id_invalid_characters.xml", "textilestemplate_valid.xml");
		verifyErrorsDetected(2);
	}

	@Test
	public void should_Allow_Templates_To_Have_Ids_With_Unicode_Word_Characters() throws IOException {
		withLocalTemplateFiles("template_id_in_chinese.xml", "textilestemplate_valid.xml");
		verifyValid();
	}

	@Test
	public void should_Detect_Errors_In_Files_With_Malformed_Xml_Structure() throws IOException {
		withLocalTemplateFiles("helptemplate_missing_fields_tag.xml", "textilestemplate_valid.xml");
		verifyErrorsDetected(1);
	}

	@Test
	public void should_Detect_Multiple_Errors_In_Files_With_Several_Missing_Fields() throws IOException {
		withLocalTemplateFiles("missing_title_content_and_purpose.xml", "textilestemplate_valid.xml");
		verifyErrorsDetected(3);
	}

	@Test
	public void should_Detect_Errors_In_Files_With_Missing_Field() throws IOException {
		withLocalTemplateFiles("helptemplate_missing_title_field.xml", "textilestemplate_valid.xml");
		verifyErrorsDetected(1);
	}

	@Test
	public void should_DetectError_When_NewlyImported_Templates_Have_Duplicate_Ids() throws IOException {
		withLocalTemplateFiles("second_textilestemplate_valid.xml", "textilestemplate_valid.xml");

		Template mailTemplate = new Template();
		mailTemplate.setId("email_workflow_complete");
		mailTemplate.setForType("emailTemplate");
		mailTemplate.setTitle("Workflow Completed");
		mailTemplate.setPrimary(false);
		mailTemplate.setContent("Email content");
		withExistingActiveTemplates(mailTemplate);

		verifyErrorsDetected(1);
	}

	@Test
	public void should_Perform_Validation_Over_All_Existing_And_Newly_Imported_Templates() throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");

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

		verifyValid();
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

		verifyErrorsDetected(1);
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

		verifyErrorsDetected(1);
	}

	@Test
	public void should_Correctly_Import_Templates_From_The_File_System() throws IOException {
		withLocalTemplateFiles("helptemplate_valid.xml", "textilestemplate_valid.xml");
		String folderPath = tempFolder.getAbsolutePath();

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

		templateImportService.importTemplates(folderPath);

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

		List<File> resultFiles = templateImportService.exportAllTemplates();

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

		List<File> resultFiles = templateImportService
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

		verifyFileNamesExported(templateImportService.exportAllTemplates(), "helptemplate.xml",
				"basetextilestemplate.xml", "testtemplate123.xml");
	}

	@Test
	public void shouldValidateMissingDefinition() throws IOException {
		withLocalTemplateFiles("missing_definition.xml", null);
		verifyErrorsDetected(1);
	}

	@Test
	public void shouldValidateTemplateRules_missingRuleField() throws IOException {
		withLocalTemplateFiles("helptemplate_missing_rule_field.xml", null);
		verifyErrorsDetected(1);
	}

	@Test
	public void shouldValidateTemplateRules_wrongFieldType() throws IOException {
		withLocalTemplateFiles("helptemplate_rule_field_wrong_type.xml", null);
		verifyErrorsDetected(1);
	}

	@Test
	public void shouldValidateTemplateRules_missingRuleValues() throws IOException {
		withLocalTemplateFiles("helptemplate_missing_rule_values.xml", null);
		// Single & multi valued rules
		verifyErrorsDetected(3);
	}

	@Before
	public void init() throws IOException {
		tempFolder = Files.createTempDirectory("template-tests").toFile();
		((TempFileProviderFake) tempFileProvider).setTempDir(tempFolder);
		defaultTypeConverter.register(typeConverter);
		availableDefinitions = new LinkedList<>();

		withDefinition("userHelp", Arrays.asList(
				getField("department", 1),
				getField("functional", 2),
				getField("filterCodelist", 3),
				getField("dateField", DataTypeDefinition.DATE)));
		withDefinition("CO1002", Collections.emptyList());

		withCodeValues(1, "ENG").withCodeValues(2, "EDG").withCodeValues(3, "AD210001");
	}

	@After
	public void destroy() {
		FileUtils.deleteQuietly(tempFolder);
	}

	private TemplateValidationRequest getRequest(String path) {
		return new TemplateValidationRequest(path, availableDefinitions);
	}

	private static WritablePropertyDefinition getField(String name, Integer clId) {
		WritablePropertyDefinition field = new FieldDefinitionImpl();
		field.setName(name);
		field.setCodelist(clId);
		return field;
	}

	private static WritablePropertyDefinition getField(String name, String type) {
		WritablePropertyDefinition field = new FieldDefinitionImpl();
		field.setName(name);
		field.setType(type);
		return field;
	}

	private TemplateImportServiceTest withCodeValues(Integer clId, String... values) {
		Arrays.asList(values).forEach(value -> {
			CodeValue codeValue = new CodeValue();
			codeValue.setValue(value);
			codeValue.setCodelist(clId);
			when(codelistService.getCodeValue(eq(clId), eq(value))).thenReturn(codeValue);
		});
		return this;
	}

	private void withDefinition(String identifier, List<PropertyDefinition> fields) {
		GenericDefinition definition = new GenericDefinitionImpl();
		definition.setIdentifier(identifier);
		definition.getFields().addAll(fields);
		availableDefinitions.add(definition);
	}

	private static void verifyFilesExported(List<File> actual, String... expectedFileNames) throws IOException {
		List<File> expectedFiles = Stream
				.of(expectedFileNames)
				.map(TemplateImportServiceTest::loadFile)
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
		verify(templateService, times(expectedTemplates.size())).saveOrUpdateImportedTemplate(captor.capture());

		List<Template> actualSavedTemplates = captor.getAllValues();

		assertTrue(expectedTemplates.containsAll(actualSavedTemplates));
	}

	private void verifyErrorsDetected(int errorsCount) {
		verifyErrorsDetected(errorsCount, templateImportService.validate(getRequest(tempFolder.getAbsolutePath())));
	}

	private static void verifyErrorsDetected(int errorsCount, List<String> errors) {
		assertEquals(errorsCount, errors.size());
	}

	private void verifyValid() {
		verifyValid(templateImportService.validate(getRequest(tempFolder.getAbsolutePath())));
	}

	private static void verifyValid(List<String> errors) {
		assertTrue(errors.isEmpty());
	}

	private void withExistingActiveTemplates(Template... templates) {
		when(templateDao.getAllTemplates()).thenReturn(Arrays.asList(templates));
		for (Template template : templates) {
			when(templateService.getTemplate(eq(template.getId()))).thenReturn(template);
			when(templateService.getContent(eq(template.getId()))).thenReturn(template.getContent());
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
		File copy = new File(tempFolder.getPath(), fileName);
		FileUtils.copyFile(originalFile, copy);
		return copy;
	}
}
