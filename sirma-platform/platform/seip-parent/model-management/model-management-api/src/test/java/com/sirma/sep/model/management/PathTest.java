package com.sirma.sep.model.management;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Test for {@link Path}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/07/2018
 */
public class PathTest {

	private static final String TEST_PATH_3_STEPS = "step1=id1/step2=id2/step3=id3";

	@Test
	public void parsePath_shouldHandleSingleElement() {
		Path path = Path.parsePath("step1=id1");
		assertNotNull(path);
		assertEquals("step1", path.getName());
		assertEquals("id1", path.getValue());
		assertNull(path.next());
		assertNull(path.previous());
	}

	@Test
	public void parsePath() {
		Path path = Path.parsePath(TEST_PATH_3_STEPS);
		assertNotNull(path);
		assertNull(path.previous());
		assertEquals("step1", path.getName());
		assertEquals("id1", path.getValue());
		Path step2 = path.next();
		assertEquals("step2", step2.getName());
		assertEquals("id2", step2.getValue());
		Path step3 = step2.next();
		assertEquals("step3", step3.getName());
		assertEquals("id3", step3.getValue());
		assertNull(step3.next());
	}

	@Test
	public void parsePath_shouldAcceptEmptyString() {
		Path path = Path.parsePath("");

		assertTrue(path.isEmpty());
		assertNull(path.next());
		assertNull(path.previous());
	}

	@Test
	public void parsePath_shouldAcceptForwardSlashOnly() {
		Path path = Path.parsePath("/");

		assertTrue(path.isEmpty());
		assertNull(path.next());
		assertNull(path.previous());
	}

	@Test
	public void toString_ShouldReturnThePathString() {
		Path path = Path.parsePath("/");
		assertEquals("Empty path should always resolve to '/'", "/", path.toString());

		path = Path.parsePath(TEST_PATH_3_STEPS);
		assertEquals("/step1=id1/step2=id2/step3=id3", path.toString());

		path = Path.parsePath(TEST_PATH_3_STEPS);
		assertEquals("/step2=id2/step3=id3", path.next().toString());

		path = Path.parsePath(TEST_PATH_3_STEPS);
		assertEquals("/step3=id3", path.next().next().toString());
	}

	@Test
	public void tail_ShouldReturnTheLastPathElement() {
		Path path = Path.parsePath(TEST_PATH_3_STEPS);
		assertEquals("/step3=id3", path.tail().toString());
	}

	@Test
	public void head_ShouldReturnTheFirstPathElement() {
		Path path = Path.parsePath(TEST_PATH_3_STEPS);
		assertEquals("/step3=id3", path.tail().toString());
		assertEquals("/step1=id1/step2=id2/step3=id3", path.tail().head().toString());
	}

	@Test
	public void walk_shouldByAbleToReachTheLastItem() {
		Path path = Path.parsePath("step1=id1/step2=id2");
		assertEquals("v2", path.walk(p1 -> p1.proceed((Walkable) p2 -> p2.proceed("v2"))));
	}

	@Test
	public void walk_shouldByAbleToTraverseModels() {
		Models models = new Models();
		models.setClasses(new HashMap<>());
		models.setDefinitions(new HashMap<>());
		models.setProperties(new HashMap<>());
		ModelsMetaInfo modelsMetaInfo = new ModelsMetaInfo();
		modelsMetaInfo.setFields(Collections.singletonList(new ModelMetaInfo().setId("displayType")));
		models.setModelsMetaInfo(modelsMetaInfo);
		Object result = Path.parsePath("definition=PR0001/field=title/attribute=displayType").walk(models);
		assertTrue(result instanceof ModelAttribute);
	}

	@Test(expected = IllegalStateException.class)
	public void proceed_shouldFailOnNonWalkableElementWithRemainingSteps() {
		Path path = Path.parsePath("step1=id1/step2=id2");
		path.walk(p1 -> p1.proceed("v2"));
	}

	@Test
	public void cutOffTail_shouldReturnCopyWithoutRemainingElements() {
		Path path = Path.parsePath(TEST_PATH_3_STEPS);
		Path copy = path.tail().previous().cutOffTail();
		assertEquals("/step1=id1/step2=id2", copy.toString());
	}

	@Test
	public void cutOffTail_shouldReturnTheSameElementIfCalledOnTheLastNode() {
		Path path = Path.parsePath("step1=id1");
		Path copy = path.cutOffTail();
		assertEquals("/step1=id1", copy.toString());
		assertNotSame(path, copy);
	}

	@Test
	public void appendPath_shouldProvideNewPathConsistingOfTheTwo() {
		ModelDefinition definition = new ModelDefinition();
		definition.setId("definition1");
		ModelField modelField = new ModelField();
		modelField.setId("field1");
		modelField.addAttribute("someAttribute", "someValue");
		ModelAttribute attribute = modelField.getAttribute("someAttribute").get();
		definition.addField(modelField);
		assertEquals("/definition=definition1/field=field1/attribute=someAttribute", attribute.getPath().toString());
	}

	@Test
	public void appendPath_shouldProvideNewPathConsistingOfTheTwo_real() {
		Path def = Path.create("definition", "definition1");
		Path field = Path.create("field", "field1");
		Path attribute = Path.create("attribute", "someAttribute");

		assertEquals("/definition=definition1/field=field1", def.append(field).toString());
		assertEquals("/definition=definition1/field=field1/attribute=someAttribute", def.append(field).append(attribute).toString());
	}
}
