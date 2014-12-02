package com.sirma.itt.idoc.web.util.sanitize;

import java.util.List;

import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamRenderer;

/**
 * This class handles the final rendering of the html. It listens for {@code widget} open and close
 * tags. If a {@code widget} open tag is detected it removes any child elements until a close tag is
 * detected. Other tags and text are delegated to the underlying renderer.
 * 
 * @author yasko
 */
public class WidgetRenderer implements HtmlStreamEventReceiver {
	
	public static final String WIDGET_TAG = "widget";
	
	private boolean inWidget = false;
	private HtmlStreamRenderer delegate;

	/**
	 * Constructs the widget renderer instance.
	 * 
	 * @param out
	 *            where the final html is writen.
	 */
	public WidgetRenderer(Appendable out) {
		this.delegate = HtmlStreamRenderer.create(out, null, null);
	}

	@Override
	public void openDocument() {
		delegate.openDocument();
	}

	@Override
	public void closeDocument() {
		delegate.closeDocument();
	}

	@Override
	public void openTag(String elementName, List<String> attrs) {
		if (WidgetRenderer.WIDGET_TAG.equals(elementName)) {
			inWidget = true;
			delegate.openTag(elementName, attrs);
			return;
		}
		if (!inWidget) {
			delegate.openTag(elementName, attrs);
		}
	}

	@Override
	public void closeTag(String elementName) {
		if (WidgetRenderer.WIDGET_TAG.equals(elementName)) {
			inWidget = false;
			delegate.closeTag(elementName);
			return;
		}
		if (!inWidget) {
			delegate.closeTag(elementName);
		}
	}

	@Override
	public void text(String text) {
		if (!inWidget) {
			delegate.text(text);
		}
	}

}