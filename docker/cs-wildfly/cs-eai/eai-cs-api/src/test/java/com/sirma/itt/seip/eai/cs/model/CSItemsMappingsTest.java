package com.sirma.itt.seip.eai.cs.model;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.itt.seip.eai.cs.model.response.CSItemsSetResponse;
import com.sirma.itt.seip.eai.mock.MockProvider;

/**
 * Test java<->json mapping.
 *
 * @author bbanchev
 */
public class CSItemsMappingsTest {

	/**
	 * Test parsing of {@link CSItemsSetResponse}.
	 *
	 * @throws Exception
	 *             on any error
	 */
	@Test
	public void testCSItemsSetResponseParsing() throws Exception {
		String source = "{ 	\"items\": [{ 		\"url\": \"https://data.nga.gov/art/objects/1138.json\", 		\"description\": \"\\nThe Feast of the Gods; Giovanni Bellini and Titian; 1514/1529\", 		\"record\": { 			\"namespace\": \"cultObj\", 			\"id\": \"1138\", 			\"classification\": \"painting\", 			\"title\": \"The Feast of the Gods\", 			\"number\": \"1942.9.1\", 			\"references\": [{ 				\"predicate\": \"parent\", 				\"object\": { 					\"namespace\": \"cultObj\", 					\"id\": \"1\", 					\"classification\": \"painting\", 					\"title\": \"The Feast of the Gods\", 					\"number\": \"1942.9.1\" 				} 			}] 		}, 		\"thumbnail\": \"https://vm-imgrepo-tdp.nga.gov/public/objects/1/1/3/8/1138-primary-0-90x90.jpg\" 	}], 	\"paging\": { 		\"limit\": 1, 		\"skip\": 50, 		\"total\": 176 	} }";

		ObjectMapper provideObjectMapper = MockProvider.getMapperProvider().provideObjectMapper();
		CSItemsSetResponse readValue = provideObjectMapper.readValue(source, CSItemsSetResponse.class);
		Assert.assertEquals(1, readValue.getItems().size());
		Assert.assertEquals("\nThe Feast of the Gods; Giovanni Bellini and Titian; 1514/1529",
				readValue.getItems().get(0).getDescription());
		Assert.assertEquals("painting", readValue.getItems().get(0).getRecord().getClassification());
		Assert.assertEquals(3, readValue.getItems().get(0).getRecord().getProperties().size());
		Assert.assertEquals(1, readValue.getItems().get(0).getRecord().getRelationships().size());
		Assert.assertEquals(1, readValue.getPaging().getLimit());
		Assert.assertEquals(50, readValue.getPaging().getSkip());
		Assert.assertEquals(176, readValue.getPaging().getTotal());
	}

	/**
	 * Test newline parsing.
	 *
	 * @throws Exception
	 *             on any error
	 */
	@Test
	public void testNewline() throws Exception {
		String source = "{	\"record\": {		\"namespace\": \"cultObj \\n test\"} }";
		ObjectMapper provideObjectMapper = MockProvider.getMapperProvider().provideObjectMapper();
		CSItemRecord readValue = provideObjectMapper.readValue(source, CSItemRecord.class);
		Assert.assertNotNull(readValue);

	}

	/**
	 * Test {@link CSResultItem} parse.
	 *
	 * @throws Exception
	 *             on any error
	 */
	@Test
	public void testCSResultItem() throws Exception {

		String source = "{	\"record\": {		\"namespace\": \"cultObj  test\",		\"id\": 1138,		\"classification\": \"painting\",		\"title\": \"The Feast of the Gods\",		\"artistNameList\": [\"Bellini, Giovanni\",		\"Titian\"],		\"attribution\": \"Giovanni Bellini and Titian\",		\"accessionNumber\": \"1942.9 .1\",		\"bibliographyList\": [\"Hartshorne, Rev.C.H.A Guide to Alnwick Castle.London, 1865: 62.\",		\"<i> Paintings in the Collection of Joseph Widener at Lynnewood Hall. < /i> Intro. by Wilhelm R. Valentiner. Elkins Park, Pennsylvania, 1923: unpaginated, repro., as by Giovanni Bellini.\"],		\"displayDate\": \"1514 / 1529\",		\"creditLine\": \"Widener Collection\",		\"description\": \"The Feast of the Gods, Giovanni Bellini and Titian, 1514 / 1529\",		\"location\": \"WB - M12\",		\"homeLocation\": \"WB - M12\",		\"inscription\": \"lower right on wooden tub: joannes bellinus venetus / p MDXIIII\",		\"medium\": \"oil on canvas\",		\"departmentAbbr\": \"DCRS\",		\"dimensions\": \"overall: 170.2 x 188 cm(67 x 74 in .)  framed: 203.8 x 218.4 x 7.6 cm(80 1 / 4 x 86 x 3 in .)\",		\"provenance\": \"Probably commissioned by Alfonso I d 'Este, Duke of Ferrara [d. 1534);[1] by inheritance to his son, Ercole II d'Este, Duke of Ferrara[d .1559];by inheritance to his son, Alfonso II d 'Este, Duke of Ferrara [d. 1597]; by inheritance to his cousin, Cesare d' Este...\",		\"references\": [{			\"predicate\": \"hasPrimaryDepiction\",			\"object\": {				\"namespace\": \"image\",				\"id\": \"d63a8ffd-bdac-498c-b861-a53e11989cef\"			}		},		{			\"predicate\": \"hasChild\",			\"object\": {				\"namespace\": \"cultObj\",				\"id\": \"1234\",				\"title\": \"\",				\"classification\": \"frame\"			}		}]	}}";
		ObjectMapper provideObjectMapper = MockProvider.getMapperProvider().provideObjectMapper();
		CSResultItem readValue = provideObjectMapper.readValue(source, CSResultItem.class);
		Assert.assertEquals(16, readValue.getRecord().getProperties().size());
		Assert.assertEquals("painting", readValue.getRecord().getClassification());
	}
}
