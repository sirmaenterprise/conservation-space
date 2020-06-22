import {PrintService, PRINT_IN_PROGRESS_NOTIFICATION} from 'services/print/print-service';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('PrintService', () => {

  const NOTIFICATION_FOR_PRINT = 'notification message';

  describe('print', () => {
    it('should execute preparePrint', () => {
      let url = '/#/idoc/emf:123456?mode=print';
      let service = getPrintServiceInstance();
      sinon.spy(service, 'preparePrint');
      service.print(url);
      expect(service.preparePrint.calledOnce).to.be.true;
      expect(service.preparePrint.calledWith(url)).to.be.true;
    });

    it('should notify for print in progress', () => {
      let url = '/#/idoc/emf:123456?mode=print';
      let service = getPrintServiceInstance();
      sinon.spy(service, 'sendNotification');
      service.queue.push(url);
      service.print(url);
      expect(service.sendNotification.calledOnce).to.be.true;
      expect(service.sendNotification.calledWith(PRINT_IN_PROGRESS_NOTIFICATION)).to.be.true;
    });
  });

  describe('getIframe', () => {
    it('should build iframe html', () => {
      expect(PrintService.getIframe({
        id: '',
        url: '/#/idoc/emf:123456?mode=print',
        frameWidth: 1000,
        frameHeight: 500
      })).to.equal('<iframe id="printPage" name="printPage" src="/#/idoc/emf:123456?mode=print" style="position: absolute; top: -1000px; width: 1000px; height: 500px; @media print { display: block; }"></iframe>');
    });
  });

  describe('preparePrint', () => {
    it('should rise notification for initiated print action', () => {
      let service = getPrintServiceInstance();
      service.preparePrint('');
      expect(service.notificationService.info.calledOnce).to.be.true;
      expect(service.notificationService.info.calledWith(NOTIFICATION_FOR_PRINT)).to.be.true;
    });

    it('should bind onmessage event handler that listens for event when loaded in iframe idoc page is ready for print', () => {
      let service = getPrintServiceInstance();
      service.preparePrint('');
      expect(service.windowAdapter.window.addEventListener.calledOnce).to.be.true;
      // check the event name only - the first argument
      expect(service.windowAdapter.window.addEventListener.calledWith('message')).to.be.true;
    });

    it('should append iframe to the body', () => {
      let jquerySpy = sinon.spy($.fn, 'append');
      let service = getPrintServiceInstance();
      service.preparePrint('');
      expect(jquerySpy.calledOnce).to.be.true;
      jquerySpy.restore();
    });

    it('should clean the print on timeout ', () => {
      let url = '/#/idoc/emf:123456?mode=print';
      let service = getPrintServiceInstance();
      sinon.spy(service, 'cleanPrint');
      service.preparePrint(url);
      expect(service.cleanPrint.calledOnce).to.be.true;
    });
  });

  describe('executePrint', () => {
    it('should focus the iframe and call print', () => {
      let service = getPrintServiceInstance();
      service.executePrint();
      expect(service.windowAdapter.window.frames.printPage.focus.calledOnce).to.be.true;
      expect(service.windowAdapter.window.frames.printPage.print.calledOnce).to.be.true;
    });
  });

  describe('cleanup', () => {
    it('should remove the iframe from the DOM', () => {
      let printPageIframe = [{}];
      printPageIframe.remove = sinon.spy();
      let service = getPrintServiceInstance();
      service.cleanup(printPageIframe);
      expect(printPageIframe.remove.calledOnce).to.be.true;
    });

    it('should not remove anything if no frame is found', () => {
      let printPageIframe = [];
      printPageIframe.remove = sinon.spy();
      let service = getPrintServiceInstance();
      service.cleanup(printPageIframe);
      expect(printPageIframe.remove.callCount).to.equal(0);
    });
  });

  describe('cleanPrint', () => {
    it('should invoke next print', () => {
      let url = '/#/idoc/emf:123456?mode=print';
      let service = getPrintServiceInstance();
      sinon.spy(service, 'preparePrint');
      service.queue.push(url);
      service.queue.push(url);
      service.cleanPrint();
      expect(service.preparePrint.called).to.be.true;
    });
  });

  describe('onIdocReady', () => {
    let event = {
      message: 'idocReady'
    };

    it('should unsubscribe from onmessage event when event payload is in \'message\' field', () => {
      let service = getPrintServiceInstance();
      service.onIdocReady(event);
      checkUnsubscription(service);
    });

    it('should unsubscribe from onmessage event when event payload is in \'data\' field', () => {
      let service = getPrintServiceInstance();
      service.onIdocReady({
        data: 'idocReady'
      });
      checkUnsubscription(service);
    });

    it('should not handle the event if the message is not \'idocReady\'', () => {
      let service = getPrintServiceInstance();
      sinon.spy(service, 'executePrint');
      sinon.spy(service, 'cleanup');
      service.onIdocReady({
        data: 'someMessage'
      });
      expect(service.windowAdapter.window.removeEventListener.calledOnce).to.be.false;
      expect(service.executePrint.calledOnce).to.be.false;
      expect(service.cleanup.calledOnce).to.be.false;
    });

    function checkUnsubscription(service) {
      expect(service.windowAdapter.window.removeEventListener.calledOnce).to.be.true;
      // check the event name only - the first argument
      expect(service.windowAdapter.window.removeEventListener.calledWith('message')).to.be.true;
    }

    it('should execute print and cleanup', () => {
      let service = getPrintServiceInstance();
      sinon.spy(service, 'executePrint');
      sinon.spy(service, 'cleanup');
      service.onIdocReady(event);
      expect(service.executePrint.calledOnce).to.be.true;
      expect(service.cleanup.calledOnce).to.be.true;
    });
  });

  function getPrintServiceInstance() {
    let windowAdapter = {
      window: {
        close: () => {
        },
        addEventListener: sinon.spy(),
        removeEventListener: sinon.spy(),
        frames: {
          printPage: {
            focus: sinon.spy(),
            print: sinon.spy()
          }
        }
      }
    };
    let notificationService = {
      info: sinon.spy(),
      error: sinon.spy()
    };
    let eventbus = {};
    let translateService = {
      translateInstant: () => {
        return NOTIFICATION_FOR_PRINT
      }
    };
    let $timeout = (callback) => {
      callback();
    };
    $timeout.cancel = (()=> {
    });
    return new PrintService(windowAdapter, notificationService, eventbus, translateService, IdocMocks.mockConfiguration(), $timeout);
  }
});