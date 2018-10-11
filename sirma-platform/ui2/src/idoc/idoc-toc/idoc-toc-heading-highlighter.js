import {SELECTION_CHANGED_TOPIC} from 'idoc/editor/idoc-editor-selection-listener';
import {ToCConstants} from 'idoc/idoc-toc/idoc-toc-constants';
import $ from 'jquery';

export class IdocTocHeadingHighlighter {
  constructor(config) {
    this.eventbus = config.eventbus.instance;
    this.channel = config.eventbus.channel;
    this.contentContainer = $(config.source);
    this.events = [this.eventbus.subscribe({
      channel: this.channel,
      topic: SELECTION_CHANGED_TOPIC,
      callback: this.onSelectionChanged.bind(this)
    })];
  }

  onSelectionChanged(selection) {
    if (selection && selection.getStartElement()) {
      let startElement = selection.getStartElement();
      this.styleCurrentOrNearestHeading(startElement.$);
    } else {
      this.removePreviousHeaderSelection();
    }
  }

  /**
   * Checks whether the element is heading.
   * If it is applies class style in order to style the heading.
   * If not it applies the class style to the nearest parent/sibling heading.
   * @param element {HTML Element} The element.
   */
  styleCurrentOrNearestHeading(element) {
    let jqElement = $(element);

    if (!this.isHeading(jqElement)) {
      jqElement = this.findNearestHeading(jqElement);
    }
    this.removePreviousHeaderSelection();
    this.markHeadingAsSelected(jqElement);
  }

  removePreviousHeaderSelection() {
    if (this.lastSelectedHeadingID) {
      $(`#${this.lastSelectedHeadingID}`).removeClass(ToCConstants.SECTION_SELECTED);
    }
  }

  markHeadingAsSelected(headingEl) {
    if (headingEl) {
      this.lastSelectedHeadingID = headingEl.attr('id');
      headingEl.addClass(ToCConstants.SECTION_SELECTED);
    }
  }

  /**
   * Finds the nearest heading and adds selected class
   * @param prevElement {JQuery Object}
   * @private
   */
  findNearestHeading(jqElement) {
    // If there is no heading before the selected position
    if (jqElement.is(this.contentContainer)) {
      return null;
    }

    // Check if the previous element is a heading since because prevUntil search will not match it
    let headingCandidate = jqElement.prev();
    if (headingCandidate.length > 0 && this.isHeading(headingCandidate)) {
      return headingCandidate;
    }

    // Search for heading in the siblings of the current element
    let siblingHeader = jqElement.prevUntil(ToCConstants.HEADERS_SELECTOR).last();

    headingCandidate = siblingHeader.prev();
    if (headingCandidate.length > 0 && this.isHeading(headingCandidate)) {
      return headingCandidate;
    }

    // go to the parent and search again
    return this.findNearestHeading(jqElement.parent());

  }

  /**
   * Checks if the element is heading
   * @param element {JQuery Obj} The element that need a check.
   */
  isHeading(element) {
    return (element.prop('tagName').substring(0, 1) === 'H');
  }

  destroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}