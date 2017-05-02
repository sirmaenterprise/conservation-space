package com.sirma.itt.seip.mail.extensions;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.mail.DefaultNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.mails.UsersMailExtractor;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Provider extension to add mail extension.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 17)
public class MailScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String MAIL_SCRIPT = "mail-actions.js";

	@Inject
	private javax.enterprise.inject.Instance<MailNotificationService> mailNotificationService;

	@Inject
	private UsersMailExtractor usersMailExtractor;

	@Inject
	private ResourceService resourceService;

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
			LOGGER.info("Mail service implementation not found. No mail integertion in script api");
		}
	}

	@Override
	public Map<String, Object> getBindings() {
		if (!enabled) {
			return Collections.emptyMap();
		}
		return Collections.<String, Object> singletonMap("mail", this);
	}

	@Override
	public Collection<String> getScripts() {
		if (!enabled) {
			return Collections.emptyList();
		}
		return ResourceLoadUtil.loadResources(getClass(), MAIL_SCRIPT);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 */
	public void sendNotifications(ScriptInstance rootNode, String subject, String templateId, String[] recipients) {
		// Gives warning and can we just use MailAttachment[] instead of MailAttachment.... ?
		sendNotifications(rootNode, null, subject, templateId, recipients, null, null, (MailAttachment[]) null);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 * @param relevantNodes
	 *            all relevant nodes to be put in data model for latter processing in the freemarker template.
	 */
	public void sendNotifications(ScriptInstance rootNode, String subject, String templateId, String[] recipients, ScriptInstance[] relevantNodes) {
		sendNotifications(rootNode, null, subject, templateId, recipients, relevantNodes);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param user
	 *            user executed the operation (currently logged user).
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 * @param relevantNodes
	 *            all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @param attachments
	 *            the attachments which will be set to the email. Can be path to files, document instances ids or both
	 */
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId, String[] recipients, MailAttachment... attachments) {
		sendNotifications(rootNode, user, subject, templateId, recipients, null, null, attachments);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 *
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param user
	 *            user executed the operation (currently logged user).
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 * @param relevantNodes
	 *            all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @param attachments
	 *            the attachments which will be set to the email. Can be path to files, document instances ids or both
	 */
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId, String[] recipients, ScriptInstance[] relevantNodes, MailAttachment... attachments) {
		sendNotifications(rootNode, user, subject, templateId, recipients, relevantNodes, null, attachments);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 * 
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param user
	 *            user executed the operation (currently logged user).
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 * @param properties
	 *            additional properties that are not contained in the instance or user, and we want to use them in the
	 *            template.
	 * @param attachments
	 *            the attachments which will be set to the email. Can be path to files, document instances ids or both
	 */
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId, String[] recipients, Map<String, Object> properties, MailAttachment... attachments) {
		sendNotifications(rootNode, user, subject, templateId, recipients, null, properties, attachments);
	}

	/**
	 * Send email notifications to all recipients if assignees notification is enabled.
	 * 
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param user
	 *            user executed the operation (currently logged user).
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 * @param relevantNodes
	 *            all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @param properties
	 *            additional properties that are not contained in the instance or user, and we want to use them in the
	 *            template.
	 * @param attachments
	 *            the attachments which will be set to the email. Can be path to files, document instances ids or both
	 */
	public void sendNotifications(ScriptInstance rootNode, User user, String subject, String templateId, String[] recipients, ScriptInstance[] relevantNodes, Map<String, Object> properties, MailAttachment... attachments) {

		Collection<String> recipientsEmails = usersMailExtractor.extractMails(Arrays.asList(recipients), rootNode.getTarget());

		if (recipientsEmails.isEmpty()) {
			LOGGER.error("Could not extract any valid emails form {}", Arrays.asList(recipients));
			return;
		}

		LOGGER.debug("Extracted a total of {} mails. The full list is {}", recipientsEmails.size(), recipientsEmails);
		User loggedUser = getUser(user);

		List<MailNotificationContext> mailNotificationContexts = createMailNotificationContexts(rootNode, loggedUser, subject, templateId, recipientsEmails, relevantNodes, properties, attachments);
		String mailGroupId = UUID.randomUUID().toString();
		for (MailNotificationContext mailNotificationContext : mailNotificationContexts) {
			for (MailNotificationService service : mailNotificationService) {
				service.sendEmail(mailNotificationContext, mailGroupId);
			}
		}
	}

	private User getUser(User user) {
		User loggedUser = user;
		if (loggedUser == null) {
			com.sirma.itt.seip.security.User effectiveUser;
			if (securityContextManager.getCurrentContext().isActive()) {
				effectiveUser = securityContextManager.getCurrentContext().getAuthenticated();
			} else {
				effectiveUser = securityContextManager.getAdminUser();
			}

			return resourceService.getResource(effectiveUser.getSystemId());
		}

		return loggedUser;
	}

	/**
	 * Creates the mail notification contexts.
	 *
	 * @param rootNode
	 *            The root node - specific script node implementation
	 * @param user
	 *            user executed the operation (currently logged user)
	 * @param subject
	 *            the subject of the message to be sent
	 * @param templateId
	 *            The template id - String containing the name of e-mail template to be send
	 * @param recipients
	 *            Array with all recipients. Recipient can be single user, group of users or both. If recipient is user
	 *            the corresponding user name should be entered. If recipient is group of users than the group name
	 *            should be entered.
	 * @param relevantNodes
	 *            all relevant nodes to be put in data model for latter processing in the freemarker template.
	 * @param properties
	 *            additional properties that are not contained in the instance or user, and we want to use them in the
	 *            template.
	 * @return list of prepared {@link MailNotificationContext}
	 */
	private static List<MailNotificationContext> createMailNotificationContexts(ScriptInstance rootNode, User user, String subject, String templateId, Collection<String> recipients, ScriptInstance[] relevantNodes,
			Map<String, Object> properties, MailAttachment[] attachments) {
		List<MailNotificationContext> mailNotificationContexts = new ArrayList<>(recipients.size());

		for (String recipient : recipients) {
			mailNotificationContexts.add(new DefaultNotificationContext(recipient, user, subject, templateId, rootNode, relevantNodes, properties, attachments));
		}

		return mailNotificationContexts;
	}

}
