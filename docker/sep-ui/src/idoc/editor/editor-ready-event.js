import {Event} from 'app/app';

/**
 * Event fired when idoc editor is loaded.
 */
@Event()
export class EditorReadyEvent {
  constructor(data) {
    this.data = data;
  }

  getData() {
    return this.data;
  }
}
