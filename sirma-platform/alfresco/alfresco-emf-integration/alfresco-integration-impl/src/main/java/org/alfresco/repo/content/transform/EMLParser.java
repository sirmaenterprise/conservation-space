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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.util.Pair;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Defines a EML document content extractor.
 */
public class EMLParser implements AlternativeContentParser {

	/** The Constant MULTIPART_ALTERNATIVE. */
	private static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8969240499669909899L;

	/** The Constant SUPPORTED_TYPES. */
	private static final Set<MediaType> SUPPORTED_TYPES = Collections
			.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(MediaType
					.application(MimetypeMap.MIMETYPE_RFC822))));
	/** The ref cache. */
	private final Map<String, String> referencesCache = new HashMap<String, String>();

	/** The base dir. */
	private File workingDirectory;

	/** The base file. */
	private final Map<String, Pair<File, String>> parsedContent = new HashMap<String, Pair<File, String>>();
	private Logger logger = Logger.getLogger(getClass());

	/**
	 * default construct with base file to store temp files.
	 *
	 * @param baseDir
	 *            is where to store temp files.
	 */
	public EMLParser(File baseDir) {
		this.workingDirectory = baseDir;
	}

	/**
	 * Extracts properties and text from an EML Document input stream.
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

		Properties props = System.getProperties();
		Session mailSession = Session.getDefaultInstance(props, null);

		try {
			MimeMessage message = new MimeMessage(mailSession, stream);

			String subject = message.getSubject();
			String from = this.convertAddressesToString(message.getFrom());
			// Recipients :
			String messageException = "";
			String to = "";
			String cc = "";
			String bcc = "";
			try {
				// QVIDMS-2004 Added because of bug in Mail Api
				to = this.convertAddressesToString(message.getRecipients(Message.RecipientType.TO));
				cc = this.convertAddressesToString(message.getRecipients(Message.RecipientType.CC));
				bcc = this.convertAddressesToString(message
						.getRecipients(Message.RecipientType.BCC));
			} catch (AddressException e) {
				e.printStackTrace();
				messageException = e.getRef();
				if (messageException.indexOf("recipients:") != -1) {
					to = messageException.substring(0, messageException.indexOf(":"));
				}
			}
			metadata.set(Office.AUTHOR, from);
			metadata.set(DublinCore.TITLE, subject);
			metadata.set(DublinCore.SUBJECT, subject);

			xhtml.element("h1", subject);

			xhtml.startElement("dl");
			header(xhtml, "From", MimeUtility.decodeText(from));
			header(xhtml, "To", MimeUtility.decodeText(to.toString()));
			header(xhtml, "Cc", MimeUtility.decodeText(cc.toString()));
			header(xhtml, "Bcc", MimeUtility.decodeText(bcc.toString()));

			// // Parse message
			// if (message.getContent() instanceof MimeMultipart) {
			// // Multipart message, call matching method
			// MimeMultipart multipart = (MimeMultipart) message.getContent();
			// this.extractMultipart(xhtml, multipart, context);

			List<String> attachmentList = new ArrayList<String>();
			// prepare attachments
			prepareExtractMultipart(xhtml, message, null, context, attachmentList);
			if (attachmentList.size() > 0) {
				// TODO internationalization
				header(xhtml, "Attachments", attachmentList.toString());
			}
			xhtml.endElement("dl");

			// a supprimer si pb et a remplacer par ce qui est commenT
			adaptedExtractMultipart(xhtml, message, null, context);

			xhtml.endDocument();
		} catch (Exception e) {
			throw new TikaException("Error while processing message", e);
		}
	}

	/**
	 * Gets the content handler.
	 *
	 * @param targetMimeType
	 *            the target mime type
	 * @param output
	 *            the output
	 * @return the content handler
	 * @throws TransformerConfigurationException
	 *             the transformer configuration exception
	 */
	protected static ContentHandler getContentHandler(String targetMimeType, Writer output)
			throws TransformerConfigurationException {
		if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimeType)) {
			return new BodyContentHandler(output);
		}

		SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler handler = factory.newTransformerHandler();
		handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		handler.setResult(new StreamResult(output));

		if (MimetypeMap.MIMETYPE_HTML.equals(targetMimeType)) {
			handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
		} else if (MimetypeMap.MIMETYPE_XHTML.equals(targetMimeType)
				|| MimetypeMap.MIMETYPE_XML.equals(targetMimeType)) {
			handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
		} else {

		}
		return handler;
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
	@Deprecated
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

	// Convert list of addresses into String
	/**
	 * Convert addresses to string.
	 *
	 * @param addresses
	 *            the addresses
	 * @return the string
	 */
	private String convertAddressesToString(Address[] addresses) {
		StringBuilder result = new StringBuilder();
		if (addresses != null) {
			String addressToAdd;
			for (int i = 0; i < addresses.length; i++) {
				addressToAdd = addresses[i].toString().replaceAll("<", "").replaceAll(">", "");
				result.append(addressToAdd).append("; ");
			}
			int resultLength = result.length();
			if (resultLength > 1)
				result.delete(resultLength - 2, resultLength);
		}
		return result.toString();
	}

	/**
	 * Adapted extract multipart is the recusrsive parser that splits the data and apend it to the
	 * final xhtml file.
	 *
	 * @param xhtml
	 *            the xhtml
	 * @param part
	 *            the part
	 * @param parentPart
	 *            the parent part
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
	public void adaptedExtractMultipart(XHTMLContentHandler xhtml, Part part, Part parentPart,
			ParseContext context) throws MessagingException, IOException, SAXException,
			TikaException {

		String disposition = part.getDisposition();
		if ((disposition != null && disposition.contains(Part.ATTACHMENT))) {
			return;
		}

		if (part.isMimeType("text/plain")) {
			if (parentPart != null && parentPart.isMimeType(MULTIPART_ALTERNATIVE)) {
				return;
			} else {
				// add file
				String data = part.getContent().toString();
				writeContent(part, data, MimetypeMap.MIMETYPE_TEXT_PLAIN, "txt");
			}
		} else if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			Part parentPartLocal = part;
			if (parentPart != null && parentPart.isMimeType(MULTIPART_ALTERNATIVE)) {
				parentPartLocal = parentPart;
			}
			int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				adaptedExtractMultipart(xhtml, mp.getBodyPart(i), parentPartLocal, context);
			}
		} else if (part.isMimeType("message/rfc822")) {
			adaptedExtractMultipart(xhtml, (Part) part.getContent(), part, context);
		} else if (part.isMimeType("text/html")) {

			if ((parentPart != null && parentPart.isMimeType(MULTIPART_ALTERNATIVE))
					|| (part.getDisposition() == null || !part.getDisposition().contains(
							Part.ATTACHMENT))) {
				Object data = part.getContent();
				String htmlFileData = prepareString(new String(data.toString()));
				writeContent(part, htmlFileData, MimetypeMap.MIMETYPE_HTML, "html");
			}

		} else if (part.isMimeType("image/*")) {

			String[] encoded = part.getHeader("Content-Transfer-Encoding");
			if (isContained(encoded, "base64")) {
				if (part.getDisposition() != null
						&& part.getDisposition().contains(Part.ATTACHMENT)) {
					InputStream stream = part.getInputStream();
					byte[] binaryData = new byte[part.getSize()];
					stream.read(binaryData, 0, part.getSize());
					String encodedData = new String(Base64.encodeBase64(binaryData));
					String[] split = part.getContentType().split(";");
					String src = "data:" + split[0].trim() + ";base64," + encodedData;
					AttributesImpl attributes = new AttributesImpl();
					attributes.addAttribute(null, "src", "src", "String", src);
					xhtml.startElement("img", attributes);
					xhtml.endElement("img");
				}
			}

		} else {
			Object content = part.getContent();
			if (content instanceof String) {
				xhtml.element("div", prepareString(part.getContent().toString()));
			} else if (content instanceof InputStream) {
				InputStream fileContent = part.getInputStream();

				Parser parser = new AutoDetectParser();
				Metadata attachmentMetadata = new Metadata();

				BodyContentHandler handlerAttachments = new BodyContentHandler();
				parser.parse(fileContent, handlerAttachments, attachmentMetadata, context);

				xhtml.element("div", handlerAttachments.toString());

			}
		}
	}

	/**
	 * Write content of part to file.
	 *
	 * @param part
	 *            the part to process
	 * @param data
	 *            the data to write as string
	 * @param type
	 *            the mimetype
	 * @param extension
	 *            the extension to create file with
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeContent(Part part, String data, String type, String extension)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		final File createdFile = new File(workingDirectory, UUID.randomUUID().toString() + "."
				+ extension);
		final String charset = getCharset(part);
		final BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(createdFile), charset));
		final char[] charArray = data.toCharArray();
		outStream.write(charArray, 0, charArray.length);
		IOUtils.closeQuietly(outStream);
		parsedContent.put(type, new Pair<File, String>(createdFile, charset));
	}

	/**
	 * Gets the charset from content type.
	 *
	 * @param part
	 *            the part to process
	 * @return the charset if found or UTF-8 as default
	 */
	private String getCharset(Part part) {
		String charset = UTF_8;
		String contentType = null;
		try {
			contentType = part.getContentType();
			int indexOf = contentType.indexOf("charset=");
			if (indexOf != -1) {
				charset = (contentType.substring(indexOf) + ";").replaceAll("charset=\"?", "")
						.replaceAll("\"?;(\\s*.*)", "");
			}
			Charset.forName(charset);
		} catch (Exception e) {
			logger.error("Error processing content type: " + contentType, e);
			charset = UTF_8;
		}
		return charset;
	}

	/**
	 * Prepare string.
	 *
	 * @param htmlFileData
	 *            the html file data
	 * @return the string
	 */
	private String prepareString(String htmlFileData) {
		String tempData = htmlFileData;
		Iterator<String> iterator = referencesCache.keySet().iterator();
		while (iterator.hasNext()) {
			String cid = iterator.next();
			tempData = tempData.replace("cid:" + cid, referencesCache.get(cid));
		}
		return tempData;
	}

	/**
	 * Prepare extract multipart.
	 *
	 * @param xhtml
	 *            the xhtml
	 * @param part
	 *            the part
	 * @param parentPart
	 *            the parent part
	 * @param context
	 *            the context
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
	private void prepareExtractMultipart(XHTMLContentHandler xhtml, Part part, Part parentPart,
			ParseContext context, List<String> attachmentList) throws MessagingException,
			IOException, SAXException, TikaException {

		String disposition = part.getDisposition();
		if ((disposition != null && disposition.contains(Part.ATTACHMENT))) {
			String fileName = part.getFileName();
			if (fileName != null && fileName.startsWith("=?")) {
				fileName = MimeUtility.decodeText(fileName);
			}
			attachmentList.add(fileName);
		}

		String[] header = part.getHeader("Content-ID");
		String key = null;
		if (header != null) {
			for (String string : header) {
				key = string;
			}
		}

		if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				prepareExtractMultipart(xhtml, mp.getBodyPart(i), part, context, attachmentList);
			}
		} else if (part.isMimeType(MimetypeMap.MIMETYPE_RFC822)) {
			prepareExtractMultipart(xhtml, (Part) part.getContent(), part, context, attachmentList);
		} else {

			if (key == null) {
				return;
			}
			// if ((disposition != null && disposition.contains(Part.INLINE))) {
			InputStream stream = part.getInputStream();

			File file = new File(workingDirectory, System.currentTimeMillis() + "");
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			IOUtils.copy(stream, fileOutputStream);
			IOUtils.closeQuietly(fileOutputStream);
			String src = file.getName();
			String replace = key.replace("<", "").replace(">", "");
			referencesCache.put(replace, src);
			// }

		}
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

	/*
	 * (non-Javadoc)
	 * @see org.apache.tika.parser.Parser#getSupportedTypes(org.apache.tika.parser .ParseContext)
	 */
	@Override
	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return SUPPORTED_TYPES;
	}

	/**
	 * Checks if value is contained in array of values.
	 *
	 * @param arr
	 *            the arr to check
	 * @param value
	 *            the value
	 * @return true, if is contained
	 */
	private boolean isContained(String[] arr, String value) {
		if (arr != null && arr.length > 0 && arr[0].contains(value)) {
			return true;
		}
		return false;
	}
}
