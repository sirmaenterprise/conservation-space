package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.sep.content.idoc.nodes.TextNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNode;

/**
 * Represents a Intelligent Document instance (IDOC). Provides methods for working on a document or accessing parts of
 * the IDOC DOM. The Implementation uses JSOP DOM structure to traverse and update the IDOC content.
 *
 * @author BBonev
 */
public class Idoc implements NodeContainer {

	private final Sections sections;
	private final Document content;

	/**
	 * Instantiates a new IDOC from the given {@link Document}
	 *
	 * @param content
	 *            the content, required argument
	 */
	public Idoc(Document content) {
		this.content = Objects.requireNonNull(content, "Content is required argument");
		sections = new Sections(getSectionsRoot(content));
	}

	private static Element getSectionsRoot(Document content) {
		// this is a case when the document was stripped from it's body so we probably have a root level idoc
		if (content.body() == null) {
			return content.children().first();
		}
		Element firstDiv = content.body().children().first();
		if (firstDiv == null) {
			Attributes attributes = new Attributes();
			attributes.put("data-tabs-counter", "0");
			firstDiv = new Element(Tag.valueOf("div"), content.baseUri(), attributes);
			content.body().appendChild(firstDiv);
		}
		return firstDiv;
	}

	/**
	 * Parses the given IDOC content and builds an {@link Idoc} instance. Uses the default
	 * {@link Jsoup#parseBodyFragment(String)} method. The method will not append HTML head and body tags
	 *
	 * @param content
	 *            the content to parse
	 * @return the idoc instance
	 */
	public static Idoc parse(String content) {
		return new Idoc(Jsoup.parseBodyFragment(content));
	}

	/**
	 * Parses given file to IDOC instance. The content is read using UTF-8 encoding.
	 *
	 * @param contentLocation
	 *            the content location
	 * @return the idoc instance
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Idoc parse(File contentLocation) throws IOException {
		Objects.requireNonNull(contentLocation, "Cannot parse from null file");
		try (FileInputStream contentStream = new FileInputStream(contentLocation)) {
			return parse(contentStream);
		}
	}

	/**
	 * Parses given input stream to IDOC instance. The content is read using UTF-8 encoding. The stream will be consumed
	 * and closed.
	 *
	 * @param contentStream
	 *            the content stream
	 * @return the idoc instance
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Idoc parse(InputStream contentStream) throws IOException {
		Objects.requireNonNull(contentStream, "Cannot parse from null InputStream");
		try (InputStream content = contentStream) {
			return parse(IOUtils.toString(content));
		}
	}

	/**
	 * Returns all widgets in the {@link Idoc}.
	 *
	 * @return the stream of widgets
	 */
	@Override
	public Stream<Widget> widgets() {
		return sections.widgets().map(Widget.class::cast);
	}

	/**
	 * Select widgets that have the given name (like 'object-data-widget')
	 *
	 * @param type
	 *            the widget type to select
	 * @return the stream of widgets that have the given name
	 */
	public Stream<Widget> widgets(String type) {
		return sections.widgets().filter(widget -> nullSafeEquals(widget.getName(), type));
	}

	/**
	 * Select a single widget by it's unique identifier.
	 *
	 * @param id
	 *            the id of the widget to find
	 * @return the optional that contains the found widget or not
	 */
	public Optional<Widget> selectWidget(String id) {
		return sections
				.widgets()
					.map(Widget.class::cast)
					.filter(widget -> nullSafeEquals(widget.getId(), id))
					.findFirst();
	}

	/**
	 * Select a single section by it's unique identifier.
	 *
	 * @param id
	 *            the id of the section to find
	 * @return the optional that contains the found section or not
	 */
	public Optional<SectionNode> selectSection(String id) {
		return StreamSupport
				.stream(sections.spliterator(), false)
					.filter(section -> nullSafeEquals(section.getId(), id))
					.findFirst();
	}

	/**
	 * Stream of all content nodes of the {@link Idoc}
	 *
	 * @return the stream
	 */
	@Override
	public Stream<ContentNode> children() {
		return sections.children();
	}

	/**
	 * Gets the content sections (tabs).
	 *
	 * @return the sections
	 */
	public Sections getSections() {
		return sections;
	}

	/**
	 * Gets the original content.
	 *
	 * @return the content
	 */
	public Document getContent() {
		return content;
	}

	/**
	 * Gets the IDOC content as HTML text. The method will flush all widget configurations before returning the content
	 *
	 * @return the string
	 */
	public String asHtml() {
		flushWidgetConfigurations();
		Element body = content.body();
		if (body == null) {
			return content.html();
		}
		return body.html();
	}

	private void flushWidgetConfigurations() {
		widgets().forEach(widget -> widget.getConfiguration().writeConfiguration());
	}

	@Override
	public Stream<LayoutNode> layouts() {
		return sections.layouts();
	}

	@Override
	public Stream<LayoutManagerNode> layoutManagerNodes() {
		return sections.layoutManagerNodes();
	}

	@Override
	public Stream<TextNode> textNodes() {
		return sections.textNodes();
	}

	/**
	 * Deep clone the {@link Idoc} structure.
	 *
	 * @return copy of the current {@link Idoc} instance.
	 */
	public Idoc deepCopy() {
		return parse(asHtml());
	}

	/**
	 * Generates new ids for all nodes in the current {@link Idoc}.
	 */
	public void generateNewIds() {
		widgets().forEach(Widget::generateNewId);
		getSections().forEach(SectionNode::generateNewId);
	}

}
