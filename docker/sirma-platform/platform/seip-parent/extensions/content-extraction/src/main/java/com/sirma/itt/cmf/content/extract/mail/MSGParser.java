package com.sirma.itt.cmf.content.extract.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Defines a Msg document content extractor. Mail is extracted based on the requested parts only.
 *
 * @author BBanchev
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = MailExtractor.TARGET_NAME, order = 1)
public class MSGParser extends BaseMailExtractor<MAPIMessage> {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MSGParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<MAPIMessage, String> openMessage(File file) throws Exception {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			return openMessage(inputStream);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<MAPIMessage, String> openMessage(InputStream stream) throws Exception {
		NPOIFSFileSystem npoifsFileSystem = new NPOIFSFileSystem(stream);
		MAPIMessage message = new MAPIMessage(npoifsFileSystem);
		String encoding = null;
		message.setReturnNullOnMissingChunk(true);
		// // If the message contains strings that aren't stored
		// // as Unicode, try to sort out an encoding for them
		if (message.has7BitEncodingStrings()) {
			if (message.getHeaders() != null) {
				// There's normally something in the headers
				message.guess7BitEncoding();
				encoding = "utf-7";
			} else {
				// // Nothing in the header, try encoding detection
				// // on the message body
				StringChunk text = message.getMainChunks().getTextBodyChunk();
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
			encoding = "utf-8";
		}
		npoifsFileSystem.close();
		return new Pair<>(message, encoding);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MailExtractor<MAPIMessage> isApplicable(File file) {
		return file.getName().endsWith(".msg") ? this : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postExtract(FileDescriptor descriptor, Pair<MAPIMessage, String> openedMessage, boolean deleteParts)
			throws IOException {
		if (deleteParts) {
			clean(descriptor.getInputStream());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void extractAttachments(Pair<MAPIMessage, String> message, String prefix, File tempStoreDir,
			boolean deleteParts, List<File> attachmentList) throws IOException {
		// just in case
		if (prefix == null) {
			prefix = "";
		}
		MAPIMessage mapiMessage = message.getFirst();
		for (AttachmentChunks attachment : mapiMessage.getAttachmentFiles()) {
			// attachment.getEmbeddedAttachmentObject()
			String filename = null;
			if (attachment.getAttachFileName() != null) {
				filename = attachment.getAttachFileName().getValue();
			} else if (attachment.getAttachLongFileName() != null) {
				filename = attachment.getAttachLongFileName().getValue();
			}

			if (filename != null && filename.length() > 0) {
				Chunk[] chunks = attachment.getChunks();
				byte[] data = null;
				for (Chunk chunk : chunks) {
					if (MAPIProperty.ATTACH_DATA.id == chunk.getChunkId() && chunk instanceof ByteChunk) {
						ByteChunk chunkByte = (ByteChunk) chunk;
						data = chunkByte.getValue();
						break;
					}

				}
				if (data != null) {
					File tempFile = new File(tempStoreDir, prefix + filename);
					try (OutputStream writer = new FileOutputStream(tempFile)) {
						IOUtils.copy(new ByteArrayInputStream(data), writer);
					}
					attachmentList.add(tempFile);
				}
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Clean the message from attachments.
	 *
	 * @param msg
	 *            the msg
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void clean(InputStream msg) throws IOException {
		parse(new POIFSFileSystem(msg).getRoot());
	}

	/**
	 * Parses the.
	 *
	 * @param node
	 *            the node
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void parse(DirectoryNode node) throws IOException {
		// FIXME currently not working
	}
}
