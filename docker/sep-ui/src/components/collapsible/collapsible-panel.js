import {Component, Inject, NgElement} from 'app/app';

/**
 * Attribute component for collapsing bootstrap panels.
 *
 * Example declaration:
 *  <div class="panel panel-default" collapsible-panel>
 *    <div class="panel-heading">
 *      <i class="panel-caret fa fa-caret-down"></i>
 *    </div>
 *    <div class="panel-body">
 *    </div>
 *  </div>
 *
 * @author Mihail Radkov
 */
@Component({
  selector: '[collapsible-panel]'
})
@Inject(NgElement)
export class CollapsiblePanel {

  constructor($element) {
    this.$element = $element;
  }

  ngOnInit() {
    let bodyElement = this.$element.find('.panel-body');
    let caretElement = this.$element.find('.panel-caret');

    this.clickHandler = () => {
      bodyElement.toggle();
      caretElement.toggleClass('fa-caret-right');
      caretElement.toggleClass('fa-caret-down');
    };

    this.headerElement = this.$element.find('.panel-heading');
    this.headerElement.on('click', this.clickHandler);
  }

  ngOnDestroy() {
    this.headerElement.off('click', this.clickHandler);
  }
}