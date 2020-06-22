package com.sirma.sep.export.xlsx;

import static org.junit.Assert.assertNotNull;

import javax.json.Json;

import org.junit.Test;

/**
 * Test for {@link SearchData}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class SearchDataTest {

	@Test
	public void getSearchCriteria() {
		SearchData searchData = new SearchData(Json.createObjectBuilder().build(), "property", "asc");
		assertNotNull(searchData.getSearchCriteria());
	}

	@Test
	public void getOrderBy() {
		SearchData searchData = new SearchData(Json.createObjectBuilder().build(), "property", "asc");
		assertNotNull(searchData.getOrderBy());
	}

	@Test
	public void getOrderDirection() {
		SearchData searchData = new SearchData(Json.createObjectBuilder().build(), "property", "asc");
		assertNotNull(searchData.getOrderDirection());
	}

}
