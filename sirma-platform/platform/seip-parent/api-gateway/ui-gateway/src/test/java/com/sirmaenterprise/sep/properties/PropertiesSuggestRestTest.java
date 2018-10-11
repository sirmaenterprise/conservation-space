package com.sirmaenterprise.sep.properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.properties.PropertiesSuggestService;

/**
 * Tests for {@link PropertiesSuggestRest}.
 *
 * @author smustafov
 * @author svetlozar.iliev
 */
public class PropertiesSuggestRestTest {

	@Mock
	private PropertiesSuggestService propertiesSuggestService;

	@InjectMocks
	private PropertiesSuggestRest propertiesRest;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests that {@link PropertiesSuggestService} receives valid & unaltered parameters.
	 */
	@Test
	public void shouldCallSuggestPropertiesWithCorrectParameters() {
		propertiesRest.suggest("context", "type", true);
		Mockito.verify(propertiesSuggestService, Mockito.times(1)).suggestPropertiesIds(Matchers.eq("context"),
				Matchers.eq("type"), Matchers.eq(Boolean.TRUE));
	}
}
