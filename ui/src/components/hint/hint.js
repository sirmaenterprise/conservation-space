import {Component, View, Inject, NgElement} from 'app/app';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import './hint.css!';
import tooltipTemplate from './hint.html!text';

@Component({
  selector: 'seip-hint',
  properties: {
    'hintText': 'hint-text',
    'hintClass': 'hint-class',
    'hintVisible': 'hint-visible'
  }
})
@View({
  template: tooltipTemplate
})
@Inject(TooltipAdapter, NgElement)
export class Hint {
  constructor(tooltipAdapter, $element) {
    if (!this.hintClass) {
      this.hintClass = Hint.defaultClass;
    }
    this.tooltipAdapter = tooltipAdapter;
    this.$element = $element;

    this.tooltipsRegistry = new Set();

    this.$element.on({
      'mouseover': () => {
        this.tooltipsRegistry.add(tooltipAdapter.tooltip($element[0].firstElementChild,
          {
            title: $element.tooltipText,
            template: '<div class="tooltip seip-hint-tooltip" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
          },
          true
        ));
      },
      'mouseleave': () => {
        this.tooltipsRegistry.delete(tooltipAdapter.hide($element[0].firstElementChild));
      }
    });
  }

  ngOnDestroy() {
    this.tooltipAdapter.clearTooltips([...this.tooltipsRegistry]);
    this.tooltipsRegistry.clear();
    this.$element.off();
  }
}

Hint.defaultClass = 'form-tooltip';