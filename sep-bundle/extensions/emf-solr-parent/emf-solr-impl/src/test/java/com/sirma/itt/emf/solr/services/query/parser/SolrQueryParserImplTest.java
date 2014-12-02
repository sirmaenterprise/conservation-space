package com.sirma.itt.emf.solr.services.query.parser;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.solr.services.query.parser.SolrQueryParserImpl;

/**
 * The SolrQueryParserImplTest tests the {@link SolrQueryParserImpl}.
 *
 * @author bbanchev
 */
public class SolrQueryParserImplTest {

	/** The fts query parser impl. */
	private static SolrQueryParserImpl ftsQueryParserImpl;

	/**
	 * Before class construct the parser.
	 */
	@BeforeClass
	public void beforeClass() {
		ftsQueryParserImpl = new SolrQueryParserImpl();
		ReflectionUtils.setField(ftsQueryParserImpl, "template", "(all:({0}) OR other:({0}))");
		ReflectionUtils.setField(ftsQueryParserImpl, "escapeRegex", "([:\\[\\]])");
		ftsQueryParserImpl.init();
	}

	/**
	 * Test the prepare method.
	 */
	@Test
	public void testPrepare() {
		String preparedQuery = ftsQueryParserImpl.prepare("abc");
		Assert.assertEquals(preparedQuery, "(all:(abc) OR other:(abc))");

		preparedQuery = ftsQueryParserImpl.prepare("(\"abc(:]\" AND \"efg\" AND \"hij\")");
		Assert.assertEquals(
				preparedQuery,
				"((all:(\"abc(\\:\\]\") OR other:(\"abc(\\:\\]\")) AND (all:(\"efg\") OR other:(\"efg\")) AND (all:(\"hij\") OR other:(\"hij\")))");

		preparedQuery = ftsQueryParserImpl.prepare(" (\"abc\" and \"efg\") or klm");
		Assert.assertEquals(
				preparedQuery,
				"((all:(\"abc\") OR other:(\"abc\")) AND (all:(\"efg\") OR other:(\"efg\"))) OR (all:(klm) OR other:(klm))");

		preparedQuery = ftsQueryParserImpl.prepare(" (\"abc\" and \\\"efg) or klm");
		Assert.assertEquals(
				preparedQuery,
				"((all:(\"abc\") OR other:(\"abc\")) AND (all:(\\\"efg) OR other:(\\\"efg))) OR (all:(klm) OR other:(klm))");

		preparedQuery = ftsQueryParserImpl.prepare("abc or asd");
		Assert.assertEquals(preparedQuery,
				"(all:(abc) OR other:(abc)) OR (all:(asd) OR other:(asd))");

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

		preparedQuery = ftsQueryParserImpl
				.prepare("(\"abc and abs\" or asd) and efg and not (hij)");
		Assert.assertEquals(
				preparedQuery,
				"((all:(\"abc and abs\") OR other:(\"abc and abs\")) OR (all:(asd) OR other:(asd))) AND (all:(efg) OR other:(efg)) AND NOT ((all:(hij) OR other:(hij)))");

		preparedQuery = ftsQueryParserImpl.prepare("abc and \"efg\"");
		Assert.assertEquals(preparedQuery,
				"(all:(abc) OR other:(abc)) AND (all:(\"efg\") OR other:(\"efg\"))");
	}

}
