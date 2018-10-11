package com.sirma.itt.seip.rest.handlers.readers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;

/**
 * Test for the instance message body writer implementation.
 *
 * @author velikov
 */
public class InstanceCollectionBodyReaderTest {

	@InjectMocks
	private InstanceCollectionBodyReader reader;

	@Mock
	private InstanceResourceParser instanceResourceParser;

	@BeforeMethod
	protected void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_isReadable() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Instance.class });
		assertTrue(reader.isReadable(Collection.class, type, null, null));
		assertFalse(reader.isReadable(Collection.class, null, null, null));
		assertTrue(reader.isReadable(List.class, type, null, null));
		assertFalse(reader.isReadable(Instance.class, type, null, null));
	}

	@Test
	public void readFrom_invokes_parser() throws Exception {
		try (InputStream resourceAsStream = getClass()
				.getClassLoader()
					.getResourceAsStream("instance-list-w-properties-and-purpose.json")) {
			reader.readFrom(null, null, null, null, null, resourceAsStream);
			Mockito.verify(instanceResourceParser, Mockito.times(1)).toInstanceList(resourceAsStream);
		}
	}

}
