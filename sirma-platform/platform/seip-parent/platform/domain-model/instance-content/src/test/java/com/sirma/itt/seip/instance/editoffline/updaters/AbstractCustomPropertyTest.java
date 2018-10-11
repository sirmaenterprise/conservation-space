package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.sirma.itt.seip.testutil.io.FileTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * @author Boyan Tonchev.
 */
public abstract class AbstractCustomPropertyTest {

	protected static final String INSTANCE_ID = "emf:id";

	@Mock
	protected InstanceContentService instanceContentService;

	@Mock
	protected SystemConfiguration systemConfiguration;

	@Mock
	protected TempFileProvider tempFileProvider;

	@Mock
	protected InstanceTypeResolver instanceTypeResolver;

	/**
	 * Runs before each method and setup mockito.
	 */
	@Before
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests method update scenario with FileCustomPropertiesUpdateException.
	 * @param customPropertyUpdater custom property updater to be used into test.
	 * @param outputFileName fileName of instance to be use into test.
	 * @param testFilePath path to test file this file will be copied which will be used for test. When test finish created
	 *                     file will be delete.
	 * @throws IOException
	 */
	protected void updateFileCustomPropertiesUpdateException(CustomPropertyUpdater customPropertyUpdater,
			String outputFileName, String testFilePath) throws IOException {
		File fileIn = null;
		String errorMessage = "Test for update fail for custom property with exception: " + customPropertyUpdater.getClass().getName();
		try {
			ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
			Mockito.when(contentInfo.getName()).thenReturn(outputFileName);
			Mockito.when(instanceContentService.getContent(INSTANCE_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
			Mockito.when(systemConfiguration.getRESTRemoteAccessUrl()).thenReturn(new ConfigurationPropertyMock<>(URI.create("http://localhost/emf/api")));
			InstanceReference mockedInstaceReference = Mockito.mock(InstanceReference.class);
			Instance mockedInstance = Mockito.mock(Instance.class);
			Mockito.when(mockedInstaceReference.toInstance()).thenReturn(mockedInstance);
			Mockito.when(instanceTypeResolver.resolveReference(INSTANCE_ID)).thenReturn(Optional.of(mockedInstaceReference));
			fileIn = FileTestUtils.copyFileToTempDir(testFilePath, errorMessage);
			Mockito.when(tempFileProvider.createTempFile(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(fileIn);

			customPropertyUpdater.update(INSTANCE_ID);
		} finally {
			if (fileIn != null) {
				FileTestUtils.deleteFile(new File(fileIn.getParent(), outputFileName));
				FileTestUtils.deleteFile(fileIn);
			}
		}
	}

	/**
	 * Test update method right flow.
	 * @param customPropertyUpdater custom property updater to be used into test.
	 * @param outputFileName fileName of instance to be use into test.
	 * @param testFilePath path to test file this file will be copied which will be used for test. When test finish created
	 *                     file will be delete.
	 * @throws IOException
	 */
	protected void update(CustomPropertyUpdater customPropertyUpdater,
			String outputFileName, String testFilePath) throws IOException {
		File fileIn = null;
		File result = null;
		String errorMessage = "Test for update fail for custom property: " + customPropertyUpdater.getClass().getName();
		try {
			ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
			Mockito.when(contentInfo.getName()).thenReturn(outputFileName);
			Mockito.when(instanceContentService.getContent(INSTANCE_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
			Mockito.when(systemConfiguration.getRESTRemoteAccessUrl()).thenReturn(new ConfigurationPropertyMock<>(URI.create("http://localhost/emf/api")));
			InstanceReference mockedInstaceReference = Mockito.mock(InstanceReference.class);
			Instance mockedInstance = Mockito.mock(Instance.class);
			Mockito.when(mockedInstaceReference.toInstance()).thenReturn(mockedInstance);
			Mockito.when(instanceTypeResolver.resolveReference(INSTANCE_ID)).thenReturn(Optional.of(mockedInstaceReference));
			fileIn = FileTestUtils.copyFileToTempDir(testFilePath, errorMessage);
			Mockito.when(tempFileProvider.createTempFile(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(fileIn);

			result = customPropertyUpdater.update(INSTANCE_ID);

			Assert.assertNotNull(errorMessage, result);
			assertCustomProperty(result, errorMessage);
			Mockito.verify(tempFileProvider).deleteFile(fileIn);


		} finally {
			FileTestUtils.deleteFile(result);
			FileTestUtils.deleteFile(fileIn);
		}
	}

	/**
	 * Assert if custom property is set.
	 *
	 * @param file
	 */
	public abstract void assertCustomProperty(File file, String errorMessage);
}
