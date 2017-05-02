import $ from 'jquery';
import {ToCVirtualTreeView} from './idoc-toc-virtual-tree-view';
import {ToCVirtualTree} from './idoc-toc-virtual-tree';
import {ToCDomManipulator} from './idoc-toc-dom-manipulator';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import './idoc-toc.css!css';
import _ from 'lodash';

export class IdocToC {

  constructor(config) {
    this.config = config;

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
      if (event.id === $(this.config.source).attr('id')) {

        // Subscribe after editor is ready because otherwise if Save is pressed before editor is fully loaded this will override tab's content with empty string
        this.events.push(eventbus.subscribe(AfterIdocContentModelUpdateEvent, () => {
          var wholeDoc = this.domTree.buildDocument(true);
          this.config.tab.content = wholeDoc.html();
        }));

        this.refresh();
      }
    }));

    // We don't need to rebuild the toc so often.
    let _slowRefresh = _.debounce(this.refresh.bind(this), 500);
    this.events.push(eventbus.subscribe({
      channel: channel, topic: 'idoc:editor:content-changed', callback: () => {
        _slowRefresh();
      }
    }));

    this.events.push(eventbus.subscribe({
      channel: channel, topic: 'idoc:toc:section-deleted', callback: (data) => {
        this.domTree.deleteSection(data.sectionID);
      }
    }));

    this.events.push(eventbus.subscribe({
      channel: channel, topic: 'idoc:toc:section-moved', callback: (data) => {
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
      channel: channel, topic: 'idoc:editor:heading-collapsed-expanded', callback: function (data) {
        _collapseExpandSection(data.sectionID);
      }
    }));

    this.events.push(eventbus.subscribe({
      channel: channel, topic: 'idoc:toc:heading-collapsed-expanded', callback: () => {
        this.refresh();
      }
    }));

    /**
     * Slows refresh function in case the user abuses the document with many continues 'enters'.
     * Sets the execution at the beginning for better user experience.
     */
    let _mediumRefresh = _.debounce(this.refresh.bind(this), 200, {'leading': true, 'trailing': false});
    this.events.push(eventbus.subscribe({
      channel: channel, topic: 'idoc:editor:selection-changed', callback: function () {
        _mediumRefresh();
      }
    }));

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
  }

}