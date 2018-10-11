/**
 *
 */
package com.sirma.itt.seip.serialization;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.sirma.itt.seip.serialization.kryo.KryoHelper;

/**
 * @author BBonev
 *
 */
public class SerializationHelperTest {

	@Mock
	KryoHelper kryoHelper;
	@Mock
	KryoPool kryoPool;
	@Mock
	SerializationEngine serializationEngine;

	@InjectMocks
	SerializationHelper serializationHelper;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(kryoHelper.getClonePool()).thenReturn(kryoPool);
		when(kryoHelper.getSerializationPool()).thenReturn(kryoPool);

		when(kryoHelper.createEngine()).thenReturn(serializationEngine);
		when(kryoHelper.getPooled()).thenReturn(serializationEngine);
	}

	@Test
	public void test_copy() {
		serializationHelper.copy(new ArrayList<>(Arrays.asList("1", "2")));
		verify(kryoHelper).getClonePool();
		verify(kryoPool).run(any());
	}

	@Test
	public void test_serialize_defaultEngine() {
		serializationHelper.serialize("value");
		verify(kryoHelper).getPooled();
		verify(serializationEngine).serialize(any());
	}

	@Test
	public void test_deserialize_defaultEngine() {
		serializationHelper.deserialize(new byte[0]);
		verify(kryoHelper).getPooled();
		verify(serializationEngine).deserialize(any());
	}

	@Test
	public void test_serialize_withCustomEngine() {
		SerializationEngine engine = mock(SerializationEngine.class);

		serializationHelper.serialize("value", engine);
		verify(kryoHelper, never()).getPooled();
		verify(engine).serialize(any());
	}

	@Test
	public void test_serialize_deserialize_withCustomEngine() {
		SerializationEngine engine = mock(SerializationEngine.class);

		serializationHelper.deserialize(new byte[0], engine);
		verify(kryoHelper, never()).getPooled();
		verify(engine).deserialize(any());
	}
}
