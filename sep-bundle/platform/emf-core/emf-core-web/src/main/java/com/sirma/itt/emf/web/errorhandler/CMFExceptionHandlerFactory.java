package com.sirma.itt.emf.web.errorhandler;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * A factory for creating CMFExceptionHandler objects.
 * 
 * @author y.yordanov
 */
public class CMFExceptionHandlerFactory extends ExceptionHandlerFactory {

	/** The factory. */
	private final ExceptionHandlerFactory factory;

	/**
	 * Constructs the factory.
	 * 
	 * @param factory
	 *            - {@link ExceptionHandlerFactory}
	 */
	public CMFExceptionHandlerFactory(ExceptionHandlerFactory factory) {
		this.factory = factory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExceptionHandler getExceptionHandler() {
		ExceptionHandler handler = new CMFExceptionHandler(
				this.factory.getExceptionHandler());
		return handler;
	}

}
