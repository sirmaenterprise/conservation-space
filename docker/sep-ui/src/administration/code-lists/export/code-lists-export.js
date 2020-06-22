import {Component, View, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import FileSaver from 'file-saver';

import './code-lists-export.css!css';
import template from './code-lists-export.html!text';

const CONTENT_TYPE = {type: 'application/vnd.ms-excel'};
const CODE_LISTS_FILENAME = 'Controlled-Vocabularies-List.xls';

@Component({
  selector: 'seip-code-lists-export',
  events: ['onExport']
})
@View({
  template
})
@Inject(NotificationService, TranslateService)
export class CodeListsExport {

  constructor(notificationService, translateService) {
    this.translateService = translateService;
    this.notificationService = notificationService;
  }

  ngOnInit() {
    this.errorMessage = this.translateService.translateInstant('code.lists.export.message.error');
    this.successMessage = this.translateService.translateInstant('code.lists.export.message.success');
  }

  exportCodeLists() {
    if (this.onExport) {
      this.onExport().then(data => {
        let blob = new Blob([data], CONTENT_TYPE);
        FileSaver.saveAs(blob, CODE_LISTS_FILENAME);
        this.notificationService.success(this.successMessage);
      }).catch(() => {
        this.notificationService.error(this.errorMessage);
      });
    }
  }
}