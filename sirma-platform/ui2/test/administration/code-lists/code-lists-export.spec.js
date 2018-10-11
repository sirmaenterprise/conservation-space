import {CodeListsExport} from 'administration/code-lists/export/code-lists-export';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub'
import FileSaver from 'file-saver';
import {stub} from 'test/test-utils';

describe('CodeListsExport', () => {

  let codeListsExport;

  beforeEach(() => {
    codeListsExport = new CodeListsExport(stub(NotificationService), stub(TranslateService));
    codeListsExport.translateService.translateInstant.returns('translated-export-message');
    codeListsExport.ngOnInit();
  });

  it('should call export and click the download clink', () => {
    let data = new Uint8Array([1, 2, 3]);
    let saverStub = sinon.stub(FileSaver, 'saveAs');
    codeListsExport.onExport = sinon.spy(() => PromiseStub.resolve(data));
    codeListsExport.exportCodeLists();

    expect(saverStub.calledOnce).to.be.true;
    expect(codeListsExport.onExport.calledOnce).to.be.true;

    saverStub.restore();
  });

  it('should notify user when export succeed', () => {
    codeListsExport.onExport = sinon.spy(() => PromiseStub.resolve('data'));
    codeListsExport.exportCodeLists();
    expect(codeListsExport.notificationService.success.calledOnce).to.be.true;
  });

  it('should notify user when export fails', () => {
    codeListsExport.onExport = sinon.spy(() => PromiseStub.reject());
    codeListsExport.exportCodeLists();
    expect(codeListsExport.notificationService.error.calledOnce).to.be.true;
  });
});