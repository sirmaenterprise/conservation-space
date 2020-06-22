/**
 * Class representing a model operation which is directly associated with the
 * {@link ModelChangeSet} type. Operations provided here are relevant for the
 * current implementation and should be exclusively and directly used when
 * building a change set.
 *
 * @author Svetlozar Iliev
 */
export class ModelOperation {
}

ModelOperation.MODIFY = 'modify';
ModelOperation.RESTORE = 'restore';
ModelOperation.REMOVE = 'remove';