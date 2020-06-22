import {View, Component, Inject, NgElement} from 'app/app';
import 'components/collapsible/collapsible';
import uuid from 'common/uuid';
import {FormControl} from 'form-builder/form-control';
import template from './region.html!text';

@Component({
  selector: 'seip-region',
  // the wrapper should allow children
  transclude: true,
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({template})
@Inject(NgElement)
export class Region extends FormControl {
  constructor($element) {
    super();
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
}
