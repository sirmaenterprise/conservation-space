import {View, Component, Inject, NgElement} from 'app/app';
import {Collapsible} from 'components/collapsible/collapsible';
import uuid from 'common/uuid';
import template from './region.html!text';

@Component({
  selector: 'seip-region',
  // the wrapper should allow children
  transclude: true,
  properties: {
    'fieldViewModel': 'field-view-model',
    'widgetConfig': 'widget-config',
    'objectId': 'object-id'
  }
})
@View({
  template: template
})
@Inject(NgElement)
export class Region {
  constructor($element) {
    this.collapsibleClass = 'c' + uuid();
    this.collapseState = (this.fieldViewModel && this.fieldViewModel.collapsed) ? '' : 'in';
    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      if (propertyChanged.rendered !== undefined && propertyChanged.rendered) {
        $element.show();
      } else if (propertyChanged.rendered !== undefined) {
        $element.hide();
      }
    });
  }

  isRendered() {
    return this.fieldViewModel.rendered === undefined || this.fieldViewModel.rendered === true;
  }

  ngOnDestroy() {
    this.fieldViewModelSubscription.unsubscribe();
  }
}