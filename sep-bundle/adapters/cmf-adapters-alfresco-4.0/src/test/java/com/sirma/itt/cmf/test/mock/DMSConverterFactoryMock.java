package com.sirma.itt.cmf.test.mock;

import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * {@link DMSConverterFactory} mock that injects other mocked services. Should be extends in other
 * adapters.
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
		if (model.isAssignableFrom(DocumentInstance.class)) {
			return getConverterByName(ConverterConstants.DOCUMENT);
		}
		if (model.isAssignableFrom(SectionInstance.class)) {
			return getConverterByName(ConverterConstants.SECTION);
		}
		if (model.isAssignableFrom(CaseInstance.class)) {
			return getConverterByName(ConverterConstants.CASE);
		}
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
