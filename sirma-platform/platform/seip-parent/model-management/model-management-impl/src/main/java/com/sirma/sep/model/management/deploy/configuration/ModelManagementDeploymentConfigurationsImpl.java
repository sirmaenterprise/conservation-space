package com.sirma.sep.model.management.deploy.configuration;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Configuration properties related to the deployment of models.
 *
 * @author Mihail Radkov
 */
public class ModelManagementDeploymentConfigurationsImpl implements ModelManagementDeploymentConfigurations {

	public static final String DEFAULT_SEMANTIC_CONTEXT = "http://ittruse.ittbg.com/model/enterpriseManagementFramework";

	@ConfigurationPropertyDefinition(
			type = IRI.class,
			defaultValue = DEFAULT_SEMANTIC_CONTEXT,
			subSystem = "models",
			label = "Configuration to specify the default semantic persistence context for changes made via the model management."
					+ " Default value is " + DEFAULT_SEMANTIC_CONTEXT)
	private static final String MODEL_MANAGEMENT_SEMANTIC_PERSISTENCE_CONTEXT = "model.management.semantic.persistence.context";

	@ConfigurationPropertyDefinition(
			type = Boolean.class,
			defaultValue = "true",
			subSystem = "models",
			label = "Configuration to specify the default ontology export behaviour. If enabled the exported ontologies "
					+ "will be in pretty printed. This includes grouping and sorting the exported statements as well as "
					+ "collapsing multiple predicates and values per subject")
	private static final String MODEL_MANAGEMENT_SEMANTIC_EXPORT_PRETTY_PRINT = "model.management.semantic.export.prettyPrint";

	@Inject
	@Configuration(MODEL_MANAGEMENT_SEMANTIC_PERSISTENCE_CONTEXT)
	private ConfigurationProperty<IRI> semanticContext;

	@Inject
	@Configuration(MODEL_MANAGEMENT_SEMANTIC_EXPORT_PRETTY_PRINT)
	private ConfigurationProperty<Boolean> prettyPrintEnabled;

	@ConfigurationConverter(MODEL_MANAGEMENT_SEMANTIC_PERSISTENCE_CONTEXT)
	static IRI registerOntology(ConverterContext context, TransactionSupport transactionSupport, RepositoryConnection repositoryConnection) {
		String rawValue = context.getRawValue();
		if (rawValue == null) {
			return null;
		}
		SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
		IRI ontology = valueFactory.createIRI(rawValue);
		if (EMF.DATA_CONTEXT.equals(ontology)) {
			// if configured to this context and export is triggered the user could break the application if pretty
			// print is enabled as will try to load all application data in memory and the server will most linkly crash
			throw new ConfigurationException("Setting " + EMF.DATA_CONTEXT + " as ontology is not allowed!");
		}
		String title = createTitleFromOntology(rawValue);
		if (!repositoryConnection.hasStatement(ontology, RDF.TYPE, OWL.ONTOLOGY, true)) {
			transactionSupport.invokeInTx(() -> {
				repositoryConnection.add(valueFactory.createStatement(ontology, RDF.TYPE, OWL.ONTOLOGY), ontology);
				repositoryConnection.add(valueFactory.createStatement(ontology, RDFS.LABEL, valueFactory.createLiteral(title)), ontology);
				return null;
			});
		}
		return ontology;
	}

	private static String createTitleFromOntology(String ontology) {
		URI uri = URI.create(ontology);
		return Arrays.stream(uri.getPath().replaceAll("[/#:]+", " ").trim().split(" "))
				.map(StringUtils::capitalize)
				.flatMap(s -> Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)))
				.collect(Collectors.joining(" "));
	}

	@Override
	public ConfigurationProperty<IRI> getSemanticContext() {
		return semanticContext;
	}

	@Override
	public ConfigurationProperty<Boolean> getPrettyPrintEnabled() {
		return prettyPrintEnabled;
	}
}
