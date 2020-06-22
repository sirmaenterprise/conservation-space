package com.sirma.itt.seip.content.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.ContentInfo;

/**
 * Expression evaluator for the instance thumbnails.
 *
 * @author Nikolay Ch
 */
@Singleton
public class ThumbnailEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 1L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START + "\\{thumbnailUri\\(([0-9]+)\\)\\}");
	private static final String PATH_TO_CLASS_ICON = "/remote/api/content/resource/";
	private static final String PATH_TO_DEFAULT_ICON = "/images/instance-icons/";
	private static final String DEFAULT_HEADER_PURPOSE = "64";

	@Inject
	private SecurityContext securityContext;

	@Inject
	private ContentResourceManagerService contentService;

	@Inject
	private TypeConverter typeConverter;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "thumbnailUri";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String purpose = matcher.group(1);
		Instance currentInstance = (Instance) evaluateFrom(null, context, values);
		if (currentInstance == null || currentInstance.type() == null) {
			return null;
		}
		Serializable instanceThumbnail = currentInstance.get("thumbnailImage");
		Serializable typeId = currentInstance.type().getId();
		Serializable instanceType = currentInstance.type().getCategory();

		if (instanceThumbnail != null && purpose.equals(DEFAULT_HEADER_PURPOSE) && !isClassInstance(instanceType)) {
			return instanceThumbnail;
		}
		Serializable classId;
		if (isClassInstance(instanceType)) {
			classId = currentInstance.getId();
		} else {
			classId = typeConverter.convert(ShortUri.class, typeId);
		}

		ContentInfo contentInfo = contentService.getContent(classId.toString(), purpose);
		if (contentInfo.exists()) {
			String tenantId = securityContext.getCurrentTenantId();
			return PATH_TO_CLASS_ICON + tenantId + "/" + contentInfo.getContentId();
		}

		String defaultImagePath;
		if (instanceType != null) {
			defaultImagePath = PATH_TO_DEFAULT_ICON + instanceType + "-icon-" + purpose + ".png";
		} else {
			defaultImagePath = PATH_TO_DEFAULT_ICON + "documentinstance-icon-" + purpose + ".png";
		}
		return defaultImagePath;
	}

	private boolean isClassInstance(Serializable instanceType) {
		return instanceType != null && "classinstance".equals(instanceType.toString());
	}

}
