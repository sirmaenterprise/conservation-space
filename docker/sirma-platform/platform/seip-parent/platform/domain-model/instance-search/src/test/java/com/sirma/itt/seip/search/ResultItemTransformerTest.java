package com.sirma.itt.seip.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Test;

/**
 * Test for {@link ResultItemTransformer}
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/07/2017
 */
public class ResultItemTransformerTest {
	@Test
	public void asIs_shouldPassValuesAsIs() throws Exception {
		ResultItem item = mock(ResultItem.class);
		ResultItem result = ResultItemTransformer.asIs().transform(item);
		assertEquals(result, item);
	}

	@Test
	public void asSingleValue_shouldMapToSingleValueFromEachItem() throws Exception {
		ResultItem item = mock(ResultItem.class);
		when(item.getResultValue("property")).thenReturn("value");
		when(item.getResultValue("otherProperty")).thenReturn("otherValue");
		Serializable result = ResultItemTransformer.asSingleValue("property").transform(item);
		assertEquals("value", result);
		result = ResultItemTransformer.asSingleValue("otherProperty").transform(item);
		assertEquals("otherValue", result);
	}

}
