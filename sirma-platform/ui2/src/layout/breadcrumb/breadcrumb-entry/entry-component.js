import {View, Component, Inject, NgElement} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {EntrySelectedFromBreadcrumbEvent} from 'layout/breadcrumb/breadcrumb-entry/entry-selected-event';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';

import 'instance-header/static-instance-header/static-instance-header';
import 'filters/to-trusted-html';

@Component({
  selector: 'seip-breadcrumb-entry',
  properties: {
    entry: 'entry'
  }
})
@View({
  template: '<span><seip-static-instance-header header-type="entryComponent.headerType" header="entryComponent.entry.header" is-disabled="!entryComponent.entry.isPersisted"></seip-static-instance-header></span>'
})
@Inject(NgElement, Eventbus)
export class EntryComponent {

  constructor(element, eventbus) {
    this.element = element;
    this.eventbus = eventbus;
    this.headerType = HEADER_BREADCRUMB;
  }

  ngOnInit() {
    this.element.on('click', (evt) => {
      if (this.entry.isPersisted()) {
        let link = this.element.find('.instance-link');
        link.onclick = this.eventbus.publish(new EntrySelectedFromBreadcrumbEvent(this.entry.getIndex()));
      } else {
        evt.preventDefault();
      }
    });
  }
}