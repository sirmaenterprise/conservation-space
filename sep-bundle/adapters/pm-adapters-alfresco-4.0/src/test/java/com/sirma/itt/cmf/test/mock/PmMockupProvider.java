package com.sirma.itt.cmf.test.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sirma.itt.cmf.alfresco4.services.CmfDefintionAdapterServiceExtension;
import com.sirma.itt.cmf.alfresco4.services.DefinitionAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverterMockUp;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.pm.alfresco4.services.PMDefinitionServiceExtension;
import com.sirma.itt.pm.alfresco4.services.ProjectInstanceAlfresco4Service;
import com.sirma.itt.pm.alfresco4.services.convert.PmConverterContants;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;

/**
 * The Class PmMockupProvider extends default {@link MockupProvider} and adds pm specific settings.
 */
public class PmMockupProvider extends MockupProvider {

	/**
	 * Instantiates a new mockup provider.
	 *
	 * @param client
	 *            the client
	 */
	public PmMockupProvider(RESTClient client) {
		super(client);
	}

	/**
	 * Gets the dictionary impl.
	 *
	 * @return the dictionary impl
	 */
	@Override
	protected DictionaryServiceMock getDictionaryImpl() {
		return new PmDictionaryServiceMock(this);
	}

	/**
	 * Mockup project adapter.
	 *
	 * @return the project instance alfresco4 service
	 */
	public ProjectInstanceAlfresco4Service mockupProjectAdapter() {
		ProjectInstanceAlfresco4Service adapter = new ProjectInstanceAlfresco4Service();
		setParam(adapter, "restClient", httpClient);
		setParam(adapter, "dictionaryService", dictionaryService);
		setParam(adapter, "projectConvertor", mockupDMSTypeConverter("project"));
		return adapter;
	}

	@Override
	public DMSTypeConverter mockupDMSTypeConverter(String model) {
		try {
			return DMSTypeConverterMockUp.create(converter, dictionaryService, evaluatorManager,
					model.toLowerCase(), cacheContext, getConverterProperties());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public DMSDefintionAdapterService mockupDefinitonAdapter() {
		DefinitionAlfresco4Service adapter = new DefinitionAlfresco4Service();
		setParam(adapter, "restClient", httpClient);
		List<DMSDefintionAdapterServiceExtension> list = new ArrayList<>(2);
		list.add(new CmfDefintionAdapterServiceExtension());
		list.add(new PMDefinitionServiceExtension());
		setParam(adapter, "extensions", list);
		adapter.initialize();
		return adapter;
	}

	@Override
	protected Properties getConverterProperties() throws IOException {
		Properties converterProperties = super.getConverterProperties();
		Properties props = new Properties();
		props.load(PmConverterContants.class.getResourceAsStream("pmconvertor.properties"));
		converterProperties.putAll(props);
		return converterProperties;
	}

	/**
	 * Creates the project definition.
	 *
	 * @param identifier
	 *            the identifier for the definition - dms id
	 * @return the project definition
	 */
	public ProjectDefinition createProjectDefinition(String identifier) {
		ProjectDefinition definition = new ProjectDefinitionImpl();
		definition.setIdentifier(identifier.toString());
		definition.setContainer("dms");
		definition.setDmsId(identifier);
		return definition;
	}
}
