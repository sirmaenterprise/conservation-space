/**
 *
 */
package com.sirma.itt.seip.serialization.kryo;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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

import com.sirma.itt.seip.Pair;

/**
 * @author BBonev
 *
 */
public class ChainingKryoInitializerTest {

	@InjectMocks
	ChainingKryoInitializer initializer;

	@Mock
	KryoInitializer kryoInitializer1;
	@Mock
	KryoInitializer kryoInitializer2;

	@Spy
	List<KryoInitializer> initializers = new ArrayList<>();

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		initializers.clear();
		initializers.add(kryoInitializer1);
		initializers.add(kryoInitializer2);
	}

	@Test
	public void test_testName() {
		Pair<Class<?>, Integer> pair1 = new Pair<>(ChainingKryoInitializerTest.class, 1);
		Pair<Class<?>, Integer> pair2 = new Pair<>(ChainingKryoInitializer.class, 2);

		when(kryoInitializer1.getClassesToRegister()).thenReturn(Arrays.asList(pair1));
		when(kryoInitializer2.getClassesToRegister()).thenReturn(Arrays.asList(pair2));

		List<Pair<Class<?>, Integer>> classesToRegister = initializer.getClassesToRegister();

		assertNotNull(classesToRegister);
		assertFalse(classesToRegister.isEmpty());
		assertEquals(classesToRegister.size(), 2);
		assertEquals(classesToRegister.get(0), pair1);
		assertEquals(classesToRegister.get(1), pair2);

	}

}
