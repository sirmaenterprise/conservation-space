package com.sirma.itt.emf.semantic;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.model.OpenRdfUriProxy;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link NamespaceRegistry}
 *
 * @author Valeri Tishev
 */
public class NamespaceRegistryTest extends GeneralSemanticTest<NamespaceRegistryService> {

	private NamespaceRegistryService cut;

	/**
	 * Register EMF namespace.
	 */
	@BeforeClass
	public void registerEmfNamespace() {
		cut = new NamespaceRegistryMock(context);
	}

	/**
	 * Test EMF namespace.
	 */
	@Test
	public void testEmfNamespace() {
		String expectedEmfNamespace = EMF.NAMESPACE;
		String actualEmfNamespace = cut.getNamespace(EMF.PREFIX);
		Assert.assertEquals(actualEmfNamespace, expectedEmfNamespace);
	}

	/**
	 * Test getting property full uri.
	 */
	@Test
	public void testGettingPropertyFullUri() {
		// define expected property uri as
		// "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#someProperty"
		String expectedEmfNamespace = EMF.NAMESPACE;
		String expectedProperty = "someProperty";
		String expectedPropertyUri = expectedEmfNamespace + expectedProperty;

		String actualPropertyUri = cut
				.buildFullUri(EMF.PREFIX + NamespaceRegistry.SHORT_URI_DELIMITER + expectedProperty);

		Assert.assertEquals(actualPropertyUri, expectedPropertyUri);
	}

	/**
	 * Test getting property full uri with null short uri.
	 */
	@Test(expectedExceptions = java.lang.IllegalArgumentException.class)
	public void testGettingPropertyFullUriWithNullShortUri() {
		cut.buildFullUri(null);
	}

	/**
	 * Test getting property full uri with malformed short uri.
	 */
	@Test(expectedExceptions = java.lang.IllegalArgumentException.class)
	public void testGettingPropertyFullUriWithMalformedShortUri1() {
		cut.buildFullUri("malformed/uri");
	}

	/**
	 * Test getting property full uri with malformed short uri.
	 */
	@Test(expectedExceptions = java.lang.IllegalArgumentException.class)
	public void testGettingPropertyFullUriWithMalformedShortUri2() {
		cut.buildFullUri("malformeduri");
	}

	/**
	 * Test getting property full uri with malformed short uri.
	 */
	@Test(expectedExceptions = java.lang.IllegalArgumentException.class)
	public void testGettingPropertyFullUriWithMalformedShortUri3() {
		cut.buildFullUri("malformed:uri:");
	}

	/**
	 * Test getting property full uri from undefined namespace.
	 */
	@Test(expectedExceptions = java.lang.IllegalStateException.class)
	public void testGettingPropertyFullUriFromUndefinedNamespace() {
		cut.buildFullUri("unknownPrefix:someProperty");
	}

	/**
	 * Test getting short dcterms uri.
	 */
	@Test
	public void testGettingShortDctermsUri() {
		URI fullURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/title");
		String expectedShortURI = "dcterms:title";
		String actualShortURI = cut.getShortUri(fullURI);

		Assert.assertEquals(expectedShortURI, actualShortURI);
	}

	/**
	 * Test getting unknown short uri.
	 */
	@Test(expectedExceptions = java.lang.IllegalStateException.class)
	public void testGettingUnknownShortUri() {
		URI fullURI = ValueFactoryImpl.getInstance().createURI("http://unknown");
		cut.getShortUri(fullURI);
	}

	/**
	 * Tests getShortUri(Uri fullUri) with null value
	 */
	@Test(expectedExceptions = java.lang.IllegalArgumentException.class)
	public void testGetShortUriNullvalue() {
		cut.getShortUri((Uri) null);
	}

	/**
	 * Tests getShortUri(Uri fullUri) with existing namespace
	 */
	@Test
	public void testGetShortUri() {
		Uri uri = new OpenRdfUriProxy(ValueFactoryImpl.getInstance()
				.createURI("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isBlockedBy"));
		String shortUri = cut.getShortUri(uri);
		Assert.assertEquals(shortUri, "emf:isBlockedBy");
	}

	/**
	 * Tests getShortUri(Uri fullUri) with illegal namespace
	 */
	@Test(expectedExceptions = java.lang.IllegalStateException.class)
	public void testGetShortUriIllegalNamespace() {
		Uri uri = new OpenRdfUriProxy(ValueFactoryImpl.getInstance().createURI("http://Framework#isBlockedBy"));
		cut.getShortUri(uri);
	}

	/**
	 * Tests getShortUri(Uri fullUri) with null value
	 */
	@Test(expectedExceptions = java.lang.IllegalArgumentException.class)
	public void testGetShortUriStringNullvalue() {
		cut.getShortUri((String) null);
	}

	/**
	 * Tests getShortUri(Uri fullUri) with existing namespace
	 */
	@Test
	public void testGetShortUriString() {
		String shortUri = cut
				.getShortUri("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isBlockedBy");
		Assert.assertEquals(shortUri, "emf:isBlockedBy");
	}

	/**
	 * Tests getShortUri(Uri fullUri) with illegal namespace
	 */
	@Test(expectedExceptions = java.lang.IllegalStateException.class)
	public void testGetShortUriStringIllegalNamespace() {
		cut.getShortUri("http://Framework#isBlockedBy");
	}

	/**
	 * Tests getDataGraph with configured data graph
	 */
	@Test
	public void testGetDataGraph() {
		String expectedDataGraph = "http://dataGraph";
		Options.USE_CUSTOM_GRAPH.set(expectedDataGraph);
		URI dataGraph = cut.getDataGraph();
		Assert.assertNotNull(dataGraph);
		Assert.assertEquals(dataGraph.toString(), expectedDataGraph);
		Options.USE_CUSTOM_GRAPH.clear();
	}

	/**
	 * Tests getDataGraph without configured data graph
	 */
	@Test
	public void testGetDataGraphWithoutConfiguredOption() {
		String expectedDataGraph = "http://ittruse.ittbg.com/data/enterpriseManagementFramework";
		URI dataGraph = cut.getDataGraph();
		Assert.assertNotNull(dataGraph);
		Assert.assertEquals(dataGraph.toString(), expectedDataGraph);
	}

	/**
	 * Tests buildUri(String uri)
	 */
	@Test
	public void testBuildUri() {
		String expectedUri = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isBlockedBy";
		URI uri = cut.buildUri("emf:isBlockedBy");
		Assert.assertEquals(uri.toString(), expectedUri);
	}

	/**
	 * Tests getNamespaces
	 */
	@Test
	public void testGetNamespaces() {
		String namespaces = cut.getNamespaces();
		Assert.assertTrue(StringUtils.isNotNullOrEmpty(namespaces));
	}

	@Override
	protected String getTestDataFile() {
		// no test data needed so far
		return null;
	}

}
