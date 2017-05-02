package com.sirma.itt.seip.template.rest.handlers.readers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.testutil.io.FileTestUtils;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

@Test
public class TemplateInstanceMessageBodyReaderTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@InjectMocks
	private TemplateInstanceMessageBodyReader reader = new TemplateInstanceMessageBodyReader();

	@BeforeTest
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	public void testIsReadable() {
		Assert.assertTrue(reader.isReadable(TemplateInstance.class, null, null, null));
		Assert.assertFalse(reader.isReadable(Instance.class, null, null, null));
	}

	@Test(expectedExceptions = BadRequestException.class)
	public void testMissingGroupId() throws Exception {
		InputStream in = new ByteArrayInputStream("{ }".getBytes(StandardCharsets.UTF_8));
		reader.readFrom(null, null, null, null, null, in);
	}

	public void testDeserialize() throws Exception {
		TypeConverterUtil.setTypeConverter(Mockito.mock(TypeConverter.class));

		Instance owningInstance = new EmfInstance();
		InstanceReference instanceReference = new InstanceReferenceMock(owningInstance);
		Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class)))
				.thenReturn(Optional.of(instanceReference));
		InputStream in = FileTestUtils.getResourceAsStream("/json/full-template.json");
		TemplateInstance instance = reader.readFrom(null, null, null, null, null, in);
		Assert.assertEquals(instance.getId(), 1L);
		Assert.assertEquals(instance.getForType(), "definition-id");
		Assert.assertEquals(instance.getPrimary(), Boolean.TRUE);
		Assert.assertEquals(instance.getProperties().get(DefaultProperties.TITLE), "Test Template");
		Assert.assertEquals(instance.getContent(), "test");
		Assert.assertEquals(instance.getPurpose(), TemplatePurposes.CREATABLE);
		Assert.assertEquals(instance.getOwningInstance(), owningInstance);
	}

	public void testDeserializeNoProperties() throws Exception {
		InputStream in = FileTestUtils.getResourceAsStream("/json/no-props-template.json");
		TemplateInstance instance = reader.readFrom(null, null, null, null, null, in);
		Assert.assertEquals(instance.getId(), 1L);
		Assert.assertEquals(instance.getForType(), "definition-id");
		// if primary is not provided, its default value should be false
		Assert.assertFalse(instance.getPrimary());
		Assert.assertNull(instance.getProperties().get(DefaultProperties.TITLE));
		Assert.assertEquals(instance.getContent(), "");
		Assert.assertNull(instance.getPurpose());
		Assert.assertNull(instance.getOwningInstance());
	}

	public void testDeserializeNoId() throws Exception {
		InputStream in = FileTestUtils.getResourceAsStream("/json/no-id-template.json");
		TemplateInstance instance = reader.readFrom(null, null, null, null, null, in);
		Assert.assertNull(instance.getId());
	}

}
