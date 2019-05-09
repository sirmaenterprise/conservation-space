package com.sirma.sep.model.management.deploy.configuration;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Configuration properties related to the deployment of models.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/03/2019
 */
public interface ModelManagementDeploymentConfigurations {
	/**
	 * The semantic context where model changes should be persisted.
	 *
	 * @return the save context of model changes
	 */
	ConfigurationProperty<IRI> getSemanticContext();

	/**
	 * Controls how ontology data is exported. If true the files will be formatted and organized by entry types.
	 * The downside is it requires to load the data in memory. Not applicable for big graphs
	 *
	 * @return if pretty print is enabled for ontology export
	 */
	ConfigurationProperty<Boolean> getPrettyPrintEnabled();
}
