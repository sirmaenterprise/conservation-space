package com.sirma.itt.seip.template.observers;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.template.TemplateProperties;

/**
 * Test for {@link TemplateInstanceSaveObserverTest}.
 */
public class TemplateInstanceSaveObserverTest {

	@InjectMocks
	private TemplateInstanceSaveObserver observer;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private CodelistService codelistService;

	@Before
	public void init() {
		observer = new TemplateInstanceSaveObserver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_SetForObjectTypeLabelProperty() {
		final String TYPE = "book";
		final Integer CODELIST = 31;
		final String CODE_VALUE = "TEST";
		final String LABEL = "Book";

		withDefinition(TYPE, CODELIST, CODE_VALUE);

		withCodeValueDescription(CODELIST, CODE_VALUE, LABEL);

		EmfInstance instance = constructTemplateInstance(TYPE);

		BeforeInstanceSaveEvent event = new BeforeInstanceSaveEvent(instance, null, new Operation());

		observer.onInstanceSave(event);

		assertEquals(instance.getString(TemplateProperties.FOR_OBJECT_TYPE_LABEL), LABEL);
	}

	@Test
	public void should_NotSetForObjectTypeLabelProperty_TheDefinitionTypeIsNotBoundToCodelist() {
		final String TYPE = "book";
		final Integer CODELIST = null;
		final String CODE_VALUE = null;

		withDefinition(TYPE, CODELIST, CODE_VALUE);

		EmfInstance instance = constructTemplateInstance(TYPE);

		BeforeInstanceSaveEvent event = new BeforeInstanceSaveEvent(instance, null, new Operation());

		observer.onInstanceSave(event);

		assertNull(instance.getString(TemplateProperties.FOR_OBJECT_TYPE_LABEL));
	}

	@Test
	public void should_NotSetForObjectTypeLabelProperty_TheDefinitionDoesNotHaveALabel() {
		final String TYPE = "book";
		final Integer CODELIST = 31;
		final String CODE_VALUE = null;

		withDefinition(TYPE, CODELIST, CODE_VALUE);

		EmfInstance instance = constructTemplateInstance(TYPE);

		BeforeInstanceSaveEvent event = new BeforeInstanceSaveEvent(instance, null, new Operation());

		observer.onInstanceSave(event);

		assertNull(instance.getString(TemplateProperties.FOR_OBJECT_TYPE_LABEL));
	}

	@Test
	public void should_NotSetForObjectTypeLabelProperty_WhenInstanceIsNotTemplate() {
		EmfInstance instance = new EmfInstance();
		InstanceType instanceType = mock(InstanceType.class);
		instance.setType(instanceType);

		BeforeInstanceSaveEvent event = new BeforeInstanceSaveEvent(instance, null, new Operation());

		observer.onInstanceSave(event);

		assertNull(instance.getString(TemplateProperties.FOR_OBJECT_TYPE_LABEL));
	}

	private static EmfInstance constructTemplateInstance(String forObjectType) {
		EmfInstance instance = new EmfInstance();
		instance.add(TemplateProperties.FOR_OBJECT_TYPE, forObjectType);

		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is(ObjectTypes.TEMPLATE)).thenReturn(true);
		instance.setType(instanceType);

		return instance;
	}

	private void withCodeValueDescription(Integer codelist, String code, String description) {
		when(codelistService.getDescription(codelist, code)).thenReturn(description);
	}

	private void withDefinition(String type, Integer codelist, String code) {
		DefinitionModel definition = mock(DefinitionModel.class);
		when(definition.getIdentifier()).thenReturn(type);

		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getCodelist()).thenReturn(codelist);
		when(property.getDefaultValue()).thenReturn(code);

		when(definition.getField(eq(DefaultProperties.TYPE))).thenReturn(Optional.of(property));

		when(definitionService.find(type)).thenReturn(definition);
	}
}
