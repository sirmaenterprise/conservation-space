import {View, Component, Inject} from 'app/app';
import {UrlUtils} from 'common/url-utils';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {HelpService} from 'services/help/help-service';

import './contextual-help.css!css';
import template from './contextual-help.html!text';

/**
 * Reusable component for rendering a contextual help button.
 *
 * The component is configured via the <code>target</code> component property. This property is used to point the help
 * target which on the other hand is bound to an instance which the component will redirect to user to.
 *
 * Upon initializing, the component fetches the instance ID for which the provided target points to. If no instance
 * exists for it - the component will not be rendered.
 *
 * When the help button is clicked, the contextual help will be opened in another tab of the browser.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-contextual-help',
  properties: {
    'target': 'target'
  }
})
@View({template})
@Inject(HelpService, WindowAdapter)
export class ContextualHelp {

  constructor(helpService, windowAdapter) {
    this.helpService = helpService;
    this.windowAdapter = windowAdapter;
    this.initialize();
  }

  initialize() {
    if (!this.target || this.target.length < 1) {
      return;
    }
    this.hasTargetInstance = !!this.helpService.getHelpInstanceId(this.target);
  }

  openContextualHelp() {
    var instanceId = this.helpService.getHelpInstanceId(this.target);
    if (instanceId) {
      var url = UrlUtils.buildIdocUrl(instanceId);
      this.windowAdapter.openInNewTab(url);
    }
  }

}
