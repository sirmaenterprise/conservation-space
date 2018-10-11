package com.sirma.sep.content;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provider for content digests. This will be called when content digest is needed in order to perform content reuse.
 * This allows providing custom processing of the content that need to be digested
 *
 * @see Content#allowReuse()
 * @author BBonev
 */
public interface ContentDigestProvider extends Plugin {

	String EXTENSION_NAME = "digestProvider";
	/**
	 * Checks if the current provider could handle the given content.
	 *
	 * @param content
	 *            the content that need to be digested
	 * @return <code>true</code> if could be handled
	 */
	boolean accept(Content content);

	/**
	 * Perform content digest
	 *
	 * @param content
	 *            the content to digest
	 * @return digested content as hex string
	 */
	String digest(Content content);
}
