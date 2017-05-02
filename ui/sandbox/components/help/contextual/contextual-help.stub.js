import {Component, View, Inject} from 'app/app';
import {ContextualHelp} from 'components/help/contextual-help';
import template from 'contextual-help-template!text';

/**
 * Stubbed component for testing wrapped contextual help components.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-contextual-help-stub'
})
@View({
  template: template
})
@Inject()
export class ContextualHelpStub {
  // Wrapper stub.
}