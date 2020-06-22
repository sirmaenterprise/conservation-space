package com.sirma.itt.seip.concurrent.collections;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.junit.Test;

/**
 * Test for {@link FixedBatchSpliterator}
 *
 * @author BBonev
 */
public class FixedBatchSpliteratorTest {

	@Test(expected = IllegalArgumentException.class)
	public void constructionShouldFailForBatchSizeLessThanOne() throws Exception {
		FixedBatchSpliterator.withBatchSize(Stream.of("item"), 0).count();
	}

	@Test
	public void shouldAllowMinimumBatchSizeOfOne() throws Exception {
		assertEquals(1L, FixedBatchSpliterator.withBatchSize(Stream.of("item"), 1).count());
	}
}
