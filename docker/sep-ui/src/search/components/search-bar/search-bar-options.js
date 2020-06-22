import {Component, View, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
import {KEY_ENTER} from 'common/keys';
import {SavedSearchesLoader} from 'search/components/saved/saved-searches-loader';
import {SearchService} from 'services/rest/search-service';
import 'instance-header/static-instance-header/static-instance-header';
import 'components/help/contextual-help';

import './search-bar-options.css!css';
import template from './search-bar-options.html!text';

/**
 * Component visualizing saved searches with the option to filter them and quick links like advanced search and help.
 *
 * If at least one of the EAI systems is enabled then the external search option will be rendered too.
 *
 * When a saved search is selected from the list, the component event onSearchSelected is called with the selected
 * saved search to notify any wrapping component.
 * When a search mode is selected, onModeSelected is fired with the selected mode.
 *
 * Providing onSearchSelected & onModeSelected is mandatory.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'search-bar-options',
  properties: {
    'config': 'config'
  },
  events: ['onSearchSelected', 'onModeSelected']
})
@View({template})
@Inject(SearchService, Configuration)
export class SearchBarOptions {

  constructor(searchService, configuration) {
    this.searchService = searchService;
    this.configuration = configuration;
  }

  ngOnInit() {
    this.headerConfig = {
      preventLinkRedirect: true
    };
    this.savedSearchesLoader = new SavedSearchesLoader(this.searchService);
    this.filterSavedSearches();
  }

  onKeyPressed(event) {
    if (event.keyCode === KEY_ENTER) {
      this.filterSavedSearches();
    }
  }

  filterSavedSearches() {
    delete this.savedSearches;
    this.savedSearchesLoader.filterSavedSearches(this.savedSearchFilter).then((filterResponse) => {
      this.savedSearches = filterResponse.values;
    });
  }

  selectSavedSearch(savedSearch) {
    this.onSearchSelected({savedSearch});
  }

  changeMode(mode) {
    this.onModeSelected({mode});
  }

  renderExternalSearch() {
    let damEnabled = this.configuration.get(Configuration.EAI_DAM_ENABLED);
    let cmsEnabled = this.configuration.get(Configuration.EAI_CMS_ENABLED);
    return damEnabled || cmsEnabled;
  }
}
