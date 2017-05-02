/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package org.alfresco.repo.content.transform;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mbox.MboxParser;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Defines a Msg document content extractor. Based on
 * {@link org.apache.tika.parser.microsoft.OutlookExtractor}
 */
public class MSGParser implements AlternativeContentParser {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8969240499669909899L;

	/** The Constant SUPPORTED_TYPES. */
	private static final Set<MediaType> SUPPORTED_TYPES = Collections
			.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(MediaType
					.application(MimetypeMap.MIMETYPE_OUTLOOK_MSG))));
	/** The ref cache. */
	private Map<String, String> referencesCache = new HashMap<String, String>();
	/** The base dir. */
	private File workingDirectory;

	/** The base file. */
	private final Map<String, Pair<File, String>> parsedContent = new HashMap<String, Pair<File, String>>();
	/** The message. */
	private MAPIMessage message;

	/** The encoding. */
	private String encoding;

	/**
	 * default construct with base file to store temp files.
	 *
	 * @param baseDir
	 *            is where to store temp files.
	 */
	public MSGParser(File baseDir) {
		this.workingDirectory = baseDir;
	}

	/**
	 * Extracts properties and text from an Msg Document input stream.
	 *
	 * @param stream
	 *            the stream
	 * @param handler
	 *            the handler
	 * @param metadata
	 *            the metadata
	 * @param context
	 *            the context
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the sAX exception
	 * @throws TikaException
	 *             the tika exception
	 */
	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata,
			ParseContext context) throws IOException, SAXException, TikaException {

		XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
		xhtml.startDocument();

		try {
			this.message = new MAPIMessage(new NPOIFSFileSystem(stream));
			message.setReturnNullOnMissingChunk(true);
			// // If the message contains strings that aren't stored
			// // as Unicode, try to sort out an encoding for them
			if (message.has7BitEncodingStrings()) {
				if (message.getHeaders() != null) {
					// There's normally something in the headers
					message.guess7BitEncoding();
					encoding = "utf-7";
				} else {
					// Nothing in the header, try encoding detection
					// on the message body
					StringChunk text = message.getMainChunks().textBodyChunk;
					if (text != null) {
						CharsetDetector detector = new CharsetDetector();
						detector.setText(text.getRawValue());
						CharsetMatch match = detector.detect();
						if (match.getConfidence() > 35) {
							message.set7BitEncoding(match.getName());
							encoding = match.getName();
						}
					}
				}
			} else {
				encoding = UTF_8;
			}

			processHeader(message, metadata, xhtml);

			// real work.
			adaptedExtractMultipart(xhtml, message, context);

			xhtml.endDocument();

		} catch (Exception e) {
			throw new TikaException("Error while processing message", e);
		}
	}

	/**
	 * Parses the data.
	 *
	 * @param stream
	 *            the stream
	 * @param handler
	 *            the handler
	 * @param metadata
	 *            the metadata
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the sAX exception
	 * @throws TikaException
	 *             the tika exception
	 * @deprecated This method will be removed in Apache Tika 1.0.
	 */
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata)
			throws IOException, SAXException, TikaException {
		parse(stream, handler, metadata, new ParseContext());
	}

	/**
	 * Creates header part.
	 *
	 * @param xhtml
	 *            the xhtml
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @throws SAXException
	 *             the sAX exception
	 */
	private void header(XHTMLContentHandler xhtml, String key, String value) throws SAXException {
		if (value.length() > 0) {
			xhtml.element("dt", key);
			xhtml.element("dd", value);
		}
	}

	/**
	 * Process header.
	 *
	 * @param msg
	 *            the msg
	 * @param metadata
	 *            the metadata
	 * @param xhtml
	 *            the xhtml
	 * @throws Exception
	 *             the exception
	 */
	private void processHeader(MAPIMessage msg, Metadata metadata, XHTMLContentHandler xhtml)
			throws Exception {
		StringChunk subjectChunk = msg.getMainChunks().subjectChunk;
		if (msg.has7BitEncodingStrings()) {
			CharsetDetector detector = new CharsetDetector();
			detector.setText(subjectChunk.getRawValue());
			CharsetMatch detect = detector.detect();
			if (detect.getConfidence() >= 20) {
				subjectChunk.set7BitEncoding(detect.getName());
			}
		}
		String subject = subjectChunk.getValue();
		String from = msg.getDisplayFrom();

		metadata.set(DublinCore.CREATOR, from);
		metadata.set(Metadata.MESSAGE_FROM, from);
		metadata.set(Metadata.MESSAGE_TO, msg.getDisplayTo());
		metadata.set(Metadata.MESSAGE_CC, msg.getDisplayCC());
		metadata.set(Metadata.MESSAGE_BCC, msg.getDisplayBCC());

		metadata.set(DublinCore.TITLE, subject);
		metadata.set(DublinCore.SUBJECT, msg.getConversationTopic());

		try {
			for (String recipientAddress : msg.getRecipientEmailAddressList()) {
				if (recipientAddress != null)
					metadata.add(Metadata.MESSAGE_RECIPIENT_ADDRESS, recipientAddress);
			}
		} catch (ChunkNotFoundException he) {
		} // Will be fixed in POI 3.7 Final

		// Date - try two ways to find it
		// First try via the proper chunk
		if (msg.getMessageDate() != null) {
			metadata.set(DublinCore.DATE, msg.getMessageDate().getTime());
			metadata.set(Office.CREATION_DATE, msg.getMessageDate().getTime());
			metadata.set(Office.SAVE_DATE, msg.getMessageDate().getTime());
		} else {
			try {
				// Failing that try via the raw headers
				String[] headers = msg.getHeaders();
				if (headers != null && headers.length > 0) {
					for (String header : headers) {
						if (header.toLowerCase().startsWith("date:")) {
							String date = header.substring(header.indexOf(':') + 1).trim();

							// See if we can parse it as a normal mail date
							try {
								Date d = MboxParser.parseDate(date);
								metadata.set(DublinCore.DATE, d);
								metadata.set(Office.CREATION_DATE, d);
								metadata.set(Office.SAVE_DATE, d);
							} catch (ParseException e) {
								// Store it as-is, and hope for the best...
								metadata.set(DublinCore.DATE, date);
								metadata.set(Office.CREATION_DATE, date);
								metadata.set(Office.SAVE_DATE, date);
							}
							break;
						}
					}
				}
			} catch (ChunkNotFoundException he) {
				// We can't find the date, sorry...
			}
		}

		xhtml.element("h1", subject);

		// Output the from and to details in text, as you
		// often want them in text form for searching
		xhtml.startElement("dl");
		if (from != null) {
			header(xhtml, "From", from);
		}
		header(xhtml, "To", msg.getDisplayTo());
		header(xhtml, "Cc", msg.getDisplayCC());
		header(xhtml, "Bcc", msg.getDisplayBCC());
		try {
			header(xhtml, "Recipients", msg.getRecipientEmailAddress());
		} catch (ChunkNotFoundException e) {
		}
		List<String> attachmentList = new ArrayList<String>();
		// // prepare attachments
		prepareExtractMultipart(xhtml, message, attachmentList);
		if (attachmentList.size() > 0) {
			header(xhtml, "Attachments", attachmentList.toString());
		}
		xhtml.endElement("dl");

	}

	// Convert list of addresses into String

	/**
	 * Adapted extract multipart is parser that extracts the html body if exists, rtf body if exists
	 * or at least plain text. The html or rtf file could be obtained as alternative.
	 *
	 * @param xhtml
	 *            the xhtml
	 * @param msg
	 *            the message part
	 * @param context
	 *            the context
	 * @throws MessagingException
	 *             the messaging exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the sAX exception
	 * @throws TikaException
	 *             the tika exception
	 */
	public void adaptedExtractMultipart(XHTMLContentHandler xhtml, MAPIMessage msg,
			ParseContext context) throws MessagingException, IOException, SAXException,
			TikaException {
		// Get the message body. Preference order is: html, rtf, text
		Chunk htmlChunk = null;
		Chunk rtfChunk = null;
		Chunk textChunk = null;
		for (Chunk chunk : msg.getMainChunks().getAll()) {
			if (chunk.getChunkId() == MAPIProperty.BODY_HTML.id) {
				htmlChunk = chunk;
			}
			if (chunk.getChunkId() == MAPIProperty.RTF_COMPRESSED.id) {
				rtfChunk = chunk;
			}
			if (chunk.getChunkId() == MAPIProperty.BODY.id) {
				textChunk = chunk;
			}
		}

		boolean doneBody = false;
		if (htmlChunk != null) {
			byte[] data = null;
			if (htmlChunk instanceof ByteChunk) {
				data = ((ByteChunk) htmlChunk).getValue();
			} else if (htmlChunk instanceof StringChunk) {
				data = ((StringChunk) htmlChunk).getRawValue();
			}
			File tempHtmlFile = new File(workingDirectory, System.currentTimeMillis() + ".html");
			BufferedOutputStream rtfOutStream = new BufferedOutputStream(new FileOutputStream(
					tempHtmlFile));
			byte[] preparedStringData = referencesCache.size() > 0 ? prepareHTMLString(
					new String(data)).getBytes() : data;
			IOUtils.copy(new ByteArrayInputStream(preparedStringData), rtfOutStream);
			IOUtils.closeQuietly(rtfOutStream);
			parsedContent.put(MimetypeMap.MIMETYPE_HTML, new Pair<File, String>(tempHtmlFile,
					encoding));
			doneBody = true;

		}
		if (rtfChunk != null && !doneBody) {
			ByteChunk chunk = (ByteChunk) rtfChunk;

			MAPIProperty property = MAPIProperty.RTF_COMPRESSED;
			int type = Types.BINARY.getId();
			byte[] data = chunk.getValue();
			MAPIRtfAttribute rtf = new MAPIRtfAttribute(property, type, data);

			File tempRtfFile = new File(workingDirectory, System.currentTimeMillis() + ".rtf");
			BufferedOutputStream rtfOutStream = new BufferedOutputStream(new FileOutputStream(
					tempRtfFile));

			byte[] preparedStringData = referencesCache.size() > 0 ? prepareRTFString(
					new String(rtf.getData())).getBytes() : rtf.getData();
			IOUtils.copy(new ByteArrayInputStream(preparedStringData), rtfOutStream);
			IOUtils.closeQuietly(rtfOutStream);

			parsedContent.put(MIMETYPE_RTF, new Pair<File, String>(tempRtfFile, encoding));
			doneBody = true;
		}
		if (textChunk != null && !doneBody) {
			xhtml.element("p", ((StringChunk) textChunk).getValue());
		}

	}

	/**
	 * Prepare extract multipart by filling attachment list.
	 *
	 * @param xhtml
	 *            the xhtml
	 * @param msg
	 *            the message
	 * @param attachmentList
	 *            is list with attachments to fill
	 * @throws MessagingException
	 *             the messaging exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the sAX exception
	 * @throws TikaException
	 *             the tika exception
	 */
	private void prepareExtractMultipart(XHTMLContentHandler xhtml, MAPIMessage msg,
			List<String> attachmentList) throws MessagingException, IOException, SAXException,
			TikaException {

		// Process the attachments
		for (AttachmentChunks attachment : msg.getAttachmentFiles()) {
			String filename = null;
			if (attachment.attachLongFileName != null) {
				filename = attachment.attachLongFileName.getValue();
			} else if (attachment.attachFileName != null) {
				filename = attachment.attachFileName.getValue();
			}

			if (filename != null && filename.length() > 0) {
				Chunk[] chunks = attachment.getChunks();
				String id = null;
				byte[] data = null;
				// String mimetype = null;
				for (Chunk chunk : chunks) {
					if (MAPIProperty.ATTACH_CONTENT_ID.id == chunk.getChunkId()) {
						id = chunk.toString();
						// } else if (MAPIProperty.ATTACH_MIME_TAG.id == chunk
						// .getChunkId()) {
						// mimetype = chunk.toString();
					} else if (MAPIProperty.ATTACH_DATA.id == chunk.getChunkId()
							&& (chunk instanceof ByteChunk)) {
						ByteChunk chunkByte = (ByteChunk) chunk;
						data = chunkByte.getValue();

					}

				}
				if (id != null && data != null) {
					File file = new File(workingDirectory, System.currentTimeMillis() + "");
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					IOUtils.copy(new ByteArrayInputStream(data), fileOutputStream);
					IOUtils.closeQuietly(fileOutputStream);
					String src = file.getName();
					String replace = id.replace("<", "").replace(">", "");
					// String encodedData = new
					// String(Base64.encodeBase64(data));
					// String src = "data:" + mimetype + ";base64," +
					// encodedData;

					referencesCache.put(replace, src);
				} else {
					attachmentList.add(filename);
				}
			}

		}
	}

	/**
	 * Prepare string for rtf html data.
	 *
	 * @param htmlFileData
	 *            the html file data
	 * @return the string prepared
	 */
	private String prepareRTFString(String htmlFileData) {
		String tempData = htmlFileData;
		Iterator<String> iterator = referencesCache.keySet().iterator();
		while (iterator.hasNext()) {
			String cid = iterator.next();
			String regex = "\\{\\\\\\*\\\\[\\w\\s]+<img.+?src=\"cid:"
					+ cid.replaceAll("\\.", "\\\\.") + "\".+?}";
			String replacement = "{\\\\field{\\\\*\\\\fldinst{INCLUDEPICTURE \""
					+ referencesCache.get(cid)
					+ "\" MERGEFORMAT \\\\\\\\d \\\\\\\\pm1 \\\\\\\\px0 \\\\\\\\py0 \\\\\\\\pw0}}}";
			tempData = tempData.replaceAll(regex, replacement);
		}
		return tempData;
	}

	/**
	 * Prepare string for html files.
	 *
	 * @param htmlFileData
	 *            the html file data
	 * @return the string
	 */
	private String prepareHTMLString(String htmlFileData) {
		String tempData = htmlFileData;
		Iterator<String> iterator = referencesCache.keySet().iterator();
		while (iterator.hasNext()) {
			String cid = (String) iterator.next();
			tempData = tempData.replace("cid:" + cid, referencesCache.get(cid));
		}
		return tempData;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.tika.parser.Parser#getSupportedTypes(org.apache.tika.parser .ParseContext)
	 */
	@Override
	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return SUPPORTED_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.content.transform.AlternativeContentParser#getAlternatives()
	 */
	@Override
	public Map<String, Pair<File, String>> getAlternatives() {
		return parsedContent;
	}

	/**
	 * Clear.
	 */
	public void clear() {
	}

}
