import {Event} from 'app/app';

@Event()
export class ReloadCommentsEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
