package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests for the image physical scale service
 *
 * @author radoslav
 *
 */
public class ImagePhysicalScaleServiceImplTest {

	@InjectMocks
	private ImagePhysicalScaleService physicalScaleService = new ImagePhysicalScaleServiceImpl();

	@Mock
	InstanceTypeResolver instanceTypeResolver;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the calculating of the scale ratio with valid canvas data
	 */
	@Test
	public void testGetPhysicalScaleWithExistingInstanceAndValidData() throws Exception {

		InstanceReference instanceReference = mock(InstanceReference.class);
		Optional<InstanceReference> instanceReferenceOptional = Optional.of(instanceReference);

		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(instanceReferenceOptional);

		Instance instance = mock(Instance.class);
		when(instanceReference.toInstance()).thenReturn(instance);

		when(instance.getAsDouble(anyString())).thenReturn(100.0);
		when(instance.getString(anyString())).thenReturn("cm");

		Dimension<Integer> canvasDimensions = mock(Dimension.class);
		when(canvasDimensions.getWidth()).thenReturn(1000);

		PhysicalScale scale = physicalScaleService.getPhysicalScale(anyString(), canvasDimensions);

		JsonAssert.assertJsonEquals(scale.getScale().toString(),
				new String(
						IOUtils.toByteArray(
								getClass().getClassLoader().getResourceAsStream("manifests/physicalScale.json")),
						"utf-8"));
	}

	/**
	 * Tests the calculating of the scale ratio with invalid canvas data
	 */
	@Test
	public void testGetPhysicalScaleWithExistingInstanceAndInvalidData() {

		InstanceReference instanceReference = mock(InstanceReference.class);
		Optional<InstanceReference> instanceReferenceOptional = Optional.of(instanceReference);

		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(instanceReferenceOptional);

		Instance instance = mock(Instance.class);
		when(instanceReference.toInstance()).thenReturn(instance);

		when(instance.getAsDouble(anyString())).thenReturn(100.0);
		when(instance.getString(anyString())).thenReturn("cm");

		Dimension<Integer> canvasDimensions = new Dimension<>(0, 0);

		PhysicalScale scale = physicalScaleService.getPhysicalScale(anyString(), canvasDimensions);

		assertNull(scale);
	}

	/**
	 * Tests the calculating of the scale ratio with invalid physical dimensions data
	 */
	@Test
	public void testGetPhysicalScaleWithExistingInstanceAndInvalidPhysicalData() {

		InstanceReference instanceReference = mock(InstanceReference.class);
		Optional<InstanceReference> instanceReferenceOptional = Optional.of(instanceReference);

		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(instanceReferenceOptional);

		Instance instance = mock(Instance.class);
		when(instanceReference.toInstance()).thenReturn(instance);

		when(instance.getAsDouble(anyString())).thenReturn(-1.0);
		when(instance.getString(anyString())).thenReturn("cm");

		Dimension<Integer> canvasDimensions = mock(Dimension.class);
		when(canvasDimensions.getWidth()).thenReturn(0);

		PhysicalScale scale = physicalScaleService.getPhysicalScale(anyString(), canvasDimensions);

		assertNull(scale);
	}

	/**
	 * Tests the calculating of the scale ratio with invalid instance reference
	 */
	@Test
	public void testGetPhysicalScaleWithInvalidInstance() {

		Optional<InstanceReference> instanceReferenceOptional = Optional.ofNullable(null);

		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(instanceReferenceOptional);

		Dimension<Integer> canvasDimensions = mock(Dimension.class);
		when(canvasDimensions.getWidth()).thenReturn(0);

		PhysicalScale scale = physicalScaleService.getPhysicalScale(anyString(), canvasDimensions);

		assertNull(scale);
	}
}
