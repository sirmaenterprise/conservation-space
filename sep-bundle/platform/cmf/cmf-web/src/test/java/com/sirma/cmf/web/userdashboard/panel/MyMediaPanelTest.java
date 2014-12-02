package com.sirma.cmf.web.userdashboard.panel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;

/**
 * Test class for {@link MyMediaPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class MyMediaPanelTest extends CMFTest {

	/** The media panel for personal dashboards. */
	private MyMediaPanel myMediaPanel;

	/** The rendition service. */
	private RenditionService renditionService;

	/** The document instant that will be used in the tests. */
	private DocumentInstance documentInstnace;

	/**
	 * Default constructor.
	 */
	public MyMediaPanelTest() {

		myMediaPanel = new MyMediaPanel() {

			/** The serial version constant. */
			private static final long serialVersionUID = 3590093946385044620L;

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

		documentInstnace = createDocumentInstance(1L);

		renditionService = Mockito.mock(RenditionService.class);
		Mockito.when(renditionService.getThumbnail(new DocumentInstance())).thenReturn(null);
		ReflectionUtils.setField(myMediaPanel, "renditionService", renditionService);
	}

	/**
	 * Test for empty instance.
	 */
	public void checkMimeTypeNullInstanceTest() {
		Assert.assertFalse("Will not pass, document instance is null.", myMediaPanel
				.getDocumentContext().getDocumentInstance() != null ? true : false);
	}

	/**
	 * Test for supported mime type.
	 */
	public void checkMimeTypeSupportedTest() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DocumentProperties.MIMETYPE, "image/png");
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setProperties(properties);
		Assert.assertTrue("Will pass, mime type is supported.", documentInstance.getProperties()
				.get(DocumentProperties.MIMETYPE) != null ? true : false);
	}

	/**
	 * Test for not supported mime type.
	 */
	public void checkMimeTypeNotSupportedTest() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DocumentProperties.MIMETYPE, "application/xml");
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setProperties(properties);
		Assert.assertFalse("Will pass, mime type is supported.", documentInstance.getProperties()
				.get(DocumentProperties.MIMETYPE) == "image/png" ? true : false);
	}

	/**
	 * Test for not supported default header.
	 */
	public void getCompactHeaderNotSupportedTest() {
		DocumentInstance document = new DocumentInstance();
		Assert.assertNull("Will not pass, the compact header is not available.",
				myMediaPanel.getDefaultHeader(document));
	}

	/**
	 * Test for not supported default header.
	 */
	public void getCompactHeaderSupportedTest() {
		DocumentInstance documentInstance = new DocumentInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.HEADER_COMPACT, "Compact Header");
		documentInstance.setProperties(properties);
		Assert.assertEquals("Will pass, we have compact header in the instance. ",
				"Compact Header", myMediaPanel.getDefaultHeader(documentInstance));
	}

	/**
	 * Test for not supported thumbnail.
	 */
	public void getThumbnailNotAvailableTest() {
		DocumentInstance documentInstance = new DocumentInstance();
		Assert.assertNull("This test will return null becouse instance has no needed properties.",
				renditionService.getThumbnail(documentInstance));
	}

}
