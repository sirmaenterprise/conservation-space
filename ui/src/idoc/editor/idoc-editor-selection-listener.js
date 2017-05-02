import $ from 'jquery';
import {ToCConstants} from 'idoc/idoc-toc/idoc-toc-constants';

export class IdocEditorSelectionListener {

  constructor(config) {
    this.eventbus = config.eventbus;
  }

  listen(editor) {
    // this variable is global because it should cache data ignoring the event.
    this.lastSelectedHeadingID;

    editor.on('selectionChange', ()=> {
      // At the moment when selectionChange event is fired selected node is still not changed from editor
      // this happened under Chrome only
      setTimeout(() => {
        let selection = editor.getSelection();
        // there are cases where the selection doesn't provide start element
        if (selection && selection.getStartElement()) {
          let startElement = selection.getStartElement();
          this.styleCurrentOrNearestHeading(startElement.$);
          this.publishSelectionChangedEvent();

          // for Firefox only
          if (!editor.readOnly && CKEDITOR.env.gecko) {
            this.updateRangeForNotEditableElements(selection);
          }
        }
      }, 0);
    });

    // Deselect header when focus is lost. For example when document is changed from edit to preview mode
    editor.on('blur', () => {
      $(document.getElementById(this.lastSelectedHeadingID)).removeClass(ToCConstants.SECTION_SELECTED);
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
   * Checks whether the element is heading.
   * If it is applies class style in order to style the heading.
   * If not it applies the class style to the nearest parent heading.
   * @param element {HTML Element} The element.
   */
  styleCurrentOrNearestHeading(element) {
    let jqElement = $(element);
    if (this.isHeading(jqElement)) {
      jqElement.addClass(ToCConstants.SECTION_SELECTED);
      if (this.lastSelectedHeadingID !== element.id) {
        $(document.getElementById(this.lastSelectedHeadingID)).removeClass(ToCConstants.SECTION_SELECTED);
      }
      this.lastSelectedHeadingID = element.id;
    } else {
      if (this.lastSelectedHeadingID) {
        $(document.getElementById(this.lastSelectedHeadingID)).removeClass(ToCConstants.SECTION_SELECTED);
      }
      let prevElement = jqElement.prev();
      if (this.isListItem(jqElement)) {
        //if it is a list item, we need to get the parent <ul> first, and then get its previous
        prevElement = jqElement.parent().prev();
      }
      this._findNearestHeading(prevElement, jqElement);
    }
  }

  /**
   * Finds the nearest heading and adds selected class
   * @param prevElement {JQuery Object}
   * @private
   */
  _findNearestHeading(prevElement, jqElement) {
    if (prevElement.length) {
      if (!this.isHeading(prevElement)) {
        prevElement = this.getNearestHeadingInternal(jqElement);
      }
      if (prevElement.length) {
        this.lastSelectedHeadingID = prevElement.prop('id');
        prevElement.addClass(ToCConstants.SECTION_SELECTED);
      }
    }
  }

  /**
   * Finds the nearest predecessor heading, starting from the appropriate DOM element.
   *
   * @param element {JQuery Object} the initial start element
   * @returns {JQuery Object} the nearest heading
   */
  getNearestHeadingInternal(element) {
    if (this.isListItem(element)) {
      //Search for headings, starting from the parent <ul>, not from the list item itself
      element = element.parent();
    }
    return element.prevUntil(ToCConstants.HEADERS_SELECTOR).last().prev();
  }

  /**
   * Checks if the provided element is a list item - <li>.
   */
  isListItem(element) {
    return element.prop('tagName') === 'LI';
  }

  /**
   * Checks if the element is heading
   * @param element {JQuery Obj} The element that need a check.
   */
  isHeading(element) {
    return (element.prop('tagName').substring(0, 1) === 'H');
  }

  /**
   * Fire selection changed event
   */
  publishSelectionChangedEvent() {
    this.eventbus.instance.publish({
      channel: this.eventbus.channel,
      topic: 'idoc:editor:selection-changed'
    });
  }

}