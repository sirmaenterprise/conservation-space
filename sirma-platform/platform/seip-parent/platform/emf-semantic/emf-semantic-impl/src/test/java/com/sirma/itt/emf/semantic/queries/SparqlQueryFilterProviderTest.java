package com.sirma.itt.emf.semantic.queries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.sirma.itt.seip.search.NamedQueries;

/**
 * Tests for {@link SparqlQueryFilterProvider}
 *
 * @author BBonev
 */
public class SparqlQueryFilterProviderTest {

	private SparqlQueryFilterProvider filterProvider = new SparqlQueryFilterProvider();

	@Test
	public void getFilter() throws Exception {
		assertNotNull(filterProvider.getFilterBuilder(NamedQueries.Filters.IS_DELETED));
		assertNull(filterProvider.getFilterBuilder("notRegistredFilter"));
	}

	@Test
	public void getFilters() throws Exception {
		List<Function<String, String>> builders = filterProvider.getFilterBuilders(NamedQueries.Filters.IS_DELETED);
		assertNotNull(builders);
		assertFalse(builders.isEmpty());

		builders = filterProvider.getFilterBuilders((String) null);
		assertNotNull(builders);
		assertTrue(builders.isEmpty());

		builders = filterProvider.getFilterBuilders((String[]) null);
		assertNotNull(builders);
		assertTrue(builders.isEmpty());

		builders = filterProvider.getFilterBuilders("notRegistredFilter");
		assertNotNull(builders);
		assertTrue(builders.isEmpty());

		builders = filterProvider.getFilterBuilders(new String[0]);
		assertNotNull(builders);
		assertTrue(builders.isEmpty());
	}

	@Test
	public void testFilter_isDeleted() throws Exception {
		String filter = filterProvider.getFilterBuilder(NamedQueries.Filters.IS_DELETED).apply("?instance");
		assertTrue(filter.contains("?instance"));
		assertTrue(filter.contains("emf:isDeleted"));
		assertTrue(filter.contains("\"true\"^^xsd:boolean"));
	}

	@Test
	public void testFilter_isNotDeleted() throws Exception {
		String filter = filterProvider.getFilterBuilder(NamedQueries.Filters.IS_NOT_DELETED).apply("?instance");
		assertTrue(filter.contains("?instance"));
		assertTrue(filter.contains("emf:isDeleted"));
		assertTrue(filter.contains("\"false\"^^xsd:boolean"));
	}

	@Test
	public void testFilter_isRevision() throws Exception {
		String filter = filterProvider.getFilterBuilder(NamedQueries.Filters.IS_REVISION).apply("?instance");
		assertTrue(filter.contains("?instance"));
		assertTrue(filter.contains("emf:revisionType"));
		assertTrue(filter.contains("emf:revision"));
	}

	@Test
	public void testFilter_isNotRevision() throws Exception {
		String filter = filterProvider.getFilterBuilder(NamedQueries.Filters.IS_NOT_REVISION).apply("?instance");
		assertTrue(filter.contains("?instance"));
		assertTrue(filter.contains("emf:revisionType"));
		assertTrue(filter.contains("emf:revision"));
		assertTrue(filter.contains("?check"));
	}
}
