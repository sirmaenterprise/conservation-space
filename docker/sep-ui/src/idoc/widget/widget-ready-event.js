import {Event} from 'app/app';

/**
 * Event fired when a widget has loaded its content and is fully rendered which means its content is available in the
 * DOM tree.
 * data:
 * {
 *   widgetId: widgetId
 * }
 */
@Event()
export class WidgetReadyEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
