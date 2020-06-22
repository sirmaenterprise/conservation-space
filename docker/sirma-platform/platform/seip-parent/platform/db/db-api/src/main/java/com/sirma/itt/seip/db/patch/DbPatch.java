package com.sirma.itt.seip.db.patch;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.StringPair;

/**
 * All db schema patches should implement it and provide the class path for the xml file containing the change log.
 *
 * @author BBonev
 */
public interface DbPatch {

	/**
	 * Provides the path to the patch inside the java classpath.
	 *
	 * @return patch path.
	 */
	String getPath();

	/**
	 * Builds the path to file from the current package so it could be returned from the method {@link #getPath()}
	 *
	 * @param fileName
	 *            the file name
	 * @return the string
	 */
	default String buildPathFromCurrentPackage(String fileName) {
		String name = fileName;
		if (!name.startsWith("/")) {
			name = '/' + name;
		}
		return getClass().getPackage().getName().replace(".", "/") + name;
	}

	/**
	 * Return list of properties to add to the change log. No check if duplication exists is performed
	 * 
	 * @return list of properties to add to change log
	 */
	default List<StringPair> getProperties() {
		return Collections.emptyList();
	}
}
