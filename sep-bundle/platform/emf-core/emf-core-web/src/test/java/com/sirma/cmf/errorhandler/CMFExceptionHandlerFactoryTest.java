package com.sirma.cmf.errorhandler;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.web.errorhandler.CMFExceptionHandlerFactory;

/**
 * Factory test.
 * 
 * @author svelikov
 */
public class CMFExceptionHandlerFactoryTest {

	private CMFExceptionHandlerFactory cmfExceptionHandlerFactory;

	/**
	 * Init test constructor.
	 */
	public CMFExceptionHandlerFactoryTest() {
		cmfExceptionHandlerFactory = new CMFExceptionHandlerFactory(new ExceptionHandlerFactory() {

			@Override
			public ExceptionHandler getExceptionHandler() {
				return null;
			}
		});
	}

	/**
	 * Test for getExceptionHandler.
	 */
	@Test
	public void getExceptionHandlerTest() {
		ExceptionHandler exceptionHandler = cmfExceptionHandlerFactory.getExceptionHandler();
		Assert.assertNotNull(exceptionHandler);
	}
}
