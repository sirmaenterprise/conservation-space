/*
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.services.DmsInstanceAlfresco4Service;
import com.sirma.itt.cmf.beans.InputStreamFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.remote.DMSClientException;

/**
 * Test uploading by services of files. Base testing of {@link CMFDocumentAdapterService} impl
 *
 * @author bbanchev
 */
@Test
public class DocumentAlfresco4ServiceCITest extends BaseAlfrescoTest {

	/** The document adapter. */
	private CMFDocumentAdapterService documentAdapter;

	/** The uploaded doc. */
	private DocumentInstance uploadedDoc;

	/** The temp working folder. */
	private String tempWorkingFolder;

	/**
	 * {@inheritDoc}
	 */
	@BeforeClass
	@Override
	protected void setUp() {
		super.setUp();
		documentAdapter = mockupProvider.mockupDocumentAdapter();
		tempWorkingFolder = createTestFolder(baseUploadPath);
		cleanUpGlobalIds.add(tempWorkingFolder);
	}

	/**
	 * Initial upload of file test.
	 */
	@Test(enabled = true)
	public void uploadContent() {
		try {

			SectionInstance owning = new SectionInstance();
			owning.setDmsId(tempWorkingFolder);

			DocumentInstance documentInstance = mockupProvider.createDocument(owning, false,
					"test.txt");
			Map<String, Serializable> metadataToInclude = documentInstance.getProperties();

			Serializable setTitle = metadataToInclude.get(DocumentProperties.TITLE);
			// metadataToInclude.put("cm:" + DocumentProperties.TITLE, setTitle);
			FileAndPropertiesDescriptor uploadContent = documentAdapter.uploadContent(
					documentInstance, getSimpleFileDescriptor("test.txt", metadataToInclude),
					Collections.singleton(DocumentProperties.TYPE_DOCUMENT_STRUCTURED));
			Map<String, Serializable> properties = uploadContent.getProperties();
			assertNotNull(uploadContent, "Descriptor for uploaded file should not be null!");
			assertNotNull(uploadContent.getProperties(),
					"Descriptor properties for uploaded file should not be null!");
			assertEquals(properties.get(DocumentProperties.VERSION), "1.0", "Version should be set");
			assertEquals(properties.get(DocumentProperties.TITLE), setTitle, "Title should be set");
			documentInstance.setDmsId(uploadContent.getId());
			uploadedDoc = documentInstance;
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	/**
	 * Mockup a simple upload descriptor for file from the {@link DmsInstanceAlfresco4Service} path.
	 *
	 * @param name
	 *            is the file to look for
	 * @param metadataToInclude
	 *            is the data to include
	 * @return the file descriptor
	 */
	private UploadWrapperDescriptor getSimpleFileDescriptor(String name,
			Map<String, Serializable> metadataToInclude) {
		return new UploadWrapperDescriptor(new InputStreamFileDescriptor(name,
				DmsInstanceAlfresco4Service.class.getResourceAsStream(name)), tempWorkingFolder,
				metadataToInclude);
	}

	/**
	 * Test upload new version of {@link #uploadedDoc}.
	 */
	@Test(enabled = true, dependsOnMethods = "uploadContent")
	public void uploadNewVersion() {
		try {
			// based on uploaded document
			Map<String, Serializable> metadataToInclude = uploadedDoc.getProperties();
			String newDescription = "newDescription";
			metadataToInclude.put(DocumentProperties.DESCRIPTION, newDescription);
			metadataToInclude.put(DocumentProperties.IS_MAJOR_VERSION, Boolean.TRUE);
			metadataToInclude.put(DocumentProperties.VERSION_DESCRIPTION, "test update");
			FileAndPropertiesDescriptor uploadNewVersion = documentAdapter.uploadNewVersion(
					uploadedDoc, getSimpleFileDescriptor("test.xml", metadataToInclude));
			Assert.assertNotNull(uploadNewVersion,
					"Descriptor for uploaded file should not be null!");
			Map<String, Serializable> properties = uploadNewVersion.getProperties();
			assertNotNull(properties, "Description should be set");
			assertEquals(properties.get(DocumentProperties.NAME), "test.xml",
					"Name should be updated");
			assertEquals(properties.get(DocumentProperties.VERSION), "2.0", "Version should be set");
		} catch (DMSException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	/**
	 * Tests the revert operation of document.
	 */
	@Test(enabled = true, dependsOnMethods = { "uploadNewVersion", "getDocumentDirectAccessURI" })
	public void revert() {
		try {
			uploadedDoc.getProperties().put(DocumentProperties.IS_MAJOR_VERSION, Boolean.TRUE);
			uploadedDoc.getProperties()
					.put(DocumentProperties.VERSION_DESCRIPTION, "Revert to 1.0");
			String oldDmsId = uploadedDoc.getDmsId();
			String documentDirectAccessURI = documentAdapter
					.getDocumentDirectAccessURI(uploadedDoc);
			assertNotNull(documentDirectAccessURI, "Access url should not be null");
			DocumentInstance revertVersion = documentAdapter.revertVersion(uploadedDoc, "1.0");
			assertEquals(revertVersion.getDmsId(), oldDmsId, "DMS Id should be the same");
			assertEquals(revertVersion.getProperties().get(DocumentProperties.VERSION), "3.0",
					"Version should be 3.0");
			assertEquals(revertVersion.getProperties().get(DocumentProperties.TITLE),
					"Title for (test.txt)", "Title as created");
			assertEquals(revertVersion.getProperties().get(DocumentProperties.NAME), "test.txt",
					"File name of fist version");
			documentDirectAccessURI = documentAdapter.getDocumentDirectAccessURI(revertVersion);
			assertNotNull(documentDirectAccessURI, "Access url should not be null for reverted");
			checkPreview(documentDirectAccessURI);
		} catch (Exception e) {
			fail(e);
		}

	}

	/**
	 * Tests the url access to the uploaded document.
	 */
	@Test(enabled = true, dependsOnMethods = "uploadContent")
	public void getDocumentDirectAccessURI() {
		try {
			String documentDirectAccessURI = documentAdapter
					.getDocumentDirectAccessURI(uploadedDoc);
			assertNotNull(documentDirectAccessURI, "Access url should not be null");
			logger.debug(documentDirectAccessURI);
			Assert.assertTrue(
					documentDirectAccessURI.contains(uploadedDoc.getDmsId().replace(":/", "")),
					"URL should contain the id of the uploaded document");
			checkPreview(documentDirectAccessURI);
		} catch (Exception e) {
			fail(e);
		}

	}

	/**
	 * Check preview by http request
	 *
	 * @param documentDirectAccessURI
	 *            the document direct access uri
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	private void checkPreview(String documentDirectAccessURI) throws DMSClientException {
		HttpMethod rawRequest = httpClient.rawRequest(new GetMethod(),
				documentDirectAccessURI.replace("/document/access", ""));
		assertEquals(rawRequest.getStatusCode(), 200, "Should be accessible!");
	}
}
