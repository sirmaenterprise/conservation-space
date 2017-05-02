import {Event} from 'app/app';

@Event()
export class PermissionsChangedEvent {

  constructor(isEditAllowed) {
    this.isEditAllowed = isEditAllowed;
  }

  getData() {
    return this.isEditAllowed;
  }
}
