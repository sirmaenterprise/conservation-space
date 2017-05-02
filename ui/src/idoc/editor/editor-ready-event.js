import {Event} from 'app/app';

/**
 * Event fired when idoc editor is loaded.
 */
@Event()
export class EditorReadyEvent {
  constructor(id) {
    this.data = {id};
  }

  getData() {
    return this.data;
  }
}
