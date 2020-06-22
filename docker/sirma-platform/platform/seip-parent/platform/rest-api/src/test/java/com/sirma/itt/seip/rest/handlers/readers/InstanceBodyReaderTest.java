package com.sirma.itt.seip.rest.handlers.readers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test {@link JsonObject} to {@link Instance} conversion.
 *
 * @author yasko
 */
@Test
public class InstanceBodyReaderTest {

	@InjectMocks
	private InstanceBodyReader reader;

	private InstanceResourceParser instanceResourceParser;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	private DefinitionModel definition;

	private EmfInstance instance;

	@Mock
	private Instance parent;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private DefinitionService definitionService;

	@Spy
	private InstanceContextServiceMock contextService;

	private Map<String, Serializable> props = new HashMap<>();

	@BeforeMethod
	protected void init() {
		reader = new InstanceBodyReader();
		MockitoAnnotations.initMocks(this);

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));
		when(parent.getId()).thenReturn("emf:id");

		props.clear();
		props.put("title", "this is a test");
		props.put("name", "Test name");
		instance = new EmfInstance("id");
		contextService.bindContext(instance, parent);
		definition = new DefinitionMock();

		Mockito.when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);

		instanceResourceParser = new InstanceResourceParser();
		Mockito.when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(definition);
		Mockito.when(domainInstanceService.createInstance(eq("def-1"), any())).thenReturn(instance);

		ReflectionUtils.setFieldValue(instanceResourceParser, "definitionService", definitionService);
		ReflectionUtils.setFieldValue(instanceResourceParser, "domainInstanceService", domainInstanceService);
		ReflectionUtils.setFieldValue(instanceResourceParser, "typeConverter", typeConverter);
		ReflectionUtils.setFieldValue(reader, "instanceResourceParser", instanceResourceParser);
	}

	public void testIsReadable() {
		Assert.assertTrue(reader.isReadable(Instance.class, null, null, null));
		Assert.assertFalse(reader.isReadable(String.class, null, null, null));
	}

	/**
	 * Test bad requests - missing fields in json.
	 */
	@Test(expectedExceptions = { BadRequestException.class,
			JsonException.class }, dataProvider = "bad-request-provider")
	public void testNotAnObject(InputStream in) throws Exception {
		reader.readFrom(null, null, null, null, null, in);
	}

	/**
	 * Test successful creation of instance (no id in json)
	 */
	@Test(dataProvider = "create-instance-provider")
	public void testCreate(InputStream in, Map<String, Serializable> properties) throws Exception {
		Instance from = reader.readFrom(null, null, null, null, null, in);
		Assert.assertNotNull(from);
		Assert.assertTrue(from.getProperties().keySet().containsAll(properties.keySet()));
	}

	/**
	 * Test creation with a parentId
	 */
	public void testCreateWithParent() throws IOException {
		String json = "{ \"definitionId\": \"def-1\", \"parentId\": \"parent\" }";
		Instance owned = InstanceReferenceMock.createGeneric("owned").toInstance();

		contextService.bindContext(owned, parent);

		Instance from = reader.readFrom(null, null, null, null, null, toInputStream(json));
		Assert.assertNotNull(from);
		Assert.assertNotNull(contextService.getContext(from));
	}

	/**
	 * Test update of existing instance (id is present in json)
	 */
	public void testUpdate() throws IOException {
		String json = "{ \"id\":\"1\", \"definitionId\": \"def-1\", \"parentId\": \"parent\" }";
		Instance owned = InstanceReferenceMock.createGeneric("owned").toInstance();
		contextService.bindContext(owned, parent);
		Mockito.when(domainInstanceService.loadInstance("1")).thenReturn(owned);

		Instance from = reader.readFrom(null, null, null, null, null, toInputStream(json));
		Assert.assertNotNull(from);
		Assert.assertNotNull(contextService.getContext(from));
	}

	@DataProvider(name = "bad-request-provider")
	protected Object[][] provideBadRequestData() {
		return new Object[][] { { toInputStream("[]") }, { toInputStream("{}") } };
	}

	@DataProvider(name = "create-instance-provider")
	protected Object[][] provideCreateInstanceData() {
		Map<String, Serializable> case3Properties = new HashMap<>();
		case3Properties.put("title", "this is a test");
		case3Properties.put(TEMP_CONTENT_VIEW, "this is a test content");

		case3Properties.put(DefaultProperties.EMF_PURPOSE, "iDoc");

		return new Object[][] { { toInputStream("{ \"definitionId\": \"def-1\" }"), new HashMap<>() },
				{ toInputStream(
				"{ \"definitionId\": \"def-1\", \"content\": \"this is a test content\", \"properties\": { \"title\": \"this is a test\", \"name\": \"Test name\" } }"),
				props },
				{ InstanceBodyReaderTest.class.getResourceAsStream("/instance-w-properties-and-purpose.json"),
						case3Properties } };
	}

	private static InputStream toInputStream(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}
}