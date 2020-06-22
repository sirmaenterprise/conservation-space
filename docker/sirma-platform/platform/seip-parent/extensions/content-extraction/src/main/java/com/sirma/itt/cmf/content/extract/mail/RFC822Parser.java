package com.sirma.itt.cmf.content.extract.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.WritableFileDescriptor;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Mail extractor for rfc822 message format.
 *
 * @author BBanchev
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = MailExtractor.TARGET_NAME, order = 0)
public class RFC822Parser extends BaseMailExtractor<MimeMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RFC822Parser.class);

	@Override
	public Pair<MimeMessage, String> openMessage(File mail) throws Exception {
		try (FileInputStream inputStream = new FileInputStream(mail)) {
			return openMessage(inputStream);
		}
	}

	@Override
	public Pair<MimeMessage, String> openMessage(InputStream stream) throws Exception {
		Properties props = System.getProperties();
		Session mailSession = Session.getDefaultInstance(props, null);

		MimeMessage message = new MimeMessage(mailSession, stream);

		return new Pair<>(message, message.getEncoding());
	}

	@Override
	public MailExtractor<MimeMessage> isApplicable(File file) {
		return file.getName().endsWith(".eml") ? this : null;
	}

	/**
	 * Extract message and fills attachment list. Optionally could delete the attachments.
	 *
	 * @param currentPart
	 *            the part currently processed
	 * @param parentPart
	 *            the parent part of current
	 * @param prefix
	 *            the prefix for attachment file name
	 * @param tempStoreDir
	 *            the temp store dir to use to store attachments in
	 * @param deletePart
	 *            the delete part. whether to delete attachment after extract
	 * @param attachmentList
	 *            the attachment list. Filled during iterate
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws MessagingException
	 *             the messaging exception
	 */
	private void extractAttachments(Part currentPart, Part parentPart, String prefix, File tempStoreDir,
			boolean deletePart, List<File> attachmentList) throws IOException, MessagingException {

		if (currentPart.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) currentPart.getContent();
			int count = mp.getCount();
			List<BodyPart> parts = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mp.getBodyPart(i);
				extractAttachments(bodyPart, currentPart, prefix, tempStoreDir, deletePart, attachmentList);
				parts.add(bodyPart);
			}
			for (BodyPart bodyPart : parts) {
				if (bodyPart.getDisposition() != null && bodyPart.getDisposition().contains(Part.ATTACHMENT)) {
					if (deletePart) {
						mp.removeBodyPart(bodyPart);
					}
				}
			}

		} else if (currentPart.isMimeType("message/rfc822")) {
			extractAttachments((Part) currentPart.getContent(), currentPart, prefix, tempStoreDir, deletePart,
					attachmentList);
		} else {
			String disposition = currentPart.getDisposition();
			if (disposition != null && disposition.contains(Part.INLINE)) {
				LOGGER.error("INLINE ATTACHMENT CURRENTLY NOT SUPPORTED");
			}
			if (disposition != null && disposition.contains(Part.ATTACHMENT)) {
				InputStream stream = currentPart.getInputStream();
				String filename = currentPart.getFileName();
				if (filename.startsWith("=?")) {
					filename = MimeUtility.decodeText(filename);
				}
				File tempFile = new File(tempStoreDir, prefix + filename);
				OutputStream writer = new FileOutputStream(tempFile);
				IOUtils.copy(stream, writer);
				IOUtils.closeQuietly(writer);
				attachmentList.add(tempFile);

			}
		}
	}

	@Override
	protected void postExtract(FileDescriptor descriptor, Pair<MimeMessage, String> openedMessage, boolean deleteParts)
			throws Exception {
		if (!(descriptor instanceof WritableFileDescriptor)) {
			// the argument does not support modification we ignore the request
			return;
		}
		WritableFileDescriptor writableDescriptor = (WritableFileDescriptor) descriptor;
		MimeMessage mimeMessage = openedMessage.getFirst();

		mimeMessage.saveChanges();

		File tempFile = tempFileProvider.createTempFile("baseMailFile", null, getTempFolder(null));
		try (OutputStream os = new FileOutputStream(tempFile)) {
			// save the message to a temp file so we can set it to the descriptor again
			// the file will be deleted when the upload is finished or in 24h
			mimeMessage.writeTo(os);
		}

		try (InputStream fileInputStream = new FileInputStream(tempFile)) {
			writableDescriptor.write(fileInputStream);
		}
	}

	@Override
	protected void extractAttachments(Pair<MimeMessage, String> message, String prefix, File tempStoreDir,
			boolean deleteParts, List<File> attachmentList) throws Exception {
		MimeMessage currentPart = message.getFirst();
		if (currentPart.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) currentPart.getContent();
			int count = mp.getCount();
			List<BodyPart> parts = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mp.getBodyPart(i);
				extractAttachments(bodyPart, currentPart, prefix, tempStoreDir, deleteParts, attachmentList);
				parts.add(bodyPart);
			}
			for (BodyPart bodyPart : parts) {
				if (bodyPart.getDisposition() != null && bodyPart.getDisposition().contains(Part.ATTACHMENT)) {
					if (deleteParts) {
						mp.removeBodyPart(bodyPart);
					}
				}
			}

		} else if (currentPart.isMimeType("message/rfc822")) {
			extractAttachments((Part) currentPart.getContent(), currentPart, prefix, tempStoreDir, deleteParts,
					attachmentList);
		} else {
			String disposition = currentPart.getDisposition();
			if (disposition != null && disposition.contains(Part.INLINE)) {
				LOGGER.error("INLINE ATTACHMENT CURRENTLY NOT SUPPORTED");
			}
			if (disposition != null && disposition.contains(Part.ATTACHMENT)) {
				String filename = currentPart.getFileName();
				if (filename.startsWith("=?")) {
					filename = MimeUtility.decodeText(filename);
				}
				File tempFile = new File(tempStoreDir, prefix + filename);
				try (InputStream stream = currentPart.getInputStream();
						OutputStream writer = new FileOutputStream(tempFile)) {
					IOUtils.copy(stream, writer);
					attachmentList.add(tempFile);
				}
			}
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
