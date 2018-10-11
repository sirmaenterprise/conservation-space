package com.sirma.sep.content.preview.generator;

import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import com.sirma.sep.content.preview.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for generating previews and thumbnails for provided content. This service requires LibreOffice and Image
 * Magic to be present in the user's PATH.
 *
 * @author Mihail Radkov
 */
@Service
public class ContentPreviewGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final int DEFAULT_TIMEOUT_MULTIPLIER = 1;

	public static final String PDF_EXTENSION = ".pdf";

	private final ProcessProvider processProvider;
	private final ContentPreviewConfiguration contentPreviewConfiguration;

	/**
	 * Instantiates a new content preview generator with the provided {@link ProcessProvider} and {@link
	 * ContentPreviewConfiguration}.
	 *
	 * @param processProvider
	 * 		- a process provider used to provide the generator with instantiated processes
	 * @param contentPreviewConfiguration
	 * 		- global configurations for the preview application
	 */
	@Autowired
	public ContentPreviewGenerator(ProcessProvider processProvider,
			ContentPreviewConfiguration contentPreviewConfiguration) {
		this.processProvider = processProvider;
		this.contentPreviewConfiguration = contentPreviewConfiguration;
	}

	/**
	 * Generates a preview for the provided content by executing a process that calls LibreOffice with proper arguments
	 * for headless conversion.
	 * <p>
	 * If the executed process couldn't generate a preview, the returned content may not exist. Users should check for
	 * its existence with {@link File#exists()}.
	 * <p>
	 * To increase the generation timeout defined in {@link ContentPreviewConfiguration#getTimeout()} pass a timeout
	 * multiplier greater than 1. Used to allow preview generation for larger files which require more time in next attempts.
	 *
	 * @param content
	 * 		an existing {@link File} for which a preview should be generated.
	 * @param timeoutMultiplier
	 * 		number greater than zero
	 * @return a {@link File} with the generated content preview. Never {@code null}
	 */
	public File generatePreview(File content, int timeoutMultiplier) {
		String inputDocument = content.getAbsolutePath();
		File pidFile = new File(inputDocument + ".pid");

		try {
			execute(Arrays.asList("libreoffice", "--nologo", "--norestore", "--invisible", "--headless",
					"--nolockcheck", "--nodefault", "--pidfile=" + pidFile.getPath(), "--convert-to",
					"pdf:writer_pdf_Export", "--outdir", content.getParent(), inputDocument), timeoutMultiplier);
		} finally {
			// Existing means LibreOffice is hanging
			if (pidFile.exists()) {
				killLibreOffice(pidFile);
			}
		}

		return new File(FileUtils.withoutExtension(inputDocument) + PDF_EXTENSION);
	}

	private void killLibreOffice(File pidFile) {
		try {
			String processID = new String(Files.readAllBytes(pidFile.toPath()));
			execute(Arrays.asList("kill", processID), DEFAULT_TIMEOUT_MULTIPLIER);
			LOGGER.warn("Killed LibreOffice with pid={}", processID);
		} catch (IOException ex) {
			throw new IllegalStateException("Couldn't read from LibreOffice's process ID file!", ex);
		} finally {
			FileUtils.deleteFile(pidFile);
		}
	}

	/**
	 * Generates a thumbnail for the provided content preview by executing a process that calls Image Magic's convert
	 * CLI tool with proper arguments.
	 * <p>
	 * If the executed process couldn't generate a thumbnail, the returned file may not exist. Users should check for
	 * its existence with {@link File#exists()}.
	 *
	 * @param preview
	 * 		- an existing {@link File} for which a thumbnail should be generated.
	 * @return - a {@link File} with the generated preview thumbnail. Never {@code null}
	 */
	public File generateThumbnail(File preview) {
		String previewPath = preview.getAbsolutePath();
		String withoutExtension = FileUtils.withoutExtension(previewPath);
		String outputPath = withoutExtension + "." + contentPreviewConfiguration.getThumbnailFormat();
		execute(Arrays.asList("convert", previewPath + "[0]", "-thumbnail", "64x64", "-quality", "95", outputPath),
				DEFAULT_TIMEOUT_MULTIPLIER);

		return new File(outputPath);
	}

	private void execute(List<String> commandAndArguments, int timeoutMultiplier) {
		Process process = null;
		try {
			process = processProvider.getProcess(commandAndArguments);

			boolean hasFinishedOnTime = process.waitFor(contentPreviewConfiguration.getTimeout() * timeoutMultiplier,
					TimeUnit.MILLISECONDS);
			if (!hasFinishedOnTime) {
				throw new IllegalStateException("Generation hasn't finished on time!");
			}
			if (process.exitValue() != 0) {
				try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
					String errorOutput = errorReader.lines().collect(Collectors.joining(System.lineSeparator()));
					throw new IllegalStateException(
							"Generation exited with " + process.exitValue() + ", reason:" + System.lineSeparator()
									+ errorOutput);
				}
			}
		} catch (IOException | InterruptedException ex) {
			throw new IllegalStateException("Generation was interrupted!", ex);
		} finally {
			destroyProcess(process);
		}
	}

	private static void destroyProcess(Process process) {
		if (process != null && process.isAlive()) {
			process.destroy();
			if (process.isAlive()) {
				process.destroyForcibly();
			}
		}
	}
}
