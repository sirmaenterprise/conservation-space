import $ from 'jquery';
import _ from 'lodash';
import {ToCConstants} from './idoc-toc-constants';
import {ToCUtils} from './idoc-toc-utils';
import uuid from 'common/uuid';
import base64 from 'common/lib/base64';
import {IdocUpdateUndoManagerEvent} from '../events/idoc-update-undo-manager-event';

export class ToCDomManipulator {
  constructor(config, virtualTree) {
    this.config = config;
    this.tree = virtualTree;
  }

  /**
   * Traverses the dom and builds virtual tree.
   */
  buildFromDom() {
    let forest = new Map();
    $(ToCConstants.HEADERS_SELECTOR, this.config.source).each((i, element) => {
      let parentID = element.id;
      this.tree.createSection(parentID);
      let smallerOrEqualHeadingNumber = ToCUtils.getSmallerOrEqualHeadings($(element));

      $(element).nextUntil(smallerOrEqualHeadingNumber, ToCConstants.HEADERS_SELECTOR).each((i, node) => {
        forest.set(node.id, parentID);
      });
    });

    for (let key of forest.keys()) {
      if (key) {
        this.tree.createSection(key, forest.get(key));
      }
    }
  }

  /**
   * Traverse the collapsed sections and append them to their parent section.
   * @param clone if true content should be cloned (save, save draft). If false it will work directly with the current DOM (destroy of navigation section)
   * @returns {jQuery|HTMLElement} Object with the whole document content.
   */
  buildDocument(clone) {
    let documentDom = clone ? $(this.config.source).clone(true) : $(this.config.source);
    let collapsedSections = this.collapsedContainer.children('div');

    $(collapsedSections.get().reverse()).each(function () {
      let $element = $(this);
      let $parent = documentDom.find(`#${$element.attr('data-ref-id')}`);
      let children = clone ? $element.children().clone(true) : $element.children();
      children.insertAfter($parent);
    });

    return this.sanitizeDomContent(documentDom);
  }

  /**
   * Remove some constant classes
   */
  sanitizeDomContent(domContainer) {
    domContainer.find('*').removeClass(ToCConstants.COLLAPSED_CLASS + ' ' + ToCConstants.COLLAPSED_HIDDEN_CLASS + ' ' + ToCConstants.SECTION_SELECTED).css('display', '');
    domContainer.find('.' + ToCConstants.SECTION_COLLAPSE_BUTTON_CLASS).remove();
    return domContainer;
  }

  /**
   * Deletes the collapsed sub sections in the container that refers to their parent ID.
   * @param parentID {String} The parent id.
   */
  deleteCollapsedSection(parentID) {
    if (this.collapsedContainer) {
      let collapsedSectionContainer = this.collapsedContainer.find('[data-ref-id="' + parentID + '"]');
      if (collapsedSectionContainer.length > 0) {
        collapsedSectionContainer.remove();
      }
      //CMF-22341 update the undo manager, so that the most recent snapshot of the expanded/collapsed section is stored
      this.config.eventbus.instance.publish(new IdocUpdateUndoManagerEvent(this.config.tab));
    }
  }

  /**
   * Deletes the corresponding section dom elements and updates the change to the tree model.
   * @param id {String] The id of the section.
   */
  deleteSection(id) {
    let heading = $(document.getElementById(id));
    let smallerOrEqualHeadings = ToCUtils.getSmallerOrEqualHeadings(heading);
    heading.nextUntil(smallerOrEqualHeadings).each((i, element) => {
      this.deleteCollapsedSection(element.id);
    }).remove();

    heading.remove();
    this.deleteCollapsedSection(id);
    this.tree.deleteSubTree(id);
  }

  /**
   * Checks if every heading has its own id.
   * Adds the source encoded in base64 to the id in order to create unique ids.
   */
  checkHeadingsID() {
    $(ToCConstants.HEADERS_SELECTOR, this.config.source).each((i, element) => {
      let newHeading = false;

      /**
       * https://ittruse.ittbg.com/jira/browse/CMF-23047
       * Since you can copy and paste headings from different tabs
       * and the editor does not remove the id attributes there is chance of id collision.
       * The source selector is encoded and included as part of the heading id.
       * Id selectors cannot contain the '=' base64 padding symbol so we remove it.
       */
      let DIVIDER = '_';

      let sourceSelectorInBase64 = base64.encode(this.config.source).replace('=', '');


      let genSourcePrefix = () => {
        return DIVIDER + sourceSelectorInBase64;
      };

      if (element.id) {
        // the pasted content may contain id with invalid values that break the jquery selection
        // as far as I know only copied content from "confluence" have this problem since they add the content of the heading as id
        if (element.id.indexOf('"') !== -1) {
          element.id = uuid();
        }
      } else {
        element.id = uuid() + genSourcePrefix();
        newHeading = true;
      }

      var idTokens = element.id.split(DIVIDER);
      var origin = idTokens.length === 2 ? idTokens[1] : '';
      // if the heading is copied from another tab its origin should now be set to the current source
      if (!origin || ( origin && origin !== sourceSelectorInBase64 )) {
        element.id = idTokens[0] + genSourcePrefix();
      }

      let el = $(element);
      // When new heading is inserted
      // it should not be deleted so we add a whitespace to prevent it
      // safari don't handle empty spaces well that is why &nbsp is used
      if (el.text() === '' && newHeading) {
        el.html('&nbsp;');
      }

      let sectionContent = $('div[data-ref-id="' + element.id + '"]');

      /**
       * When an element, aka header has no value it should be removed.
       * If the heading was collapsed the content
       * is expanded in the editor area and the heading removed.
       */
      if (el.text() === '' && !newHeading) {
        sectionContent.detach();
        let sectionContentChildren = sectionContent.children();
        if (sectionContentChildren.length) {
          let notifyAfterAppendingToDocument = () => {
            this.config.eventbus.instance.publish({
              channel: this.config.eventbus.channel,
              topic: 'idoc:toc:heading-collapsed-expanded'
            });

            this.config.eventbus.instance.publish({
              channel: this.config.eventbus.channel,
              topic: 'idoc:toc:section-deleted',
              data: {
                sectionID: element.id
              }
            });
            el.remove();
          };
          let _notifyAfterAppendingToDocument = _.debounce(notifyAfterAppendingToDocument, 100);
          // For the same reason as above
          sectionContentChildren.insertAfter(el).toggleClass(ToCConstants.COLLAPSED_HIDDEN_CLASS).toggle('blind', _notifyAfterAppendingToDocument);
        } else {
          el.remove();
        }

      }

      if (!el.hasClass(ToCConstants.COLLAPSED_CLASS) && (el.next().length <= 0)) {
        // insert empty line if the heading has no content and it is not collapsed
        $('<p>').insertAfter(el);
      }

    });
  }

  /**
   * When the element is dropped finds if there is a previous sibling in the parent. If there is such,
   * the sections should be inserted in the new parent just after the previous sibling. If there isn't next sibling,
   * the section will be prepended to the new parent.
   *
   * @param movedElement {JQuery Obj}
   *    The view element that has been moved.
   *    The position of the header is calculated via the place of the corresponding element of the view.
   * @returns {boolean} Whether the movement was successful or not.
   */
  moveSection(movedElement) {
    let movedElementID = movedElement.children('a').attr('data-ref-id');
    let movedElementParentID = movedElement.parent().parent().children('a').data('ref-id');

    // When a subsection is made main it has no anchor then its parent is the main list
    if (!movedElementParentID) {
      movedElementParentID = null;
    }

    let sectionHeading = $(document.getElementById(movedElementID));
    let smallerOrEqualHeadings = ToCUtils.getSmallerOrEqualHeadings(sectionHeading);
    let sectionContent = sectionHeading.nextUntil(smallerOrEqualHeadings);

    let prevSibling = movedElement.prev();
    // if there is a previous sibling, insert content after it
    if (prevSibling.length) {
      let prevSectionHeading = $(document.getElementById(prevSibling.children('a').attr('data-ref-id')));

      if (this.canMove(sectionHeading, sectionContent, ToCUtils.getTagName(prevSectionHeading))) {
        this.detachSectionHeadingAndContent(sectionHeading, sectionContent);

        // find the content of the previous sibling
        let prevSectionContent = prevSectionHeading.nextUntil(ToCUtils.getSmallerOrEqualHeadings(prevSectionHeading));

        // if there is no content use the heading as it's the last element within the section
        if (!prevSectionContent.length) {
          prevSectionContent = prevSectionHeading;
        }
        // insert the dropped heading and its content just after the end of the content of the previous section
        sectionHeading.add(sectionContent).insertAfter(prevSectionContent.last());

        this.moveSectionInTheModel(movedElementID, movedElementParentID);
        this.recalculateHeadings(sectionHeading, sectionContent, ToCUtils.getTagName(prevSectionHeading));
        return true;
      }
    } else {
      let nextSibling = movedElement.next();
      if (nextSibling.length) {
        let nextSectionHeading = $(document.getElementById(nextSibling.children('a').attr('data-ref-id')));

        if (this.canMove(sectionHeading, sectionContent, ToCUtils.getTagName(nextSectionHeading))) {
          this.detachSectionHeadingAndContent(sectionHeading, sectionContent);
          // insert the dropped heading and its content just before the heading of the next section
          sectionHeading.add(sectionContent).insertBefore(nextSectionHeading);

          this.moveSectionInTheModel(movedElementID, movedElementParentID);
          this.recalculateHeadings(sectionHeading, sectionContent, ToCUtils.getTagName(nextSectionHeading));
          return true;
        }
      } else {
        return this.insertAfterRoot(movedElement, sectionHeading, sectionContent, movedElementID, movedElementParentID);
      }
    }
    return false;
  }

  /**
   * If there isn't a next or previous sibling insert the content directly after the content of the root heading
   * in the parent section and the new tag name will be greater by 1
   * @param movedElement {JQuery Object} The moved element.
   * @returns {boolean} True if the operation is successful, false otherwise.
   */
  insertAfterRoot(movedElement, sectionHeading, sectionContent, movedElementID, movedElementParentID) {
    let parentHeading = $(document.getElementById(movedElement.parent().closest('li').children('a').attr('data-ref-id')));
    if (parentHeading.length) {
      let parentHeadingNumber = 'H' + (parseInt(ToCUtils.getHeadingSize(parentHeading)) + 1);
      if (this.canMove(sectionHeading, sectionContent, parentHeadingNumber)) {
        this.detachSectionHeadingAndContent(sectionHeading, sectionContent);
        let parentContent = parentHeading.nextUntil(ToCConstants.HEADERS_SELECTOR);
        //if there is no content use the heading as it's the last element within the section
        if (!parentContent.length) {
          parentContent = parentHeading;
        }
        sectionHeading.add(sectionContent).insertAfter($(parentContent[parentContent.length - 1]));

        this.moveSectionInTheModel(movedElementID, movedElementParentID);
        //recalculate heading using the heading number of the parent + 1
        this.recalculateHeadings(sectionHeading, sectionContent, parentHeadingNumber);
        return true;
      }
    }
    return false;
  }

  /**
   * Updates the model for a movement of a section.
   * @param movedElementID {String}
   * @param movedElementParentID {String}
   */
  moveSectionInTheModel(movedElementID, movedElementParentID) {
    this.tree.moveSection(movedElementID, movedElementParentID);
  }

  /**
   * Detaches the section heading and its content so it doesn't interfere the algorithm.
   * @param sectionHeading {JQuery Obj}
   * @param sectionContent {JQuery Obj}
   */
  detachSectionHeadingAndContent(sectionHeading, sectionContent) {
    sectionHeading.detach();
    sectionContent.detach();
  }

  /**
   * When a section is moved into another place, its heading has to be recalculated according to the new position.
   * I.e. if the section was on second level (H2) and it has children with H3 but now it's moved on root position, its heading
   * has to be changed to H1 and the headings of its children has to be changed to H2 instead of H3.
   *
   * @param sectionHeading {JQuery Obj}
   * @param sectionContent {JQuery Obj}
   * @param newSectionTag {String}
   */
  recalculateHeadings(sectionHeading, sectionContent, newSectionTag) {
    let sectionTag = ToCUtils.getTagName(sectionHeading);
    if (sectionTag !== newSectionTag) {
      let difference = parseInt(newSectionTag.substring(1)) - parseInt(sectionTag.substring(1));
      sectionContent.filter(ToCConstants.HEADERS_SELECTOR).each((i, element) => {
        this.updateHeading($(element), difference);
      });

      this.updateHeading(sectionHeading, difference);
    }
  }

  /**
   *  Updates the heading of an element with the provided difference.
   *  I.e. if the heading is H2 and the difference is 1, then the heading becomes H3.
   *  If the difference is -1, the heading becomes H1.
   * @param heading {JQuery Obj} The heading.
   * @param difference {Integer} The difference.
   */
  updateHeading(heading, difference) {
    let newHeadingNumber = parseInt(ToCUtils.getHeadingSize(heading)) + difference;
    let newSection = $('<H' + newHeadingNumber + '>' + heading.html() + '</H' + newHeadingNumber + '>');
    let elementAttributes = heading.prop('attributes');

    for (let key in Object.keys(elementAttributes)) {
      if (elementAttributes[key]) {
        newSection.attr(elementAttributes[key].name, elementAttributes[key].value);
      }
    }

    heading.replaceWith(newSection);
  }

  /**
   * Creates div where the collapsed content will be kept.
   */
  createCollapseContainer() {
    let collapsedContainerID = this.config.collapsedContainer || ('collapse-container-' + this.config.source.substring(1));
    this.collapsedContainer = $('<div>').prop('id', collapsedContainerID).css('display', null).appendTo($('body'));
  }

  /**
   * Removes the div where the collapsed content was kept and all icons.
   */
  removeCollapseContainer() {
    if (this.collapsedContainer) {
      this.collapsedContainer.remove();
    }
    $('.' + ToCConstants.SECTION_COLLAPSE_BUTTON_CLASS, this.config.source).remove();
  }

  /**
   * Loops through the given heading content add css style.
   * It also updates the model that the heading has been hidden.
   *
   * @param section {JQuery Obj} The collapsed heading.
   */
  collapseExpandSection(section) {
    section.toggleClass(ToCConstants.COLLAPSED_CLASS);

    if (section.hasClass(ToCConstants.COLLAPSED_CLASS)) {

      let bodyContainer = $('<div>').css('display', null).attr('data-ref-id', section.prop('id'));
      let smallerOrEqualHeadings = ToCUtils.getSmallerOrEqualHeadings(section);
      let sectionContent = section.nextUntil(smallerOrEqualHeadings);

      let appendToTempContainer = () => {
        sectionContent.detach();
        sectionContent.appendTo(bodyContainer);
        this.collapsedContainer.append(bodyContainer);
        this.config.eventbus.instance.publish({
          channel: this.config.eventbus.channel,
          topic: 'idoc:toc:heading-collapsed-expanded'
        });
      };
      let _appendToTempContainer = _.debounce(appendToTempContainer, 100);
      /*
       * If you do .toggle('',callback) on array of JQuery elements it will execute it for each element, which is not okey.
       * Adding the callback only to the last element resulted in other behaviour, because the toggle('blind) animations is async.
       */
      sectionContent.toggleClass(ToCConstants.COLLAPSED_HIDDEN_CLASS).toggle('blind', _appendToTempContainer);
    } else {
      let sectionContent = $('div[data-ref-id="' + section.prop('id') + '"]', this.collapsedContainer);
      sectionContent.detach();
      let sectionContentChildren = sectionContent.children();
      let notifyAfterAppendingToDocument = () => {
        this.config.eventbus.instance.publish({
          channel: this.config.eventbus.channel,
          topic: 'idoc:toc:heading-collapsed-expanded'
        });
      };
      let _notifyAfterAppendingToDocument = _.debounce(notifyAfterAppendingToDocument, 100);
      // For the same reason as above
      sectionContentChildren.insertAfter(section).toggleClass(ToCConstants.COLLAPSED_HIDDEN_CLASS).toggle('blind', _notifyAfterAppendingToDocument);
    }

  }

  /**
   * After moving the section headings number need updating according to the level of nesting. Nesting on a level deeper
   * than six is impossible because there are no H7,H8,... tags.
   *
   * @param sectionHeading {JQuery Obj} The section which is beeing moved.
   * @param sectionContent {JQuery Obj} The content of the section i.e  object with all the subsections.
   * @param newSectionTag {String} The new section tag.
   * @returns {boolean} True if the move is legit otherwise false.
   */
  canMove(sectionHeading, sectionContent, newSectionTag) {
    let sectionTag = ToCUtils.getTagName(sectionHeading);
    if (newSectionTag !== sectionTag) {
      let difference = parseInt(newSectionTag.substring(1)) - parseInt(sectionTag.substring(1));
      let highestHeading = sectionTag;

      sectionContent.filter(ToCConstants.HEADERS_SELECTOR).each(function () {
        if (highestHeading == null || this.tagName > highestHeading) {
          highestHeading = this.tagName;
        }
      });

      let newHeadingNumber = parseInt(highestHeading.substring(1)) + difference;
      if (newHeadingNumber > 6) {
        return false;
      }
    }

    return true;
  }
}
