import {Component, Inject, NgElement, View} from 'app/app';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {Hint} from './hint';
import './hint.css!';

/**
 * Reusable component for rendering a tooltip.
 *
 * The component is configured via the <code>hint-text</code>, <code>hint-class</code> and <code>hint-visible</code>
 * component properties.
 *
 * It is expected that the component has a child element!
 *
 * This component works in the same way as <code>Hint</code> with the difference that it is
 * added as attribute to other components.
 *
 * When the mouse is over the first child of the component and the component has a configured
 * <code>label-hint</code> attribute, a tooltip pops over, showing the <code>hint-text</code>`s value.
 *
 * @author Radoslav Dimitrov
 */
@Component({
  selector: '[label-hint]',
  properties: {
    'hintText': 'hint-text',
    'hintClass': 'hint-class',
    'hintVisible': 'hint-visible'
  }
})
@View({})
@Inject(TooltipAdapter, NgElement)
export class LabelHint extends Hint {
  constructor(tooltipAdapter, $element) {
    super(tooltipAdapter, $element);
  }
}
