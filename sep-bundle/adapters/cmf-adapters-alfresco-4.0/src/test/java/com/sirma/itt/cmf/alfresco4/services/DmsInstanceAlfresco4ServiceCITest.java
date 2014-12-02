package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.ByteArrayAndPropertiesDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;

/**
 * Tests the dms instance general adapter.
 *
 * @author bbanchev
 */
public class DmsInstanceAlfresco4ServiceCITest extends BaseAlfrescoTest {
	private Logger logger = Logger.getLogger(getClass());

	/**
	 * Tests the
	 * {@link DMSInstanceAdapterService#updateNode(com.sirma.itt.emf.instance.model.DMSInstance)}
	 * for correct update of document instances.
	 */
	@Test
	public void updateNode() {
		try {
			DMSInstanceAdapterService mockupDmsInstanceAdapter = mockupProvider
					.mockupDmsInstanceAdapter();
			SectionInstance owning = new SectionInstance();
			owning.setDmsId(createTestFolder(baseUploadPath));
			Assert.assertNotNull(owning.getDmsId(), "DMS id shoud be set");
			cleanUpDMSIds.add(owning.getDmsId());
			owning.setProperties(new HashMap<String, Serializable>());
			owning.getProperties().put(SectionProperties.TITLE, "create by test");
			owning.getProperties().put(SectionProperties.DESCRIPTION, "create by test");
			DocumentInstance createDocument = mockupProvider.createDocument(owning, false,
					"test.txt");
			byte[] byteArray = IOUtils.toByteArray(this.getClass().getResourceAsStream("test.txt"));

			FileAndPropertiesDescriptor uploadContent = mockupProvider.mockupDocumentAdapter()
					.uploadContent(
							createDocument,
							new ByteArrayAndPropertiesDescriptor("test.txt", byteArray, owning
									.getDmsId(), createDocument.getProperties()),
							Collections.singleton(DocumentProperties.TYPE_DOCUMENT_ATTACHMENT));
			createDocument.setDmsId(uploadContent.getId());
			createDocument.getProperties().putAll(uploadContent.getProperties());
			createDocument.getProperties().put(DocumentProperties.DESCRIPTION,
					"| \\ / * : < >\" ? ");
			Map<String, Serializable> updatedMetadata = mockupDmsInstanceAdapter
					.updateNode(createDocument);
			logger.debug("Response of update 1: " + updatedMetadata);
			Assert.assertEquals(updatedMetadata.get(DocumentProperties.DESCRIPTION),
					"| \\ / * : < >\" ? ");
			createDocument.getProperties().put(DocumentProperties.NAME, "test2.txt");
			updatedMetadata = mockupDmsInstanceAdapter.updateNode(createDocument);
			logger.debug("Response of update 2: " + updatedMetadata);
			Assert.assertEquals(updatedMetadata.get(DocumentProperties.NAME), "test2.txt");
		} catch (Exception e) {
			fail(e);
		}
	}
}
