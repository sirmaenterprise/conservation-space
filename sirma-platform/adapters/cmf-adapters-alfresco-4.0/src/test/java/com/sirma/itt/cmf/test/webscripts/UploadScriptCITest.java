package com.sirma.itt.cmf.test.webscripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.remote.ContentUpdateContext;
import com.sirma.itt.cmf.alfresco4.remote.ContentUploadContext;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;

/**
 * Tests uploading script.
 */
@Test
public class UploadScriptCITest extends BaseAlfrescoTest {
	/** The Constant TEST_FILE. */
	private static final String TEST_FILE = "src/test/resources/test.txt";

	/** The alfresco uploader. */
	private AlfrescoUploader alfrescoUploader;
	private static final String THUMB_MODE_GENERATION = ThumbnailGenerationMode.ASYNCH.toString();
	private static final UploadMode UPLOAD_MODE = UploadMode.DIRECT;

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.test.BaseAlfrescoTest#setUp()
	 */
	@Override
	@BeforeClass
	public void setUp() {
		super.setUp();
		alfrescoUploader = mockupProvider.mockupAlfrescoUploader();
	}

	/**
	 * Test by id.
	 */
	@Test(enabled = false)
	public void testContinuesly() {
		Set<String> aspectsProp = new HashSet<>();
		Map<String, Serializable> props = new HashMap<>();
		String uploadFile;
		try {
			String parentId = getTestParentId(baseUploadPath);
			props.put("cm:description", "test file");
			props.put("cm:title", "set by test");
			uploadFile = alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UPLOAD_MODE));
			// test by upload
			Assert.assertNotNull(uploadFile, "Result should be json object");
			Assert.assertEquals("1.0", getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_VERSION));
			uploadFile = alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UPLOAD_MODE));
			Assert.assertNotNull(uploadFile, "Result should be json object");
			Assert.assertEquals("2.0", getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_VERSION));
			uploadFile = alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UPLOAD_MODE));
			Assert.assertNotNull(uploadFile, "Result should be json object");
			Assert.assertEquals("2.1", getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_VERSION));

			String node = getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_NODEREF);

			uploadFile = alfrescoUploader.updateFile(ContentUpdateContext.create("serviceUrl", "dmsId"));
			Assert.assertNotNull(uploadFile, "Result should be json object");
			Assert.assertEquals("3.0", getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_VERSION));
			uploadFile = alfrescoUploader.updateFile(ContentUpdateContext.create("serviceUrl", "dmsId"));
			Assert.assertNotNull(uploadFile, "Result should be json object");
			Assert.assertEquals("3.1", getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_VERSION));
			HttpMethod method = createMethod("", new DeleteMethod());
			String delete = httpClient.request("/slingshot/doclib/action/file/node/" + node.replace(":/", ""), method);
			Assert.assertNotNull(delete, "Result should be json object");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test by id.
	 */
	@Test(enabled = false)
	public void testUploadFileInFile() {
		Set<String> aspectsProp = new HashSet<>();
		Map<String, Serializable> props = new HashMap<>();
		String uploadFile;
		try {
			String parentId = getTestParentId(baseUploadPath);
			props.put("cm:description", "test file");
			props.put("cm:title", "set by test");
			uploadFile = alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UPLOAD_MODE));
			// test by upload
			Assert.assertNotNull(uploadFile, "Result should be json object");
			String node = getJsonString(uploadFile, AlfrescoCommunicationConstants.KEY_NODEREF);

			uploadFile = alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UPLOAD_MODE));
			Assert.assertNotNull(uploadFile, "Result should be json object");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
