import {Event} from 'app/app';

/**
 * Base class for saved search events.
 */
export class SavedSearchEvent {
  constructor(savedSearch) {
    this.savedSearch = savedSearch;
  }

  getData() {
    return this.savedSearch;
  }
}

/**
 * Fired when a saved search is loaded and assigned.
 */
@Event()
export class SavedSearchLoadedEvent extends SavedSearchEvent {
  constructor(savedSearch) {
    super(savedSearch);
  }
}

/**
 * Fired when a saved search is created.
 */
@Event()
export class SavedSearchCreatedEvent extends SavedSearchEvent {
  constructor(savedSearch) {
    super(savedSearch);
  }
}

/**
 * Fired when a saved search is updated.
 */
@Event()
export class SavedSearchUpdatedEvent extends SavedSearchEvent {
  constructor(savedSearch) {
    super(savedSearch);
  }
}