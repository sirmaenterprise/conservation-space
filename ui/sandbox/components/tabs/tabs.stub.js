import {Component, View} from 'app/app';
import tabsStubTemplate from 'tabs.stub.html!text';
import 'components/tabs/tabs';

@Component({
  selector: 'seip-tabs-stub'
})
@View({
  template: tabsStubTemplate
})
class TabsStub {
  constructor() {
    this.horizontal = false;
    this.vertical = false;

    this.tabsConfigHorizontal = {
      tabs: [{
        id: 'tab1',
        label: 'Tab1',
        target: '.tab1-target',
        classes: 'custom-class'
      }, {
        id: 'tab2',
        label: 'Tab2',
        target: '.tab2-target',
        postfix: () => {
          return '<span>Custom</span>';
        }
      }],
      activeTab: 'tab2'
    };

    this.tabsConfigVertical = {
      classes: 'vertical',
      tabs: [{
        id: 'tab1',
        label: 'Tab1',
        target: '.tab1-vertical'
      }, {
        id: 'tab2',
        label: 'Tab2',
        target: '.tab2-vertical'
      }]
    };
  }

  activateHorizontal() {
    this.horizontal = true;
  }

  activateVertical() {
    this.vertical = true;
  }
}