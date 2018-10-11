import {Component, View} from 'app/app';
import 'components/tabs/tabs';
import tabsStubTemplate from 'tabs.stub.html!text';

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
    this.sorted = false;

    this.tabsConfigHorizontal = {
      tabs: [{
        id: 'tab1',
        label: 'Tab1',
        classes: 'custom-class'
      }, {
        id: 'tab2',
        label: 'Tab2',
        postfix: () => {
          return '<span>Custom</span>';
        }
      }],
      activeTab: 'tab2'
    };

    this.tabsConfigVertical = {
      classes: 'nav-stacked nav-left',
      tabs: [{
        id: 'tab1',
        label: 'Tab1'
      }, {
        id: 'tab2',
        label: 'Tab2'
      }]
    };

    this.tabsConfigSorted = {
      classes: 'vertical',
      sortComparator: function (lhs, rhs) {
        return lhs.label.localeCompare(rhs.label);
      },
      tabs: [{
        id: 'tab1',
        label: 'Curie',
        target: '.tab1-vertical'
      }, {
        id: 'tab2',
        label: 'Oppenheimer',
        target: '.tab2-vertical'
      }, {
        id: 'tab3',
        label: 'Feynman',
        target: '.tab2-vertical'
      }, {
        id: 'tab4',
        label: 'Einstein',
        target: '.tab2-vertical'
      }, {
        id: 'tab5',
        label: 'Wheeler',
        target: '.tab2-vertical'
      }, {
        id: 'tab6',
        label: 'Newton',
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

  activateSorted() {
    this.sorted = true;
  }
}