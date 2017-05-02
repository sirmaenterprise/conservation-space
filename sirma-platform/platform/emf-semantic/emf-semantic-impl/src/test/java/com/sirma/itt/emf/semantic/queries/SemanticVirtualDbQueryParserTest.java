package com.sirma.itt.emf.semantic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.search.NamedQueries;

/**
 * Test for {@link SemanticVirtualDbQueryParser}
 *
 * @author BBonev
 */
public class SemanticVirtualDbQueryParserTest {

	SemanticVirtualDbQueryParser parser = new SemanticVirtualDbQueryParser();

	@Test
	public void parseNamed() throws Exception {
		Optional<Collection<Object>> parsed = parser.parseNamed(NamedQueries.LOAD_PROPERTIES, Collections.emptyList());
		assertNotNull(parsed);
		assertFalse(parsed.isPresent());

		parsed = parser.parseNamed(NamedQueries.SELECT_BY_IDS, Collections.emptyList());
		assertNotNull(parsed);
		assertFalse(parsed.isPresent());

		parsed = parser.parseNamed(NamedQueries.SELECT_BY_IDS,
				Arrays.asList(new Pair<>(NamedQueries.Params.URIS, Arrays.asList("1"))));
		assertNotNull(parsed);
		assertTrue(parsed.isPresent());
		assertEquals(Arrays.asList("1"), parsed.get());
	}

	@Test
	public void parse() throws Exception {
		Optional<Collection<Object>> optional = parser.parse("", Collections.emptyList());
		assertNotNull(optional);
		assertFalse(optional.isPresent());
	}
}
