/**
 *
 */
package com.sirma.itt.seip.mapping.dozer;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * @author BBonev
 *
 */
public class DozerObjectMapperProducerTest {

	@InjectMocks
	DozerObjectMapperProducer producer;

	@Spy
	List<DozerMapperMappingProvider> extensions = new ArrayList<>();

	@Mock
	DozerMapperMappingProvider extension;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		extensions.clear();
		extensions.add(extension);
	}

	@Test
	public void test_produce() {
		when(extension.getMappingUries())
				.thenReturn(Arrays.asList("com/sirma/itt/seip/mapping/dozer/testDozerMapping.xml"));

		ObjectMapper mapper = producer.getMapper();
		assertNotNull(mapper);

		TestMappingClass1 source = new TestMappingClass1();
		source.setField1("value");
		TestMappingClass2 destination = mapper.map(source, TestMappingClass2.class);

		assertNotNull(destination);

		assertEquals(destination.getField1(), source.getField1());

		producer.reset();

		producer.onShutdown();
	}
}
