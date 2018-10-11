package com.sirmaenterprise.sep.monitor.prometheus;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantManager;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hibernate.HibernateStatisticsCollector;

/**
 * Prometheus metrics reporter for hibernate session statistics. There is a configuration to enable disable the
 * statistics reporting
 *
 * @author BBonev
 */
@ApplicationScoped
public class PrometheusHibernateMetricsRegister {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ConfigurationPropertyDefinition(type = Boolean.class, defaultValue = "false", system = true, subSystem = "metrics", label = "Defines if the Promethus hibernate collector is active or not")
	private static final String ENABLE_PROMETHUS_HIBERNATE_COLLECTOR = "metrics.prometheus.hibernate.enabled";

	@Inject
	private TenantManager tenantManager;
	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	@Configuration(ENABLE_PROMETHUS_HIBERNATE_COLLECTOR)
	private ConfigurationProperty<Boolean> hibernateCollectorActivator;

	@PersistenceContext(unitName = PersistenceUnits.PRIMARY)
	private EntityManager tenantEntityManager;

	@PersistenceContext(unitName = PersistenceUnits.CORE)
	private EntityManager coreEntityManager;

	@PersistenceContext(unitName = "seip-auditlog")
	private EntityManager auditEntityManager;

	private HibernateStatisticsCollector hibernateCollector;

	@Startup
	void initialize() {
		// when disabled we will unregister it
		hibernateCollectorActivator.addConfigurationChangeListener(newValue -> {
			// depending on the new value will register or unregister the reporter
			if (Boolean.TRUE.equals(newValue.get())) {
				CollectorRegistry.defaultRegistry.register(buildHibernateStatisticsCollector());
				LOGGER.info("Enabled Prometheus hibernate metrics reporter");
			} else {
				CollectorRegistry.defaultRegistry.unregister(buildHibernateStatisticsCollector());
				LOGGER.info("Disabled Prometheus hibernate metrics reporter");
			}
		});
		// this will trigger initial registration if enabled
		Boolean isEnabled = hibernateCollectorActivator.get();
		if (Boolean.TRUE.equals(isEnabled)) {
			CollectorRegistry.defaultRegistry.register(buildHibernateStatisticsCollector());
		}
	}

	private HibernateStatisticsCollector buildHibernateStatisticsCollector() {
		if (hibernateCollector == null) {
			HibernateStatisticsCollector collector = new HibernateStatisticsCollector();
			collector.add(getSessionFactory(coreEntityManager), "corePersistence");

			tenantManager.getActiveTenantsInfo(false).forEach(tenantInfo -> {
				String tenantId = tenantInfo.getTenantId();
				ContextualExecutor executor = securityContextManager.executeAsTenant(tenantId);

				SessionFactory mainPersistence = executor.function(this::getSessionFactory, tenantEntityManager);
				SessionFactory auditPersistence = executor.function(this::getSessionFactory, auditEntityManager);

				collector.add(new PerTenantSessionFactoryProxy(mainPersistence, executor),
						tenantId + "_mainPersistence");
				collector.add(new PerTenantSessionFactoryProxy(auditPersistence, executor),
						tenantId + "_auditPersistence");
			});
			hibernateCollector = collector;
		}
		return hibernateCollector;
	}

	private SessionFactory getSessionFactory(EntityManager entityManager) {
		return entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
	}

	/**
	 * Session factory proxy that provides the factory statistics that wraps the current tenant
	 * 
	 * @author BBonev
	 */
	private static class PerTenantSessionFactoryProxy implements SessionFactory {
		private static final long serialVersionUID = 460321306708968536L;
		private final SessionFactory delegate;
		private final transient ContextualExecutor tenantScope;

		PerTenantSessionFactoryProxy(SessionFactory delegate, ContextualExecutor tenantScope) {
			this.delegate = delegate;
			this.tenantScope = tenantScope;
		}

		@Override
		public Reference getReference() throws NamingException {
			return delegate.getReference();
		}

		@Override
		public SessionFactoryOptions getSessionFactoryOptions() {
			return delegate.getSessionFactoryOptions();
		}

		@Override
		public SessionBuilder withOptions() {
			return delegate.withOptions();
		}

		@Override
		public Session openSession()  {
			return delegate.openSession();
		}

		@Override
		public Session getCurrentSession()  {
			return delegate.getCurrentSession();
		}

		@Override
		public StatelessSessionBuilder withStatelessOptions() {
			return delegate.withStatelessOptions();
		}

		@Override
		public StatelessSession openStatelessSession() {
			return delegate.openStatelessSession();
		}

		@Override
		public StatelessSession openStatelessSession(Connection connection) {
			return delegate.openStatelessSession(connection);
		}

		@Override
		public ClassMetadata getClassMetadata(Class entityClass) {
			return delegate.getClassMetadata(entityClass);
		}

		@Override
		public ClassMetadata getClassMetadata(String entityName) {
			return delegate.getClassMetadata(entityName);
		}

		@Override
		public CollectionMetadata getCollectionMetadata(String roleName) {
			return delegate.getCollectionMetadata(roleName);
		}

		@Override
		public Map<String, ClassMetadata> getAllClassMetadata() {
			return delegate.getAllClassMetadata();
		}

		@Override
		public Map getAllCollectionMetadata() {
			return delegate.getAllCollectionMetadata();
		}

		@Override
		public Statistics getStatistics() {
			return tenantScope.supplier(delegate::getStatistics);
		}

		@Override
		public void close()  {
			delegate.close();
		}

		@Override
		public boolean isClosed() {
			return delegate.isClosed();
		}

		@Override
		public Cache getCache() {
			return delegate.getCache();
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evict(Class persistentClass)  {
			delegate.evict(persistentClass);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evict(Class persistentClass, Serializable id)  {
			delegate.evict(persistentClass, id);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evictEntity(String entityName)  {
			delegate.evictEntity(entityName);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evictEntity(String entityName, Serializable id)  {
			delegate.evictEntity(entityName, id);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evictCollection(String roleName)  {
			delegate.evictCollection(roleName);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evictCollection(String roleName, Serializable id)  {
			delegate.evictCollection(roleName, id);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evictQueries(String cacheRegion)  {
			delegate.evictQueries(cacheRegion);
		}

		@Override
		@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
		public void evictQueries()  {
			delegate.evictQueries();
		}

		@Override
		public Set getDefinedFilterNames() {
			return delegate.getDefinedFilterNames();
		}

		@Override
		public FilterDefinition getFilterDefinition(String filterName)  {
			return delegate.getFilterDefinition(filterName);
		}

		@Override
		public boolean containsFetchProfileDefinition(String name) {
			return delegate.containsFetchProfileDefinition(name);
		}

		@Override
		public TypeHelper getTypeHelper() {
			return delegate.getTypeHelper();
		}
	}
}
