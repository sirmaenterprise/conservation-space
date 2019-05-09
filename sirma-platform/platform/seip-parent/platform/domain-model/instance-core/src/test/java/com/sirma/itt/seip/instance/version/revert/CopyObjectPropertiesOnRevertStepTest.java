package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link CopyObjectPropertiesOnRevertStep}.
 *
 * @author A. Kunchev
 */
public class CopyObjectPropertiesOnRevertStepTest {

	@InjectMocks
	private CopyObjectPropertiesOnRevertStep step;

	@Mock
	private DefinitionService definitionService;
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;
	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		step = new CopyObjectPropertiesOnRevertStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("copyObjectProperties", step.getName());
	}

	@Test
	public void invoke_withDefinition_propertiesTransferred() {
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(TYPE, "current-instance-type");

		Instance result = new EmfInstance();
		result.setId("instance-id-v1.6");
		result.add(TYPE, "version-instnace-type");
		result.add(NAME, "Bruce Wayne");

		when(definitionService.getInstanceObjectProperties(current))
				.thenReturn(Stream.of(buildField("type"), buildField("name")));
		when(definitionService.getInstanceObjectProperties(result))
				.thenReturn(Stream.of(buildField("type"), buildField("name")));

		RevertContext context = RevertContext
				.create("instance-id-v1.6")
					.setCurrentInstance(current)
					.setRevertResultInstance(result);
		step.invoke(context);

		assertEquals(1, context.getRevertResultInstance().getProperties().size());
		assertEquals("current-instance-type", context.getRevertResultInstance().getString(TYPE));
	}

	@Test
	public void invoke_withDefinition_ShouldNotResetRdfType() {
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(SEMANTIC_TYPE, EMF.DOCUMENT.toString());

		Instance result = new EmfInstance();
		result.setId("instance-id-v1.6");
		result.add(SEMANTIC_TYPE, EMF.MEDIA.toString());

		when(definitionService.getInstanceObjectProperties(current))
				.thenReturn(Stream.of(buildField(SEMANTIC_TYPE)));
		when(definitionService.getInstanceObjectProperties(result))
				.thenReturn(Stream.of(buildField(SEMANTIC_TYPE)));

		RevertContext context = RevertContext
				.create("instance-id-v1.6")
				.setCurrentInstance(current)
				.setRevertResultInstance(result);
		step.invoke(context);

		assertEquals(1, context.getRevertResultInstance().getProperties().size());
		assertEquals(EMF.MEDIA.toString(), context.getRevertResultInstance().getString(SEMANTIC_TYPE));
	}

	@Test
	public void invoke_shouldKeepAndConvertNonMatchingPropertiesDueToTypeChange() {
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(TYPE, "current-instance-type");

		Instance result = new EmfInstance();
		result.setId("instance-id-v1.6");
		result.add(TYPE, "version-instnace-type");
		result.add("hasAssignee", "emf:testUser-v1.2");
		result.add(NAME, "Bruce Wayne");

		when(definitionService.getInstanceObjectProperties(current))
				.thenReturn(Stream.of(buildField("type"), buildField("name")));
		when(definitionService.getInstanceObjectProperties(result))
				.thenReturn(Stream.of(buildField("type"), buildField("name"), buildField("hasAssignee")));

		RevertContext context = RevertContext
				.create("instance-id-v1.6")
				.setCurrentInstance(current)
				.setRevertResultInstance(result);
		step.invoke(context);

		assertEquals(2, context.getRevertResultInstance().getProperties().size());
		assertEquals("current-instance-type", context.getRevertResultInstance().getString(TYPE));
		assertEquals("emf:testUser", context.getRevertResultInstance().getString("hasAssignee"));
	}

	@Test
	public void invoke_shouldRevertTemplateIfCurrentIsNotApplicable() {
		Instance current = new EmfInstance();
		current.setIdentifier("currentType");
		current.setId("instance-id");
		current.add(LinkConstants.HAS_TEMPLATE, "current-template-id");

		Instance result = new EmfInstance();
		result.setIdentifier("oldType");
		result.setId("instance-id-v1.6");
		result.add(LinkConstants.HAS_TEMPLATE, "old-template-id-v1.2");

		Instance template = new EmfInstance();
		template.setId("current-template-id");
		template.add(TemplateProperties.EMF_FOR_OBJECT_TYPE, "currentType");
		when(instanceService.loadByDbId("current-template-id")).thenReturn(template);

		when(definitionService.getInstanceObjectProperties(current))
				.thenReturn(Stream.of(buildField("type"), buildField("name")));
		when(definitionService.getInstanceObjectProperties(result))
				.thenReturn(Stream.of(buildField("type"), buildField("name"), buildField("hasAssignee")));

		RevertContext context = RevertContext
				.create("instance-id-v1.6")
				.setCurrentInstance(current)
				.setRevertResultInstance(result);
		step.invoke(context);

		assertEquals(1, context.getRevertResultInstance().getProperties().size());
		assertEquals("old-template-id", context.getRevertResultInstance().getString(LinkConstants.HAS_TEMPLATE));
	}

	@Test
	public void invoke_shouldNotRevertTemplateIfCurrentIsApplicable() {
		Instance current = new EmfInstance();
		current.setIdentifier("currentType");
		current.setId("instance-id");
		current.add(LinkConstants.HAS_TEMPLATE, "current-template-id");

		Instance result = new EmfInstance();
		result.setIdentifier("currentType");
		result.setId("instance-id-v1.6");
		result.add(LinkConstants.HAS_TEMPLATE, "current-template-id-v1.2");

		Instance template = new EmfInstance();
		template.setId("current-template-id");
		template.add(TemplateProperties.EMF_FOR_OBJECT_TYPE, "currentType");
		when(instanceService.loadByDbId("current-template-id")).thenReturn(template);

		when(definitionService.getInstanceObjectProperties(current))
				.thenReturn(Stream.of(buildField("type"), buildField("name")));
		when(definitionService.getInstanceObjectProperties(result))
				.thenReturn(Stream.of(buildField("type"), buildField("name"), buildField("hasAssignee")));

		RevertContext context = RevertContext
				.create("instance-id-v1.6")
				.setCurrentInstance(current)
				.setRevertResultInstance(result);
		step.invoke(context);

		assertEquals(1, context.getRevertResultInstance().getProperties().size());
		assertEquals("current-template-id", context.getRevertResultInstance().getString(LinkConstants.HAS_TEMPLATE));
	}

	private static PropertyDefinition buildField(String identifier) {
		PropertyDefinitionMock type = new PropertyDefinitionMock();
		type.setIdentifier(identifier);
		type.setName(identifier);
		type.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		return type;
	}
}
