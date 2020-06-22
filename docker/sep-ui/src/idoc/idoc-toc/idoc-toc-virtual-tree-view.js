import $ from 'jquery';
import {DragAndDrop} from 'components/draganddrop/drag-and-drop';
import {ToCUtils} from './idoc-toc-utils';
import {ToCConstants} from './idoc-toc-constants';

export class ToCVirtualTreeView {

  constructor(config, tocVirtualTree) {
    this.config = config;
    this.tree = tocVirtualTree;
  }

  clearView() {
    let navigationRoot = $(this.config.navigation);
    navigationRoot.empty();
  }

  // istanbul ignore next
  correctChanges(virtualTreeFromDom) {
    this.tree = virtualTreeFromDom;
    this.refresh();
  }

  // istanbul ignore next
  refresh() {
    this.clearView();
    this.build();
  }

  /**
   * Finds the corresponding section of the view. Calculates the highest section heading number.
   * @param sectionView The item from the navigation view.
   * @returns {{headingNumber: (number|*|Number), highestContentHeadingNumber: (number|*|Number)}}
   */
  cacheHeadingSectionProperties(sectionView) {
    let correspondingDomSection = document.getElementById(sectionView.children('a').data('ref-id'));
    let domSection = $(correspondingDomSection, this.config.source);
    let smallerOrEqualHeadings = ToCUtils.getSmallerOrEqualHeadings(domSection);
    let domSectionContent = domSection.nextUntil(smallerOrEqualHeadings);

    let highestHeadingNumber = parseInt(domSection.prop('tagName').substring(1));
    let headingSize;
    domSectionContent.filter(ToCConstants.HEADERS_SELECTOR).each((i, element)=> {
      headingSize = parseInt(element.tagName.substring(1));
      if (headingSize > highestHeadingNumber) {
        highestHeadingNumber = headingSize;
      }
    });

    headingSize = parseInt(domSection.prop('tagName').substring(1));

    return {
      headingNumber: headingSize,
      highestContentHeadingNumber: highestHeadingNumber
    };
  }

  /**
   * After moving the section headings number need updating according to the level of nesting. Nesting on a level deeper
   * than six is impossible.
   *
   * @param target {JQuery Obj} The element of the current drop position.
   * @param cache {Object} Contains the highest number heading of the corresponding currently dragged section
   * @returns {boolean} Whether the ` is valid or not.
   */
  isValidMove(target, cache) {
    let targetDomSectionID = target.parent().children('a').data('ref-id');
    let targetDomSectionTagNumber;
    if (targetDomSectionID) {
      targetDomSectionTagNumber = parseInt($(document.getElementById(targetDomSectionID), this.config.source).prop('tagName').substring(1));
    } else {
      targetDomSectionTagNumber = 0;
    }
    let difference = targetDomSectionTagNumber + 1 - cache.headingNumber;
    return (cache.highestContentHeadingNumber + difference) <= 6;
  }

  makeNestable(element) {
    let cached = {};
    DragAndDrop.makeDraggable(element, {
      forcePlaceholderSize: true,
      itemSelector: 'li',
      //handle: '',
      placeholderClass: 'placeholder',
      placeholder: '<li class="placeholder idoc-nav-placeholder"></li>',
      containerSelector: 'ul',
      delay: 100,
      draggedClass: 'dragged',
      itemPositionValidClass: 'item-position-valid',
      itemPositionInvalidClass: 'item-position-invalid',
      bodyClass: 'dragging',
      onDragStart: (item, container)=> {
        item.addClass(container.group.options.draggedClass);
        $('body').addClass(container.group.options.bodyClass);
        item.css({
          'display': 'block'
        });
        cached = this.cacheHeadingSectionProperties(item);
      },
      isValidTarget: (item, container)=> {
        let valid = this.isValidMove(container.el, cached);
        if (valid) {
          item.addClass(container.group.options.itemPositionValidClass);
          item.removeClass(container.group.options.itemPositionInvalidClass);
        } else {
          item.addClass(container.group.options.itemPositionInvalidClass);
          item.removeClass(container.group.options.itemPositionValidClass);
        }
        return valid;
      },
      onDrop: (item, container)=> {
        item.removeClass(container.group.options.draggedClass).removeAttr('style');
        item.removeClass(container.group.options.itemPositionInvalidClass);
        item.removeClass(container.group.options.itemPositionValidClass);
        $('body').removeClass(container.group.options.bodyClass);
        this.config.eventbus.instance.publish({
          channel: this.config.eventbus.channel,
          topic: 'idoc:toc:section-moved',
          data: {
            uiItem: item
          }
        });
        return false;
      }
    });
  }

  build() {
    let navigationRoot = $(this.config.navigation);
    let sectionRootsDomElements = this.tree.fullTraversal(new TreeViewDecorator(this.config, this.tree));
    let treeViewElement = $('<ul>');
    treeViewElement.addClass('idoc-toc');
    treeViewElement.attr('data-ref-id', this.config.source.substring(1));

    for (let element of sectionRootsDomElements) {
      treeViewElement.append(element.element);
    }

    navigationRoot.append(treeViewElement);
    if (!this.config.previewMode) {
      this.makeNestable(navigationRoot.children('ul'));
    }
  }
}

class TreeViewDecorator {

  constructor(config, tree) {
    this.treeViewElements = new TreeViewElements(config);
    this.tree = tree;
  }

  /**
   * Creates dom element for the node.
   * @param node {String}
   */
  decorateNode(node) {
    this.treeViewElements.createElement(node);
  }

  /**
   * Appends the child dom element to the parent one.
   * @param node {String}
   * @param child {String}
   */
  decorateNodeChild(node, child) {
    this.treeViewElements.get(node).append(child);
  }

  /**
   * Adds all root dom elements to array.
   * @param roots {Array}
   */
  decorateRoots(roots) {
    let rootsElements = [];
    for (let root of roots) {
      rootsElements.push(this.treeViewElements.get(root));
    }
    return rootsElements;
  }
}

class TreeViewElements {
  constructor(config) {
    this.elements = [];
    this.config = config;
  }

  createElement(node) {
    this.elements[node] = new TreeViewElement(node, this);
  }

  get(node) {
    return this.elements[node];
  }

}

class TreeViewElement {

  constructor(sectionID, treeViewElements) {
    this.sectionID = sectionID;
    this.treeViewElements = treeViewElements;
    this.config = treeViewElements.config;
    this.element = this.createElement();
  }

  createAnchor() {
    let headingElement = $(document.getElementById(this.sectionID));
    let anchor = $('<a class="idoc-section-link" href="javascript:void(0);" data-ref-id="' + this.sectionID + '" />').text(headingElement.text());

    anchor.click((event)=> {
      headingElement = $(document.getElementById(this.sectionID));
      let editorFieldParent = $(this.config.source).parent();
      let topPosition = editorFieldParent.scrollTop() - editorFieldParent.offset().top + headingElement.offset().top;

      // Animate the transition to the selected header.
      $('html, body').animate({
        scrollTop: topPosition
      }, this.config.scrollTime);

      event.preventDefault();
    });

    // check if the section is selected
    if (headingElement.hasClass(ToCConstants.SECTION_SELECTED)) {
      anchor.addClass(ToCConstants.SECTION_VIEW_SELECTED);
    }

    return anchor;
  }

  /**
   *  Creates the collapse expand button of the view.
   *  @return {JQuery Obj} The icon button.
   */
  createCollapseExpandButton() {
    // check the current status of the section - it is either collapsed or expanded
    let iconClass;
    let correspondingHeading = $(document.getElementById(this.sectionID));
    if (correspondingHeading.hasClass(ToCConstants.COLLAPSED_CLASS)) {
      iconClass = ToCConstants.COLLAPSED_ICON_CLASS;
    } else {
      iconClass = ToCConstants.EXPANDED_ICON_CLASS;
    }

    let icon = $(`<span class="toc-icon no-print ${iconClass}"></span>`);
    icon.click(() => {
      this.config.eventbus.instance.publish({
        channel: this.config.eventbus.channel,
        topic: 'idoc:editor:heading-collapsed-expanded',
        data: {
          sectionID: this.sectionID
        }
      });
      // If you try to collapse empty section it won't trigger view refresh so we change it manually.
      icon.toggleClass(ToCConstants.EXPANDED_ICON_CLASS).toggleClass(ToCConstants.COLLAPSED_ICON_CLASS);

    });

    return icon;
  }

  createElement() {
    let element = $('<li>');
    element.anchor = this.createAnchor();
    element.collapseExpandButton = this.createCollapseExpandButton();
    element.append(element.collapseExpandButton);
    element.append(element.anchor);
    // This is fix for the http://johnny.github.io/jquery-sortable/ because the plugin
    // does not allow nesting unless there is nested 'ul' tag.
    element.append($('<ul>'));

    return element;
  }

  append(node) {
    let ul = this.element.children('ul').first();
    let nodeElement = ul.append(this.treeViewElements.get(node).element);
    this.element.append(nodeElement);
  }

}
