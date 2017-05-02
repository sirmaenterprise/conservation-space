package com.sirma.itt.cmf.alfresco4.remote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.services.DmsInstanceAlfresco4Service;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.seip.content.descriptor.InputStreamFileDescriptor;
import com.sirma.itt.seip.domain.exceptions.DmsRuntimeException;

/**
 * Tests alfersco uploader for upload and update.
 *
 * @author bbanchev
 */
public class AlfrescoUploaderCITest extends BaseAlfrescoTest {
	private static final String THUMB_MODE_GENERATION = ThumbnailGenerationMode.ASYNCH.toString();
	private AlfrescoUploader alfrescoUploader;
	private String testFolder;

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.test.BaseAlfrescoTest#setUp()
	 */
	@Override
	@BeforeClass
	public void setUp() {
		super.setUp();
		alfrescoUploader = mockupProvider.mockupAlfrescoUploader();
		testFolder = createTestFolder(baseUploadPath);
		cleanUpGlobalIds.add(testFolder);
	}

	/**
	 * Tests
	 * {@link AlfrescoUploader#uploadFile(com.sirma.itt.emf.adapter.DMSFileDescriptor, String, String, String, String, java.util.Map, java.util.Set, String)}
	 */
	@Test(enabled = false)
	public void uploadByStandartService() {

	}

	/**
	 * Tests
	 * {@link AlfrescoUploader#uploadFile(String, com.sirma.itt.emf.adapter.DMSFileDescriptor, String, String, String, String, java.util.Map, java.util.Set, String)}
	 */
	@Test(enabled = false)
	public void uploadFileByServiceAndDescriptor() {
		Map<String, Serializable> propertiesProp = new HashMap<>();
		Set<String> aspectsProp = new HashSet<>();
		String parentId = null;
		try {
			alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UploadMode.DIRECT));
			Assert.fail("Exception should be thrown - missing source!");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("source"), "Exception should be thrown - missing source!");
		}
		try {
			alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UploadMode.DIRECT));
			Assert.fail("Exception should be thrown!");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("storage location"), "Exception should be thrown - missing storage!");
		}

		try {
			alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UploadMode.DIRECT));
			Assert.fail("Exception should be thrown for missing folder!");
		} catch (DmsRuntimeException e) {
			// runtime exception should be thrown for invalid parent
			logger.debug(e);
		} catch (Exception e) {
			fail(e);
		}
		try {
			alfrescoUploader.uploadFile(ContentUploadContext.create("serviceUrl", UploadMode.DIRECT));
		} catch (Exception e) {
			if (!e.getMessage().contains("Site (notfoundsite) not found.")) {
				fail(e);
			}
		}
		try {
			parentId = testFolder;
			String uploadFile = alfrescoUploader
					.uploadFile(ContentUploadContext.create("serviceUrl", UploadMode.DIRECT));
			assertNotNull(uploadFile, "Upload result should be not null!");
			logger.debug(new JSONObject(uploadFile));
		} catch (Exception e) {
			fail(e);
		}
	}

	/**
	 * Tests
	 * {@link AlfrescoUploader#uploadFile(String, org.apache.commons.httpclient.methods.multipart.PartSource, String, String, String, String, java.util.Map, java.util.Set, Boolean, Boolean, String, String)}
	 */
	@Test
	public void uploadFileByPath() {
	}

	/**
	 * Tests
	 * {@link AlfrescoUploader#updateFile(String, com.sirma.itt.emf.adapter.DMSFileDescriptor, String, String, Map, Set, Boolean, String, String)
	 */
	@Test
	public void updateFile() {
	}

	/**
	 * Mockup a simple upload descriptor for file from the {@link DmsInstanceAlfresco4Service} path
	 *
	 * @param name
	 *            is the file to look for
	 * @param metadataToInclude
	 *            is the data to include
	 * @return the file descriptor
	 */
	private UploadWrapperDescriptor getSimpleFileDescriptor(String name, Map<String, Serializable> metadataToInclude) {
		return new UploadWrapperDescriptor(
				new InputStreamFileDescriptor(name, DmsInstanceAlfresco4Service.class.getResourceAsStream(name)),
				testFolder, metadataToInclude);
	}
}
