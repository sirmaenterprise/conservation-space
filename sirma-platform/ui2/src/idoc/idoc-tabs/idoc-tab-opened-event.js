import {Event} from 'app/app';

@Event()
export class IdocTabOpenedEvent {

  constructor(tab) {
    this.tab = tab;
  }

  getData() {
    return this.tab;
  }
}
