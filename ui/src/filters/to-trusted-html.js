import {Inject, Filter} from 'app/app';
import {ContextualEscapeAdapter} from 'adapters/angular/contextual-escape-adapter';
import _ from 'lodash';

@Filter
@Inject(ContextualEscapeAdapter)
export class ToTrustedHtml {
  constructor(contextualEscapeAdapter) {
    this.contextualEscapeAdapter = contextualEscapeAdapter;
  }

  filter(text) {
    // $sce breaks for non-string values. I.e integers.
    if (!_.isString(text) && !_.isUndefined(text)) {
      text = '' + text;
    }
    return this.contextualEscapeAdapter.trustAsHtml(text);
  }
}
