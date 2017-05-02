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
	 * Selects all {@link AuditActivity} from provided {@link EntityManager}.
	 *
	 * @param em
	 *            the provided {@link EntityManager}
	 * @return {@link List} of {@link AuditActivity}
	 */
	public static List<AuditActivity> getAllActivities(EntityManager em) {
		TypedQuery<AuditActivity> query = em.createQuery("from AuditActivity aa", AuditActivity.class);
		return query.getResultList();
	}

}
