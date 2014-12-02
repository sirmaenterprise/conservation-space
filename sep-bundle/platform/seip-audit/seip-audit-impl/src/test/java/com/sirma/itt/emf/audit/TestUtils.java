/**
 * 
 */
package com.sirma.itt.emf.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.processor.AuditProcessor;

import de.akquinet.jbosscc.needle.db.transaction.TransactionHelper;
import de.akquinet.jbosscc.needle.db.transaction.VoidRunnable;
import de.akquinet.jbosscc.needle.junit.DatabaseRule;

/**
 * Class containing common methods used in several tests.
 * 
 * @author Mihail Radkov
 */
public final class TestUtils {

	/**
	 * Private constructor for utility class.
	 */
	private TestUtils() {
	}

	/**
	 * Checks if a file pointed by the provided path exists and if so deletes it.
	 * 
	 * @param path
	 *            the provided path
	 */
	public static void deleteFile(String path) {
		try {
			Files.deleteIfExists(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Invokes {@link AuditProcessor#processActivity(AuditActivity)} in a transaction provided by
	 * {@link DatabaseRule}.
	 * 
	 * @param rule
	 *            the database rule
	 * @param processor
	 *            the processor
	 * @param activity
	 *            the audit activity to be processed
	 * @throws Exception
	 *             if while operating with the database a problem occurs
	 */
	public static void executeInTransaction(DatabaseRule rule, final AuditProcessor processor,
			final AuditActivity activity) throws Exception {
		TransactionHelper helper = rule.getTransactionHelper();
		helper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				processor.processActivity(activity);
			}
		});
	}

	/**
	 * Selects all {@link AuditActivity} from provided {@link EntityManager}.
	 * 
	 * @param em
	 *            the provided {@link EntityManager}
	 * @return {@link List} of {@link AuditActivity}
	 */
	public static List<AuditActivity> getAllActivities(EntityManager em) {
		TypedQuery<AuditActivity> query = em.createQuery("from AuditActivity aa",
				AuditActivity.class);
		return query.getResultList();
	}

}
