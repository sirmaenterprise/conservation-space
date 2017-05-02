import {DeleteIdocTabs} from 'idoc/idoc-tabs/delete-idoc-tabs';

describe('Tests for DeleteIdocTabs component', () => {
  it('should delete tab when deleteTab() is called', () => {

    let context = {
      mode: 'edit',
      activeTabId: 2,
      tabsCounter: 3,
      tabs: [
        {id: 1, title: 'Tab1', showNavigation: true, showComments: false},
        {id: 2, title: 'Tab2', showNavigation: true, showComments: false},
        {id: 3, title: 'Tab3', showNavigation: true, showComments: false}
      ]
    };
    let deleteIdocTabs = new DeleteIdocTabs({}, undefined);
    deleteIdocTabs.initContext(context);
    expect(deleteIdocTabs.context.tabs.length).to.equal(3);

    let tabToDelete = {id: 1, title: 'Tab1', showNavigation: true, showComments: false};
    deleteIdocTabs.deleteTab(tabToDelete);
    expect(deleteIdocTabs.context.tabs.length).to.equal(2);
  });
});
