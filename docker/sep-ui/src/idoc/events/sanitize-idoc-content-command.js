import {Event} from 'app/app';

/**
 * An event fired to notify idoc-editor that idoc content is updated but still unsanitized and should be set in editor.
 */
@Event()
export class SanitizeIdocContentCommand {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
