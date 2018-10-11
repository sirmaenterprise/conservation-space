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

		RichtextPropertyEntity expected = new RichtextPropertyEntity();
		expected.setId(Long.valueOf(12));
		expected.setInstanceId(INSTANCE_ID);
		expected.setPropertyId(PROPERTY_ID);
		expected.setContent("<b>Old value</b>");

		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(Collections.singletonList(expected));
		when(sanitizer.sanitize(CONTENT)).thenReturn(CONTENT);

		richtextPropertiesDao.saveOrUpdate(INSTANCE_ID, PROPERTY_ID, CONTENT);

		ArgumentCaptor<RichtextPropertyEntity> captor = ArgumentCaptor.forClass(RichtextPropertyEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());

		assertEquals(expected, captor.getValue());
		assertEquals(CONTENT, captor.getValue().getContent());
	}

	@Test
	public void should_Save_Property() {

		RichtextPropertyEntity expected = new RichtextPropertyEntity();
		expected.setInstanceId(INSTANCE_ID);
		expected.setPropertyId(PROPERTY_ID);
		expected.setContent(CONTENT);

		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(Collections.emptyList());

		richtextPropertiesDao.saveOrUpdate(INSTANCE_ID, PROPERTY_ID, CONTENT);

		ArgumentCaptor<RichtextPropertyEntity> captor = ArgumentCaptor.forClass(RichtextPropertyEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());

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

		RichtextPropertyEntity richtextProperty = createRichtextProperty();

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

		RichtextPropertyEntity richtextProperty = createRichtextProperty();

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

	private static RichtextPropertyEntity createRichtextProperty() {
		RichtextPropertyEntity richtextProperty = new RichtextPropertyEntity();
		richtextProperty.setId(Long.valueOf(12));
		richtextProperty.setInstanceId(INSTANCE_ID);
		richtextProperty.setPropertyId(PROPERTY_ID);
		richtextProperty.setContent(CONTENT);
		return richtextProperty;
	}

}
