/**
 *
 */
package com.sirma.itt.pm.test.webscripts;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.test.PmBaseAlfrescoCITest;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;

/**
 * Tests the init of cmf structures in dms.
 *
 * @author borislav banchev
 */
public class InitPMScriptCITest extends PmBaseAlfrescoCITest {
	/** The definiton adapter. */
	private DMSDefintionAdapterService definitonAdapter;

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.test.BaseAlfrescoTest#setUp()
	 */
	@Override
	@BeforeClass
	public void setUp() {
		super.setUp();
		definitonAdapter = mockupProvider.mockupDefinitonAdapter();
	}

	/**
	 * Test init.
	 */
	@Test(enabled = true)
	public void testInit() {
		try {
			List<FileDescriptor> allProjectDefinitions = definitonAdapter
					.getDefinitions(ProjectDefinition.class);
			Assert.assertTrue(allProjectDefinitions.size() > 0,
					"Should have at least 1 pm definition");
		} catch (DMSException e) {
			Assert.fail("Definition test failed!", e);
		}
	}
}
