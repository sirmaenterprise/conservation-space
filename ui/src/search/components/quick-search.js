import { Component, View, Inject } from 'app/app';
import { Configurable } from 'components/configurable';
import { Router } from 'adapters/router/router';
import { Keys } from 'common/keys';
import './quick-search.css!css';
import 'font-awesome/css/font-awesome.css!';
import template from './quick-search.html!text';

@Component({
  selector: 'seip-quick-search',
  properties: {'config': 'config'}
})
@View({template: template})
@Inject(Router)
export class QuickSearch extends Configurable {

  constructor(router) {
    const defaultOnSearchCallback = (params) => this.router.navigate('search', params, {reload: true});

    super({onSearch: defaultOnSearchCallback});
    this.router = router;
  }

  search() {
    this.config.onSearch({metaText: this.metaText});
    this.metaText = null;
  }

  onKeypress(event) {
    if (Keys.isEnter(event.keyCode)) {
      this.search();
    }
  }
}