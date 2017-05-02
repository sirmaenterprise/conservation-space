package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sirma.itt.seip.domain.mock.TestType;

/**
 * Tests for {@link ClassInstance}.
 * 
 * @author smustafov
 */
public class ClassInstanceTest {

	@Test
	public void testHasSubType() {
		ClassInstance classInstance = new ClassInstance();

		TestType type = new TestType();
		type.setId("type1");
		ClassInstance subClassInstance1 = new ClassInstance();
		subClassInstance1.setType(type);

		ClassInstance subClassInstance2 = new ClassInstance();
		TestType type2 = new TestType();
		type2.setId("type2");
		subClassInstance2.setType(type2);

		Map<String, ClassInstance> map1 = new HashMap<>();
		map1.put("type1", subClassInstance1);
		classInstance.setSubClasses(map1);

		Map<String, ClassInstance> map2 = new HashMap<>();
		map2.put("type2", subClassInstance2);
		subClassInstance1.setSubClasses(map2);

		TestType nullType = null;

		assertTrue(classInstance.hasSubType(type));
		assertTrue(classInstance.hasSubType(type2));
		assertFalse(classInstance.hasSubType(nullType));
		assertFalse(classInstance.hasSubType("notExisting"));
	}

}
