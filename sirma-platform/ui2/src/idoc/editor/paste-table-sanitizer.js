import _ from 'lodash';

export class PasteTableSanitizer {

  listen(editor) {
    editor.on('paste', this.onEditorPasteEvent.bind(this));
  }

  onEditorPasteEvent(event) {
    let transferType = event.data.dataTransfer.getTransferType(event.editor);

    if (transferType !== CKEDITOR.DATA_TRANSFER_INTERNAL) {
      let fragment = CKEDITOR.htmlParser.fragment.fromHtml(event.data.dataValue);

      let pasteFilter = this.getPasteFilter();
      pasteFilter.applyTo(fragment);

      let writer = new CKEDITOR.htmlParser.basicWriter();
      fragment.writeHtml(writer);
      event.data.dataValue = writer.getHtml();
    }
  }

  getPasteFilter() {
    let rules = {
      elements: {
        table: (element) => {
          this.removeCKEditorElementStyles(element);
          element.addClass('sep-table');
          element.attributes.border = 1;
          element.attributes.cellpadding = 1;
          element.attributes.cellspacing = 1;
        },
        td: (element) => {
          this.removeCKEditorElementStyles(element);
          let compiledElement = $(element.getOuterHtml());
          let cellLink = compiledElement.find('a').eq(0);
          let elementText;
          if (cellLink.length) {
            elementText = $('<a/>', {href: cellLink.attr('href'), text: cellLink.text()})[0].outerHTML;
          } else {
            elementText = compiledElement.text();
          }
          if (_.isEmpty(elementText)) {
            // default behavior of table plugin
            elementText = '<br />';
          }
          element.setHtml(elementText);
        },
        tr: (element) => {
          this.removeCKEditorElementStyles(element);
        },
        colgroup: () => {
          // removes element
          return false;
        }
      }
    };
    return new CKEDITOR.htmlParser.filter(rules);
  }

  removeCKEditorElementStyles(element) {
    delete element.attributes.style;
    delete element.attributes.class;
    delete element.attributes.height;
    delete element.attributes.width;
  }
}
