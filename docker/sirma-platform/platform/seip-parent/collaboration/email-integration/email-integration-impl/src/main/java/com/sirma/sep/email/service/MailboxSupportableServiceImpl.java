package com.sirma.sep.email.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.entity.MailboxSupportable;

/**
 * Implementation of {@link MailboxSupportableService}.
 * 
 * @author svelikov
 */
@Singleton
public class MailboxSupportableServiceImpl implements MailboxSupportableService {

	@Inject
	private DbDao dbDao;

	@Override
	@Transactional
	public boolean isMailboxSupportable(String className) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("className", className));
		List<EmailAddress> result = dbDao.fetchWithNamed(MailboxSupportable.QUERY_BY_CLASSNAME_KEY, args);
		return !result.isEmpty();
	}

}
