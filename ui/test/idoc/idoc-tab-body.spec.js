import {IdocTabBody} from 'idoc/idoc-tab-body';
import {IdocMocks} from './idoc-mocks';

describe('IdocTabBody', function () {

  var idocTabBody = new IdocTabBody();

  var idocPage = IdocMocks.instantiateIdocPage();
  var idocTabBody = new IdocTabBody();
  idocTabBody.tabsConfig = idocPage.tabsConfig;

  idocTabBody.windowAdapter = {
    window: {
      addEventListener: function () {
      },
      removeEventListener: function () {
      }
    }
  };

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

    idocTabBody.$scope = {
      $new: () => {
      }
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
    var eventHandler;
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

  describe('Should cleanup eventbus handlers', function () {
    idocTabBody.subscription = {
      unsubscribe: sinon.spy()
    };

    idocTabBody.resizeHandler = function () {
    };

    var removeEventListenerSpy = sinon.spy();

    idocTabBody.windowAdapter.window.removeEventListener = removeEventListenerSpy;

    idocTabBody.ngOnDestroy();

    expect(idocTabBody.subscription.unsubscribe.calledOnce).to.be.true;
    expect(removeEventListenerSpy.calledOnce).to.be.true;
    expect(removeEventListenerSpy.getCall(0).args).to.eql(['resize', idocTabBody.resizeHandler]);
  });

  describe('Splitter configuration', () => {

    it('calculatePaneSizes() should return proper sizes if navigation and comment columns are shown ', ()=> {
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
  });

  it('isActiveTab should return correct boolean result', () => {
    idocTabBody.tabsConfig.activeTabId = 'tab1';
    expect(idocTabBody.isActiveTab({id: 'tab1'})).to.be.true;
    expect(idocTabBody.isActiveTab({id: 'tab2'})).to.be.false;
  });

  it('isEditMode should return false if tab is locked', () => {
    idocTabBody.tab = {
      locked: false
    };
    idocTabBody.context = {
      isEditMode: () => true
    };
    expect(idocTabBody.isEditMode()).to.be.true;
    idocTabBody.tab = {
      locked: true
    };
    expect(idocTabBody.isEditMode()).to.be.false;
  });
});
