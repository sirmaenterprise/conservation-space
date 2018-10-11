package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.stream.Stream;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.nodes.TextNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNode;

/**
 * Represents a single IDOC section (tab)
 *
 * @author BBonev
 */
public class SectionNode extends TextNode implements NodeContainer {

	public static final String SECTION_DEFAULT_KEY = "data-default";
	public static final String SECTION_NODE_ID_KEY = "data-id";
	public static final String SECTION_TITLE_KEY = "data-title";
	public static final String SECTION_PUBLISH_MODE_KEY = "data-revision";
	public static final String SECTION_LOCKED = "data-locked";
	public static final String SECTION_USER_DEFINED = "data-user-defined";

	private ContentNodes nodes;

	/**
	 * Instantiates a new section node.
	 *
	 * @param node
	 *            the node
	 */
	public SectionNode(Element node) {
		super(node);
		nodes = new ContentNodes(node.select("*"));
	}

	/**
	 * Gets the layout managers.
	 *
	 * @return the layout managers
	 */
	@Override
	public Stream<LayoutManagerNode> layoutManagerNodes() {
		return children().filter(ContentNode::isLayoutManager).map(LayoutManagerNode.class::cast);
	}

	/**
	 * Gets the layout nodes.
	 *
	 * @return the layouts
	 */
	@Override
	public Stream<LayoutNode> layouts() {
		return children().filter(ContentNode::isLayout).map(LayoutNode.class::cast);
	}

	/**
	 * Gets the widget nodes.
	 *
	 * @return the widgets
	 */
	@Override
	public Stream<Widget> widgets() {
		return children().filter(ContentNode::isWidget).map(Widget.class::cast);
	}

	/**
	 * Gets the text nodes.
	 *
	 * @return the text nodes
	 */
	@Override
	public Stream<TextNode> textNodes() {
		return children().filter(ContentNode::isTextNode).map(TextNode.class::cast);
	}

	/**
	 * Gets node's child elements, but only at one level.
	 *
	 * @return the children
	 */
	@Override
	public Stream<ContentNode> children() {
		return nodes.getNodes().stream();
	}

	@Override
	public String getId() {
		return getProperty(SECTION_NODE_ID_KEY);
	}

	@Override
	public void setId(String id) {
		setProperty(SECTION_NODE_ID_KEY, id);
	}

	@Override
	public boolean isWidget() {
		return false;
	}

	@Override
	public boolean isLayout() {
		return false;
	}

	@Override
	public boolean isTextNode() {
		return false;
	}

	/**
	 * Gets the title of the current section node.
	 *
	 * @return title of the node
	 */
	public String getTitle() {
		return getProperty(SECTION_TITLE_KEY);
	}

	/**
	 * Sets title for the current node.
	 *
	 * @param title
	 *            to be set
	 * @return the current node for chaining
	 */
	public SectionNode setTitle(String title) {
		setProperty(SECTION_TITLE_KEY, title);
		return this;
	}

	/**
	 * Shows if the current section node is default or not. That means if it should be opened first, when the idoc is
	 * opened.
	 *
	 * @return <code>true</code> if the current section node is default, <code>false</code> otherwise
	 */
	public boolean isDefault() {
		return Boolean.parseBoolean(getProperty(SECTION_DEFAULT_KEY));
	}

	/**
	 * Indicates that the particular section is locked.
	 *
	 * @return true if locked, false otherwise.
	 */
	public boolean isLocked() {
		return Boolean.parseBoolean(getProperty(SECTION_LOCKED));
	}

	/**
	 * Indicates that the particular section is manually defined by the user (not auto-generated).
	 *
	 * @return true if defined by user, false otherwise.
	 */
	public boolean isUserDefined() {
		return Boolean.parseBoolean(getProperty(SECTION_USER_DEFINED));
	}

	/**
	 * Get the publish mode for the current tab
	 *
	 * @return the publish mode, never null
	 */
	public PublishMode getPublishMode() {
		return PublishMode.parse(getProperty(SECTION_PUBLISH_MODE_KEY));
	}

	/**
	 * Defines the possible values for the publish mode of a tab.
	 *
	 * @author BBonev
	 */
	public enum PublishMode {
		/**
		 * The tab is exported as is
		 */
		EXPORT("exportable"),
		/**
		 * The tab is exported but data in widgets is cleared
		 */
		CLONE("cloneable"),
		/**
		 * The tab is skipped and not present in the published idoc
		 */
		SKIP("skip");
		private String mode;

		private PublishMode(String mode) {
			this.mode = mode;
		}

		/**
		 * @return the mode value
		 */
		public String getMode() {
			return mode;
		}

		/**
		 * Parse the given argument and try to convert it to {@link PublishMode}. Possible values: exportable, clonable,
		 * skip. If non is match then {@link PublishMode#EXPORT} will be returned as default
		 *
		 * @param modeName
		 *            mode name to parse
		 * @return valid {@link PublishMode}
		 */
		public static PublishMode parse(String modeName) {
			for (PublishMode mode : values()) {
				if (nullSafeEquals(mode.getMode(), modeName)) {
					return mode;
				}
			}
			return PublishMode.EXPORT;
		}
	}

}
