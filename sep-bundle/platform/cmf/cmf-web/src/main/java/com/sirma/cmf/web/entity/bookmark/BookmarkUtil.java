package com.sirma.cmf.web.entity.bookmark;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.services.LinkProviderService;
import com.sirma.itt.emf.web.application.EmfApplication;

/**
 * BookmarkUtil is responsible of building bookmarkable links to given instance. If any of required
 * request parameters are missing, then the link builder fails and builds an empty link as result.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class BookmarkUtil implements LinkProviderService {

	/** The log. */
	private final Logger log = Logger.getLogger(BookmarkUtil.class);

	/** Pattern to match open html a tags. */
	private final Pattern openHtmlATag = Pattern.compile("<a ", Pattern.CANON_EQ);

	/** The Constant BROKEN_LINK. */
	private static final String BROKEN_LINK = "";

	/** The Constant OPENER_LINK. */
	private static final String OPENER_LINK = "/entity/open.jsf?";

	/**
	 * An application scoped instance that is being initialized with convenient data on application
	 * startup.
	 */
	@Inject
	private EmfApplication emfApplication;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Getter method for requestContextPath.
	 * 
	 * @return the requestContextPath
	 */
	public String getRequestContextPath() {
		return emfApplication.getContextPath() + OPENER_LINK;
	}

	/**
	 * Builds bookmarkable link for provided instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return bookamark link
	 */
	@Override
	public String buildLink(Instance instance) {
		return build(instance).toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildLink(Instance instance, String tab) {
		StringBuilder linkBuilder = build(instance);
		addTabName(linkBuilder, tab);
		return linkBuilder.toString();
	}

	/**
	 * Builds the.
	 * 
	 * @param instance
	 *            the instance
	 * @return the string builder
	 */
	private StringBuilder build(Instance instance) {
		StringBuilder linkBuilder = new StringBuilder();

		// this link should not go anywhere
		if (instance == null) {
			log.warn("BookmarkUtil: build link invoked for null instance!");
			return brokenLink(linkBuilder);
		}
		InstanceReference instanceReference = typeConverter.convert(InstanceReference.class,
				instance);
		addRequestContextPath(linkBuilder, instanceReference);
		return linkBuilder;
	}

	/**
	 * Adds the tab name.
	 * 
	 * @param linkBuilder
	 *            the link builder
	 * @param tab
	 *            the tab
	 */
	private void addTabName(StringBuilder linkBuilder, String tab) {
		if (StringUtils.isNotNullOrEmpty(tab)) {
			linkBuilder.append("&tab=").append(tab);
		}
	}

	/**
	 * Adds the request context path.
	 * 
	 * @param linkBuilder
	 *            the link builder
	 * @param instanceReference
	 *            the instance reference
	 * @return the string builder
	 */
	private StringBuilder addRequestContextPath(StringBuilder linkBuilder,
			InstanceReference instanceReference) {
		linkBuilder.append(getRequestContextPath());
		return addType(linkBuilder, instanceReference);
	}

	/**
	 * Adds the type parameter to the link.
	 * 
	 * @param linkBuilder
	 *            the link builder
	 * @param instanceReference
	 *            the instance reference
	 * @return link builder
	 */
	private StringBuilder addType(StringBuilder linkBuilder, InstanceReference instanceReference) {
		DataTypeDefinition referenceType = instanceReference.getReferenceType();
		if (referenceType == null) {
			log.warn("BookmarkUtil: can't build link for null instance type!");
			return brokenLink(linkBuilder);
		}
		String name = referenceType.getName();
		linkBuilder.append("type=").append(name);
		return addId(linkBuilder, instanceReference);
	}

	/**
	 * Adds the instance id parameter to the link.
	 * 
	 * @param linkBuilder
	 *            the link builder
	 * @param instanceReference
	 *            the instance reference
	 * @return link builder
	 */
	private StringBuilder addId(StringBuilder linkBuilder, InstanceReference instanceReference) {
		String identifier = instanceReference.getIdentifier();
		if ((identifier == null) || "null".equals(identifier)) {
			String type = instanceReference.getReferenceType().getJavaClassName();
			log.warn("BookmarkUtil: provided instance [" + type + "] has no identifier!");
			return brokenLink(linkBuilder);
		}

		linkBuilder.append("&instanceId=").append(identifier);
		return addCurentPage(linkBuilder);
	}

	/**
	 * Adds the current page parameter. <br>
	 * <b>Note that this method may not add the parameter if this class is used when there is no
	 * user session.</b>
	 * 
	 * @param linkBuilder
	 *            the link builder
	 * @return linkBuilder
	 */
	private StringBuilder addCurentPage(StringBuilder linkBuilder) {
		String servletPath = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			servletPath = FacesContext.getCurrentInstance().getExternalContext()
					.getRequestServletPath();
			String currentPage = servletPath.substring(servletPath.lastIndexOf("/") + 1,
					servletPath.lastIndexOf("."));
			linkBuilder.append("&currentPage=").append(currentPage);
		}
		return linkBuilder;
	}

	/**
	 * Broken link.
	 * 
	 * @param linkBuilder
	 *            the link builder
	 * @return linkBuilder
	 */
	private StringBuilder brokenLink(StringBuilder linkBuilder) {
		linkBuilder.setLength(0);
		return linkBuilder.append(BROKEN_LINK);
	}

	/**
	 * Adds the target blank to all anchor tags inside the header, and adds marker css class to tell
	 * the block UI to not be triggered by the link click.
	 * 
	 * @param header
	 *            the header
	 * @return updated header
	 */
	public String addTargetBlank(String header) {
		if (StringUtils.isNotNullOrEmpty(header) && !header.contains(" target=")) {
			Matcher matcher = openHtmlATag.matcher(header);
			String updatedHeader = matcher.replaceAll("<a target='_blank' ");
			updatedHeader = updatedHeader.replaceAll(" class=\"", " class=\"dontBlockUI ");
			return updatedHeader;
		}
		return header;
	}
}
