import {Inject, Component, View, NgScope, NgElement, NgInterval} from 'app/app';
import BpmnJS from 'bpmn-js';
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
@Inject(NgScope, NgElement, NgInterval)
export class BpmnFullscreenDialog {

  constructor($scope, $element, $interval) {
    this.bpmnDiagramContainer = '.bpmn-diagram-container';
    this.$scope = $scope;
    this.$interval = $interval;
    this.$element= $element;
    let interval = this.$interval(() => {
      if(this.$element.find(this.bpmnDiagramContainer) && this.viewer === undefined) {
        this.viewer = this.createViewer();
        this.updateDiagram();
        this.$interval.cancel(interval);
      }
    }, 1500, 5);
  }

  /**
   * Generates a BPMN viewer.
   *
   * @return the new viewer instance.
   */
  createViewer() {
    return new BpmnJS({
      container: this.bpmnDiagramContainer
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
      canvas.zoom('fit-viewport');
      canvas.resized();
    });
  }
}