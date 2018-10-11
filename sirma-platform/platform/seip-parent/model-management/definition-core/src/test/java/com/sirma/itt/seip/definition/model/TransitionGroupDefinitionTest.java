package com.sirma.itt.seip.definition.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;

public class TransitionGroupDefinitionTest {

	private TransitionGroupDefinitionImpl definition;

	@Before
	public void init() {
		definition = new TransitionGroupDefinitionImpl();
	}

	@Test
	public void should_SupportMergeFromAnotherGroupDefinition() {
		TransitionGroupDefinitionImpl source = new TransitionGroupDefinitionImpl();

		final String LABEL = "gr1.label";
		final String DIVIDER = "divider";

		source.setLabelId(LABEL);
		source.setOrder(1);
		source.setType("menu");
		source.setParent("parent1");

		definition.setType(DIVIDER);
		definition.setParent("parent2");

		definition.mergeFrom(source);

		assertEquals(definition.getLabelId(), LABEL);
		assertEquals(definition.getOrder().intValue(), 1);
		assertEquals(definition.getType(), DIVIDER);
		assertEquals(definition.getParent(), "parent2");
	}

	@Test
	public void should_ProvideToStringFuction() {
		definition.setLabelId("label");
		definition.setOrder(1);
		definition.setType("menu");
		definition.setParent("parent1");

		String toString = definition.toString();
		assertTrue(toString.contains("labelId=label"));
		assertTrue(toString.contains("order=1"));
		assertTrue(toString.contains("type=menu"));
		assertTrue(toString.contains("parent=parent1"));
	}

	@Test
	public void should_SetIdentifier_WhenNotSealed() {
		definition.setIdentifier("1");

		assertEquals(definition.getIdentifier(), "1");
	}

	@Test
	public void should_NotSetIdentifier_WhenNotSealed() {
		definition.setIdentifier("1");
		definition.seal();
		definition.setIdentifier("2");

		assertEquals(definition.getIdentifier(), "1");
	}

	@Test
	public void should_SupportCloneOperation() {
		definition.setIdentifier("id1");
		definition.setLabelId("label");
		definition.setOrder(1);
		definition.setType("menu");
		definition.setParent("parent1");

		TransitionGroupDefinitionImpl cloning = definition.createCopy();

		assertEquals(cloning.getIdentifier(), definition.getIdentifier());
		assertEquals(cloning.getLabelId(), definition.getLabelId());
		assertEquals(cloning.getOrder(), definition.getOrder());
		assertEquals(cloning.getType(), definition.getType());
		assertEquals(cloning.getParent(), definition.getParent());

		assertTrue(cloning.equals(definition));
		assertEquals(cloning.hashCode(), definition.hashCode());
	}

	@Test
	public void should_ProvideDefinitionLabelUsingLabelProvider() {
		final String LABEL = "lbl";
		final String LABEL_TEXT = "This is label";

		LabelProvider labelProvider = mock(LabelProvider.class);
		when(labelProvider.getLabel(LABEL)).thenReturn(LABEL_TEXT);

		definition.setLabelId(LABEL);
		definition.setLabelProvider(labelProvider);

		assertEquals(definition.getLabel(), LABEL_TEXT);
	}

	@Test
	public void should_ProvideNoLabel_WhenLabelIdIsNotSet() {
		final String LABEL = "lbl";
		final String LABEL_TEXT = "This is label";

		LabelProvider labelProvider = mock(LabelProvider.class);
		when(labelProvider.getLabel(LABEL)).thenReturn(LABEL_TEXT);

		definition.setLabelProvider(labelProvider);

		assertEquals(definition.getLabel(), null);
	}

	@Test
	public void should_ProvideNoLabel_WhenNoProviderIsSet() {
		assertEquals(definition.getLabel(), null);
	}

}
