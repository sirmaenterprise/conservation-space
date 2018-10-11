import {ConfigureIdocTabs} from 'idoc/idoc-tabs/configure-idoc-tabs';
import {PromiseStub} from 'promise-stub';

describe('Tests for ConfigureIdocTabs component', () => {
  it('Should have a successful execution', () => {
      let context = {
        id: 'test',
        mode: 'edit',
        activeTabId: 'test_id',
        tabsCounter: 2,
        tabs: [
          {
            id: 'test_id',
            title: 'test_id',
            showNavigation: true,
            showComments: false,
            revision: 'exportable'
          }, {
            id: 2,
            title: 'new_tab',
            showNavigation: true,
            showComments: false,
            revision: 'cloneable'
          }]
      };
      let tabsService = {
        openConfigureTabDialog: sinon.spy()
      };

      let configureIdocTabs = new ConfigureIdocTabs(tabsService, PromiseStub);

      configureIdocTabs.execute({}, context);

      expect(tabsService.openConfigureTabDialog.calledOnce).to.be.true;
    }
  );
});
