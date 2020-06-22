import {PasteTableSanitizer} from 'idoc/editor/paste-table-sanitizer';

describe('PasteTableSanitizer', () => {
  let pasteTableSanitizer;
  before(() => {
    pasteTableSanitizer = new PasteTableSanitizer();
  });

  it('should sanitize tables pasted from external source', () => {
    let expectedDataValue = '<table class="sep-table" border="1" cellpadding="1" cellspacing="1"><tbody><tr><td>Cell 1.1</td><td>Cell 1.2</td><td><br /></td></tr></tbody></table>';
    let event = mockEvent('<table style="border-collapse:collapse; border:none; width:100%; table-layout:fixed"><colgroup><col style=""><col style=""></colgroup><tbody><tr height="17" style="height:12.75pt"><td height="17" class="xl24" style="border:none; height:12.75pt; white-space:nowrap"><span style="font-weight:400"><span style="font-style:normal">Cell 1.1</span></span></td><td class="xl25" style="border:none; white-space:nowrap">Cell 1.2</td><td><span></span></td></tr></tbody></table>',
      CKEDITOR.DATA_TRANSFER_EXTERNAL);
    pasteTableSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals(expectedDataValue);
  });

  it('should not sanitize tables pasted from another/same editor', () => {
    let expectedDataValue = '<table class="custom-class"><tbody><tr><td width="60%"><a href="test-url.com">Cell 1.1</a></td><td width="40%">Cell 1.2</td></tr></tbody></table>';
    let event = mockEvent('<table class="custom-class"><tbody><tr><td width="60%"><a href="test-url.com">Cell 1.1</a></td><td width="40%">Cell 1.2</td></tr></tbody></table>',
      CKEDITOR.DATA_TRANSFER_INTERNAL);
    pasteTableSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals(expectedDataValue);
  });

  function mockEvent(dataValue, transferType) {
    return {
      data: {
        dataTransfer: {
          getTransferType: () => transferType
        },
        dataValue
      }
    };
  }
});
