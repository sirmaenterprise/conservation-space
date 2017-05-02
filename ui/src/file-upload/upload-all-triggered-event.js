import {Event} from 'app/app';

/**
 * Published when the uploadAll button of the FileUploadPanel is clicked.
 */
@Event()
export class UploadAllTriggeredEvent {

  constructor() {
    this.args = arguments;
  }

}