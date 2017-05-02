import {Injectable, Inject, NgTimeout} from 'app/app';
import $ from 'jquery';
import _ from 'lodash';
import {Eventbus} from 'services/eventbus/eventbus';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {NotificationService} from 'services/notification/notification-service';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';

const PRINT_PAGE_ID = 'printPage';

@Injectable()
@Inject(WindowAdapter, NotificationService, Eventbus, TranslateService, NgTimeout)
export class PrintService {

  constructor(windowAdapter, notificationService, eventbus, translateService, $timeout) {
    this.eventbus = eventbus;
    this.$timeout = $timeout;
    this.windowAdapter = windowAdapter;
    this.translateService = translateService;
    this.notificationService = notificationService;
  }

  print(url) {
    this.preparePrint(url);
  }

  preparePrint(url) {
    let notification = this.translateService.translateInstant('action.print.idoc.notification');
    this.notificationService.info(notification);

    // Remove previous print iframe if such exists to avoid concurrent print operations
    if ($('#' + PRINT_PAGE_ID).length > 0) {
      $('#' + PRINT_PAGE_ID).remove();
    }

    // Listen to for a message from the child window
    this.boundListener = this.onIdocReady.bind(this);
    this.windowAdapter.window.addEventListener('message', this.boundListener, false);
    $('body').append(PrintService.getIframe({
      id: '',// set instance id
      url: url,
      // frame dimensions have to be set in order to have proper element sizes when document is printed
      frameWidth: $(window).width(),
      frameHeight: $(window).height()
    }));
  }

  executePrint() {
    // Prepare textareas for print by formatting inner html otherwise new lines are missing in print preview
    let textareas = $('#' + PRINT_PAGE_ID).contents().find('.textarea');
    textareas.each((ind, current) => {
      let formatedText = current.innerHTML.replace(/(\r\n|\n|\r)/gm, '<br/>');
      current.innerHTML = formatedText;
    });
    this.windowAdapter.window.frames.printPage.focus();
    this.windowAdapter.window.frames.printPage.print();
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
      this.windowAdapter.window.removeEventListener('message', this.boundListener, false);
      this.executePrint();
      this.$timeout(() => {
        this.cleanup($('#' + PRINT_PAGE_ID));
      }, 100);
    }
  }

  static getIframe(opts) {
    return `<iframe id="${PRINT_PAGE_ID}" name="${PRINT_PAGE_ID}" src="${opts.url}" style="position: absolute; top: -1000px; width: ${opts.frameWidth}px; height: ${opts.frameHeight}px; @media print { display: block; }"></iframe>`;
  }
}