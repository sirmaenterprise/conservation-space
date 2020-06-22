/**
 *
 */
package com.sirma.itt.seip.mapping.dozer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.dozer.Mapper;
import org.dozer.MappingException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.mapping.ObjectMappingException;

/**
 * @author BBonev
 *
 */
public class DozerObjectMapperTest {

	@Mock
	Mapper mock;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_map_to_class() {
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), Object.class);
		verify(mock).map(any(Object.class), eq(Object.class));
	}

	@Test
	public void test_map_to_object() {
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), new Object());
		verify(mock).map(any(Object.class), any(Object.class));
	}

	@Test
	public void test_map_to_class_and_mapId() {
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), Object.class, "mapId");
		verify(mock).map(any(Object.class), eq(Object.class), eq("mapId"));
	}

	@Test
	public void test_map_to_object_and_mapId() {
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), new Object(), "mapId");
		verify(mock).map(any(Object.class), any(Object.class), eq("mapId"));
	}

	@Test
	public void test_init_from_Supplier() {
		DozerObjectMapper mapper = new DozerObjectMapper(() -> mock);
		mapper.map(new Object(), Object.class);
		verify(mock).map(any(Object.class), eq(Object.class));
	}

	@Test(expectedExceptions = ObjectMappingException.class)
	public void test_map_to_class_with_exception() {
		when(mock.map(any(Object.class), eq(Object.class))).thenThrow(new MappingException(""));
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), Object.class);
	}

	@Test(expectedExceptions = ObjectMappingException.class)
	public void test_map_to_object_with_exception() {
		doThrow(new MappingException("")).when(mock).map(any(Object.class), any(Object.class));
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), new Object());
	}

	@Test(expectedExceptions = ObjectMappingException.class)
	public void test_map_to_class_and_mapId_with_exception() {
		when(mock.map(any(Object.class), eq(Object.class), eq("mapId"))).thenThrow(new MappingException(""));
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), Object.class, "mapId");
	}

	@Test(expectedExceptions = ObjectMappingException.class)
	public void test_map_to_object_and_mapId_with_exception() {
		doThrow(new MappingException("")).when(mock).map(any(Object.class), any(Object.class), eq("mapId"));
		DozerObjectMapper mapper = new DozerObjectMapper(mock);
		mapper.map(new Object(), new Object(), "mapId");
	}
}
