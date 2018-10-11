package com.sirma.itt.seip.instance.archive;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.ArchivedInstanceReference;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link ArchivedInstanceToArchivedReferenceConverterProvider}.
 *
 * @author A. Kunchev
 */
public class ArchivedInstanceToArchivedReferenceConverterProviderTest {

	@InjectMocks
	private ArchivedInstanceToArchivedReferenceConverterProvider provider;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private InstanceTypes instanceTypes;

	@Before
	public void setup() {
		provider = new ArchivedInstanceToArchivedReferenceConverterProvider();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register() {
		provider.register(typeConverter);
		verify(typeConverter).addConverter(eq(ArchivedInstance.class), eq(ArchivedInstanceReference.class),
				any(Converter.class));
		verify(typeConverter).addConverter(eq(ArchivedInstanceReference.class), eq(InitializedInstance.class),
				any(Converter.class));
	}

	@Test
	public void convertInstanceToReference_fromOriginalReference() {
		TypeConverter converter = new TypeConverterImpl();
		provider.register(converter);

		ArchivedInstance instance = new ArchivedInstance();
		instance.setId("instance-id-v1.6");
		instance.setTargetId("instance-id");

		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock("");
		InstanceReference originalReference = new InstanceReferenceMock("instance-id", dataTypeDefinition);
		InstanceType instanceType = InstanceTypeFake.buildForClass("emf:Object");
		originalReference.setType(instanceType);
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(originalReference));

		ArchivedInstanceReference reference = converter.convert(ArchivedInstanceReference.class, instance);

		assertEquals("instance-id-v1.6", reference.getId());
		assertEquals(dataTypeDefinition, reference.getReferenceType());
		assertEquals(instanceType, reference.getType());
	}

	@Test(expected = EmfRuntimeException.class)
	public void convertInstanceToReference_originalReferenceNotPresentAndNoSemanticType() {
		TypeConverter converter = new TypeConverterImpl();
		provider.register(converter);

		ArchivedInstance instance = new ArchivedInstance();
		instance.setId("instance-id-v1.6");
		instance.setTargetId("instance-id");
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());
		converter.convert(ArchivedInstanceReference.class, instance);
	}

	@Test(expected = EmfRuntimeException.class)
	public void convertInstanceToReference_originalReferenceNotPresentAndNotResolvedType() {
		TypeConverter converter = new TypeConverterImpl();
		provider.register(converter);

		ArchivedInstance instance = new ArchivedInstance();
		instance.setId("instance-id-v1.6");
		instance.setTargetId("instance-id");
		instance.add(SEMANTIC_TYPE, "semantic-type-uri");

		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());
		when(instanceTypes.from("semantic-type-uri")).thenReturn(Optional.empty());

		converter.convert(ArchivedInstanceReference.class, instance);
	}

	@Test
	public void convert_instanceToReference_originalReferenceNotPresentAndWithResolvedType() {
		TypeConverter converter = new TypeConverterImpl();
		provider.register(converter);

		ArchivedInstance instance = new ArchivedInstance();
		instance.setId("instance-id-v1.5");
		instance.setTargetId("instance-id");
		instance.add(SEMANTIC_TYPE, "semantic-type-uri");

		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());

		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock("");
		when(definitionService.getDataTypeDefinition("emf:Object")).thenReturn(dataTypeDefinition);

		InstanceType instanceType = InstanceTypeFake.buildForClass("emf:Object");
		when(instanceTypes.from("semantic-type-uri")).thenReturn(Optional.of(instanceType));

		ArchivedInstanceReference reference = converter.convert(ArchivedInstanceReference.class, instance);

		assertEquals("instance-id-v1.5", reference.getId());
		assertEquals(dataTypeDefinition, reference.getReferenceType());
		assertEquals(instanceType, reference.getType());
	}

	@Test(expected = InstanceNotFoundException.class)
	public void convert_referenceToInstance_noInstanceFound() {
		TypeConverter converter = new TypeConverterImpl();
		provider.register(converter);

		when(instanceVersionService.loadVersion("instance-id-v1.3")).thenReturn(null);
		ArchivedInstanceReference reference = new ArchivedInstanceReference("instance-id-v1.3",
				new DataTypeDefinitionMock(""));

		converter.convert(InitializedInstance.class, reference);
	}

	@Test
	public void convert_referenceToInstance_instanceFound() {
		TypeConverter converter = new TypeConverterImpl();
		provider.register(converter);

		when(instanceVersionService.loadVersion("instance-id-v1.3")).thenReturn(new ArchivedInstance());
		ArchivedInstanceReference reference = new ArchivedInstanceReference("instance-id-v1.3",
				new DataTypeDefinitionMock(""));

		assertNotNull(converter.convert(InitializedInstance.class, reference));
	}

}
