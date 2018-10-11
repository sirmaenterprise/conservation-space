package org.alfresco.repo.content.transform;

import java.io.File;
import java.util.Map;

import org.alfresco.util.Pair;
import org.apache.tika.parser.Parser;

/**
 * Base parser for email messages that has alternative parts - html or rtf.
 *
 * @author hackyou
 */
public interface AlternativeContentParser extends Parser {

	/** The mimetype rtf. */
	String MIMETYPE_RTF = "application/rtf";
	/** The UTF_8 encoding. */
	String UTF_8 = "UTF-8";

	/**
	 * Gets the parsed alternatives.
	 *
	 * @return list of alternative files keyed with mimetype of part. The pair is the file with its
	 *         encoding.
	 */
	public Map<String, Pair<File, String>> getAlternatives();
}
