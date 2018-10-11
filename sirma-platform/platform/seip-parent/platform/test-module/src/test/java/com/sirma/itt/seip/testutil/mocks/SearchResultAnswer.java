package com.sirma.itt.seip.testutil.mocks;

import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.domain.search.SearchArguments;

/**
 * Mockito answer that can be used when the search services search method needs to be mocked.
 *
 * @author yasko
 * @param <T>
 *            Type of the search results.
 */
public class SearchResultAnswer<T> implements Answer<Void> {

	private int total;
	private List<T> results;

	/**
	 * Constructor.
	 *
	 * @param total
	 *            Total number of results to set upon answering.
	 * @param results
	 *            The actual results to set upon answering.
	 */
	public SearchResultAnswer(int total, List<T> results) {
		this.results = results;
		this.total = total;
	}

	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable {

		@SuppressWarnings("unchecked")
		SearchArguments<T> args = (SearchArguments<T>) invocation.getArguments()[1];
		if (args != null) {
			args.setTotalItems(total);
			args.setResult(results);
		}
		return null;
	}

	/**
	 * @return the total
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * @param total
	 *            the total to set
	 */
	public void setTotal(int total) {
		this.total = total;
	}

}
