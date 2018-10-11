/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.AlternativeContentParser.UTF_8;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

/**
 * Class that converts eml/msg to pdf. The intermediate transform can be to html or to text. The
 * control of decision is the property 'htmlMode'.
 *
 * @author hackyou
 */
public class EmailToPDFContentTransformer extends AbstractContentTransformer2 {

	/** The Constant WRONG_FORMAT_MESSAGE_ID. */
	private static final String WRONG_FORMAT_MESSAGE_ID = "transform.err.format_or_password";
	/** The transformer. */
	private ITextPDFWorker transformer;
	/** The worker. */
	private ContentTransformerWorker worker;
	/** The html mode. */
	private boolean htmlMode = true;
	/** path to 'wkhtmltopdf' exe/bin. */
	private String htmlToPdfConvertorLocation;
	/** The source mime types. */
	protected List<String> sourceMimeTypes;
	/** The working dir. */
	private File workingDirectory;

	/**
	 * creates new delegate transformer.
	 */
	public EmailToPDFContentTransformer() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable
	 * (java.lang.String, java.lang.String,
	 * org.alfresco.service.cmr.repository.TransformationOptions)
	 */
	@Override
	public boolean isTransformable(String sourceMimetype, String targetMimetype,
			TransformationOptions options) {
		if (MimetypeMap.MIMETYPE_RFC822.equals(sourceMimetype)
				&& MimetypeMap.MIMETYPE_PDF.equals(targetMimetype)) {
			return true;
		} else if (MimetypeMap.MIMETYPE_OUTLOOK_MSG.equals(sourceMimetype)
				&& MimetypeMap.MIMETYPE_PDF.equals(targetMimetype)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#
	 * transformInternal(org.alfresco.service.cmr.repository.ContentReader,
	 * org.alfresco.service.cmr.repository.ContentWriter,
	 * org.alfresco.service.cmr.repository.TransformationOptions)
	 */
	@Override
	protected void transformInternal(ContentReader reader, ContentWriter writer,
			TransformationOptions options) throws Exception {

		if (!htmlMode) {
			// do plain text transform
			doTxtTransform(reader.getContentInputStream(), writer.getContentOutputStream(),
					reader.getMimetype(), writer.getMimetype(), reader.getEncoding(),
					writer.getEncoding());
		} else {// do html transform with tika

			doHtmlTransform(reader.getContentInputStream(), writer.getContentOutputStream(),
					reader.getMimetype(), writer.getMimetype(), writer.getEncoding());
		}
	}

	/**
	 * Do txt transform of eml file.
	 *
	 * @param is
	 *            the input stream
	 * @param os
	 *            the final output stream
	 * @param inputMime
	 *            the input mime type
	 * @param targetMimeType
	 *            the target mime type
	 * @param encoding
	 *            the encoding of reader
	 * @param writerEncoding
	 *            the writer encoding
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TransformerConfigurationException
	 *             the transformer configuration exception
	 * @throws SAXException
	 *             the sAX exception
	 * @throws TikaException
	 *             the tika exception
	 * @throws MessagingException
	 *             the messaging exception
	 */
	protected void doTxtTransform(InputStream is, OutputStream os, String inputMime,
			String targetMimeType, String encoding, String writerEncoding) throws IOException,
			TransformerConfigurationException, SAXException, TikaException, MessagingException {
		MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()), is);
		final StringBuilder sb = new StringBuilder();
		Object content = mimeMessage.getContent();
		if (content instanceof Multipart) {
			Multipart multipart = (Multipart) content;
			Part part = multipart.getBodyPart(0);

			if (part.getContent() instanceof Multipart) {
				multipart = (Multipart) part.getContent();
				for (int i = 0, n = multipart.getCount(); i < n; i++) {
					part = multipart.getBodyPart(i);
					if (part.isMimeType("text/*")) {
						sb.append(part.getContent().toString()).append("\n");

					}

				}

			} else if (part.isMimeType("text/*")) {
				sb.append(part.getContent().toString());
			}

		} else {
			sb.append(content.toString());
		}

		textToPDF(new ByteArrayInputStream(sb.toString().getBytes()), UTF_8, os);
	}

	/**
	 * Do html transform from eml or msg.
	 *
	 * @param is
	 *            the input stream data
	 * @param osFinal
	 *            the resulted pdf stream
	 * @param inputMime
	 *            the input mime
	 * @param targetMimeType
	 *            the target mime type
	 * @param encoding
	 *            the encoding
	 * @throws Exception
	 *             the exception
	 */
	protected void doHtmlTransform(InputStream is, OutputStream osFinal, String inputMime,
			String targetMimeType, String encoding) throws Exception {
		workingDirectory = null;
		AlternativeContentParser parser = null;
		OutputStream os = null;
		BufferedWriter ow = null;
		try {
			// store at single location
			workingDirectory = generateWorkingDir();
			// prepare parsing
			File headerFile = new File(workingDirectory, "MailHeader.xhtml");
			os = new FileOutputStream(headerFile);
			ow = new BufferedWriter(new OutputStreamWriter(os, encoding));
			Properties localProps = new Properties();
			localProps.put("char-encoding", UTF_8);
			parser = getParser(inputMime);
			Metadata metadata = new Metadata();
			ParseContext context = buildParseContext(metadata, targetMimeType);
			ContentHandler handler = getContentHandler(MimetypeMap.MIMETYPE_XHTML, ow);
			// do parse the mail
			parser.parse(is, handler, metadata, context);
			IOUtils.closeQuietly(ow);
			// convert the header
			File headerPdfFile = new File(workingDirectory, "MailHeader.pdf");
			messageToPDF(headerFile, headerPdfFile, localProps);
			// the actual content
			File contentFile = null;
			if (!parser.getAlternatives().isEmpty()) {
				contentFile = new File(workingDirectory, "MailContent.pdf");
				Pair<File, String> fileWithEncodingPair = null;
				if (parser.getAlternatives().get(MimetypeMap.MIMETYPE_HTML) != null) {
					fileWithEncodingPair = parser.getAlternatives().get(MimetypeMap.MIMETYPE_HTML);
				} else if (parser.getAlternatives().get(MimetypeMap.MIMETYPE_TEXT_PLAIN) != null) {
					fileWithEncodingPair = parser.getAlternatives().get(
							MimetypeMap.MIMETYPE_TEXT_PLAIN);
				} else if (parser.getAlternatives().get(AlternativeContentParser.MIMETYPE_RTF) != null) {
					fileWithEncodingPair = parser.getAlternatives().get(
							AlternativeContentParser.MIMETYPE_RTF);
				} else {
					throw new RuntimeException("Unrecognized type! " + parser.getAlternatives());
				}
				encoding = fileWithEncodingPair.getSecond();
				localProps.put("char-encoding", encoding);
				messageToPDF(fileWithEncodingPair.getFirst(), contentFile, localProps);
			}
			MergePDF.concatPDFs(osFinal, contentFile, headerPdfFile);

		} finally {
			IOUtils.closeQuietly(ow);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(osFinal);
			// sanity
			deleteFile(workingDirectory);
		}
	}

	/**
	 * Convert alternative message (html/rtf) to pdf.
	 *
	 * @param input
	 *            the temp file
	 * @param pdfFile
	 *            the os final
	 * @param properties
	 *            are the tidy properties to use
	 * @throws Exception
	 *             the exception
	 */
	private void messageToPDF(File input, File pdfFile, Properties properties) throws Exception {
		OutputStream fileOutputStream = null;
		InputStream fileInputStream = null;
		try {
			if (input.getName().endsWith("html")) {
				ProcessBuilder processBuilder = new ProcessBuilder(getHtmlToPdfConvertorLocation(),
						"--encoding", properties.get("char-encoding").toString(), input.getAbsolutePath(), pdfFile.getAbsolutePath());
				Process start = processBuilder.start();
				start.waitFor();
			} else if (input.getName().endsWith("rtf") && worker != null) {
				FileContentReader reader = new FileContentReader(input);
				reader.setMimetype(AlternativeContentParser.MIMETYPE_RTF);
				FileContentWriter writer = new FileContentWriter(pdfFile);
				writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
				TransformationOptions options = new TransformationOptions();
				worker.transform(reader, writer, options);
			} else if (input.getName().endsWith("txt")) {
				fileInputStream = new FileInputStream(input);
				fileOutputStream = new FileOutputStream(pdfFile);
				textToPDF(fileInputStream, properties.getProperty("char-encoding"),
						fileOutputStream);
			} else {
				fileOutputStream = new FileOutputStream(pdfFile);
				textToPDF(new ByteArrayInputStream("Internal Error!".getBytes()),
						properties.getProperty("char-encoding"), fileOutputStream);
			}
		} finally {
			IOUtils.closeQuietly(fileInputStream);
			IOUtils.closeQuietly(fileOutputStream);
		}
	}

	/**
	 * Text to pdf.
	 *
	 * @param is
	 *            the is
	 * @param encoding
	 *            the encoding
	 * @param os
	 *            the os
	 */
	private void textToPDF(InputStream is, String encoding, OutputStream os) {
		Document pdf = null;
		transformer = new ITextPDFWorker();
		try {
			pdf = transformer.createPDFFromText(is, encoding, os);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				pdf = transformer.createEmptyPDF(os);
			} catch (Exception e2) {
				// skip
			}
		} finally {
			if (pdf != null) {
				try {
					pdf.close();
				} catch (Throwable e) {// skip
				}
			}
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);

		}
	}

	/**
	 * Gets the html to pdf convertor location.
	 *
	 * @return the html to pdf convertor location
	 */
	private String getHtmlToPdfConvertorLocation() {
		return htmlToPdfConvertorLocation;
	}

	/**
	 * Delete file or schedule the deletion on fail.
	 *
	 * @param tempFile
	 *            the temp file
	 */
	private void deleteFile(File tempFile) {
		if (tempFile != null) {
			if (!tempFile.delete()) {
				if (tempFile.isDirectory()) {
					File[] listFiles = tempFile.listFiles();
					for (File child : listFiles) {
						deleteFile(child);
					}
				}
			}
			tempFile.delete();
			if (tempFile.canRead()) {
				tempFile.deleteOnExit();
			}

		}
	}

	/**
	 * Instantiates a new eM lto pdf content transformer.
	 *
	 * @param sourceMimeTypes
	 *            the source mime types
	 */
	protected EmailToPDFContentTransformer(List<String> sourceMimeTypes) {
		this.sourceMimeTypes = sourceMimeTypes;
	}

	/**
	 * Instantiates a new eM lto pdf content transformer.
	 *
	 * @param sourceMimeTypes
	 *            the source mime types
	 */
	protected EmailToPDFContentTransformer(String[] sourceMimeTypes) {
		this(Arrays.asList(sourceMimeTypes));
	}

	/**
	 * Returns the correct Tika Parser to process the document. If you don't know which you want,
	 * use {@link TikaAutoContentTransformer} which makes use of the Tika auto-detection.
	 *
	 * @param mimetype
	 *            the mimetype
	 * @return the parser
	 */
	protected AlternativeContentParser getParser(String mimetype) {
		if (MimetypeMap.MIMETYPE_OUTLOOK_MSG.equals(mimetype)) {
			return new MSGParser(workingDirectory);
		}
		return new EMLParser(workingDirectory);
	}

	/**
	 * generates a temporary dir using.
	 *
	 * @return the created directory or null if the dir could not be accessed for write
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 *             {@link File#createTempFile(String, String, File)} as child of alfresco temporary
	 *             dir. Directory is scheduled for deletion using {@link File#deleteOnExit()}
	 */
	private File generateWorkingDir() throws IOException {

		File rootDir = null;
		rootDir = File.createTempFile("EmailContentTransform-", "", TempFileProvider.getTempDir());
		if (rootDir.delete()) {
			rootDir.mkdirs();
			return rootDir.canWrite() ? rootDir : null;
		}
		return rootDir;
	}

	/**
	 * Returns an appropriate Tika ContentHandler for the requested content type. Normally you'll
	 * let this work as default, but if you need fine-grained control of how the Tika events become
	 * text then override and supply your own.
	 *
	 * @param targetMimeType
	 *            the target mime type
	 * @param output
	 *            the output
	 * @return the content handler
	 * @throws TransformerConfigurationException
	 *             the transformer configuration exception
	 */
	protected ContentHandler getContentHandler(String targetMimeType, Writer output)
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
			throw new TransformerInfoException(WRONG_FORMAT_MESSAGE_ID,
					new IllegalArgumentException("Requested target type " + targetMimeType
							+ " not supported"));
		}
		return handler;
	}

	/**
	 * By default returns a ParseContent that does not recurse.
	 *
	 * @param metadata
	 *            the metadata
	 * @param targetMimeType
	 *            the target mime type
	 * @return the parses the context
	 */
	protected ParseContext buildParseContext(Metadata metadata, String targetMimeType) {
		return new ParseContext();
	}

	/**
	 * Checks if is html mode.
	 *
	 * @return true, if is html mode
	 */
	public boolean isHtmlMode() {
		return htmlMode;
	}

	/**
	 * Sets the html mode.
	 *
	 * @param htmlMode
	 *            the new html mode
	 */
	public void setHtmlMode(boolean htmlMode) {
		this.htmlMode = htmlMode;
	}

	/**
	 * Sets the worker.
	 *
	 * @param worker
	 *            the worker to set
	 */
	public void setWorker(ContentTransformerWorker worker) {
		this.worker = worker;
	}

	/**
	 * Sets the html to pdf convertor location.
	 *
	 * @param htmlToPdfConvertorLocation
	 *            the htmlToPdfConvertorLocation to set
	 */
	public void setHtmlToPdfConvertorLocation(String htmlToPdfConvertorLocation) {
		this.htmlToPdfConvertorLocation = htmlToPdfConvertorLocation;
	}

	/**
	 * Merge pdfs by adding each one of the file to single one.
	 */
	static class MergePDF {

		/**
		 * Concat the pdf files.
		 *
		 * @param osFinal
		 *            the final stream to hold the document - it is closed
		 * @param files
		 *            the files is list of pdf files to concat
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws DocumentException
		 *             the document exception
		 */
		public static void concatPDFs(OutputStream osFinal, File... files) throws IOException,
				DocumentException {

			Document wholeDocument = null;
			try {
				wholeDocument = new Document();
				PdfCopy copy = new PdfCopy(wholeDocument, osFinal);
				wholeDocument.open();
				PdfReader pdfInput;
				int numbOfPages = 0;
				for (int i = 0; i < files.length; i++) {
					if (files[i] == null) {
						continue;
					}
					pdfInput = new PdfReader(files[i].getAbsolutePath());
					numbOfPages = pdfInput.getNumberOfPages();
					for (int page = 0; page < numbOfPages;) {
						copy.addPage(copy.getImportedPage(pdfInput, ++page));
					}
					pdfInput.close();
				}
				copy.close();
			} finally {
				if (wholeDocument != null) {
					wholeDocument.close();
				}
				IOUtils.closeQuietly(osFinal);
			}

		}
	}
}
