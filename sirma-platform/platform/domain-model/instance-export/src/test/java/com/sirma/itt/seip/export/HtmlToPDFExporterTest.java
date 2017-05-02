package com.sirma.itt.seip.export;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Test for {@link PDFExporter}.
 *
 * @author A. Kunchev
 */
public class HtmlToPDFExporterTest {

	@InjectMocks
	private HtmlToPDFExporter exporter = new HtmlToPDFExporter();

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private ConfigurationProperty<String> phantomjsLocation;

	@Mock
	private ConfigurationProperty<Set<String>> phantomjsArguments;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private ConfigurationProperty<String> uriConfiguration;

	@Mock
	private ConfigurationProperty<String> jsFilePath;

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private ConfigurationProperty<Integer> timeout;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		EmfUser user = new EmfUser("user");
		user.setTicket("ticket");
		when(securityContextManager.getAdminUser()).thenReturn(user);
	}

	@Test
	public void initialize_tempFileProviderCalled() {
		exporter.initialize();
		verify(tempFileProvider).createLongLifeTempDir(anyString());
	}

	@Test
	public void exportToPdf_exportToolNotSet() throws URISyntaxException, TimeoutException {
		when(systemConfiguration.getUi2Url()).thenReturn(uriConfiguration);
		when(uriConfiguration.get()).thenReturn("/uri/someUri");
		mockTempFileProvider();
		when(phantomjsLocation.isNotSet()).thenReturn(true);
		exporter.initialize();
		File pdf = exporter.export("/url/someUrl");
		assertNull(pdf);
	}

	@Test
	public void exportToPdf_exportToolSet_fileResult() throws URISyntaxException, IOException, TimeoutException {
		when(systemConfiguration.getUi2Url()).thenReturn(uriConfiguration);
		when(uriConfiguration.get()).thenReturn("/uri/someUri");
		mockTempFileProvider();
		when(phantomjsLocation.isNotSet()).thenReturn(false);
		when(phantomjsArguments.isSet()).thenReturn(false);
		when(jsFilePath.isNotSet()).thenReturn(false);
		Path path = Files.createTempFile(null, ".js");
		File file = path.toFile();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("some random content \n");
			writer.flush();
		}
		when(jsFilePath.get()).thenReturn(path.toString());
		when(timeout.get()).thenReturn(5);
		exporter.initialize();
		File pdf = exporter.export("/url/someUrl");
		assertNotNull(pdf);
		file.delete();
	}

	@Test
	public void getWorkingFileById_nullParam_nullResult() {
		assertNull(exporter.getWorkingFileById(null));
	}

	@Test
	public void getWorkingFileById_emptyParam_nullResult() {
		assertNull(exporter.getWorkingFileById(""));
	}

	@Test(expected = EmfRuntimeException.class)
	public void getWorkingFileById_exportDirNotExist_exception() {
		File file = mock(File.class);
		when(file.exists()).thenReturn(false);
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(file);
		exporter.initialize();
		exporter.getWorkingFileById("someUUID");
	}

	@Test(expected = EmfRuntimeException.class)
	public void getWorkingFileById_mkdirsFalse_exception() {
		File file = mock(File.class);
		when(file.exists()).thenReturn(false);
		when(file.mkdirs()).thenReturn(false);
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(file);
		exporter.initialize();
		exporter.getWorkingFileById("someUUID");
	}

	@Test
	public void getWorkingFileById_newFile() {
		exporter.initialize();
		mockTempFileProvider();
		exporter.initialize();
		File pdf = exporter.getWorkingFileById("someUUID");
		assertNotNull(pdf);
	}

	private void mockTempFileProvider() {
		File file = mock(File.class);
		when(file.exists()).thenReturn(true);
		when(file.mkdirs()).thenReturn(true);
		ReflectionUtils.setField(file, "path", "/path/somePath");
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(file);
		when(tempFileProvider.createTempFile(anyString(), anyString())).thenReturn(file);
	}

}
