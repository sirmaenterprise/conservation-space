package com.sirma.itt.cmf.alfresco4.evaluators;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;

/**
 * Evaluator that handle generation of DMS links for folder details page
 *
 * @author BBonev
 */
@ApplicationScoped
public class DmsFolderLinkEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5046450111482046063L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{dmsFolderLink\\(([\\w]+)\\)\\}");

	@Inject
	@Config(name = CmfConfigurationProperties.DMS_HOST)
	private String dmsHost;
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_PORT)
	private Integer dmsPort;
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_PROTOCOL, defaultValue = "http")
	private String dmsProtocol;

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
				+ "/share/page/site/{0}/folder-details?nodeRef={1}";
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
		if (instance instanceof SectionInstance) {
			Serializable site = ((SectionInstance) instance).getProperties().get("siteName");
			String dmsId = ((SectionInstance) instance).getDmsId();
			if ((site != null) && (dmsId != null)) {
				String link = MessageFormat.format(linkTemplate, site, dmsId);
				return link;
			}
		}
		return null;
	}

}
