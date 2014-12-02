package com.sirma.itt.objects.web.userdashboard.panel;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.objects.ObjectsTest;

/**
 * Test class for {@link MyObjectsPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class MyObjectsPanelTest extends ObjectsTest {

	private static final String DOMAIN_OBJECT_URI = "domainObjectURI";

	/** The object panel for user dashboard. */
	private MyObjectsPanel myObjectsPanel;

	/**
	 * Default constructor.
	 */
	public MyObjectsPanelTest() {

		myObjectsPanel = new MyObjectsPanel() {

			/** The serial version constant. */
			private static final long serialVersionUID = 3011010651846187150L;

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
	 * Test for domain object types.
	 */
	public void getDomainObjectTypeTest() {
		Assert.assertEquals(myObjectsPanel.getDomainObjectType(),
				DOMAIN_OBJECT_URI,
				"This will pass, domain data types are available.");
	}

	/**
	 * Test for null URI wrapper.
	 */
	public void uriWrapperNullTest() {
		Assert.assertNull(myObjectsPanel.uriWrapper(null),
				"Should not passed, the URI is null.");
	}

	/**
	 * Test for empty URI wrapper.
	 */
	public void uriWrapperEmptyTest() {
		Assert.assertNull(myObjectsPanel.uriWrapper(""),
				"Should not passed, the URI is empty.");
	}

}
