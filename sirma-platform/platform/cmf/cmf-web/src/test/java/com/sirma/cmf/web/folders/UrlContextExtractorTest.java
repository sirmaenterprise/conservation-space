package com.sirma.cmf.web.folders;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test class for {@link UrlContextExtractor}
 *
 * @author cdimitrov
 */
@Test
public class UrlContextExtractorTest {

	private static final String INSTANCE_ID = "instanceId";
	private static final String INSTANCE_TYPE = "type";

	private UrlContextExtractor contextExtractor;
	private TypeConverter typeConverter;
	private InstanceReference instanceReference;
	private EmfInstance instance;

	/**
	 * Prepare test components.
	 */
	@SuppressWarnings("serial")
	@BeforeTest
	public void init() {

		// has two roles, can be child or root instance
		instance = new EmfInstance();
		final Map<String, String> requestMap = new HashMap<String, String>();

		// initialize the context executor
		contextExtractor = new UrlContextExtractor() {
			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

			@Override
			protected Map<String, String> getRequestedMap() {
				return requestMap;
			}
		};

		// initialize the instance reference that will
		// received from the type converter
		instanceReference = new InstanceReferenceMock(null, null, instance);

		typeConverter = Mockito.mock(TypeConverter.class);
		TypeConverterUtil.setTypeConverter(typeConverter);
		ReflectionUtils.setField(contextExtractor, "typeConverter", typeConverter);
	}

	/**
	 * Test method for {@link UrlContextExtractor}.
	 */
	public void extractAndPopulateFromUrlTest() {
		Instance rootInstance = contextExtractor.getDocumentContext().getRootInstance();
		Instance currentInstance = contextExtractor.getDocumentContext().getCurrentInstance();
		Assert.assertNull(rootInstance);
		Assert.assertNull(currentInstance);

		Mockito.when(typeConverter.convert(InstanceReference.class, INSTANCE_TYPE)).thenReturn(instanceReference);

		// simulate request map extraction with instance identifier
		contextExtractor.getRequestedMap().put(INSTANCE_ID, INSTANCE_ID);
		// simulate request map extraction with instance type
		contextExtractor.getRequestedMap().put(INSTANCE_TYPE, INSTANCE_TYPE);

		contextExtractor.extractAndPopulateFromUrl();

		rootInstance = contextExtractor.getDocumentContext().getRootInstance();
		currentInstance = contextExtractor.getDocumentContext().getCurrentInstance();
		Assert.assertNotNull(instance);
		// instance extracted from the url is root instance
		Assert.assertEquals(rootInstance, instance);
		Assert.assertEquals(currentInstance, instance);

		Instance root = new EmfInstance();
		root.setId("root");
		// simulate child instance
		instance.setOwningInstance(root);

		contextExtractor.extractAndPopulateFromUrl();

		// extract root instance form the document context
		rootInstance = contextExtractor.getDocumentContext().getRootInstance();
		// extract child instance from document context
		currentInstance = contextExtractor.getDocumentContext().getCurrentInstance();
		Assert.assertNotNull(rootInstance);
		Assert.assertEquals(rootInstance, root);
		Assert.assertNotNull(currentInstance);
		Assert.assertEquals(currentInstance, instance);
	}

}
