import {Component, View} from 'app/app';
import 'search/components/search';

@Component({
  selector: 'search-stub'
})
@View({
  template: '<div><seip-search ng-if="::searchStub.render"></seip-search></div>'
})
export class SearchStub {

  ngOnInit() {
    this.render = true;
  }
}