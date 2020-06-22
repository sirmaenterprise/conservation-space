package com.sirma.itt.seip.instance.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link InstancePropertyComparator}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/09/2017
 */
public class InstancePropertyComparatorTest {

	@Test
	public void compare_shouldDoNothingOnNullProperty() throws Exception {
		String[] ids = { "emf:instance1", "emf:instance3", "emf:instance2" };
		List<Instance> instances = Arrays.stream(ids).map(EmfInstance::new).collect(Collectors.toList());

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty(null).build();

		instances.sort(comparator);

		assertEquals("emf:instance1", instances.get(0).getId());
		assertEquals("emf:instance3", instances.get(1).getId());
		assertEquals("emf:instance2", instances.get(2).getId());
	}

	@Test
	public void compare_shouldDoNothingForSameInstances() throws Exception {
		Instance instance = new EmfInstance("emf:instance");

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		assertEquals(0, comparator.compare(instance, instance));
	}

	@Test
	public void compare_ShouldReportEqualsForBothMissingPropertyValues() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		Instance instance2 = new EmfInstance("emf:instance2");

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		assertEquals(0, comparator.compare(instance1, instance2));
	}

	@Test
	public void compare_ShouldReportPositiveIfSecondPropertyIsMissing() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		instance1.add("test", "value");
		Instance instance2 = new EmfInstance("emf:instance2");

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		assertTrue("Should be positive number", comparator.compare(instance1, instance2) > 0);
	}

	@Test
	public void compare_ShouldReportNegativeIfSecondPropertyIsMissing() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		Instance instance2 = new EmfInstance("emf:instance2");
		instance2.add("test", "value");

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		assertTrue("Should be negative number", comparator.compare(instance1, instance2) < 0);
	}

	@Test
	public void compare_ShouldReportZeroIfValuesAreEqual() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		instance1.add("test", "value");
		Instance instance2 = new EmfInstance("emf:instance2");
		instance2.add("test", "value");

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		assertEquals(0, comparator.compare(instance1, instance2));
	}

	@Test
	public void compare_ShouldApplyInverseOrder() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		instance1.add("test", "value1");
		Instance instance2 = new EmfInstance("emf:instance2");
		instance2.add("test", "value2");

		InstancePropertyComparator ascendingComparator = InstancePropertyComparator.forProperty("test")
				.ascending().build();

		assertTrue("'value1' should be 'bigger' then 'value2'", ascendingComparator.compare(instance1, instance2) < 0);

		InstancePropertyComparator descendingComparator = InstancePropertyComparator.forProperty("test")
				.descending().build();

		assertTrue("'value1' should be 'smaller' then 'value2'",
				descendingComparator.compare(instance1, instance2) > 0);
	}

	@Test
	public void compare_ShouldSortByNaturalLanguageIfEnabled() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		instance1.add("test", "User");
		Instance instance2 = new EmfInstance("emf:instance2");
		instance2.add("test", "user");

		InstancePropertyComparator noCollator = InstancePropertyComparator.forProperty("test").build();

		List<Instance> list = new ArrayList<>(Arrays.asList(instance1, instance2));
		list.sort(noCollator);

		assertEquals("emf:instance1", list.get(0).getId());
		assertEquals("emf:instance2", list.get(1).getId());

		assertTrue("'User' should be 'bigger' than 'user' without collator",
				noCollator.compare(instance1, instance2) < 0);

		InstancePropertyComparator withCollator = InstancePropertyComparator.forProperty("test")
				.localeSensitive(Locale.ENGLISH).build();

		list = new ArrayList<>(Arrays.asList(instance1, instance2));
		list.sort(withCollator);

		assertEquals("emf:instance2", list.get(0).getId());
		assertEquals("emf:instance1", list.get(1).getId());

		assertTrue("'User' should be 'smaller' than 'user' with collator",
				withCollator.compare(instance1, instance2) > 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compare_ShouldFailForInvalidValue1() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		instance1.add("test", new LinkedList<>());
		Instance instance2 = new EmfInstance("emf:instance2");
		instance2.add("test", "value2");

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		comparator.compare(instance1, instance2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compare_ShouldFailForInvalidValue2() throws Exception {
		Instance instance1 = new EmfInstance("emf:instance1");
		instance1.add("test", "value1");
		Instance instance2 = new EmfInstance("emf:instance2");
		instance2.add("test", new LinkedList<>());

		InstancePropertyComparator comparator = InstancePropertyComparator.forProperty("test").build();

		comparator.compare(instance1, instance2);
	}
}
