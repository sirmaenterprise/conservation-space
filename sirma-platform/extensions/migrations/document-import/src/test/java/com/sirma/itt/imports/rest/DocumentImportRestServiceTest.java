package com.sirma.itt.imports.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.remote.AlfrescoRESTClient;
import com.sirma.itt.cmf.alfresco4.remote.UnicodeFilePart;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class DocumentImportRestServiceTest.
 *
 * @author BBonev
 */
// @Test
public class DocumentImportRestServiceTest extends EmfTest {

	/**
	 * Test calls.
	 *
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test(enabled = false)
	public void testCalls() throws DMSClientException, FileNotFoundException {
		AlfrescoRESTClient restClient = new AlfrescoRESTClient();
		restClient.setUseServicePath(false);
		restClient.setDefaultCredentials("localhost", 8080, "admin", "admin");

		List<Part> formData = new ArrayList<Part>(2);
		formData.add(new UnicodeFilePart("csv", new File(
				"src/test/resources/com/sirma/itt/imports/ECN25685.html")));
		formData.add(new UnicodeFilePart("htm", new File(
				"src/test/resources/com/sirma/itt/imports/Tr_ECN25685.csv")));

		HttpMethod method = restClient.createMethod(new PostMethod(),
				formData.toArray(new Part[2]), true);
		restClient.request("/emf/service/document-import/importDocument/", method);
	}

	/**
	 * Test calls.
	 *
	 * @param csvFile
	 *            the csv file
	 * @param htmFile
	 *            the htm file
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test(dataProvider = "sourceFiles", enabled = false)
	public void testTransformationCalls(String csvFile, String htmFile) throws DMSClientException,
			FileNotFoundException {
		AlfrescoRESTClient restClient = new AlfrescoRESTClient();
		restClient.setUseServicePath(false);
		restClient.setDefaultCredentials("localhost", 8080, "admin", "admin");

		List<Part> formData = new LinkedList<Part>();
		formData.add(new UnicodeFilePart("htm", new File(
				"src/test/resources/com/sirma/itt/imports/" + htmFile)));
		formData.add(new UnicodeFilePart("csv", new File(
				"src/test/resources/com/sirma/itt/imports/" + csvFile)));


		HttpMethod method = restClient.createMethod(new PostMethod(),
				formData.toArray(new Part[2]), true);
		InputStream responseStream = restClient.request(method,
				"/emf/service/document-import/transformDocument");
		StringWriter writer = null;
		BufferedReader reader = null;
		try {

			InputStream inputStream = method.getResponseBodyAsStream();

			Assert.assertNotNull(inputStream);

			reader = new BufferedReader(new InputStreamReader(responseStream, "utf-8"));

			if (inputStream.available() != -1) {
				writer = new StringWriter(inputStream.available());
			} else {
				writer = new StringWriter();
			}

			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write("\n");
			}

			Assert.assertFalse(writer.toString().isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (responseStream != null) {
				try {
					responseStream.close();
				} catch (IOException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Provide file names.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "sourceFiles")
	public Object[][] provideFileNames() {
		return new Object[][] {/* { "ECN25693.csv", "ECN25693.htm" },
				{ "ECN25321.csv", "ECN25321.htm" }, { "ECN25320.csv", "ECN25320.htm" },*/
				/*{ "ECN23717-1.csv", "ECN23717-1.htm" }, { "ECN23743-1.csv", "ECN23743-1.htm" },
				{ "ECN23752-1.csv", "ECN23752-1.htm" }, { "ECN23713.csv", "ECN23713.htm" },*/{ "b/ECN25350.csv", "b/ECN25350.htm" } };
	}

}
