package com.sirma.itt.seip.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Basic {@link FilenameFilter} that filter for specified extension
 *
 * @author BBonev
 */
public class FileExtensionFilter implements FilenameFilter {

	/** The extension to look for. */
	private String extension;

	/**
	 * Instantiates a new file extension filter.
	 *
	 * @param extension
	 *            the extension to accept
	 */
	public FileExtensionFilter(String extension) {
		if (extension == null) {
			throw new IllegalArgumentException("Cannot create filter without specifying what to filter for!");
		}
		this.extension = extension.toLowerCase();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(extension);
	}

}
