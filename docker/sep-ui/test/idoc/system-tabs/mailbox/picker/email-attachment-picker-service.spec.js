import {EmailAttachmentPickerService} from 'idoc/system-tabs/mailbox/picker/email-attachment-picker-service';
import {HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {EMF_VERSION} from 'instance/instance-properties';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {PickerService, SEARCH_EXTENSION, RECENT_EXTENSION} from 'services/picker/picker-service';
import {stub} from 'test/test-utils';

describe('EmailAttachmentPickerService', () => {

  let pickerServiceStub;
  let attachmentPickerService;

  beforeEach(() => {
    pickerServiceStub = stub(PickerService);
    attachmentPickerService = new EmailAttachmentPickerService(pickerServiceStub);
  });

  it('should open picker with class configuration', () => {
    let expectedPickerConfig = {
      extensions: {}
    };
    expectedPickerConfig.extensions[SEARCH_EXTENSION] = {
      arguments: {
        properties: ['id', HEADER_DEFAULT, HEADER_BREADCRUMB, EMF_VERSION, 'title']
      },
      results: {config: {selection: MULTIPLE_SELECTION, selectedItems: []}}
    };
    expectedPickerConfig.extensions[RECENT_EXTENSION] = {
      propertiesToLoad: ['id', HEADER_DEFAULT, HEADER_BREADCRUMB, EMF_VERSION, 'title']
    };

    attachmentPickerService.selectAttachments();
    expect(pickerServiceStub.configureAndOpen.calledOnce).to.be.true;
    expect(pickerServiceStub.configureAndOpen.getCall(0).args[0]).to.eql(expectedPickerConfig);
  });
});