import {Inject, Component, View} from 'app/app';
import {SearchResults} from 'search/components/common/search-results';
import {NO_SELECTION, SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';
import { ResultsWithActions } from 'external-search/components/results-with-actions';
import resultsWithActionsTemplateStub from 'resultsWithActionsTemplateStub!text';
import 'font-awesome';

@Component({
  selector: 'results-with-actions-stub'
})
@View({
  template: resultsWithActionsTemplateStub
})
export class  ResultsWithActionsStub {

  constructor() {
    this.results = {
      data: [
        {id: 1, default_header: 'One'},
        {id: 2, default_header: 'Two'},
        {id: 3, default_header: 'Three'}
      ]
    };
    this.resultsWithActions = {
      data: [
        {id: 1, default_header: 'One', type: 'documentInstance'},
        {id: 2, default_header: 'Two', type: 'documentInstance'},
        {id: 3, default_header: 'Three', type: 'documentInstance'}
      ]
    };

    this.noSelectionWithActions = {
      config: {
        selection: NO_SELECTION,
        renderMenu: true
      }
    }

    this.noSelection = {
      config: {
        selection: NO_SELECTION
      }
    }
    this.singleSelection = {
      config: {
        selection: SINGLE_SELECTION
      },
      items: [{id: 2, default_header: 'Two'}]
    }
    this.multipleSelection = {
      config: {
        selection: MULTIPLE_SELECTION
      },
      items: [{id: 1, default_header: 'One'}, {id: 2, default_header: 'Two'}]
    };
  }
}
