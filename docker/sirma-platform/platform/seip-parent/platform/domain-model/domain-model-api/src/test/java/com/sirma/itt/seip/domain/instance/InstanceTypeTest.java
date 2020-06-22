package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sirma.itt.seip.domain.mock.TestType;

/**
 * Test for {@link InstanceType}
 *
 * @author BBonev
 */
public class InstanceTypeTest {

	@Test
	public void testInstanceOf_InstanceType() throws Exception {
		ClassInstance instance = new ClassInstance();
		instance.setId("emf:Case");
		ClassInstance activity = new ClassInstance();
		activity.setId("emf:Activity");
		instance.getSuperClasses().add(activity);

		assertFalse(instance.instanceOf((InstanceType) null));
		assertTrue(instance.instanceOf(InstanceType.create("emf:Case")));
		assertTrue(instance.instanceOf(InstanceType.create("emf:Activity")));
		assertFalse(instance.instanceOf(InstanceType.create("emf:Project")));
	}

	@Test
	public void testInstanceOf_typeId() throws Exception {
		ClassInstance instance = new ClassInstance();
		instance.setId("emf:Case");
		ClassInstance object = new ClassInstance();
		object.setId("ptop:Object");
		instance.getSuperClasses().add(object);
		ClassInstance activity = new ClassInstance();
		activity.setId("emf:Activity");
		instance.getSuperClasses().add(activity);

		assertFalse(instance.instanceOf((Serializable) null));
		assertTrue(instance.instanceOf("emf:Case"));
		assertTrue(instance.instanceOf("emf:Activity"));
		assertFalse(instance.instanceOf("emf:Project"));
	}

	@Test
	public void testIsAllowedForMimetype() throws Exception {
		ClassInstance instance = new ClassInstance();
		instance.setId("emf:Case");

		assertFalse(instance.isDataTypePatternSupported());
		assertTrue(instance.isAllowedForMimetype("image/jpg"));

		instance.add("acceptDataTypePattern", "^image/");

		assertTrue(instance.isDataTypePatternSupported());

		assertFalse(instance.isAllowedForMimetype(null));
		assertTrue(instance.isAllowedForMimetype("image/jpg"));
		assertFalse(instance.isAllowedForMimetype("text/html"));
	}

	@Test
	public void testSearchable() throws Exception {
		ClassInstance instance = new ClassInstance();

		assertFalse(instance.isSearchable());
		instance.add("searchable", Boolean.FALSE);
		assertFalse(instance.isSearchable());
		instance.add("searchable", Boolean.TRUE);
		assertTrue(instance.isSearchable());
	}

	@Test
	public void testCreateable() throws Exception {
		ClassInstance instance = new ClassInstance();

		assertFalse(instance.isCreatable());
		instance.add("createable", Boolean.FALSE);
		assertFalse(instance.isCreatable());
		instance.add("createable", Boolean.TRUE);
		assertTrue(instance.isCreatable());
	}

	@Test
	public void testUploadable() throws Exception {
		ClassInstance instance = new ClassInstance();

		assertFalse(instance.isUploadable());
		instance.add("uploadable", Boolean.FALSE);
		assertFalse(instance.isUploadable());
		instance.add("uploadable", Boolean.TRUE);
		assertTrue(instance.isUploadable());
	}

	@Test
	public void testMailboxSupportable() throws Exception {
		ClassInstance instance = new ClassInstance();

		assertFalse(instance.isMailboxSupportable());
		instance.add("mailboxSupportable", Boolean.FALSE);
		assertFalse(instance.isMailboxSupportable());
		instance.add("mailboxSupportable", Boolean.TRUE);
		assertTrue(instance.isMailboxSupportable());
	}

	@Test
	public void testIsPartOfObjectLibrary() throws Exception {
		ClassInstance instance = new ClassInstance();

		assertFalse(instance.isPartOflibrary());
		instance.add("partOfObjectLibrary", Boolean.FALSE);
		assertFalse(instance.isPartOflibrary());
		instance.add("partOfObjectLibrary", Boolean.TRUE);
		assertTrue(instance.isPartOflibrary());
	}

	@Test
	public void testIsCategory() throws Exception {
		ClassInstance instance = new ClassInstance();
		instance.setCategory("case");

		assertFalse(instance.is(null));
		assertFalse(instance.is("project"));
		assertFalse(instance.is("CASE"));
		assertTrue(instance.is("case"));
	}

	@Test
	public void testDefaultInstanceType_Equals() throws Exception {

		assertTrue(InstanceType.create("emf:Case").equals(InstanceType.create("emf:Case")));
		assertFalse(InstanceType.create("emf:Case").equals(InstanceType.create("emf:Project")));
		assertFalse(InstanceType.create("emf:Case").equals(InstanceType.create("emf:CASE")));
		assertFalse(InstanceType.create("emf:Case").equals(null));
		assertFalse(InstanceType.create("emf:Case").equals(new Object()));

		ClassInstance instance = new ClassInstance();
		instance.setId("emf:Case");
		assertTrue(InstanceType.create("emf:Case").equals(instance));
		assertTrue(instance.equals(InstanceType.create("emf:Case")));
	}

	@Test
	public void testDefaultInstanceType_HashCode() throws Exception {

		assertTrue(InstanceType.create("emf:Case").hashCode() == InstanceType.create("emf:Case").hashCode());
		assertFalse(InstanceType.create("emf:Case").hashCode() == InstanceType.create("emf:Project").hashCode());
		assertFalse(InstanceType.create("emf:Case").hashCode() == InstanceType.create("emf:CASE").hashCode());
		assertFalse(InstanceType.create("emf:Case").hashCode() == new Object().hashCode());

		ClassInstance instance = new ClassInstance();
		instance.setId("emf:Case");
		assertTrue(InstanceType.create("emf:Case").hashCode() == instance.hashCode());
		assertTrue(instance.hashCode() == InstanceType.create("emf:Case").hashCode());
	}

	@Test
	public void testDefaultInstanceType_defaultMethods() throws Exception {
		InstanceType type = InstanceType.create("emf:Case");
		assertNull(type.getCategory());
		assertNull(type.getProperty("test"));
		assertNull(type.getProperty(null));
		assertFalse(type.hasTrait("test"));
		assertFalse(type.hasTrait(null));
		assertFalse(type.is("test"));
		assertFalse(type.isCreatable());
		assertFalse(type.isSearchable());
		assertFalse(type.isUploadable());
		assertFalse(type.isDataTypePatternSupported());

		Set<InstanceType> superTypes = type.getSuperTypes();
		assertNotNull(superTypes);
		assertTrue(superTypes.isEmpty());

		Set<InstanceType> subTypes = type.getSubTypes();
		assertNotNull(subTypes);
		assertTrue(subTypes.isEmpty());
	}

	@Test
	public void testHasSubType() {
		Set<InstanceType> subTypes = new HashSet<>();
		TestType subType1 = new TestType();
		subType1.setId("subType1");
		subTypes.add(subType1);

		TestType subType3 = new TestType();
		subType3.setId("subType3");
		Set<InstanceType> subTypesOf1 = new HashSet<>();
		subTypesOf1.add(subType3);
		subType1.setSubTypes(subTypesOf1);

		TestType subType2 = new TestType();
		subType2.setId("subType2");
		subTypes.add(subType2);

		TestType parentType = new TestType();
		parentType.setSubTypes(subTypes);
		String nullType = null;

		assertTrue(parentType.hasSubType(subType2));
		assertTrue(parentType.hasSubType("subType3"));
		assertFalse(parentType.hasSubType("notExistingSubType"));
		assertFalse(parentType.hasSubType(nullType));
	}

}
