package com.sirma.itt.seip.testutil;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.fail;

import javax.enterprise.inject.spi.BeanManager;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.convert.UriConverterProvider;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.json.JsonConverterProvider;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.DatabaseIdManagerFake;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.CDI;

/**
 * The Class EmfTest.
 *
 * @author BBonev
 */
public abstract class EmfTest {
	@Spy
	protected DatabaseIdManagerFake idManager;
	@Spy
	protected SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	protected SecurityConfiguration securityConfiguration;
	@Mock
	protected SystemConfiguration systemConfiguration;
	@Mock
	protected SecurityContext securityContext;
	@Mock
	protected UserPreferences userPreferences;
	@Spy
	protected InstanceContextServiceMock contextService;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		initMocks(this);
		initInstanceUtil(idManager, contextService);
		setUpSecurityContext();
	}

	public static void initInstanceUtil(DatabaseIdManager idManager, InstanceContextService contextService) {
		CDI.setCachedBeanManager(Mockito.mock(BeanManager.class));
		InstanceUtil.init(new InstanceUtil(idManager, contextService));
	}

	/**
	 * Sets a current user to be admin before class execution.<br>
	 * Initialize the sequence generator.
	 */
	public void setUpSecurityContext() {
		EmfUser user = new EmfUser("admin");
		user.setId("emf:admin");
		user.getProperties().put(ResourceProperties.FIRST_NAME, "Admin");
		user.getProperties().put(ResourceProperties.LAST_NAME, "Adminov");
		user.getProperties().put(ResourceProperties.LANGUAGE, "bg");
		when(securityContextManager.getAdminUser()).thenReturn(user);
		when(securityContextManager.getSystemUser()).thenReturn(user);
		when(securityConfiguration.getAdminUser()).thenReturn(new ConfigurationPropertyMock<>(user));
		when(securityConfiguration.getSystemUser()).thenReturn(new ConfigurationPropertyMock<>(user));
		when(securityContext.getAuthenticated()).thenReturn(user);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.getCurrentTenantId()).thenReturn(SecurityContext.DEFAULT_TENANT);
		when(systemConfiguration.getSystemLanguage()).thenReturn("bg");
		when(userPreferences.getLanguage()).thenReturn("bg");
		when(userPreferences.getLanguage(any())).thenReturn("bg");
	}

	/**
	 * Creates the type converter initialized with some default converter.s
	 *
	 * @return the type converter
	 */
	public TypeConverter createTypeConverter() {
		TypeConverter converter = new TypeConverterImpl();
		new DefaultTypeConverter().register(converter);
		converter.addConverter(InstanceReference.class, Class.class, ref -> ref.getReferenceType().getJavaClass());
		converter.addConverter(InstanceReferenceMock.class, Class.class, ref -> ref.getReferenceType().getJavaClass());

		converter.addConverter(Instance.class, InstanceReference.class, InstanceReferenceMock::new);
		converter.addConverter(String.class, InstanceReference.class, source -> {
			DataTypeDefinitionMock type = new DataTypeDefinitionMock(null, null);
			String name = source;
			if (name.contains(".")) {
				try {
					type = new DataTypeDefinitionMock(Class.forName(name), null);
				} catch (Exception e) {
					fail("", e);
				}
				name = name.substring(name.lastIndexOf('.'));
			}
			type.setName(name);

			return new InstanceReferenceMock(source, type);
		});
		new JsonConverterProvider().register(converter);
		new UriConverterProvider().register(converter);
		registerConverters(converter);
		TypeConverterUtil.setTypeConverter(converter);
		return converter;
	}

	/**
	 * Override to provide type converters. The emthod is called when new type converter is created.
	 *
	 * @param typeConverter
	 *            the type converter
	 */
	protected void registerConverters(TypeConverter typeConverter) {
		// nothing to do here
	}

	/**
	 * Sets the reference field.
	 *
	 * @param instance
	 *            the new reference field
	 */
	protected void setReferenceField(Instance instance) {
		InstanceReferenceMock.createGeneric(instance);
	}

	/**
	 * Creates the data type.
	 *
	 * @param source
	 *            the source
	 * @return the data type
	 */
	protected DataTypeDefinition createDataType(Instance source) {
		return new DataTypeDefinitionMock(source);
	}
}
