import $ from 'jquery';
import {ToCVirtualTreeView} from './idoc-toc-virtual-tree-view';
import {ToCVirtualTree} from './idoc-toc-virtual-tree';
import {ToCDomManipulator} from './idoc-toc-dom-manipulator';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import {NavigationEnabledEvent} from 'idoc/idoc-navigation/navigation-enabled-event';
import {IdocTocHeadingHighlighter} from './idoc-toc-heading-highlighter';
import {IdocContentSanitizedEvent} from 'idoc/events/idoc-content-sanitized-event';
import {AfterIdocSaveEvent} from 'idoc/actions/events/after-idoc-save-event';

import {SELECTION_CHANGED_TOPIC} from 'idoc/editor/idoc-editor-selection-listener';
import _ from 'lodash';
import './idoc-toc.css!css';

export class IdocToC {

  constructor(config) {
    this.config = config;

    this.headingHigligther = new IdocTocHeadingHighlighter({
      eventbus: {
        instance: this.config.eventbus.instance,
        channel: this.config.tab.id
      },
      source: this.config.source
    });

    let tree = new ToCVirtualTree();
    this.domTree = new ToCDomManipulator(this.config, tree);
    this.domTree.checkHeadingsID();
    this.domTree.createCollapseContainer();
    this.domTree.buildFromDom();

    this.view = new ToCVirtualTreeView(this.config, tree);
    this.view.build();

    this.events = [];

    let eventbus = this.config.eventbus.instance;
    let channel = this.config.eventbus.channel;

    this.events.push(eventbus.subscribe(EditorReadyEvent, (event) => {
      this.subscribeAndRefreshToC(event.editorId);
    }));

    this.events.push(eventbus.subscribe(NavigationEnabledEvent, (event) => {
      this.subscribeAndRefreshToC(event.id);
    }));

    this.events.push(eventbus.subscribe(AfterIdocSaveEvent, () => {
      // save operation reinitializes the editor, so container needs to be cleared of data also.
      this.domTree.collapsedContainer.children().remove();
    }));

    // We don't need to rebuild the toc so often.
    let _slowRefresh = _.debounce(this.refresh.bind(this), 500);
    this.events.push(eventbus.subscribe({
      channel,
      topic: 'idoc:editor:content-changed',
      callback: () => {
        _slowRefresh();
      }
    }));

    this.events.push(eventbus.subscribe({
      channel,
      topic: 'idoc:toc:section-moved',
      callback: (data) => {
        this.domTree.moveSection(data.uiItem);
      }
    }));

    /**
     * Debounces the collapsing and expanding section functionality in order to prevent it
     * from user abuse that would break the content.
     * Executes at the beginning for better user experience.
     */
    let _collapseExpandSection = _.debounce((sectionID) => {
      let section = $(this.config.source).find('#' + sectionID);
      this.domTree.collapseExpandSection(section);
    }, 250, {'leading': true, 'trailing': false});

    this.events.push(eventbus.subscribe({
      channel,
      topic: 'idoc:editor:heading-collapsed-expanded',
      callback(data) {
        _collapseExpandSection(data.sectionID);
      }
    }));

    this.events.push(eventbus.subscribe({
      channel,
      topic: 'idoc:toc:heading-collapsed-expanded',
      callback: () => {
        this.refresh();
      }
    }));

    /**
     * Slows refresh function in case the user abuses the document with many continues 'enters'.
     * Sets the execution at the beginning and at the end for better user experience.
     */
    let _mediumRefresh = _.debounce(this.refresh.bind(this), 200, {'leading': true, 'trailing': true});
    this.events.push(eventbus.subscribe({
      channel,
      topic: SELECTION_CHANGED_TOPIC,
      callback() {
        _mediumRefresh();
      }
    }));
  }

  subscribeAndRefreshToC(id) {
    if ((id === $(this.config.source).attr('id')) || ($('#tab-' + id).find('.idoc-editor').attr('id') === $(this.config.source).attr('id'))) {
      // Subscribe after editor is ready because otherwise if Save is pressed before editor is fully loaded this will override tab's content with empty string
      this.events.push(this.config.eventbus.instance.subscribe(AfterIdocContentModelUpdateEvent, () => {
        let wholeDoc = this.domTree.buildDocument(true);
        this.config.tab.content = wholeDoc.html();
      }));
      this.events.push(this.config.eventbus.instance.subscribe(IdocContentSanitizedEvent, (event) => {
        if (this.config.tab.id === event[0].tabId) {
          this.config.tab.content = event[0].sanitizedContent;
        }
      }));
      this.refresh();
    }
  }

  /**
   * Toggles preview/edit mode, ie enable or disable title dragging
   * @param preview
   */
  setPreviewMode(preview = true) {
    this.config.previewMode = preview;
  }

  /**
   * Parse the dom, build the model and correct the existing one
   */
  refresh() {
    let tempVT = new ToCVirtualTree();
    let dom = new ToCDomManipulator(this.config, tempVT);

    dom.checkHeadingsID();
    dom.buildFromDom();
    this.view.correctChanges(dom.tree);
  }

  destroy() {
    var assembledDocument = this.domTree.buildDocument(false);
    $(this.config.source).replaceWith(assembledDocument);
    this.domTree.removeCollapseContainer();
    this.events.forEach(function (event) {
      event.unsubscribe();
    });
    this.headingHigligther.destroy();
  }

}
