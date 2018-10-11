import {Injectable} from 'app/app';
import _ from 'lodash';

/**
 * Holds all current breadcrumb entries.
 */
@Injectable()
export class BreadcrumbEntryManager {
  constructor() {
    this.entries = [];
  }

  /**
   * Adds entry to the model of all entries only if its id is different from the last one added.
   * @param entry
   * @returns {*}
   */
  add(entry) {
    let lastEntry = this.getLastEntry();
    if (lastEntry && !lastEntry.isPersisted()) {
      // When the object is not persisted a temporary entry is created
      // and should be removed as soon as new entry is about to be added.
      this.clear(lastEntry.getIndex() - 1);
    }
    if (lastEntry && lastEntry.getId() === entry.getId()) {
      // refresh the last entry
      this.setLastEntry(entry);
      return false;
    }
    entry.setIndex(this.entries.length + 1);
    this.entries.push(entry);
    this.compactEntries();
    return true;
  }

  getEntries() {
    return this.entries;
  }

  /**
   * Clears the models from fromIndex until the end.
   * @param fromIndex The index from which it will remove models
   */
  clear(fromIndex = 0) {
    this.entries.splice(fromIndex);
  }

  /**
   * Goes trough the breadcrumb entries and removes all that have objectId equal to provided.
   *
   * @param objectId The objectId of the entries that should be removed from the array.
   */
  removeEntry(objectId) {
    _.remove(this.entries, (entry) => {
      return entry.getId() === objectId;
    });
    this.compactEntries();
  }

  /**
   * Removes equal successive entries from the array.
   */
  compactEntries() {
    _.remove(this.entries, (entry, index) => {
      return index > 0 && this.entries[index - 1].getId() === entry.getId();
    });
  }

  back() {
    let lastEntry = this.getLastEntry();
    if (lastEntry) {
      this.clear(lastEntry.getIndex() - 1);
    }
  }

  setLastEntry(entry) {
    entry.setIndex(this.entries.length);
    this.entries[this.entries.length - 1] = entry;
  }

  getLastEntry() {
    return this.entries[this.entries.length - 1];
  }

  getPreviousEntry() {
    return this.entries[this.entries.length - 2];
  }
}