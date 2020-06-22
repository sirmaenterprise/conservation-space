package com.sirma.itt.emf.semantic.definitions;

import static com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper.createStatement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.definitions.SemanticDefinitionsModelProvider;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Class responsible for registering properties as semantic properties to be returned from queries.
 *
 * @author BBonev
 * @author A. Kunchev
 */
@Extension(target = SemanticDefinitionsModelProvider.TARGET_NAME, enabled = true, order = 5)
public class SemanticPropertyRegister implements SemanticDefinitionsModelProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private NamespaceRegistryService registryService;

	/**
	 * Collect URIs from definition and creates statements which are added to the semantic model. This method collects
	 * all definitions URIs, including controls, regions and etc. in the definitions.
	 *
	 * @param definition
	 *            the definition from which are builded URIs
	 * @param model
	 *            the semantic model to which will be added created statements
	 */
	@Override
	public void provideModelStatements(DefinitionModel definition, Model model) {
		if (definition == null || model == null) {
			return;
		}

		// collect object properties and mark them as such in the model
		definition
				.fieldsStream()
					.flatMap(PropertyDefinition::stream)
					.filter(PropertyDefinition.isObjectProperty())
					.map(PropertyDefinition.resolveUri())
					.filter(StringUtils::isNotBlank)
					.flatMap(this::setAsObjectProperty)
					.filter(Objects::nonNull)
					.forEach(model::add);

		// collect data properties and marks them as such in the model
		definition
				.fieldsStream()
					.flatMap(PropertyDefinition::stream)
					.filter(PropertyDefinition.isObjectProperty().negate())
					.map(PropertyDefinition.resolveUri())
					.filter(StringUtils::isNotBlank)
					.flatMap(this::setAsDataProperty)
					.filter(Objects::nonNull)
					.forEach(model::add);
	}

	private Stream<Statement> setAsObjectProperty(String uri) {
		try {
			return Stream.of(createStatement(uri, RDF.TYPE, EMF.DEFINITION_PROPERTY, registryService, valueFactory),
					createStatement(uri, RDF.TYPE, EMF.DEFINITION_OBJECT_PROPERTY, registryService, valueFactory));
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid URI: {} -> {}", uri, e.getMessage());
			LOGGER.trace("Invalid URI: {}", uri, e);
			return Stream.empty();
		}
	}

	private Stream<Statement> setAsDataProperty(String uri) {
		try {
			return Stream.of(createStatement(uri, RDF.TYPE, EMF.DEFINITION_PROPERTY, registryService, valueFactory),
					createStatement(uri, RDF.TYPE, EMF.DEFINITION_DATA_PROPERTY, registryService, valueFactory));
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid URI: {} -> {}", uri, e.getMessage());
			LOGGER.trace("Invalid URI: {}", uri, e);
			return Stream.empty();
		}
	}
}
