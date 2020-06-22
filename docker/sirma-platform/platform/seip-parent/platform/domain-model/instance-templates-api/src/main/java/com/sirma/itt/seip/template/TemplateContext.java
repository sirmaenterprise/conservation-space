package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import com.sirma.itt.seip.context.Context;

/**
 * Template context used in {@link TemplatePreProcessor}. Wraps a {@link Template} and additional parameters
 * that will be available during template pre processing
 *
 * @author BBonev
 */
public class TemplateContext extends Context<String, Object> {

	private static final long serialVersionUID = 2093129604010066942L;
	private final Template template;

	/**
	 * Instantiate new template context for the given template
	 *
	 * @param template
	 *            the template instance
	 */
	public TemplateContext(Template template) {
		this.template = template;
	}

	/**
	 * @return the template instance
	 */
	public Template getTemplate() {
		return template;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof TemplateContext)) {
			return false;
		}
		TemplateContext other = (TemplateContext) obj;
		return nullSafeEquals(template, other.template);
	}
}
