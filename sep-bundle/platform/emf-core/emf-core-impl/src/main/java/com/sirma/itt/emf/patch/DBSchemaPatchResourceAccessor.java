package com.sirma.itt.emf.patch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Extends the built-in {@link ClassLoaderResourceAccessor} by providing a dynamically constructed
 * xml for the fictive changeset.
 * 
 * @author Adrian Mitev
 */
public class DBSchemaPatchResourceAccessor extends ClassLoaderResourceAccessor {

	public static final String FICTIVE_CHANGELOG = "$fictive_changelog$.xml";
	private static final String CHANGELOG_TEMPLATE = "changelog-template.xml";
	private static final String CHANGELOG_TEMPLATE_ZONE = "$template$";
	private static final String FILE_INCLUDE = "<include file=\"%s\" />";

	private final String constructedChangelog;

	/**
	 * Initializes the changelog xml that will be used as a root of the patch session based on the
	 * provided patches.
	 * 
	 * @param patches
	 *            all patches that should be executed.
	 */
	public DBSchemaPatchResourceAccessor(Iterable<DBSchemaPatch> patches) {
		super(Thread.currentThread().getContextClassLoader());

		try (InputStream templateStream = getClass().getResourceAsStream(CHANGELOG_TEMPLATE);) {
			try (Scanner scanner = new Scanner(templateStream, "UTF-8").useDelimiter("\\A")) {
				String template = scanner.next();

				StringBuilder changelogbBuilder = new StringBuilder(600);
				changelogbBuilder.append(template);
				// remove template string
				int templateZoneIndex = changelogbBuilder.indexOf(CHANGELOG_TEMPLATE_ZONE);
				changelogbBuilder.delete(templateZoneIndex, templateZoneIndex
						+ CHANGELOG_TEMPLATE_ZONE.length());

				// add includes
				for (DBSchemaPatch patch : patches) {
					String currentInclude = String.format(FILE_INCLUDE, patch.getPath());
					changelogbBuilder.insert(templateZoneIndex, currentInclude);
					templateZoneIndex = templateZoneIndex + currentInclude.length();
				}

				constructedChangelog = changelogbBuilder.toString();
			}
		} catch (IOException e) {
			throw new RuntimeException("Error occured when processing changelog template", e);
		}

	}

	@Override
	public InputStream getResourceAsStream(String file) throws IOException {
		// provide the dynamic changelog if the fictive file is requested
		if (file.equals(FICTIVE_CHANGELOG)) {
			return new ByteArrayInputStream(constructedChangelog.getBytes(StandardCharsets.UTF_8));
		}
		return super.getResourceAsStream(file);
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
