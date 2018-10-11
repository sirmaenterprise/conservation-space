import {Event} from 'app/app';

/**
 * Fired after addThumbnail operation completes.
 */
@Event()
export class ThumbnailUpdatedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}