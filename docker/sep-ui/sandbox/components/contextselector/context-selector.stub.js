import {Component, View, Inject} from 'app/app';
import 'components/contextselector/context-selector';
import {SELECTION_MODE_BOTH, SELECTION_MODE_IN_CONTEXT, SELECTION_MODE_WITHOUT_CONTEXT} from 'components/contextselector/context-selector';

import contextSelectorStubTemplate from 'context-selector.stub.html!text';

@Component({
  selector: 'seip-context-selector-stub'
})
@View({
  template: contextSelectorStubTemplate
})
@Inject()
export class ContextSelectorStub {

  constructor() {
    this.config = {
      parentId: 'test_id'
    };

    this.config.both = {
      parentId: 'test_id',
      contextSelectorSelectionMode: SELECTION_MODE_BOTH
    };

    this.config.selectionModeInContext = {
      parentId: 'test_id',
      contextSelectorSelectionMode: SELECTION_MODE_IN_CONTEXT
    };

    this.config.selectionModeWithoutContext = {
      parentId: 'test_id',
      contextSelectorSelectionMode: SELECTION_MODE_WITHOUT_CONTEXT
    };
  }

}