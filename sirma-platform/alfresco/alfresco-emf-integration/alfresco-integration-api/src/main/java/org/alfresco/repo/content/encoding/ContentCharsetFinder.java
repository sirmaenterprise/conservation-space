/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.encoding;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.alfresco.encoding.AbstractCharactersetFinder;
import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.service.cmr.repository.MimetypeService;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Utility bean to guess the charset given a stream and a mimetype.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class ContentCharsetFinder {

	/** The default charset. */
	private Charset defaultCharset = Charset.defaultCharset();

	/** The mimetype service. */
	private MimetypeService mimetypeService;

	/** The characterset finders. */
	private List<CharactersetFinder> charactersetFinders;

	/**
	 * Instantiates a new content charset finder.
	 */
	public ContentCharsetFinder() {

	}

	/**
	 * The Class IBMCharsetFinder is the lastest detector used.
	 */
	public class IBMCharsetFinder extends AbstractCharactersetFinder {

		/**
		 * Instantiates a new iBM charset finder.
		 */
		private IBMCharsetFinder() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.alfresco.encoding.AbstractCharactersetFinder#detectCharsetImpl
		 * (byte[])
		 */
		@Override
		protected Charset detectCharsetImpl(byte[] buffer) throws Exception {
			CharsetDetector detector = new CharsetDetector();
			detector.setText(buffer);
			CharsetMatch match = detector.detect();

			if (match != null && match.getConfidence() > getThreshold()) {
				try {
					return Charset.forName(match.getName());
				} catch (UnsupportedCharsetException e) {
					// LOGGER.info("Charset detected as " + match.getName()
					// +
					// " but the JVM does not support this, detection skipped");
				}
			}
			return null;
		}

		/**
		 * Return the matching threshold before we decide that what we detected
		 * is a good match. In the range 0-100.
		 * 
		 * @return the threshold
		 */
		public int getThreshold() {
			return 10;
		}

	}

	/**
	 * Override the system default charset. Where the characterset cannot be
	 * determined for a mimetype and input stream, this mimetype will be used.
	 * The default is 'UTF-8'.
	 * 
	 * @param defaultCharset
	 *            the default characterset
	 */
	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = Charset.forName(defaultCharset);
	}

	/**
	 * Set the mimetype service that will help determine if a particular
	 * mimetype can be treated as encoded text or not.
	 * 
	 * @param mimetypeService
	 *            the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * Set the list of characterset finder to execute, in order, for text based
	 * content.
	 * 
	 * @param charactersetFinders
	 *            a list of finders
	 */
	public void setCharactersetFinders(List<CharactersetFinder> charactersetFinders) {
		this.charactersetFinders = charactersetFinders;
		// add the fallback finder
		addCharactersetFinder(new IBMCharsetFinder());
	}

	/**
	 * Adds the characterset finder.
	 * 
	 * @param charactersetFinder
	 *            the characterset finder
	 */
	public void addCharactersetFinder(CharactersetFinder charactersetFinder) {
		if (charactersetFinder != null && !this.charactersetFinders.contains(charactersetFinder)) {
			this.charactersetFinders.add(charactersetFinder);
		}
	}

	/**
	 * Gets the characterset from the stream, if the mimetype is text and the
	 * text has enough information to give the encoding away. Otherwise, the
	 * default is returned.
	 * 
	 * @param is
	 *            a stream that will not be affected by the call, but must
	 *            support marking
	 * @param mimetype
	 *            the mimetype of the stream data - <tt>null</tt> if not known
	 * @return returns a characterset and never <tt>null</tt>
	 */
	public Charset getCharset(InputStream is, String mimetype) {
		if (mimetype == null) {
			return defaultCharset;
		}
		// Is it text?
		if (!mimetypeService.isText(mimetype)) {
			return defaultCharset;
		}
		// Try the finders
		Charset charset = null;
		for (CharactersetFinder finder : charactersetFinders) {
			charset = finder.detectCharset(is);
			if (charset != null) {
				break;
			}
		}
		// Done
		if (charset == null) {
			return defaultCharset;
		} else {
			return charset;
		}
	}
}
