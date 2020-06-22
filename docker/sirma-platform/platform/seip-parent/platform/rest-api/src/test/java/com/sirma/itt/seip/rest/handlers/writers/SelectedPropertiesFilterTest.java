package com.sirma.itt.seip.rest.handlers.writers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.Test;

import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test fot {@link SelectedPropertiesFilter}
 *
 * @author BBonev
 */
public class SelectedPropertiesFilterTest {

	@Test
	public void shouldFilterProvidedProperties() throws Exception {
		DefinitionMock model = new DefinitionMock();
		PropertyDefinitionMock property1 = new PropertyDefinitionMock();
		property1.setName("property1");
		PropertyDefinitionMock property2 = new PropertyDefinitionMock();
		property2.setName("property2");
		model.getFields().add(property1);
		model.getFields().add(property2);

		SelectedPropertiesFilter filter = new SelectedPropertiesFilter(Arrays.asList("property1"));
		Predicate<String> predicate = filter.buildFilter(model);
		assertTrue(predicate.test("property1"));
		assertFalse(predicate.test("property2"));
	}
}
