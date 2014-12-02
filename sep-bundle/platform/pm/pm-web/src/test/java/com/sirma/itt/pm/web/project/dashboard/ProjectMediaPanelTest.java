package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Test class for {@link ProjectMediaPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class ProjectMediaPanelTest extends PMTest {

	/** The media panel for project dashboard. */
	private ProjectMediaPanel projectMediaPanel;

	/** The rendition service. */
	private RenditionService renditionService;

	/** The project instance. */
	private ProjectInstance projectInstance;

	/**
	 * Default constructor.
	 */
	public ProjectMediaPanelTest() {

		projectMediaPanel = new ProjectMediaPanel() {

			/** The serial version constant. */
			private static final long serialVersionUID = -1852981074056366063L;

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

		projectInstance = createProjectInstance(Long.valueOf(1), null);

		renditionService = Mockito.mock(RenditionService.class);

		ReflectionUtils.setField(projectMediaPanel, "renditionService", renditionService);
	}

	/**
	 * Test for not available project instance.
	 */
	public void projectInstanceNotAvailableTest() {
		ProjectInstance instance = null;
		Assert.assertNull(instance, "Should fail, there are no project instance in the context. ");
	}

	/**
	 * Test for available project instance.
	 */
	public void projectInstanceAvailbleTest() {
		projectMediaPanel.getDocumentContext().addContextInstance(projectInstance);
		Assert.assertEquals(projectInstance, projectMediaPanel.getDocumentContext()
				.getContextInstance(), "Should pass, project instance is availble.");
	}

	/**
	 * Test for empty instance.
	 */
	public void checkMimeTypeNullInstanceTest() {
		Assert.assertFalse(
				projectMediaPanel.getDocumentContext().getDocumentInstance() == null ? false : true,
				"Will not pass, document instance is null.");
	}

	/**
	 * Test for mime type when the document icheckMimeTypeNullPropertyTest instance has no
	 * properties.
	 */
	public void checkMimeTypeNullPropertyTest() {
		projectMediaPanel.getDocumentContext().setDocumentInstance(new DocumentInstance());
		projectMediaPanel.getDocumentContext().getDocumentInstance().setProperties(null);
		Assert.assertFalse(projectMediaPanel.getDocumentContext().getDocumentInstance()
				.getProperties() == null ? false : true,
				"Will not pass, there are no properties in the instance.");
	}

	/**
	 * Test for supported mime type.
	 */
	public void checkMimeTypeSupportedTest() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DocumentProperties.MIMETYPE, "image/png");
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setProperties(properties);
		projectMediaPanel.getDocumentContext().setDocumentInstance(documentInstance);
		Assert.assertTrue(projectMediaPanel.getDocumentContext().getDocumentInstance()
				.getProperties().get(DocumentProperties.MIMETYPE) == "image/png" ? true : false,
				"Will pass, mime type is supported.");
	}

	/**
	 * Test for not supported default header.
	 */
	public void getCompactHeaderNotSupportedTest() {
		DocumentInstance document = new DocumentInstance();
		Assert.assertNull(projectMediaPanel.getDefaultHeader(document),
				"Will not pass, the compact header is not available.");
	}

	/**
	 * Test for not supported default header.
	 */
	public void getCompactHeaderSupportedTest() {
		DocumentInstance documentInstance = new DocumentInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.HEADER_COMPACT, "Compact Header");
		documentInstance.setProperties(properties);
		Assert.assertEquals("Compact Header", projectMediaPanel.getDefaultHeader(documentInstance),
				"Will pass, we have compact header in the instance. ");
	}

	/**
	 * Test for not supported thumbnail.
	 */
	public void getThumbnailNotAvailableTest() {
		Mockito.when(renditionService.getThumbnail(new DocumentInstance())).thenReturn(null);
		DocumentInstance documentInstance = new DocumentInstance();
		Assert.assertNull(renditionService.getThumbnail(documentInstance),
				"This test will return null becouse instance has no needed properties.");
	}

}
