import {Component, View, Inject} from 'app/app';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {ExtensionsPanel} from 'components/extensions-panel/extensions-panel';
import {ContextSelector} from 'components/contextselector/context-selector';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import _ from 'lodash';

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
  }

}