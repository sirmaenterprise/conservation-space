package com.sirma.itt.idoc.web.util.sanitize;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;

/**
 * Test for html cleanup policy.
 * 
 * @author yasko
 */
public class DefaultPolicyTest {

	/**
	 * Test if the default policy will remove script, style, iframe, link and noscript tags.
	 */
	// @Test
	public void testRemoveScriptingTags() {
		String clean = null;

		String dirty = "<div> alabala <i>some italics<script>emptyBankAccount();</script> and some more</i>";
		clean = HTMLPolicy.POLICY_DEFINITION.sanitize(dirty);
		Assert.assertEquals("<div> alabala <i>some italics and some more</i></div>", clean);

		dirty = "<p><style>.this-tag-should-be-removed { color: red; } </style> yo!</p>";
		clean = HTMLPolicy.POLICY_DEFINITION.sanitize(dirty);
		Assert.assertEquals("<p> yo!</p>", clean);

		dirty = "<div> alabala asda ad <iframe>emptyBankAccount();</div></iframe> sad0-aosda";
		clean = HTMLPolicy.POLICY_DEFINITION.sanitize(dirty);
		Assert.assertEquals("<div> alabala asda ad  sad0-aosda</div>", clean);

		dirty = "<div> alabala asda ad <link href=\"http://evil.org\" type=\"text/css\" rel=\"stylesheet\" /> sad0-aosda";
		clean = HTMLPolicy.POLICY_DEFINITION.sanitize(dirty);
		Assert.assertEquals("<div> alabala asda ad  sad0-aosda</div>", clean);

		dirty = "<div><noscript><form method=POST action=http://evil.org/><input name=x value=y><input type=submit></form></noscript></div>";
		clean = HTMLPolicy.POLICY_DEFINITION.sanitize(dirty);
		Assert.assertEquals("<div></div>", clean);
	}

	/**
	 * Test removing of on* attributes. They are removed by default, this is just to make sure we
	 * don't accidentally enable them.
	 */
	// @Test
	public void testRemoveOnAttributes() {
		String dirty = "<div onblur=\"doBAdStuff();\" onchange=\"doBAdStuff();\" onclick=\"doBAdStuff();\""
				+ " ondblclick=\"doBAdStuff();\" onfocus=\"doBAdStuff();\" onkeydown=\"doBAdStuff();\""
				+ " onkeypress=\"doBAdStuff();\" onkeyup=\"doBAdStuff();\""
				+ " onload=\"doBAdStuff();\" onmousedown=\"doBAdStuff();\""
				+ " onmousemove=\"doBAdStuff();\" onmouseout=\"doBAdStuff();\""
				+ " onmouseover=\"doBAdStuff();\" onmouseup=\"doBAdStuff();\""
				+ " onreset=\"doBAdStuff();\" onselect=\"doBAdStuff();\""
				+ " onsubmit=\"doBAdStuff();\" onunload=\"doBAdStuff();\">asd</div>";
		Assert.assertEquals("<div>asd</div>", HTMLPolicy.sanitize(dirty));
	}

	/**
	 * Allow anchor tags with no js in href attribute, also add rel nofollow.
	 */
	// @Test
	public void testRemoveJsCalls() {
		String dirty = "<a href=\"javascript:stealCookies();\">Click me, I'm harmless - promise!</a>";

		Assert.assertEquals("Click me, I&#39;m harmless - promise!", HTMLPolicy.sanitize(dirty));

		dirty = "<a href=\"/this/should/pass\">Click me, I'm harmless - promise!</a>";

		Assert.assertEquals(
				"<a href=\"/this/should/pass\" rel=\"nofollow\">Click me, I&#39;m harmless - promise!</a>",
				HTMLPolicy.sanitize(dirty));

		dirty = "<a onclick=\"javascript:exploitUser()\">Click me, I'm harmless - promise!</a>";

		Assert.assertEquals("Click me, I&#39;m harmless - promise!", HTMLPolicy.sanitize(dirty));

		dirty = "<a onclick=\"exploitUser()\">Click me, I'm harmless - promise!</a>";

		Assert.assertEquals("Click me, I&#39;m harmless - promise!", HTMLPolicy.sanitize(dirty));
	}

	/**
	 * Test if the default policy will remove class added just for visual aid at runtime.
	 */
	// @Test
	public void testRemoveIdocSpecificVisualOnlyClasses() {
		String dirty = "<h1 class=\"h1 idoc-visual-selected-heading\">This is heading level 1</h1>";
		Assert.assertEquals("<h1 class=\"h1\">This is heading level 1</h1>",
				HTMLPolicy.sanitize(dirty));
	}

	/**
	 * Test that the policy removes angularjs specific css classes - those starting with ng-.
	 */
	// @Test
	public void testRemoveAngularClasses() {
		String dirty = "<h1 class=\"h1 ng-scope\">This is heading level 1</h1>";
		Assert.assertEquals("<h1 class=\"h1\">This is heading level 1</h1>",
				HTMLPolicy.sanitize(dirty));

		// also test that empty class attributes are removed
		dirty = "<h1 class=\"ng-scope\">This is heading level 1</h1>";
		Assert.assertEquals("<h1>This is heading level 1</h1>", HTMLPolicy.sanitize(dirty));
	}

	/**
	 * Test for widget tag attribute cleanup. A widget cannot exist without any attributes and only
	 * allowed is the config attribute.
	 */
	// @Test
	public void testWidgetAttributeCleanup() {
		String dirty = "<widget>only text</widget>";
		Assert.assertEquals("only text", HTMLPolicy.sanitize(dirty));

		// test for validness of named attribute
		dirty = "<widget name=\"image\"></widget>";
		Assert.assertEquals(dirty, HTMLPolicy.sanitize(dirty));

		dirty = "<widget type=\"image\">something else</widget>";
		Assert.assertEquals("something else", HTMLPolicy.sanitize(dirty));

		dirty = "<widget config=\"{ type: 'image'}\" type=\"image\">something</widget>";
		Assert.assertEquals("<widget config=\"{ type: &#39;image&#39;}\"></widget>",
				HTMLPolicy.sanitize(dirty));
	}

	/**
	 * The widget tag should be an empty tag, all children are create at runtime by angularjs based
	 * on the widget configuration. Speaking of configuration ...the only allowed attribute is
	 * {@code config}, everything else is remove. A widget with no attributes at all is also
	 * removed.
	 */
	// @Test
	public void testRemoveWidgetContent() {
		String dirty = "<widget config=\"{ text: 'something' }\" contenteditable=\"false\"><div>something</div></widget>";
		Assert.assertEquals("<widget config=\"{ text: &#39;something&#39; }\"></widget>",
				HTMLPolicy.sanitize(dirty));

		dirty = "<widget><em>something</em></widget>";
		Assert.assertEquals("<em>something</em>", HTMLPolicy.sanitize(dirty));

		dirty = "<widget type=\"link\"><em>something</em></widget>";
		Assert.assertEquals("<em>something</em>", HTMLPolicy.sanitize(dirty));
	}

	/**
	 * Test for 'document' config tag. The config attribute is optional, but the only one that is
	 * allowed, it contains the JASON configuration for the document.
	 */
	// @Test
	public void testDocumentConfigTag() {
		String dirty = "<document><div>tesdt</div></document>";
		Assert.assertEquals("<document><div>tesdt</div></document>", HTMLPolicy.sanitize(dirty));

		dirty = "<document config=\"{ showTitle: true }\"><p>asd</p</document>";
		Assert.assertEquals("<document config=\"{ showTitle: true }\"><p>asd</p></document>",
				HTMLPolicy.sanitize(dirty));
	}

	/**
	 * Tests entire content.
	 * 
	 * @throws URISyntaxException
	 *             bad syntax
	 * @throws IOException
	 *             something bad
	 */
	public void testWithEntireDocument() throws IOException, URISyntaxException {
		String content = readFileAsString("/data/widget.xml");
		String expected = readFileAsString("/data/result.xml");

		String result = HTMLPolicy.sanitize(content);

		Assert.assertEquals(expected, result);
	}

	/**
	 * Reads a file and return it as string.
	 * 
	 * @param filePath
	 *            file to read.
	 * @return file as string.
	 */
	private String readFileAsString(String filePath) {
		URL url = DefaultPolicyTest.class.getResource(filePath);
		try {
			return FileUtils.readFileToString(new File(url.toURI()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
