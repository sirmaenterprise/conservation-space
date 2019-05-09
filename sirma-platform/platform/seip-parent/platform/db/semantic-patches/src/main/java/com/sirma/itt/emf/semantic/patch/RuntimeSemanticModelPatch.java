package com.sirma.itt.emf.semantic.patch;

import static com.sirma.itt.emf.semantic.patch.UpdateSemanticTask.FILE_TYPE_NAMESPACES;
import static com.sirma.itt.emf.semantic.patch.UpdateSemanticTask.FILE_TYPE_RDF;
import static com.sirma.itt.emf.semantic.patch.UpdateSemanticTask.FILE_TYPE_SPARQL;
import static com.sirma.itt.emf.semantic.patch.UpdateSemanticTask.NAMESPACES_FILE_EXTENSION;
import static com.sirma.itt.emf.semantic.patch.UpdateSemanticTask.NS_FILE_EXTENSION;
import static com.sirma.itt.emf.semantic.patch.UpdateSemanticTask.SPARQL_FILE_EXTENSION;
import static com.sirma.itt.seip.db.patch.DBSchemaPatchResourceAccessor.CHANGELOG_TEMPLATE;
import static com.sirma.itt.seip.db.patch.DBSchemaPatchResourceAccessor.CHANGELOG_TEMPLATE_ZONE;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.db.patch.DBSchemaPatchResourceAccessor;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.ResourceLoadUtil;

/**
 * Ontology changelog for runtime execution.
 *
 * @author bbanchev
 */
public class RuntimeSemanticModelPatch implements SemanticSchemaPatches {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String CHANGELOG_XML_SUFFIX = "-changelog.xml";
	private static final String[] ALLOWED_FILES = { NAMESPACES_FILE_EXTENSION, NS_FILE_EXTENSION,
			SPARQL_FILE_EXTENSION };
	private static final String CHANGE_SET_TEMPLATE = "<changeSet id=\"%s\" author=\"system\">\r\n"
			+ "		<comment>Generated change set for inserting file</comment>\r\n"
			+ "			<customChange class=\"com.sirma.itt.emf.semantic.patch.UpdateSemanticTask\">\r\n"
			+ "				<param name=\"fileName\" value=\"${currentdir}/%s\" />\r\n"
			+ "				<param name=\"fileType\" value=\"%s\" />\r\n" + "			</customChange>\r\n"
			+ "		</changeSet>";
	private File patchPath;

	/**
	 * Instantiates a new runtime semantic repository update.
	 *
	 * @param location
	 *            the location - -changelog.xml file or directory containing it
	 */
	public RuntimeSemanticModelPatch(File location) {
		patchPath = getPatchPath(location);
		if (patchPath == null) {
			throw new EmfRuntimeException(
					location + " is not a valid directory containing patch files or a valid patch file!");
		}
	}

	private static File getPatchPath(File location) {
		if (location == null) {
			return null;
		}
		File result = null;
		if (location.isDirectory()) {
			result = handleDirectory(location);
		} else if (location.canRead() && location.getName().endsWith(CHANGELOG_XML_SUFFIX)) {
			result = location;
		}
		return result;
	}

	private static File handleDirectory(File location) {
		File result;
		File[] list = location.listFiles((dir, name) -> name.endsWith(CHANGELOG_XML_SUFFIX));
		if (list != null && list.length == 1) {
			result = list[0];
		} else {
			result = generateChangelogFile(location);
		}
		return result;
	}

	private static File generateChangelogFile(File location) {
		File[] files = loadSemanticFiles(location);

		try {
			StringBuilder changeSetBuilder = new StringBuilder(2048);
			addChanges(files, changeSetBuilder);

			File changeLogFile = new File(location, RandomStringUtils.randomAlphanumeric(5) + CHANGELOG_XML_SUFFIX);
			String template = ResourceLoadUtil.loadResource(DBSchemaPatchResourceAccessor.class, CHANGELOG_TEMPLATE);
			String changes = template.replace(CHANGELOG_TEMPLATE_ZONE, changeSetBuilder.toString());
			FileUtils.writeStringToFile(changeLogFile, changes, StandardCharsets.UTF_8);
			return changeLogFile;
		} catch (IOException e) {
			throw new EmfRuntimeException("Error occurred when processing changelog template", e);
		}
	}

	private static void addChanges(File[] files, StringBuilder changeSetBuilder) {
		for (File file : files) {
			String fileName = file.getName();
			String fileType = getFileType(fileName);
			if (fileType == null) {
				continue;
			}

			String changeSet = String.format(CHANGE_SET_TEMPLATE, UUID.randomUUID().toString(), fileName, fileType);
			changeSetBuilder.append(changeSet).append(System.lineSeparator());
		}
	}

	private static String getFileType(String fileName) {
		String fileType = null;
		if (RDFParserRegistry.getInstance().getFileFormatForFileName(fileName).isPresent()) {
			fileType = FILE_TYPE_RDF;
		} else if (fileName.endsWith(SPARQL_FILE_EXTENSION)) {
			fileType = FILE_TYPE_SPARQL;
		} else if (fileName.endsWith(NAMESPACES_FILE_EXTENSION) || fileName.endsWith(NS_FILE_EXTENSION)) {
			fileType = FILE_TYPE_NAMESPACES;
		} else {
			LOGGER.warn("Cannot import patch {}! Unsupported format!", fileName);
		}
		return fileType;
	}

	@Override
	public String getPath() {
		return patchPath.getAbsolutePath();
	}

	@Override
	public List<StringPair> getProperties() {
		return Collections.singletonList(new StringPair("currentdir", patchPath.getParentFile().getAbsolutePath()));
	}

	/**
	 * Loads all files that have extension described in ALLOWED_FILES or have RDF type extension
	 *
	 * @param location
	 *            File Location
	 * @return List of files that answer the file type condition
	 */
	private static File[] loadSemanticFiles(File location) {
		File[] files = location.listFiles((dir, name) -> {
			String nameToLower = name.toLowerCase();
			for (String suffix : ALLOWED_FILES) {
				if (nameToLower.endsWith(suffix)) {
					return true;
				}
			}
			return RDFParserRegistry.getInstance().getFileFormatForFileName(name).isPresent();
		});
		Arrays.sort(files);
		return files;
	}
}