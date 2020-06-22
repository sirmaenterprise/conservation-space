package com.sirma.itt.seip.instance.version.compare;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Test;

import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Test for {@link VersionCompareConfigurationsImpl}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class VersionCompareConfigurationsImplTest {

	@Test
	public void buildServiceUrl_test() {
		GroupConverterContext context = mock(GroupConverterContext.class);
		when(context.get("compare.service.protocol")).thenReturn("http");
		when(context.get("compare.service.host")).thenReturn("localhost");
		when(context.get("compare.service.port")).thenReturn(8125);
		URI serviceUrl = VersionCompareConfigurationsImpl.buildServiceUrl(context);
		assertEquals("http://localhost:8125/compare", serviceUrl.toString());
	}

}
