/*
 *
 */
package com.sirma.itt.cmf.test.mock;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.mockito.Mockito;
import org.testng.Assert;

import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.services.CaseInstanceAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.CmfDefintionAdapterServiceExtension;
import com.sirma.itt.cmf.alfresco4.services.DefinitionAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.DmsInstanceAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.DocumentAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.PermissionAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.SearchAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.TenantAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverterMockUp;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.adapter.DMSTenantAdapterService;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.converter.extensions.DefaultTypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.ControlParamImpl;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.remote.RESTClient;

/**
 * The Class MockupProvider.
 */
public class MockupProvider {

	/** The definitions cache. */
	protected static Map<String, DefinitionModel> definitionsCache = new HashMap<String, DefinitionModel>();

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

	/**
	 * Instantiates a new mockup provider.
	 *
	 * @param client
	 *            the client
	 */
	public MockupProvider(RESTClient client) {
		setHttpClient(client);
		new DefaultTypeConverter().register(converter);
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
	protected WritablePropertyDefinition buildPropertyDefinition(String label, String name,
			String type, DisplayType displayType, Set<String> filters) {
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
	 * Creates the case definition.
	 *
	 * @param defId
	 *            the defId - dms id
	 * @return the case definition
	 */
	public CaseDefinition getOrCreateCaseDefinition(String defId) {

		if (definitionsCache.containsKey(defId)) {
			return (CaseDefinition) definitionsCache.get(defId);
		}
		CaseDefinition caseDefinition = new CaseDefinitionImpl();
		caseDefinition.setIdentifier(defId);
		caseDefinition.setContainer("dms");
		caseDefinition.setDmsId(defId);
		caseDefinition.setDmsId(UUID.randomUUID().toString()); // ((CaseInstance)
		// instance).getCaseDefinitionId());
		List<PropertyDefinition> fields = caseDefinition.getFields();
		fields.add(getFieldDefinition("name", "cm:name", DisplayType.EDITABLE));
		fields.add(getFieldDefinition("title", "cm:title", DisplayType.EDITABLE));
		fields.add(getFieldDefinition("type", "emf:type", DisplayType.EDITABLE));
		fields.add(getFieldDefinition("modifiedOn", "-cm:modified", DisplayType.SYSTEM));
		fields.add(getFieldDefinition("modifiedBy", "-cm:modifier", DisplayType.SYSTEM));
		fields.add(getFieldDefinition("createdOn", "-cm:created", DisplayType.SYSTEM));
		fields.add(getFieldDefinition("createdBy", "-cm:creator", DisplayType.SYSTEM));
		fields.add(getFieldDefinition("identifier", "emf:identifier", DisplayType.EDITABLE));
		caseDefinition.getSectionDefinitions().add(getSectionDefinition("audit"));
		caseDefinition.getSectionDefinitions().add(getSectionDefinition("private"));
		caseDefinition.getSectionDefinitions().add(getSectionDefinition("public"));

		definitionsCache.put(defId, caseDefinition);
		return caseDefinition;
	}

	/**
	 * Gets the section definition.
	 *
	 * @param string
	 *            the string
	 * @return the section definition
	 */
	protected SectionDefinition getSectionDefinition(String string) {
		SectionDefinitionImpl sectionDefinitionImpl = new SectionDefinitionImpl();
		sectionDefinitionImpl.setIdentifier(string);
		return sectionDefinitionImpl;
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
	public ControlDefinition createControlDefinition(String controlId,
			List<ControlParam> controlParams, List<ControlParam> uiParams) {
		ControlDefinitionImpl controlDefinition = new ControlDefinitionImpl();
		// controlDefinition.setId(1l);
		Assert.fail("Definition model changed. Please check the test");
		controlDefinition.setIdentifier(controlId);
		controlDefinition.setControlParams(controlParams);
		controlDefinition.setUiParams(uiParams);

		return controlDefinition;
	}

	/**
	 * Creates the document.
	 *
	 * @param owning
	 *            the owning
	 * @param standalone
	 *            - is standalone
	 * @param name
	 *            the name
	 * @return the document instance
	 */
	public DocumentInstance createDocument(Instance owning, boolean standalone, String name) {
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setOwningInstance(owning);
		documentInstance.setIdentifier("commonDocument");
		documentInstance.setStandalone(standalone);
		documentInstance.setProperties(new HashMap<String, Serializable>());
		documentInstance.getProperties().put(DocumentProperties.TITLE, "Title for (" + name + ")");
		documentInstance.getProperties().put(DocumentProperties.NAME, name);
		return documentInstance;
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
	protected DictionaryServiceMock getDictionaryImpl() {
		return new DictionaryServiceMock(this);
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
		Set<String> filters = new LinkedHashSet<String>();
		filters.add("filter1");
		return buildPropertyDefinition("Name", "nameField", DataTypeDefinition.TEXT,
				DisplayType.READ_ONLY, filters);
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
	protected PropertyDefinition getFieldDefinition(String id, String dmsId, DisplayType type,
			final Class<?> javaCls) {
		FieldDefinitionImpl fieldDefinitionImpl = new FieldDefinitionImpl();
		fieldDefinitionImpl.setIdentifier(id);
		fieldDefinitionImpl.setDmsType(dmsId);
		fieldDefinitionImpl.setDisplayType(type);
		fieldDefinitionImpl.setDataType(new DataTypeDefinition() {
			private Long id = System.currentTimeMillis();

			@Override
			public String getDescription() {
				return javaCls.getSimpleName();
			}

			@Override
			public Long getId() {
				return id;
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
			public void setId(Long id) {

			}

			@Override
			public String getFirstUri() {
				// TODO Auto-generated method stub
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
	public WritablePropertyDefinition getFieldDefinition(String label, String name, String type,
			String displayType, Set<String> filters) {
		return buildPropertyDefinition(label, name, type, DisplayType.parse(displayType), filters);
	}

	/**
	 * Gets the or create document definition.
	 *
	 * @param defId
	 *            the def id
	 * @return the or create document definition
	 */
	public DocumentDefinitionRef getOrCreateDocumentDefinition(String defId) {
		if (definitionsCache.containsKey(defId)) {
			DefinitionModel definitionModel = definitionsCache.get(defId);
			if (definitionModel instanceof DocumentDefinitionTemplate) {
				return new DocumentDefinitionRefProxy((DocumentDefinitionTemplate) definitionModel);
			}else{
				return (DocumentDefinitionRef) definitionModel;
			}
		}
		DocumentDefinitionRef definition = new DocumentDefinitionRefImpl();
		return createDocumentDefinitionInternal(defId, definition);
	}

	/**
	 * Gets the or create document definition.
	 *
	 * @param defId
	 *            the def id
	 * @return the or create document definition
	 */
	public DocumentDefinitionTemplate getOrCreateDocumentTemplateDefinition(String defId) {
		if (definitionsCache.containsKey(defId)) {
			return (DocumentDefinitionTemplate) definitionsCache.get(defId);
		}
		DocumentDefinitionTemplate definition = new DocumentDefinitionImpl();

		return createDocumentDefinitionInternal(defId, definition);
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
	private <T extends DefinitionModel> T createDocumentDefinitionInternal(String defId,
			T definition) {
		definition.setIdentifier(defId);
		List<PropertyDefinition> fields = definition.getFields();
		fields.add(getFieldDefinition(DocumentProperties.NAME, "cm:" + DocumentProperties.NAME,
				DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition(DocumentProperties.TITLE, "cm:" + DocumentProperties.TITLE,
				DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition(DocumentProperties.TYPE, "emf:" + DocumentProperties.TYPE,
				DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition(DocumentProperties.DESCRIPTION, "cm:"
				+ DocumentProperties.DESCRIPTION, DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition(DocumentProperties.UNIQUE_IDENTIFIER, "emf:"
				+ DocumentProperties.UNIQUE_IDENTIFIER, DisplayType.EDITABLE, String.class));
		fields.add(getFieldDefinition("documentVersion", "version", DisplayType.HIDDEN,
				String.class));
		fields.add(getFieldDefinition("modifiedOn", "-cm:modified", DisplayType.SYSTEM, Date.class));
		fields.add(getFieldDefinition("modifiedBy", "-cm:modifier", DisplayType.SYSTEM,
				String.class));
		fields.add(getFieldDefinition("createdOn", "-cm:created", DisplayType.SYSTEM, Date.class));
		fields.add(getFieldDefinition("createdBy", "-cm:creator", DisplayType.SYSTEM, String.class));
		fields.add(getFieldDefinition("fileSize", "", DisplayType.HIDDEN, String.class));
		fields.add(getFieldDefinition("documentLocation", "nodeRef", DisplayType.HIDDEN,
				String.class));
		fields.add(getFieldDefinition("mimetype", "cm:content.mimetype", DisplayType.HIDDEN,
				String.class));
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
	 * Mockup case adapter.
	 *
	 * @return the cMF case instance adapter service
	 */
	public CMFCaseInstanceAdapterService mockupCaseAdapter() {
		CMFCaseInstanceAdapterService service = new CaseInstanceAlfresco4Service();
		setParam(service, "restClient", httpClient);
		setParam(service, "dictionaryService", getDictionaryImpl());
		setParam(service, "caseConvertor", mockupDMSTypeConverter("case"));
		return service;
	}

	/**
	 * Gets the converter properties.
	 *
	 * @return the converter properties
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected Properties getConverterProperties() throws IOException {
		Properties props = new Properties();
		props.load(DMSTypeConverter.class.getResourceAsStream("convertor.properties"));
		return props;
	}

	/**
	 * Mockup definiton adapter.
	 *
	 * @return the pM defintion adapter service
	 */
	public DMSDefintionAdapterService mockupDefinitonAdapter() {
		DefinitionAlfresco4Service adapter = new DefinitionAlfresco4Service();
		setParam(adapter, "restClient", httpClient);
		List<DMSDefintionAdapterServiceExtension> list = new ArrayList<>(2);
		list.add(new CmfDefintionAdapterServiceExtension());
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
		@SuppressWarnings("unchecked")
		javax.enterprise.inject.Instance<RESTClient> mockedInstance = Mockito.mock(javax.enterprise.inject.Instance.class);
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
	public DMSTypeConverter mockupDMSTypeConverter(String model) {
		try {
			return DMSTypeConverterMockUp.create(converter, dictionaryService, evaluatorManager,
					model.toLowerCase(), cacheContext, getConverterProperties());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Mockup document adapter.
	 *
	 * @return the cMF document adapter service
	 */
	public CMFDocumentAdapterService mockupDocumentAdapter() {

		DocumentAlfresco4Service dmsService = new DocumentAlfresco4Service();

		DMSConverterFactory converterFactory = Mockito.mock(DMSConverterFactory.class);
		DMSTypeConverter mockupDMSTypeConverter = this
				.mockupDMSTypeConverter(ConverterConstants.DOCUMENT.toLowerCase());
		Mockito.when(converterFactory.getConverter(SectionInstance.class)).thenReturn(
				mockupDMSTypeConverter);
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
		DMSTypeConverter mockupDMSTypeConverter = this
				.mockupDMSTypeConverter(ConverterConstants.DOCUMENT.toLowerCase());
		Mockito.when(converterFactory.getConverter(SectionInstance.class)).thenReturn(
				mockupDMSTypeConverter);
		setParam(adapter, "converterFactory", new DMSConverterFactoryMock(new MockupProvider(
				httpClient)));
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
		setParam(userService, "ssoEnabled", Boolean.TRUE);
		setParam(userService, "configAdminUser", "system");
		return userService;
	}

	/**
	 * Mock up the permission adapter.
	 *
	 * @return the permission adapter
	 */
	public CMFPermissionAdapterService mockupPermissionAdapter() {
		CMFPermissionAdapterService adapter = new PermissionAlfresco4Service();
		setParam(adapter, "restClient", httpClient);
		DMSConverterFactoryMock converterFactory = new DMSConverterFactoryMock(new MockupProvider(
				httpClient));
		setParam(adapter, "convertorFactory", converterFactory);
		setParam(adapter, "caseConvertor",
				mockupDMSTypeConverter(ConverterConstants.CASE.toLowerCase()));
		setParam(adapter, "permissionPropserties", "");
		return adapter;
	}

	/**
	 * Mock up the search adapter.
	 *
	 * @return the search adapter
	 */
	public CMFSearchAdapterService mockupSearchAdapter() {
		CMFSearchAdapterService searchAdapterService = new SearchAlfresco4Service();
		setParam(searchAdapterService, "restClient", httpClient);
		setParam(searchAdapterService, "convertorFactory", new DMSConverterFactoryMock(
				new MockupProvider(httpClient)));
		setParam(searchAdapterService, "permissionAdapterService", mockupPermissionAdapter());
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
}
