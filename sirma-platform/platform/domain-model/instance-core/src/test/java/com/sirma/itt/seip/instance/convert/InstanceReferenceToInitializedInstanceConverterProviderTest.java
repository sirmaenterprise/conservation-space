package com.sirma.itt.seip.instance.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.instance.convert.InstanceReferenceToInitializedInstanceConverterProvider.InstanceToInitializedInstanceConverter;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;

/**
 * Test for {@link InstanceReferenceToInitializedInstanceConverterProvider}
 *
 * @author BBonev
 */
public class InstanceReferenceToInitializedInstanceConverterProviderTest {

	@InjectMocks
	private InstanceReferenceToInitializedInstanceConverterProvider converterProvider;

	@Mock
	private InstanceService instanceService;
	@Mock
	private TypeConverter typeConverter;

	private InstanceToInitializedInstanceConverter converter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		converter = converterProvider.new InstanceToInitializedInstanceConverter();
	}

	@Test
	public void testRegister() throws Exception {
		converterProvider.register(typeConverter);

		verify(typeConverter).addConverter(eq(LinkSourceId.class), eq(InitializedInstance.class),
				any(InstanceToInitializedInstanceConverter.class));
	}

	@Test(expected = EmfConfigurationException.class)
	public void testConvert_invalid_noType() throws Exception {
		converter.convert(new LinkSourceId());
	}

	@Test(expected = EmfConfigurationException.class)
	public void testConvert_invalid_noId() throws Exception {
		converter.convert(new LinkSourceId(null, mock(DataTypeDefinition.class)));
	}

	@Test
	public void testLoadFromService() throws Exception {
		DataTypeDefinition type = new DataTypeDefinitionMock(EmfInstance.class, null);
		LinkSourceId linkSourceId = new LinkSourceId("emf:instance", type);
		linkSourceId.setType(InstanceType.create("emf:Case"));
		linkSourceId.setParent(new LinkSourceId("parent", mock(DataTypeDefinition.class)));

		when(instanceService.loadByDbId(any(Serializable.class))).then(a -> {
			EmfInstance instance = new EmfInstance();
			instance.setId(a.getArgumentAt(0, Serializable.class));
			return instance;
		});

		InitializedInstance initializedInstance = converter.convert(linkSourceId);
		assertNotNull(initializedInstance);
		assertNotNull(initializedInstance.getInstance());
		assertEquals("emf:instance", initializedInstance.getInstance().getId());
		assertEquals(InstanceType.create("emf:Case"), initializedInstance.getInstance().type());
		assertNotNull(((OwnedModel) initializedInstance.getInstance()).getOwningReference());
	}

	@Test
	public void testLoadFromService_topLevel() throws Exception {
		DataTypeDefinition type = new DataTypeDefinitionMock(EmfInstance.class, null);
		LinkSourceId linkSourceId = new LinkSourceId("emf:instance", type);
		linkSourceId.setType(InstanceType.create("emf:Case"));
		linkSourceId.setParent(InstanceReference.ROOT_REFERENCE);

		when(instanceService.loadByDbId(any(Serializable.class))).then(a -> {
			EmfInstance instance = new EmfInstance();
			instance.setId(a.getArgumentAt(0, Serializable.class));
			return instance;
		});

		InitializedInstance initializedInstance = converter.convert(linkSourceId);
		assertNotNull(initializedInstance);
		assertNotNull(initializedInstance.getInstance());
		assertEquals("emf:instance", initializedInstance.getInstance().getId());
		assertEquals(InstanceType.create("emf:Case"), initializedInstance.getInstance().type());
		assertNull(((OwnedModel) initializedInstance.getInstance()).getOwningReference());
	}
}
