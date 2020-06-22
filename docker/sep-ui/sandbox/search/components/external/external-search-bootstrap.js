import {Inject, Component, View} from 'app/app';
import {Configuration} from 'common/application-config';
import 'search/components/search';
import {EXTERNAL_MODE} from 'search/utils/search-criteria-utils';

import template from './external-search-bootstrap.html!text';

@Component({
  selector: 'external-search-bootstrap'
})
@View({
  template: template
})
@Inject(Configuration)
export class ExternalSearchBootstrap {

  constructor(configuration) {
    this.configuration = configuration;
  }

  ngOnInit() {
    this.configuration.configs[Configuration.EAI_DAM_ENABLED] = true;
    this.searchConfig = {
      searchMode: EXTERNAL_MODE
    };
    this.render = true;
  }
}