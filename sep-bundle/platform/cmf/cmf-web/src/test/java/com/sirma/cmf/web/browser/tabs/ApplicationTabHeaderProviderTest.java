package com.sirma.cmf.web.browser.tabs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Test class for {@link ApplicationTabHeaderProvider}
 * 
 * @author cdimitrov
 */
@Test
public class ApplicationTabHeaderProviderTest extends CMFTest {

	/** The application tab header provider. */
	private ApplicationTabHeaderProvider tabHeaderProvider;

	/** The browser tab container, holds all supported pages with their objects. */
	private Map<String, BrowserTab> browserTabContainer;

	/** The tested page path. */
	private static final String CMF_PAGE_URL = "specific/url/path";

	/** The tested instance title. */
	private static final String CMF_INSTANCE_TITLE = "instanceTitle";

	/** The tested instance icon. */
	private static final String CMF_CASE_INSTANCE_ICON = "images:caseinstance-icon-16.png";

	/**
	 * The default application tab header provider constructor.
	 */
	public ApplicationTabHeaderProviderTest() {

		browserTabContainer = new HashMap<String, BrowserTab>();

		tabHeaderProvider = new ApplicationTabHeaderProvider() {

			/** The document context. */
			private DocumentContext documentContext = new DocumentContext();

			/**
			 * Getter method for document context.
			 * 
			 * @return application document context
			 */
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			/**
			 * Getter method for browser tab object based on current page path.
			 * 
			 * @return current browser tab
			 */
			protected BrowserTab getBrowserTabBasedPagePath() {
				return browserTabContainer.get(CMF_PAGE_URL);
			}

		};

		CaseInstance caseInstance = new CaseInstance();
		Map<String, Serializable> properties = new HashMap<String, Serializable>();

		properties.put(DefaultProperties.TITLE, CMF_INSTANCE_TITLE);
		caseInstance.setProperties(properties);

		tabHeaderProvider.getDocumentContext().setCurrentInstance(caseInstance);
		browserTabContainer = new HashMap<String, BrowserTab>();
		browserTabContainer.put(CMF_PAGE_URL, new BrowserTab());
	}

	/**
	 * Test method for supported pages in the container.
	 */
	public void supportedPagesContainerTest() {
		Assert.assertNotNull(browserTabContainer);
		Assert.assertTrue(browserTabContainer.size() > 0);
	}

	/**
	 * Test for available context instance.
	 */
	public void availableContextInstanceTest() {
		Assert.assertNotNull(tabHeaderProvider.getDocumentContext().getCurrentInstance());
	}

	/**
	 * Test for available icon based on context instance.
	 */
	public void iconFromInstanceTypeTest() {
		Instance contextInstance = tabHeaderProvider.getDocumentContext().getCurrentInstance();
		Assert.assertNotNull(contextInstance);
		String contextInstanceTypeLowerCase = contextInstance.getClass().getSimpleName()
				.toLowerCase();
		Assert.assertEquals(tabHeaderProvider.generateIcon(contextInstanceTypeLowerCase),
				CMF_CASE_INSTANCE_ICON);
	}

	/**
	 * Test for browser tab details based on current context instance.
	 */
	public void browserTabBasedOnContextTest() {
		BrowserTab browserTab = tabHeaderProvider.getPageBrowserTab();
		Assert.assertNotNull(browserTab);
		Assert.assertEquals(browserTab.getBrowserTabTitle(), CMF_INSTANCE_TITLE);
		Assert.assertEquals(browserTab.getBrowserTabIcon(), CMF_CASE_INSTANCE_ICON);
	}

	/**
	 * Test for browser tab object based on page path.
	 */
	public void browserTabBasedOnPagePathTest() {
		BrowserTab browserTab = tabHeaderProvider.getBrowserTabBasedPagePath();
		Assert.assertNotNull(browserTab);
	}

	/**
	 * Test for null-able instances that type values will be searched in code-lists.
	 */
	public void isCodelistLocatedNullableInstanceTest() {
		Assert.assertFalse(tabHeaderProvider.isCodelistLocated(null));
	}

	/**
	 * Test for supported instances that type values will be searched in code-lists.
	 */
	public void isCodelistLocatedSupportedInstancesTest() {
		Assert.assertTrue(tabHeaderProvider.isCodelistLocated(createWorkflowInstance(1L)));
		Assert.assertTrue(tabHeaderProvider.isCodelistLocated(createWorkflowTaskInstance(1L)));
	}
}
