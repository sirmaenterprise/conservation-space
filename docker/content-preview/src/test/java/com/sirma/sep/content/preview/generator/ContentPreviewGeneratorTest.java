package com.sirma.sep.content.preview.generator;

import com.sirma.sep.content.preview.TestFileUtils;
import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests the thumbnail & preview generation in {@link ContentPreviewGenerator}
 *
 * @author Mihail Radkov
 */
public class ContentPreviewGeneratorTest {

	@Mock
	private ProcessProvider processBuilderProvider;
	@Spy
	private ContentPreviewConfiguration contentPreviewConfiguration;
	@InjectMocks
	private ContentPreviewGenerator previewProcessor;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		contentPreviewConfiguration.setThumbnailFormat("jpeg");
		contentPreviewConfiguration.setTimeout(5000L);
	}

	@Test
	public void shouldGeneratePreview() throws Exception {
		mockProcess(true, 0);
		String tempDirPath = TestFileUtils.getSystemTempDir().getPath();
		File content = new File(tempDirPath + File.separatorChar + "document.doc");
		File generatedPreview = previewProcessor.generatePreview(content, 1);
		Assert.assertNotNull(generatedPreview);
		Assert.assertEquals(tempDirPath + File.separatorChar + "document" + ContentPreviewGenerator.PDF_EXTENSION,
							generatedPreview.getAbsolutePath());
	}

	@Test
	public void previewGeneration_shouldProvideProcessArguments() throws Exception {
		Process process = mockProcess(true, 0);
		ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.when(processBuilderProvider.getProcess(listCaptor.capture())).thenReturn(process);

		String tempDirPath = TestFileUtils.getSystemTempDir().getPath();
		File content = new File(tempDirPath + File.separatorChar + "document.doc");
		previewProcessor.generatePreview(content, 1);

		List processArguments = listCaptor.getValue();
		Assert.assertEquals("libreoffice", processArguments.get(0));
		Assert.assertTrue(processArguments.contains("--pidfile=" + content.getAbsolutePath() + ".pid"));
		Assert.assertTrue(processArguments.contains(tempDirPath));
		Assert.assertTrue(processArguments.contains(content.getAbsolutePath()));
	}

	@Test
	public void previewGeneration_shouldUseTimeoutMultiplier() throws Exception {
		Process process = mockProcess(true, 0);
		Mockito.when(processBuilderProvider.getProcess(Matchers.anyListOf(String.class))).thenReturn(process);

		String tempDirPath = TestFileUtils.getSystemTempDir().getPath();
		File content = new File(tempDirPath + File.separatorChar + "document.doc");
		previewProcessor.generatePreview(content, 3);

		Mockito.verify(process).waitFor(Matchers.eq(15000L), Matchers.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void previewGeneration_shouldKillLibreOfficeIfHanging() throws Exception {
		Process process = mockProcess(true, 0);
		ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.when(processBuilderProvider.getProcess(listCaptor.capture())).thenReturn(process);

		Process killProcess = mockProcess(true, 0);
		ArgumentCaptor<List> killArgumentsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.when(processBuilderProvider.getProcess(killArgumentsCaptor.capture())).thenReturn(killProcess);

		String tempDirPath = TestFileUtils.getSystemTempDir().getPath();
		File content = new File(tempDirPath + File.separatorChar + "document.doc");
		// Simulate the hanging LibreOffice
		File pidFile = new File(content.getAbsolutePath() + ".pid");
		Files.write(pidFile.toPath(), "1234".getBytes());
		Assert.assertTrue(pidFile.exists());

		previewProcessor.generatePreview(content, 1);

		List killArguments = killArgumentsCaptor.getValue();
		Assert.assertEquals("kill", killArguments.get(0));
		Assert.assertTrue(killArguments.contains("1234"));
		Assert.assertFalse(pidFile.exists());
	}

	@Test
	public void shouldGenerateThumbnail() throws Exception {
		mockProcess(true, 0);
		String tempDirPath = TestFileUtils.getSystemTempDir().getPath();
		File preview = new File(tempDirPath + File.separatorChar + "document.pdf");
		File generatedThumbnail = previewProcessor.generateThumbnail(preview);
		Assert.assertNotNull(generatedThumbnail);
		Assert.assertEquals(tempDirPath + File.separatorChar + "document.jpeg", generatedThumbnail.getAbsolutePath());
	}

	@Test
	public void thumbnailGeneration_shouldProvideProcessArguments() throws Exception {
		Process process = mockProcess(true, 0);
		ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.when(processBuilderProvider.getProcess(listCaptor.capture())).thenReturn(process);

		String tempDirPath = TestFileUtils.getSystemTempDir().getPath();
		File preview = new File(tempDirPath + File.separatorChar + "document.pdf");
		previewProcessor.generateThumbnail(preview);

		List<String> processArguments = listCaptor.getValue();
		Assert.assertEquals("convert", processArguments.get(0));
		Assert.assertTrue(processArguments.contains(preview.getAbsolutePath() + "[0]"));
		Assert.assertTrue(processArguments.contains(tempDirPath + File.separatorChar + "document.jpeg"));
	}

	@Test(expected = IllegalStateException.class)
	public void notGeneratingPreviewOnTime_shouldFail() throws Exception {
		mockProcess(false, 0);
		previewProcessor.generatePreview(new File(""), 1);
	}

	@Test(expected = IllegalStateException.class)
	public void notGeneratingPreviewSuccessfully_shouldFail() throws Exception {
		mockProcess(true, 1);
		previewProcessor.generatePreview(new File(""), 1);
	}

	@Test(expected = IllegalStateException.class)
	public void notBeingAbleToStartTheProcess_shouldFail() throws Exception {
		Mockito.when(processBuilderProvider.getProcess(Matchers.anyListOf(String.class))).thenThrow(new IOException());
		previewProcessor.generatePreview(new File(""), 1);
	}

	@Test(expected = IllegalStateException.class)
	public void interruptingTheProcess_shouldFail() throws Exception {
		Process process = mockProcess(true, 0);
		Mockito.when(process.waitFor(Matchers.anyLong(), Matchers.eq(TimeUnit.MILLISECONDS)))
			   .thenThrow(new InterruptedException());
		previewProcessor.generatePreview(new File(""), 1);
	}

	@Test
	public void shouldDestroyProcesses() throws Exception {
		Process process = mockProcess(true, 0);
		Mockito.when(process.isAlive()).thenReturn(true).thenReturn(false);
		previewProcessor.generatePreview(new File(""), 1);
		Mockito.verify(process).destroy();
	}

	@Test
	public void shouldDestroyProcessesForcibly() throws Exception {
		Process process = mockProcess(true, 0);
		Mockito.when(process.isAlive()).thenReturn(true).thenReturn(true);
		previewProcessor.generatePreview(new File(""), 1);
		Mockito.verify(process).destroyForcibly();
	}

	private Process mockProcess(boolean finishOnTime, int exitValue) throws InterruptedException, IOException {
		Process process = Mockito.mock(Process.class);
		Mockito.when(process.waitFor(Matchers.anyLong(),
									 Matchers.eq(TimeUnit.MILLISECONDS))).thenReturn(finishOnTime);
		Mockito.when(process.exitValue()).thenReturn(exitValue);
		Mockito.when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("Error!".getBytes()));
		Mockito.when(process.destroyForcibly()).thenReturn(process);
		Mockito.when(processBuilderProvider.getProcess(Matchers.anyListOf(String.class))).thenReturn(process);
		return process;
	}
}
