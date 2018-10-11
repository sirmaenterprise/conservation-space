package com.sirma.itt.seip.instance.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesDao;
import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.DatabaseIdManagerFake;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Unit test for {@link ObjectInstanceDao}
 *
 * @author BBonev
 */
public class ObjectInstanceDaoTest {

	@InjectMocks
	private ObjectInstanceDao instanceDao;

	@Mock
	private LinkService relationalLinkService;
	@Mock
	private LinkService linkService;
	@Spy
	private InstanceProxyMock<DbDao> semanticDbDao = new InstanceProxyMock<>();
	@Spy
	private Iterable<SemanticNonPersistentPropertiesExtension> semanticNonPersistentProperties = new ArrayList<>();
	@Mock
	private InstanceLoader instanceLoader;
	@Mock
	private DbDao dbDao;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private ServiceRegistry serviceRegistry;
	@Mock
	private EventService eventService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private ResourceService resourceService;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private InstanceTypes instanceTypes;

	@Mock
	private PropertiesService propertiesService;
	@Mock
	private ExpressionsManager evaluatorManager;
	@Mock
	private ObjectMapper dozerMapper;
	@Spy
	private DatabaseIdManager idManager = new DatabaseIdManagerFake();
	@Mock
	private SecurityContext securityContext;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	// not injected fields
	@Mock
	private InstancePersistCallback persistCollback;
	@Mock
	private EntityConverter entityConverter;
	@Mock
	private EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache;

	@Mock
	private RichtextPropertiesDao richtextPropertiesDao;

	@Spy
	private InstanceContextServiceMock contextService;
	@Spy
	private ChangedInstancesBuffer changedInstancesBuffer;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		semanticDbDao.set(dbDao);
		when(instanceLoader.getPersistCallback()).thenReturn(persistCollback);
		when(persistCollback.getCache()).thenReturn(cache);
		when(persistCollback.getEntityConverter()).thenReturn(entityConverter);

		when(entityConverter.convertToEntity(any())).then(a -> a.getArgumentAt(0, Instance.class));

		DefinitionMock definition = new DefinitionMock();
		definition.getFields().add(createProperty("prop1", false));
		definition.getFields().add(createProperty("objProp1", true));
		definition.getFields().add(createProperty("objProp2", true));
		definition.getFields().add(createProperty("objProp3", true));
		definition.getFields().add(createProperty("objProp4", true));
		definition.getFields().add(createProperty("objProp5", true));

		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);

		when(dbDao.saveOrUpdate(any(), any())).then(a -> a.getArgumentAt(0, Entity.class));
		instanceDao.initialize();
	}

	private static PropertyDefinitionMock createProperty(String name, boolean isObjectProperty) {
		PropertyDefinitionMock property = new PropertyDefinitionMock();
		property.setName(name);
		property.setUri("emf:" + name);
		if (isObjectProperty) {
			property.setType(DataTypeDefinition.URI);
		} else {
			property.setType(DataTypeDefinition.TEXT);
		}
		property.setDataType(mock(DataTypeDefinition.class));
		property.setDisplayType(DisplayType.EDITABLE);
		return property;
	}

	@Test
	public void testTouchInstance() throws Exception {
		instanceDao.touchInstance(new Object());
		instanceDao.touchInstance("emf:instanceId");
		instanceDao.touchInstance(Collections.singletonList("emf:instanceId"));
		instanceDao.touchInstance(InstanceReferenceMock.createGeneric("emf:instanceId"));
		instanceDao.touchInstance(InstanceReferenceMock.createGeneric("emf:instanceId").toInstance());

		verify(cache, times(4)).removeByKey("emf:instanceId");
	}

	@Test
	public void testPersistChanges_nonTrackableInstance() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		contextService.bindContext(instance, "emf:parent");
		instance.add("objProp1", "emf:ref-instance1");
		instance.add("emf:objProp5", "emf:ref-instance6");
		instance.add("objProp2", (Serializable) Collections.singletonList("emf:ref-instance2"));
		instance.add("objProp3",
				(Serializable) Arrays.asList("emf:ref-instance1", "emf:ref-instance2", "emf:ref-instance3"));
		instance.add("objProp2", (Serializable) Arrays.asList("emf:ref-instance2", "emf:ref-instance4"));
		instance.remove("objProp1");
		instance.add("emf:objProp4", "emf:ref-instance5");
		Options.CURRENT_OPERATION.set(new Operation("operation"));

		instanceDao.persistChanges(instance);

		// 6 times, as all referenced object properties are affected, excluding the current instance
		verify(cache, times(6)).removeByKey(anyString());
		verify(instanceLoader.getPersistCallback()).persistAndUpdateCache(any());
	}

	@Test
	public void testPersistChanges() throws Exception {
		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:instance");
		contextService.bindContext(instance, "emf:parent");
		instance.add("objProp1", "emf:ref-instance1");
		instance.add("emf:objProp5", "emf:ref-instance6");
		instance.add("objProp2", (Serializable) Collections.singletonList("emf:ref-instance2"));
		instance.enableChangesTracking();
		instance.add("objProp3",
				(Serializable) Arrays.asList("emf:ref-instance1", "emf:ref-instance2", "emf:ref-instance3"));
		instance.add("objProp2", (Serializable) Arrays.asList("emf:ref-instance2", "emf:ref-instance4"));
		instance.remove("objProp1");
		instance.add("emf:objProp4", "emf:ref-instance5");
		Options.CURRENT_OPERATION.set(new Operation("operation"));

		instanceDao.persistChanges(instance);

		// 5 times, because emf:objProp5 is added before changes tracking
		verify(cache, times(5)).removeByKey(anyString());
		verify(instanceLoader.getPersistCallback()).persistAndUpdateCache(any());
	}
}
