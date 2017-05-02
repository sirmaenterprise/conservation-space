package com.sirma.itt.emf.web;

import static org.mockito.MockitoAnnotations.initMocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

/**
 * Base test class for unit test in emf web module. This class is not necessary to be extended if not needed.
 *
 * @author svelikov
 */
public class EmfWebTest {

	/** The Constant LOG. */
	protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EmfWebTest.class);

	/** The Constant slf4j logger. */
	protected static final Logger SLF4J_LOG = LoggerFactory.getLogger(EmfWebTest.class);

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		initMocks(this);
	}

}
