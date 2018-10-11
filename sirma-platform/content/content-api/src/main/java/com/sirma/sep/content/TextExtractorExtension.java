package com.sirma.sep.content;

import java.io.IOException;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin for text extraction of documents. Implementation might include tika or ocr
 *
 * @author bbanchev
 */
@Documentation("The Interface TextExtractor is extension for text content extractors")
public interface TextExtractorExtension extends Plugin {

	/** The target name of extension. */
	String TARGET_NAME = "TextExtractor";

	/**
	 * Checks if current extractor could handle the given mimetype
	 *
	 * @param mimetype       the mime type of the content that we want to extract from
	 * @param fileDescriptor the file descriptor.
	 * @return <code>true</code> if the current extractor can handle the given mime type
	 */
	boolean isApplicable(String mimetype, FileDescriptor fileDescriptor);

	/**
	 * Extract the text and return it as a string
	 *
	 * @param instance
	 *            is the instance to get the data from
	 * @return the extracted content or null if no extraction is possible
	 * @throws IOException
	 *             on any error during extraction
	 */
	String extract(FileDescriptor instance) throws IOException;
}
