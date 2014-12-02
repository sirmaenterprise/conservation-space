package com.sirma.itt.cmf.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.CMFMailNotificaionAdapterService;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.NonTxAsyncCallableEvent;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.io.ContentService;
import com.sirma.itt.emf.mail.MailService;
import com.sirma.itt.emf.mail.notification.MailNotificationContext;
import com.sirma.itt.emf.mail.notification.MailNotificationService;
import com.sirma.itt.emf.mail.notification.SyncMailNotificationTemplatesEvent;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.time.TimeTracker;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * The MailNotificationService is class that dynamically builds a ftl template with the details
 * provided in the {@link MailNotificationContext}.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class MailNotificationServiceImpl implements MailNotificationService {

	/** The sync templates. */
	private final SyncTemplates syncTemplates = new SyncTemplates();
	/** The Constant EMAIL_PATTERN. */
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", Pattern.CANON_EQ
					| Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	/** The template storage. */
	@Inject
	@Config(name = EmfConfigurationProperties.NOTIFICATIONS_TEMPLATES_STORAGE_DIRECTORY)
	private String templateStorage;
	/** The mail service. */
	@Inject
	private MailService mailService;

	/** The helper service. */
	@Inject
	private MailNotificationHelperService helperService;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

	/** The cfg. */
	private Configuration cfg;

	/** The search service. */
	@Inject
	private CMFMailNotificaionAdapterService searchService;

	/** The content service. */
	@Inject
	private ContentService contentService;

	/** The task executor. */
	@Inject
	private TaskExecutor taskExecutor;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The notification enabled. */
	@Inject
	@Config(name = EmfConfigurationProperties.NOTIFICATIONS_ENABLED, defaultValue = "true")
	private Boolean notificationEnabled;

	/**
	 * Inits the mail service. The ftl configuration is initialized and path to the ftl files is
	 * set. Definitions are dowloaded from dms as final step and any exists in the store location it
	 * is overwritten
	 *
	 * @throws IOException
	 *             if ftl location could not be found
	 */
	@PostConstruct
	public void init() throws IOException {
		if (templateStorage == null) {
			LOGGER.error("Missing required configuration for e-mail notifications. The service will be disabled until configured!");
			return;
		}
		File configDir = new File(templateStorage);
		if (!configDir.exists() || !configDir.isDirectory()) {
			LOGGER.error(
					"Configured folder path {} does not exist or is not a folder. The service will be disabled until configured!",
					configDir.getAbsoluteFile());
			return;
		}
		/* Create and adjust the configuration */
		cfg = new Configuration();
		cfg.setDirectoryForTemplateLoading(configDir);
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
	}

	/**
	 * Sync templates on application start up.
	 * 
	 * @param event
	 *            the input event
	 */
	@Secure(runAsSystem = true)
	public void onApplicationStart(@Observes ApplicationStartupEvent event) {
		// load templates asynchronously
		synchronizeTemplates(null);
	}

	/**
	 * Download definitions from dms system. If there is exception error is printed and server
	 * continues its startup
	 */
	private void downloadDefinitions() {
		try {
			List<FileDescriptor> templates = searchService.getTemplates();
			List<GenericAsyncTask> tasks = new ArrayList<GenericAsyncTask>(templates.size());
			for (final FileDescriptor template : templates) {
				tasks.add(new GenericAsyncTaskExtension(template));
			}
			taskExecutor.execute(tasks);
		} catch (Exception e) {
			LOGGER.warn("Notification templates could not be downloaded!", e);
		}
	}

	/**
	 * Send email to the user specified by the delegate's data.
	 *
	 * @param delegate
	 *            the delegate
	 * @throws EmfRuntimeException
	 *             on unexpected error during build of model - wrong users or model
	 */
	@Override
	public void sendEmail(MailNotificationContext delegate) throws EmfRuntimeException {
		if (cfg == null) {
			LOGGER.warn("The e-mail notification service is not configured! Ignoring request!");
			return;
		}
		if (delegate == null) {
			return;
		}
		if (!notificationEnabled.booleanValue()) {
			LOGGER.warn("E-mail notification service is disabled! Ignoring request!");
			return;
		}
		Map<String, Object> preparedModel = prepareModel(delegate);
		try {
			Pair<List<String>, List<String>> usersMails = null;
			// exract send to
			if ((delegate.getSendTo() != null) && !delegate.getSendTo().isEmpty()) {
				usersMails = getUsersMails(delegate.getSendTo());
			} else {
				LOGGER.warn("No receivers are found for " + delegate.getClass().getName());
				return;
			}
			sendMailInternal(delegate, preparedModel, usersMails);
		} catch (Exception e) {
			// indicate programmer error
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Send mail internal.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param preparedModel
	 *            the prepared model
	 * @param usersMails
	 *            the users mails
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TemplateException
	 *             the template exception
	 */
	private void sendMailInternal(MailNotificationContext delegate,
			Map<String, Object> preparedModel, Pair<List<String>, List<String>> usersMails)
			throws IOException, TemplateException {
		if (!usersMails.getFirst().isEmpty()) {
			String sendFromMail = null;
			// extract send from
			if (delegate.getSendFrom() != null) {
				Pair<List<String>, List<String>> sendFromPair = getUsersMails(Collections
						.singletonList(delegate.getSendFrom()));
				if (sendFromPair.getFirst().size() == 1) {
					sendFromMail = sendFromPair.getFirst().get(0);
				}
			}
			Template temp = cfg.getTemplate(delegate.getTemplateId());
			Writer templateWriter = new StringWriter();
			temp.process(preparedModel, templateWriter);
			IOUtils.closeQuietly(templateWriter);
			// use send from if it is available
			if (sendFromMail != null) {
				mailService.sendMessage(usersMails.getFirst(), delegate.getSubject(),
						templateWriter.toString(), sendFromMail);
			} else {
				mailService.sendMessage(usersMails.getFirst(), delegate.getSubject(),
						templateWriter.toString());
			}
		} else {
			LOGGER.warn("Mails have not been sent to the following users: "
					+ usersMails.getSecond());
		}
	}

	/**
	 * Prepare model by adding some system data and as final step the model provided by the
	 * delegate.
	 *
	 * @param delegate
	 *            the delegate to get data from
	 * @return the map containing the model to be provided to ftl engine
	 */
	private Map<String, Object> prepareModel(MailNotificationContext delegate) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("codelists", helperService.getCodelistService());
		model.put("notifications", helperService);
		model.put("labels", helperService.getLabelProvider());
		model.putAll(delegate.getModel());
		return model;
	}

	/**
	 * Extracts the users mails from a list of users. Successfully extracted mails are contained in
	 * the first key of the pair, the skipped users are in the second
	 *
	 * @param users
	 *            the users to iterate
	 * @return the users mails and the list of skipped users
	 */
	private Pair<List<String>, List<String>> getUsersMails(Collection<Resource> users) {
		if (users == null) {
			return null;
		}
		List<String> usersMails = new ArrayList<String>();
		List<String> notFound = new ArrayList<String>();
		for (Resource user : users) {
			if (user.getType() == ResourceType.USER) {
				Serializable mail = user.getProperties().get(ResourceProperties.EMAIL);
				if ((mail != null) && EMAIL_PATTERN.matcher(mail.toString()).matches()) {
					usersMails.add(mail.toString());
				} else {
					notFound.add(user.getIdentifier());
				}
			}
		}
		return new Pair<List<String>, List<String>>(usersMails, notFound);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure(runAsSystem = true)
	public void synchronizeTemplates(@Observes SyncMailNotificationTemplatesEvent event) {
		eventService.fire(new NonTxAsyncCallableEvent(syncTemplates, SecurityContextManager
				.getCurrentSecurityContext()));
	}

	/**
	 * The Class GenericAsyncTaskExtension.
	 * 
	 * @author BBonev
	 */
	private final class GenericAsyncTaskExtension extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -7461018027435347768L;
		/** The template. */
		private final FileDescriptor template;

		/**
		 * Instantiates a new generic async task extension.
		 * 
		 * @param template
		 *            the template
		 */
		private GenericAsyncTaskExtension(FileDescriptor template) {
			this.template = template;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean executeTask() throws Exception {
			try (FileOutputStream output = new FileOutputStream(new File(
					templateStorage, template.getId()))) {
				contentService.getContent(template, output);
			}
			return true;
		}
	}

	/**
	 * The Class SyncTemplates.
	 * 
	 * @author bbonev
	 */
	private class SyncTemplates implements Callable<Void> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void call() throws Exception {
			LOGGER.debug("Initiated mail template synchronization!");
			TimeTracker timeTracker = new TimeTracker().begin();
			downloadDefinitions();
			LOGGER.debug("Mail template synchronization completed in "
					+ timeTracker.stopInSeconds() + " s");
			return null;
		}

	}
}
