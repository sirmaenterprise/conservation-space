import {
  NotificationService,
  DEFAULT_POSITION,
  TOP_POSITION,
  ERROR_TIMEOUT
} from 'services/notification/notification-service';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {Eventbus} from 'services/eventbus/eventbus';
import toastr from 'CodeSeven/toastr/toastr';

const titles = {
  [NotificationService.TOAST_LABELS.SUCCESS]: 'Done',
  [NotificationService.TOAST_LABELS.INFO]: 'Info',
  [NotificationService.TOAST_LABELS.WARNING]: 'Warning',
  [NotificationService.TOAST_LABELS.ERROR]: 'Error'
};

describe('NotificationService', function () {
  let toastSpy = sinon.spy(toastr, 'info');
  let eventBus = IdocMocks.mockEventBus();
  let translateAdapter = {
    instant: (key) => titles[key]
  };
  let service;
  beforeEach(function () {
    toastr.success = sinon.spy();
    toastr.error = sinon.spy();
    service = new NotificationService(eventBus, translateAdapter);
  });

  function checkDefaultConfig() {
    expect(toastr.options.progressBar).to.be.true;
    expect(toastr.options.newestOnTop).to.be.false;
    expect(toastr.options.preventDuplicates).to.be.true;
    expect(toastr.options.closeButton).to.be.true;
    expect(toastr.options.hideOnHover).to.be.true;
    expect(toastr.options.timeOut).to.equal(5000);
    expect(toastr.options.extendedTimeOut).to.equal(2000);
    expect(toastr.options.positionClass).to.equal(DEFAULT_POSITION);
  }

  describe('#constructor', () => {
    let translateSpy = sinon.spy(translateAdapter, 'instant');

    beforeEach(() => {
      translateSpy.reset();
    });

    it('should resolve toast titles from translate service and subscribe to language change event', () => {
      new NotificationService(eventBus, translateAdapter);
      expect(translateSpy.callCount).to.equal(4);
    });

    it('should reinitialize toast titles on a language change event', () => {
      let eventBus = new Eventbus();
      new NotificationService(eventBus, translateAdapter);
      eventBus.publish(new LanguageChangeSuccessEvent());
      expect(translateSpy.callCount).to.equal(8);
    });
  });

  describe('on init', () => {
    it('should set the default configurations', () => {
      checkDefaultConfig();
    });

    it('default configuration is not allowed to be changed', () => {
      let predefinedOptions = {
        opts: {
          progressBar: false,
          newestOnTop: true,
          preventDuplicates: false,
          closeButton: false,
          hideOnHover: false,
          timeOut: 15000,
          extendedTimeOut: 2000,
          positionClass: 'toast-top-left'
        }
      };
      service.success(predefinedOptions);
      checkDefaultConfig();
      service.info(predefinedOptions);
      checkDefaultConfig();
      service.warning(predefinedOptions);
      checkDefaultConfig();
      service.error(predefinedOptions);
      checkDefaultConfig();
      service.notify('error', predefinedOptions);
      checkDefaultConfig();
    });
  });

  describe('getTitle()', function () {
    it('should return correct default notification title', function () {
      let titles = {
        success: 'Done',
        info: 'Info',
        warning: 'Warning',
        error: 'Error'
      };
      let getTitleMethodSpy = sinon.spy(service, 'getTitle');
      ['success', 'info', 'warning', 'error'].forEach(function (val, ind) {
        let opts = {};
        service.getTitle(val, opts);
        expect(getTitleMethodSpy.returnValues[ind]).to.equal(titles[val]);
      });
    });

    it('should return correct custom notification title', function () {
      let getTitleMethodSpy = sinon.spy(service, 'getTitle');
      ['success', 'info', 'warning', 'error'].forEach(function (val, ind) {
        let opts = {title: 'Custom title'};
        service.getTitle(val, opts);
        expect(getTitleMethodSpy.returnValues[ind]).to.equal('Custom title');
      });
    });
  });

  describe('getMessage()', function () {
    it('should return the opts argument as notification message when it is a string object', function () {
      let getMessageMethodSpy = sinon.spy(service, 'getMessage');
      let opts = 'Option is a message';
      service.getMessage(opts);
      expect(getMessageMethodSpy.returnValues[0]).to.equal('Option is a message');
    });

    it('should return provided notification message', function () {
      let getMessageMethodSpy = sinon.spy(service, 'getMessage');
      let opts = {message: 'A message'};
      service.getMessage(opts);
      expect(getMessageMethodSpy.returnValues[0]).to.equal('A message');
    });

    it('should return correct empty notification message', function () {
      let getMessageMethodSpy = sinon.spy(service, 'getMessage');
      let opts = {};
      service.getMessage(opts);
      expect(getMessageMethodSpy.returnValues[0]).to.equal('');
    });
  });

  it('style change on big message', function () {
    //The message must be atleas 200 charecters long.
    let opts = 'Lorem ipsum dolor sit amet, ullum aperiam vulputate has cu, duis mandamus eam ea. Elit vocent quaerendum duo ne, nostro commodo probatus vel eu. Ut sed ferri inciderint.\
      Ei legendos erroribus consectetuer eos, at mea efficiendi intellegam. Id esse voluptatum duo, nostrud vulputate sed ex.\
      Stet eruditi ea sit, no cum dolore noster. Te posse erant his. Elaboraret efficiendi in nam. No vitae necessitatibus eos,\
      veri labitur mel at, usu diceret tacimates elaboraret ei. Elaboraret efficiendi in nam. No vitae necessitatibus.';
    service.info(opts);
    expect(toastSpy.getCall(0).args).to.eql([opts, 'Info', {positionClass: TOP_POSITION}]);
    toastSpy.reset();
  });

  // TODO refactor these tests
  it('error() should call the notify() method', function () {
    sinon.stub(service, 'notify', function () {
    });
    service.error({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('warning() should call the notify() method', function () {
    sinon.stub(service, 'notify', function () {
    });
    service.warning({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('success() should call the notify() method', function () {
    sinon.stub(service, 'notify', function () {
    });
    service.success({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('info() should call the notify() method', function () {
    sinon.stub(service, 'notify', function () {
    });
    service.info({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('clear() should call the toastr.clear() method', function () {
    let clearMethodSpy = sinon.spy(toastr, 'clear');
    service.clear();
    expect(clearMethodSpy.callCount).to.equal(1);
  });

  it('remove() should call the toastr.remove() method', function () {
    let removeMethodSpy = sinon.spy(toastr, 'remove');
    service.remove();
    expect(removeMethodSpy.callCount).to.equal(1);
  });

  it('should display a simple message passed', function () {
    service.notify('success', 'Big success');
    let methodCall = toastr.success.getCall(0);
    expect(methodCall.calledWithExactly('Big success', 'Done', {})).to.be.true;
  });

  it('error() should display only popup with the default Error title', function () {
    service.notify('error', {});
    let methodCall = toastr.error.getCall(0);
    expect(methodCall.calledWithExactly('', 'Error', {})).to.be.true;
  });

  it('notify("error", {opts: {}}) should call toastr.error("", "Error",  {})', function () {
    let optsArg = {opts: {}};
    service.notify('error', optsArg);
    let methodCall = toastr.error.getCall(0);
    let expectedOpts = {};
    expect(methodCall.calledWithExactly('', 'Error', expectedOpts)).to.be.true;
    // when closeButton=true then timeOut=5000
    expect(toastr.options.timeOut).to.equal(5000);
  });

  it('notify("error", {opts: { closeButton: true }}) should call toastr.error("", "Error",  { closeButton: true })', function () {
    let optsArg = {opts: {closeButton: true}};
    service.notify('error', optsArg);
    let methodCall = toastr.error.getCall(0);
    let expectedOpts = {
      closeButton: true,
      timeOut: 0
    };
    expect(methodCall.calledWithExactly('', 'Error', expectedOpts)).to.be.true;
  });

  it('should allow disabling of the automatic hide on hover', function () {
    let optsArg = {opts: {hideOnHover: false}};
    service.notify('error', optsArg);
    let methodCall = toastr.error.getCall(0);
    let expectedOpts = {
      hideOnHover: false,
      extendedTimeOut: 0,
    };

    expect(methodCall.calledWithExactly('', 'Error', expectedOpts)).to.be.true;
  });

  it('notify with a message longer than 200 characters with a set positionClass should not change its position', () => {
    let opts = {
      opts: {positionClass: 'test.position'}, message: 'Lorem ipsum dolor sit amet, ullum aperiam vulputate has cu, duis mandamus eam ea. Elit vocent quaerendum duo ne, nostro commodo probatus vel eu. Ut sed ferri inciderint.\
      Ei legendos erroribus consectetuer eos, at mea efficiendi intellegam. Id esse voluptatum duo, nostrud vulputate sed ex.\
      Stet eruditi ea sit, no cum dolore noster. Te posse erant his. Elaboraret efficiendi in nam. No vitae necessitatibus eos,\
      veri labitur mel at, usu diceret tacimates elaboraret ei. Elaboraret efficiendi in nam. No vitae necessitatibus.',
    };

    service.info(opts);
    expect(toastSpy.getCall(0).args).to.eql([opts.message, 'Info', {positionClass: 'test.position'}]);
    toastSpy.reset();
  });

  it('error() should call notify with timeout set for error notifications if not explicitly set', () => {
    let opts = {
      message: 'Error message'
    };
    service.error(opts);
    expect(toastr.error.getCall(0).args[2].timeOut).to.equals(ERROR_TIMEOUT);
    toastr.error.reset();
    opts.opts = {
      timeOut: 1000
    };
    service.error(opts);
    expect(toastr.error.getCall(0).args[2].timeOut).to.equals(1000);
  });
});
