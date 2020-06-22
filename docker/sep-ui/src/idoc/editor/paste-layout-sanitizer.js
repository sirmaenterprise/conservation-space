export class PasteLayoutSanitizer {

  listen(editor) {
    editor.on('paste', this.onEditorPasteEvent.bind(this));
  }

  onEditorPasteEvent(event) {
    let pastedContent = $('<div>').append(event.data.dataValue);

    let layoutEditable = pastedContent.find('.layout-column-editable');

    layoutEditable.each((index, elem) => {
      let editable = $(elem);
      let column = editable.closest('.layout-column');

      if (column.length > 0) {
        // check if it has immediate row because layouts might be nested
        let row = column.parentsUntil('.layout-column-editable', '.layout-row');
        if (row.length > 0) {
          let container = row.closest('.layout-container');
          if (container.length === 0) {
            row.wrap('<div class="layout-container"></div>');
          }
        } else {
          // if there is no row we remove the layout because it is broken
          this.unwrap(column);
          this.unwrap(editable);
        }
      } else {
        this.unwrap(editable);
      }
    });

    event.data.dataValue = pastedContent.html();

  }

  unwrap(elem) {
    elem.replaceWith(elem.contents());
  }

}