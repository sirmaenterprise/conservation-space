package com.sirma.itt.emf.semantic.definitions;

import static com.sirma.itt.semantic.model.vocabulary.EMF.DEFINITION;
import static com.sirma.itt.semantic.model.vocabulary.EMF.DEFINITION_ID;
import static com.sirma.itt.semantic.model.vocabulary.EMF.HAS_MODEL;
import static com.sirma.itt.semantic.model.vocabulary.EMF.INSTANCE_TYPE;
import static com.sirma.itt.semantic.model.vocabulary.EMF.IS_DELETED;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.definitions.SemanticDefinitionsModelProvider;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Used to prepare statements for inserting the definitions in the semantic. Builds unique identifier for the definition
 * using its model type and the definition id.
 *
 * @author A. Kunchev
 */
@Extension(target = SemanticDefinitionsModelProvider.TARGET_NAME, enabled = true, order = 10)
public class SemanticDefinitionsURIBuilder implements SemanticDefinitionsModelProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String COMMON_INSTANCE = "commoninstance";

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private TypeMappingProvider typeProvider;

	/**
	 * Creates statements for all none abstract top level definitions and add them to the model, which will be saved in
	 * the semantic. First for the passed definition is build unique identifier used for the URI. This identifier is
	 * created from the definition model type and the id of the definition, separated with "-". For the created URI are
	 * added additional properties. For the RDF type for this URIs is {@link EMF#DEFINITION} and for the
	 * {@link EMF#INSTANCE_TYPE} is used "commoninstance".
	 */
	@Override
	public void provideModelStatements(DefinitionModel definition, Model model) {
		if (definition instanceof TopLevelDefinition && !((TopLevelDefinition) definition).isAbstract()
				&& definition.getType() != null) {
			String uriValue = definitionService.getDefinitionIdentifier(definition);
			if (StringUtils.isBlank(uriValue)) {
				return;
			}

			model.add(SemanticPersistenceHelper.createStatement(uriValue, RDF.TYPE, DEFINITION,
					namespaceRegistryService, valueFactory));
			model.add(SemanticPersistenceHelper.createStatement(uriValue, DEFINITION_ID, definition.getIdentifier(),
					namespaceRegistryService, valueFactory));
			model.add(SemanticPersistenceHelper.createStatement(uriValue, IS_DELETED, Boolean.FALSE,
					namespaceRegistryService, valueFactory));
			model.add(SemanticPersistenceHelper.createStatement(uriValue, INSTANCE_TYPE, COMMON_INSTANCE,
					namespaceRegistryService, valueFactory));

			PropertyDefinition semanticType = PathHelper.findProperty(definition, (PathElement) definition,
					DefaultProperties.SEMANTIC_TYPE);
			Set<String> value = new HashSet<>(1);
			if (semanticType != null) {
				value.add(semanticType.getDefaultValue());
			} else {
				DataTypeDefinition dataType = typeProvider.getDataType(definition.getType());
				if (dataType != null) {
					value = dataType.getUries();
				}
			}
			if (CollectionUtils.isNotEmpty(value)) {
				value.stream().filter(StringUtils::isNotBlank).forEach(uri -> model.add(SemanticPersistenceHelper
						.createStatement(uri, HAS_MODEL, uriValue, namespaceRegistryService, valueFactory)));
			} else {
				LOGGER.warn("Could not load semantic type for definition {}", definition.getIdentifier());
			}
		}
	}
}