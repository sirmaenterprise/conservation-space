package com.sirma.itt.emf.script;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.EMAIL_DISTRIBUTION_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.util.ReflectionUtils;

public class ScriptNodeTest {

	@InjectMocks
	private ScriptNode node;

	@Mock
	private InstanceService instanceService;

	@Mock
	private TypeMappingProvider allowedChildrenTypeProvider;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private EventService eventService;

	@Mock
	private LinkProviderService linkProviderService;

	@Mock
	private InstanceContextService contextService;

	@Before
	public void beforeMethod() {
		node = new ScriptNode();
		MockitoAnnotations.initMocks(this);
		TypeConverterUtil.setTypeConverter(typeConverter);
		InstanceUtil.init(new InstanceUtil(null, contextService));
	}

	@Test
	public void testFireAuditableEvent() {
		assertEquals(node, node.fireAuditableEvent("someEvent"));
		verify(eventService, times(1)).fire(anyObject());
	}

	@Test
	public void testSaveWithNullTarget() {
		node.save();
		Mockito.verifyZeroInteractions(instanceService);
	}

	@Test
	public void testSaveTarget() {
		node.setTarget(new EmfInstance());
		node.save();
		verify(instanceService, times(1)).save(anyObject(), anyObject());
	}

	@Test
	public void testIsWithNullTarget() {
		assertFalse(node.is("something that is not supposed to be"));
	}

	@Test
	public void testIs() {
		EmfInstance instance = new EmfInstance();
		ClassInstance classInstance = new ClassInstance();
		classInstance.setCategory("emfinstance");
		instance.setType(classInstance.type());
		node.setTarget(instance);
		assertFalse(node.is("something that is not supposed to be"));
		assertTrue(node.is("emfinstance"));
	}

	@Test
	public void testGetPropertiesWithNullTarget() {
		assertEquals(null, node.getProperties());
	}

	@Test
	public void testGetProperties() {
		Instance instance = new CommonInstance();
		node.setTarget(instance);
		assertEquals(instance.getProperties(), node.getProperties());
	}

	@Test
	public void testRefreshWithNullTarget() {
		node.refresh();
		Mockito.verifyZeroInteractions(instanceService);
	}

	@Test
	public void testRefreshTarget() {
		node.setTarget(new CommonInstance());
		node.refresh();
		verify(instanceService, times(1)).refresh(anyObject());
	}

	/**
	 * @TODO this should be moved to instance core Test {@link ScriptNode#getParent()} method
	 */
	@Test
	public void testGettingParent() {
		Instance parent = new EmfInstance("parentId");
		ScriptNode parentScriptNode = new ScriptNode();
		parentScriptNode.setTarget(parent);
		when(typeConverter.convert(ScriptNode.class, parent)).thenReturn(parentScriptNode);

		Instance target = new EmfInstance();
		InstanceReference context = mock(InstanceReference.class);
		when(context.toInstance()).thenReturn(parent);

		when(contextService.getContext(target)).thenReturn(Optional.of(context));

		node.setTarget(target);

		assertEquals(parentScriptNode, node.getParent());
		assertEquals(parent, node.getParent().getTarget());
	}

	@Test
	public void testGettingNullParentCase() {

		DataTypeDefinition typeDefinition = Mockito.mock(DataTypeDefinition.class);

		Class<DataTypeDefinition> clazz = DataTypeDefinition.class;
		Mockito.doReturn(clazz).when(typeDefinition).getJavaClass();
		when(allowedChildrenTypeProvider.getDataType("case")).thenReturn(typeDefinition);

		node.setTarget(new CommonInstance());
		when(contextService.getContext(node.getTarget())).thenReturn(Optional.empty());
		ScriptNode parentCase = node.getParent("case");

		assertNull(parentCase);
	}

	@Test
	public void testBuildBookmarkableURL() {
		when(linkProviderService.buildLink(any(Instance.class))).thenReturn("link");
		node.setTarget(new EmfInstance());
		assertEquals("link", node.buildBookmarkableURL());
	}

	@Test
	public void getArray_nullProperty_emptyArray() {
		node.setTarget(new EmfInstance());
		node.add("test-property", null);
		String[] result = node.getArray("test-property");
		assertEquals(0, result.length);
	}

	@Test
	public void getArray_notNullCollection_arrayWithValues() {
		node.setTarget(new EmfInstance());
		node.add("test-property", (Serializable) Arrays.asList("property-value"));
		String[] result = node.getArray("test-property");
		assertEquals(1, result.length);
		assertEquals("property-value", result[0]);
	}

	@Test
	public void getEmailDistributionListTest() {
		node.setTarget(new EmfInstance());
		node.add(EMAIL_DISTRIBUTION_LIST, (Serializable) Arrays.asList("user-mail"));
		String[] result = node.getEmailDistributionList();
		assertEquals(1, result.length);
		assertEquals("user-mail", result[0]);
	}

	@Test
	public void getArray() {
		node.setTarget(new EmfInstance());
		node.add("test-property", (Serializable) Arrays.asList(1, 2));
		String[] result = node.getArray("test-property");
		assertEquals(0, result.length);
	}

}
