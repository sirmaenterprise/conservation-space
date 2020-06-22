import {Injectable} from 'app/app';
import {EditorContentProcessor} from 'idoc/editor/editor-content-processor';

import $ from 'jquery';
import 'common/lib/lazyload/jquery.lazyload';

/*
 Used when idoc is loaded to process the widget attributes.
 Processes the attributes to allow lazy widget loading.
 */
@Injectable()
export class EditorContentWidgetProcessor extends EditorContentProcessor {

  preprocessContent(editor, content){
    // not implemented
    return content;
  }
  // After CKEditor's content is set, applies widgets' lazy loading. In edit mode ensures CKEditor's undo/redo capability.
  postprocessContent(editorInstance) {
    let context = editorInstance.context;
    if (context.isPreviewMode() || context.isEditMode()) {
      this.setLazyLoad(editorInstance.editor);
    }
    if (context.isEditMode()) {
      let onPasteEvent = (event) => {
        let parser = new DOMParser();
        let html = parser.parseFromString(event.data.dataValue, 'text/html');
        let widgets = html.getElementsByClassName('widget');
        // skip processing if any widget has data-render=true to avoid endless loop with other async paste listeners
        let skipProcessing = false;
        for (let i = 0; i < widgets.length && !skipProcessing; i++) {
          if (widgets.item(i).getAttribute('data-render')) {
            skipProcessing = true;
          }
        }

        if (widgets.length > 0 && !skipProcessing) {
          this.processPastedContent(event, event.editor);
          event.cancel();
        }
      };

      let onAfterPasteEvent = (event) => {
        $(event.editor.element.find('[data-render="true"]').$).each((index, wdgt) => {
          let widget = $(wdgt);
          widget.removeAttr('data-render');
          widget.lazyload();
        });
      };

      let onAfterCommandExec = (event) => {
        if (event.data.name === 'undo' || event.data.name === 'redo') {
          this.setLazyLoad(editorInstance.editor);
        }
      };

      // Undo / Redo commands break widgets' lazy load and we have to reprocess editors content - if such widgets in tab.
      editorInstance.editor.on('afterCommandExec', onAfterCommandExec);
      editorInstance.editor.on('paste', onPasteEvent);
      editorInstance.editor.on('afterPaste', onAfterPasteEvent);
    }
  }

  processPastedContent(event, editor) {
    let parser = new DOMParser();
    let html = parser.parseFromString(event.data.dataValue, 'text/html');
    Array.from(html.getElementsByClassName('widget')).forEach(widget => widget.setAttribute('data-render', 'true'));

    event.data.dataValue = html.documentElement.innerHTML;
    editor.fire('paste', event.data);
  }

  // Applies lazy load to editor's instance window only.
  setLazyLoad(editor) {
    $(editor.element.find('.widget').$).lazyload();
  }

  detachLazyload(editor) {
    $(editor.element.find('.widget').$).each((index, wdgt) => {

      let widget = $(wdgt);
      if (widget.lazyload) {
        widget.lazyload.destroy();
      }
    });
  }
}