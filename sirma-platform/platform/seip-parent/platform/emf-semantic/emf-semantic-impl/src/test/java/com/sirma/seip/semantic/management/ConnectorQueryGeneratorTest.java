package com.sirma.seip.semantic.management;

import static com.sirma.seip.semantic.management.ConnectorQueryGenerator.CONNECTOR_NAME;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.testng.Assert;

import com.sirma.itt.emf.semantic.model.Rdf4JStringUriProxy;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.seip.semantic.management.ConnectorConfiguration.ConnectorField;

/**
 * @author kirq4e
 */
public class ConnectorQueryGeneratorTest {

	private static final String TEST_CONNECTOR_NAME = "test";

	@Test
	public void testGenerateInitializationQueryNoFields() {
		String queryTemplate = ConnectorQueryGenerator.QUERY_INITIALIZE_CONNECTOR_SCHEME_TEMPLATE;

		ConnectorConfiguration configuration = new ConnectorConfiguration();

		String generatedQuery = ConnectorQueryGenerator.generateInitializationQuery(configuration);
		Assert.assertEquals(generatedQuery, queryTemplate.replace("$CONNECTOR_FIELDS$", ""));
	}

	@Test
	public void testGenerateInitializationQueryOneTextField() {
		String queryTemplate = ConnectorQueryGenerator.QUERY_INITIALIZE_CONNECTOR_SCHEME_TEMPLATE;

		ConnectorConfiguration configuration = new ConnectorConfiguration();
		generateField(configuration, "type", "native:text_tokenized", false);

		String generatedQuery = ConnectorQueryGenerator.generateInitializationQuery(configuration);
		Assert.assertEquals(generatedQuery, queryTemplate.replace("$CONNECTOR_FIELDS$",
				"<" + EMF.NAMESPACE + "type> \"initial_data\"^^xsd:string ;\n"));
	}

	@Test
	public void testGenerateInitializationQueryAllFields() {
		ConnectorConfiguration configuration = new ConnectorConfiguration();
		generateField(configuration, "type", "native:text_tokenized", false);
		generateField(configuration, "isDeleted", "native:booleans", false);
		generateField(configuration, "isDeleted2", "xsd:boolean", false);
		generateField(configuration, "text", "native:text_tokenized", false);
		generateField(configuration, "text2", "", false);
		generateField(configuration, "dateTime", "xsd:dateTime", false);
		generateField(configuration, "dateTimeStamp", "xsd:dateTimeStamp", false);
		generateField(configuration, "nonNegativeInteger", "xsd:nonNegativeInteger", false);
		generateField(configuration, "int", "xsd:int", false);
		generateField(configuration, "integer", "xsd:integer", false);
		generateField(configuration, "long", "xsd:long", false);
		generateField(configuration, "double", "xsd:double", false);
		generateField(configuration, "float", "xsd:float", false);
		generateField(configuration, "parentOf", "relation", false);

		String generatedQuery = ConnectorQueryGenerator.generateInitializationQuery(configuration);
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "type> \"initial_data\"^^xsd:string ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "isDeleted> \"true\"^^xsd:boolean ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "isDeleted2> \"true\"^^xsd:boolean ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "text> \"initial_data\"^^xsd:string ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "text2> \"initial_data\"^^xsd:string ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "dateTime> \"2017-11-20T14:43:10.521Z\"^^xsd:dateTime ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "dateTimeStamp> \"2017-11-20T14:43:10.521Z\"^^xsd:dateTime ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "nonNegativeInteger> \"123\"^^xsd:nonNegativeInteger ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "int> \"123\"^^xsd:int ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "integer> \"123\"^^xsd:integer ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "long> \"123\"^^xsd:long ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "double> \"12.345\"^^xsd:double ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "float> \"12.345\"^^xsd:float ;"));
		Assert.assertTrue(generatedQuery.contains("<" + EMF.NAMESPACE + "parentOf> emf:dummyInstance ;"));
	}

	@Test
	public void testGenerateCreateQuery() {
		ConnectorConfiguration configuration = new ConnectorConfiguration();
		configuration.setConnectorName("testConnector");
		configuration.setAddress("http://localhost:8983/solr");

		generateField(configuration, "type", "native:text_tokenized", false);
		generateField(configuration, "isDeleted", "native:booleans", false);
		generateField(configuration, "isDeleted2", "xsd:boolean", false);
		generateField(configuration, "text", "string", false);
		generateField(configuration, "parentOf", "relation", false);

		String createQuery = ConnectorQueryGenerator.generateCreateQuery(configuration);
		Assert.assertTrue(StringUtils.isNoneEmpty(createQuery));
		Assert.assertEquals(createQuery, "INSERT DATA {\n" + "solr-inst:testConnector solr:createConnector '''\n"
				+ "{\"fields\": [{\n" + "\"fieldName\": \"isDeleted2\",\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isDeleted2\" ],\n"
				+ "\"datatype\": \"xsd:boolean\",\n" + "\"analyzed\": false,\n" + "\"multivalued\": false\n" + "},{\n"
				+ "\"fieldName\": \"parentOf\",\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#parentOf\" ],\n"
				+ "\"datatype\": \"relation\",\n" + "\"analyzed\": false,\n" + "\"multivalued\": false\n" + "},{\n"
				+ "\"fieldName\": \"text\",\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#text\" ],\n"
				+ "\"datatype\": \"string\",\n" + "\"analyzed\": false,\n" + "\"multivalued\": false\n" + "},{\n"
				+ "\"fieldName\": \"type\",\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#type\" ],\n"
				+ "\"datatype\": \"native:text_tokenized\",\n" + "\"analyzed\": false,\n" + "\"multivalued\": false\n"
				+ "},{\n" + "\"fieldName\": \"isDeleted\",\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isDeleted\" ],\n"
				+ "\"datatype\": \"native:booleans\",\n" + "\"analyzed\": false,\n" + "\"multivalued\": false\n"
				+ "}],\n" + "\"types\": [\n" + "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
				+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
				+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"\n" + "],\n"
				+ "\"entityFilter\": \"?isDeleted in (\\\\\\\"false\\\\\\\"^^xsd:boolean) && bound(?isDeleted)\",\n"
				+ "\"solrUrl\": \"http://localhost:8983/solr\",\n" + "\"copyConfigsFrom\": \"collection1\",\n"
				+ "\"solrCore\": \"testConnector\",\n" + "\"manageCore\": true,\n" + "\"manageSchema\": true}\n"
				+ "''' .\n" + "}");
	}

	/**
	 * Tests generation of create solr connector query with field that contains that contains single field
	 */
	@Test
	public void testGenerationWithSingleField() {
		ConnectorConfiguration config = buildConnectorConfiguration();

		generateField(config, TEST_CONNECTOR_NAME, "string", false);

		Assert.assertEquals(ConnectorQueryGenerator.generateCreateQuery(config), "INSERT DATA {\n"
				+ "solr-inst:test solr:createConnector '''\n" + "{\"fields\": [{\n" + "\"fieldName\": \"test\",\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#test\" ],\n"
				+ "\"datatype\": \"string\",\n" + "\"analyzed\": false,\n" + "\"multivalued\": false\n" + "}],\n"
				+ "\"types\": [\n" + "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
				+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
				+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"\n" + "],\n"
				+ "\"entityFilter\": \"entityFilter\",\n" + "\"solrUrl\": \"http://localhost:8983/solr\",\n"
				+ "\"copyConfigsFrom\": \"collection1\",\n" + "\"solrCore\": \"test\",\n"
				+ "\"manageCore\": manageCore,\n" + "\"manageSchema\": true}\n" + "''' .\n" + "}");
	}

	/**
	 * Tests generation of create solr connector query with field that contains multiple field descriptions
	 */
	@Test
	public void testGenerationForMultipleChanins() {
		ConnectorConfiguration config = buildConnectorConfiguration();

		generateField(config, "assignee",
				"\"propertyChain\": [\n"
						+ "    \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\",\n"
						+ "    \"http://www.ontotext.com/proton/protontop#hasMember\"\n" + "  ],\n"
						+ "\"analyzed\": false,\n" + "\"multivalued\": true",
				"\"propertyChain\": [\n"
						+ "    \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\"\n"
						+ "  ],\n" + "\"analyzed\": false,\n" + "\"multivalued\": true");

		Assert.assertEquals(ConnectorQueryGenerator.generateCreateQuery(config), "INSERT DATA {\n"
				+ "solr-inst:test solr:createConnector '''\n" + "{\"fields\": [{\n" + "\"fieldName\": \"assignee/1\",\n"
				+ "\"propertyChain\": [\n"
				+ "    \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\",\n"
				+ "    \"http://www.ontotext.com/proton/protontop#hasMember\"\n" + "  ],\n" + "\"analyzed\": false,\n"
				+ "\"multivalued\": true}, \n" + "{\n" + "\"fieldName\": \"assignee/2\",\n" + "\"propertyChain\": [\n"
				+ "    \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\"\n" + "  ],\n"
				+ "\"analyzed\": false,\n" + "\"multivalued\": true}],\n" + "\"types\": [\n"
				+ "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
				+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
				+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"\n" + "],\n"
				+ "\"entityFilter\": \"entityFilter\",\n" + "\"solrUrl\": \"http://localhost:8983/solr\",\n"
				+ "\"copyConfigsFrom\": \"collection1\",\n" + "\"solrCore\": \"test\",\n"
				+ "\"manageCore\": manageCore,\n" + "\"manageSchema\": true}\n" + "''' .\n" + "}");
	}

	/**
	 * Tests generation of create solr connector query with field that contains field name in its field description
	 */
	@Test
	public void testGenerationFieldWithFieldName() {
		ConnectorConfiguration config = buildConnectorConfiguration();

		generateField(config, TEST_CONNECTOR_NAME, "\"fieldName\": \"anotherName\",");

		Assert.assertEquals(ConnectorQueryGenerator.generateCreateQuery(config), "INSERT DATA {\n"
				+ "solr-inst:test solr:createConnector '''\n" + "{\"fields\": [{\n"
				+ "\"propertyChain\": [ \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#test\" ],\n"
				+ "\"fieldName\": \"anotherName\",}],\n" + "\"types\": [\n"
				+ "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
				+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
				+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"\n" + "],\n"
				+ "\"entityFilter\": \"entityFilter\",\n" + "\"solrUrl\": \"http://localhost:8983/solr\",\n"
				+ "\"copyConfigsFrom\": \"collection1\",\n" + "\"solrCore\": \"test\",\n"
				+ "\"manageCore\": manageCore,\n" + "\"manageSchema\": true}\n" + "''' .\n" + "}");
	}

	/**
	 * Tests generation of create solr connector query with field that contains property chain in its description
	 */
	@Test
	public void testGenerationFieldWithPropertyChain() {
		ConnectorConfiguration config = buildConnectorConfiguration();

		generateField(config, TEST_CONNECTOR_NAME,
				"\"propertyChain\": [\n"
						+ "\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\",\n"
						+ "\"http://www.ontotext.com/proton/protontop#hasMember\"\n"
						+ "],\"analyzed\": false,\n\"multivalued\": true");

		Assert.assertEquals(ConnectorQueryGenerator.generateCreateQuery(config),
				"INSERT DATA {\n" + "solr-inst:test solr:createConnector '''\n" + "{\"fields\": [{\n"
						+ "\"fieldName\": \"test\",\n" + "\"propertyChain\": [\n"
						+ "\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\",\n"
						+ "\"http://www.ontotext.com/proton/protontop#hasMember\"\n" + "],\"analyzed\": false,\n"
						+ "\"multivalued\": true}],\n" + "\"types\": [\n"
						+ "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
						+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
						+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"\n"
						+ "],\n" + "\"entityFilter\": \"entityFilter\",\n"
						+ "\"solrUrl\": \"http://localhost:8983/solr\",\n" + "\"copyConfigsFrom\": \"collection1\",\n"
						+ "\"solrCore\": \"test\",\n" + "\"manageCore\": manageCore,\n" + "\"manageSchema\": true}\n"
						+ "''' .\n" + "}");
	}

	/**
	 * Tests generation of create solr connector query with field that contains property chain and field name in its
	 * description
	 */
	@Test
	public void testGenerationFieldWithPropertyChainAndFieldName() {
		ConnectorConfiguration config = buildConnectorConfiguration();

		generateField(config, TEST_CONNECTOR_NAME,
				"\"fieldName\": \"assignee/1\",\"propertyChain\": [\n"
						+ "\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\",\n"
						+ "\"http://www.ontotext.com/proton/protontop#hasMember\"\n"
						+ "],\"analyzed\": false,\n\"multivalued\": true");

		Assert.assertEquals(ConnectorQueryGenerator.generateCreateQuery(config),
				"INSERT DATA {\n" + "solr-inst:test solr:createConnector '''\n" + "{\"fields\": [{\n"
						+ "\"fieldName\": \"assignee/1\",\"propertyChain\": [\n"
						+ "\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasAssignee\",\n"
						+ "\"http://www.ontotext.com/proton/protontop#hasMember\"\n" + "],\"analyzed\": false,\n"
						+ "\"multivalued\": true}],\n" + "\"types\": [\n"
						+ "\t\"http://www.ontotext.com/proton/protontop#Happening\",\n"
						+ "\t\"http://www.ontotext.com/proton/protontop#Object\",\n"
						+ "\t\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription\"\n"
						+ "],\n" + "\"entityFilter\": \"entityFilter\",\n"
						+ "\"solrUrl\": \"http://localhost:8983/solr\",\n" + "\"copyConfigsFrom\": \"collection1\",\n"
						+ "\"solrCore\": \"test\",\n" + "\"manageCore\": manageCore,\n" + "\"manageSchema\": true}\n"
						+ "''' .\n" + "}");
	}

	@Test
	public void testGenerateDropQuery() {
		String generatedDropQuery = ConnectorQueryGenerator.generateDropQuery(TEST_CONNECTOR_NAME);

		Assert.assertEquals(generatedDropQuery,
				ConnectorQueryGenerator.CONNECTOR_DROP_QUERY_TEMPLATE.replace(CONNECTOR_NAME, TEST_CONNECTOR_NAME));
	}

	@Test
	public void testGenerateDeleteConfigurationQuery() {
		String generatedDeleteConfigurationQuery = ConnectorQueryGenerator
				.generateDeleteConfigurationQuery(TEST_CONNECTOR_NAME);

		Assert.assertEquals(generatedDeleteConfigurationQuery,
				ConnectorQueryGenerator.DELETE_CONNECTOR_CONFIGURATION.replace(CONNECTOR_NAME, TEST_CONNECTOR_NAME));
	}

	@Test
	public void testGenereateRepairQuery() {
		String generatedRepairQuery = ConnectorQueryGenerator.genereateRepairQuery(TEST_CONNECTOR_NAME);

		Assert.assertEquals(generatedRepairQuery,
				ConnectorQueryGenerator.CONNECTOR_REPAIR_QUERY_TEMPLATE.replace(CONNECTOR_NAME, TEST_CONNECTOR_NAME));
	}

	@Test
	public void testGenerateDeleteInitializationData() {
		ConnectorConfiguration configuration = buildConnectorConfiguration();
		generateField(configuration, "type", "native:text_tokenized", false);
		generateField(configuration, "isDeleted", "native:booleans", false);
		generateField(configuration, "isDeleted2", "xsd:boolean", false);
		generateField(configuration, "text", "string", false);
		generateField(configuration, "parentOf", "relation", false);

		String initializationQuery = ConnectorQueryGenerator.generateInitializationQuery(configuration);

		String generatedDeleteInitDataQuery = ConnectorQueryGenerator
				.generateDeleteInitializationData(initializationQuery);

		Assert.assertEquals(generatedDeleteInitDataQuery, ConnectorQueryGenerator.DELETE_SOLR_CONNECTOR_INIT_DATA
				.replace("CONNECTOR_GRAPH", "http://ittruse.ittbg.com/data/enterpriseManagementFramework/init-solr"));
	}

	@Test
	public void testGenerateExistsConnectorQuery() {
		String query = ConnectorQueryGenerator.generateExistsConnectorQuery(TEST_CONNECTOR_NAME);

		Assert.assertEquals(query,
				ConnectorQueryGenerator.CHECK_SOLR_CONNECTOR.replace(CONNECTOR_NAME, TEST_CONNECTOR_NAME));
	}

	public static ConnectorConfiguration buildConnectorConfiguration() {
		ConnectorConfiguration config = new ConnectorConfiguration();

		config.setAddress("http://localhost:8983/solr");
		config.setConnectorName(TEST_CONNECTOR_NAME);
		config.setId(TEST_CONNECTOR_NAME);

		config.add("copyConfigsFrom", "collection1");
		config.add("entityFilter", "entityFilter");
		config.add("manageCore", "manageCore");
		config.add("manageSchetypesma", "manageSchetypesma");
		return config;
	}

	private static void generateField(ConnectorConfiguration configuration, String id, String type,
			boolean isSortable) {
		ConnectorField field = new ConnectorField();
		field.setId(new Rdf4JStringUriProxy(EMF.NAMESPACE + id));
		field.setType(type);
		field.setIsSortable(isSortable);

		String description = "";
		if (StringUtils.isNotEmpty(type)) {
			description = "\"datatype\": \"" + type + "\",\n";
		}

		description = description + "\"analyzed\": false,\n" + "\"multivalued\": false\n";
		field.addDescription(description);

		configuration.getFields().put(id, field);
	}

	private static void generateField(ConnectorConfiguration configuration, String id, String... descriptions) {
		ConnectorField field = new ConnectorField();
		field.setId(new Rdf4JStringUriProxy(EMF.NAMESPACE + id));

		for (String description : descriptions) {
			field.addDescription(description);
		}

		configuration.getFields().put(id, field);
	}

}
