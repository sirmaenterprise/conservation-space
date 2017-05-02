package com.sirma.itt.seip;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

/**
 * Test for {@link Sealable}
 *
 * @author BBonev
 */
public class SealableTest {

	@Test
	public void seal() throws Exception {
		assertNotNull(Sealable.seal(new Object()));

		Sealable sealable = mock(Sealable.class);

		assertNotNull(Sealable.seal(sealable));
		verify(sealable).seal();
	}
}
