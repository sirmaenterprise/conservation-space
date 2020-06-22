export const SELECTION_CHANGED_TOPIC = 'idoc:editor:selection-changed';

export class IdocEditorSelectionListener {

  constructor(config) {
    this.eventbus = config.eventbus;
  }

  listen(editor) {
    editor.on('selectionChange', () => {
      // At the moment when selectionChange event is fired selected node is still not changed from editor
      // this happened under Chrome only
      setTimeout(() => {
        let selection = editor.getSelection();
        // there are cases where the selection doesn't provide start element
        if (selection && selection.getStartElement()) {
          editor.fire('lockSnapshot');
          this.publishSelectionChangedEvent(selection);
          editor.fire('unlockSnapshot');

          // for Firefox only
          if (!editor.readOnly && CKEDITOR.env.gecko) {
            this.updateRangeForNotEditableElements(selection);
          }
        }
      }, 0);
    });

    // Publish empty selection when editor has lost its focus
    editor.on('blur', () => {
      this.publishSelectionChangedEvent();
    });
  }

  /**
   * Update range if selection is in non editable (contenteditable=false) element like inline widgets.
   * Moving cursor to the first editable position after such element to avoid problems with FF.
   * @param selection current selection
   */
  updateRangeForNotEditableElements(selection) {
    let ranges = selection.getRanges();
    // skip if more than one range is selected or selected element is editable or whole element is selected
    if (ranges.length !== 1 || selection.getStartElement().isEditable() || selection.getSelectedElement()) {
      return;
    }

    let range = ranges[0];
    let ancestor = range.getCommonAncestor(true, true);
    let boundaryNodes = range.getBoundaryNodes();
    if (!ancestor.isEditable()) {
      let topNonEditableElement = this.getTopNonEditableElement(ancestor);
      // if selection is only within one non editable element move the cursor after it
      if (!this.getClosestNodeElement(boundaryNodes.startNode).equals(this.getClosestNodeElement(boundaryNodes.endNode))) {
        selection.selectElement(topNonEditableElement);
      } else {
        range.moveToClosestEditablePosition(topNonEditableElement, true);
        selection.selectRanges(ranges);
      }
    }
  }

  /**
   * Returns first parent which is node
   * @param element
   * @returns {*}
   */
  getClosestNodeElement(element) {
    if (element.type === CKEDITOR.NODE_ELEMENT) {
      return element;
    } else {
      return this.getClosestNodeElement(element.getParent());
    }
  }

  /**
   * Return first parent which is editable
   * @param element
   * @param child
   * @returns {*}
   */
  getTopNonEditableElement(element) {
    if (!element.getParent()) {
      return;
    }
    if (element.getParent().isEditable()) {
      return element;
    } else {
      return this.getTopNonEditableElement(element.getParent());
    }
  }

  /**
   * Fire selection changed event
   */
  publishSelectionChangedEvent(selection) {
    this.eventbus.instance.publish({
      channel: this.eventbus.channel,
      topic: SELECTION_CHANGED_TOPIC,
      data: selection
    });
  }

}