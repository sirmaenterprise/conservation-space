import {Component, View, Inject, NgScope} from 'app/app';
import template from './libraries-bootstrap-template.html!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'libraries-bootstrap'
})
@View({
  template: template
})
@Inject(NgScope)
class LibrariesBootstrap {
  
  constructor($scope) {
    this.$scope = $scope;
  }
}