package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_TOOLTIP;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Test for {@link ForbiddenPropertiesFilter}
 *
 * @author BBonev
 */
public class ForbiddenPropertiesFilterTest {

	@Test
	public void shouldAcceptPropertiesFromTheDownstreamFilter() throws Exception {
		ForbiddenPropertiesFilter filter = new ForbiddenPropertiesFilter(model -> value -> value.equals("testKey"));
		assertTrue(filter.buildFilter(mock(DefinitionModel.class)).test("testKey"));
		assertFalse(filter.buildFilter(mock(DefinitionModel.class)).test("otherKey"));
	}

	@Test
	public void shouldAcceptAllPropertiesIfNoDownStreamIsProvided() throws Exception {
		assertTrue(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test("someKey"));
	}

	@Test
	public void shouldNotAcceptHeaders() throws Exception {
		assertFalse(
				ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test(HEADER_BREADCRUMB));
		assertFalse(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test(HEADER_COMPACT));
		assertFalse(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test(HEADER_DEFAULT));
		assertFalse(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test(HEADER_TOOLTIP));
	}

	@Test
	public void shouldNotAcceptContentFields() throws Exception {
		assertFalse(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test(CONTENT));
		assertFalse(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test("emf:" + CONTENT));
		assertFalse(
				ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test("emf:viewContent"));
	}

	@Test
	public void shouldNotAcceptThumbnail() throws Exception {
		assertFalse(ForbiddenPropertiesFilter.INSTANCE.buildFilter(mock(DefinitionModel.class)).test(THUMBNAIL_IMAGE));
	}
}
