import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import template from './empty-cell.html!text';

@Component({
  selector: 'seip-empty-cell',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
/**
 * Used to render empty cells instead of field. Mainly usable in data table widget.
 */
@Inject(NgElement)
export class EmptyCell extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
  }

  ngOnInit() {
    this.setRendered(this.$element, this.fieldViewModel.rendered);
  }

  notifyWhenReady() {
    return true;
  }
}
