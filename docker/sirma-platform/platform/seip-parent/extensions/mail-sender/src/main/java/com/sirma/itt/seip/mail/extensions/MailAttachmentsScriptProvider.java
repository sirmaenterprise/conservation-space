package com.sirma.itt.seip.mail.extensions;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.descriptor.LocalFileDescriptor;

/**
 * Contains methods for creating mails attachments, which can be passed to the mail sander. The methods are exposed and
 * can be used in the definitions actions scripts.
 *
 * @author A. Kunchev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 211)
public class MailAttachmentsScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("attachments", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Creates new {@link MailAttachments} object.
	 *
	 * @return {@link MailAttachments} object
	 */
	public MailAttachments createMailAttachments() {
		LOGGER.info("Producing new MailAttachments object.");
		Instance instance = new EmfInstance();
		instance.setId(UUID.randomUUID().toString());
		return new MailAttachments(instance, instanceContentService, tempFileProvider);
	}

	/**
	 * Used to build and collect mail attachments.
	 *
	 * @author A. Kunchev
	 */
	public static class MailAttachments {

		private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		int index;

		private List<Content> contents;

		private Instance contentInstance;

		private InstanceContentService instanceContentService;

		private TempFileProvider tempFileProvider;

		/**
		 * Constructs a mail object given some data.
		 *  @param instance
		 *            the instance used to associate the contents, when they are saved
		 * @param instanceContentService
		 *            the instance service used to extract and save attachments content
		 * @param tempFileProvider
		 */
		public MailAttachments(Instance instance,
				InstanceContentService instanceContentService, TempFileProvider tempFileProvider) {
			contents = new ArrayList<>();
			index = 0;
			contentInstance = instance;
			this.instanceContentService = instanceContentService;
			this.tempFileProvider = tempFileProvider;
		}

		/**
		 * Adds mail attachments directly from file. Adds the file to the collection of {@link Content}.
		 * <p>
		 * The file/s won't be add as attachment when: <br />
		 * - the input argument is null or it's length is 0 <br />
		 * - there are a path, which value is null or empty <br />
		 * - the passed path/s is not resolved as a file <br />
		 * - there are IO problems with the file
		 *
		 * @param paths
		 *            the paths to the files, which should be added as mail attachments
		 * @return {@link MailAttachments} object
		 */
		public MailAttachments addFromPath(String... paths) {
			if (paths != null && paths.length > 0) {
				for (String path : paths) {
					if (StringUtils.isBlank(path)) {
						LOG.warn("The path is empty or null.");
						return this;
					}

					addAttachmentFromPath(path);
				}
			}
			return this;
		}

		private void addAttachmentFromPath(String path) {
			File file = new File(path);
			if (file.exists() && file.isFile()) {
				String name = file.getName();
				Content content = Content
						.createEmpty()
							.setName(name)
							.setContentLength(Long.valueOf(file.length()))
							.setContent(new LocalFileDescriptor(path))
							.setPurpose(genratePurpose());
				contents.add(content);
			}
		}

		/**
		 * Adds mail attachment from passed instance. The instance is passed as {@link ScriptInstance}. For the content
		 * extraction is used {@link InstanceContentService}. Adds the instance content in the
		 * collection of {@link Content}, which represents the mail attachments.
		 * <p>
		 * The instance won't be added as attachment, when: <br />
		 * - the input argument is null or it length is 0 <br />
		 * - the script instance is null <br />
		 * - the target instance is null <br />
		 * - the target instance is not a document instance <br />
		 * - the {@link InstanceContentService} returns non existing content
		 *
		 * @param instances
		 *            the instances, which should be added as mail attachments
		 * @return {@link MailAttachments} object
		 */
		public MailAttachments addFromInstance(ScriptInstance... instances) {
			if (instances != null && instances.length > 0) {
				for (ScriptInstance instance : instances) {
					if (instance == null) {
						LOG.warn("The passed instance is null.");
						return this;
					}

					addAttachmentFormInstance(instance);
				}
			}
			return this;
		}

		private void addAttachmentFormInstance(ScriptInstance scriptNode) {
			Instance instance = scriptNode.getTarget();
			if (instance == null) {
				LOG.warn("There is no target instance for ScriptInstance with id [{}]", scriptNode.getId());
				return;
			}

			Serializable targetId = instance.getId();
			Object target = instance;

			BiFunction<Object, File, Content> builder = this::buildContent;

			ContentInfo contentInfo = instanceContentService.getContent(targetId, Content.PRIMARY_CONTENT);
			if (contentInfo.exists()) {
				target = contentInfo;
				builder = this::buildContentFromContentInfo;
			}

			File file = writeToFile(contentInfo);
			if (file != null) {
				contents.add(builder.apply(target, file));
			} else {
				LOG.warn("The content for the instance with id [{}] is not found.", targetId);
			}
		}

		private File writeToFile(FileDescriptor descriptor) {
			if (descriptor == null) {
				LOG.debug("The file descriptor is null. The attachment won't be added.");
				return null;
			}

			File tempFile = tempFileProvider.createTempFile("MailAttachment", null);
			try {
				descriptor.writeTo(tempFile);
				return tempFile;
			} catch (IOException e) {
				LOG.debug("IO while writing in the temp file. The produced filed will be empty.", e);
			}

			tempFile.delete();
			return null;
		}

		/**
		 * Builds {@link Content} object from passed {@link ContentInfo} object and file.
		 *
		 * @param info
		 *            the contentInfo from which will be get the information about the content
		 * @param file
		 *            the file with the content
		 * @return new {@link Content}
		 */
		Content buildContentFromContentInfo(Object info, File file) {
			ContentInfo contentInfo = (ContentInfo) info;
			return Content
					.createEmpty()
						.setName(contentInfo.getName())
						.setContent(file)
						.setMimeType(contentInfo.getMimeType())
						.setPurpose(genratePurpose());
		}

		/**
		 * Builds {@link Content} object from passed {@link Instance} and file.
		 *
		 * @param target
		 *            the instance from which will be get the information about the content
		 * @param file
		 *            the file with the content
		 * @return new {@link Content}
		 */
		Content buildContent(Object target, File file) {
			Instance instance = (Instance) target;
			String escapedId = ((String) instance.getId()).replace(":", "-");
			return Content
					.createEmpty()
						.setName(instance.getString(DefaultProperties.NAME, escapedId))
						.setContent(file)
						.setMimeType(instance.getString(DefaultProperties.MIMETYPE))
						.setPurpose(genratePurpose());
		}

		private String genratePurpose() {
			return String.valueOf(index++);
		}

		/**
		 * Collects all the added attachments and return them.
		 * <p>
		 * May return empty array, when: <br />
		 * - no attachments are added before collecting <br />
		 * - the content is not extracted form the passed paths or instances
		 * </p>
		 *
		 * @return collection of {@link MailAttachment}
		 */
		public MailAttachment[] collect() {
			MailAttachment[] attachments = new MailAttachment[contents.size()];
			if (contents.isEmpty()) {
				LOG.warn("Add attachments before collecting them.");
				return attachments;
			}

			List<ContentInfo> contentIds = instanceContentService.saveContent(contentInstance, contents);
			for (int i = 0; i < contents.size(); i++) {
				String contentId = contentIds.get(i).getContentId();
				if (StringUtils.isNotBlank(contentId)) {
					Content content = contents.get(i);
					attachments[i] = new MailAttachment(content.getName(), content.getMimeType(), contentId);
				}
			}

			int attachmentCount = attachments.length;
			if (attachmentCount == 0) {
				LOG.warn("There are not attachment created, check the log for more information.");
			}

			LOG.info("The added attachment count is [{}].", attachmentCount);
			return attachments;
		}

	}

}
