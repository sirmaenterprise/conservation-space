package com.sirma.itt.cmf.test.mock;

import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * {@link DMSConverterFactory} mock that injects other mocked services. Should be extends in other adapters.
 *
 * @author bbanchev
 */
public class DMSConverterFactoryMock extends DMSConverterFactory {
	MockupProvider provider;

	/**
	 * Init new factory.
	 *
	 * @param provider
	 *            is the rest client
	 */
	public DMSConverterFactoryMock(MockupProvider provider) {
		super();
		this.provider = provider;

	}

	@Override
	public DMSTypeConverter getConverter(Class<? extends Instance> model) {
		return getConverterByName(ConverterConstants.GENERAL);
	}

	/**
	 * Internal method to get converter for given prefix config name.
	 *
	 * @param key
	 *            is the key to use
	 * @return the converter from mockup
	 */
	private DMSTypeConverter getConverterByName(String key) {
		return provider.mockupDMSTypeConverter(key);
	}
}
