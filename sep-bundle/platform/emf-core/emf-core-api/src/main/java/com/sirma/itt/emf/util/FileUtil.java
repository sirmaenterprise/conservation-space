package com.sirma.itt.emf.util;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;

/**
 * The Class FileUtil holds some basic methods to work with files.
 */
public class FileUtil {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * Split name and extension and return as pair. If the extension could not be obtained null is
	 * returned as second value.
	 *
	 * @param filenameProperty
	 *            could be file or string
	 * @return the pair <name,extension>
	 */
	public static Pair<String, String> splitNameAndExtension(Object filenameProperty) {
		@SuppressWarnings("unchecked")
		Pair<String, String> result = Pair.NULL_PAIR;
		// Extract the extension
		if (filenameProperty != null) {
			String filename = null;
			if (filenameProperty instanceof File) {
				filename = ((File) filenameProperty).getName();
			} else {
				filename = filenameProperty.toString();
			}
			result = new Pair<String, String>(filename, null);
			if (filename.length() > 0) {
				int index = filename.lastIndexOf('.');
				if ((index > -1) && (index < (filename.length() - 1))) {
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
	 * Converts bytes into human readable format.
	 * 
	 * @param bytes
	 *            bytes to convert.
	 * @return human readable string.
	 */
	public static String humanReadableByteCount(long bytes) {
		int unit = 1000;
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
		try {
			if (name.getBytes("UTF-8").length > 255) {
				String extension = splitNameAndExtension(name).getSecond();
				if (StringUtils.isNullOrEmpty(extension)) {
					extension = ".tmp";
				}
				return System.nanoTime() + extension;
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug("Invalid encoding", e);
		}
		return name;
	}
}
