import {Event} from 'app/app';

/**
 * Event fired when the idoc is loaded and all its widgets are rendered.
 *
 *
 * Constructed with a json of two values:
 * <b>editorId</b> which is the editor id used within the platform
 * <b>editorName</b> which is the editor instance name initialized within ckeditor.
 */
@Event()
export class IdocReadyEvent {
  constructor(data) {
    this.data = data;
  }

}
