package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Sections;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerBuilder;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNodeBuilder;

/**
 * Test for {@link Sections}.
 *
 * @author A. Kunchev
 */
public class SectionsTest {

	private Idoc idoc;

	@BeforeClass
	public static void beforeClass() {
		ContentNodeFactory factory = ContentNodeFactory.getInstance();
		factory.registerBuilder(new LayoutManagerBuilder());
		factory.registerBuilder(new LayoutNodeBuilder());
	}

	@Before
	public void setup() {
		try (InputStream inStream = SectionsTest.class.getResourceAsStream("/sections-test-data.html")) {
			idoc = Idoc.parse(inStream);
		} catch (IOException e) {
			throw new RuntimeException("There was a problem with the idoc parsing.", e);
		}
	}

	@Test
	public void should_AddACollectionOfSectionNodes() {
		List<SectionNode> sectionNodes = new ArrayList<>();
		sectionNodes.add(buildSection("s1"));
		sectionNodes.add(buildSection("s2"));
		sectionNodes.add(buildSection("s3"));

		Sections sections = idoc.getSections();
		sections.removeAll();

		sections.addAll(sectionNodes);

		assertEquals(3, sections.count());

		assertEquals("s1", sections.getSectionByIndex(0).getId());
		assertEquals("s2", sections.getSectionByIndex(1).getId());
		assertEquals("s3", sections.getSectionByIndex(2).getId());
	}

	@Test(expected = NullPointerException.class)
	public void addSectionAt_nullSection() {
		Sections sections = idoc.getSections();
		sections.addSectionAt(0, null);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void addSectionAt_incorrectIndex() {
		Sections sections = idoc.getSections();
		sections.addSectionAt(-10, buildSection("section-id"));
	}

	@Test(expected = NullPointerException.class)
	public void addSectionAt_wrongArgs() {
		Sections sections = idoc.getSections();
		sections.addSectionAt(-1, null);
	}

	@Test
	public void addSectionAt_asFirst() {
		Sections sections = idoc.getSections();
		SectionNode testSection = buildSection("first-section");
		sections.addSectionAt(0, testSection);
		assertEquals("first-section", sections.getSectionByIndex(0).getId());
	}

	@Test
	public void addSectionAt_asSecond() {
		Sections sections = idoc.getSections();
		SectionNode testSection = buildSection("second-section");
		sections.addSectionAt(1, testSection);
		assertEquals("second-section", sections.getSectionByIndex(1).getId());
	}

	@Test
	public void addFirstOnEmptySections() throws Exception {
		Idoc doc = Idoc.parse("");
		SectionNode section = buildSection("test-section");
		doc.getSections().addFirst(section);
		Idoc copy = doc.deepCopy();
		Optional<SectionNode> sectionById = copy.getSections().getSectionById("test-section");
		assertTrue(sectionById.isPresent());
		SectionNode node = copy.getSections().getSectionByIndex(0);
		assertNotNull(node);
	}

	@Test
	public void addAllAfter_wrongSectionId_addedLast() {
		Sections sections = idoc.getSections();
		boolean result = sections.addAfter("some-section-id",
				Arrays.asList(buildSection("section-id-1"), buildSection("section-id-2")));
		assertTrue(result);
		Idoc idocCopy = Idoc.parse(idoc.asHtml());
		Sections copy = idocCopy.getSections();
		int size = (int) copy.stream().count();
		SectionNode node = copy.getSectionByIndex(size - 2);
		assertNotNull(node);
		assertEquals("section-id-1", node.getId());
		node = copy.getSectionByIndex(size - 1);
		assertNotNull(node);
		assertEquals("section-id-2", node.getId());
	}

	@Test
	public void addAllAfter_nullCollection_notAdded() {
		Sections sections = idoc.getSections();
		boolean result = sections.addAfter("some-section-id", null);
		assertFalse(result);
	}

	@Test
	public void addAllAfter_emptyCollection_notAdded() {
		Sections sections = idoc.getSections();
		boolean result = sections.addAfter("some-section-id", emptyList());
		assertFalse(result);
	}

	@Test
	public void addAllAfter_added() {
		Sections sections = idoc.getSections();
		boolean result = sections.addAfter("d5d9b193-8b41-4faf-841e-46b00cd27228",
				Arrays.asList(buildSection("section-id-1"), buildSection("section-id-2")));
		assertTrue(result);
		List<String> newIdOrder = idoc
				.deepCopy()
					.getSections()
					.stream()
					.map(SectionNode::getId)
					.collect(Collectors.toList());
		// the new sections should be after the given section
		assertEquals(1,
				newIdOrder.indexOf("section-id-1") - newIdOrder.indexOf("d5d9b193-8b41-4faf-841e-46b00cd27228"));
		assertEquals(2,
				newIdOrder.indexOf("section-id-2") - newIdOrder.indexOf("d5d9b193-8b41-4faf-841e-46b00cd27228"));
		assertEquals(3, newIdOrder.indexOf("af6843e2-c0cd-4303-cac7-8ab89e8b3d69")
				- newIdOrder.indexOf("d5d9b193-8b41-4faf-841e-46b00cd27228"));
	}

	@Test
	public void removeSection_nullId() {
		boolean removed = idoc.getSections().removeById(null);
		assertFalse(removed);
	}

	@Test
	public void removeSection_emptyId() {
		boolean removed = idoc.getSections().removeById("");
		assertFalse(removed);
	}

	@Test
	public void removeSection_noSectionWithSuchId() {
		boolean removed = idoc.getSections().removeById("some-section-id");
		assertFalse(removed);
	}

	@Test
	public void removeSection_successful() {
		boolean removed = idoc.getSections().removeById("d5d9b193-8b41-4faf-841e-46b00cd27228");
		assertTrue(removed);

		Elements selectedSections = idoc.getContent().select("section");
		assertEquals(5, selectedSections.size());
	}

	@Test
	public void removeAllNodes_nullCollection() {
		boolean result = idoc.getSections().removeAll(null);
		assertFalse(result);
	}

	@Test
	public void removeAllNodes_emptyCollection() {
		boolean result = idoc.getSections().removeAll(emptyList());
		assertFalse(result);
	}

	@Test
	public void removeAllNodes_noSushElementsInIdoc() {
		boolean result = idoc
				.getSections()
					.removeAll(Arrays.asList(buildSection("section-id-1"), buildSection("section-id-1")));
		assertFalse(result);
	}

	@Test
	public void removeAllNodes_successful() {
		Sections sections = idoc.getSections();
		SectionNode section1 = sections.getSectionByIndex(1);
		SectionNode section2 = sections.getSectionByIndex(2);
		boolean result = sections.removeAll(Arrays.asList(section1, section2));
		assertTrue(result);

		Elements selectedSections = idoc.getContent().select("section");
		assertEquals(4, selectedSections.size());
	}

	@Test
	public void removeAll() {
		List<SectionNode> removedSections = idoc.getSections().removeAll();
		assertEquals(6, removedSections.size());

		Idoc copy = idoc.deepCopy();
		assertEquals(0, copy.getSections().count());
	}

	@Test
	public void removeAllWithIds_nullCollection() {
		boolean result = idoc.getSections().removeAllById(null);
		assertFalse(result);
	}

	@Test
	public void removeAllWithIds_emptyCollection() {
		boolean result = idoc.getSections().removeAllById(emptyList());
		assertFalse(result);
	}

	@Test
	public void removeAllWithIds_noSuchSections() {
		boolean result = idoc.getSections().removeAllById(Arrays.asList("section-id-1", "section-id-2"));
		assertFalse(result);
	}

	@Test
	public void removeAllWithIds_successful() {
		boolean result = idoc.getSections().removeAllById(
				Arrays.asList("28855475-ec07-42aa-b030-65944d15c996", "f0f062e3-163e-4ced-84f5-d2d31fb5dbf7"));
		assertTrue(result);

		Elements selectedSections = idoc.getContent().select("section");
		assertEquals(4, selectedSections.size());
	}

	@Test
	public void setDefaultSection_nullId() {
		idoc.getSections().setDefaultSection(null);

		boolean isDefault = idoc.getSections().getSectionById("f0f062e3-163e-4ced-84f5-d2d31fb5dbf7").get().isDefault();
		assertTrue(isDefault);
	}

	@Test
	public void setDefaultSection_emptyId() {
		idoc.getSections().setDefaultSection("");

		boolean isDefault = idoc.getSections().getSectionById("f0f062e3-163e-4ced-84f5-d2d31fb5dbf7").get().isDefault();
		assertTrue(isDefault);
	}

	@Test
	public void setDefaultSection_noSuchSection() {
		idoc.getSections().setDefaultSection("some-section-id");

		boolean isDefault = idoc.getSections().getSectionById("f0f062e3-163e-4ced-84f5-d2d31fb5dbf7").get().isDefault();
		assertTrue(isDefault);
	}

	@Test
	public void setDefaultSection_successful_defaultChanged() {
		idoc.getSections().setDefaultSection("28855475-ec07-42aa-b030-65944d15c996");

		boolean oldDefault = idoc
				.getSections()
					.getSectionById("f0f062e3-163e-4ced-84f5-d2d31fb5dbf7")
					.get()
					.isDefault();
		assertFalse(oldDefault);

		boolean newDefault = idoc
				.getSections()
					.getSectionById("28855475-ec07-42aa-b030-65944d15c996")
					.get()
					.isDefault();
		assertTrue(newDefault);
	}

	@Test
	public void testLayoutManagers() throws Exception {
		long count = idoc.getSections().layoutManagerNodes().count();
		assertEquals(1L, count);
	}

	@Test
	public void testLayoutNodes() throws Exception {
		long count = idoc.getSections().layouts().count();
		assertEquals(2L, count);
	}

	@Test
	public void testTextNodes() throws Exception {
		long count = idoc.getSections().textNodes().count();
		assertEquals(7L, count);
	}

	@Test
	public void testChildren() throws Exception {
		long count = idoc.getSections().children().count();
		assertEquals(53L, count);
	}

	private static SectionNode buildSection(String id) {
		Element element = new Element(Tag.valueOf("section"), "");
		element.attr("data-id", id);
		return new SectionNode(element);
	}
}
