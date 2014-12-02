package com.sirma.itt.objects.web.caseinstance.dashboard;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.objects.ObjectsTest;

/**
 * Test class for {@link CaseObjectsPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class CaseObjectsPanelTest extends ObjectsTest {

	private static final String DOMAIN_OBJECT_URI = "domainObjectURI";

	/** The object panel for case dashboard. */
	private CaseObjectsPanel caseObjectsPanel;

	/**
	 * Default constructor.
	 */
	public CaseObjectsPanelTest() {

		caseObjectsPanel = new CaseObjectsPanel() {

			/** The serial version constant. */
			private static final long serialVersionUID = 1L;
			
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
	public void caseInstanceNotAvailableTest() {
		Assert.assertNull(caseObjectsPanel.getDocumentContext()
				.getContextInstance(),
				"This will fail, the context instance(CaseInstance) is not available.");
	}

	/**
	 * Test for domain object types.
	 */
	public void getDomainObjectTypeTest() {
		Assert.assertEquals(caseObjectsPanel.getDomainObjectType(),
				DOMAIN_OBJECT_URI,
				"This will pass, domain data types are available.");
	}

	/**
	 * Test for null URI wrapper.
	 */
	public void uriWrapperNullTest() {
		Assert.assertNull(caseObjectsPanel.uriWrapper(null),
				"Should not passed, the URI is null.");
	}

	/**
	 * Test for empty URI wrapper.
	 */
	public void uriWrapperEmptyTest() {
		Assert.assertNull(caseObjectsPanel.uriWrapper(""),
				"Should not passed, the URI is empty.");
	}

}
