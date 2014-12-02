package com.sirma.itt.cmf.test.webscripts;

import org.testng.annotations.DataProvider;

/**
 * The Class AlfrescoTestProvider is the base provider for all test data.
 */
public class AlfrescoTestProvider {

	public static final String CASE_DOCUMENT_UNDERTEST_DMSID = "workspace://SpacesStore/7807aba7-2b90-47e3-9d0d-1b0a2aeca4e1";

	/**
	 * Action data.
	 * 
	 * @return the object[][]
	 */
	@DataProvider(name = "ActionTest")
	public static Object[][] actionData() {
		// TODO by retrieving case's documents
		return new Object[][] {

		{ CASE_DOCUMENT_UNDERTEST_DMSID }

		};

	}
}