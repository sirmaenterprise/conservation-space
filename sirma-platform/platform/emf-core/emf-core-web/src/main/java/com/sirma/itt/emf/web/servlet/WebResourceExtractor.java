/**
 * Copyright (c) 2013 11.09.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.web.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Extracts zipped resources located in META-INF/resources of the jars so they can be hot redeployed. WARNING: Should be
 * used for development purposes only!!<br/>
 * Disabled by default.
 * <p>
 * <b>NOTE: Intentionally is not implemented to be changed at runtime! Do not change this behavior!</b>
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
public class WebResourceExtractor {

	private static final String PATH_SEPARATOR = "/";

	private static final Logger LOGGER = LoggerFactory.getLogger(WebResourceExtractor.class);

	private static final String WEBINF = "WEB-INF";

	private static final String METAINF_RESOURCES = "META-INF/resources";

	private Map<File, Long> jarFiles;

	private File base;

	private Map<String, Long> crcHashes;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "hot.redeploy.enabled", type = Boolean.class, defaultValue = "false", sensitive = true, system = true, label = "If hot redeploy functionality should be enabled.")
	private ConfigurationProperty<Boolean> hotRedeploy;

	private Thread extractorThread;

	private Thread cleanUpThread;

	/**
	 * Called when the servlet context is loaded. Registers itself as a filter.
	 *
	 * @param event
	 *            ServletContextEvent
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		if (hotRedeploy.get().booleanValue()) {
			ServletContext servletContext = event.getServletContext();

			Dynamic filterConfig = servletContext.addFilter(WebResourceServingFilter.class.getSimpleName(),
					WebResourceServingFilter.class);
			filterConfig.addMappingForUrlPatterns(null, true, "/*");

			// get list with all web jars (the ones containing web-fragment.xml)
			File lib = new File(event.getServletContext().getRealPath(PATH_SEPARATOR + WEBINF + "/lib"));

			FilenameFilter jarFilter = (directory, fileName) -> fileName.endsWith(".jar");

			File[] jars = lib.listFiles(jarFilter);

			jarFiles = getJarFiles(jars);

			crcHashes = new HashMap<>();
			base = new File(event.getServletContext().getRealPath(PATH_SEPARATOR));

			extractorThread = new Thread(new WebResourceExtractorWorker(),
					WebResourceExtractorWorker.class.getSimpleName());
			extractorThread.setDaemon(true);
			extractorThread.start();

			cleanUpThread = new Thread(new WebResourceRemovingWorker(),
					WebResourceRemovingWorker.class.getSimpleName());
			cleanUpThread.setDaemon(true);
			cleanUpThread.start();
		}
	}

	private Map<File, Long> getJarFiles(File[] jars) {
		Map<File, Long> result = new HashMap<>();
		if (jars == null) {
			return result;
		}
		for (File jar : jars) {
			if (jar.isFile()) {
				try (ZipFile zipFile = new ZipFile(jar)) {
					ZipEntry entry = zipFile.getEntry("META-INF/web-fragment.xml");
					if (entry != null) {
						// putting 0 will cause all jars to be scanned at startup
						result.put(jar, 0L);
					}
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return result;
	}

	/**
	 * On shutdown stop all workers. If not called running threads will remain running if the server is not stopped but
	 * only the application was shutdown.
	 */
	@PreDestroy
	public void onShutdown() {
		if (hotRedeploy.get().booleanValue()) {
			LOGGER.info("Shutting down workers");
			if (extractorThread != null) {
				extractorThread.interrupt();
			}
			if (cleanUpThread != null) {
				cleanUpThread.interrupt();
			}
		}
	}

	/**
	 * Periodically checks for modified files and synchronize them.
	 *
	 * @author Adrian Mitev
	 */
	private class WebResourceExtractorWorker implements Runnable {
		private final Logger log = LoggerFactory.getLogger(WebResourceExtractorWorker.class.getSimpleName());

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(50);
					unzipFiles();
				} catch (InterruptedException e) {
					log.warn("Extract worker has been shutdowned!");
					return;
				}
			}
		}

		/**
		 * Checks all web jars for changes by comparing the timestamps. When a jar is changed, all resources within the
		 * jar are checked for changes by comparing the CRC hash and when a resource is changed it is extracted in the
		 * root directory of the application.
		 */
		private void unzipFiles() {
			try {
				for (Entry<File, Long> entry : jarFiles.entrySet()) {
					File jarFile = entry.getKey();

					// if the file exists and is modified, process the zip entries in it
					long modifiedPlusLength = jarFile.lastModified() + jarFile.length();
					if (jarFile.exists() && jarFile.canRead() && modifiedPlusLength != entry.getValue()) {
						try (ZipFile zipFile = new ZipFile(jarFile)) {
							Enumeration<? extends ZipEntry> entries = zipFile.entries();
							while (entries.hasMoreElements()) {
								ZipEntry zipEntry = entries.nextElement();

								String entryName = zipEntry.getName();
								// process only entries in META-INF/resource because they are web
								// resources
								if (entryName.startsWith(METAINF_RESOURCES)) {
									File file = new File(base, entryName.replace(METAINF_RESOURCES, ""));
									if (zipEntry.isDirectory()) {
										file.mkdirs();
									} else {
										String filePath = file.getAbsolutePath();
										Long crc = crcHashes.get(filePath);
										// if crc for that file is not calculated yet, calculate it
										if (crc == null) {
											if (file.exists()) {
												crc = calculateCRC(file);
												crcHashes.put(filePath, crc);
											} else {
												crc = 0L;
											}
										}

										// if the file doesn't exists or is modified (compare the
										// CRC code of the zip entry with the CRC of the previously
										// processed resource with the same path
										if (!file.exists() || file.exists() && crc != zipEntry.getCrc()) {
											file.getParentFile().mkdirs();
											file.createNewFile();
											try (FileOutputStream outputStream = new FileOutputStream(file)) {
												IOUtils.copy(zipFile.getInputStream(zipEntry), outputStream);
											}

											log.trace("Syncronized: " + zipEntry.getName());

											crcHashes.put(filePath, zipEntry.getCrc());
										}
									}
								}
							}

							// update the timestamp
							jarFiles.put(jarFile, jarFile.lastModified() + jarFile.length());
						} catch (FileNotFoundException | ZipException e) {
							// Thrown for some reason even if i check that the file exists
							log.trace("Error processing files", e);
						}
					}
				}
			} catch (IOException | RuntimeException e) {
				log.trace("Error occured when processing unzipped files", e);
			}
		}

		/**
		 * Calculates the CRC code of a file.
		 *
		 * @param file
		 *            file which checksum to calculate
		 * @return the calculated checksum
		 */
		private long calculateCRC(File file) {
			try {
				CRC32 crc = new CRC32();
				crc.update(FileUtils.readFileToByteArray(file));
				return crc.getValue();
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	/**
	 * Periodically checks for removed files and synchronize (remove from war directory) them.
	 *
	 * @author Adrian Mitev
	 */
	private class WebResourceRemovingWorker implements Runnable {
		private final Logger log = LoggerFactory.getLogger(WebResourceRemovingWorker.class.getSimpleName());

		private final IOFileFilter fileFilter = new TrueFileFilter() {
			// nothing to do
		};

		private final IOFileFilter dirFilter = new AbstractFileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.getPath().contains(WEBINF);
			}
		};

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(10000);
					removeFiles();
				} catch (InterruptedException e) {// NOSONAR
					log.warn("Remove worker has been shutdowned!");
					return;
				}
			}
		}

		/**
		 * Check all files in the root directory of the .war. If the file is presented in the CRC cache (this means that
		 * it's managed by the WebResourceExtractor) look in all web jars to see if the file is there. If the file is
		 * not presented in any jar this means that the file was deleted from the workspace and it should also be
		 * deleted from the .war. WEB-INF directory is not scanned, because it's not managed by WebResourceExtractor.
		 */
		private void removeFiles() {
			if (!base.isDirectory()) {
				log.warn("The provided base {} is not a directory. Nothing will be removed!", base);
				return;
			}
			Iterator<File> iterator = FileUtils.iterateFiles(base, fileFilter, dirFilter);
			while (iterator.hasNext()) {
				File file = iterator.next();
				if (crcHashes.containsKey(file.getAbsolutePath())) {
					// iterate all jars to find if the file is missing
					String zipEntryName = METAINF_RESOURCES + file
							.getAbsolutePath()
								.substring(base.getAbsolutePath().length())
								.replace("\\", PATH_SEPARATOR);
					boolean contains = false;
					try {
						for (File jarFile : jarFiles.keySet()) {
							try (ZipFile zipFile = new ZipFile(jarFile)) {
								if (zipFile.getEntry(zipEntryName) != null) {
									contains = true;
									break;
								}
							}
						}
					} catch (IOException e) {
						// if something goes wrong with the jar reading silently catch the
						// exception and break the operation
						log.trace("Error reading jar files", e);
						continue;
					}

					if (!contains && file.exists() && file.canWrite() && file.delete()) {
						log.trace("Deleted " + zipEntryName);
					}
				}
			}
		}
	}

}
