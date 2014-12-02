package com.sirma.itt.emf.content.extract;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Plugin for text extraction of documents. Implementation might include tika or ocr
 *
 * @author bbanchev
 */
@Documentation("The Interface TextExtractor is extension for text content extractors")
public interface TextExtractor extends Plugin {

	/** The target name of extension. */
	String TARGET_NAME = "TextExtractor";

	/**
	 * Extract the text and return it as a string
	 *
	 * @param instance
	 *            is the instance to get the data from
	 * @return the extracted content or null if no extraction is possible
	 * @throws Exception
	 *             on any error during extraction
	 */
	String extract(FileDescriptor instance) throws Exception;

	/**
	 * Checks if is applicable for extracting by current extractor
	 *
	 * @param instance
	 *            the instance to check
	 * @return the current extractor if it is applicable or null if not
	 * @throws Exception
	 *             on any error during detection
	 */
	boolean isApplicable(Instance instance) throws Exception;
}
