package com.sirma.itt.emf.mail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.patch.PatchDbService;

/**
 * Singleton EJB acting as a mail queue.
 * 
 * @author Adrian Mitev
 */
@Singleton
@Startup
@Lock(LockType.READ)
@DependsOn(value = PatchDbService.SERVICE_NAME)
public class MailQueue {

	private Logger LOG = Logger.getLogger(MailQueue.class);

	@PersistenceContext(unitName = DbDao.PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

	@Inject
	private MessageSender messageSender;

	/**
	 * Adds a {@link MailMessage} to the queue by serializing it to xml.
	 * 
	 * @param message
	 *            message to enqueue.
	 */
	public void enqueueMessage(MailMessage message) {
		StringWriter writer = new StringWriter();
		JAXB.marshal(message, writer);
		String xml = writer.toString();

		MailQueueEntry entry = new MailQueueEntry();
		entry.setContent(xml);

		entityManager.persist(entry);
	}

	/**
	 * Fetches the first message in the queue (uses order by id) and sends it using
	 * {@link MailSender}. Query serialization is performed using pessimistic locking on the fetched
	 * resource.
	 */
	@Schedule(second = "*/10", minute = "*", hour = "*", info = "Every 10 seconds", persistent = false)
	@SuppressWarnings("unchecked")
	protected void sendMailFromQueue() {
		List<MailQueueEntry> resultList = entityManager
				.createQuery("select m from MailQueueEntry m order by m.id asc").setFirstResult(0)
				.setMaxResults(1).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
		if (!resultList.isEmpty()) {
			MailQueueEntry entry = resultList.get(0);
			MailMessage message = JAXB.unmarshal(new StringReader(entry.getContent()),
					MailMessage.class);
			// send the email
			try {
				messageSender.sendMessage(message);
			} catch (MailSendingException e) {
				// remove the message from the queue if it cannot be send due to wrong recipient.
				if ((e.getErrorType() == MailSendingException.MailSendingErrorType.UKNOWN_RECEPIENT)
						|| (e.getErrorType() == MailSendingException.MailSendingErrorType.INVALID_ADDRESS)) {
					LOG.warn("Invalid recepient(s): " + Arrays.toString(message.getRecipients()));
				} else {
					throw e;
				}
			}
			// remote entry from the queue
			entityManager.remove(entry);
		}
	}
}
