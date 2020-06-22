import {PasteBase64} from 'idoc/editor/plugins/paste-base64/paste-base64';
import {TranslateService} from 'services/i18n/translate-service';
import {stub} from 'test/test-utils';

describe('PasteBase64', () => {
  let pasteBase64;
  before(() => {
    pasteBase64 = new PasteBase64(stub(TranslateService));
  });

  describe('editorPasteHandler', () => {
    it('should not try convert to base64 if pasted content is part of excel document', () => {
      pasteBase64.pasteHandler = sinon.spy();
      let excelHtml = '<html xmlns:o="urn:schemas-microsoft-com:office:office" ' +
                            'xmlns:x="urn:schemas-microsoft-com:office:excel"' +
                            'xmlns="http://www.w3.org/TR/REC-html40"> ' +
                        '<head> ' +
                          '<meta http-equiv=Content-Type content="text/html; charset=utf-8"> ' +
                          '<meta name=ProgId content=Excel.Sheet> ' +
                          '<meta name=Generator content="Microsoft Excel 15"> ' +
                        '</head> ' +
                      '</html>';

      pasteBase64.editorPasteHandler(getEvent(excelHtml, 1), {});
      expect(pasteBase64.pasteHandler.called).to.be.false;
    });

    it('should not try convert to base64 if pasted content is part of word document', () => {
      pasteBase64.pasteHandler = sinon.spy();
      let wordHtml =  '<html xmlns:o="urn:schemas-microsoft-com:office:office" ' +
                          'xmlns:w="urn:schemas-microsoft-com:office:word" ' +
                          'xmlns:m="http://schemas.microsoft.com/office/2004/12/omml" ' +
                          'xmlns="http://www.w3.org/TR/REC-html40"> ' +
                        '<head> ' +
                          '<meta name=Title content=""> ' +
                          '<meta name=Keywords content=""> ' +
                          '<meta http-equiv=Content-Type content="text/html; charset=utf-8"> ' +
                          '<meta name=ProgId content=Word.Document> ' +
                          '<meta name=Generator content="Microsoft Word 15"> ' +
                          '<meta name=Originator content="Microsoft Word 15"> ' +
                        '</head> ' +
                      '</html>';

      pasteBase64.editorPasteHandler(getEvent(wordHtml, 1), {});
      expect(pasteBase64.pasteHandler.called).to.be.false;
    });

    it('should not try convert to base64 if pasted content is simple text', () => {
      pasteBase64.pasteHandler = sinon.spy();
      let textHtml =  '<html > ' +
                        '<head> ' +
                          '<meta charset="utf-8">' +
                          '<h1>Simple text</h1> ' +
                        '</head> ' +
                      '</html>';
      pasteBase64.editorPasteHandler(getEvent(textHtml, 0), {});
      expect(pasteBase64.pasteHandler.called).to.be.false;
    });

    it('should try convert to base64 if pasted content is image', () => {
      pasteBase64.pasteHandler = sinon.spy();
      let imageHtml = '<html > ' +
                        '<head> ' +
                          '<meta charset="utf-8">' +
                          '<img id="testPic" data-embedded-id="emf:f97221f1-ff3e-497c-a08c-e6267cb37622" data-original="../editor/test-image.png" src="../editor/test-image.png" /> ' +
                        '</head> ' +
                      '</html>';
      pasteBase64.editorPasteHandler(getEvent(imageHtml, 1), {});
      expect(pasteBase64.pasteHandler.called).to.be.true;
    });
  });
});

function getEvent(data, filesCount) {
  return {
    data: {
      dataTransfer: {
        getData: () => {
          return data;
        },
        getFilesCount: () => {
          return filesCount;
        }
      }
    }
  };
}