import {Event} from 'app/app';

/**
 * Event fired when idoc editor is resized (by splitter dragging, toggle sections, window resize)
 */
@Event()
export class EditorResizedEvent {
  constructor(data) {
    this.data = data;
  }

  /**
   * Returns event's data
   * @returns JSON object containing new width and height values and
   * widthChanged and heightChanged boolean flags indicating whether given properties are changed since last resize
   */
  getData() {
    return this.data;
  }
}