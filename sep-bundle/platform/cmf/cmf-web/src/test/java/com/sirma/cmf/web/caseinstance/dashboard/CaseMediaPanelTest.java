package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.rendition.RenditionService;

/**
 * Test class for {@link CaseMediaPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class CaseMediaPanelTest extends CMFTest {

	/** The media panel for case dashboard. */
	private CaseMediaPanel caseMediaPanle;

	/** The rendition service. */
	private RenditionService renditionService;

	private CaseInstance caseInstance;

	/**
	 * Default constructor.
	 */
	public CaseMediaPanelTest() {

		caseMediaPanle = new CaseMediaPanel() {

			/** The serial version constant. */
			private static final long serialVersionUID = 1L;

			/** The document context. */
			private DocumentContext docContext = new DocumentContext();

			/**
			 * {@inheritDoc}
			 */
			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}
		};

		caseInstance = new CaseInstance();

		renditionService = Mockito.mock(RenditionService.class);
		Mockito.when(renditionService.getThumbnail(new DocumentInstance()))
				.thenReturn(null);
		ReflectionUtils.setField(caseMediaPanle, "renditionService",
				renditionService);
	}

	/**
	 * Test for not available case instance.
	 */
	public void caseInstanceNotAvailableTest() {
		CaseInstance instance = null;
		Assert.assertNull(
				"Should fail, there are no case instance in the context. ",
				instance);
	}

	/**
	 * Test for available case instance.
	 */
	public void caseInstanceAvailbleTest() {
		caseMediaPanle.getDocumentContext().addContextInstance(caseInstance);
		Assert.assertEquals("Should pass, case instance is availble.",
				caseInstance, (CaseInstance) caseMediaPanle
						.getDocumentContext().getContextInstance());
	}

	/**
	 * Test for empty instance.
	 */
	public void checkMimeTypeNullInstanceTest() {
		Assert.assertFalse("Will not pass, document instance is null.",
				caseMediaPanle.isImage(caseMediaPanle.getDocumentContext()
						.getDocumentInstance()));
	}

	/**
	 * Test for mime type when the document instance has no properties.
	 */
	public void checkMimeTypeNullPropertyTest() {
		DocumentInstance documentInstance = new DocumentInstance();
		Assert.assertFalse(
				"Will not pass, there are no properties in the instance.",
				caseMediaPanle.isImage(documentInstance));
	}

	/**
	 * Test for supported mime type.
	 */
	public void checkMimeTypeSupportedTest() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("mimetype", "image/png");
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setProperties(properties);
		Assert.assertTrue("Will pass, mime type is supported.",
				caseMediaPanle.isImage(documentInstance));
	}

	/**
	 * Test for not supported mime type.
	 */
	public void checkMimeTypeNotSupportedTest() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("mimetype", "application/xml");
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setProperties(properties);
		Assert.assertFalse("Will not pass, mime type is not supported.",
				caseMediaPanle.isImage(documentInstance));
	}

	/**
	 * Test for not supported default header.
	 */
	public void getCompactHeaderNotSupportedTest() {
		DocumentInstance document = new DocumentInstance();
		Assert.assertNull(
				"Will not pass, the compact header is not available.",
				caseMediaPanle.getDefaultHeader(document));
	}

	/**
	 * Test for not supported default header.
	 */
	public void getCompactHeaderSupportedTest() {
		DocumentInstance documentInstance = new DocumentInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("compact_header", "Compact Header");
		documentInstance.setProperties(properties);
		Assert.assertEquals(
				"Will pass, we have compact header in the instance. ",
				"Compact Header",
				caseMediaPanle.getDefaultHeader(documentInstance));
	}

	/**
	 * Test for not supported thumbnail.
	 */
	public void getThumbnailNotAvailableTest() {
		DocumentInstance documentInstance = new DocumentInstance();
		Assert.assertNull(
				"This test will return null becouse instance has no needed properties.",
				renditionService.getThumbnail(documentInstance));
	}

}
