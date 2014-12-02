package com.sirma.itt.objects.web.project.dashboard;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.objects.ObjectsTest;

/**
 * Test class for {@link ProjectObjectsPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class ProjectObjectsPanelTest extends ObjectsTest {

	private static final String DOMAIN_OBJECT_URI = "domainObjectURI";

	/** The object panel for project dashboard. */
	private ProjectObjectsPanel projectObjectsPanel;

	/**
	 * Default constructor.
	 */
	public ProjectObjectsPanelTest() {

		projectObjectsPanel = new ProjectObjectsPanel() {

			/** The serial version constant. */
			private static final long serialVersionUID = 7748150326191505221L;

			/** The document context. */
			private DocumentContext documentContext = new DocumentContext();

			/**
			 * {@inheritDoc}
			 */
			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected String getDomainObjectType() {
				return DOMAIN_OBJECT_URI;
			}
		};
	}

	/**
	 * Test for not available project instance.
	 */
	public void projectInstanceNotAvailableTest() {
		Assert.assertNull(projectObjectsPanel.getDocumentContext()
				.getContextInstance(),
				"This will fail, the context instance(ProjectInstance) is not available.");
	}

	/**
	 * Test for domain object types.
	 */
	public void getDomainObjectTypeTest() {
		Assert.assertEquals(projectObjectsPanel.getDomainObjectType(),
				DOMAIN_OBJECT_URI,
				"This will pass, domain data types are available.");
	}

	/**
	 * Test for null URI wrapper.
	 */
	public void uriWrapperNullTest() {
		Assert.assertNull(projectObjectsPanel.uriWrapper(null),
				"Should not passed, the URI is null.");
	}

	/**
	 * Test for empty URI wrapper.
	 */
	public void uriWrapperEmptyTest() {
		Assert.assertNull(projectObjectsPanel.uriWrapper(""),
				"Should not passed, the URI is empty.");
	}

}
