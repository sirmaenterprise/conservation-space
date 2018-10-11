package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceExistResult;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link ProcessObjectPropertiesVersionStep}.
 *
 * @author A. Kunchev
 */
public class ProcessObjectPropertiesVersionStepTest {

	@InjectMocks
	private ProcessObjectPropertiesVersionStep step;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private VersionDao versionDao;

	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		step = new ProcessObjectPropertiesVersionStep();
		MockitoAnnotations.initMocks(this);

		when(instanceService.exist(anyCollectionOf(Serializable.class))).then(a -> {
			Collection<Serializable> ids = a.getArgumentAt(0, Collection.class);
			return new InstanceExistResult<>(
					ids.stream().collect(Collectors.toMap(Function.identity(), id -> Boolean.TRUE)));
		});

		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class)))
				.then(a -> a.getArgumentAt(0, Collection.class).stream().collect(
						Collectors.toMap(Function.identity(), value -> value + "-v1.0")));

		when(typeConverter.convert(eq(ShortUri.class), anyCollectionOf(Serializable.class))).then(a -> {
			Collection<Serializable> ids = a.getArgumentAt(1, Collection.class);
			Collection<Serializable> result = new ArrayList<>(ids.size());
			for (Serializable id : ids) {
				if (id.toString().contains("#")) {
					result.add(new ShortUri("emf:" + id.toString().split("#")[1]));
				} else {
					result.add(new ShortUri(id.toString()));
				}
			}

			return result;
		});
	}

	@Test
	public void getName() {
		assertEquals("processObjectPropertiesVersion", step.getName());
	}

	@Test
	public void execute_objectPropertiesVersioningDisabled_servicesNotCalled() {
		VersionContext context = VersionContext.create(new EmfInstance()).disableObjectPropertiesVersioning();
		step.execute(context);
		verifyZeroInteractions(definitionService, versionDao, instanceService);
	}

	@Test(expected = EmfRuntimeException.class)
	public void execute_missingVersionInstance_servicesNotCalled() {
		VersionContext context = VersionContext.create(new EmfInstance());
		step.execute(context);
		verifyZeroInteractions(definitionService, versionDao, instanceService);
	}

	@Test
	public void execute_noObjectProperties_versionDaoNotCalled() {
		when(definitionService.getInstanceObjectProperties(any(Instance.class))).thenReturn(Stream.empty());

		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(new ArchivedInstance());
		step.execute(context);
		verifyZeroInteractions(versionDao, instanceService);
	}

	@Test
	public void execute_withNullValueObjectPropertiy_versionDaoNotCalled() {
		stubDefinitionService("object-property", Boolean.FALSE);

		Instance version = new ArchivedInstance();
		version.add("object-property", null);
		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		step.execute(context);
		verifyZeroInteractions(versionDao, instanceService);
	}

	@Test
	public void execute_objectPropertiyWithSingleValueFullUri_versionDaoCalledNoResults() {
		stubDefinitionService("single-object-property", Boolean.FALSE);

		Instance version = new ArchivedInstance();
		version.add("single-object-property", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#id");
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class)))
				.thenReturn(emptyMap());
		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		step.execute(context);
		assertEquals("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#id",
				context.getVersionInstance().get().getString("single-object-property"));
	}

	@Test
	public void execute_objectPropertiyWithSingleValue_versionDaoCalled() {
		stubDefinitionService("single-object-property", Boolean.FALSE);

		Instance version = new ArchivedInstance();
		version.add("single-object-property", "emf:instance-id");
		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		step.execute(context);
		verify(versionDao).findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class));
		verify(instanceService).exist(anyCollectionOf(Serializable.class));
		assertEquals("emf:instance-id-v1.0", context.getVersionInstance().get().getString("single-object-property"));
	}

	@Test
	public void execute_objectPropertiyWithSingleValue_failedToFindVersion() {
		stubDefinitionService("single-object-property", Boolean.FALSE);

		Instance version = new ArchivedInstance();
		version.add("single-object-property", "emf:instance-id");

		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollection(), eq(context.getCreationDate())))
				.thenReturn(emptyMap());

		step.execute(context);
		verify(versionDao).findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class));
		verify(instanceService).exist(anyCollectionOf(Serializable.class));
		assertEquals("emf:instance-id", context.getVersionInstance().get().getString("single-object-property"));
	}

	@Test
	public void execute_multivalueObjectPropertiyWithEmptyCollectionValue_versionDaoNotCalled() {
		stubDefinitionService("multi-object-property", Boolean.TRUE);

		Instance version = new ArchivedInstance();
		version.add("multi-object-property", (Serializable) emptyList());

		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		step.execute(context);
		verifyZeroInteractions(versionDao, instanceService);
	}

	@Test
	public void execute_multivalueObjectPropertiyWithValue_failedToFindAllVersions() {
		stubDefinitionService("multi-object-property", Boolean.TRUE);

		Instance version = new ArchivedInstance();
		List<Serializable> ids = Arrays.asList("emf:instance-id-1", "emf:instance-id-2");
		version.add("multi-object-property", (Serializable) ids);

		Map<Serializable, Serializable> versionDaoResult = new HashMap<>(1);
		versionDaoResult.put("emf:instance-id-1", "emf:instance-id-1-v1.0");
		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollection(), eq(context.getCreationDate())))
				.thenReturn(versionDaoResult);

		step.execute(context);
		verify(versionDao).findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class));
		verify(instanceService).exist(anyCollectionOf(Serializable.class));
		assertEquals(Arrays.asList("emf:instance-id-1-v1.0", "emf:instance-id-2"), context
				.getVersionInstance()
					.get()
					.getAsCollection("multi-object-property", () -> Collections.emptyList()));
	}

	@Test
	public void execute_multivalueObjectPropertiyWithValue_versionDaoCalled() {
		stubDefinitionService("multi-object-property", Boolean.TRUE);

		Instance version = new ArchivedInstance();
		version.add("multi-object-property", (Serializable) Arrays.asList("emf:instance-id-1", "emf:instance-id-2"));
		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(version);
		step.execute(context);
		verify(versionDao).findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class));
		verify(instanceService).exist(anyCollectionOf(Serializable.class));
		assertEquals(Arrays.asList("emf:instance-id-2-v1.0", "emf:instance-id-1-v1.0"), context
				.getVersionInstance()
					.get()
					.getAsCollection("multi-object-property", () -> Collections.emptyList()));
	}

	// weird case that instance data validation should not allow, but we handle it anyway
	@Test
	public void execute_multivalueObjectPropertiyAsSingleValue_versionDaoCalled() {
		stubDefinitionService("multi-object-property", Boolean.TRUE);

		Instance verion = new ArchivedInstance();
		verion.add("multi-object-property", "emf:instance-id-1");
		VersionContext context = VersionContext.create(new EmfInstance()).setVersionInstance(verion);
		step.execute(context);
		verify(versionDao).findVersionIdsByTargetIdAndDate(anyCollectionOf(Serializable.class), any(Date.class));
		verify(instanceService).exist(anyCollectionOf(Serializable.class));
		assertEquals("emf:instance-id-1-v1.0", context.getVersionInstance().get().get("multi-object-property"));
	}

	private void stubDefinitionService(String propertyName, Boolean multiValued) {
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		propertyDefinition.setMultiValued(multiValued);
		propertyDefinition.setName(propertyName);
		when(definitionService.getInstanceObjectProperties(any(Instance.class)))
				.thenReturn(Stream.of(propertyDefinition));
	}
}
