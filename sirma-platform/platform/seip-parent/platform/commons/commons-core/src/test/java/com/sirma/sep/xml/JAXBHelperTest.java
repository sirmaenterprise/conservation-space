package com.sirma.sep.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Tests the functionality of {@link JAXBHelper}
 *
 * @author Vilizar Tsonev
 */
public class JAXBHelperTest {

	@Test
	public void should_IndicateWellFormedDefinitionAsCorrect() throws URISyntaxException {
		withXmlFile("valid_user.xml");
		verifyValid();
	}

	@Test
	public void should_Collect_Detected_Errors_In_A_List() throws URISyntaxException {
		withXmlFile("invalid_user.xml");
		List<Message> errors = new ArrayList<>();
		JAXBHelper.validateFile(testFile, USER, errors);
		assertEquals(1, errors.size());

		List<String> result = JAXBHelper.validateFile(testPath, USER);
		assertEquals(1, result.size());
	}

	@Test
	public void should_TransformXmlFileIntoJavaObject() throws URISyntaxException {
		withXmlFile("valid_user.xml");

		User user = JAXBHelper.load(testFile, User.class);
		assertEquals("Test", user.getName());

		user = JAXBHelper.load(testPath, User.class);
		assertEquals("Test", user.getName());
	}

	@Test
	public void should_ConvertObjectIntoXml() throws IOException, URISyntaxException {
		User user = new User();

		user.name = "Test";
		user.password = "pass";

		String result = JAXBHelper.toXml(user);

		withXmlFile("valid_user.xml");
		String expected = FileUtils.readFileToString(testFile);

		assertEquals(expected, result);
	}

	private void verifyValid() {
		assertTrue(JAXBHelper.validateFile(testFile, USER, CollectionUtils.emptyList()));

		assertTrue(JAXBHelper.validateFile(testPath, USER).isEmpty());
	}

	private void withXmlFile(String fileName) throws URISyntaxException {
		URL url = JAXBHelperTest.class.getResource(fileName);
		testFile = new File(url.toURI());
		testPath = testFile.toPath();
	}

	private File testFile;
	private Path testPath;

	public static final XmlSchemaProvider USER = XmlSchemaProvider.create("user",
			SchemaInstance.fromResource(JAXBHelperTest.class, "user.xsd"));

	@XmlRootElement
	public static class User {

		private String name;

		private String password;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

}
