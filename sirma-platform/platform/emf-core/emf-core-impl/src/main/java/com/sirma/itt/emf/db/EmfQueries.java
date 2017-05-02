package com.sirma.itt.emf.db;

/**
 * Predefined JPA queries used in the DbDao(s).
 *
 * @author BBonev
 */
public interface EmfQueries {

	/*
	 * DELETED INSTANCE QUERIES
	 */

	/*
	 * LABEL PROVIDER QUERIES
	 */

	/** The Constant QUERY_LABEL_BY_ID_KEY. */
	String QUERY_LABEL_BY_ID_KEY = "QUERY_LABEL_BY_ID";

	/** The Constant QUERY_LABEL_BY_ID. */
	String QUERY_LABEL_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.identifier=:labelId";

	/** The Constant QUERY_LABELS_BY_ID_KEY. */
	String QUERY_LABELS_BY_ID_KEY = "QUERY_LABELS_BY_ID";

	/** The Constant QUERY_LABELS_BY_ID. */
	String QUERY_LABELS_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.identifier in (:labelId)";

	/*
	 * Link queries
	 */

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS_KEY. */
	String QUERY_LINK_BY_SRC_AND_IDS_KEY = "QUERY_LINK_BY_SRC_AND_IDS";

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS. */
	String QUERY_LINK_BY_SRC_AND_IDS = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.identifier in (:identifier) AND l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_AND_IDS_KEY = "QUERY_LINK_BY_TARGET_AND_IDS";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS. */
	String QUERY_LINK_BY_TARGET_AND_IDS = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.identifier in (:identifier) AND l.to.sourceId = :toId AND l.to.sourceType.id=:toType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY = "QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.identifier in (:identifier) AND l.to.sourceId = :toId AND l.to.sourceType.id=:toType and l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS_KEY. */
	String QUERY_LINK_BY_SRC_KEY = "QUERY_LINK_BY_SRC";

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS. */
	String QUERY_LINK_BY_SRC = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_KEY = "QUERY_LINK_BY_TARGET";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS. */
	String QUERY_LINK_BY_TARGET = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.to.sourceId = :toId AND l.to.sourceType.id=:toType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE_KEY = "QUERY_LINK_BY_TARGET_AND_SOURCE";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.to.sourceId = :toId AND l.to.sourceType.id=:toType and l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE_KEY. */
	String DELETE_ALL_LINKS_FOR_INSTANCE_KEY = "DELETE_ALL_LINKS_FOR_INSTANCE";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE. */
	String DELETE_ALL_LINKS_FOR_INSTANCE = "delete from LinkEntity l where (l.to.sourceType.id=:toType AND l.to.sourceId=:toId) or (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId)";

	/** The Constant DELETE_LINKS_FOR_INSTANCE_AND_TYPE_KEY. */
	String DELETE_LINK_FOR_INSTANCE_AND_TYPE_KEY = "DELETE_LINK_FOR_INSTANCE_AND_TYPE";

	/** The Constant DELETE_LINKS_FOR_INSTANCE_AND_TYPE. */
	String DELETE_LINK_FOR_INSTANCE_AND_TYPE = "delete from LinkEntity l where ((l.to.sourceType.id=:toType AND l.to.sourceId=:toId) or (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId)) AND l.identifier=:linkId";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE_KEY. */
	String DELETE_LINKS_FOR_INSTANCE_KEY = "DELETE_LINKS_FOR_INSTANCE";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE. */
	String DELETE_LINKS_FOR_INSTANCE = "delete from LinkEntity l where (l.to.sourceType.id=:toType AND l.to.sourceId=:toId) AND (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId)";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE_KEY. */
	String DELETE_LINK_FOR_INSTANCE_KEY = "DELETE_LINK_FOR_INSTANCE";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE. */
	String DELETE_LINK_FOR_INSTANCE = "delete from LinkEntity l where (l.to.sourceType.id=:toType AND l.to.sourceId=:toId) AND (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId) AND l.identifier=:linkId";

	/*
	 * SEQUENCE QUERIES
	 */
	/** The Constant QUERY_SEQUENCES_KEY. */
	String QUERY_SEQUENCES_KEY = "QUERY_SEQUENCES";

	/** The Constant QUERY_SEQUENCES. */
	String QUERY_SEQUENCES = "from SequenceEntity";
	String QUERY_SEQUENCE_BY_NAME_KEY = "QUERY_SEQUENCE_BY_NAME";
	String QUERY_SEQUENCE_BY_NAME = "from SequenceEntity where sequenceId=:sequenceId";

	/** The Constant UPDATE_SEQUENCES_ENTRY_KEY. */
	String UPDATE_SEQUENCES_ENTRY_KEY = "UPDATE_SEQUENCES_ENTRY";

	/** The Constant UPDATE_SEQUENCES_ENTRY. */
	String UPDATE_SEQUENCES_ENTRY = "update SequenceEntity set sequence=:sequence where sequenceId=:sequenceId";

}
