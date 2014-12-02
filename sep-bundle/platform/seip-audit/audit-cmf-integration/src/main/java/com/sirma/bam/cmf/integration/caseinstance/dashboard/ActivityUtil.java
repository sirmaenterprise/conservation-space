package com.sirma.bam.cmf.integration.caseinstance.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.bam.cmf.integration.userdashboard.filter.ActivitiesFilterConstants;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.util.DateRangeUtil;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * This class will help for retrieving activity data.
 * 
 * @author cdimitrov
 */
@ApplicationScoped
public class ActivityUtil {

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/**
	 * Construct recent activity data based on BAM data. TODO: Re-implement after data corrected.
	 * 
	 * @param activities
	 *            data receive from BAM server
	 * @return recent activity list
	 */
	public List<Activities> constructActivities(List<AuditActivity> activities) {
		if (activities.isEmpty()) {
			return new ArrayList<Activities>();
		}
		Instance objectInstance = null;
		Instance parent = null;
		Instance root = null;
		List<Activities> activityList = new ArrayList<Activities>();
		for (AuditActivity activity : activities) {
			Activities activityRecord = new Activities();
			String objectType = activity.getObjectInstanceType();
			if (objectType != null) {
				objectInstance = getInstanceByType(activity.getObjectSystemID(), objectType);
				String actionStr = activity.getAction();

				// work-around for skipping records with missing data
				if ((objectInstance != null) && actionStr != null) {

					root = getTopInstance(objectInstance, true);

					parent = getTopInstance(objectInstance, false);

					if ((objectInstance instanceof ObjectInstance) == false) {
						if (parent != null) {
							if (parent.equals(root)) {
								parent = null;
							} else {
								// generate parent data for the activity
								activityRecord.createParentActivity((String) parent.getProperties()
										.get(DefaultProperties.TITLE), parent.getClass()
										.getSimpleName());
							}
						}
						if (root != null) {
							// generate root data for the activity
							activityRecord.createRootActivity(
									(String) root.getProperties().get(DefaultProperties.TITLE),
									root.getClass().getSimpleName());
						}
					}

					activityRecord.setName((String) objectInstance.getProperties().get(
							DefaultProperties.TITLE));
					activityRecord.setUrl(activity.getObjectURL());
					activityRecord.setIconPath(objectInstance.getClass().getSimpleName());
					activityRecord.setAction(actionStr);
					activityRecord.setUser(getUserResource(activity.getUserName()));
					activityRecord.setTimesince(timeSince(activity.getEventDate()));
					activityList.add(activityRecord);
				}
			}
		}
		return activityList;
	}

	/**
	 * This method will help us for retrieving parent or root instance based on current. If parent
	 * is {@link SectionInstance} or {@link WorkflowInstanceContext}, this method will invoke and
	 * search for next parent element.
	 * 
	 * @param instance
	 *            object instance
	 * @param root
	 *            based on this parameter will search for parent or root
	 * @return parent or root element
	 */
	private Instance getTopInstance(Instance instance, boolean root) {
		if (instance != null) {
			if (root) {
				return InstanceUtil.getRootInstance(instance, true);
			} else {
				Instance current = InstanceUtil.getDirectParent(instance, true);
				// proceed searching if element is section or workflow instance.
				if ((current instanceof SectionInstance)
						|| (current instanceof WorkflowInstanceContext)) {
					return getTopInstance(current, root);
				} else {
					return current;
				}
			}
		}
		return null;
	}

	/**
	 * Extract instance with properties based on instance id and instance type.
	 * 
	 * @param instanceId
	 *            current instance id
	 * @param instanceType
	 *            current instance type
	 * @return extracted instance
	 */
	private Instance getInstanceByType(String instanceId, String instanceType) {
		Instance instance = null;
		if (StringUtils.isNotNullOrEmpty(instanceId) && StringUtils.isNotNullOrEmpty(instanceType)
				&& !"null".equals(instanceId)) {

			DataTypeDefinition dataTypeDefinition = dictionaryService
					.getDataTypeDefinition(instanceType);

			InstanceReference instanceReference = new LinkSourceId(instanceId, dataTypeDefinition);

			instance = typeConverter.convert(InitializedInstance.class, instanceReference)
					.getInstance();

		}
		return instance;
	}

	/**
	 * Get user as resource based on user name.
	 * 
	 * @param username
	 *            user name
	 * @return user data
	 */
	private Resource getUserResource(String username) {
		return resourceService.getResource(username, ResourceType.USER);
	}

	/**
	 * Retrieve date range by filter name.
	 * 
	 * @param filterName
	 *            current filter name
	 * @return date range
	 */
	public DateRange getDateRange(String filterName) {
		switch (filterName) {
			case ActivitiesFilterConstants.SORT_BY_TODAY:
				return DateRangeUtil.getToday();
			case ActivitiesFilterConstants.SORT_BY_LAST7DAYS:
				return DateRangeUtil.getLast7Days();
			case ActivitiesFilterConstants.SORT_BY_LAST14DAYS:
				return DateRangeUtil.getNDaysRange(14, 0, false);
			case ActivitiesFilterConstants.SORT_BY_LAST28DAYS:
				return DateRangeUtil.getNDaysRange(30, 0, false);
			default:
				break;
		}
		DateRange dateRange = DateRangeUtil.getAll();

		return dateRange;
	}

	/**
	 * Calculate time since current date. TODO: This method need to be re-factor. Suffix text need
	 * to be generated by the label builder.
	 * 
	 * @param dateTime
	 *            event date
	 * @return event date
	 */
	private String timeSince(Date dateTime) {

		long seconds = (long) (System.currentTimeMillis() - dateTime.getTime()) / 1000;
		long interval = (long) seconds / 31536000;

		if (interval >= 1) {
			if (interval == 1) {
				return interval + " year ago";
			}
			return interval + " years ago";
		}

		interval = (long) Math.floor(seconds / 2592000);
		if (interval >= 1) {
			if (interval == 1) {
				return interval + " month ago";
			}
			return interval + " months ago";
		}

		interval = (long) Math.floor(seconds / 86400);
		if (interval >= 1) {
			if (interval == 1) {
				return interval + " day ago";
			}
			return interval + " days ago";
		}

		interval = (long) Math.floor(seconds / 3600);
		if (interval >= 1) {
			if (interval == 1) {
				return interval + " hour ago";
			}
			return interval + " hours ago";
		}

		interval = (long) Math.floor(seconds / 60);
		if (interval >= 1) {
			if (interval == 1) {
				return interval + " minute ago";
			}
			return interval + " minutes ago";
		}
		return "few seconds ago";
	}
}
