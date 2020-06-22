package com.sirma.itt.seip.testutil.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test for {@link RandomPortGenerator}
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class RandomPortGeneratorTest {

	@Test
	public void shouldReturnPortsInTheGivenRange() throws Exception {
		for (int i = 0; i < 100; i++) {
			int port = RandomPortGenerator.generatePort(8900, 9000);
			assertTrue(port + " not in expected range of [8900, 9000]", port >= 8900 && port <= 9000);
		}
	}

}
