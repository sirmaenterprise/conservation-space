/*
 *
 */
package com.sirma.itt.imports;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.actions.ActionTypeConstants;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.descriptors.ByteArrayFileDescriptor;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Parser that uses information collected from {@link CsvImportParser} to update a html file with
 * information for the processed instances.
 *
 * @author BBonev
 */
@ApplicationScoped
public class HtmlAnnotationParser {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlAnnotationParser.class);
	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The expressions manager. */
	@Inject
	private ExpressionsManager expressionsManager;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/**
	 * Creates the annotation.
	 *
	 * @param originalInstance
	 *            the original instance
	 * @param pathToValueMapping
	 *            the instances
	 * @param htmlProperties
	 *            the html properties
	 * @return the instance
	 */
	public Instance createAnnotation(Instance originalInstance,
			Map<String, AnnotationEntry> pathToValueMapping,
			Map<String, Serializable> htmlProperties) {
		if ((htmlProperties == null) || htmlProperties.isEmpty()) {
			LOGGER.warn("No html file information found. No annotation will be performed.");
			return null;
		}

		Element root = readXMLDom(htmlProperties.get(DocumentProperties.FILE_LOCATOR));

		if (root == null) {
			LOGGER.warn("Failed to read and parse the html for annotation or no file provided.");
			return null;
		}

		DocumentInstance annotatedInstance = getOrCreateDocumentForAnnotation(originalInstance,
				htmlProperties);

		String query;
		for (Entry<String, AnnotationEntry> entry : pathToValueMapping
				.entrySet()) {
			query = entry.getKey();

			Elements selector = Selector.select(query, root);
			if (!selector.isEmpty()) {
				if (selector.size() > 1) {
					LOGGER.warn("The selector {} found {} elements. Will update for all of them.",
							query, selector.size());
				}
				for (Element element : selector) {
					Node parent = element.parent();
					if (parent != null) {
						// wraps the elements html data in outer element that have a link if any
						String currentHtml = element.outerHtml();
						String fragmentHtml = createNode(annotatedInstance, entry.getValue(),
								currentHtml);
						if (!currentHtml.equals(fragmentHtml)) {
							List<Node> parseFragment = parseFragment(parent, fragmentHtml);

							element.replaceWith(parseFragment.get(0));
						}
					} else {
						LOGGER.warn(
								"The selector returned a node without parent. Nothing is changed for node {}={}",
								element.nodeName(), element.html());
					}
				}
			}
		}

		insertWidgets(root);

		updateFileContents(annotatedInstance.getProperties(), root);

		instanceService.save(annotatedInstance, new Operation(ActionTypeConstants.UPLOAD));
		return annotatedInstance;
	}

	/**
	 * Insert widgets.
	 * 
	 * @param root
	 *            the root
	 */
	private void insertWidgets(Element root) {
		String objectdata = "<div><widget name=\"objectData\"></widget></div>";
		String relations = "<div class=\"noneditable\"><widget name=\"relationships\"></widget></div>";

		Elements select = root.select("html:eq(0) body:eq(1) div:eq(0)");
		if (!select.isEmpty()) {
			select.get(0).insertChildren(0, parseFragment(select.get(0), objectdata));
		}
		select = root.select("div[style=mso-element:footer]");
		if (!select.isEmpty()) {
			select.get(0).before(parseFragment(select.get(0), relations).get(0));
		}

	}

	/**
	 * Parses the fragment.
	 *
	 * @param parent
	 *            the parent
	 * @param fragmentHtml
	 *            the fragment html
	 * @return the list
	 */
	private List<Node> parseFragment(Node parent, String fragmentHtml) {
		List<Node> parseFragment = Parser.parseFragment(fragmentHtml,
				(Element) parent, parent.baseUri());
		// if the parser returns more then one element this means we can't
		// replace the node because we have to update with all nodes not just
		// the first. So we are going to wrap them in a span and build it again
		if (parseFragment.size() > 1) {
			parseFragment = parseFragment(parent, "<span>" + fragmentHtml + "</span>");
		}
		return parseFragment;
	}

	/**
	 * Gets the or create document for annotation.
	 *
	 * @param originalInstance
	 *            the original instance
	 * @param htmlProperties
	 *            the html properties
	 * @return the or create document for annotation
	 */
	private DocumentInstance getOrCreateDocumentForAnnotation(Instance originalInstance,
			Map<String, Serializable> htmlProperties) {
		DocumentInstance annotatedInstance = null;
		if (originalInstance != null) {
			List<LinkReference> links = linkService.getLinks(originalInstance.toReference(), LinkConstants.REFERENCES_URI);
			if (!links.isEmpty()) {
				for (LinkReference reference : links) {
					if (reference.getTo().getReferenceType().getJavaClass().equals(DocumentInstance.class)) {
						Instance instance = reference.getTo().toInstance();

						if ((instance instanceof DocumentInstance)
								&& EqualsHelper.nullSafeEquals(
										((DocumentInstance) instance).getPurpose(), "iDoc", true)) {
							annotatedInstance = (DocumentInstance) instance;
						}
					}
				}
			}
		}

		if (annotatedInstance == null) {
			// create document instance and save it
			EmfInstance instance = new EmfInstance();
			instance.setProperties(new LinkedHashMap<String, Serializable>());
			if (originalInstance != null) {
				instance.setIdentifier(originalInstance.getIdentifier());
				instance.setRevision(originalInstance.getRevision());
				Map<String, Serializable> properties = PropertiesUtil
						.cloneProperties(originalInstance.getProperties());
				properties.remove(DocumentProperties.ATTACHMENT_LOCATION);
				properties.remove(DocumentProperties.FILE_LOCATOR);
				properties.remove(DocumentProperties.FILE_SIZE);
				instance.getProperties().putAll(properties);
			}
			instance.getProperties().putAll(htmlProperties);
			DocumentInstance documentInstance = typeConverter.convert(DocumentInstance.class,
					instance);
			documentInstance.setPurpose("iDoc");
			annotatedInstance = documentInstance;
		} else {
			annotatedInstance.getProperties().putAll(htmlProperties);
		}
		return annotatedInstance;
	}

	/**
	 * Update file contents.
	 *
	 * @param properties
	 *            the html properties
	 * @param root
	 *            the root
	 */
	private void updateFileContents(Map<String, Serializable> properties, Element root) {
		Serializable serializable = properties.get(DocumentProperties.FILE_LOCATOR);
		if (serializable instanceof FileDescriptor) {
			FileDescriptor descriptor = (FileDescriptor) serializable;
			try {
				properties.put(DocumentProperties.FILE_LOCATOR,
						new ByteArrayFileDescriptor(descriptor.getId(),
								descriptor.getContainerId(), root.html().getBytes("utf-8")));
			} catch (UnsupportedEncodingException e) {
				LOGGER.debug("", e);
			}
			if (properties.containsKey(DocumentProperties.ATTACHMENT_LOCATION)) {
				properties.put(DocumentProperties.IS_MAJOR_VERSION, Boolean.FALSE);
				properties.put(DocumentProperties.VERSION_DESCRIPTION, "Annotation update");
			}
		}
	}

	/**
	 * Creates the node.
	 *
	 * @param instance
	 *            the instance
	 * @param entry
	 *            the pair
	 * @param originalText
	 *            the original text
	 * @return the string
	 */
	private String createNode(Instance instance, AnnotationEntry entry,
			String originalText) {
		Serializable value = entry.getUpdatedValue();
		if ("LINK".equals(entry.getOptions())) {
			ExpressionContext context = expressionsManager.createDefaultContext(
					entry.getOwningInstance(), null, null);

			String expression = getExpression(entry, originalText);
			String string = expressionsManager.evaluateRule(expression, String.class, context);
			if (StringUtils.isBlank(string)) {
				return originalText;
			}
			return string;
		} else if (value instanceof Instance) {
			ExpressionContext context = expressionsManager.createDefaultContext((Instance) value,
					null, null);

			String expression = getExpression(entry, originalText);
			String string = expressionsManager.evaluateRule(expression, String.class, context);
			if (StringUtils.isBlank(string)) {
				return originalText;
			}
			return string;
		}
		return originalText;
	}

	/**
	 * Gets the expression.
	 *
	 * @param entry
	 *            the entry
	 * @param originalText
	 *            the original text
	 * @return the expression
	 */
	private String getExpression(AnnotationEntry entry, String originalText) {
		/*if (entry.getUpdatedValue() instanceof Resource) {
			Resource resource = (Resource) entry.getUpdatedValue();
			return "${eval(<a href=\"${userLink(" + resource.getId() + ")}\">" + originalText
					+ "</a>)}";
		} else */if ("LINK".equals(entry.getOptions())
				|| (entry.getUpdatedValue() instanceof Instance)) {
			return insertLinkExpression(originalText, entry.getOriginalValue(),
					"<a href=\"${link(currentInstance)}\">", "</a>");
		}
		return originalText;
	}

	/**
	 * Insert link expression.
	 *
	 * @param originalText
	 *            the original text
	 * @param value
	 *            the value
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @return the string
	 */
	private String insertLinkExpression(String originalText, String value, String begin, String end) {
		String replaced = originalText.replace(value, begin + value + end);
		if (replaced.equals(originalText)) {
			// replaced = begin + originalText + end;
		}
		return "${eval(" + replaced + ")}";
	}

	/**
	 * Read xml dom.
	 *
	 * @param serializable
	 *            the serializable
	 * @return the element
	 */
	private Element readXMLDom(Serializable serializable) {
		if (serializable instanceof FileDescriptor) {
			InputStream stream = ((FileDescriptor) serializable).getInputStream();
			try {
				if (stream == null) {
					return null;
				}
				Document document = Jsoup.parse(stream, "utf-8", "");
				return document;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return null;
	}
}
