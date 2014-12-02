package com.sirma.itt.emf.solr.services.ws.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;

// TODO: Auto-generated Javadoc
/**
 * Handles requests for properties retrieval from the solr services.
 */
@Path("/properties")
@ApplicationScoped
public class PropertiesRestService extends EmfRestService {

	/** The logger. */
	private Logger logger = LoggerFactory.getLogger(getClass());
	/** The solr connector. */
	@Inject
	private SolrConnector solrConnector;

	/** The semantic definition service. */
	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The semantic db url. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_URL)
	private String semanticDBUrl;

	/** The semantic definition mapping. */
	private Map<String, DefinitionModel> semanticDefinitionMapping;

	/**
	 * Iterates over all definitions and if any rdf type is specified, puts it in the
	 * semanticDefinitionMapping.
	 */
	@PostConstruct
	private void init() {
		semanticDefinitionMapping = new HashMap<>();
		List<ClassInstance> instances = semanticDefinitionService.getClasses();
		for (ClassInstance instance : instances) {
			DataTypeDefinition typeDefinition = dictionaryService
					.getDataTypeDefinition(namespaceRegistryService.buildFullUri(instance.getId()
							.toString()));
			if (typeDefinition != null) {
				InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
						.getInstanceService(typeDefinition.getJavaClass());
				if (instanceService != null) {
					List<DefinitionModel> allDefinitions = dictionaryService
							.getAllDefinitions(instanceService.getInstanceDefinitionClass());
					for (DefinitionModel definition : allDefinitions) {
						PropertyDefinition rdfType = PathHelper.findProperty(definition,
								(PathElement) definition, "rdf:type");
						if (rdfType != null && rdfType.getDefaultValue() != null) {
							semanticDefinitionMapping
									.put(namespaceRegistryService.getShortUri(rdfType
											.getDefaultValue()), definition);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the fields listed in solr's schema.xml
	 * 
	 * @return the response
	 */
	@Path("/fields")
	@GET
	public String getProperties() {
		Map<String, String> responseObj = getSolrFields();
		Iterator<String> propertiesIterator = responseObj.keySet().iterator();
		JSONArray keysJSON = new JSONArray();
		while (propertiesIterator.hasNext()) {
			String property = propertiesIterator.next();
			keysJSON.put(property);
		}
		return keysJSON.toString();
	}

	/**
	 * Gets the intersection between definition properties and properties indexed in solr.
	 * 
	 * @param forType
	 *            the for type. If getting a definition's properties, the semantic short type of the
	 *            definition should be attached to her with a '_' symbol e.g. (emf:Project_PRJ10001)
	 * @param commonOnly
	 *            If the service should return the intersection of the properties.
	 * @param multiValued
	 *            Indicates whether the definition properties should be compared to the multivalued
	 *            fields in solr or to their single valued equivalents.
	 * @return the fields by type
	 */
	@Path("/searchable-fields")
	@GET
	public String getSearchableFields(@QueryParam("forType") String forType,
			@DefaultValue("false") @QueryParam("commonOnly") Boolean commonOnly,
			@DefaultValue("true") @QueryParam("multiValued") boolean multiValued) {

		JSONArray result = new JSONArray();
		Map<String, String> definitionFieldsByType = getFields(forType, commonOnly);
		Map<String, String> solrFields = getSolrFields();
		if (solrFields != null) {
			Set<String> addedProperties = new HashSet<>();
			Iterator<Entry<String, String>> iterator = definitionFieldsByType.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> property = iterator.next();
				String identifier = property.getKey();

				String solrType;
				if (multiValued) {
					solrType = solrFields.get(identifier);
				} else {
					solrType = solrFields.get("_sort_" + identifier);
				}
				if (solrType != null) {
					if (addedProperties.add(identifier)) {
						JSONObject jsonObj = new JSONObject();
						JsonUtil.addToJson(jsonObj, "id", identifier);
						JsonUtil.addToJson(jsonObj, "text", property.getValue());
						JsonUtil.addToJson(jsonObj, "type", solrType);
						result.put(jsonObj);
					}
				}
			}
		}
		return result.toString();
	}

	/**
	 * Returns the fields for specified types.
	 * 
	 * @param forType
	 *            Types to retrieve literals for (optional). Parent semantic type and definition sub
	 *            type must be delimited by underscore
	 * @param commonOnly
	 *            If the service should return the intersection of the properties.
	 * @return list with the definition fields by type. Returns map with field id and label
	 */
	// TODO: This method shouldn't be here.
	private Map<String, String> getFields(String forType, Boolean commonOnly) {
		Map<String, String> properties = new HashMap<>();
		if (StringUtils.isNotNull(forType)) {
			String[] split = forType.split(",");
			// Take each type's properties and do an intersection of them.
			for (String forTypeEntry : split) {
				forTypeEntry = forTypeEntry.trim();
				String semanticType = forTypeEntry;
				String definitionType = null;
				int lastDashPos = forTypeEntry.lastIndexOf("_");
				if (lastDashPos != -1) {
					semanticType = forTypeEntry.substring(0, lastDashPos);
					definitionType = forTypeEntry.substring(lastDashPos + 1);
				}
				if (commonOnly && !properties.isEmpty()) {
					properties.keySet().retainAll(
							getProperties(definitionType, semanticType).keySet());
				} else {
					properties.putAll(getProperties(definitionType, semanticType));
				}
			}
		} else {
			properties = getAllFields();
		}
		return properties;

	}

	/**
	 * Gets the definition fields. There are three combinations of parameters that can be passed to
	 * this method.
	 * <p>
	 * <b>Specified rdf type</b>, in which case, the fields will be retrieved from all definitions
	 * of the given rdf type.
	 * </p>
	 * <p>
	 * <b>Specified rdf type and definition</b>, in which case, the fields will be retrieved from
	 * the definition.
	 * </p>
	 * <p>
	 * <b>Specified rdf type and another child rdf type</b>, in which case, if the object is a
	 * domain object, the fields will be retrieved from the according definition, otherwise they
	 * will be retrieved from all definitions for the child type.
	 * </p>
	 * 
	 * @param id
	 *            the definition id
	 * @param rdfType
	 *            the rdf type
	 * @return the definition properties
	 */
	private Map<String, String> getProperties(String id, String rdfType) {
		Map<String, String> properties = new HashMap<>();

		// If there is only a semantic type specified (ptop:DomainObject_)
		if (StringUtils.isNullOrEmpty(id)) {
			DefinitionModel definition = semanticDefinitionMapping.get(rdfType);
			if (definition != null) {
				properties.putAll(getDefinitionFields(definition));
			} else {
				properties.putAll(getAllDefinitionsFields(rdfType));
			}
		} else {
			// If the id is also specified, try to get it's fields from the specified
			// definition. (ptop:DomainObject_EO10...)
			DataTypeDefinition typeDefinition = dictionaryService
					.getDataTypeDefinition(namespaceRegistryService.buildFullUri(rdfType));
			InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
					.getInstanceService(typeDefinition.getJavaClass());
			DefinitionModel definition = dictionaryService.getDefinition(
					instanceService.getInstanceDefinitionClass(), id);
			// If nothing is found, the id must also be semantic so we should get all
			// it's
			// definitions and get their fields.(ptop:DomainObject_chd:Book)
			if (definition == null) {
				// If the id is found in the semantic-definition mapping, it is a domain object and
				// we can get the matching definition.
				definition = semanticDefinitionMapping.get(id);
				if (definition == null) {
					// If not, get all it's definitions and return their fields.
					properties.putAll(getAllDefinitionsFields(id));
				} else {
					properties.putAll(getDefinitionFields(definition));
				}
			} else {
				properties.putAll(getDefinitionFields(definition));
			}

		}

		return properties;
	}

	/**
	 * Gets all fields from all definitions with the specified semantic type.
	 * 
	 * @param shortUri
	 *            the semantic type
	 * @return all definitions fields
	 */
	private Map<String, String> getAllDefinitionsFields(String shortUri) {
		Map<String, String> properties = new HashMap<>();
		if (StringUtils.isNotNullOrEmpty(shortUri)) {
			DataTypeDefinition typeDefinition = dictionaryService
					.getDataTypeDefinition(namespaceRegistryService.buildFullUri(shortUri));
			if (typeDefinition != null) {
				InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
						.getInstanceService(typeDefinition.getJavaClass());
				if (instanceService != null) {
					List<DefinitionModel> allDefinitions = dictionaryService
							.getAllDefinitions(instanceService.getInstanceDefinitionClass());
					for (DefinitionModel definition : allDefinitions) {
						properties.putAll(getDefinitionFields(definition));
					}
				}
			}
		}
		return properties;
	}

	/**
	 * Gets all fields from all definitions from all semantic objects.
	 * 
	 * @return the fields
	 */
	private Map<String, String> getAllFields() {
		Map<String, String> properties = new HashMap<>();
		List<ClassInstance> instances = semanticDefinitionService.getClasses();
		for (ClassInstance instance : instances) {
			properties.putAll(getAllDefinitionsFields(instance.getId().toString()));
		}
		return properties;
	}

	/**
	 * Gets the fields of a definition that are not hidden or system.
	 * 
	 * @param definition
	 *            the definition
	 * @return the definition fields
	 */
	private Map<String, String> getDefinitionFields(DefinitionModel definition) {
		Map<String, String> properties = new HashMap<>();
		if (definition != null) {
			List<PropertyDefinition> fields = definition.getFields();
			for (PropertyDefinition propertyDefinition : fields) {
				if (propertyDefinition.getLabel() != null
						&& !propertyDefinition.getDisplayType().equals(DisplayType.SYSTEM)) {
					properties.put(propertyDefinition.getIdentifier(),
							propertyDefinition.getLabel());
				}

			}
			// If this is a region definition, get it's regions and retrieve their fields.
			if (definition instanceof BaseRegionDefinition<?>) {
				List<RegionDefinition> regions = ((BaseRegionDefinition<?>) definition)
						.getRegions();
				for (RegionDefinition region : regions) {
					List<PropertyDefinition> regionFields = region.getFields();
					for (PropertyDefinition propertyDefinition : regionFields) {
						if (propertyDefinition.getLabel() != null
								&& !propertyDefinition.getDisplayType().equals(DisplayType.SYSTEM)) {
							properties.put(propertyDefinition.getIdentifier(),
									propertyDefinition.getLabel());
						}

					}
				}
			}
		}
		return properties;
	}

	/**
	 * Gets the solr fields.
	 * 
	 * @return the solr fields
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> getSolrFields() {
		try {
			SolrQuery parameters = new SolrQuery();
			parameters.setParam(CommonParams.QT, "/schema/fields");
			parameters.setParam(CommonParams.WT, "json");
			QueryResponse queryResponse = solrConnector.queryWithGet(parameters);
			Map<String, String> fields = new HashMap<>();
			List<SimpleOrderedMap<String>> fieldsList = (List<SimpleOrderedMap<String>>) queryResponse
					.getResponse().get("fields");

			for (SimpleOrderedMap<String> field : fieldsList) {
				fields.put(field.get("name"), field.get("type"));
			}
			return fields;
		} catch (SolrClientException e) {
			logger.error("Query client error: " + e.getMessage(), e);
		}
		return null;
	}
}