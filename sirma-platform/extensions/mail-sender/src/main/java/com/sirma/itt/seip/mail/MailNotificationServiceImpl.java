package com.sirma.itt.seip.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplatesSynchronizedEvent;
import com.sirma.itt.seip.time.TimeTracker;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * The MailNotificationService is class that dynamically builds a ftl template with the details provided in the
 * {@link MailNotificationContext}.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class MailNotificationServiceImpl implements MailNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

	private static final String EMAIL_TEMPLATE = "emailTemplate";

	private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
			Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	@Inject
	private MailService mailService;

	@Inject
	private MailNotificationHelperService helperService;

	/** The freemarker configuration. */
	private Configuration configuration;

	@Inject
	protected ContextualTemplateLoader templateLoader;

	@Inject
	protected TemplateService documentTemplateService;

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "notifications.enabled", defaultValue = "true", type = Boolean.class, label = "Activates mail notification sub system.")
	private ConfigurationProperty<Boolean> notificationEnabled;

	@Inject
	private ContextualLock lock;

	@Inject
	private CodelistService codelistService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private PermissionService service;

	@Inject
	private ResourceService resourceService;

	@Inject
	private MailResourceProvider mailResourceProvider;

	@Inject
	private InstanceContextInitializer contextInitializer;

	@Inject
	private TypeConverter typeConverter;

	/**
	 * Inits the FTL configuration. The ftl configuration is initialized and path to the ftl files is set. Definitions
	 * are dowloaded from dms as final step and any exists in the store location it is overwritten
	 *
	 * @throws IOException
	 *             if ftl location could not be found
	 */
	@PostConstruct
	public void init() {
		/* Create and adjust the configuration */
		configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
		configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		configuration.setTemplateLoader(templateLoader);
	}

	/**
	 * Sync templates on application start up.
	 */
	@RunAsAllTenantAdmins(parallel = true)
	@Startup(async = true, phase = StartupPhase.AFTER_APP_START)
	public void onApplicationStart() {
		// load templates asynchronously
		downloadDefinitions();
	}

	/**
	 * Download definitions from dms system. If there is exception error is printed and server continues its startup
	 */
	private void downloadDefinitions() {
		// the locking is because the trigger for template loading could come
		// from different places
		// during startup and to prevent multiple parallel loading
		if (lock.tryLock()) {
			LOGGER.debug("Initiated mail template synchronization!");
			TimeTracker timeTracker = new TimeTracker().begin();
			try {
				List<TemplateInstance> templates = documentTemplateService.getTemplates(EMAIL_TEMPLATE);
				LOGGER.trace("Found [{}] email templates to load.", templates.size());

				List<GenericAsyncTask> tasks = new ArrayList<>(templates.size());
				for (final TemplateInstance template : templates) {
					tasks.add(new GenericAsyncTaskExtension(template, documentTemplateService, templateLoader));
				}
				taskExecutor.execute(tasks);
			} catch (Exception e) {
				LOGGER.warn("Notification templates could not be downloaded!", e);
			} finally {
				lock.unlock();
				LOGGER.debug("Mail template synchronization completed in {} ms", timeTracker.stop());
			}
		} else {
			LOGGER.debug("Template realoding is in progress. Skipping request.");
		}
	}

	@Override
	public void sendEmail(MailNotificationContext delegate) {
		sendEmail(delegate, UUID.randomUUID().toString());
	}

	@Override
	public void sendEmail(MailNotificationContext delegate, String groupId) {
		if (configuration == null) {
			LOGGER.warn("The e-mail notification service is not configured! Ignoring request!");
			return;
		}
		if (delegate == null) {
			return;
		}
		if (!notificationEnabled.get().booleanValue()) {
			LOGGER.warn("E-mail notification service is disabled! Ignoring request!");
			return;
		}
		Map<String, Object> preparedModel = prepareModel(delegate);
		try {
			Collection<String> sendTo = delegate.getSendTo();
			if (CollectionUtils.isEmpty(sendTo)) {
				LOGGER.warn("No receivers are found for " + delegate.getClass().getName());
				return;
			}

			MailAttachment[] attachments = delegate.getAttachments();
			if (attachments != null && attachments.length > 0) {
				sendMailInternal(delegate, preparedModel, sendTo, groupId, attachments);
			} else {
				sendMailInternal(delegate, preparedModel, sendTo, groupId);
			}
		} catch (Exception e) {
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
	 * @param mailGroupId
	 *            the id used to extract all the mails with the same message
	 * @param userMails
	 *            the user mails to with the mail will be send
	 * @param mailAttachments
	 *            the mail attachments which should be send
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TemplateException
	 *             the template exception
	 */
	private void sendMailInternal(MailNotificationContext delegate, Map<String, Object> preparedModel,
			Collection<String> userMails, String mailGroupId, MailAttachment... mailAttachments)
					throws IOException, TemplateException {
		if (userMails == null || userMails.isEmpty()) {
			LOGGER.warn("Will not send message, because there are no recipients.");
			return;
		}

		String sendFromMail = null;
		// extract send from
		if (delegate.getSendFrom() != null) {
			String userMail = delegate.getSendFrom().getUserMail();
			if (userMail != null && EMAIL_PATTERN.matcher(userMail).matches()) {
				sendFromMail = userMail;
			}
		}

		Template temp = configuration.getTemplate(delegate.getTemplateId());
		try (Writer templateWriter = new StringWriter()) {
			temp.process(preparedModel, templateWriter);

			LOGGER.trace("Processed message: \n {}", templateWriter.toString());

			String mailsId = mailGroupId;
			if (StringUtils.isNullOrEmpty(mailsId)) {
				mailsId = UUID.randomUUID().toString();
			}
			// use send from if it is available
			if (sendFromMail != null) {
				mailService.enqueueMessage(userMails, delegate.getSubject(), mailsId, templateWriter.toString(),
						sendFromMail, mailAttachments);
			} else {
				mailService.enqueueMessage(userMails, delegate.getSubject(), mailsId, templateWriter.toString(),
						mailAttachments);
			}
		}
	}

	/**
	 * Prepare model by adding some system data and as final step the model provided by the delegate.
	 *
	 * @param delegate
	 *            the delegate to get data from
	 * @return the map containing the model to be provided to ftl engine
	 */
	private Map<String, Object> prepareModel(MailNotificationContext delegate) {
		Map<String, Object> model = new HashMap<>(delegate.getModel());
		model.put("codelists", codelistService);
		model.put("permissions", service);
		model.put("resource", resourceService);
		model.put("notifications", helperService);
		model.put("utils", mailResourceProvider);
		model.put("labels", labelProvider);
		setTargetParent(model);
		return model;
	}

	private void setTargetParent(Map<String, Object> model) {
		Object target = model.get("target");
		if (target instanceof ScriptInstance) {
			Instance instance = ((ScriptInstance) target).getTarget();
			contextInitializer.restoreHierarchy(instance);
			Instance parent = InstanceUtil.getDirectParent(instance);
			ScriptInstance parentScriptInstance = typeConverter.convert(ScriptInstance.class, parent);
			model.put("parent", parentScriptInstance);
		}
	}

	/**
	 * Updates the content of all email templates whenever the latter are already synchronised with the DMS
	 *
	 * @param event
	 *            {@link TemplatesSynchronizedEvent} fired to notify that the template loading has been completed
	 */
	@RunAsSystem
	public void synchronizeTemplates(@Observes TemplatesSynchronizedEvent event) {
		taskExecutor.executeAsync(this::downloadDefinitions);
	}

	/**
	 * The Class GenericAsyncTaskExtension.
	 *
	 * @author BBonev
	 */
	private static final class GenericAsyncTaskExtension extends GenericAsyncTask {

		private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private static final long serialVersionUID = -7461018027435347768L;

		private final TemplateInstance template;

		private TemplateService documentTemplateService;

		private ContextualTemplateLoader templateLoader;

		/**
		 * Instantiates a new generic async task extension.
		 *
		 * @param template
		 *            the template
		 * @param documentTemplateService
		 *            the document template service
		 * @param templateLoader
		 *            the tamplate loader
		 */
		protected GenericAsyncTaskExtension(TemplateInstance template, TemplateService documentTemplateService,
				ContextualTemplateLoader templateLoader) {
			this.template = template;
			this.documentTemplateService = documentTemplateService;
			this.templateLoader = templateLoader;
		}

		@Override
		protected boolean executeTask() throws Exception {
			LOG.trace("Loading content of email template with id [{}]", template.getIdentifier());

			documentTemplateService.loadContent(template);
			templateLoader.putTemplate(template.getIdentifier(), template.getContent());

			return true;
		}
	}

}
