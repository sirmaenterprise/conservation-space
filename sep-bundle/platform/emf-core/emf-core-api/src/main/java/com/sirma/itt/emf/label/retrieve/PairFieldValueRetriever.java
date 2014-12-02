package com.sirma.itt.emf.label.retrieve;

import java.util.List;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * Abstract class containing common logic for field value retrievers that need to be
 * internationalized and return pairs of data.
 * 
 * @author nvelkov
 */
public abstract class PairFieldValueRetriever implements FieldValueRetriever {

	@Inject
	private Instance<AuthenticationService> authenticationService;

	/**
	 * Gets the current user language.
	 * 
	 * @return the current user language
	 */
	protected String getCurrentUserLanguage() {
		try {
			AuthenticationService service = authenticationService.get();
			return SecurityContextManager.getUserLanguage(service.getCurrentUser());
		} catch (ContextNotActiveException e) {
			return SecurityContextManager.getSystemLanguage();
		}
	}

	/**
	 * Validate and create a pair from the first and second elements. The second element should
	 * start with the filter, the offset should be lower than the total, which should be lower than
	 * the limit (done for paging purposes).
	 * 
	 * @param results
	 *            the results to which the pair will be aded
	 * @param first
	 *            the first element of the pair
	 * @param second
	 *            the second element of the pair
	 * @param filter
	 *            the filter, the second element should start with
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @param total
	 *            the total
	 */
	protected void validateAndAddPair(List<Pair<String, String>> results, String first,
			String second, String filter, Integer offset, Integer limit, Long total) {
		if (offset <= total && (limit == null || results.size() < limit)) {
			results.add(new Pair<String, String>(first, second));
		}
	}
}
