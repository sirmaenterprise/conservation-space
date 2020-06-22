package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static com.sirma.sep.content.idoc.SectionNode.SECTION_DEFAULT_KEY;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.content.idoc.nodes.TextNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNode;

/**
 * Represents a collection of {@link SectionNode}s located in an {@link Idoc}
 *
 * @author BBonev
 */
public class Sections implements Iterable<SectionNode>, NodeContainer {

	private static final String CANNOT_ADD_NULL_SECTION_NODE = "Cannot add null SectionNode";
	private final List<SectionNode> sectionNodes;
	private final Element parentNode;

	/**
	 * Instantiates a new {@link Sections} instance initialized with the given element to represents a parent it's
	 * children. Any add operations will be attached to the given node.
	 *
	 * @param parent
	 *            specified parent node to be used, required argument.
	 */
	public Sections(Element parent) {
		parentNode = Objects.requireNonNull(parent, "Parent node is required");
		sectionNodes = parent.select("section").stream().map(SectionNode::new).collect(Collectors.toList());
	}

	/**
	 * Adds section on specific index.
	 *
	 * @param position
	 *            where the section will be added
	 * @param section
	 *            section to add
	 * @return current node for chaining
	 * @see List#add(int, Object)
	 */
	public Sections addSectionAt(int position, SectionNode section) {
		Objects.requireNonNull(section, CANNOT_ADD_NULL_SECTION_NODE);
		rangeCheck(position);

		if (isEmpty(sectionNodes)) {
			parentNode.insertChildren(position, Arrays.asList(section.getElement()));
			sectionNodes.add(section);
		} else {
			// insert the new node before the current node in the dom
			sectionNodes.get(position).getElement().before(section.getElement());
			sectionNodes.add(position, section);
		}
		return this;
	}

	/**
	 * Adds section after section with specific id. If the specified id is not found then all elements will be added at
	 * the end of the current sections
	 *
	 * @param sectionId
	 *            the id of the section after which will be added
	 * @param sections
	 *            nodes to add
	 * @return <code>true</code> if at least one section is added successfully. May return <code>false</code> if the is
	 *         no section with the specified id after which should be added or the sections argument is empty
	 * @see List#addAll(int, Collection)
	 */
	public boolean addAfter(String sectionId, Collection<SectionNode> sections) {
		if (isEmpty(sections)) {
			return false;
		}

		int index = getSectionIndex(sectionId);
		if (index < 0) {
			sections.forEach(this::addLast);
		} else {
			Element elementAtIndex = sectionNodes.get(index).getElement();
			sectionNodes.addAll(index, sections);
			// add all elements in reverse order after the element at the specified index
			// after the add they will appear in the correct order
			new LinkedList<>(sections)
					.descendingIterator()
						.forEachRemaining(node -> elementAtIndex.after(node.getElement()));
		}
		return true;
	}

	/**
	 * Appends a collection of sections.
	 *
	 * @param sections sections to append.
	 */
	public void addAll(Collection<SectionNode> sections) {
		sections.forEach(this::addLast);
	}

	/**
	 * Add the given section in from of all other sections
	 *
	 * @param node
	 *            the section node to add. Cannot be <code>null</code>
	 * @return the current instance for chaining
	 */
	public Sections addFirst(SectionNode node) {
		Objects.requireNonNull(node, CANNOT_ADD_NULL_SECTION_NODE);
		addSectionAt(0, node);
		return this;
	}

	/**
	 * Add the given section node as last element in the list of sections
	 *
	 * @param node
	 *            the node to add. Cannot be <code>null</code>
	 * @return the current instance for chaining
	 */
	public Sections addLast(SectionNode node) {
		Objects.requireNonNull(node, CANNOT_ADD_NULL_SECTION_NODE);

		sectionNodes.add(node);
		parentNode.appendChild(node.getElement());
		return this;
	}

	/**
	 * Retrieves section by id.
	 *
	 * @param sectionId
	 *            the id of the section that should be returned
	 * @return {@link SectionNode} or <code>null</code> if there is no node with such id
	 * @see List#get(int)
	 */
	public Optional<SectionNode> getSectionById(String sectionId) {
		int index = getSectionIndex(sectionId);
		return index < 0 ? Optional.empty() : Optional.of(sectionNodes.get(index));
	}

	private int getSectionIndex(String id) {
		if (StringUtils.isBlank(id)) {
			return -1;
		}

		int index = 0;
		for (SectionNode section : sectionNodes) {
			if (id.equals(section.getId())) {
				return index;
			}
			index++;
		}

		return -1;
	}

	/***
	 * Retrieves section by index.
	 *
	 * @param index
	 *            of the section that should be returned
	 * @return {@link SectionNode}
	 * @see List#get(int)
	 */
	public SectionNode getSectionByIndex(int index) {
		rangeCheck(index);
		return sectionNodes.get(index);
	}

	private void rangeCheck(int index) {
		if (0 > index || index > sectionNodes.size()) {
			throw new IndexOutOfBoundsException("Invalid section index: " + index);
		}
	}

	/**
	 * Removes section by id.
	 *
	 * @param id
	 *            of the section to remove
	 * @return <code>true</code> if the section is successfully removed
	 */
	public boolean removeById(String id) {
		if (StringUtils.isBlank(id)) {
			return false;
		}
		int count = 0;
		for (Iterator<SectionNode> it = iterator(); it.hasNext();) {
			SectionNode node = it.next();
			if (nullSafeEquals(id, node.getId())) {
				it.remove();
				count++;
			}
		}
		return count > 0;
	}

	/**
	 * Removes multiple sections by ids, if they are available in the idoc.
	 *
	 * @param sectionIds
	 *            the ids of the sections that should be removed
	 * @return <code>true</code> if at least one section was removed from the idoc
	 * @see List#removeIf(java.util.function.Predicate)
	 */
	public boolean removeAllById(Collection<String> sectionIds) {
		if (isEmpty(sectionIds)) {
			return false;
		}
		int count = 0;
		for (Iterator<SectionNode> it = iterator(); it.hasNext();) {
			SectionNode node = it.next();
			if (sectionIds.contains(node.getId())) {
				it.remove();
				count++;
			}
		}
		return count > 0;
	}

	/**
	 * Removes from sections all passed sections, if they are available in the idoc.
	 *
	 * @param sections
	 *            to remove
	 * @return <code>true</code> if at least one section was removed from the idoc
	 * @see List#removeAll(Collection)
	 */
	public boolean removeAll(Collection<SectionNode> sections) {
		if (isEmpty(sections)) {
			return false;
		}
		return removeAllById(sections.stream().map(SectionNode::getId).collect(Collectors.toSet()));
	}

	/**
	 * Removes all sections from the idoc.
	 *
	 * @return removed sections
	 */
	public List<SectionNode> removeAll() {
		List<SectionNode> old = CollectionUtils.clone(sectionNodes);
		Iterator<SectionNode> it = iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		return old;
	}

	/**
	 * Sets specific section to be default and makes the value of 'data-default' attribute to be <code>false</code> for
	 * all others sections. This property is used to show which section should be loaded by default, when specific idoc
	 * is opened.
	 *
	 * @param id
	 *            of the section that should be default.
	 */
	public void setDefaultSection(String id) {
		if (StringUtils.isBlank(id)) {
			return;
		}

		boolean sectionExist = sectionNodes.stream().anyMatch(node -> id.equals(node.getId()));
		if (!sectionExist) {
			return;
		}

		sectionNodes.forEach(node -> node.setProperty(SECTION_DEFAULT_KEY, String.valueOf(id.equals(node.getId()))));
	}

	@Override
	public Stream<Widget> widgets() {
		return sectionNodes.stream().flatMap(SectionNode::widgets);
	}

	@Override
	public Stream<LayoutNode> layouts() {
		return sectionNodes.stream().flatMap(SectionNode::layouts);
	}

	@Override
	public Stream<LayoutManagerNode> layoutManagerNodes() {
		return sectionNodes.stream().flatMap(SectionNode::layoutManagerNodes);
	}

	@Override
	public Stream<TextNode> textNodes() {
		return sectionNodes.stream().flatMap(SectionNode::textNodes);
	}

	@Override
	public Stream<ContentNode> children() {
		return sectionNodes.stream().flatMap(SectionNode::children);
	}

	/**
	 * Mutable iterator for all {@link SectionNode}s
	 *
	 * @return the iterator
	 */
	@Override
	public Iterator<SectionNode> iterator() {
		return new ContentNodeIterator<>(sectionNodes.iterator());
	}

	/**
	 * Return a stream of first level sections contained in the current instance
	 *
	 * @return of all sections contained in the current {@link Sections} instance.
	 */
	public Stream<SectionNode> stream() {
		return sectionNodes.stream();
	}

	/**
	 * The number of sections present
	 *
	 * @return the sections number.
	 */
	public int count() {
		return sectionNodes.size();
	}
}
