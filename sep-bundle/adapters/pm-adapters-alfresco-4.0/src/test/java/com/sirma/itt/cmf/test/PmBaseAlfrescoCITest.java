/**
 *
 */
package com.sirma.itt.cmf.test;

import com.sirma.itt.cmf.test.mock.MockupProvider;
import com.sirma.itt.cmf.test.mock.PmMockupProvider;

/**
 * Base remote client test for alfresco. All common methods should be placed here
 *
 * @author borislav banchev
 */
public class PmBaseAlfrescoCITest extends BaseAlfrescoTest {


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected MockupProvider createMockupProvider() {
		return new PmMockupProvider(httpClient);
	}

	/**
	 * Gets the mockup provider.
	 *
	 * @return the mockup provider
	 */
	protected PmMockupProvider getMockupProvider() {
		return (PmMockupProvider) mockupProvider;
	}
}
