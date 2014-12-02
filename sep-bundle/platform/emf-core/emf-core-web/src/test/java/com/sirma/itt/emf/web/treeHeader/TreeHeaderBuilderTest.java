package com.sirma.itt.emf.web.treeHeader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.web.treeHeader.TreeHeaderBuilder.Display;

/**
 * The Class TreeHeaderBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class TreeHeaderBuilderTest {

	/** The Constant log. */
	protected static final Logger LOG = Logger.getLogger(TreeHeaderBuilderTest.class);

	/** The tree header builder. */
	private final TreeHeaderBuilder treeHeaderBuilder;

	private final RenditionService renditionService;

	/**
	 * Instantiates a new tree header builder test.
	 */
	public TreeHeaderBuilderTest() {
		treeHeaderBuilder = new TreeHeaderBuilder() {

			@Override
			protected String generateIconUrl(String icon) {
				return icon;
			}
		};

		renditionService = Mockito.mock(RenditionService.class);

		ReflectionUtils.setField(treeHeaderBuilder, "log", LOG);
		ReflectionUtils.setField(treeHeaderBuilder, "renditionService", renditionService);
	}

	/**
	 * Call init.
	 */
	@BeforeMethod
	public void callInit() {
		treeHeaderBuilder.init();
	}

	/**
	 * Gets the parents test.
	 */
	public void getParentsTest() {
		List<Instance> parents = treeHeaderBuilder.getParents(null, null, false);

		// if instance is null then an empty list is expected
		assertEquals(parents, Collections.EMPTY_LIST);

		// for current_only we should the current instance only in the parents list
		SomeInstance currentInstance = new SomeInstance();
		currentInstance.setId(Long.valueOf(1));
		parents = treeHeaderBuilder.getParents(currentInstance, Display.CURRENT_ONLY.name()
				.toLowerCase(), false);
		assertEquals(parents.size(), 1);
	}

	/**
	 * Calculate icon size by mode test.
	 */
	public void calculateIconSizeByModeTest() {
		Size size = treeHeaderBuilder.calculateIconSizeByMode(null, null);

		// size=null and mode==null -> we should get null result
		assertNull(size);

		// if size is provided, the we should get it as result
		size = treeHeaderBuilder.calculateIconSizeByMode(null, Size.BIG);
		assertEquals(size, Size.BIG);

		// mode==default_header -> size==BUGGER
		size = treeHeaderBuilder.calculateIconSizeByMode(DefaultProperties.HEADER_DEFAULT, null);
		assertEquals(size, Size.BIGGER);

		// mode==compact_header -> size==MEDIUM
		size = treeHeaderBuilder.calculateIconSizeByMode(DefaultProperties.HEADER_COMPACT, null);
		assertEquals(size, Size.MEDIUM);

		// mode==breadcrumb_header -> size==SMALL
		size = treeHeaderBuilder.calculateIconSizeByMode(DefaultProperties.HEADER_BREADCRUMB, null);
		assertEquals(size, Size.SMALL);

		// for mode is passed invalid value -> we should get null result
		size = treeHeaderBuilder.calculateIconSizeByMode("invalid_value", null);
		assertNull(size);
	}

	/**
	 * Gets the icon name test.
	 */
	public void getIconNameTest() {
		// if instance is null
		String iconName = treeHeaderBuilder.getIcon(null, null, null, false);
		assertNull(iconName);

		SomeInstance instance = new SomeInstance();
		instance.setId(Long.valueOf(1));
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		instance.setProperties(properties);

		Mockito.when(renditionService.getThumbnail(Mockito.any(Instance.class))).thenReturn(null);
		iconName = treeHeaderBuilder.getIcon(instance, DefaultProperties.HEADER_DEFAULT, "", false);
		assertEquals(iconName, "someinstance-icon-24.png");

		iconName = treeHeaderBuilder.getIcon(instance, null, null, false);
		assertEquals(iconName, "someinstance-icon-24.png");
	}

	// TODO: implement test
	// public void getIconSizeTest() {
	//
	// }

	/**
	 * Gets the header test.
	 */
	public void getHeaderTest() {
		String header = null;

		// if no instance is provided we should return null
		header = treeHeaderBuilder.getHeader(null, null, false);
		assertNull(header);

		// if passed instance has no properties we should return null
		Instance instance = new SomeInstance();
		header = treeHeaderBuilder.getHeader(instance, null, false);
		assertNull(header);

		// if there is no header fields in properties map
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		instance.setProperties(properties);
		header = treeHeaderBuilder.getHeader(instance, null, false);
		assertNull(header);

		// if no mode is provided, then the default header should be returned
		properties = instance.getProperties();
		properties.put(DefaultProperties.HEADER_DEFAULT, "default header");
		header = treeHeaderBuilder.getHeader(instance, null, false);
		assertEquals(header, "default header");

		// if empty string is passed for mode, then the default header should be returned
		header = treeHeaderBuilder.getHeader(instance, "", false);
		assertEquals(header, "default header");

		// if mode is provided, then return the header for that mode
		properties.put(DefaultProperties.HEADER_DEFAULT, "default header");
		properties.put(DefaultProperties.HEADER_COMPACT, "compact header");
		header = treeHeaderBuilder.getHeader(instance, DefaultProperties.HEADER_COMPACT, false);
		assertEquals(header, "compact header");

	}

	// /**
	// * Disable links test.
	// */
	// public void disableLinksTest() {
	//
	// }

	/**
	 * Gets the tree header style class test.
	 */
	public void getTreeHeaderStyleClassTest() {
		String treeHeaderStyleClass = null;
		boolean contains = false;

		// check for defaults
		treeHeaderStyleClass = treeHeaderBuilder.getTreeHeaderStyleClass(null, null);
		contains = treeHeaderStyleClass.contains(TreeHeaderBuilder.TREE_HEADER);
		assertTrue(contains);
		contains = treeHeaderStyleClass.contains(TreeHeaderBuilder.DEFAULT_MODE);
		assertTrue(contains);

		//
		treeHeaderStyleClass = treeHeaderBuilder.getTreeHeaderStyleClass("custom-class", null);
		contains = treeHeaderStyleClass.contains(TreeHeaderBuilder.TREE_HEADER);
		assertTrue(contains);
		contains = treeHeaderStyleClass.contains("custom-class");
		assertTrue(contains);

		//
		treeHeaderStyleClass = treeHeaderBuilder.getTreeHeaderStyleClass("custom-class",
				DefaultProperties.HEADER_COMPACT);
		contains = treeHeaderStyleClass.contains(TreeHeaderBuilder.TREE_HEADER);
		assertTrue(contains);
		contains = treeHeaderStyleClass.contains("custom-class");
		assertTrue(contains);
		contains = treeHeaderStyleClass.contains(DefaultProperties.HEADER_COMPACT);
		assertTrue(contains);
	}

	/**
	 * Gets the default header style class test.
	 */
	public void getDefaultHeaderStyleClassTest() {
		String defaultHeaderStyleClass = null;
		boolean contains = false;

		// check for defaults
		defaultHeaderStyleClass = treeHeaderBuilder.getDefaultHeaderStyleClass(null, null);
		contains = defaultHeaderStyleClass.contains(TreeHeaderBuilder.INSTANCE_HEADER_CLASS);
		assertTrue(contains);

		//
		defaultHeaderStyleClass = treeHeaderBuilder.getDefaultHeaderStyleClass(null,
				Size.BIG.getSize());
		contains = defaultHeaderStyleClass.contains(Size.BIG.getSize());
		assertTrue(contains);

		//
		defaultHeaderStyleClass = treeHeaderBuilder.getDefaultHeaderStyleClass(new SomeInstance(),
				Size.BIG.getSize());
		contains = defaultHeaderStyleClass.contains(Size.BIG.getSize());
		assertTrue(contains);
		contains = defaultHeaderStyleClass.contains("someinstance");
		assertTrue(contains);

		// invalid size value
		defaultHeaderStyleClass = treeHeaderBuilder
				.getDefaultHeaderStyleClass(null, "invalid_size");
		contains = defaultHeaderStyleClass.contains("invalid_size");
		assertTrue(contains);
	}

	/**
	 * Gets the compact header style class test.
	 */
	public void getCompactHeaderStyleClassTest() {
		String compactHeaderStyleClass = null;
		boolean contains = false;

		// check for defaults
		compactHeaderStyleClass = treeHeaderBuilder.getCompactHeaderStyleClass(null, null, false,
				false);
		contains = compactHeaderStyleClass.contains(TreeHeaderBuilder.INSTANCE_HEADER_CLASS);
		assertTrue(contains);

		// if is first, then we should have a 'first' style class appended
		compactHeaderStyleClass = treeHeaderBuilder.getCompactHeaderStyleClass(null, null, true,
				false);
		contains = compactHeaderStyleClass.contains("first");
		assertTrue(contains);

		// if is first and is last in the same time, then we should have a 'current' style class
		// appended
		compactHeaderStyleClass = treeHeaderBuilder.getCompactHeaderStyleClass(null, null, true,
				true);
		contains = compactHeaderStyleClass.contains("current");
		assertTrue(contains);

		// if is last, then we should have a 'last' and 'current' style classes appended
		compactHeaderStyleClass = treeHeaderBuilder.getCompactHeaderStyleClass(null, null, false,
				true);
		contains = compactHeaderStyleClass.contains("last");
		assertTrue(contains);
		contains = compactHeaderStyleClass.contains("current");
		assertTrue(contains);

		//
		compactHeaderStyleClass = treeHeaderBuilder.getCompactHeaderStyleClass(new SomeInstance(),
				Size.BIG.getSize(), false, true);
		contains = compactHeaderStyleClass.contains("last");
		assertTrue(contains);
		contains = compactHeaderStyleClass.contains("current");
		assertTrue(contains);
		contains = compactHeaderStyleClass.contains(Size.BIG.getSize());
		assertTrue(contains);
		contains = compactHeaderStyleClass.contains("someinstance");
		assertTrue(contains);
	}

	/**
	 * Gets the breadcrumb header style class test.
	 */
	public void getBreadcrumbHeaderStyleClassTest() {
		String breadcrumbHeaderStyleClass = null;
		boolean contains = false;

		// check for defaults
		breadcrumbHeaderStyleClass = treeHeaderBuilder.getBreadcrumbHeaderStyleClass(null, null);
		contains = breadcrumbHeaderStyleClass.contains(TreeHeaderBuilder.INSTANCE_HEADER_CLASS);
		assertTrue(contains);

		//
		breadcrumbHeaderStyleClass = treeHeaderBuilder.getBreadcrumbHeaderStyleClass(null,
				Size.BIG.getSize());
		contains = breadcrumbHeaderStyleClass.contains(Size.BIG.getSize());
		assertTrue(contains);

		//
		breadcrumbHeaderStyleClass = treeHeaderBuilder.getBreadcrumbHeaderStyleClass(
				new SomeInstance(), Size.BIG.getSize());
		contains = breadcrumbHeaderStyleClass.contains("someinstance");
		assertTrue(contains);
		contains = breadcrumbHeaderStyleClass.contains(Size.BIG.getSize());
		assertTrue(contains);

		// invalid size value
		breadcrumbHeaderStyleClass = treeHeaderBuilder.getBreadcrumbHeaderStyleClass(null,
				"invalid_size");
		contains = breadcrumbHeaderStyleClass.contains("invalid_size");
		assertTrue(contains);
	}

	/**
	 * Checks if is default mode test.
	 */
	public void isDefaultModeTest() {
		boolean defaultMode = treeHeaderBuilder.isDefaultMode(DefaultProperties.HEADER_DEFAULT);
		assertTrue(defaultMode);

		// check when null value is passed - we should get true by default assuming the default mode
		// is a default one :)
		defaultMode = treeHeaderBuilder.isDefaultMode(null);
		assertTrue(defaultMode);

		// check if other valid value is passed
		defaultMode = treeHeaderBuilder.isDefaultMode(DefaultProperties.HEADER_BREADCRUMB);
		assertFalse(defaultMode);
	}

	/**
	 * Checks if is compact mode test.
	 */
	public void isCompactModeTest() {
		boolean compactbMode = treeHeaderBuilder.isCompactMode(DefaultProperties.HEADER_COMPACT);
		assertTrue(compactbMode);

		// check when null value is passed
		compactbMode = treeHeaderBuilder.isCompactMode(null);
		assertFalse(compactbMode);

		// check if other valid value is passed
		compactbMode = treeHeaderBuilder.isCompactMode(DefaultProperties.HEADER_BREADCRUMB);
		assertFalse(compactbMode);
	}

	/**
	 * Checks if is breadcrumb mode test.
	 */
	public void isBreadcrumbModeTest() {
		boolean breadcrumbMode = treeHeaderBuilder
				.isBreadcrumbMode(DefaultProperties.HEADER_BREADCRUMB);
		assertTrue(breadcrumbMode);

		// check when null value is passed
		breadcrumbMode = treeHeaderBuilder.isBreadcrumbMode(null);
		assertFalse(breadcrumbMode);

		// check if other valid value is passed
		breadcrumbMode = treeHeaderBuilder.isBreadcrumbMode(DefaultProperties.HEADER_COMPACT);
		assertFalse(breadcrumbMode);
	}

	/**
	 * Gets the node padding test.
	 */
	public void getCompactHeaderNodePaddingTest() {
		String padding = null;

		// check for configured default values
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(0, null);
		assertEquals(padding, "padding-left: 0px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(1, null);
		assertEquals(padding, "padding-left: 24px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(2, null);
		assertEquals(padding, "padding-left: 48px;");

		// check for invalid value passed for size - method should return value based on the default
		// size value for compact header MEDIUM
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(1, "invalid_size");
		assertEquals(padding, "padding-left: 24px;");

		// for the zero index we should always get 0px padding
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(0, "small");
		assertEquals(padding, "padding-left: 0px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(0, "medium");
		assertEquals(padding, "padding-left: 0px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(0, "big");
		assertEquals(padding, "padding-left: 0px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(0, "bigger");
		assertEquals(padding, "padding-left: 0px;");

		//
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(1, "small");
		assertEquals(padding, "padding-left: 16px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(2, "small");
		assertEquals(padding, "padding-left: 32px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(3, "small");
		assertEquals(padding, "padding-left: 48px;");
		//
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(1, "medium");
		assertEquals(padding, "padding-left: 24px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(2, "medium");
		assertEquals(padding, "padding-left: 48px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(3, "medium");
		assertEquals(padding, "padding-left: 72px;");
		//
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(1, "big");
		assertEquals(padding, "padding-left: 32px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(2, "big");
		assertEquals(padding, "padding-left: 64px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(3, "big");
		assertEquals(padding, "padding-left: 96px;");
		//
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(1, "bigger");
		assertEquals(padding, "padding-left: 64px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(2, "bigger");
		assertEquals(padding, "padding-left: 128px;");
		padding = treeHeaderBuilder.getCompactHeaderNodePadding(3, "bigger");
		assertEquals(padding, "padding-left: 192px;");
	}

	/**
	 * Test hasThumbnail if we have all the needed data.
	 */
	public void hasThumbnailTest() {
		SomeInstance instance = new SomeInstance();
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(DefaultProperties.THUMBNAIL_IMAGE, "data uri");
		instance.setProperties(properties);
		boolean hasThumbnail = treeHeaderBuilder.hasThumbnail(instance);
		assertTrue(hasThumbnail, "Should pass, there are property thumbnail into the instance.");
	}

	/**
	 * Test hasThumbnail if the argument is null.
	 */
	public void nullInstanceHasThumbnailTest() {
		boolean hasThumbnail = treeHeaderBuilder.hasThumbnail(null);
		assertFalse(hasThumbnail, "Should fail, the instance is null.");
	}

	/**
	 * Test hasThumbnail when the property of the instance is null.
	 */
	public void instanceNullPropertyHasThumbnail() {
		boolean hasThumbnail = treeHeaderBuilder.hasThumbnail(new SomeInstance());
		assertFalse(hasThumbnail, "Should fail, the instance haven't properties.");
	}

	/**
	 * Render current test.
	 */
	public void renderCurrentTest() {
		boolean renderCurrent = treeHeaderBuilder.renderCurrent(null, null, null);
		assertTrue(renderCurrent);

		renderCurrent = treeHeaderBuilder.renderCurrent("fail", null, null);
		assertTrue(renderCurrent);

		renderCurrent = treeHeaderBuilder.renderCurrent("skip_current", null, null);
		assertFalse(renderCurrent);
	}

	/**
	 * The Class SomeInstance.
	 */
	private class SomeInstance implements Instance {

		private static final long serialVersionUID = -8609505256131351378L;

		/** The id. */
		private Serializable id;

		/** The properties. */
		private Map<String, Serializable> properties;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map<String, Serializable> getProperties() {
			return properties;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setProperties(Map<String, Serializable> properties) {
			this.properties = properties;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getRevision() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public PathElement getParentElement() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasChildren() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Node getChild(String name) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getIdentifier() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setIdentifier(String identifier) {

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getId() {
			return (Long) id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setId(Serializable id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setRevision(Long revision) {

		}

		@Override
		public InstanceReference toReference() {
			return null;
		}

	}

}
