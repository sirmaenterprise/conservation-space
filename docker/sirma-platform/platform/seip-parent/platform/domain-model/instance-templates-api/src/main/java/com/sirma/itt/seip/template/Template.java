package com.sirma.itt.seip.template;

import com.sirma.itt.seip.template.rules.TemplateRuleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a holder for template data.
 *
 * @author Vilizar Tsonev
 */
public class Template {

	private String id;

	private String title;

	private String forType;

	private boolean primary;

	private String purpose;

	private String rule;

	private int ruleWeight;

	private String correspondingInstance;

	private String publishedInstanceVersion;

	private String content;

	private String contentDigest;

	private Date modifiedOn;

	private String modifiedBy;


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String identifier) {
		this.id = identifier;
	}

	public String getForType() {
		return forType;
	}

	public void setForType(String forType) {
		this.forType = forType;
	}

	public boolean getPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String getContentDigest() {
		return contentDigest;
	}

	public void setContentDigest(String contentDigest) {
		this.contentDigest = contentDigest;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
		setRuleWeight();
	}

	public int getRuleWeight() {
		return ruleWeight;
	}

	private void setRuleWeight() {
		this.ruleWeight = calculateRuleWeight(getRule());
	}

	public String getCorrespondingInstance() {
		return correspondingInstance;
	}

	public void setCorrespondingInstance(String correspondingInstance) {
		this.correspondingInstance = correspondingInstance;
	}

	public String getPublishedInstanceVersion() {
		return publishedInstanceVersion;
	}

	public void setPublishedInstanceVersion(String publishedInstanceVersion) {
		this.publishedInstanceVersion = publishedInstanceVersion;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Template)) {
			return false;
		}
		Template template = (Template) o;
		return primary == template.primary &&
				Objects.equals(id, template.id) &&
				Objects.equals(title, template.title) &&
				Objects.equals(forType, template.forType) &&
				Objects.equals(purpose, template.purpose) &&
				Objects.equals(rule, template.rule) &&
				Objects.equals(correspondingInstance, template.correspondingInstance) &&
				Objects.equals(publishedInstanceVersion, template.publishedInstanceVersion) &&
				Objects.equals(content, template.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, forType, primary, purpose, rule, correspondingInstance,
				publishedInstanceVersion, content);
	}

	private int calculateRuleWeight(String rule) {
		if (StringUtils.isBlank(rule)) {
			return 0;
		}
		return TemplateRuleUtils.parseRule(rule).size();
	}
}
