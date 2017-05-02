package com.sirma.itt.seip.permissions.role.provider;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
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
import com.sirma.itt.seip.content.descriptor.LocalFileDescriptor;
import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirma.itt.seip.permissions.ExternalModelActionProvider;
import com.sirma.itt.seip.permissions.ExternalRoleDataProvider;
import com.sirma.itt.seip.permissions.ExternalRoleParser;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.model.RoleInstance;
import com.sirma.itt.seip.permissions.model.SecurityDozerProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * The Class ExternalFileRoleProviderTest.
 *
 * @author BBonev
 */
@RunWith(DataProviderRunner.class)
public class ExternalDefinitionRoleProviderTest {

	private static final String CONFIRM_READ = "confirmRead";

	private static final String UPLOAD = "upload";

	/** The type provider. */
	@Mock
	private TypeMappingProvider typeProvider;

	@Mock
	private DefinitionManagementService managementService;

	@Mock
	private TempFileProvider tempFileProvider;

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
	ExternalDefinitionRoleProvider roleProvider;

	@Mock
	Supplier<File> supplier;

	@Spy
	ConfigurationPropertyMock<File> propertyMock = new ConfigurationPropertyMock<>();

	private File currentDir;

	/**
	 * Before method.
	 */
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		currentDir = new File(UUID.randomUUID().toString());
		currentDir.mkdirs();

		// mockito does not inject fields that are annotated with @InjectMocks
		ReflectionUtils.setField(roleProvider, "roleParser", roleParser);

		when(typeProvider.getInstanceClass(anyString())).then(invocation -> EmfInstance.class);
		when(tempFileProvider.createTempFile(anyString(), any()))
				.then(a -> new File(currentDir, UUID.randomUUID().toString()));
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
		currentDir = new File(UUID.randomUUID().toString());
		currentDir.mkdirs();
		List<FileDescriptor> descriptors = new LinkedList<>();
		for (String file : files) {
			File outFile = new File(currentDir, UUID.randomUUID().toString() + ".xml");
			try {
				outFile.createNewFile();
			} catch (IOException e1) {
				fail(e1.getMessage(), e1);
			}
			try (InputStream stream = ExternalRoleDataProvider.class.getResourceAsStream(file);
					OutputStream output = new FileOutputStream(outFile)) {
				IOUtils.copyLarge(stream, output);
			} catch (IOException e) {
				fail(e.getMessage(), e);
			}
			descriptors.add(new LocalFileDescriptor(outFile) {
				private static final long serialVersionUID = -2126426494271618657L;

				@Override
				public String getContainerId() {
					return "test";
				}
			});
		}
		when(managementService.getDefinitions(RoleInstance.class)).thenReturn(descriptors);
		when(supplier.get()).thenReturn(new File(currentDir.getAbsolutePath()));
		propertyMock.valueUpdated();
	}

	/**
	 * Clean dir .
	 */
	@After
	public void cleanDir() {
		File[] files = currentDir.listFiles();
		for (File file : files) {
			file.delete();
		}
		currentDir.delete();
	}

}
