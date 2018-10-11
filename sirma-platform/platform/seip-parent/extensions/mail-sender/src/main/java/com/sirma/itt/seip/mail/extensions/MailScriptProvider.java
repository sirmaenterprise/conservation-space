package com.sirma.itt.seip.mail.extensions;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.toArray;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.mail.DefaultNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.mails.UsersMailExtractor;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Add support for sending emails from server side JavaScript snippets. <br>
 * suggested usage is
 * <pre><code>
 *     mail.createNew()
 *     		.instance(mainAffectedInstance)
 *     		.fromSystem()
 *     		.subject('Mail subject goes here')
 *     		.template('mail.template')
 *     		.addAttachment(attachment1)
 *     		.addAttachment(attachment2)
 *     		.addProperty('templateProperty1', 'propertyValue')
 *     		.addRelevantNodes(nodes)
 *     		.sendTo(recipient1, recipient2, ...); // should be called at the end, do actual sending
 * </code></pre>
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 17)
public class MailScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String MAIL_SCRIPT = "mail-actions.js";

	private static final User SYSTEM = new EmfUser("$SYSTEM$");

	@Inject
	private javax.enterprise.inject.Instance<MailNotificationService> mailNotificationService;

	@Inject
	private UsersMailExtractor usersMailExtractor;

	@Inject
	private ResourceService resourceService;

	@Inject
	private SecurityContext securityContext;
	@Inject
	private SecurityContextManager securityContextManager;

	private boolean enabled = true;

	/**
	 * Initialize bean.
	 */
	@PostConstruct
	public void initialize() {
		enabled = !mailNotificationService.isUnsatisfied();
		if (!enabled) {
			LOGGER.info("Mail service implementation not found. No mail integration in script api");
		}
	}

	@Override
	public Map<String, Object> getBindings() {
		if (!enabled) {
			return Collections.emptyMap();
		}
		return Collections.singletonMap("mail", this);
	}

	@Override
	public Collection<String> getScripts() {
		if (!enabled) {
			return Collections.emptyList();
		}
		return ResourceLoadUtil.loadResources(getClass(), MAIL_SCRIPT);
	}

	/**
	 * Create new mail sending context. The returned context has convenient methods for setting all possible attributes
	 * for sending an email to one or more recipients.
	 *
	 * @return new sending context instance
	 */
	public SendContext createNew() {
		LOGGER.trace("Starting new mail creation");
		return new SendContext();
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode The root node - specific script node implementation
	 * @param subject the subject of the message to be sent
	 * @param templateId The template id - String containing the name of e-mail template to be send
	 * @param recipients Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 * the corresponding user name should be entered. If recipient is group of users than the group name
	 * should be entered.
	 * @deprecated Should use the more explicit {@link #createNew()} and {@link SendContext}
	 */
	@Deprecated
	public void sendNotifications(ScriptInstance rootNode, String subject, String templateId, String[] recipients) {
		// Gives warning and can we just use MailAttachment[] instead of MailAttachment.... ?
		createNew().instance(rootNode)
				.subject(subject)
				.template(templateId)
				.fromSystem()
				.sendTo(recipients);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode The root node - specific script node implementation
	 * @param subject the subject of the message to be sent
	 * @param templateId The template id - String containing the name of e-mail template to be send
	 * @param recipients Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 * the corresponding user name should be entered. If recipient is group of users than the group name
	 * should be entered.
	 * @param relevantNodes all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @deprecated Should use the more explicit {@link #createNew()} and {@link SendContext}
	 */
	@Deprecated
	public void sendNotifications(ScriptInstance rootNode, String subject, String templateId, String[] recipients,
			ScriptInstance[] relevantNodes) {
		createNew().instance(rootNode)
				.subject(subject)
				.template(templateId)
				.addRelevantNodes(relevantNodes)
				.fromSystem()
				.sendTo(recipients);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode The root node - specific script node implementation
	 * @param user user executed the operation (currently logged user).
	 * @param subject the subject of the message to be sent
	 * @param templateId The template id - String containing the name of e-mail template to be send
	 * @param recipients Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 * the corresponding user name should be entered. If recipient is group of users than the group name
	 * should be entered.
	 * @param attachments the attachments which will be set to the email. Can be path to files, document instances ids or both
	 * @deprecated Should use the more explicit {@link #createNew()} and {@link SendContext}
	 */
	@Deprecated
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId,
			String[] recipients, MailAttachment... attachments) {
		createNew().instance(rootNode)
				.subject(subject)
				.template(templateId)
				.addAttachments(attachments)
				.fromUser(user == null ? SYSTEM : user)
				.sendTo(recipients);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode The root node - specific script node implementation
	 * @param user user executed the operation (currently logged user).
	 * @param subject the subject of the message to be sent
	 * @param templateId The template id - String containing the name of e-mail template to be send
	 * @param recipients Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 * the corresponding user name should be entered. If recipient is group of users than the group name
	 * should be entered.
	 * @param relevantNodes all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @param attachments the attachments which will be set to the email. Can be path to files, document instances ids or both
	 * @deprecated Should use the more explicit {@link #createNew()} and {@link SendContext}
	 */
	@Deprecated
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId,
			String[] recipients, ScriptInstance[] relevantNodes, MailAttachment... attachments) {
		createNew().instance(rootNode)
				.subject(subject)
				.template(templateId)
				.addRelevantNodes(relevantNodes)
				.addAttachments(attachments)
				.fromUser(user == null ? SYSTEM : user)
				.sendTo(recipients);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode The root node - specific script node implementation
	 * @param user user executed the operation (currently logged user).
	 * @param subject the subject of the message to be sent
	 * @param templateId The template id - String containing the name of e-mail template to be send
	 * @param recipients Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 * the corresponding user name should be entered. If recipient is group of users than the group name
	 * should be entered.
	 * @param properties additional properties that are not contained in the instance or user, and we want to use them in the
	 * template.
	 * @param attachments the attachments which will be set to the email. Can be path to files, document instances ids or both
	 * @deprecated Should use the more explicit {@link #createNew()} and {@link SendContext}
	 */
	@Deprecated
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId,
			String[] recipients, Map<String, Object> properties, MailAttachment... attachments) {
		createNew().instance(rootNode)
				.subject(subject)
				.template(templateId)
				.addProperties(properties)
				.addAttachments(attachments)
				.fromUser(user == null ? SYSTEM : user)
				.sendTo(recipients);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode The root node - specific script node implementation
	 * @param user user executed the operation (currently logged user).
	 * @param subject the subject of the message to be sent
	 * @param templateId The template id - String containing the name of e-mail template to be send
	 * @param recipients Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 * the corresponding user name should be entered. If recipient is group of users than the group name
	 * should be entered.
	 * @param relevantNodes all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @param properties additional properties that are not contained in the instance or user, and we want to use them in the
	 * template.
	 * @param attachments the attachments which will be set to the email. Can be path to files, document instances ids or both
	 * @deprecated Should use the more explicit {@link #createNew()} and {@link SendContext}
	 */
	@SuppressWarnings("squid:S00107")
	@Deprecated
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId,
			String[] recipients, ScriptInstance[] relevantNodes, Map<String, Object> properties,
			MailAttachment... attachments) {

		createNew().instance(rootNode)
				.subject(subject)
				.template(templateId)
				.addRelevantNodes(relevantNodes)
				.addProperties(properties)
				.addAttachments(attachments)
				.fromUser(user == null ? SYSTEM : user)
				.sendTo(recipients);
	}

	public class SendContext {
		private ScriptInstance rootNode;
		private User user = SYSTEM;
		private String subject;
		private String templateId;
		private List<ScriptInstance> relevantNodes = new LinkedList<>();
		private Map<String, Object> properties = new HashMap<>();
		private List<MailAttachment> attachments = new LinkedList<>();

		/**
		 * Sets the main affected instance, normally {@code root}
		 *
		 * @param instance the affected instance
		 * @return current context for chaining
		 */
		public SendContext instance(ScriptInstance instance) {
			rootNode = instance;
			return this;
		}

		/**
		 * Set the email FROM field. The value will be taken from the given user if any. Otherwise default system mail
		 * will be used.
		 *
		 * @param user the user to get it's email for mail origin
		 * @return current context for chaining
		 */
		public SendContext fromUser(User user) {
			this.user = user;
			LOGGER.trace("Setting FROM: {}", user);
			return this;
		}

		/**
		 * Set the email FROM field. Set explicit email that should be set for origin in the send email.
		 *
		 * @param email the valid email to set in the from field in the send email
		 * @return current context for chaining
		 */
		public SendContext fromEmail(String email) {
			if (StringUtils.isNotBlank(email)) {
				LOGGER.trace("Setting custom FROM: {}", email);
				EmfUser dummyUser = new EmfUser(email);
				dummyUser.setEmail(email);
				this.user = dummyUser;
			} else {
				user = null;
			}
			return this;
		}

		/**
		 * Set the email FROM field. The value will be taken from the currently logged in user, if
		 * present, or will use the default system one.
		 *
		 * @return current context for chaining
		 * @see #fromSystem()
		 */
		public SendContext fromCurrentUser() {
			if (securityContextManager.isAuthenticatedAsAdmin()) {
				// the admin email may not be the correct or not set. Generally users should not receive emails
				// from the admin itself but from generic email. CMF-26585
				return fromSystem();
			}
			Resource resource = resourceService.findResource(securityContext.getAuthenticated());
			if (resource.getEmail() == null) {
				LOGGER.info("Trying to send email from the current user {}, but he/she does not have an email set. "
								+ "Will be send from the name of the system.", resource.getName());
				return fromSystem();
			}
			LOGGER.trace("Setting FROM: current user {}", resource);
			user = (User) resource;
			return this;
		}

		/**
		 * Set the email FROM field. Set that the email for the mail origin will be the one defined in the current
		 * tenant configuration.
		 *
		 * @return current context for chaining
		 */
		public SendContext fromSystem() {
			LOGGER.trace("Setting mail FROM: System");
			this.user = SYSTEM;
			return this;
		}

		/**
		 * Set the mail subject field
		 *
		 * @param subject the mail subject
		 * @return current context for chaining
		 */
		public SendContext subject(String subject) {
			this.subject = subject;
			LOGGER.trace("Setting mail Subject: {}", subject);
			return this;
		}

		/**
		 * Set the template to be used when sending the email. Required field.
		 *
		 * @param templateId the identifier of the template to load and send
		 * @return current context for chaining
		 */
		public SendContext template(String templateId) {
			this.templateId = Objects.requireNonNull(templateId, "The template id is required");
			LOGGER.trace("Setting mail template: {}", templateId);
			return this;
		}

		/**
		 * Add array of relevant nodes mentioned in the send email.
		 *
		 * @param nodes the nodes that should be mentioned in the send email.
		 * @return current context for chaining
		 */
		public SendContext addRelevantNodes(ScriptInstance... nodes) {
			if (nodes != null) {
				relevantNodes.addAll(Arrays.asList(nodes));
			}
			return this;
		}

		/**
		 * Add custom properties that should be available in the mail template context.
		 *
		 * @param properties the properties to add
		 * @return current context for chaining
		 */
		public SendContext addProperties(Map<String, Object> properties) {
			this.properties.putAll(properties);
			return this;
		}

		/**
		 * Add single custom property that should be available in the mail template context.
		 *
		 * @param key the property key
		 * @param value the property value
		 * @return current context for chaining
		 */
		public SendContext addProperty(String key, Object value) {
			this.properties.put(key, value);
			return this;
		}

		/**
		 * Add an attachment file that should be send with the mail.
		 *
		 * @param attachment the attachment descriptor to add
		 * @return current context for chaining
		 */
		public SendContext addAttachment(MailAttachment attachment) {
			addNonNullValue(attachments, attachment);
			return this;
		}

		/**
		 * Add attachment files that should be send with the mail.
		 *
		 * @param mailAttachments the attachment descriptors to add
		 * @return current context for chaining
		 */
		public SendContext addAttachments(MailAttachment... mailAttachments) {
			if (mailAttachments != null) {
				attachments.addAll(Arrays.asList(mailAttachments));
			}
			return this;
		}

		/**
		 * Terminal operation. Perform the actual send to the given list of recipients. <br>
		 * The method will fail if no recipients are passed.
		 *
		 * @param recipients the list of recipients to send the mail to.
		 */
		public void sendTo(String... recipients) {
			if (recipients == null || recipients.length == 0) {
				throw new IllegalArgumentException(
						String.format("Tried to send an email %s/%s, but forget to pass recipients", subject,
								templateId));
			}

			Collection<String> recipientsEmails = usersMailExtractor.extractMails(Arrays.asList(recipients),
					rootNode.getTarget());

			if (recipientsEmails.isEmpty()) {
				LOGGER.warn("Could not extract any valid emails form {}", Arrays.asList(recipients));
				return;
			}
			LOGGER.trace("Sending emails to: {}", recipientsEmails);

			User loggedUser = getUser(user);

			Function<String, MailNotificationContext> contextBuilder = createContext(rootNode, loggedUser, subject,
					templateId, toArray(relevantNodes, ScriptInstance.class), properties,
					toArray(attachments, MailAttachment.class));
			String mailGroupId = UUID.randomUUID().toString();
			recipientsEmails.stream().map(contextBuilder).forEach(sendMessageForGroupId(mailGroupId));
		}

		private Consumer<MailNotificationContext> sendMessageForGroupId(String mailGroupId) {
			return mailNotificationContext -> {
				for (MailNotificationService service : mailNotificationService) {
					service.sendEmail(mailNotificationContext, mailGroupId);
				}
			};
		}

		private User getUser(User user) {
			if (user == null) {
				com.sirma.itt.seip.security.User effectiveUser;
				if (!securityContext.isSystemTenant()) {
					effectiveUser = securityContext.getAuthenticated();
				} else {
					// as system user, using the admin user produce confusing results
					return null;
				}

				return resourceService.getResource(effectiveUser.getSystemId());
			} else if (SYSTEM.equals(user)) {
				// the send from mail will be fetched from the a configuration
				return null;
			}

			return user;
		}

		/**
		 * Creates the mail notification contexts builder that will create single context for each recipient pass to the
		 * returned function
		 *
		 * @param rootNode The root node - specific script node implementation
		 * @param user user executed the operation (currently logged user)
		 * @param subject the subject of the message to be sent
		 * @param templateId The template id - String containing the name of e-mail template to be send
		 * @param relevantNodes all relevant nodes to be put in data model for latter processing in the freemarker template.
		 * @param properties additional properties that are not contained in the instance or user, and we want to use them in the
		 * template.
		 * @return list of prepared {@link MailNotificationContext}
		 */
		private Function<String, MailNotificationContext> createContext(ScriptInstance rootNode, User user,
				String subject, String templateId, ScriptInstance[] relevantNodes,
				Map<String, Object> properties, MailAttachment[] attachments) {
			return recipient ->
					new DefaultNotificationContext(recipient, user, subject, templateId, rootNode, relevantNodes,
							properties, attachments);
		}
	}

}
