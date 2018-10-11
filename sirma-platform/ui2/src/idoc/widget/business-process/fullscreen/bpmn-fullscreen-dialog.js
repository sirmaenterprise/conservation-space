import {Inject, Component, View, NgScope, NgElement, NgInterval} from 'app/app';
import BpmnJS from 'bpmn-js';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import _ from 'lodash';
import template from './bpmn-fullscreen-dialog.html!text';
import './bpmn-fullscreen-dialog.css!';

@View({
  template: template
})
@Component({
  selector: 'seip-process-fullscreen-container',
  properties: {
    'config': 'config'
  }
})
@Inject(NgScope, NgElement, ResizeDetectorAdapter)
export class BpmnFullscreenDialog {

  constructor($scope, $element, resizeDetectorAdapter) {
    this.bpmnDiagramContainer = '.bpmn-diagram-container';
    this.$scope = $scope;
    this.$element = $element;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    this.debouncedDiagramZoom = _.debounce(() => {
      if (this.viewer) {
        let canvas = this.viewer.get('canvas');
        canvas.resized();
        canvas.zoom('fit-viewport');
        canvas.resized();
      }
    }, 250);
  }

  ngAfterViewInit() {
    this.viewer = this.createViewer();
    this.updateDiagram();
  }

  /**
   * Generates a BPMN viewer.
   *
   * @return the new viewer instance.
   */
  createViewer() {
    // width must be set, otherwise IE and Edge do not instantiate BpmnJS
    return new BpmnJS({
      container: this.bpmnDiagramContainer, width: 'inherit'
    });
  }

  /**
   * Updates the viewer diagram with new XML data, and recalculates elements positioning.
   */
  updateDiagram() {
    this.viewer.importXML(this.config.bpmn, () => {
      let canvas = this.viewer.get('canvas');
      if (this.config.activity) {
        this.config.activity.childActivityInstances.forEach((element) => {
          canvas.addMarker(element.activityId, 'highlight');
        });
      }
      
      this.resizeListener = this.resizeDetectorAdapter.addResizeListener(this.$element[0], this.debouncedDiagramZoom);
    });
  }

  ngOnDestroy() {
    // properly destroy BpmnJS to prevent memory leaks
    if (this.viewer) {
      this.viewer.destroy();
    }
    if (this.resizeListener) {
      this.resizeListener();
    }
    this.$element.remove();
    this.$element = undefined;
  }
}