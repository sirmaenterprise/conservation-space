/**
 * A single entry in the breadcrumb. An entry might be idoc instance object or navigation state.
 *
 * @param entryId The entry's id
 * @param header The instance breadcrumb header
 * @param instanceType The instance type
 * @param persisted If the instance is persisted or not
 * @param stateUrl The state as router sees it
 */
export class EntryItem {

  constructor(entryId, header, instanceType, isPersisted, stateUrl) {
    this.entryId = entryId;
    this.header = header;
    this.instanceType = instanceType;
    this.persisted = isPersisted;
    this.stateUrl = stateUrl;
  }

  isPersisted() {
    return this.persisted;
  }

  getId() {
    return this.entryId;
  }

  setIndex(index) {
    this.index = index;
  }

  getIndex() {
    return this.index;
  }

  getStateUrl() {
    return this.stateUrl;
  }
}