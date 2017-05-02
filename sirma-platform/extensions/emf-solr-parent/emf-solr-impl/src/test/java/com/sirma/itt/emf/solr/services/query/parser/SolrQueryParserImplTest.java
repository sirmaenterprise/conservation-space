package com.sirma.itt.emf.solr.services.query.parser;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.regex.Pattern;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * The SolrQueryParserImplTest tests the {@link SolrQueryParserImpl}.
 *
 * @author bbanchev
 */
public class SolrQueryParserImplTest {

	@Mock
	SolrSearchConfiguration configuration;
	@InjectMocks
	SolrQueryParserImpl ftsQueryParserImpl;

	/**
	 * Before class construct the parser.
	 */
	@BeforeClass
	public void beforeClass() {
		initMocks(this);
		when(configuration.getFullTextSearchTemplate())
				.thenReturn(new ConfigurationPropertyMock<>("(all:({0}) OR other:({0}))"));
		when(configuration.getFullTextSearchEscapePattern())
				.thenReturn(new ConfigurationPropertyMock<>(Pattern.compile("([\\[\\]\\{\\}\\?\\&\\=\\/\\\\\\:])")));
		when(configuration.getFullTextTokenPreprocessModel())
				.thenReturn(new ConfigurationPropertyMock<>(Collections.emptyList()));
	}

	/**
	 * Test the prepare method.
	 */
	@Test
	public void testPrepare() {
		String preparedQuery = ftsQueryParserImpl.prepare(null);
		Assert.assertEquals(preparedQuery, SolrQueryConstants.QUERY_DEFAULT_EMPTY);

		preparedQuery = ftsQueryParserImpl.prepare("abc");
		Assert.assertEquals(preparedQuery, "(all:(abc) OR other:(abc))");

		preparedQuery = ftsQueryParserImpl.prepare("(\"abc(:]\" AND \"efg\" AND \"hij\")");
		Assert.assertEquals(preparedQuery,
				"((all:(\"abc(:]\") OR other:(\"abc(:]\")) AND (all:(\"efg\") OR other:(\"efg\")) AND (all:(\"hij\") OR other:(\"hij\")))");

		preparedQuery = ftsQueryParserImpl.prepare(" (\"abc\" and \"efg\") or klm");
		Assert.assertEquals(preparedQuery,
				"((all:(\"abc\") OR other:(\"abc\")) AND (all:(\"efg\") OR other:(\"efg\"))) OR (all:(klm) OR other:(klm))");

		preparedQuery = ftsQueryParserImpl.prepare(" (\"abc\" and \\\"efg) or klm");
		Assert.assertEquals(preparedQuery,
				"((all:(\"abc\") OR other:(\"abc\")) AND (all:(\\\"efg) OR other:(\\\"efg))) OR (all:(klm) OR other:(klm))");

		preparedQuery = ftsQueryParserImpl.prepare("abc or asd");
		Assert.assertEquals(preparedQuery, "(all:(abc) OR other:(abc)) OR (all:(asd) OR other:(asd))");

		preparedQuery = ftsQueryParserImpl.prepare("\"abc and abs\" or asd");
		Assert.assertEquals(preparedQuery,
				"(all:(\"abc and abs\") OR other:(\"abc and abs\")) OR (all:(asd) OR other:(asd))");

		preparedQuery = ftsQueryParserImpl.prepare("\"abc and abs\"  or (asd)");
		Assert.assertEquals(preparedQuery,
				"(all:(\"abc and abs\") OR other:(\"abc and abs\"))  OR ((all:(asd) OR other:(asd)))");

		preparedQuery = ftsQueryParserImpl.prepare("\"abc and abs\" or or orr");
		Assert.assertEquals(preparedQuery,
				"(all:(\"abc and abs\") OR other:(\"abc and abs\")) OR OR (all:(orr) OR other:(orr))");

		preparedQuery = ftsQueryParserImpl.prepare("\"abc and abs\" Or nOt orr");
		Assert.assertEquals(preparedQuery,
				"(all:(\"abc and abs\") OR other:(\"abc and abs\")) OR NOT (all:(orr) OR other:(orr))");

		preparedQuery = ftsQueryParserImpl.prepare("(\"abc and abs\" or asd) and efg and not (hij)");
		Assert.assertEquals(preparedQuery,
				"((all:(\"abc and abs\") OR other:(\"abc and abs\")) OR (all:(asd) OR other:(asd))) AND (all:(efg) OR other:(efg)) AND NOT ((all:(hij) OR other:(hij)))");

		preparedQuery = ftsQueryParserImpl.prepare("abc and \"efg\"");
		Assert.assertEquals(preparedQuery, "(all:(abc) OR other:(abc)) AND (all:(\"efg\") OR other:(\"efg\"))");
	}

	/**
	 * Tests the escaping of special Solr symbols.
	 */
	@Test
	public void testEscapeQuery() {
		String preparedQuery = ftsQueryParserImpl.prepare("aBc /");
		Assert.assertEquals(preparedQuery, "(all:(aBc) OR other:(aBc)) (all:(\\/) OR other:(\\/))");
	}

	/**
	 * Verifies that wildcard symbol * is not escaped.
	 */
	@Test
	public void testEscapingOfWildcard() {
		String preparedQuery = ftsQueryParserImpl.prepare("aBc*");
		Assert.assertEquals(preparedQuery, "(all:(aBc*) OR other:(aBc*))");
	}

}
