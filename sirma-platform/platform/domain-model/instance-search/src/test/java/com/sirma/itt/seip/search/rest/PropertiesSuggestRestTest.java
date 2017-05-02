package com.sirma.itt.seip.search.rest;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.search.SearchService;

/**
 * Tests for {@link PropertiesSuggestRest}.
 *
 * @author smustafov
 */
public class PropertiesSuggestRestTest {

	@Mock
	private SearchService searchService;

	@Mock
	private InstanceTypeResolver typeResolver;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private InstanceContextInitializer contextInitializer;

	@InjectMocks
	private PropertiesSuggestRest propertiesRest;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests with empty string parameters.
	 */
	@Test
	public void testWithEmptyParameters() {
		List<Instance> result = propertiesRest.suggest("", "", true);
		Assert.assertEquals(0, result.size());
	}

	/**
	 * Tests that context is of the given type for single valued property.
	 */
	@Test
	public void testSingleValuedProperty_contextIsOfType() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);
		Instance instance = new ObjectInstance();

		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(ShortUri.class, type)).thenReturn(shortUri);
		when(instanceType.getId()).thenReturn(type);
		when(reference.getType()).thenReturn(instanceType);
		when(reference.toInstance()).thenReturn(instance);

		List<Instance> result = propertiesRest.suggest(contextId, type, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testSingleValuedProperty_closestParent() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = mock(InstanceReference.class);
		InstanceReference parentReference = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		InstanceType parentInstanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);
		Instance instance = new ObjectInstance();

		when(reference.getParent()).thenReturn(parentReference);
		when(reference.getType()).thenReturn(instanceType);
		when(parentReference.getType()).thenReturn(parentInstanceType);
		when(parentReference.toInstance()).thenReturn(instance);
		when(instanceType.getId()).thenReturn(type);
		when(parentInstanceType.getId()).thenReturn("emf:Case");
		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(Matchers.eq(ShortUri.class), Matchers.anyString()))
				.thenReturn(new ShortUri("emf:Project"))
				.thenReturn(shortUri);

		List<Instance> result = propertiesRest.suggest(contextId, type, false);

		Assert.assertEquals(1, result.size());
		verify(contextInitializer).restoreHierarchy(reference);
	}

	@Test
	public void testSingleValuedProperty_anyRelation() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = mock(InstanceReference.class);
		InstanceReference parentReference = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		InstanceType parentInstanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);

		when(reference.getParent()).thenReturn(parentReference);
		when(reference.getType()).thenReturn(instanceType);
		when(parentReference.getType()).thenReturn(parentInstanceType);
		when(parentReference.isRoot()).thenReturn(Boolean.TRUE);
		when(instanceType.getId()).thenReturn(type);
		when(parentInstanceType.getId()).thenReturn("emf:Case");
		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(Matchers.eq(ShortUri.class), Matchers.anyString()))
				.thenReturn(new ShortUri("emf:Project")).thenReturn(shortUri);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				ArrayList<Instance> searchResult = new ArrayList<>();
				ObjectInstance instance = new ObjectInstance();
				instance.setId("emf:instanceId");
				searchResult.add(instance);
				searchArguments.setResult(searchResult);
				return searchArguments;
			}
		}).when(searchService).search(Matchers.anyObject(), Matchers.anyObject());

		List<Instance> resolvedInstances = new ArrayList<>();
		resolvedInstances.add(new ObjectInstance());
		when(typeResolver.resolveInstances(Matchers.anyCollection())).thenReturn(resolvedInstances);

		List<Instance> result = propertiesRest.suggest(contextId, type, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testMultiValuedProperty() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);
		Instance instance = new ObjectInstance();

		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(ShortUri.class, type)).thenReturn(shortUri);
		when(instanceType.getId()).thenReturn(type);
		when(reference.getType()).thenReturn(instanceType);
		when(reference.toInstance()).thenReturn(instance);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				ArrayList<Instance> searchResult = new ArrayList<>();
				ObjectInstance instance = new ObjectInstance();
				instance.setId("emf:instanceId");
				searchResult.add(instance);
				searchArguments.setResult(searchResult);
				return searchArguments;
			}
		}).when(searchService).search(Matchers.anyObject(), Matchers.anyObject());

		List<Instance> resolvedInstances = new ArrayList<>();
		resolvedInstances.add(new ObjectInstance());
		when(typeResolver.resolveInstances(Matchers.anyCollection())).thenReturn(resolvedInstances);

		List<Instance> result = propertiesRest.suggest(contextId, type, true);

		Assert.assertEquals(2, result.size());
	}

	@Test
	public void testMultiValuedProperty_anyRelations() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri("emf:Case");
		Instance instance = new ObjectInstance();

		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(ShortUri.class, type)).thenReturn(shortUri);
		when(instanceType.getId()).thenReturn(type);
		when(reference.getType()).thenReturn(instanceType);
		when(reference.toInstance()).thenReturn(instance);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				searchArguments.setResult(new ArrayList<>());
				return searchArguments;
			}
		}).doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				ArrayList<Instance> searchResult = new ArrayList<>();
				ObjectInstance instance = new ObjectInstance();
				instance.setId("emf:instanceId");
				searchResult.add(instance);
				searchArguments.setResult(searchResult);
				return searchArguments;
			}
		}).when(searchService).search(Matchers.anyObject(), Matchers.anyObject());

		List<Instance> resolvedInstances = new ArrayList<>();
		resolvedInstances.add(new ObjectInstance());
		when(typeResolver.resolveInstances(Matchers.anyCollection())).thenReturn(new ArrayList<>())
				.thenReturn(resolvedInstances);

		List<Instance> result = propertiesRest.suggest(contextId, type, true);

		Assert.assertEquals(1, result.size());
	}

}
