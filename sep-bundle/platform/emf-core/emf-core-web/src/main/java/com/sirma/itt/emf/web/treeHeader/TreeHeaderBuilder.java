package com.sirma.itt.emf.web.treeHeader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.web.application.EmfApplication;
import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;
import com.sirma.itt.emf.web.resources.WebResourceUtil;

/**
 * Contains utility functions needed when a header should be built. At this point the tree header
 * builder manage tree types of headers. <b>DEFAULT</b> - header that has title and description for
 * the object. <b>COMPACT</b> - header that has only title for the object. <b>BREADCRUMB</b> -
 * header that combine titles of object in hierarchical structure. The builder manage dimensions,
 * icons and display modes for every one of the tree headers.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class TreeHeaderBuilder implements InstanceHeaderBuilder {

	/** Logger that will display development information. */
	private static Logger log = Logger.getLogger(TreeHeaderBuilder.class);

	/** The tree header root node style class. */
	protected static final String TREE_HEADER = "tree-header";

	/** Default size value for default header is BIGGER. */
	private static final Size DEFAULT_HEADER_DEFAULT_SIZE = Size.BIGGER;

	/** Default size value for compact header is MEDIUM. */
	private static final Size COMPACT_HEADER_DEFAULT_SIZE = Size.MEDIUM;

	/** Default size value for breadcrumb header is SMALL. */
	private static final Size BREADCRUMB_HEADER_DEFAULT_SIZE = Size.SMALL;

	/** The Constant INSTANCE_HEADER_CLASS. */
	protected static final String INSTANCE_HEADER_CLASS = "instance-header";

	/** Constant represent default display mode for the header. */
	protected static final String DEFAULT_MODE = DefaultProperties.HEADER_DEFAULT;

	/** Constant that helps for inspect DOM elements and remove anchor tag. */
	private static final Pattern HREF_REGEX_PATTERN = Pattern.compile("(?<=<)a(?= )|(?<=</)a(?=>)");

	/** used for retrieving link title for current header for further manipulation */
	private static final Pattern A_TAGS = Pattern.compile("<a[^>]*>(.*?)</a>");

	/** The Constant THUMBNAIL. */
	private static final String THUMBNAIL = DefaultProperties.THUMBNAIL_IMAGE;

	/** The rendition service. */
	@Inject
	private RenditionService renditionService;

	@Inject
	private EmfApplication emfApplication;

	/**
	 * The Enum display.
	 */
	enum Display {

		/** The all. */
		ALL,
		/** The current only. */
		CURRENT_ONLY,
		/** The current and parent. */
		CURRENT_AND_PARENT,
		/** The skip root. */
		SKIP_ROOT,
		/** The skip current. */
		SKIP_CURRENT
	}

	/**
	 * The Constant iconSizes mapping. Contains mapping for header icon size according to the header
	 * mode and size attributes.
	 */
	private static final Map<String, String> ICON_SIZES = new HashMap<String, String>();

	/**
	 * Initialize the builder.
	 */
	@PostConstruct
	public void init() {
		ICON_SIZES.put("default_header_small", "16");
		ICON_SIZES.put("default_header_medium", "24");
		ICON_SIZES.put("default_header_big", "32");
		ICON_SIZES.put("default_header_bigger", "64");
		//
		ICON_SIZES.put("compact_header_small", "16");
		ICON_SIZES.put("compact_header_medium", "24");
		ICON_SIZES.put("compact_header_big", "32");
		ICON_SIZES.put("compact_header_bigger", "64");
		//
		ICON_SIZES.put("breadcrumb_header_small", "16");
		ICON_SIZES.put("breadcrumb_header_medium", "24");
		ICON_SIZES.put("breadcrumb_header_big", "32");
		ICON_SIZES.put("breadcrumb_header_bigger", "64");
		//
		ICON_SIZES.put("thumbnail_icon_size", "70");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Instance> getParents(Instance currentInstance, String displayMode,
			boolean reverseOrder) {

		if (currentInstance == null) {
			return CollectionUtils.emptyList();
		}

		List<Instance> parentsContainer = new ArrayList<Instance>(5);

		String displayModeLowerCase = displayMode.toLowerCase();

		String currentParentDisplay = Display.CURRENT_AND_PARENT.name().toLowerCase();
		String currentOnlyDisplay = Display.CURRENT_ONLY.name().toLowerCase();
		String skipRootDisplay = Display.SKIP_ROOT.name().toLowerCase();
		String skipCurrentDisplay = Display.SKIP_CURRENT.name().toLowerCase();

		if (currentParentDisplay.equals(displayModeLowerCase)) {
			// retrieve parent element of current instance
			Instance parent = InstanceUtil.getDirectParent(currentInstance, true);
			if (parent != null) {
				parentsContainer.add(parent);
			}
			parentsContainer.add(currentInstance);

		} else if (currentOnlyDisplay.equals(displayModeLowerCase)) {
			// no parent, use current instance as parent
			parentsContainer.add(currentInstance);

		} else if (skipRootDisplay.equals(displayModeLowerCase)) {
			// retrieve all parents of current instance but skip the
			// highest parent
			parentsContainer.addAll(InstanceUtil.getParentPath(currentInstance, true));
			if (!parentsContainer.isEmpty()) {
				Instance instance = parentsContainer.get(0);
				if (instance instanceof RootInstanceContext) {
					// remove to highest parent
					parentsContainer.remove(0);
				}
			}

		} else {
			// default mode
			// add all parents elements based on current instance
			// and remove the current instance
			parentsContainer.addAll(InstanceUtil.getParentPath(currentInstance, true));
		}

		// reverse all elements
		if (reverseOrder) {
			Collections.reverse(parentsContainer);
		}
		return parentsContainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIcon(Instance instance, String mode, String size, boolean renderThumbnail) {
		// if the instance is not initialized or is not persist into the
		// database, prevent further manipulations
		if ((instance == null) || !SequenceEntityGenerator.isPersisted(instance)) {
			return null;
		}
		// if the header is the default or we explicitly set <i>renderThumbnail</i>
		// attribute, the logic will retrieve element thumbnail
		if (DefaultProperties.HEADER_DEFAULT.equals(mode) || renderThumbnail) {
			String thumbnailIcon = getThumbnailIcon(instance);
			if (thumbnailIcon != null) {
				return thumbnailIcon;
			}
		}
		String defaultSize = size;
		// applying the default icon size
		if (StringUtils.isNullOrEmpty(defaultSize)) {
			defaultSize = COMPACT_HEADER_DEFAULT_SIZE.getSize();
		}
		String instanceType = instance.getClass().getSimpleName().toLowerCase();
		// retrieve the default icon image
		String icon = buildIcon(instanceType, mode, defaultSize);
		return generateIconUrl(icon);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBreadcrumbIcon(String instanceType) {
		String mode = DefaultProperties.HEADER_BREADCRUMB;
		String size = BREADCRUMB_HEADER_DEFAULT_SIZE.getSize();
		String icon = buildIcon(instanceType, mode, size);
		return generateIconUrl(icon);
	}

	/**
	 * Help method for constructing icon string.
	 * 
	 * @param instanceType
	 *            current instance type
	 * @param mode
	 *            current header mode
	 * @param size
	 *            current icon size
	 * @return icon represent as string
	 */
	private String buildIcon(String instanceType, String mode, String size) {
		StringBuilder icon = new StringBuilder(instanceType);
		icon.append("-icon");
		icon.append("-").append(getIconSize(mode, size));
		icon.append(".png");
		return icon.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIconSize(String mode, String size) {
		String calculatedMode = DefaultProperties.HEADER_DEFAULT;
		if (mode != null) {
			calculatedMode = mode;
		}
		String calculatedSize = calculateIconSizeByMode(calculatedMode, Size.getSizeByString(size))
				.getSize();
		StringBuilder iconSizeKey = new StringBuilder(calculatedMode);
		iconSizeKey.append("_").append(calculatedSize);
		return ICON_SIZES.get(iconSizeKey.toString());
	}

	/**
	 * Retrieve thumbnail icon dimensions based on current instance.
	 * 
	 * @param instance
	 *            current instance
	 * @param renderThumbnail
	 *            specify thumbnail to be render
	 * @return specific size or empty string
	 */
	public String getThumbnailSize(Instance instance, boolean renderThumbnail) {
		String thumbnailIconSize = "";
		if (hasThumbnail(instance) && renderThumbnail) {
			thumbnailIconSize = "thumbnail_icon_size";
			return ICON_SIZES.get(thumbnailIconSize);
		}
		return thumbnailIconSize;
	}

	/**
	 * Generate icon URL based on the current instance icon.
	 * 
	 * @param icon
	 *            current instance icon
	 * @return absolute image path as string
	 */
	protected String generateIconUrl(String icon) {
		return WebResourceUtil.getIconUrl(emfApplication.getContextPath(), icon);
	}

	/**
	 * Get thumbnail icon from current instance properties.
	 * 
	 * @param instance
	 *            current instance
	 * @return data uri
	 */
	private String getThumbnailIcon(Instance instance) {
		return renditionService.getDefaultThumbnail(instance);
	}

	/**
	 * Method that will help for <b>thumbnail</b> verification based on current instance.
	 * 
	 * @param instance
	 *            current instance
	 * @return boolean result
	 */
	protected boolean hasThumbnail(Instance instance) {
		boolean hasThumbnail = false;
		// if instance is not available or has no properties, prevent
		// further manipulation
		if ((instance == null) || (instance.getProperties() == null)) {
			return hasThumbnail;
		}
		if (instance.getProperties().get(THUMBNAIL) != null) {
			hasThumbnail = true;
		}
		return hasThumbnail;
	}

	/**
	 * Calculate icon size by mode. The mode can't come null here. If size is provided, then use it.
	 * Otherwise calculate it according to the mode. If mode is passed null and the size is not
	 * provided, then null is returned.
	 * 
	 * @param mode
	 *            the mode
	 * @param size
	 *            the size
	 * @return the size if not null is passed or calculated value otherwise.
	 */
	protected Size calculateIconSizeByMode(String mode, Size size) {
		Size calculated = null;
		// if size is provided, then use it explicitly
		if (size != null) {
			calculated = size;
		} else {
			if (DefaultProperties.HEADER_DEFAULT.equals(mode)) {
				calculated = DEFAULT_HEADER_DEFAULT_SIZE;
			} else if (DefaultProperties.HEADER_COMPACT.equals(mode)) {
				calculated = COMPACT_HEADER_DEFAULT_SIZE;
			} else if (DefaultProperties.HEADER_BREADCRUMB.equals(mode)) {
				calculated = BREADCRUMB_HEADER_DEFAULT_SIZE;
			}
		}
		return calculated;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHeader(Instance instance, String mode, boolean disableLinks) {
		if (instance == null) {
			return null;
		}
		String header = null;
		Map<String, Serializable> properties = instance.getProperties();
		if (properties != null) {
			if (StringUtils.isNotNullOrEmpty(mode)) {
				if (DefaultProperties.HEADER_BREADCRUMB.equalsIgnoreCase(mode)) {
					header = optimizeBreadcrumbHeader((String) properties.get(mode));
				} else {
					header = (String) properties.get(mode);
				}
			} else {
				header = (String) properties.get(DEFAULT_MODE);
			}
			if (disableLinks) {
				header = disableLinks(header);
			}
		} else {
			log.error("EMFWeb: Missing properties for current processed instance:" + instance);
		}

		return header;
	}

	/**
	 * Optimize bread-crumb header links. Will truncate the title based on the first 30 characters.
	 * 
	 * @param breadcrumb
	 *            bread-crumb header
	 * @return truncated bread-crumb
	 */
	private String optimizeBreadcrumbHeader(String breadcrumb) {
		if (breadcrumb == null) {
			return null;
		}
		String unoptimized = breadcrumb;
		String shortHeaderSuffix = "...";
		int titleSize = 30;
		Matcher foundData = A_TAGS.matcher(unoptimized);
		if (foundData.matches()) {
			String title = foundData.group(1);
			// check title size and if is longer than <i>titleSize</i>
			// apply shortening manipulation
			if (title.length() > titleSize) {
				unoptimized = unoptimized.replaceAll(Pattern.quote(title),
						title.substring(0, titleSize) + shortHeaderSuffix);
			}
		}
		return unoptimized;
	}

	/**
	 * Disable links in template.
	 * 
	 * @param header
	 *            the header
	 * @return the string
	 */
	protected String disableLinks(String header) {
		if (StringUtils.isNotNullOrEmpty(header)) {
			return HREF_REGEX_PATTERN.matcher(header).replaceAll("span");
		}
		return header;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTreeHeaderStyleClass(String customClass, String mode) {
		StringBuilder styleClass = new StringBuilder(TREE_HEADER);
		String emptySpace = " ";
		if (StringUtils.isNotNullOrEmpty(customClass)) {
			styleClass.append(emptySpace).append(customClass);
		}
		if (StringUtils.isNotNullOrEmpty(mode)) {
			styleClass.append(emptySpace).append(mode);
		} else {
			styleClass.append(emptySpace).append(DEFAULT_MODE);
		}
		return styleClass.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultHeaderStyleClass(Instance instance, String size) {
		// instance-header medium case instance
		StringBuilder styleClass = new StringBuilder(50);
		styleClass.append(INSTANCE_HEADER_CLASS);
		appendWithSpace(styleClass, size);
		if (instance != null) {
			appendWithSpace(styleClass, instance.getClass().getSimpleName().toLowerCase());
		}
		return styleClass.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCompactHeaderStyleClass(Instance instance, String size, boolean isFirst,
			boolean isLast) {
		// instance-header current big
		StringBuilder styleClass = new StringBuilder(INSTANCE_HEADER_CLASS);
		if (isFirst && isLast) {
			// TODO: here will be some default class ?
		} else if (isFirst) {
			appendWithSpace(styleClass, "first");
		} else if (isLast) {
			appendWithSpace(styleClass, "last");
		}
		if (isLast) {
			appendWithSpace(styleClass, "current");
		}
		appendWithSpace(styleClass, size);
		if (instance != null) {
			appendWithSpace(styleClass, instance.getClass().getSimpleName().toLowerCase());
		}
		return styleClass.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBreadcrumbHeaderStyleClass(Instance instance, String size) {
		// instance-header small
		StringBuilder styleClass = new StringBuilder(40);
		styleClass.append(INSTANCE_HEADER_CLASS);
		appendWithSpace(styleClass, size);
		if (instance != null) {
			String instanceType = instance.getClass().getSimpleName().toLowerCase();
			appendWithSpace(styleClass, instanceType);
		}
		return styleClass.toString();
	}

	/**
	 * Append style class to accumulated list of styles classes.
	 * 
	 * @param accumulatedStyleClass
	 *            the accumulated style class
	 * @param styleClass
	 *            the style class
	 */
	private void appendWithSpace(StringBuilder accumulatedStyleClass, String styleClass) {
		if (StringUtils.isNotNullOrEmpty(styleClass)) {
			accumulatedStyleClass.append(" ").append(styleClass);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefaultMode(String mode) {
		return StringUtils.isNullOrEmpty(mode) || DefaultProperties.HEADER_DEFAULT.equals(mode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompactMode(String mode) {
		return StringUtils.isNotNullOrEmpty(mode) && DefaultProperties.HEADER_COMPACT.equals(mode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBreadcrumbMode(String mode) {
		return StringUtils.isNotNullOrEmpty(mode)
				&& DefaultProperties.HEADER_BREADCRUMB.equals(mode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean renderCurrent(String display, Instance instance, String mode) {
		boolean render = true;
		if (StringUtils.isNotNullOrEmpty(display)) {
			if (Display.SKIP_CURRENT.name().equalsIgnoreCase(display)) {
				render = false;
			} else if ((instance != null) && (instance.getProperties().get(mode) == null)) {
				render = false;
			}
		}
		return render;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCompactHeaderNodePadding(int index, String size) {
		StringBuilder padding = new StringBuilder(30);
		padding.append("padding-left: ");
		String compactMode = DefaultProperties.HEADER_COMPACT;
		Size calculatedIconSizeByMode = calculateIconSizeByMode(compactMode,
				Size.getSizeByString(size));
		padding.append(index * calculatedIconSizeByMode.getIconSize()).append("px;");
		return padding.toString();
	}

}
