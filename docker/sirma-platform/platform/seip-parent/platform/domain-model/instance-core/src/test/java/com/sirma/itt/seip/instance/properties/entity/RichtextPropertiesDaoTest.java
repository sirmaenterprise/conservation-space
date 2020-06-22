package com.sirma.itt.seip.instance.properties.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesDao;
import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Tests the functionality of {@link RichtextPropertiesDaoImpl}.
 *
 * @author S.Djulgerova
 */
public class RichtextPropertiesDaoTest {

	private static final String INSTANCE_ID = "emf:123456";
	private static final Long PROPERTY_ID = Long.valueOf(56);
	private static final String PROPERTY_NAME = "description";
	private static final String CONTENT = "<b> This </b> <i> is </i> test";
	private static final String SANITIZED_CONTENT = "This is test";


	@InjectMocks
	private RichtextPropertiesDao richtextPropertiesDao;

	@Mock
	DefinitionService definitionService;

	@Mock
	IdocSanitizer sanitizer;

	@Mock
	private DbDao dbDao;

	@Before
	public void setup() {
		richtextPropertiesDao = new RichtextPropertiesDaoImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Update_Property() {
		RichtextPropertyEntity oldValue = createRichtextProperty(12l, INSTANCE_ID, PROPERTY_ID, "<b>Old actualValue</b>");
		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(Collections.singletonList(oldValue));
		when(sanitizer.sanitize(CONTENT)).thenReturn(SANITIZED_CONTENT);

		richtextPropertiesDao.saveOrUpdate(INSTANCE_ID, PROPERTY_ID, CONTENT);

		ArgumentCaptor<RichtextPropertyEntity> captor = ArgumentCaptor.forClass(RichtextPropertyEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());

		RichtextPropertyEntity expected = createRichtextProperty(12l, INSTANCE_ID, PROPERTY_ID, SANITIZED_CONTENT);
		RichtextPropertyEntity actualValue = captor.getValue();
		assertEquals(expected, actualValue);
		assertEquals(SANITIZED_CONTENT, actualValue.getContent());
	}

	@Test
	public void should_Save_Property() {
		when(sanitizer.sanitize(CONTENT)).thenReturn(SANITIZED_CONTENT);
		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(Collections.emptyList());

		richtextPropertiesDao.saveOrUpdate(INSTANCE_ID, PROPERTY_ID, CONTENT);

		ArgumentCaptor<RichtextPropertyEntity> captor = ArgumentCaptor.forClass(RichtextPropertyEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());

		// its new entity so it has no dbid
		RichtextPropertyEntity expected = createRichtextProperty(null, INSTANCE_ID, PROPERTY_ID, SANITIZED_CONTENT);
		assertEquals(expected, captor.getValue());
	}

	@Test
	public void should_Save_Property_with_empty_content() {
		when(sanitizer.sanitize(null)).thenThrow(IllegalArgumentException.class);
		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(Collections.emptyList());

		richtextPropertiesDao.saveOrUpdate(INSTANCE_ID, PROPERTY_ID, null);

		ArgumentCaptor<RichtextPropertyEntity> captor = ArgumentCaptor.forClass(RichtextPropertyEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());

		// its new entity so it has no dbid
		RichtextPropertyEntity expected = createRichtextProperty(null, INSTANCE_ID, PROPERTY_ID, null);
		assertEquals(expected, captor.getValue());
	}

	@Test
	public void should_Delete_Property() {
		RichtextPropertyEntity existing = new RichtextPropertyEntity();
		existing.setId(Long.valueOf(12));
		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(Collections.singletonList(existing));

		richtextPropertiesDao.delete(INSTANCE_ID);
		verify(dbDao, times(1)).executeUpdate(any(String.class), any(List.class));
	}

	@Test
	public void should_Fetch_By_Id() {
		RichtextPropertyEntity richtextProperty = createRichtextProperty(12l, INSTANCE_ID, PROPERTY_ID, CONTENT);

		when(dbDao.fetchWithNamed(anyString(), any(List.class)))
				.thenReturn(Collections.singletonList(richtextProperty));

		PrototypeDefinition prototypeDefinition = Mockito.mock(PrototypeDefinition.class);

		when(prototypeDefinition.getIdentifier()).thenReturn(PROPERTY_NAME);
		when(definitionService.getProperty(PROPERTY_ID)).thenReturn(prototypeDefinition);

		Map<String, Serializable> properties = richtextPropertiesDao.fetchByInstanceId(INSTANCE_ID);
		assertEquals(CONTENT, properties.get(PROPERTY_NAME));
	}

	@Test
	public void should_Fetch_By_Ids() {
		RichtextPropertyEntity richtextProperty = createRichtextProperty(12l, INSTANCE_ID, PROPERTY_ID, CONTENT);

		when(dbDao.fetchWithNamed(anyString(), any(List.class)))
				.thenReturn(Collections.singletonList(richtextProperty));

		PrototypeDefinition prototypeDefinition = Mockito.mock(PrototypeDefinition.class);

		when(prototypeDefinition.getIdentifier()).thenReturn(PROPERTY_NAME);
		when(definitionService.getProperty(PROPERTY_ID)).thenReturn(prototypeDefinition);

		Map<String, Map<String, Serializable>> properties = richtextPropertiesDao
				.fetchByInstanceIds(Arrays.asList(INSTANCE_ID));

		Map<String, Map<String, Serializable>> expected = new HashMap<>();
		Map<String, Serializable> props = new HashMap<>();
		props.put(PROPERTY_NAME, CONTENT);
		expected.put(INSTANCE_ID, props);
		assertEquals(expected, properties);
	}

	private static RichtextPropertyEntity createRichtextProperty(Long dbid, String instanceId, Long propertyId, String content) {
		RichtextPropertyEntity richtextProperty = new RichtextPropertyEntity();
		richtextProperty.setId(dbid);
		richtextProperty.setInstanceId(instanceId);
		richtextProperty.setPropertyId(propertyId);
		richtextProperty.setContent(content);
		return richtextProperty;
	}

}
