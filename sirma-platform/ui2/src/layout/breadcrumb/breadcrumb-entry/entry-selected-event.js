import {Event} from 'app/app';

@Event()
export class EntrySelectedFromBreadcrumbEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}