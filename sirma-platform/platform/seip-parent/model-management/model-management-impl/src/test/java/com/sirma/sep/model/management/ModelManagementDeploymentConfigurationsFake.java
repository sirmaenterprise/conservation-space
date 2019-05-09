package com.sirma.sep.model.management;

import javax.enterprise.inject.Alternative;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurations;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurationsImpl;

/**
 * Fake to verride the {@link ModelManagementDeploymentConfigurationsImpl} in tests
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/03/2019
 */
@Alternative
public class ModelManagementDeploymentConfigurationsFake implements ModelManagementDeploymentConfigurations {

	private ConfigurationProperty<IRI> semanticContext = new ConfigurationPropertyMock<>(
			SimpleValueFactory.getInstance()
					.createIRI(ModelManagementDeploymentConfigurationsImpl.DEFAULT_SEMANTIC_CONTEXT));
	private ConfigurationProperty<Boolean> prettyPrintEnabled = new ConfigurationPropertyMock<>(Boolean.TRUE);

	@Override
	public ConfigurationProperty<IRI> getSemanticContext() {
		return semanticContext;
	}

	@Override
	public ConfigurationProperty<Boolean> getPrettyPrintEnabled() {
		return prettyPrintEnabled;
	}

	public void setSemanticContext(IRI semanticContext) {
		this.semanticContext = new ConfigurationPropertyMock<>(semanticContext);
	}

	public void setPrettyPrintEnabled(Boolean prettyPrintEnabled) {
		this.prettyPrintEnabled = new ConfigurationPropertyMock<>(prettyPrintEnabled);
	}
}
