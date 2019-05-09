package com.sirma.sep.keycloak;

import java.util.Collections;
import java.util.List;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

/**
 * A dummy entity provider which loads changelog xml file with liquibase db patches.
 *
 * @author smustafov
 */
public class SepJpaEntityProvider implements JpaEntityProvider {

	public static final String CHANGELOG_FILE = "META-INF/sep-keycloak-changelog.xml";

	@Override
	public List<Class<?>> getEntities() {
		return Collections.emptyList();
	}

	@Override
	public String getChangelogLocation() {
		return CHANGELOG_FILE;
	}

	@Override
	public String getFactoryId() {
		return SepJpaEntityProviderFactory.ID;
	}

	@Override
	public void close() {
		// not needed
	}

}
