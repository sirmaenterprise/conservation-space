package com.sirma.itt.emf.solr.services.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchableProperty;

/**
 * Tests for the {@link SearchablePropertiesUtils}.
 *
 * @author nvelkov
 */
public class SearchablePropertiesUtilsTest {

	/**
	 * Test the merge of two matching searchable properties and one that doesn't match. The two matching properties'
	 * codelists should be added to the first property's codelists.
	 */
	@Test
	public void testMerge() {
		SearchableProperty propertyToBeModified = new SearchableProperty();
		propertyToBeModified.setCodelists(new HashSet<>(Arrays.asList(1)));
		propertyToBeModified.setId("someId");

		SearchableProperty propertyToBeMatchedAgainst = new SearchableProperty();
		propertyToBeMatchedAgainst.setCodelists(new HashSet<>(Arrays.asList(2)));
		propertyToBeMatchedAgainst.setId("someId");

		SearchableProperty propertyThatDoesntMatch = new SearchableProperty();
		propertyThatDoesntMatch.setId("anotherId");

		SearchablePropertiesUtils.merge(Arrays.asList(propertyToBeModified),
				Arrays.asList(propertyToBeMatchedAgainst, propertyThatDoesntMatch),
				(first, second) -> first.getCodelists().addAll(second.getCodelists()));
		Assert.assertEquals(propertyToBeModified.getCodelists().size(), 2);
		Assert.assertTrue(propertyToBeModified.getCodelists().contains(1));
		Assert.assertTrue(propertyToBeModified.getCodelists().contains(2));
		Assert.assertEquals(propertyToBeMatchedAgainst.getCodelists().size(), 1);
	}

	/**
	 * Deep copy the provided list and modify the cloned element. The original element shouldn't be modified.
	 */
	@Test
	public void testClone() {
		SearchableProperty property = new SearchableProperty();
		property.setId("someId");
		Set<SearchableProperty> propertyClones = SearchablePropertiesUtils.clone(Arrays.asList(property));
		SearchableProperty clonedProperty = propertyClones.iterator().next();
		clonedProperty.setId("changedId");

		Assert.assertNotEquals(property.getId(), clonedProperty.getId());
	}

	/**
	 * Deep copy the provided null list. The result should be an empty collection.
	 */
	@Test
	public void testCloneEmptyList() {
		Set<SearchableProperty> propertyClones = SearchablePropertiesUtils.clone(null);
		Assert.assertTrue(CollectionUtils.isEmpty(propertyClones));
	}

	/**
	 * Test the addOrRetain method when the source collection is empty meaning that the properties should just be added
	 * instead of retained. Retaining them would result in an intersection where one of the collections is empty which
	 * will always result in an empty collection.
	 */
	@Test
	public void testAddOrRetain() {
		SearchableProperty property = new SearchableProperty();
		property.setId("newProperty1");
		List<SearchableProperty> source = new ArrayList<>();

		// The property will be added because the list is empty.
		SearchablePropertiesUtils.addOrRetain(source, Arrays.asList(property), true);
		Assert.assertEquals(source.size(), 1);

		// The property will be retained because it is already in the list.
		SearchablePropertiesUtils.addOrRetain(source, Arrays.asList(property), true);
		Assert.assertEquals(source.size(), 1);

		SearchablePropertiesUtils.addOrRetain(source, Arrays.asList(property), false);
		Assert.assertEquals(source.size(), 2);
	}

	/**
	 * Test the sort method.
	 */
	@Test
	public void testSort() {
		List<SearchableProperty> properties = new ArrayList<>();
		SearchableProperty firstProperty = new SearchableProperty();
		firstProperty.setLabelId(() -> "bid");
		firstProperty.setLabelProvider(Function.identity());
		SearchableProperty secondProperty = new SearchableProperty();
		secondProperty.setLabelId(() -> "aid");
		secondProperty.setLabelProvider(Function.identity());
		properties.addAll(Arrays.asList(firstProperty, secondProperty));

		List<SearchableProperty> sorted = SearchablePropertiesUtils.sort(properties,
				(p1, p2) -> p1.getText().compareTo(p2.getText()));
		Assert.assertEquals(sorted.get(0).getText(), "aid");
		Assert.assertEquals(sorted.get(1).getText(), "bid");
	}
}
