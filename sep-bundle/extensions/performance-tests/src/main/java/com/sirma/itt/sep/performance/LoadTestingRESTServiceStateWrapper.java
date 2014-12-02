package com.sirma.itt.sep.performance;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Class LoadTestingRESTServiceStateWrapper.
 */
public class LoadTestingRESTServiceStateWrapper {

	/** The word statistic. */
	private Map<String, Integer> wordStatistic = new HashMap<String, Integer>();

	/** The instances statistic. */
	private Map<Class<? extends Instance>, Integer> instancesStatistic = new HashMap<Class<? extends Instance>, Integer>();

	/** The user statistics. */
	private Map<String, Map<String, Integer>> userStatistics = new HashMap<String, Map<String, Integer>>();

	/** The files uploded. */
	private int filesUploded = 0;

	/** The users. */
	private List<User> users;

	/** The authority roles. */
	private Map<Resource, RoleIdentifier> authorityRoles;

	/** The english dictionary. */
	private Properties englishDictionary;

	/** The testable files. */
	private File[] testableFiles;

	/**
	 * Instantiates a new load testing rest service state wrapper.
	 *
	 * @param users
	 *            the users
	 * @param authorityRoles
	 *            the authority roles
	 * @param testableFiles
	 *            the testable files
	 */
	public LoadTestingRESTServiceStateWrapper(List<User> users,
			Map<Resource, RoleIdentifier> authorityRoles, File[] testableFiles) {
		this.users = users;
		this.authorityRoles = authorityRoles;
		this.testableFiles = testableFiles;
		init();
	}

	/**
	 * Inits the.
	 */
	public void init() {

		instancesStatistic.put(CaseInstance.class, new Integer(0));
		instancesStatistic.put(ProjectInstance.class, new Integer(0));
		instancesStatistic.put(DocumentInstance.class, new Integer(0));
		instancesStatistic.put(ObjectInstance.class, new Integer(0));
		instancesStatistic.put(CommentInstance.class, new Integer(0));
		instancesStatistic.put(TopicInstance.class, new Integer(0));
		instancesStatistic.put(TaskInstance.class, new Integer(0));
		instancesStatistic.put(StandaloneTaskInstance.class, new Integer(0));
		instancesStatistic.put(WorkflowInstanceContext.class, new Integer(0));

	}

	/**
	 * Update instances statistic.
	 *
	 * @param type
	 *            the type
	 * @param updateCount
	 *            the update count
	 */
	public void updateInstancesStatistic(Class<? extends Instance> type, int updateCount) {
		synchronized (instancesStatistic) {
			Map<Class<? extends Instance>, Integer> map = instancesStatistic;
			map.put(type, map.get(type).intValue() + updateCount);
		}
	}

	/**
	 * Update user operations statistic.
	 *
	 * @param operation
	 *            the operation
	 * @param user
	 *            the user
	 */
	public void updateUserOperationsStatistic(String operation, User user) {
		if (operation != null) {
			synchronized (userStatistics) {
				Map<String, Map<String, Integer>> map = userStatistics;
				String userId = user.getIdentifier();
				if (!map.containsKey(userId)) {
					map.put(userId, new HashMap<String, Integer>(10));
				}
				if (!map.get(userId).containsKey(operation)) {
					map.get(userId).put(operation, new Integer(0));
				}
				map.get(userId).put(operation, map.get(userId).get(operation).intValue() + 1);
			}
		}
	}

	/**
	 * Update word statistic.
	 *
	 * @param wordSelected
	 *            the word selected
	 * @param updateCount
	 *            the update count
	 */
	public void updateWordStatistic(String wordSelected, int updateCount) {
		synchronized (wordStatistic) {
			Map<String, Integer> map = wordStatistic;
			if (!map.containsKey(wordSelected)) {
				map.put(wordSelected, new Integer(0));
			}
			// update count
			map.put(wordSelected, map.get(wordSelected).intValue() + updateCount);
		}
	}

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	public JSONObject getStatistics() {
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, "WORDS STATISTIC", wordStatistic.toString());
		JsonUtil.addToJson(json, "USERS STATISTIC", userStatistics.toString());
		JsonUtil.addToJson(json, "INSTANCES STATISTIC", instancesStatistic.toString());
		return json;
	}

	/**
	 * Gets the english dictionary.
	 *
	 * @return the english dictionary
	 */
	public Properties getEnglishDictionary() {
		if (englishDictionary == null) {
			try {
				englishDictionary = new Properties();
				InputStream resourceAsStream = LoadTestingRESTService.class
						.getResourceAsStream("english.properties");
				englishDictionary.load(resourceAsStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return englishDictionary;
	}

	/**
	 * Clear.
	 *
	 * @param sessionId
	 *            the session id
	 */
	public void clear(String sessionId) {
		synchronized (wordStatistic) {
			wordStatistic.clear();
		}
		synchronized (userStatistics) {
			userStatistics.clear();
		}
		synchronized (instancesStatistic) {
			Map<Class<? extends Instance>, Integer> instances = instancesStatistic;
			instances.put(CaseInstance.class, new Integer(0));
			instances.put(ProjectInstance.class, new Integer(0));
			instances.put(DocumentInstance.class, new Integer(0));
			instances.put(ObjectInstance.class, new Integer(0));
			instances.put(CommentInstance.class, new Integer(0));
			instances.put(TaskInstance.class, new Integer(0));
			instances.put(WorkflowInstanceContext.class, new Integer(0));
		}
	}

	/**
	 * Generate random string.
	 *
	 * @param cnt
	 *            the cnt
	 * @return the string
	 */
	public String generateRandomString(int cnt) {
		List<Integer> usedIndexes = new ArrayList<Integer>(cnt);
		StringBuffer resultString = new StringBuffer();
		for (int index = 0; index < cnt;) {
			Properties englishDictionary = getEnglishDictionary();
			Integer position = new Integer((int) (Math.random() * (englishDictionary.size())));
			if (!usedIndexes.contains(position)) {
				String wordSelected = (String) englishDictionary.get(new Integer(position)
						.toString());
				resultString.append(wordSelected);
				updateWordStatistic(wordSelected, 1);
				resultString.append(" ");
				index++;
				usedIndexes.add(position);
			}
		}
		usedIndexes = null;
		return resultString.toString();
	}

	/**
	 * Gets the testable files.
	 *
	 * @return the testable files
	 */
	public File[] getTestableFiles() {
		return testableFiles;
	}

	/**
	 * Gets the authority roles.
	 *
	 * @return the authority roles
	 */
	public Map<Resource, RoleIdentifier> getAuthorityRoles() {
		return authorityRoles;
	}

	/**
	 * Gets the users.
	 *
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
	}


	/**
	 * Gets the next testable file.
	 *
	 * @return the next testable file
	 */
	public File getNextTestableFile() {
		if (testableFiles.length > filesUploded + 1) {
			return testableFiles[filesUploded++];
		}
		return null;
	}

}
