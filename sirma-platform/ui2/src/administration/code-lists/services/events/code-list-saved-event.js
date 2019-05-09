import {Event} from 'app/app';

/**
 * An event fired after a code list from the code management tool has been saved
 *
 * @author Svetlozar Iliev
 */
@Event()
export class CodeListSavedEvent {

  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}