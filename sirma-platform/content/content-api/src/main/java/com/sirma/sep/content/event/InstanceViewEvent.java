package com.sirma.sep.content.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentValidationException;

/**
 * Event fired to notify for new instance view being added. The event provides access to parsed instance of the view.
 * Note that changes to this view will not be reflected to the actual content. To modify the view before save add an
 * extension implementation of {@link com.sirma.sep.content.InstanceViewPreProcessor}.
 *
 * @author BBonev
 */
public abstract class InstanceViewEvent extends ContentEvent {

	private Document parsed;

	/**
	 * Instantiates a new instance view added event.
	 *
	 * @param owner the owner
	 * @param newView the new view
	 */
	public InstanceViewEvent(Serializable owner, Content newView) {
		super(owner, newView);
	}

	/**
	 * Gets parsed instance view
	 *
	 * @return the Jsoup {@link Document} instance containing the view
	 */
	public synchronized Document getView() {
		if (parsed == null) {
			try (InputStream stream = getContent().getContent().getInputStream()) {
				parsed = Jsoup.parse(stream, getContent().getCharset(), "");
			} catch (IOException e) {
				throw new ContentValidationException("Could not read the view content", e);
			}
		}
		return parsed;
	}
}
