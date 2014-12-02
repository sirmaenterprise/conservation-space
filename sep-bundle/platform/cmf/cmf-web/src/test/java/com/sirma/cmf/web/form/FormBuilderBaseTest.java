package com.sirma.cmf.web.form;

import com.sirma.cmf.CMFTest;

/**
 * The Class FormBuilderBaseTest.
 * 
 * @author svelikov
 */
public class FormBuilderBaseTest extends CMFTest {

	/** The builder helper. */
	protected FormBuilderHelperMock builderHelper;

	/**
	 * Instantiates a new form builder base test.
	 */
	public FormBuilderBaseTest() {
		builderHelper = new FormBuilderHelperMock();
	}

}
