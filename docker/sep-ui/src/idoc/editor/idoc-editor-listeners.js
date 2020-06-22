import {IdocEditorSelectionListener} from './idoc-editor-selection-listener';
import {IdocEditorPasteListener} from './idoc-editor-paste-listener';
import {IdocEditorChangeListener} from './idoc-editor-change-listener';
import {PasteLayoutSanitizer} from './paste-layout-sanitizer';
import {PasteTableSanitizer} from './paste-table-sanitizer';

export class IdocEditorListeners {
  constructor(config) {
    this.listeners = [];
    this.listeners.push(new IdocEditorSelectionListener(config));
    this.listeners.push(new IdocEditorPasteListener());
    this.listeners.push(new IdocEditorChangeListener(config));
    this.listeners.push(new PasteLayoutSanitizer());
    this.listeners.push(new PasteTableSanitizer());
  }

  listen(editor) {
    for (let listener of this.listeners) {
      listener.listen(editor);
    }
  }

}