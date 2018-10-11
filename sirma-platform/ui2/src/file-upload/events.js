import {Event} from 'app/app';

/**
 * Fired when file was successfully uploaded.
 */
@Event()
export class UploadCompletedEvent {
  constructor(entity) {
    this.entity = entity;
  }

  getData() {
    return this.entity;
  }
}