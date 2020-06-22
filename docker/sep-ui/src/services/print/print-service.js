import {Injectable, Inject, NgTimeout} from 'app/app';
import $ from 'jquery';
import {Eventbus} from 'services/eventbus/eventbus';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Configuration} from 'common/application-config';

const PRINT_PAGE_ID = 'printPage';

export const PRINT_START_NOTIFICATION = 'action.print.idoc.notification';
export const PRINT_IN_PROGRESS_NOTIFICATION = 'action.print.idoc.in.progress.notification';
export const PRINT_TIMEOUT_NOTIFICATION = 'action.print.idoc.timeout.notification';

@Injectable()
@Inject(WindowAdapter, NotificationService, Eventbus, TranslateService, Configuration, NgTimeout)
export class PrintService {

  constructor(windowAdapter, notificationService, eventbus, translateService, configuration, $timeout) {
    this.windowAdapter = windowAdapter;
    this.notificationService = notificationService;
    this.eventbus = eventbus;
    this.translateService = translateService;
    this.$timeout = $timeout;
    // by default this configuration accept seconds
    this.defaultTimeout = configuration.get(Configuration.IDOC_PRINT_TIMEOUT) * 1000;
    // holds document urls for printing
    this.queue = [];
  }

  print(url) {
    this.queue.push(url);
    if (this.queue.length > 1) {
      this.sendNotification(PRINT_IN_PROGRESS_NOTIFICATION);
      return;
    }
    this.preparePrint(url);
  }

  preparePrint(url) {
    this.sendNotification(PRINT_START_NOTIFICATION);

    // Listen to for a message from the child window
    this.boundListener = this.onIdocReady.bind(this);
    this.windowAdapter.window.addEventListener('message', this.boundListener, false);

    // this timeout will trigger another print operation in case of current one hangs
    this.printTimeout = this.$timeout(() => {
      this.notificationService.error(this.translateService.translateInstant(PRINT_TIMEOUT_NOTIFICATION));
      this.cleanPrint();
    }, this.defaultTimeout);

    this.addPrintFrame(url);
  }

  addPrintFrame(url) {
    $('body').append(PrintService.getIframe({
      id: '',// set instance id
      url,
      // frame dimensions have to be set in order to have proper element sizes when document is printed
      frameWidth: $(window).width(),
      frameHeight: $(window).height()
    }));
  }

  sendNotification(message) {
    this.notificationService.info(this.translateService.translateInstant(message));
  }

  executePrint() {
    this.formatHtml();
    this.windowAdapter.window.frames.printPage.focus();
    this.windowAdapter.window.frames.printPage.print();
  }

  formatHtml() {
    // Prepare textareas for print by formatting inner html otherwise new lines are missing in print preview
    let textareas = $(`#${PRINT_PAGE_ID}`).contents().find('.textarea');
    textareas.each((ind, current) => {
      let formatedText = current.innerHTML.replace(/(\r\n|\n|\r)/gm, '<br/>');
      current.innerHTML = formatedText;
    });
  }

  cleanup(printPageIframe) {
    if (printPageIframe.length > 0) {
      printPageIframe.remove();
    }
  }

  onIdocReady(evt) {
    let key = evt.message ? 'message' : 'data';
    let data = evt[key];
    if (data === 'idocReady') {
      this.executePrint();
      this.cleanPrint();
    }
  }

  cleanPrint() {
    this.windowAdapter.window.removeEventListener('message', this.boundListener, false);
    this.$timeout.cancel(this.printTimeout);
    this.$timeout(() => {
      this.cleanup($(`#${PRINT_PAGE_ID}`));
      this.queue.shift();
      if (this.queue.length > 0) {
        this.preparePrint(this.queue[0]);
      }
    }, 100);
  }

  static getIframe(opts) {
    return `<iframe id="${PRINT_PAGE_ID}" name="${PRINT_PAGE_ID}" src="${opts.url}" style="position: absolute; top: -1000px; width: ${opts.frameWidth}px; height: ${opts.frameHeight}px; @media print { display: block; }"></iframe>`;
  }
}
