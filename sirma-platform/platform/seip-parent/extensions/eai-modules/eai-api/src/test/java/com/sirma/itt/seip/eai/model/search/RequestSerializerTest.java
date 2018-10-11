package com.sirma.itt.seip.eai.model.search;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.itt.seip.eai.mock.MockProvider;
import com.sirma.itt.seip.eai.model.request.composed.Configuration;
import com.sirma.itt.seip.eai.model.request.composed.Entity;
import com.sirma.itt.seip.eai.model.request.query.RawQuery;
import com.sirma.itt.seip.rest.mapper.MapperProvider;

/**
 * Test serialize/deserialize
 */
public class RequestSerializerTest {

	/**
	 * Test read and write back a configuration
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testReadWriteConfiguration() throws Exception {
		MapperProvider mapperProvider = MockProvider.getMapperProvider();
		ObjectMapper objectMapper = mapperProvider.provideObjectMapper();
		String entity = "[{ 		\"name\": \"culturalObject\", 		\"properties\": [{ 			\"name\": \"dbId\", 			\"mandatory\": true, 			\"type\": \"string\", 			\"multivalue\": false 		}, { 			\"name\": \"title\", 			\"mandatory\": false, 			\"type\": \"string\", 			\"multivalue\": false 		}, { 			\"name\": \"accnumber\", 			\"mandatory\": true, 			\"type\": \"string\", 			\"multivalue\": false 		}, { 			\"name\": \"templateId\", 			\"mandatory\": true, 			\"type\": \"string\", 			\"multivalue\": false 		}], 		\"relations\": [{ 			\"name\": \"child\", 			\"mandatory\": false, 			\"multivalue\": true 		}, { 			\"name\": \"parent\", 			\"mandatory\": false, 			\"multivalue\": true 		}] 	}]";
		List<Entity> entities = objectMapper.readValue(entity,
				objectMapper.getTypeFactory().constructCollectionType(List.class, Entity.class));
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals("culturalObject", entities.get(0).getName());
		String config = "{ 	\"uid\": \"dbId\", 	\"entities\": [{ 		\"name\": \"culturalObject\", 		\"properties\": [{ 			\"name\": \"dbId\", 			\"mandatory\": true, 			\"type\": \"string\", 			\"multivalue\": false 		}, { 			\"name\": \"title\", 			\"mandatory\": false, 			\"type\": \"string\", 			\"multivalue\": false 		}, { 			\"name\": \"accnumber\", 			\"mandatory\": true, 			\"type\": \"string\", 			\"multivalue\": false 		}, { 			\"name\": \"templateId\", 			\"mandatory\": true, 			\"type\": \"string\", 			\"multivalue\": false 		}], 		\"relations\": [{ 			\"name\": \"child\", 			\"mandatory\": false, 			\"multivalue\": true 		}, { 			\"name\": \"parent\", 			\"mandatory\": false, 			\"multivalue\": true 		}] 	}], 	\"paging\": { 		\"limit\": 25, 		\"skip\": 50 	}, 	\"ordering\": [{ 		\"asc\": false, 		\"by\": \"accnumber\" 	}] }";
		Configuration configration = objectMapper.readValue(config, Configuration.class);
		Assert.assertEquals(1, configration.getEntities().size());

	}

	/**
	 * Test read and write back a sample query
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testReadWriteRawQuery() throws Exception {
		MapperProvider mapperProvider = MockProvider.getMapperProvider();
		ObjectMapper objectMapper = mapperProvider.provideObjectMapper();

		String raw1 = "[        {          \"property\":\"creator\",          \"type\":\"string\",          \"operator\":\"CONTAINS\",          \"values\":[             \"Rembrandt\"          ]       },       {          \"operator\":\"AND\"       },       [          {             \"property\":\"localId\",             \"type\":\"string\",             \"operator\":\"EQUALS\",             \"values\":[                \"id1\"             ]          },          {             \"operator\":\"OR\"          },          {             \"property\":\"localId\",             \"type\":\"string\",             \"operator\":\"EQUALS\",             \"values\":[                \"id2\"             ]          }       ],       {          \"operator\":\"AND\"       },       {          \"property\":\"createdOn\",          \"operator\":\"BETWEEN\",          \"type\":\"datetime\",          \"values\":[             \"1635-12-31T22:00:00.000Z\",             \"1655-12-31T22:00:00.000Z\"          ]       }    ] ";
		RawQuery readValue = objectMapper.readValue(raw1, RawQuery.class);
		Assert.assertEquals(5, readValue.getEntries().size());
		String raw2 = "[   [     {          \"property\":\"creator\",          \"type\":\"string\",          \"operator\":\"CONTAINS\",          \"values\":[             \"Rembrandt\"          ]       },       {          \"operator\":\"AND\"       },       [          {             \"property\":\"localId\",             \"type\":\"string\",             \"operator\":\"EQUALS\",             \"values\":[                \"id1\"             ]          },          {             \"operator\":\"OR\"          },          {             \"property\":\"localId\",             \"type\":\"string\",             \"operator\":\"EQUALS\",             \"values\":[                \"id2\"             ]          }       ],       {          \"operator\":\"AND\"       },       {          \"property\":\"createdOn\",          \"operator\":\"BETWEEN\",          \"type\":\"datetime\",          \"values\":[             \"1635-12-31T22:00:00.000Z\",             \"1655-12-31T22:00:00.000Z\"          ]       }   ] ] ";
		readValue = objectMapper.readValue(raw2, RawQuery.class);
		Assert.assertEquals(1, readValue.getEntries().size());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		objectMapper.writeValue(out, readValue);
		String serialized = new String(out.toByteArray());
		assertJsonEquals(raw2, serialized);
	}
}
