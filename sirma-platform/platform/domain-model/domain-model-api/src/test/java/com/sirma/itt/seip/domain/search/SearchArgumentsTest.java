package com.sirma.itt.seip.domain.search;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Tests for {@link SearchArguments}.
 *
 * @author Adrian Mitev
 */
public class SearchArgumentsTest {

	@Test
	public void getQueryTimeoutShouldProperlyConvertTimeUnits() {
		SearchArguments<String> arguments = new SearchArguments<>();

		arguments.setQueryTimeout(TimeUnit.SECONDS, 60);

		assertEquals(60, arguments.getQueryTimeout(TimeUnit.SECONDS));

		assertEquals(60_000, arguments.getQueryTimeout(TimeUnit.MILLISECONDS));

		assertEquals(1, arguments.getQueryTimeout(TimeUnit.MINUTES));
	}

}
