import {IdocTabBody} from 'idoc/idoc-tab-body';
import {IdocMocks} from './idoc-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {stub} from 'test/test-utils';
import {Eventbus} from 'services/eventbus/eventbus';
import {NavigationEnabledEvent} from 'idoc/idoc-navigation/navigation-enabled-event';

describe('IdocTabBody', function () {
  let idocTabBody;

  before(() => {
    IdocTabBody.prototype.context = {
      isEditMode: () => true
    };
    IdocTabBody.prototype.tab = {
      id: 'activeTabId',
      system: true
    };

    idocTabBody = new IdocTabBody(mock$scope());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocTabBody.tabsConfig = idocPage.tabsConfig;

    idocTabBody.windowAdapter = {
      window: {
        addEventListener: function () {
        },
        removeEventListener: function () {
        }
      }
    };
  });

  it('should load the system tab content', ()=> {
    let tabContentElement = {
      append: sinon.spy()
    };

    idocTabBody.tab = {
      id: 'activeTabId',
      system: true
    };

    idocTabBody.tabsConfig.activeTabId = 'activeTabId';

    idocTabBody.$element = {
      find: ()=> {
        return tabContentElement;
      },
      width: () => 1000
    };
    idocTabBody.$compile = ()=> {
      return ()=> {
        return [{}];
      };
    };

    idocTabBody.ngAfterViewInit();
    expect(tabContentElement.append.called).to.be.true;
  });

  it('Should remove the event subscription when the is opened (not to be unsubscribed again in the on destroy method)', ()=> {
    const TAB_ID = 'activeTabId';

    idocTabBody.tab = {
      id: TAB_ID,
      system: true
    };

    idocTabBody.insertContent = sinon.stub();

    idocTabBody.tabsConfig.activeTabId = 'otherTab';
    let eventHandler;
    idocTabBody.eventbus = {
      subscribe: function (event, f) {
        eventHandler = f;
        return {
          unsubscribe: sinon.spy()
        }
      }
    };

    idocTabBody.ngAfterViewInit();

    expect(idocTabBody.subscription).to.be.defined;

    // emulate tab opening
    idocTabBody.tabsConfig.activeTabId = TAB_ID;
    eventHandler({
      id: TAB_ID
    });

    expect(idocTabBody.subscription).to.not.be.defined;
  });

  it('Should cleanup eventbus handlers', () => {
    idocTabBody.subscription = {
      unsubscribe: sinon.spy()
    };

    idocTabBody.resizeHandler = function () {
    };

    let removeEventListenerSpy = sinon.spy();

    idocTabBody.windowAdapter.window.removeEventListener = removeEventListenerSpy;

    idocTabBody.ngOnDestroy();

    expect(idocTabBody.subscription.unsubscribe.calledOnce).to.be.true;
    expect(removeEventListenerSpy.calledOnce).to.be.true;
    expect(removeEventListenerSpy.getCall(0).args).to.eql(['resize', idocTabBody.resizeHandler]);
  });

  describe('Splitter configuration', () => {

    it('calculatePaneSizes() should return proper sizes if navigation and comment columns are shown ', ()=> {
      idocTabBody.eventbus = stub(Eventbus);
      expect(idocTabBody.calculatePaneSizes(idocTabBody.tabsConfig.tabs[0]).join()).to.equal('20,55,25');
    });

    it('calculatePaneSizes() should return proper sizes if only navigation column is shown', ()=> {
      expect(idocTabBody.calculatePaneSizes(idocTabBody.tabsConfig.tabs[1]).join()).to.equal('20,80');
    });

    it('calculatePaneSizes() should return proper sizes if no additional columns are shown ', ()=> {
      expect(idocTabBody.calculatePaneSizes(idocTabBody.tabsConfig.tabs[2]).join()).to.equal('100');
    });

    it('calculatePaneSizes() should return proper sizes if comment column is shown ', ()=> {
      expect(idocTabBody.calculatePaneSizes(idocTabBody.tabsConfig.tabs[3]).join()).to.equal('75,25');
    });

    it('should publish NavigationEnabledEvent if navigation column is shown ', ()=> {
      idocTabBody.calculatePaneSizes(idocTabBody.tabsConfig.tabs[0]);
      expect(idocTabBody.eventbus.publish.args[0][0] instanceof NavigationEnabledEvent).to.be.true;
      expect(idocTabBody.eventbus.publish.args[0][0].data).to.eql({id: 'id_0'});
    });
  });

  it('isActiveTab should return correct boolean result', () => {
    idocTabBody.tabsConfig.activeTabId = 'tab1';
    expect(idocTabBody.isActiveTab({id: 'tab1'})).to.be.true;
    expect(idocTabBody.isActiveTab({id: 'tab2'})).to.be.false;
  });

  it('getIsEditMode should return false if tab is locked', () => {
    let tab = {
      locked: false
    };
    let context = {
      isEditMode: () => true
    };
    expect(IdocTabBody.getIsEditMode(context, tab)).to.be.true;
    tab.locked = true;
    expect(IdocTabBody.getIsEditMode(context, tab)).to.be.false;
  });
});
