package com.sirma.itt.seip.search;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * The Class DefaultSearchFilterProviderTest.
 *
 * @author BBonev
 */
@Test
public class DefaultSearchFilterProviderTest extends EmfTest {

	@InjectMocks
	private DefaultSearchFilterProvider filterProvider;

	@Mock
	private DefinitionService definitionService;

	@Spy
	private TypeConverter typeConverter;

	@Mock
	private SearchConfiguration searchConfiguration;

	@Spy
	private InstanceProxyMock<SearchService> searchServiceProxy = new InstanceProxyMock<>(null);

	@Mock
	SearchService searchService;

	DefinitionMock definition;

	PropertyDefinitionMock field;

	ControlDefintionMock control;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		typeConverter = createTypeConverter();
		super.beforeMethod();
		definition = new DefinitionMock();
		definition.setIdentifier("testDefinition");
		field = new PropertyDefinitionMock();
		field.setIdentifier("testQuery");
		definition.getFields().add(field);
		control = new ControlDefintionMock();
		control.setIdentifier("query");
		field.setControlDefinition(control);
		when(definitionService.find(anyString())).thenReturn(definition);

		searchServiceProxy.set(searchService);
		when(searchService.escapeForDialect(anyString())).thenReturn(s -> s);
	}

	public void testParameterBuiding_param_defaultValue() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		control.getControlParams().add(buildParam("config", "dialect", "SOLR"));
		control.getControlParams().add(buildParam("queryParam", "paramName", "defaultValue"));

		Context<String, Object> context = new Context<>();
		SearchArguments<?> arguments = filterProvider.buildSearchArguments("testDefinition/testQuery", context);
		assertNotNull(arguments);
		assertEquals(arguments.getStringQuery(), "content:(\"*defaultValue*\")");
	}

	public void testParameterBuiding_andBindings_param_defaultValue() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		control.getControlParams().add(buildParam("config", "dialect", "SOLR"));
		control.getControlParams().add(buildParam("queryParam", "paramName", "defaultValue"));
		control.getControlParams().add(buildParam("binding", "bindingName", "bindingValue"));
		control.getControlParams().add(buildParam("binding", "copyValue", "bindingValue2"));
		control.getControlParams().add(buildParam("binding", "systemTime", null));

		Context<String, Object> context = new Context<>();
		context.put("copyValue", "copiedValue");
		SearchArguments<?> arguments = filterProvider.buildSearchArguments("testDefinition/testQuery", context);
		assertNotNull(arguments);
		assertEquals(arguments.getStringQuery(), "content:(\"*defaultValue*\")");
		assertNotNull(arguments.getArguments().get("systemTime"));
		assertEquals(arguments.getArguments().get("bindingName"), "bindingValue");
		assertEquals(arguments.getArguments().get("copyValue"), "copiedValue");
	}

	public void testParameterBuiding_param_userValue() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		control.getControlParams().add(buildParam("config", "dialect", "SOLR"));
		control.getControlParams().add(buildParam("queryParam", "paramName", "defaultValue"));

		Context<String, Object> context = new Context<>();
		context.put("paramName", "userValue");
		SearchArguments<?> arguments = filterProvider.buildSearchArguments("testDefinition/testQuery", context);
		assertNotNull(arguments);
		assertEquals(arguments.getStringQuery(), "content:(\"*userValue*\")");
	}

	public void test_getFilterConfiguration_sortFields() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		field.getControlDefinition().setIdentifier("sortFields");

		PropertyDefinitionMock sortField = new PropertyDefinitionMock();
		sortField.setDisplayType(DisplayType.EDITABLE);
		sortField.setName("sortBy");
		control.getFields().add(sortField);

		Context<String, Object> context = new Context<>();
		context.put("paramName", "userValue");
		SearchFilterConfig filterConfig = filterProvider.getFilterConfiguration("testDefinition/testQuery");
		assertNotNull(filterConfig);
		assertFalse(filterConfig.getSorterFields().isEmpty());
	}

	public void test_getFilterConfiguration_dashletFilter() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		field.getControlDefinition().setIdentifier("dashletFilter");

		PropertyDefinitionMock sortField = new PropertyDefinitionMock();
		sortField.setDisplayType(DisplayType.EDITABLE);
		sortField.setName("sortBy");
		control.getFields().add(sortField);

		Context<String, Object> context = new Context<>();
		context.put("paramName", "userValue");
		SearchFilterConfig filterConfig = filterProvider.getFilterConfiguration("testDefinition");
		assertNotNull(filterConfig);
		assertFalse(filterConfig.getFilters().isEmpty());
	}

	public void test_getFilterConfiguration_query() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		field.getControlDefinition().setIdentifier("query");

		PropertyDefinitionMock sortField = new PropertyDefinitionMock();
		sortField.setDisplayType(DisplayType.EDITABLE);
		sortField.setName("sortBy");
		control.getFields().add(sortField);

		Context<String, Object> context = new Context<>();
		context.put("paramName", "userValue");
		SearchFilterConfig filterConfig = filterProvider.getFilterConfiguration("testDefinition");
		assertNotNull(filterConfig);
		assertFalse(filterConfig.getFilters().isEmpty());
	}

	public void testParameterBuiding_param_noValue() {
		String query = "content:(\"*{paramName}*\")";
		field.setValue(query);
		control.getControlParams().add(buildParam("config", "dialect", "SOLR"));
		control.getControlParams().add(buildParam("queryParam", "paramName", null));

		Context<String, Object> context = new Context<>();
		SearchArguments<?> arguments = filterProvider.buildSearchArguments("testDefinition/testQuery", context);
		assertNotNull(arguments);
		assertEquals(arguments.getStringQuery(), "content:(\"**\")");
	}

	/**
	 * Test parameter buiding_param_default value for SPARQL query.
	 */
	public void testSPARQLParameterBuiding_param_defaultValue() {
		String query = "PREFIX emf: <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#>\r\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX ptop: <http://www.ontotext.com/proton/protontop#>\r\n"
				+ "PREFIX solr-inst: <http://www.ontotext.com/connectors/solr/instance#>\r\n"
				+ "PREFIX solr: <http://www.ontotext.com/connectors/solr#>\r\n"
				+ "PREFIX sec: <http://www.sirma.com/ontologies/2014/11/security#>\r\n"
				+ "PREFIX onto: <http://www.ontotext.com/>\r\n" + "SELECT DISTINCT ?instance ?instanceType  WHERE {\r\n"
				+ "    ?instance a emf:DomainObject .\r\n" + "    \r\n"
				+ "    emf:DomainObject emf:definitionId ?instanceType .\r\n" + "    \r\n"
				+ "    ?instance emf:isDeleted \"false\"^^xsd:boolean .\r\n"
				+ "    ?instance emf:modifiedOn ?sortVariable .\r\n" + "    \r\n"
				+ "    ?parent emf:hasChild ?instance  .\r\n" + "    ?parent emf:isDeleted \"false\"^^xsd:boolean .\r\n"
				+ "    \r\n" + "    ?parent a emf:Activity .\r\n" + "    {\r\n"
				+ "        ?parent emf:createdBy {userValue} .\r\n" + "    } UNION {\r\n"
				+ "        ?parent emf:hasAssignee {userValue} .\r\n" + "    } UNION {\r\n"
				+ "        ?parent emf:hasAssignee ?group .\r\n" + "        ?group ptop:hasMember {userValue} .\r\n"
				+ "    }\r\n" + "    \r\n" + "    OPTIONAL {\r\n" + "        ?parent emf:status \"COMPLETED\" . \r\n"
				+ "        ?parent emf:isDeleted ?statusCheck1 .\r\n" + "    }\r\n"
				+ "    filter (!bound(?statusCheck1))\r\n" + "    \r\n" + "    OPTIONAL {\r\n"
				+ "        ?parent emf:status \"STOPPED\" . \r\n" + "        ?parent emf:isDeleted ?statusCheck3 .\r\n"
				+ "    }\r\n" + "    filter (!bound(?statusCheck3))\r\n" + "    \r\n" + "    {\r\n"
				+ "        OPTIONAL  {\r\n" + "            {currentUser} sec:noPermission ?instance .\r\n"
				+ "            {currentUser} emf:isDeleted ?check1  .\r\n" + "        } \r\n"
				+ "        FILTER (  ! BOUND (  ?check1  )  )  .\r\n" + "        OPTIONAL  {\r\n"
				+ "            {currentUser} sec:isManagerOf ?instance .\r\n"
				+ "            {currentUser} emf:isDeleted ?check2  .\r\n" + "        } \r\n"
				+ "        FILTER (  ! BOUND (  ?check2  )  )  .\r\n" + "        {\r\n" + "            {\r\n"
				+ "                {currentUser} sec:canRead ?instance\r\n" + "            }  UNION  {\r\n"
				+ "                {currentUser} emf:isMemberOf ?group  .\r\n"
				+ "                ?group sec:canRead ?instance\r\n" + "            }  UNION  {\r\n"
				+ "                sec:SYSTEM_ALL_OTHER_USERS sec:canRead ?instance .\r\n"
				+ "                OPTIONAL  {\r\n"
				+ "                    {currentUser} sec:hasPermission ?instance .\r\n"
				+ "                    {currentUser} emf:isDeleted ?check3  .\r\n" + "                } \r\n"
				+ "                FILTER (  ! BOUND (  ?check3  )  )  .\r\n" + "                OPTIONAL  {\r\n"
				+ "                    {currentUser} emf:isMemberOf ?group  .\r\n"
				+ "                    ?group sec:hasPermission ?instance .\r\n"
				+ "                    {currentUser} emf:isDeleted ?check4  .\r\n" + "                } \r\n"
				+ "                FILTER (  ! BOUND (  ?check4  )  )  .\r\n" + "            }\r\n" + "        }\r\n"
				+ "    }\r\n" + "}\r\n" + "order by ASC(?sortVariable)\r\n" + "LIMIT 1000\r\n" + "\r\n" + "";
		field.setValue(query);
		control.getControlParams().add(buildParam("config", "dialect", "SPARQL"));
		control.getControlParams().add(buildParam("config", "maxSize", "1000"));
		control.getControlParams().add(buildParam("queryParam", "userValue", "emf:danielatodorova"));
		control.getControlParams().add(buildParam("queryParam", "currentUser", "emf:danielatodorova"));

		Context<String, Object> context = new Context<>();
		SearchArguments<?> arguments = filterProvider.buildSearchArguments("testDefinition/testQuery", context);

		assertEquals(arguments.getDialect(), SearchDialects.SPARQL);
		assertEquals(arguments.getMaxSize(), 1000);
		assertNotNull(arguments);
		assertEquals(arguments.getStringQuery(),
				"PREFIX emf: <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#>\r\n"
						+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX ptop: <http://www.ontotext.com/proton/protontop#>\r\n"
						+ "PREFIX solr-inst: <http://www.ontotext.com/connectors/solr/instance#>\r\n"
						+ "PREFIX solr: <http://www.ontotext.com/connectors/solr#>\r\n"
						+ "PREFIX sec: <http://www.sirma.com/ontologies/2014/11/security#>\r\n"
						+ "PREFIX onto: <http://www.ontotext.com/>\r\n"
						+ "SELECT DISTINCT ?instance ?instanceType  WHERE {\r\n"
						+ "    ?instance a emf:DomainObject .\r\n" + "    \r\n"
						+ "    emf:DomainObject emf:definitionId ?instanceType .\r\n" + "    \r\n"
						+ "    ?instance emf:isDeleted \"false\"^^xsd:boolean .\r\n"
						+ "    ?instance emf:modifiedOn ?sortVariable .\r\n" + "    \r\n"
						+ "    ?parent emf:hasChild ?instance  .\r\n"
						+ "    ?parent emf:isDeleted \"false\"^^xsd:boolean .\r\n" + "    \r\n"
						+ "    ?parent a emf:Activity .\r\n" + "    {\r\n"
						+ "        ?parent emf:createdBy emf:danielatodorova .\r\n" + "    } UNION {\r\n"
						+ "        ?parent emf:hasAssignee emf:danielatodorova .\r\n" + "    } UNION {\r\n"
						+ "        ?parent emf:hasAssignee ?group .\r\n"
						+ "        ?group ptop:hasMember emf:danielatodorova .\r\n" + "    }\r\n" + "    \r\n"
						+ "    OPTIONAL {\r\n" + "        ?parent emf:status \"COMPLETED\" . \r\n"
						+ "        ?parent emf:isDeleted ?statusCheck1 .\r\n" + "    }\r\n"
						+ "    filter (!bound(?statusCheck1))\r\n" + "    \r\n" + "    OPTIONAL {\r\n"
						+ "        ?parent emf:status \"STOPPED\" . \r\n"
						+ "        ?parent emf:isDeleted ?statusCheck3 .\r\n" + "    }\r\n"
						+ "    filter (!bound(?statusCheck3))\r\n" + "    \r\n" + "    {\r\n"
						+ "        OPTIONAL  {\r\n" + "            emf:danielatodorova sec:noPermission ?instance .\r\n"
						+ "            emf:danielatodorova emf:isDeleted ?check1  .\r\n" + "        } \r\n"
						+ "        FILTER (  ! BOUND (  ?check1  )  )  .\r\n" + "        OPTIONAL  {\r\n"
						+ "            emf:danielatodorova sec:isManagerOf ?instance .\r\n"
						+ "            emf:danielatodorova emf:isDeleted ?check2  .\r\n" + "        } \r\n"
						+ "        FILTER (  ! BOUND (  ?check2  )  )  .\r\n" + "        {\r\n" + "            {\r\n"
						+ "                emf:danielatodorova sec:canRead ?instance\r\n"
						+ "            }  UNION  {\r\n"
						+ "                emf:danielatodorova emf:isMemberOf ?group  .\r\n"
						+ "                ?group sec:canRead ?instance\r\n" + "            }  UNION  {\r\n"
						+ "                sec:SYSTEM_ALL_OTHER_USERS sec:canRead ?instance .\r\n"
						+ "                OPTIONAL  {\r\n"
						+ "                    emf:danielatodorova sec:hasPermission ?instance .\r\n"
						+ "                    emf:danielatodorova emf:isDeleted ?check3  .\r\n"
						+ "                } \r\n" + "                FILTER (  ! BOUND (  ?check3  )  )  .\r\n"
						+ "                OPTIONAL  {\r\n"
						+ "                    emf:danielatodorova emf:isMemberOf ?group  .\r\n"
						+ "                    ?group sec:hasPermission ?instance .\r\n"
						+ "                    emf:danielatodorova emf:isDeleted ?check4  .\r\n"
						+ "                } \r\n" + "                FILTER (  ! BOUND (  ?check4  )  )  .\r\n"
						+ "            }\r\n" + "        }\r\n" + "    }\r\n" + "}\r\n"
						+ "order by ASC(?sortVariable)\r\n" + "LIMIT 1000\r\n" + "\r\n" + "");
	}

	private static ControlParam buildParam(String id, String name, String value) {
		ControlParamMock param = new ControlParamMock();
		param.setIdentifier(id);
		param.setName(name);
		param.setValue(value);
		return param;
	}
}
