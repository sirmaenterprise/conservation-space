import {Component, View} from 'app/app';
import {HeaderContainer} from 'header-container/header-container';
import headerContainerTpl from 'header-container-stub-template!text';
import 'instance-header/static-instance-header/static-instance-header';
import result from 'sandbox/header-container/header-data.json!';
@Component({
  selector: 'header-container-stub'
})
@View({
  template: headerContainerTpl
})
export class HeaderContainerStub {
  constructor() {
    this.headerResult = result;
  }
}