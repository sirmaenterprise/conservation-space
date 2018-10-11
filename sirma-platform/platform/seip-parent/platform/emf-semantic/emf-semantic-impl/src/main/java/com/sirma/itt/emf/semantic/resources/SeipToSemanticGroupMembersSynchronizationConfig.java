package com.sirma.itt.emf.semantic.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesReadConverter;
import com.sirma.itt.emf.semantic.persistence.ValueProxy;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationDataProvider;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Synchronization configuration that transfers the group members in the semantic database from relational database.
 *
 * @author BBonev
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 12)
public class SeipToSemanticGroupMembersSynchronizationConfig
		implements SynchronizationConfiguration<IRI, SeipToSemanticGroupMembersSynchronizationConfig.GroupInfo> {

	private static final String IRI = "uri";
	private static final String MEMBER = "member";

	public static final String NAME = "seipToSemanticGroupMembers";

	/**
	 * Query that fetches current groups and their members.
	 *
	 * <pre>
	 * <code>select ?uri ?member where {
	 *    ?uri a emf:Group.
	 *    optional {
	 *    	?uri ptop:hasMember ?member.
	 *    }
	 * }</code>
	 * </pre>
	 */
	public static final String QUERY_GROUP_MEMBERS = "select ?" + IRI + " ?" + MEMBER + " where { ?" + IRI + " a <"
			+ EMF.GROUP + ">. optional { ?" + IRI + " " + Proton.PREFIX + ":" + Proton.HAS_MEMBER.getLocalName() + " ?"
			+ MEMBER + ".}. }";

	@Inject
	private ResourceService resourceService;
	@Inject
	@SemanticDb
	private DbDao dbDao;
	@Inject
	private RepositoryConnection repositoryConnection;
	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private HashCalculator hashCalculator;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SemanticPropertiesReadConverter propertiesConverter;
	@Inject
	private ValueFactory valueFactory;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SynchronizationDataProvider<IRI, GroupInfo> getSource() {
		return SynchronizationDataProvider.create(loadLocalResources(), GroupInfo::getSystemId);
	}

	@SuppressWarnings("squid:S1452")
	private SynchronizationProvider<Collection<GroupInfo>> loadLocalResources() {
		ResourceService resources = resourceService;
		NamespaceRegistryService namespaceRegistryService = registryService;
		return () -> {
			List<Resource> groups = resources.getAllResources(ResourceType.GROUP, null);
			List<GroupInfo> groupInfos = new ArrayList<>(groups.size());
			for (Resource resource : groups) {
				GroupInfo info = new GroupInfo();
				info.systemGroupId = namespaceRegistryService.buildUri(resource.getId().toString());

				Set<IRI> members = resources
							.getContainedResources(resource.getId())
								.stream()
								.map(id -> namespaceRegistryService.buildUri(id.getId().toString()))
								.collect(Collectors.toSet());

				info.members.addAll(members);
				groupInfos.add(info);
			}

			return groupInfos;
		};
	}

	@Override
	public SynchronizationDataProvider<IRI, GroupInfo> getDestination() {
		return SynchronizationDataProvider.create(loadResourcesFromSemantic(), GroupInfo::getSystemId);
	}

	@SuppressWarnings("squid:S1452")
	private SynchronizationProvider<Collection<GroupInfo>> loadResourcesFromSemantic() {
		RepositoryConnection localConnection = repositoryConnection;
		SemanticPropertiesReadConverter converter = propertiesConverter;
		return () -> {
			try {
				TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(localConnection, QUERY_GROUP_MEMBERS,
						Collections.emptyMap(), false, -1);
				TupleQueryResult queryResult = tupleQuery.evaluate();
				try (TupleQueryResultIterator queryResultIterator = new TupleQueryResultIterator(queryResult)) {

					return converter
							.buildQueryResultModel(queryResultIterator, IRI)
								.entrySet()
								.stream()
								.map(entry -> GroupInfo.from(entry.getKey(),
										entry.getValue().getOrDefault(MEMBER, Collections.emptySet())))
								.filter(Objects::nonNull)
								.collect(Collectors.toList());
				}
			} catch (RDF4JException e) {
				throw new SynchronizationException("Could not read groups members from semantic database", e);
			}
		};
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public GroupInfo merge(GroupInfo currentInfo, GroupInfo newValue) {
		GroupInfo merged = new GroupInfo();
		merged.systemGroupId = currentInfo.systemGroupId;
		merged.currentMembers.addAll(currentInfo.members);
		merged.newMembers.addAll(newValue.members);
		return merged;
	}

	@Override
	public BiPredicate<GroupInfo, GroupInfo> getComparator() {
		return hashCalculator::equalsByHash;
	}

	@Override
	public void save(SynchronizationResult<IRI, GroupInfo> result, SyncRuntimeConfiguration runtimeConfiguration) {
		transactionSupport.invokeBiConsumerInNewTx(this::saveChangesInTx, result,
				Boolean.valueOf(runtimeConfiguration.isForceSynchronizationEnabled()));
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private void saveChangesInTx(SynchronizationResult<IRI, GroupInfo> result, boolean force) {
		Model addModel = new LinkedHashModel();
		Model removeModel = new LinkedHashModel();

		for (GroupInfo info : result.getModified().values()) {
			Set<IRI> newMembers = new HashSet<>(info.getNewMembers());
			Set<IRI> currentMembers = new HashSet<>(info.getCurrentMembers());

			Set<IRI> common = new HashSet<>();
			Set<IRI> tmp = new HashSet<>(info.getNewMembers());
			tmp.retainAll(info.getCurrentMembers());
			common.addAll(tmp);
			tmp = new HashSet<>(info.getCurrentMembers());
			tmp.retainAll(info.getNewMembers());
			common.addAll(tmp);

			// If force sync is enabled leave them all as modified, otherwise remove the common
			// ones.
			if (!force) {
				newMembers.removeAll(common);
			}
			currentMembers.removeAll(common);

			addToModel(addModel, info.getSystemId(), newMembers);
			addToModel(removeModel, info.getSystemId(), currentMembers);
		}

		for (GroupInfo info : result.getToAdd().values()) {
			addToModel(addModel, info.getSystemId(), info.getCurrentMembers());
		}

		for (GroupInfo info : result.getToRemove().values()) {
			addToModel(removeModel, info.getSystemId(), info.getCurrentMembers());
		}

		if (!addModel.isEmpty() || !removeModel.isEmpty()) {
			SemanticPersistenceHelper.removeModel(repositoryConnection, removeModel);
			SemanticPersistenceHelper.saveModel(repositoryConnection, addModel, registryService.getDataGraph());
		}
	}

	private void addToModel(Model model, IRI systemId, Set<IRI> members) {
		for (IRI uri : members) {
			Statement addHasMemberStatement = SemanticPersistenceHelper.createStatement(systemId, Proton.HAS_MEMBER, uri,
					registryService, valueFactory);
			Statement addIsMemberOfStatement = SemanticPersistenceHelper.createStatement(uri, EMF.IS_MEMBER_OF, systemId,
					registryService, valueFactory);
			model.add(addHasMemberStatement);
			model.add(addIsMemberOfStatement);
		}
	}

	/**
	 * Represents a group information and it's members.
	 *
	 * @author BBonev
	 */
	public static class GroupInfo {
		IRI systemGroupId;
		Set<IRI> members = new HashSet<>();

		Set<IRI> currentMembers = new HashSet<>();
		Set<IRI> newMembers = new HashSet<>();

		/**
		 * Instantiates a new group info.
		 */
		public GroupInfo() {
			// implement me
		}

		/**
		 * Creates {@link GroupInfo} from the given data. If the id is not {@link IRI} <code>null</code> will be
		 * returned
		 *
		 * @param id
		 *            the id
		 * @param values
		 *            the values
		 * @return the group info
		 */
		public static GroupInfo from(Value id, Collection<? extends Value> values) {
			if (id instanceof IRI) {
				GroupInfo info = new GroupInfo();
				info.systemGroupId = ValueProxy.getValue(id);
				info.addAll(values);
				return info;
			}
			return null;
		}

		public Set<IRI> getCurrentMembers() {
			Set<IRI> result = CollectionUtils.createLinkedHashSet(currentMembers.size());
			for (IRI uri : currentMembers) {
				result.add(ValueProxy.getValue(uri));
			}
			return result;
		}

		public Set<IRI> getNewMembers() {
			Set<IRI> result = CollectionUtils.createLinkedHashSet(newMembers.size());
			for (IRI uri : newMembers) {
				result.add(ValueProxy.getValue(uri));
			}
			return result;
		}

		/**
		 * Gets the group name.
		 *
		 * @return the group name
		 */
		IRI getSystemId() {
			return systemGroupId;
		}

		/**
		 * Adds the all.
		 *
		 * @param membersToAdd
		 *            the members to add
		 */
		void addAll(Collection<? extends Value> membersToAdd) {
			for (Value member : membersToAdd) {
				if (member instanceof IRI) {
					members.add((IRI) member);
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (members == null ? 0 : members.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof GroupInfo)) {
				return false;
			}
			GroupInfo other = (GroupInfo) obj;
			return members.equals(other.members);
		}
	}
}
