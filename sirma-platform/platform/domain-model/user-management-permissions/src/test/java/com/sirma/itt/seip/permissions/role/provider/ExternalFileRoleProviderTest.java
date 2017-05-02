package com.sirma.itt.seip.permissions.role.provider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirma.itt.seip.permissions.ExternalModelActionProvider;
import com.sirma.itt.seip.permissions.ExternalRoleDataProvider;
import com.sirma.itt.seip.permissions.ExternalRoleParser;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.model.SecurityDozerProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.testutil.io.FileTestUtils;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * The Class ExternalFileRoleProviderTest.
 *
 * @author BBonev
 */
@RunWith(DataProviderRunner.class)
public class ExternalFileRoleProviderTest {

	private static final String CONFIRM_READ = "confirmRead";

	private static final String UPLOAD = "upload";

	/** The type provider. */
	@Mock
	private TypeMappingProvider typeProvider;

	/** The mapper. */
	@Spy
	private ObjectMapper mapper = new DozerObjectMapper(
			new DozerBeanMapper(Collections.singletonList(SecurityDozerProvider.DOZER_SECURITY_MAPPING_XML)));

	@Mock
	private ExternalModelActionProvider provider;

	@InjectMocks
	ExternalRoleParser roleParser;

	/** The provider. */
	@InjectMocks
	ExternalFileRoleProvider roleProvider;

	@Spy
	ConfigurationPropertyMock<File> propertyMock = new ConfigurationPropertyMock<>();

	private File currentDir;

	/**
	 * Before method.
	 */
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		ReflectionUtils.setField(roleProvider, "roleParser", roleParser);

		when(typeProvider.getInstanceClass(anyString())).then(invocation -> EmfInstance.class);
		when(provider.createAction(UPLOAD, UPLOAD, UPLOAD)).thenReturn(new EmfAction(UPLOAD),
				new EmfAction(UPLOAD),
				new EmfAction(UPLOAD),
				new EmfAction(UPLOAD));
		when(provider.createAction(CONFIRM_READ, CONFIRM_READ, CONFIRM_READ)).thenReturn(new EmfAction(CONFIRM_READ));
	}

	/**
	 * Invalid data validation.
	 *
	 * @param files
	 *            the files
	 */
	@Test(expected = DefinitionValidationException.class)
	@UseDataProvider(location = ExternalRoleDataProvider.class, value = "invalidData")
	public void invalidDataValidation(Collection<String> files) {
		prepareTempFiles(files);
		roleProvider.getModel(new HashMap<RoleIdentifier, Role>());
	}

	/**
	 * Valid data validation.
	 *
	 * @param files
	 *            the files
	 */
	@Test
	@UseDataProvider(location = ExternalRoleDataProvider.class, value = "validData")
	public void validDataValidation(Collection<String> files) {
		prepareTempFiles(files);
		try {
			Map<RoleIdentifier, Role> model = roleProvider.getModel(new HashMap<RoleIdentifier, Role>());
			assertFalse(model.isEmpty());
			for (Role role : model.values()) {
				assertFalse(role.getAllAllowedActions().isEmpty());
			}
		} catch (Exception e) {
			fail(e.getMessage(), e);
		}
	}

	/**
	 * Prepare temp files.
	 *
	 * @param files
	 *            the files
	 */
	private void prepareTempFiles(Collection<String> files) {
		currentDir = FileTestUtils.copyResourcesToTempFolder(ExternalRoleDataProvider.class, files);
		propertyMock.setValue(new File(currentDir.getAbsolutePath()));
	}

	/**
	 * Clean dir .
	 */
	@After
	public void cleanDir() {
		FileTestUtils.cleanDirectory(currentDir);
	}
}
