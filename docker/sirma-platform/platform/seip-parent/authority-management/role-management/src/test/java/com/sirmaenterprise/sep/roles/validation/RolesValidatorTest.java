package com.sirmaenterprise.sep.roles.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirmaenterprise.sep.roles.provider.SecurityDozerProvider;

/**
 * Tests {@link RolesValidator}.
 * 
 * @author Vilizar Tsonev
 */
public class RolesValidatorTest {
	
	@Spy
	private ObjectMapper mapper = new DozerObjectMapper(
			new DozerBeanMapper(Collections.singletonList(SecurityDozerProvider.DOZER_SECURITY_MAPPING_XML)));

	@InjectMocks
	private RolesValidator rolesValidator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Require_DefinitionFiles_ForAllRoles() throws IOException {
		List<File> filesUnderTest = withTestFiles("validData/consumer.xml", "validData/contributor.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Allow_Valid_Definition_Files() throws IOException {
		List<File> filesUnderTest = withTestFiles("validData/consumer.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyValid(rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Allow_Definition_Files_With_Valid_Dependencies() throws IOException {
		List<File> filesUnderTest = withTestFiles("validData/dependecy1.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyValid(rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Allow_Definition_Files_With_Valid_Dependencies_Create_Permission() throws IOException {
		List<File> filesUnderTest = withTestFiles("validData/dependecy2.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyValid(rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Allow_Definition_File_With_Valid_Dependencies() throws IOException {
		List<File> filesUnderTest = withTestFiles("validData/dependecy3.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyValid(rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Allow_Multiple_Definition_Files_With_Valid_Dependencies() throws IOException {
		List<File> filesUnderTest = withTestFiles("validData/dependecy3_part1.xml", "validData/dependecy3_part2.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyValid(rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_XSD_Errors_For_Malformed_Xml() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/malformed_tags.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Circular_Dependency_When_Role_Includes_Itself() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/circularDependecy1.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Circular_Dependency_Two_Roles_Include_Each_Other() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/circularDependecy2.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Circular_Dependency_Roles_Include_Each_Other_Multiple() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/circularDependecy4.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Circular_Dependency_In_Multiple_Files() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/circularDependecy5_part1.xml",
				"invalidData/circularDependecy5_part2.xml", "validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Missing_Dependency_Single_Role() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/missingDependecy1.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Missing_Dependency_Multiple_Roles() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/missingDependecy2.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Missing_Dependency_Short_Declaration() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/missingDependecy3.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Missing_Dependency_Create_Permission() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/missingDependecy4.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	@Test
	public void should_Detect_Missing_Dependency_Multiple() throws IOException {
		List<File> filesUnderTest = withTestFiles("invalidData/missingDependecy5.xml", "validData/contributor.xml",
				"validData/collaborator.xml", "validData/manager.xml");
		verifyErrorsDetected(1, rolesValidator.validate(filesUnderTest));
	}

	private static void verifyErrorsDetected(int errorsCount, List<String> errors) {
		assertEquals(errorsCount, errors.size());
	}

	private static void verifyValid(List<String> errors) {
		assertTrue(errors.isEmpty());
	}
	private static List<File> withTestFiles(String... filenames) throws IOException {
		List<File> files = new ArrayList<>();
		for (String fileName : filenames) {
			File file = new File(RolesValidatorTest.class
					.getClassLoader()
						.getResource(
								RolesValidatorTest.class.getPackage().getName().replace('.', '/') + "/" + fileName)
						.getFile());
			files.add(file);
		}
		return files;
	}
}
