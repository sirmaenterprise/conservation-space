package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test {@link BatchRequest}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/07/2017
 */
public class BatchRequestTest {

	@Test
	public void getProperties_shouldLazyInitializeProperties() throws Exception {
		BatchRequest request = new BatchRequest();
		assertTrue(request.getProperties() == request.getProperties());
	}

}
