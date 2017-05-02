import {ContextualHelp} from 'components/help/contextual-help';
import {PromiseStub} from 'test/promise-stub';

describe('ContextualHelp', () => {

  var helpService;
  var windowAdapter;
  var contextualHelp;
  beforeEach(() => {
    ContextualHelp.prototype.target = 'widget.content';
    initializeComponent('helpInstanceId');
  });

  afterEach(() => {
    ContextualHelp.prototype.target = undefined;
  });

  /**
   * Creates & provides the minimal amount of mocks and data for the instance to be initialized.
   */
  function initializeComponent(helpInstanceId) {
    helpService = mockHelpService(helpInstanceId);
    windowAdapter = mockWindowAdapter();
    contextualHelp = new ContextualHelp(helpService, windowAdapter);
  }

  it('should not fetch the instance id if there is no contextual help target provided', () => {
    ContextualHelp.prototype.target = '';
    initializeComponent();
    expect(helpService.getHelpInstanceId.called).to.be.false;
  });

  it('should know if there is an existing contextual help instance', () => {
    expect(helpService.getHelpInstanceId.calledOnce).to.be.true;
    expect(helpService.getHelpInstanceId.getCall(0).args[0]).to.equal('widget.content');
    expect(contextualHelp.hasTargetInstance).to.be.true
  });

  it('should know if there is not an existing contextual help instance', () => {
    ContextualHelp.prototype.target = 'target';
    initializeComponent();
    expect(contextualHelp.hasTargetInstance).to.be.false;
  });

  it('should open the help instance in another tab', () => {
    contextualHelp.openContextualHelp();

    var openSpy = contextualHelp.windowAdapter.openInNewTab;
    expect(openSpy.calledOnce).to.be.true;
    expect(openSpy.getCall(0).args[0]).to.equal('/#/idoc/helpInstanceId');
  });

  it('should not open the help instance in another tab if it the instance id goes undefined', () => {
    initializeComponent(undefined);
    contextualHelp.openContextualHelp();

    var openSpy = contextualHelp.windowAdapter.openInNewTab;
    expect(openSpy.called).to.be.false;
  });

  function mockHelpService(instanceId) {
    return {
      getHelpInstanceId: sinon.spy(() => {
        return instanceId;
      })
    };
  }

  function mockWindowAdapter() {
    return {
      openInNewTab: sinon.spy()
    };
  }
});