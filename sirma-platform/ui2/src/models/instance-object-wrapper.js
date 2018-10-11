/**
 * Wrapper of {@link InstanceObject}.
 * This object should be used when we want to use a service or component that is implemented to use {@link IdocContext}.
 * The object implements {@link IdocContext}'s methods using {@link InstanceObject} filed passed in its constructor.
 * For now, not all {@link IdocContext}'s methods have been realized, but only those that were needed.
 *
 * Example of usage can be found in {@link AddRelationAction} or {@link AddThumbnailAction}
 *
 */
export class InstanceObjectWrapper {

  constructor(promiseAdapter, instanceObject) {
    this.instanceObject = instanceObject;
    this.promiseAdapter = promiseAdapter;
  }

  getCurrentObject() {
    return this.promiseAdapter.resolve(this.instanceObject);
  }

  getCurrentObjectId() {
    return this.instanceObject.getId();
  }
}