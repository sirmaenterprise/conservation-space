package com.sirma.sep.content.idoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.sirma.sep.content.idoc.SectionNode;

/**
 * Tests for SectionNode.
 *
 * @author Boyan Tonchev
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class SectionNodeTest {

	/**
	 * Tests method getId .
	 */
	@Test
	public void getIdTest() {
		String nodeId = "node-id";
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr(SectionNode.SECTION_NODE_ID_KEY, "someId");
		SectionNode sectionNode = new SectionNode(node);
		sectionNode.setId(nodeId);
		assertEquals(nodeId, sectionNode.getId());
	}

	/**
	 * Tests method getId null scenario.
	 */
	@Test
	public void getIdNullIdTest() {
		Element node = new Element(Tag.valueOf("span"), "");
		SectionNode sectionNode = new SectionNode(node);
		assertNull(sectionNode.getId());
	}

	@Test
	public void setTitle() {
		Element node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_TITLE_KEY, "section-title");
		SectionNode sectionNode = new SectionNode(node);
		sectionNode.setTitle("new-section-title");
		assertEquals("new-section-title", sectionNode.getTitle());
	}

	@Test
	public void getTitle() {
		Element node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_TITLE_KEY, "section-title");
		SectionNode sectionNode = new SectionNode(node);
		assertEquals("section-title", sectionNode.getTitle());
	}

	@Test
	public void isDefault() {
		Element node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_DEFAULT_KEY, "true");
		SectionNode sectionNode = new SectionNode(node);
		assertTrue(sectionNode.isDefault());
	}

	@Test
	public void should_ReportIfTabIsUserDefined() {
		Element node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_USER_DEFINED, "true");

		SectionNode sectionNode = new SectionNode(node);
		assertTrue(sectionNode.isUserDefined());
	}

	@Test
	public void should_ReportThatTabIsNotUserDefined_WhenTheUserDefinedAttributeIsMissingOrIsFalse() {
		Element node = new Element(Tag.valueOf("span"), "");

		SectionNode sectionNode = new SectionNode(node);
		assertFalse(sectionNode.isUserDefined());

		node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_USER_DEFINED, "false");

		sectionNode = new SectionNode(node);
		assertFalse(sectionNode.isUserDefined());
	}

	@Test
	public void should_ReportIfTabIsLocked() {
		Element node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_LOCKED, "true");

		SectionNode sectionNode = new SectionNode(node);
		assertTrue(sectionNode.isLocked());
	}

	@Test
	public void should_ReportThatTabIsLocked_WhenTheLockedAttributeIsMissingOrIsFalse() {
		Element node = new Element(Tag.valueOf("span"), "");

		SectionNode sectionNode = new SectionNode(node);
		assertFalse(sectionNode.isLocked());

		node = new Element(Tag.valueOf("span"), "");
		node.attr(SectionNode.SECTION_LOCKED, "false");

		sectionNode = new SectionNode(node);
		assertFalse(sectionNode.isLocked());
	}

}
