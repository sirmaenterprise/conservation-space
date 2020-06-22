package com.sirma.sep.instance.properties;

import static org.mockito.Matchers.any;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.properties.PropertiesSuggestService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link PropertiesSuggestServiceImpl}.
 *
 * @author smustafov
 * @author svetlozar.iliev
 */
public class PropertiesSuggestServiceImplTest {

	@Mock
	private SearchService searchService;

	@Mock
	private InstanceTypeResolver typeResolver;

	@Mock
	private TypeConverter typeConverter;

	@Spy
	private InstanceContextServiceMock contextService;

	@InjectMocks
	private PropertiesSuggestService propertiesSuggestService = new PropertiesSuggestServiceImpl();

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests with empty string parameters.
	 */
	@Test
	public void testWithEmptyParameters() {
		List<String> result = propertiesSuggestService.suggestPropertiesIds("", "", true);
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
		Instance instance = new EmfInstance("emf:instanceId");

		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(ShortUri.class, type)).thenReturn(shortUri);
		when(instanceType.getId()).thenReturn(type);
		when(reference.getType()).thenReturn(instanceType);
		when(reference.toInstance()).thenReturn(instance);

		List<String> result = propertiesSuggestService.suggestPropertiesIds(contextId, type, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testSingleValuedProperty_closestParent() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = InstanceReferenceMock.createGeneric(contextId);
		InstanceReference parentReference = InstanceReferenceMock.createGeneric("emf:parentId");
		InstanceType instanceType = mock(InstanceType.class);
		InstanceType parentInstanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);

		contextService.bindContext(reference.toInstance(), parentReference);
		reference.setType(instanceType);
		parentReference.setType(parentInstanceType);
		when(instanceType.getId()).thenReturn(type);
		when(parentInstanceType.getId()).thenReturn("emf:Case");
		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(Matchers.eq(ShortUri.class), Matchers.anyString()))
				.thenReturn(new ShortUri("emf:Project"))
					.thenReturn(shortUri);

		List<String> result = propertiesSuggestService.suggestPropertiesIds(contextId, type, false);

		Assert.assertEquals(1, result.size());
		verify(contextService).getContext(reference);
	}

	@Test
	public void testSingleValuedProperty_anyRelation() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = InstanceReferenceMock.createGeneric(contextId);
		InstanceReference parentReference = InstanceReferenceMock.createGeneric("emf:parentId");
		InstanceType instanceType = mock(InstanceType.class);
		InstanceType parentInstanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);

		contextService.bindContext(reference.toInstance(), parentReference);
		reference.setType(instanceType);
		parentReference.setType(parentInstanceType);
		when(instanceType.getId()).thenReturn(type);
		when(parentInstanceType.getId()).thenReturn("emf:Case");
		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(Matchers.eq(ShortUri.class), Matchers.anyString()))
				.thenReturn(new ShortUri("emf:Project"))
					.thenReturn(shortUri);

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				ArrayList<Instance> searchResult = new ArrayList<>();
				searchResult.add(reference.toInstance());
				searchArguments.setResult(searchResult);
				return searchArguments;
			}
		}).when(searchService).search(Matchers.anyObject(), Matchers.anyObject());

		List<Instance> resolvedInstances = new ArrayList<>();
		resolvedInstances.add(new ObjectInstance());
		when(typeResolver.resolveInstances(Matchers.anyCollection())).thenReturn(resolvedInstances);

		List<String> result = propertiesSuggestService.suggestPropertiesIds(contextId, type, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testMultiValuedProperty() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		
		InstanceReference reference = InstanceReferenceMock.createGeneric(contextId);
		InstanceReference parentReference = InstanceReferenceMock.createGeneric("emf:parentId");
		InstanceType instanceType = mock(InstanceType.class);
		InstanceType parentInstanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri(type);

		when(instanceType.getId()).thenReturn(type);
		reference.setType(instanceType);
		parentReference.setType(parentInstanceType);
		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(ShortUri.class, type)).thenReturn(shortUri);

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				ArrayList<Instance> searchResult = new ArrayList<>();
				searchResult.add(reference.toInstance());
				searchArguments.setResult(searchResult);
				return searchArguments;
			}
		}).when(searchService).search(Matchers.anyObject(), Matchers.anyObject());

		List<Instance> resolvedInstances = new ArrayList<>();
		resolvedInstances.add(new ObjectInstance());
		when(typeResolver.resolveInstances(Matchers.anyCollection())).thenReturn(resolvedInstances);

		List<String> result = propertiesSuggestService.suggestPropertiesIds(contextId, type, true);

		Assert.assertEquals(2, result.size());
		verifyPermissionsFilter();
	}

	private void verifyPermissionsFilter() {
		ArgumentCaptor<SearchArguments<Instance>> argumentCaptor = ArgumentCaptor.forClass(SearchArguments.class);

		verify(searchService).search(any(), argumentCaptor.capture());

		Assert.assertEquals(SearchArguments.QueryResultPermissionFilter.READ,
				argumentCaptor.getValue().getPermissionsType());
	}

	@Test
	public void testMultiValuedProperty_anyRelations() {
		String contextId = "emf:contextId";
		String type = "emf:Document";

		InstanceReference reference = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		ShortUri shortUri = new ShortUri("emf:Case");
		Instance instance = new EmfInstance("emf:instanceId");

		when(typeResolver.resolveReference(contextId)).thenReturn(Optional.of(reference));
		when(typeConverter.convert(ShortUri.class, type)).thenReturn(shortUri);
		when(instanceType.getId()).thenReturn(type);
		when(reference.getType()).thenReturn(instanceType);
		when(reference.toInstance()).thenReturn(instance);

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				searchArguments.setResult(new ArrayList<>());
				return searchArguments;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				SearchArguments searchArguments = invocation.getArgumentAt(1, SearchArguments.class);
				ArrayList<Instance> searchResult = new ArrayList<>();
				Instance instance = new EmfInstance("emf:instanceId");
				searchResult.add(instance);
				searchArguments.setResult(searchResult);
				return searchArguments;
			}
		}).when(searchService).search(Matchers.anyObject(), Matchers.anyObject());

		List<Instance> resolvedInstances = new ArrayList<>();
		resolvedInstances.add(new EmfInstance("emf:instanceId"));
		when(typeResolver.resolveInstances(Matchers.anyCollection())).thenReturn(new ArrayList<>()).thenReturn(
				resolvedInstances);

		List<String> result = propertiesSuggestService.suggestPropertiesIds(contextId, type, true);

		Assert.assertEquals(1, result.size());
	}

}
