package com.sirma.itt.seip.configuration.sync;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.util.AnnotationLiteral;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * @author BBonev
 */
@Test
public class TenantConfigurationSynchronizationTest {

	@InjectMocks
	TenantConfigurationSynchronization synchronization;
	@Mock
	private RawConfigurationAccessor configurationAccessor;
	@Mock
	private ConfigurationInstanceProvider configurationInstanceProvider;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Mock
	private SecurityContext securityContext;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	private void mockInsert(int expectedSize) {
		when(configurationManagement.addConfigurations(anyCollection())).then(a -> {
			Collection<?> collection = a.getArgumentAt(0, Collection.class);
			assertEquals(collection.size(), expectedSize);
			return collection;
		});
	}

	public void test_syncSystemConfigurations() {
		mockConfigurationProvider();

		mockRawConfigurationAccessor("config1", "testValue1");
		mockRawConfigurationAccessor("config2", "testValue2");

		mockInsert(6);

		synchronization.syncSystemConfigurations();
	}

	public void test_syncTenantConfigurations() {
		mockConfigurationProvider();

		mockRawConfigurationAccessor("config4", "testValue4");
		mockRawConfigurationAccessor("config5", "testValue5");

		mockInsert(3);

		synchronization.syncTenantConfigurations();
	}

	private void mockConfigurationProvider() {
		Collection<ConfigurationInstance> configurations = new ArrayList<>();
		configurations.add(new ConfigurationInstanceStub(
				new ConfigurationPropertyDefinitionStub("config1", true, "value1", String.class)));
		configurations.add(new ConfigurationInstanceStub(
				new ConfigurationPropertyDefinitionStub("config2", true, "value2", String.class)));
		configurations.add(new ConfigurationInstanceStub(
				new ConfigurationPropertyDefinitionStub("config3", true, "value3", String.class)));
		configurations.add(new ConfigurationInstanceStub(
				new ConfigurationPropertyDefinitionStub("config4", false, "value4", String.class)));
		configurations.add(new ConfigurationInstanceStub(
				new ConfigurationPropertyDefinitionStub("config5", false, "value5", String.class)));
		configurations.add(new ConfigurationInstanceStub(
				new ConfigurationPropertyDefinitionStub("config6", false, "value6", String.class)));
		when(configurationInstanceProvider.getAllInstances()).thenReturn(configurations);
	}

	private void mockRawConfigurationAccessor(String name, String value) {
		when(configurationAccessor.getRawConfigurationValue(name)).thenReturn(value);
	}

	private static class ConfigurationPropertyDefinitionStub extends AnnotationLiteral<ConfigurationPropertyDefinition>
			implements ConfigurationPropertyDefinition {

		private String name;
		private boolean system;
		private String defaultValue;
		private Class<?> type;

		public ConfigurationPropertyDefinitionStub(String name, boolean system, String defaultValue, Class<?> type) {
			this.name = name;
			this.system = system;
			this.defaultValue = defaultValue;
			this.type = type;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean system() {
			return system;
		}

		@Override
		public boolean shared() {
			return true;
		}

		@Override
		public String defaultValue() {
			return defaultValue;
		}

		@Override
		public Class<?> type() {
			return type;
		}

		@Override
		public String label() {
			return "";
		}

		@Override
		public String subSystem() {
			return null;
		}

		@Override
		public String alias() {
			return null;
		}

		@Override
		public String converter() {
			return null;
		}

		@Override
		public boolean sensitive() {
			return false;
		}

		@Override
		public boolean password() {
			return false;
		}
	}

	private static class ConfigurationInstanceStub implements ConfigurationInstance {

		ConfigurationPropertyDefinition annotation;

		public ConfigurationInstanceStub(ConfigurationPropertyDefinition definition) {
			annotation = definition;
		}

		@Override
		public String getName() {
			return annotation.name();
		}

		@Override
		public boolean isSystemConfiguration() {
			return annotation.system();
		}

		@Override
		public boolean isSharedConfiguration() {
			return annotation.shared();
		}

		@Override
		public <T> Class<T> getType() {
			return (Class<T>) annotation.type();
		}

		@Override
		public String getLabel() {
			return annotation.label();
		}

		@Override
		public Annotation getAnnotation() {
			return annotation;
		}

		@Override
		public TypeConverterContext createConverterContext(ConfigurationInstanceProvider configurationInstanceProvider,
				RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider provider) {
			return null;
		}

		@Override
		public String getSubSystem() {
			return annotation.subSystem();
		}

		@Override
		public String getAlias() {
			return annotation.alias();
		}

		@Override
		public String getConverter() {
			return annotation.converter();
		}

		@Override
		public boolean isSensitive() {
			return annotation.sensitive();
		}

		@Override
		public boolean isPassword() {
			return annotation.password();
		}
	}
}
