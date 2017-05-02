import {Event} from 'app/app';

/**
 * An event fired when image is loaded
 */
@Event()
export class ImageReadyEvent {
  constructor(element) {
    this.data = {element};
  }

  getData() {
    return this.data;
  }
}
