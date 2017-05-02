import {NotificationService, DEFAULT_POSITION, TOP_POSITION, ERROR_TIMEOUT} from 'services/notification/notification-service';
import toastr from 'CodeSeven/toastr/toastr';

describe('NotificationService', function () {
  var toastSpy = sinon.spy(toastr, 'info');

  beforeEach(function () {
    toastr.success = sinon.spy();
    toastr.error = sinon.spy();
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

  describe('on init', () => {
    it('should set the default configurations', () => {
      var service = new NotificationService();
      checkDefaultConfig();
    });

    it('default configuration is not allowed to be changed', () => {
      var predefinedOptions = {
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
      var service = new NotificationService();
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
      var titles = {
        success: 'Done',
        info: 'Info',
        warning: 'Warning',
        error: 'Error'
      };
      var service = new NotificationService();
      var getTitleMethodSpy = sinon.spy(service, 'getTitle');
      ['success', 'info', 'warning', 'error'].forEach(function (val, ind) {
        var opts = {};
        service.getTitle(val, opts);
        expect(getTitleMethodSpy.returnValues[ind]).to.equal(titles[val]);
      });
    });

    it('should return correct custom notification title', function () {
      var service = new NotificationService();
      var getTitleMethodSpy = sinon.spy(service, 'getTitle');
      ['success', 'info', 'warning', 'error'].forEach(function (val, ind) {
        var opts = {title: 'Custom title'};
        service.getTitle(val, opts);
        expect(getTitleMethodSpy.returnValues[ind]).to.equal('Custom title');
      });
    });
  });

  describe('getMessage()', function () {
    it('should return the opts argument as notification message when it is a string object', function () {
      var service = new NotificationService();
      var getMessageMethodSpy = sinon.spy(service, 'getMessage');
      var opts = 'Option is a message';
      service.getMessage(opts);
      expect(getMessageMethodSpy.returnValues[0]).to.equal('Option is a message');
    });

    it('should return provided notification message', function () {
      var service = new NotificationService();
      var getMessageMethodSpy = sinon.spy(service, 'getMessage');
      var opts = {message: 'A message'};
      service.getMessage(opts);
      expect(getMessageMethodSpy.returnValues[0]).to.equal('A message');
    });

    it('should return correct empty notification message', function () {
      var service = new NotificationService();
      var getMessageMethodSpy = sinon.spy(service, 'getMessage');
      var opts = {};
      service.getMessage(opts);
      expect(getMessageMethodSpy.returnValues[0]).to.equal('');
    });
  });

  it('style change on big message', function () {
    var service = new NotificationService();
    //The message must be atleas 200 charecters long.
    var opts = 'Lorem ipsum dolor sit amet, ullum aperiam vulputate has cu, duis mandamus eam ea. Elit vocent quaerendum duo ne, nostro commodo probatus vel eu. Ut sed ferri inciderint.\
      Ei legendos erroribus consectetuer eos, at mea efficiendi intellegam. Id esse voluptatum duo, nostrud vulputate sed ex.\
      Stet eruditi ea sit, no cum dolore noster. Te posse erant his. Elaboraret efficiendi in nam. No vitae necessitatibus eos,\
      veri labitur mel at, usu diceret tacimates elaboraret ei. Elaboraret efficiendi in nam. No vitae necessitatibus.';
    service.info(opts);
    expect(toastSpy.getCall(0).args).to.eql([opts, 'Info', {positionClass: TOP_POSITION}]);
    toastSpy.reset();
  });

  // TODO refactor these tests
  it('error() should call the notify() method', function () {
    var service = new NotificationService();
    sinon.stub(service, 'notify', function () {
    });
    service.error({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('warning() should call the notify() method', function () {
    var service = new NotificationService();
    sinon.stub(service, 'notify', function () {
    });
    service.warning({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('success() should call the notify() method', function () {
    var service = new NotificationService();
    sinon.stub(service, 'notify', function () {
    });
    service.success({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('info() should call the notify() method', function () {
    var service = new NotificationService();
    sinon.stub(service, 'notify', function () {
    });
    service.info({});
    expect(service.notify.callCount).to.equal(1);
  });

  it('clear() should call the toastr.clear() method', function () {
    var service = new NotificationService();
    var clearMethodSpy = sinon.spy(toastr, 'clear');
    service.clear();
    expect(clearMethodSpy.callCount).to.equal(1);
  });

  it('remove() should call the toastr.remove() method', function () {
    var service = new NotificationService();
    var removeMethodSpy = sinon.spy(toastr, 'remove');
    service.remove();
    expect(removeMethodSpy.callCount).to.equal(1);
  });

  it('should display a simple message passed', function () {
    var service = new NotificationService();
    service.notify('success', 'Big success');
    var methodCall = toastr.success.getCall(0);
    expect(methodCall.calledWithExactly('Big success', 'Done', {})).to.be.true;
  });

  it('error() should display only popup with the default Error title', function () {
    var service = new NotificationService();
    service.notify('error', {});
    var methodCall = toastr.error.getCall(0);
    expect(methodCall.calledWithExactly('', 'Error', {})).to.be.true;
  });

  it('notify("error", {opts: {}}) should call toastr.error("", "Error",  {})', function () {
    var service = new NotificationService();
    var optsArg = {opts: {}};
    service.notify('error', optsArg);
    var methodCall = toastr.error.getCall(0);
    var expectedOpts = {};
    expect(methodCall.calledWithExactly('', 'Error', expectedOpts)).to.be.true;
    // when closeButton=true then timeOut=5000
    expect(toastr.options.timeOut).to.equal(5000);
  });

  it('notify("error", {opts: { closeButton: true }}) should call toastr.error("", "Error",  { closeButton: true })', function () {
    var service = new NotificationService();
    var optsArg = {opts: {closeButton: true}};
    service.notify('error', optsArg);
    var methodCall = toastr.error.getCall(0);
    var expectedOpts = {
      closeButton: true,
      timeOut: 0
    };
    expect(methodCall.calledWithExactly('', 'Error', expectedOpts)).to.be.true;
  });

  it('should allow disabling of the automatic hide on hover', function () {
    var service = new NotificationService();
    var optsArg = {opts: {hideOnHover: false}};
    service.notify('error', optsArg);
    var methodCall = toastr.error.getCall(0);
    var expectedOpts = {
      hideOnHover: false,
      extendedTimeOut: 0,
    };

    expect(methodCall.calledWithExactly('', 'Error', expectedOpts)).to.be.true;
  });

  it('notify with a message longer than 200 characters with a set positionClass should not change its position', ()=> {

    var service = new NotificationService();
    var opts = {
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
    var service = new NotificationService();
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
