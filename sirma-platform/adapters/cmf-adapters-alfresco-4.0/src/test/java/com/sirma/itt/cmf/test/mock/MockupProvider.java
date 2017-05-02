/*
 *
 */
package com.sirma.itt.cmf.test.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.joda.time.DateTime;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.services.DefinitionAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.DmsInstanceAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.DocumentAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller;
import com.sirma.itt.cmf.alfresco4.services.SearchAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.TenantAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverterImpl;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverterMockUp;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.adapter.DMSTenantAdapterService;
import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.AlfrescoRESTClient;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.definition.DefintionAdapterServiceExtension;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.resources.adapter.CMFUserService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

import sun.misc.BASE64Encoder;

/**
 * The Class MockupProvider.
 */
public class MockupProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The definitions cache. */
	protected static Map<String, DefinitionModel> definitionsCache = new HashMap<>();

	/** The cache context. */
	protected EntityLookupCacheContext cacheContext = getCacheLookupImpl();

	/** The dictionary service. */
	protected DictionaryService dictionaryService = getDictionaryImpl();

	/** The evaluator manager. */
	protected ExpressionsManager evaluatorManager = getEvaluatorManagerImpl();

	/** The converter. */
	protected TypeConverter converter = getTypeConverterImpl();

	/** The http client. */
	protected RESTClient httpClient;
	@Mock
	protected AdaptersConfiguration adaptersConfiguration;
	@Mock
	protected SecurityContext securityContext;
	@Mock
	protected SecurityConfiguration securityConfiguration;
	@Mock
	protected User authenticated;

	/**
	 * Instantiates a new mockup provider.
	 *
	 * @param port
	 * @param host
	 * @param userName
	 * @param client
	 *            the client
	 */
	public MockupProvider(String userName, String host, int port) {
		init(userName, host, port);
	}

	private void init(String userName, String host, int port) {
		httpClient = new AlfrescoRESTClient();
		MockitoAnnotations.initMocks(this);
		ReflectionUtils.setField(httpClient, "timeout", new ConfigurationPropertyMock<>(10000));
		when(authenticated.getIdentityId()).thenReturn(userName);
		String ticket = encrypt(createResponse("http://" + host + ":8080/alfresco/ServiceLogin",
				"http://" + host + ":8081", "https://localhost:9443/samlsso", userName));
		when(authenticated.getTicket()).thenReturn(ticket.replace("\r", "\t").replace("\n", "\t"));
		when(securityContext.getAuthenticated()).thenReturn(authenticated);
		when(securityContext.getEffectiveAuthentication()).thenReturn(authenticated);

		// set tenant id if needed
		when(securityContext.getCurrentTenantId()).thenReturn(null);

		when(adaptersConfiguration.getDmsAddress()).thenReturn(new ConfigurationPropertyMock<>(() -> {
			try {
				return new URI("http", null, host, port, null, null, null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}));
		when(securityConfiguration.getAdminUserName()).thenReturn(new ConfigurationPropertyMock<>("systemadmin"));
		setParam(httpClient, "securityContext", securityContext);
		setParam(httpClient, "securityConfiguration", securityConfiguration);
		setParam(httpClient, "configuration", adaptersConfiguration);

		new DefaultTypeConverter().register(converter);

	}

	public RESTClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Creates the param.
	 *
	 * @param identifier
	 *            the identifier
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return the control param
	 */
	public static ControlParam createParam(String identifier, String name, String value) {
		ControlParamImpl param = new ControlParamImpl();
		param.setIdentifier(identifier);
		param.setName(name);
		param.setValue(value);
		return param;
	}

	/**
	 * Encrypt method exposed for standalone testing.
	 *
	 * @param stringBuffer
	 *            is the plain text
	 * @return the encrypted plain text
	 */
	public static String encrypt(StringBuffer stringBuffer) {
		// only the first 8 Bytes of the constructor argument are used
		// as material for generating the keySpec
		try {
			// TODO may be as param
			DESKeySpec keySpec = new DESKeySpec("AlfrescoCMFLogin@T1st".getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey cipherKey = keyFactory.generateSecret(keySpec);
			// ENCODE plainText String
			byte[] cleartext = stringBuffer.toString().getBytes("UTF-8");
			// cipher is not thread safe
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
			String encryptedPwd = new BASE64Encoder().encode(cipher.doFinal(cleartext));
			return encryptedPwd;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(stringBuffer + " is not encrypted, due to exception: " + e.getMessage());
		}
	}

	/**
	 * Creates saml response message for current time.
	 *
	 * @param alfrescoURL
	 *            not actually used
	 * @param audianceURL
	 *            not actually used
	 * @param samlURL
	 *            not actually used
	 * @param user
	 *            to user
	 * @return the created saml2 response
	 */
	protected StringBuffer createResponse(String alfrescoURL, String audianceURL, String samlURL, String user) {

		DateTime now = new DateTime();

		DateTime barrier = now.plusMinutes(10);
		StringBuffer saml = new StringBuffer();
		saml
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
					.append("<saml2p:Response ID=\"inppcpljfhhckioclinjenlcneknojmngnmgklab\" IssueInstant=\"")
					.append(now.toString())
					.append("\" Version=\"2.0\" xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\">")
					.append("<saml2p:Status>")
					.append("<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>")
					.append("</saml2p:Status>")
					.append("<saml2:Assertion ID=\"ehmifefpmmlichdcpeiogbgcmcbafafckfgnjfnk\" IssueInstant=\"")
					.append(now.toString())
					.append("\" Version=\"2.0\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">")
					.append("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">")
					.append(samlURL)
					.append("</saml2:Issuer>")
					.append("<saml2:Subject>")
					.append("<saml2:NameID>")
					.append(user)
					.append("</saml2:NameID>")
					.append("<saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">")
					.append("<saml2:SubjectConfirmationData InResponseTo=\"0\" NotOnOrAfter=\"")
					.append(barrier.toString())
					.append("\" Recipient=\"")
					.append(alfrescoURL)
					.append("\"/>")
					.append("</saml2:SubjectConfirmation>")
					.append("</saml2:Subject>")
					.append("<saml2:Conditions NotBefore=\"")
					.append(now.toString())
					.append("\" NotOnOrAfter=\"")
					.append(barrier.toString())
					.append("\">")
					.append("<saml2:AudienceRestriction>")
					.append("<saml2:Audience>")
					.append(audianceURL)
					.append("</saml2:Audience>")
					.append("</saml2:AudienceRestriction>")
					.append("</saml2:Conditions>")
					.append("<saml2:AuthnStatement AuthnInstant=\"")
					.append(now.toString())
					.append("\">")
					.append("<saml2:AuthnContext>")
					.append("<saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml2:AuthnContextClassRef>")
					.append("</saml2:AuthnContext>")
					.append("</saml2:AuthnStatement>")
					.append("</saml2:Assertion>")
					.append("</saml2p:Response>");
		return saml;
	}

	/**
	 * Builds a {@link FieldDefinitionImpl}.
	 *
	 * @param label
	 *            label
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param displayType
	 *            displayType
	 * @param filters
	 *            the filters
	 * @return Created FieldDefinitionImpl.
	 */
	protected WritablePropertyDefinition buildPropertyDefinition(String label, String name, String type,
			DisplayType displayType, Set<String> filters) {
		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId(1l);
		property.setLabelId(label);
		property.setName(name);
		property.setType(type);
		property.setDisplayType(displayType);
		property.setFilters(filters);
		return property;
	}

	/**
	 * Gets the field definition.
	 *
	 * @param id
	 *            the id
	 * @param dmsId
	 *            the dms id
	 * @param type
	 *            the type
	 * @return the field definition
	 */
	protected PropertyDefinition getFieldDefinition(String id, String dmsId, DisplayType type) {
		FieldDefinitionImpl fieldDefinitionImpl = new FieldDefinitionImpl();
		fieldDefinitionImpl.setIdentifier(id);
		fieldDefinitionImpl.setDmsType(dmsId);
		fieldDefinitionImpl.setDisplayType(type);
		return fieldDefinitionImpl;
	}

	/**
	 * Creates the control definition.
	 *
	 * @param controlId
	 *            the control id
	 * @param controlParams
	 *            the control params
	 * @param uiParams
	 *            the ui params
	 * @return the control definition
	 */
	public ControlDefinition createControlDefinition(String controlId, List<ControlParam> controlParams,
			List<ControlParam> uiParams) {
		ControlDefinitionImpl controlDefinition = new ControlDefinitionImpl();
		// controlDefinition.setId(1l);
		Assert.fail("Definition model changed. Please check the test");
		controlDefinition.setIdentifier(controlId);
		controlDefinition.setControlParams(controlParams);
		controlDefinition.setUiParams(uiParams);

		return controlDefinition;
	}

	/**
	 * Gets the cache lookup impl.
	 *
	 * @return the cache lookup impl
	 */
	protected EntityLookupCacheContextMock getCacheLookupImpl() {
		return new EntityLookupCacheContextMock();
	}

	/**
	 * Gets the dictionary impl.
	 *
	 * @return the dictionary impl
	 */
	protected DictionaryService getDictionaryImpl() {
		return mock(DictionaryService.class);
	}

	/**
	 * Gets the evaluator manager impl.
	 *
	 * @return the evaluator manager impl
	 */
	protected EvaluatorManagerMock getEvaluatorManagerImpl() {
		return new EvaluatorManagerMock();
	}

	/**
	 * Creates a {@link FieldDefinitionImpl} with predefined values.
	 *
	 * @return Created FieldDefinitionImpl.
	 */
	public WritablePropertyDefinition getFieldDefinition() {
		Set<String> filters = new LinkedHashSet<>();
		filters.add("filter1");
		return buildPropertyDefinition("Name", "nameField", DataTypeDefinition.TEXT, DisplayType.READ_ONLY, filters);
	}

	/**
	 * Gets the field definition.
	 *
	 * @param id
	 *            the id
	 * @param dmsId
	 *            the dms id
	 * @param type
	 *            the type
	 * @param javaCls
	 *            the java cls
	 * @return the field definition
	 */
	protected PropertyDefinition getFieldDefinition(String id, String dmsId, DisplayType type, final Class<?> javaCls) {
		FieldDefinitionImpl fieldDefinitionImpl = new FieldDefinitionImpl();
		fieldDefinitionImpl.setIdentifier(id);
		fieldDefinitionImpl.setDmsType(dmsId);
		fieldDefinitionImpl.setDisplayType(type);
		fieldDefinitionImpl.setDataType(new DataTypeDefinition() {
			private Long randonId = System.currentTimeMillis();

			@Override
			public String getDescription() {
				return javaCls.getSimpleName();
			}

			@Override
			public Long getId() {
				return randonId;
			}

			@Override
			public Class<?> getJavaClass() {
				return javaCls;
			}

			@Override
			public String getJavaClassName() {
				return javaCls.getName();
			}

			@Override
			public String getName() {
				return javaCls.getSimpleName();
			}

			@Override
			public String getTitle() {
				return javaCls.getSimpleName();
			}

			@Override
			public void setId(Long someId) {
				// the id is generated
			}

			@Override
			public String getFirstUri() {
				// Not used method
				return null;
			}

			@Override
			public Set<String> getUries() {
				return null;
			}
		});
		return fieldDefinitionImpl;
	}

	/**
	 * Creates a {@link FieldDefinitionImpl}.
	 *
	 * @param label
	 *            label
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param displayType
	 *            displayType
	 * @param filters
	 *            the filters
	 * @return Created FieldDefinitionImpl.
	 */
	public WritablePropertyDefinition getFieldDefinition(String label, String name, String type, String displayType,
			Set<String> filters) {
		return buildPropertyDefinition(label, name, type, DisplayType.parse(displayType), filters);
	}

	/**
	 * Internal add document definition from provided type
	 *
	 * @param <T>
	 *            is the type
	 * @param defId
	 *            is the id
	 * @param definition
	 *            is the instance for definition
	 * @return the populated instance
	 */
	private <T extends DefinitionModel> T createDocumentDefinitionInternal(String defId, T definition) {
		definition.setIdentifier(defId);
		List<PropertyDefinition> fields = definition.getFields();
		fields.add(getFieldDefinition(DefaultProperties.NAME, "cm:" + DefaultProperties.NAME, DisplayType.EDITABLE,
				String.class));
		fields.add(getFieldDefinition(DefaultProperties.TITLE, "cm:" + DefaultProperties.TITLE, DisplayType.EDITABLE,
				String.class));
		fields.add(getFieldDefinition(DefaultProperties.TYPE, "emf:" + DefaultProperties.TYPE, DisplayType.EDITABLE,
				String.class));
		fields.add(getFieldDefinition(DefaultProperties.DESCRIPTION, "cm:" + DefaultProperties.DESCRIPTION,
				DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition(DefaultProperties.UNIQUE_IDENTIFIER, "emf:" + DefaultProperties.UNIQUE_IDENTIFIER,
				DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition("documentVersion", "version", DisplayType.HIDDEN, String.class));
		fields.add(getFieldDefinition("modifiedOn", "-cm:modified", DisplayType.SYSTEM, Date.class));
		fields.add(getFieldDefinition("modifiedBy", "-cm:modifier", DisplayType.SYSTEM, String.class));
		fields.add(getFieldDefinition("createdOn", "-cm:created", DisplayType.SYSTEM, Date.class));
		fields.add(getFieldDefinition("createdBy", "-cm:creator", DisplayType.SYSTEM, String.class));
		fields.add(getFieldDefinition("fileSize", "", DisplayType.HIDDEN, String.class));
		fields.add(getFieldDefinition("documentLocation", "nodeRef", DisplayType.HIDDEN, String.class));
		fields.add(getFieldDefinition("mimetype", "cm:content.mimetype", DisplayType.HIDDEN, String.class));
		definition.setHash(fields.hashCode());
		definitionsCache.put(defId, definition);
		return definition;
	}

	/**
	 * Gets the type converter impl.
	 *
	 * @return the type converter impl
	 */
	protected TypeConverter getTypeConverterImpl() {

		return TypeConverterUtil.getConverter();
	}

	/**
	 * Gets the converter properties.
	 *
	 * @return the converter properties
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected Properties getConverterProperties() {
		Properties props = new Properties();
		try {
			props.load(DMSTypeConverterImpl.class.getResourceAsStream("convertor.properties"));
		} catch (IOException e) {
			LOGGER.warn("", e);
			throw new EmfRuntimeException(e);
		}
		return props;
	}

	/**
	 * Mockup definiton adapter.
	 *
	 * @return the pM defintion adapter service
	 */
	public DefintionAdapterService mockupDefinitonAdapter() {
		DefinitionAlfresco4Service adapter = new DefinitionAlfresco4Service();
		setParam(adapter, "restClient", httpClient);
		List<DefintionAdapterServiceExtension> list = new ArrayList<>(2);
		setParam(adapter, "extensions", list);
		setParam(adapter, "converter", mockupDMSTypeConverter(ConverterConstants.GENERAL));
		setParam(adapter, "uploader", mockupAlfrescoUploader());
		adapter.initialize();
		return adapter;
	}

	/**
	 * Mockup alfresco uploader.
	 *
	 * @return the alfresco uploader
	 */
	public AlfrescoUploader mockupAlfrescoUploader() {
		AlfrescoUploader adapter = new AlfrescoUploader();
		javax.enterprise.inject.Instance<RESTClient> mockedInstance = Mockito
				.mock(javax.enterprise.inject.Instance.class);
		Mockito.when(mockedInstance.get()).thenReturn(httpClient);
		setParam(adapter, "restClient", mockedInstance);

		return adapter;
	}

	/**
	 * Mockup dms type convertor.
	 *
	 * @param model
	 *            the model
	 * @return the dMS type convertor
	 */
	public DMSTypeConverterImpl mockupDMSTypeConverter(String model) {
		try {
			return DMSTypeConverterMockUp.create(converter, dictionaryService, evaluatorManager, model.toLowerCase(),
					cacheContext, new CachingSupplier<>(this::getConverterProperties), baseModelProvider(model));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates lazy initialized caching supplier that provides base definition model for the given
	 * {@link ConvertibleModels}.
	 *
	 * @param model
	 *            the model
	 * @return the supplier
	 */
	Supplier<DefinitionModel> baseModelProvider(String modelId) {
		Supplier<DefinitionModel> provider = () -> {
			try {
				Properties properties = getConverterProperties();
				// load converter data
				String baseId = properties.getProperty(modelId + ".baseDefinitionId");

				return dictionaryService.find(baseId);
			} catch (Exception e) {
				LOGGER.warn("Failed to find definition class for model: " + modelId, e);
				throw new EmfRuntimeException(e);
			}
		};
		return new CachingSupplier<>(provider);
	}

	/**
	 * Mockup document adapter.
	 *
	 * @return the cMF document adapter service
	 */
	public CMFDocumentAdapterService mockupDocumentAdapter() {

		DocumentAlfresco4Service dmsService = new DocumentAlfresco4Service();

		DMSConverterFactory converterFactory = Mockito.mock(DMSConverterFactory.class);
		DMSTypeConverterImpl mockupDMSTypeConverter = mockupDMSTypeConverter(ConverterConstants.GENERAL.toLowerCase());
		// Mockito.when(converterFactory.getConverter(SectionInstance.class)).thenReturn(mockupDMSTypeConverter);
		setParam(dmsService, "restClient", httpClient);
		AlfrescoUploader alfrescoUploader = mockupAlfrescoUploader();
		setParam(dmsService, "alfrescoUploader", alfrescoUploader);
		setParam(dmsService, "typeConverter", converter);
		setParam(dmsService, "docConvertor", mockupDMSTypeConverter);
		return dmsService;
	}

	/**
	 * Sets the http client.
	 *
	 * @param httpClient
	 *            the new http client
	 */
	public void setHttpClient(RESTClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Set by reflection an object value.
	 *
	 * @param object
	 *            is the object
	 * @param field
	 *            is the field
	 * @param value
	 *            is the new value
	 * @return true on success
	 */
	protected boolean setParam(Object object, String field, Object value) {
		try {
			Field declaredField = object.getClass().getDeclaredField(field);
			declaredField.setAccessible(true);
			declaredField.set(object, value);
			declaredField.setAccessible(false);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Mock ups new instance adapter with predefined rest client and {@link DMSConverterFactory}
	 *
	 * @return the adapter service
	 */
	public DMSInstanceAdapterService mockupDmsInstanceAdapter() {
		DMSInstanceAdapterService adapter = new DmsInstanceAlfresco4Service();
		setParam(adapter, "restClient", httpClient);
		setParam(adapter, "documentUpdateAutoDescription", "test update");
		DMSConverterFactory converterFactory = Mockito.mock(DMSConverterFactory.class);
		DMSTypeConverter mockupDMSTypeConverter = mockupDMSTypeConverter(ConverterConstants.GENERAL.toLowerCase());
		// Mockito.when(converterFactory.getConverter(SectionInstance.class)).thenReturn(mockupDMSTypeConverter);
		setParam(adapter, "converterFactory", new DMSConverterFactoryMock(this));
		return adapter;
	}

	/**
	 * Mock up the {@link UsersAlfresco4Service}
	 *
	 * @return the mocked adapter
	 */
	public CMFUserService mockUserAdapter() {
		CMFUserService userService = new UsersAlfresco4Service();
		setParam(userService, "restClient", httpClient);
		setParam(userService, "securityConfiguration", securityConfiguration);
		return userService;
	}

	/**
	 * Mock up the search adapter.
	 *
	 * @return the search adapter
	 */
	public CMFSearchAdapterService mockupSearchAdapter() {
		CMFSearchAdapterService searchAdapterService = new SearchAlfresco4Service();
		setParam(searchAdapterService, "restClient", httpClient);
		setParam(searchAdapterService, "convertorFactory", new DMSConverterFactoryMock(this));
		return searchAdapterService;

	}

	/**
	 * Mock up the tenant adapter.
	 *
	 * @return the tenant adapter
	 */
	public DMSTenantAdapterService mockupTenantAdapter() {
		TenantAlfresco4Service tenantAdapter = new TenantAlfresco4Service();
		setParam(tenantAdapter, "restClient", httpClient);
		return tenantAdapter;

	}

	/**
	 * Mock up the init controller.
	 *
	 * @return the tenant adapter
	 */
	public InitAlfresco4Controller mockupInitAlfresco4Controller() {
		InitAlfresco4Controller initController = new InitAlfresco4Controller();
		setParam(initController, "restClient", httpClient);
		setParam(initController, "alfrescoUploader", mockupAlfrescoUploader());

		return initController;

	}
}
