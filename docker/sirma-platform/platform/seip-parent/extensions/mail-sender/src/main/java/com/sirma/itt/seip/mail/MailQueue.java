package com.sirma.itt.seip.mail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.mail.events.MailSendEvent;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.Schedule;

/**
 * Singleton EJB acting as a mail queue.
 *
 * @author Adrian Mitev
 */
@Singleton
@Named(MailQueue.ACTION_NAME)
public class MailQueue {
	static final String ACTION_NAME = "MAIL_QUEUE_EXECUTOR";

	private static final Logger LOGGER = LoggerFactory.getLogger(MailQueue.class);

	@PersistenceContext(unitName = PersistenceUnits.PRIMARY)
	private EntityManager entityManager;

	@Inject
	private MessageSender messageSender;

	@Inject
	private EventService eventService;

	/**
	 * Adds a {@link MailMessage} to the queue by serializing it to xml.
	 *
	 * @param message
	 *            message to enqueue.
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void enqueueMessage(MailMessage message) {
		StringWriter writer = new StringWriter();
		JAXB.marshal(message, writer);
		String xml = writer.toString();

		MailQueueEntry entry = new MailQueueEntry();
		entry.setContent(xml);
		entry.setMailGroupId(message.getMailGroupId());

		entityManager.persist(entry);
	}

	/**
	 * Fetches the first message in the queue (uses order by id) and sends it using {@link MailSender}. Query
	 * serialization is performed using pessimistic locking on the fetched resource.
	 */
	@Startup(async = true)
	@RunAsAllTenantAdmins
	@Schedule(identifier = ACTION_NAME, system = false)
	@ConfigurationPropertyDefinition(name = "mail.sender.activationExpression", defaultValue = "0/10 * * ? * *", sensitive = true, system = true, label = "Cron expression to define the activation interval for mail sending.")
	protected void sendMailFromQueue() {
		if(messageSender.isConfigured()){
			// Ordering by the failed status will make sure that if a mail sending fails, the same mail will only be sent
			// after all other mails are sent
			List<MailQueueEntry> resultList = entityManager
					.createQuery("select m from MailQueueEntry m order by m.status, m.id asc", MailQueueEntry.class)
						.setFirstResult(0)
						.setMaxResults(1)
						.setLockMode(LockModeType.PESSIMISTIC_WRITE)
						.getResultList();
			if (!resultList.isEmpty()) {
				MailQueueEntry entry = resultList.get(0);
				LOGGER.debug("Sending email {} with status {}.", entry.getId(), entry.getStatus());
				MailMessage message = JAXB.unmarshal(new StringReader(entry.getContent()), MailMessage.class);
				// send the email
				try {
					messageSender.sendMessage(message);
					deleteAttchmentsContentOnLastSend(message);
					// remote entry from the queue
					entityManager.remove(entry);
				} catch (MailSendingException e) {
					onEmailSendingFailed(entry);
					// remove the message from the queue if it cannot be send due to wrong recipient.
					if (e.getErrorType() == MailSendingException.MailSendingErrorType.UKNOWN_RECEPIENT
							|| e.getErrorType() == MailSendingException.MailSendingErrorType.INVALID_ADDRESS) {
						LOGGER.warn("Invalid recepient(s): {}", Arrays.toString(message.getRecipients()));
					} else {
						LOGGER.error("Email couldn't be sent: " + e.getMessage(), e);
					}
				} catch (RuntimeException e) {
					onEmailSendingFailed(entry);
					LOGGER.error("Email couldn't be sent: " + e.getMessage(), e);
				}

			}
		}
	}

	private void onEmailSendingFailed(MailQueueEntry entry) {
		entry.setStatus(entry.getStatus() + 1);
		entry.setLastRetry(new Date());
		entityManager.persist(entry);
	}

	private void deleteAttchmentsContentOnLastSend(MailMessage message) {
		if (message.getAttachments() != null && message.getAttachments().length != 0) {
			List<Long> resultList = entityManager
					.createNamedQuery(MailQueueEntry.COUNT_MAILS_BY_MAIL_GROPI_ID_KEY, Long.class)
						.setParameter("id", message.getMailGroupId())
						.getResultList();

			if (!resultList.isEmpty()) {
				long count = resultList.get(0);
				if (count <= 1) {
					eventService.fire(new MailSendEvent(message));
				}
			}
		}
	}

}
