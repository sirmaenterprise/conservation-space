package com.sirma.seip.semantic.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.seip.semantic.management.ConnectorConfiguration.ConnectorField;

/**
 * Generator for create, delete, repair and initialize queries for Solr connector
 * 
 * @author kirq4e
 */
public class ConnectorQueryGenerator {

	private static final String DEFAULT_ENTITY_FILTER = "?isDeleted in (\\\\\\\"false\\\\\\\"^^xsd:boolean) && bound(?isDeleted)";

	private static final String DEFAULT_TYPES_FILTER = "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
			+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
			+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"";

	public static final String CONNECTOR_NAME = "CONNECTOR_NAME";

	public static final String DELETE_CONNECTOR_CONFIGURATION = "delete where {\n" + CONNECTOR_NAME
			+ " ?predicate ?object \n }";

	public static final String CHECK_SOLR_CONNECTOR = "SELECT ?cntUri ?cntStatus {\n solr-inst:" + CONNECTOR_NAME
			+ "  solr:connectorStatus ?cntStatus .\n}";
	public static final String DELETE_SOLR_CONNECTOR_INIT_DATA = "CLEAR GRAPH <CONNECTOR_GRAPH>";
	public static final Pattern EXTRACT_GRAPH_REGEX = Pattern.compile("graph\\s+.*>");

	public static final String CONNECTOR_PROPERTIES_BLOCK = ResourceLoadUtil.loadResource(ConnectorQueryGenerator.class,
			"connector_properties_block_template.tpl");

	public static final String CONNECTOR_CRATE_QUERY_TEMPLATE = "INSERT DATA {\nsolr-inst:" + CONNECTOR_NAME
			+ " solr:createConnector '''\n{FIELDS_BLOCK,\nPROPERTIES_BLOCK}\n''' .\n}";

	public static final String CONNECTOR_DROP_QUERY_TEMPLATE = "INSERT DATA {\nsolr-inst:" + CONNECTOR_NAME
			+ " solr:dropConnector \"\" .\n}";

	public static final String CONNECTOR_REPAIR_QUERY_TEMPLATE = "INSERT DATA {	solr-inst:" + CONNECTOR_NAME
			+ " solr:repairConnector \"\"}";

	public static final String QUERY_CONNECTOR_INSTANCE_PROPERTIES = ResourceLoadUtil
			.loadResource(ConnectorQueryGenerator.class, "queryConnectorProperties.sparql");

	public static final String QUERY_CONNECTOR_FIELDS = ResourceLoadUtil.loadResource(ConnectorQueryGenerator.class,
			"queryConnectorFields.sparql");

	public static final String QUERY_DEFAULT_CONNECTOR_FIELDS = ResourceLoadUtil
			.loadResource(ConnectorQueryGenerator.class, "queryDefaultConnectorFields.sparql");

	public static final String QUERY_CONNECTOR_INSTANCES = ResourceLoadUtil.loadResource(ConnectorQueryGenerator.class,
			"queryConnectorInstances.sparql");

	public static final String QUERY_INITIALIZE_CONNECTOR_SCHEME_TEMPLATE = ResourceLoadUtil
			.loadResource(ConnectorQueryGenerator.class, "connector_scheme_initialization_template.sparql");
	

	private ConnectorQueryGenerator() {
	}
	/**
	 * Generates create SPARQL query for the connector
	 * 
	 * @return Create query for the connector
	 */
	public static String generateCreateQuery(ConnectorConfiguration configuration) {
		String connectorName = configuration.getConnectorName();

		String createQuery = CONNECTOR_CRATE_QUERY_TEMPLATE.replace(CONNECTOR_NAME, connectorName);
		createQuery = createQuery.replace("FIELDS_BLOCK", generateFields(configuration));
		createQuery = createQuery.replace("PROPERTIES_BLOCK", generateConnectorProperties(configuration));
		return createQuery;
	}

	private static String generateFields(ConnectorConfiguration configuration) {
		return configuration
				.getFields()
					.values()
					.stream()
					.map(ConnectorQueryGenerator::generateFieldDescription)
					.collect(Collectors.joining(",", "\"fields\": [", "]"));
	}

	private static String generateFieldDescription(ConnectorField field) {
		int counter = 1;
		int size = field.getDescriptions().size();
		StringBuilder tmpFieldDescription = new StringBuilder(size * 256);
		for (String fieldDescription : field.getDescriptions()) {
			tmpFieldDescription.append("{\n");
			if (!fieldDescription.contains("fieldName")) {
				tmpFieldDescription.append("\"fieldName\": \"").append(field.getId().getLocalName());

				if (size > 1) {
					tmpFieldDescription.append("/").append(counter);
				}
				tmpFieldDescription.append("\",\n");
			}

			if (!fieldDescription.contains("propertyChain")) {
				tmpFieldDescription.append("\"propertyChain\": [ \"").append(field.getId().toString()).append(
						"\" ],\n");
			}
			tmpFieldDescription.append(fieldDescription).append("}");
			if (counter < size) {
				tmpFieldDescription.append(", \n");
			}
			counter++;
		}

		return tmpFieldDescription.toString();
	}

	/**
	 * Generates properties block of the connector with the defined types, filters, solr address and etc. From the
	 * passed connector configuration are taken all properties or default values and are filled in the template
	 * connector_properties_block_template.tpl
	 * 
	 * <pre>
	"types": [
	"http://www.ontotext.com/proton/protontop#Happening",
	"http://www.ontotext.com/proton/protontop#Object",
	"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription"
	],
	"entityFilter": "?isDeleted in (\\\"false\\\"^^xsd:boolean) && bound(?isDeleted)",
	"solrUrl": "http://localhost:8983/solr",
	"copyConfigsFrom": "collection1",
	"solrCore": "ftsearch",
	"manageCore": true,
	"manageSchema": true
	 * </pre>
	 * 
	 * @param configuration
	 *            Connector configuration
	 * @return Initialized properties block of the connector
	 */
	private static String generateConnectorProperties(ConnectorConfiguration configuration) {
		String connectorProperties = CONNECTOR_PROPERTIES_BLOCK.replace("TYPES", DEFAULT_TYPES_FILTER);
		connectorProperties = connectorProperties.replace("ENTITY_FILTER",
				configuration.get("entityFilter", DEFAULT_ENTITY_FILTER).toString());
		connectorProperties = connectorProperties.replace("SOLR_ADDRESS", configuration.getAddress());
		connectorProperties = connectorProperties.replace("COPY_CONFIGS_FROM",
				configuration.get("copyConfigsFrom", "collection1").toString());
		connectorProperties = connectorProperties.replace("CORE_NAME", configuration.getConnectorName());
		connectorProperties = connectorProperties.replace("MANAGE_CORE",
				configuration.get("manageCore", "true").toString());
		connectorProperties = connectorProperties.replace("MANAGE_SCHEMA",
				configuration.get("manageSchema", "true").toString());

		return connectorProperties;
	}

	/**
	 * Generates initialization query for the connector
	 * 
	 * @return Create query for the initialization of the connector
	 */
	public static String generateInitializationQuery(ConnectorConfiguration configuration) {
		StringBuilder builder = new StringBuilder();

		for (ConnectorField field : configuration.getFields().values()) {
			String type = field.getType();

			String initialValue = "";

			switch (type) {
				case "relation":
					initialValue = "emf:dummyInstance";
					break;
				case "xsd:boolean":
				case "native:booleans":
					initialValue = "\"true\"^^xsd:boolean";
					break;

				case "xsd:dateTime":
				case "xsd:dateTimeStamp":
					initialValue = "\"2017-11-20T14:43:10.521Z\"^^xsd:dateTime";
					break;

				case "xsd:nonNegativeInteger":
				case "xsd:int":
				case "xsd:integer":
				case "xsd:long":
					initialValue = "\"123\"^^" + type;
					break;

				case "xsd:double":
				case "xsd:float":
					initialValue = "\"12.345\"^^" + type;
					break;

				case "xsd:string":
				case "native:text_tokenized":
				default:
					initialValue = "\"initial_data\"^^xsd:string";
					break;

			}

			// skip initialization of the field if it is of unknown type
			if (!StringUtils.isEmpty(initialValue)) {
				String fieldInitialization = "<" + field.getId().toString() + ">" + " " + initialValue + " ;\n";
				builder.append(fieldInitialization);
			}
		}

		return QUERY_INITIALIZE_CONNECTOR_SCHEME_TEMPLATE.replace("$CONNECTOR_FIELDS$", builder.toString());
	}

	public static String generateDropQuery(String connectorName) {
		return CONNECTOR_DROP_QUERY_TEMPLATE.replace(CONNECTOR_NAME, connectorName);
	}

	public static String generateDeleteConfigurationQuery(String connectorName) {
		return DELETE_CONNECTOR_CONFIGURATION.replace(CONNECTOR_NAME, connectorName);
	}

	public static String genereateRepairQuery(String connectorName) {
		return CONNECTOR_REPAIR_QUERY_TEMPLATE.replace(CONNECTOR_NAME, connectorName);
	}

	public static String generateDeleteInitializationData(String initialConnectorData) {
		Matcher m = EXTRACT_GRAPH_REGEX.matcher(initialConnectorData);
		if (m.find()) {
			String graph = m.group();
			graph = graph.substring(graph.indexOf('<') + 1, graph.indexOf('>'));
			return DELETE_SOLR_CONNECTOR_INIT_DATA.replace("CONNECTOR_GRAPH", graph);
		}
		return "";
	}

	public static String generateExistsConnectorQuery(String connectorName) {
		return CHECK_SOLR_CONNECTOR.replace(CONNECTOR_NAME, connectorName);
	}

}
