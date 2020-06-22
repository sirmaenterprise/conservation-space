package com.sirma.itt.cmf.content.extract.mail;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The Interface MailExtractor is base extractor for diff standard mail messages.
 *
 * @param <T>
 *            the generic type
 */
@Documentation("The Interface MailExtractor is extension for different mail standarts extrators.")
public interface MailExtractor<T> extends Plugin {

	/** The target name of extension. */
	String TARGET_NAME = "MailExtractor";

	/** The part attachments. All Attachments are returned as list */
	String PART_ATTACHMENTS = "attachments";

	/**
	 * Parses the mail. Different results are returned depending on mailParts param. For example if
	 * {@link #PART_ATTACHMENTS} is provided as argument, all attachment will be returned as list keyed with
	 * {@link #PART_ATTACHMENTS}
	 *
	 * @param descriptor
	 *            the mail to parse
	 * @param deletedata
	 *            whether on extract to delete the corresponding part
	 * @param tempDir
	 *            the temp dir to use
	 * @param prefix
	 *            is the mail name used as prefix
	 * @param mailParts
	 *            the mail parts to process. If value is recognized is silently skipped.
	 * @return the map keyed by the mail part and the processed value. {@link #PART_ATTACHMENTS} is provided only
	 *         attachments are extracted and returned.
	 */
	Map<String, Object> extractMail(FileDescriptor descriptor, boolean deletedata, File tempDir, String prefix,
			String... mailParts);

	/**
	 * Open message using the file provided.
	 *
	 * @param file
	 *            the file
	 * @return the pair of message and encoding
	 * @throws Exception
	 *             the exception
	 */
	Pair<T, String> openMessage(File file) throws Exception;

	/**
	 * Open message using the stream.
	 *
	 * @param stream
	 *            the stream
	 * @return the pair of message and encoding
	 * @throws Exception
	 *             the exception
	 */
	Pair<T, String> openMessage(InputStream stream) throws Exception;

	/**
	 * Checks if is applicable for extracting by current extractor
	 *
	 * @param file
	 *            the file to check
	 * @return the current extractor if it is applicable or null if not
	 */
	MailExtractor<T> isApplicable(File file);
}
