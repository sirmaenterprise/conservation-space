package com.sirma.itt.cmf.alfresco4.evaluators;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;

/**
 * Evaluator that handle generation of DMS links for document details page
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DmsDocumentLinkEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6134570858483875651L;
	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{dmsDocumentLink\\(([\\w]+)\\)\\}");
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_HOST)
	private String dmsHost;
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_PORT)
	private Integer dmsPort;
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_PROTOCOL, defaultValue = "http")
	private String dmsProtocol;
	/** The link template. */
	private String linkTemplate;

	/**
	 * Initializes the link template
	 */
	@PostConstruct
	public void init() {
		if ((dmsHost == null) || (dmsPort == null)) {
			logger.warn("Dms host and/or port are not configured!");
			return;
		}
		linkTemplate = dmsProtocol + "://" + dmsHost + ":" + dmsPort
				+ "/share/page/site/{0}/document-details?nodeRef={1}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		Serializable instance = getCurrentInstance(context, values);
		if (instance instanceof DocumentInstance) {
			Serializable site = ((DocumentInstance) instance).getProperties().get("siteName");
			String dmsId = ((DocumentInstance) instance).getDmsId();
			if ((site != null) && (dmsId != null)) {
				String link = MessageFormat.format(linkTemplate, site, dmsId);
				return link;
			}
		}
		return null;
	}

}
