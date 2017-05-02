import {Event} from 'app/app';

/**
 * Fired when a change has been made in idoc which requires a force update of the undo manager.
 * If a certain change for some reason remains "unnoticed" by the undo manager, using this event,
 * it could be force-stored in the undo stack and will be available next time when using "Undo" command.
 */
@Event()
export class IdocUpdateUndoManagerEvent {

  constructor(tab) {
    this.tab = tab;
  }

  getData() {
    return this.tab;
  }
}
