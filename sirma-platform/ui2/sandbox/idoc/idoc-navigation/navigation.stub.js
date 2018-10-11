import {Component, View} from 'app/app';
import {IdocNavigation} from 'idoc/idoc-navigation/navigation';
import idocNavigation from 'idoc-navigation-stub-template!text';

@Component({
  selector: 'idoc-navigation-stub'
})
@View({
  template: idocNavigation
}) class NavigationStub {

  constructor() {
    this.editMode = true;
    this.sourceSelector = '#mockEditor';
    this.channel = 'channel';
    this.tab = {};
    this.tocHolderSelector = '#toc-holder';

    this.navigationActive = true;
  }

  toggleNavigation() {
    this.navigationActive = !this.navigationActive;
  }
}