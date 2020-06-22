package com.sirma.itt.seip.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The Class FileUtil holds some basic methods to work with files.
 *
 * @author BBonev
 */
public class FileUtil {

	public static final Pattern FILE_NAME_CLEAN_PATTERN = Pattern.compile("[\\\\/:*<>?|\"\\r\\n]");
	private static final Pattern FILE_NAME_ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("[\\\\/:;*?\"<>|%#@$^`~{},\\r\\n]");

	private static final String DEFAULT_EXTENSION = "tmp";

	/**
	 * Instantiates a new file util.
	 */
	private FileUtil() {
		// utility class
	}

	/**
	 * Split name and extension and return as pair. If the extension could not be obtained null is returned as second
	 * value.
	 *
	 * @param filenameProperty
	 *            could be file or string
	 * @return the pair &lt;name,extension&gt;
	 */
	public static Pair<String, String> splitNameAndExtension(Object filenameProperty) {
		Pair<String, String> result = Pair.NULL_PAIR;
		// Extract the extension
		if (filenameProperty != null) {
			String filename = null;
			if (filenameProperty instanceof File) {
				filename = ((File) filenameProperty).getName();
			} else {
				filename = filenameProperty.toString();
			}
			result = new Pair<>(filename, null);
			if (filename.length() > 0) {
				int index = filename.lastIndexOf('.');
				if (index > -1 && index < filename.length() - 1) {
					result.setFirst(filename.substring(0, index));
					result.setSecond(filename.substring(index + 1).toLowerCase());
				} else {
					result.setFirst(filename);
				}
			}
		}
		return result;
	}

	/**
	 * Gets file name without the extension
	 *
	 * @param filenameProperty
	 *            the filename property
	 * @return the name without extension if any
	 */
	public static String getName(Object filenameProperty) {
		return splitNameAndExtension(filenameProperty).getFirst();
	}

	/**
	 * Gets the file extension if any
	 *
	 * @param filenameProperty
	 *            the filename property
	 * @return the extension if any or <code>null</code>
	 */
	public static String getExtension(Object filenameProperty) {
		return splitNameAndExtension(filenameProperty).getSecond();
	}

	/**
	 * Converts bytes into human readable format.
	 *
	 * @param bytes
	 *            bytes to convert.
	 * @return human readable string.
	 */
	public static String humanReadableByteCount(long bytes) {
		int unit = 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "kMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f%sB", Double.valueOf(bytes / Math.pow(unit, exp)), pre);
	}

	/**
	 * Ensure valid name.
	 *
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String ensureValidName(String name) {
		return ensureValidName(name, DEFAULT_EXTENSION);
	}

	/**
	 * Ensure valid name.
	 *
	 * @param name
	 *            the name
	 * @param preferredExtension
	 *            the preferred extension
	 * @return the string
	 */
	public static String ensureValidName(String name, String preferredExtension) {
		String localExt = preferredExtension;
		if (localExt == null) {
			localExt = DEFAULT_EXTENSION;
		}
		if (name == null) {
			return UUID.randomUUID() + "." + localExt;
		}

		String localName = FILE_NAME_CLEAN_PATTERN.matcher(name).replaceAll("");

		if (localName.getBytes(StandardCharsets.UTF_8).length > 255) {
			String extension = splitNameAndExtension(localName).getSecond();
			if (StringUtils.isBlank(extension)) {
				extension = "." + localExt;
			}
			return System.nanoTime() + extension;
		}
		return localName;
	}

	/**
	 * Checks if the given content is an xml.
	 *
	 * @param content
	 *            the content
	 * @return true, if is xml
	 */
	public static boolean isXml(String content) {
		if (StringUtils.isBlank(content)) {
			return false;
		}
		return content.startsWith("<") && content.endsWith(">");
	}

	/**
	 * Convert given file name to valid file name by replacing all illegal characters with underscores
	 *
	 * @param fileName
	 *            the source file name to process
	 * @return valid file name
	 */
	public static String convertToValidFileName(String fileName) {
		String validFileName = FILE_NAME_ILLEGAL_CHARACTERS_PATTERN.matcher(fileName).replaceAll("_").trim();
		if (validFileName.length() > 255) {
			validFileName = validFileName.substring(0, 255);
		}
		return validFileName;
	}

	/**
	 * Given a directory path, loads all files from it by traversing the file tree depth-first.
	 * 
	 * @param path is the directory path
	 * @return is the list of loaded {@link File}
	 */
	public static List<File> loadFromPath(String path) {
		try (Stream<Path> pathsStream = Files.walk(Paths.get(path))) {
			return pathsStream
					.filter(Files::isRegularFile)
					.map(Path::toFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new EmfRuntimeException("Failed to read files from directory " + path, e);
		}
	}
}
