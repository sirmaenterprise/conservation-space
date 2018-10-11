package com.sirma.itt.seip.db.patch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.exception.EmfRuntimeException;

import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Extends the built-in {@link ClassLoaderResourceAccessor} by providing a dynamically constructed xml for the fictive
 * changeset.
 *
 * @author Adrian Mitev
 * @author bbanchev
 */
public class DBSchemaPatchResourceAccessor extends ClassLoaderResourceAccessor {
	/** Fictive change log id. */
	public static final String FICTIVE_CHANGELOG = "$fictive_changelog$.xml";
	public static final String CHANGELOG_TEMPLATE = "changelog-template.xml";
	public static final String CHANGELOG_TEMPLATE_ZONE = "$template$";
	private static final String FILE_INCLUDE = "<include file=\"%s\" />";
	private static final String FILE_PROPERTY = "<property name=\"%s\" value=\"%s\"/>";
	private final String constructedChangelog;

	/**
	 * Initializes the changelog xml that will be used as a root of the patch session based on the provided patches.
	 *
	 * @param patches
	 *            all patches that should be executed.
	 */
	@SuppressWarnings("resource")
	public <D extends DbPatch> DBSchemaPatchResourceAccessor(Iterable<D> patches) {
		super(Thread.currentThread().getContextClassLoader());

		try (InputStream templateStream = getClass().getResourceAsStream(CHANGELOG_TEMPLATE);
				Scanner scanner = new Scanner(templateStream, "UTF-8").useDelimiter("\\A")) {

			String template = scanner.next();

			StringBuilder changelogbBuilder = new StringBuilder(2048);
			changelogbBuilder.append(template);
			// remove template string
			int templateZoneIndex = changelogbBuilder.indexOf(CHANGELOG_TEMPLATE_ZONE);
			changelogbBuilder.delete(templateZoneIndex, templateZoneIndex + CHANGELOG_TEMPLATE_ZONE.length());

			// add the properties initially
			for (D patch : patches) {
				String changeLog = collectProperties(patch);
				changelogbBuilder.insert(templateZoneIndex, changeLog);
				templateZoneIndex = templateZoneIndex + changeLog.length();
			}

			// add includes
			for (D patch : patches) {
				String changeLog = String.format(FILE_INCLUDE, patch.getPath());
				changelogbBuilder.insert(templateZoneIndex, changeLog);
				templateZoneIndex = templateZoneIndex + changeLog.length();
			}
			constructedChangelog = changelogbBuilder.toString();
		} catch (IOException e) {
			throw new EmfRuntimeException("Error occured when processing changelog template", e);
		}

	}

	private <D extends DbPatch> String collectProperties(D patch) {
		List<StringPair> properties = patch.getProperties();
		StringBuilder result = new StringBuilder();
		for (StringPair property : properties) {
			result.append(String.format(FILE_PROPERTY, property.getFirst(), property.getSecond()));
		}
		return result.toString();
	}

	@Override
	public Set<InputStream> getResourcesAsStream(String path) throws IOException {
		// provide the dynamic changelog if the fictive file is requested
		if (path.endsWith(FICTIVE_CHANGELOG)) {
			return Collections.singleton(
					(InputStream) new ByteArrayInputStream(constructedChangelog.getBytes(StandardCharsets.UTF_8)));
		}
		File file = new File(path);
		if (file.isAbsolute() && file.isFile()) {
			// the stream is closed by the library
			return Collections.singleton(new FileInputStream(file));
		}
		return super.getResourcesAsStream(path);
	}

	/**
	 * Getter method for constructedChangelog.
	 *
	 * @return the constructedChangelog
	 */
	public String getConstructedChangelog() {
		return constructedChangelog;
	}
}
