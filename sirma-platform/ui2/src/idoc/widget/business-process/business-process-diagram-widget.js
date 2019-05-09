import {View, Inject, NgElement} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {BpmService} from 'services/rest/bpm-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {DialogService} from 'components/dialog/dialog-service';
import {BpmnFullscreenDialog} from 'idoc/widget/business-process/fullscreen/bpmn-fullscreen-dialog';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceUtils} from 'instance/utils';
import {StatusCodes} from 'services/rest/status-codes';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import BpmnJS from 'bpmn-js';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import {DEFINITION_ID} from 'instance/instance-properties';
import template from './business-process-diagram-widget.html!text';
import './business-process-diagram-widget.css!';

@Widget
@View({template})
@Inject(BpmService, InstanceRestService, DialogService, ObjectSelectorHelper, Eventbus, PromiseAdapter, NgElement, ResizeDetectorAdapter)
export class BusinessProcessDiagramWidget {

  constructor(bpmService, instanceRestService, dialogService, objectSelectorHelper, eventbus, promiseAdapter, $element, resizeDetectorAdapter) {
    this.bpmService = bpmService;
    this.instanceRestService = instanceRestService;
    this.dialogService = dialogService;
    this.objectSelectorHelper = objectSelectorHelper;
    this.eventbus = eventbus;
    this.$element = $element;
    this.promiseAdapter = promiseAdapter;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    this.control.onConfigConfirmed = this.onConfigConfirmed.bind(this);
  }

  /**
   * Generates a BPMN viewer.
   *
   * @return the viewer instance.
   */
  createViewer() {
    if (this.viewer) {
      return this.viewer;
    }
    // creates the viewer selector based on the widget id.
    this.diagramContainer = '.diagram-' + this.control.getId();
    // width must be set, otherwise IE and Edge do not instantiate BpmnJS
    this.viewer = new BpmnJS({
      container: this.diagramContainer, width: 'inherit'
    });
    this.addPrintListeners(this.viewer);
    return this.viewer;
  }

  /**
   * Inspired by http://stackoverflow.com/questions/1234008/detecting-browser-print-event/11060206
   *
   * Adds a special listener to enable proper printing when used ctrl+p or print not export.
   * This triggers a new resize event before the print preview is present so that the diagram is scaled down properly in the PDF.
   *
   * @param viewer the content viewer
   */
  addPrintListeners(viewer) {
    this.mediaQueryList = window.matchMedia('print');
    this.mediaQueryListScreen = window.matchMedia('screen');
    this.mqlListener = (mql) => {
      if (mql.matches && viewer) {
        let canvas = viewer.get('canvas');
        canvas.zoom('fit-viewport');
        canvas.resized();
      }
    };
    this.mediaQueryList.addListener(this.mqlListener);
    this.mediaQueryListScreen.addListener(this.mqlListener);
  }

  ngAfterViewInit() {
    this.initDiagram();
    this.applyCustomStyles();
  }

  onConfigConfirmed(config) {
    this.config = config;
    // Reset message.
    delete this.message;
    this.initDiagram();
  }

  initDiagram() {
    if (this.widgetShouldBeEmpty()) {
      this.clearDiagram();
      delete this.message;
      this.publishWidgetReadyEvent();
      return;
    }
    this.objectSelectorHelper.getSelectedObject(this.config, this.context).then((selectedObject) => {
      this.workflowID = selectedObject;
      if (InstanceUtils.isTempId(this.workflowID)) {
        this.setMessage('widgets.process.not.started');
        this.clearDiagram();
      } else if (this.workflowID) {
        this.setDiagramContents();
      } else {
        this.setMessage('widgets.process.empty');
        this.clearDiagram();
      }
    }, (rejection) => {
      if (rejection.noSelection || rejection.noResults) {
        this.setMessage('widgets.process.empty');
      } else if (rejection.reason) {
        this.setMessage(rejection.reason);
      } else {
        this.setMessage('widgets.process.select.single.value');
      }
      this.clearDiagram();
      delete this.workflowID;
    });
  }

  setDiagramContents() {
    this.createViewer();
    this.context.getCurrentObject().then((instance) => {
      if (instance.isVersion()) {
        this.loadHistoricVersion();
      } else {
        this.instanceRestService.load(this.workflowID).then((response) => {
          this.retrieveWorkflow(response);
        });
      }
    });
  }

  /**
   * Method for handling the service responses and extracting their data.
   *
   * @param diagram the response from the diagram rest.
   *
   * @param activity the response from the activity rest.
   */
  processResponse(diagram, activity) {
    this.config.bpmn = diagram.data.id;
    this.bpmn = diagram.data.bpmn20Xml;
    this.config.activity = activity ? activity.data : undefined;
    this.control.baseWidget.saveConfigWithoutReload(this.config);
    this.loadDiagram(diagram.data.bpmn20Xml, this.config.activity);
  }

  /**
   * Loads the diagram into the viewer object.
   *
   * @param xml the XML data that is needed in the viewer to display the diagram.
   *
   * @param activity the current activity if there is one present.
   */
  loadDiagram(xml, activity) {
    this.viewer.importXML(xml, () => {
      let canvas = this.viewer.get('canvas');
      if (activity) {
        activity.childActivityInstances.forEach((element) => {
          let id = element.activityId.replace('#multiInstanceBody', '');
          canvas.addMarker(id, 'highlight');
        });
      }
      if (this.$element.find(this.diagramContainer)[0]) {
        if (this.resizeListener) {
          this.resizeListener();
        }
        this.resizeListener = this.resizeDetectorAdapter.addResizeListener(this.$element.find(this.diagramContainer)[0], this.resizeCallback);
      }
      //Resize the diagram in case there was previously a smaller diagram.
      canvas.zoom('fit-viewport');
      canvas.resized();
      //The diagram is loaded and can be interacted with.
      this.publishWidgetReadyEvent();
    });
  }

  /**
   * Loads the historical version of the process by requesting it from the engine, and then loading the diagram.
   */
  loadHistoricVersion() {
    this.bpmService.getEngine().then((response) => {
      let historicURL = this.bpmService.generateVersionXmlUrl(response.data, this.config.bpmn);
      this.bpmService.executeCustomProcessRequestGet(historicURL).then((diagram) => {
        this.bpmn = diagram.data.bpmn20Xml;
        this.loadDiagram(diagram.data.bpmn20Xml, this.config.activity);
      });
    });
  }

  /**
   * Applies resizeCallback and adds the current container to the resize listener, so that the diagram may be updated when user,
   * changes some aspects of the ui.
   */
  applyCustomStyles() {
    this.resizeCallback = this.resiseHandler();
  }

  /**
   * Creates a special callback that is used for scaling the diagram.
   *
   * @return a function that styles the widget in preview and edit mode.
   */
  resiseHandler() {
    return _.debounce(() => {
      if (this.viewer) {
        let canvas = this.viewer.get('canvas');
        canvas.resized();
        canvas.zoom('fit-viewport');
        canvas.resized();
      }
    }, 250);
  }

  clearDiagram() {
    if (this.viewer) {
      this.viewer.destroy();
      delete this.viewer;
    }
  }

  /**
   * Configures the widget in fullscreen mode.
   */
  openInFullscreenMode() {
    let buttons = [{
      id: DialogService.CLOSE,
      label: 'dialog.button.cancel'
    }];

    let dialogConfig = {
      modalCls: 'seip-process-fullscreen-container',
      header: 'process.title',
      largeModal: true,
      customStyles: 'fullscreen',
      buttons,
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        this.handleButtonClickedEvent(buttonId, componentScope, dialogConfig);
      }
    };

    let properties = {
      // Add XML data and other properties.
      config: {
        bpmn: this.bpmn,
        activity: this.config.activity
      }
    };

    this.dialogService.create(BpmnFullscreenDialog, properties, dialogConfig);
  }

  /**
   * Handles fullscreen window button events.
   *
   * @param buttonId the id of the button that was pressed
   *
   * @param componentScope the scope of the component
   *
   * @param dialogConfig the config that was passed to the current dialog
   */
  handleButtonClickedEvent(buttonId, componentScope, dialogConfig) {
    //No need to check we only have one button.
    dialogConfig.dismiss();
  }

  /**
   * Publishes a widget ready event to notify that the widget is ready for printing.
   */
  publishWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  retrieveWorkflow(instance) {
    this.promiseAdapter.all([this.bpmService.getEngine(), this.bpmService.getInfo(this.workflowID)]).then((values) => {
      // http://stackoverflow.com/questions/31166178/retrieving-a-process-instance-diagram-in-camunda
      let urlXML;
      let urlActivity;
      if (values[1].status === StatusCodes.NO_CONTENT) {
        // property emf:definitionId will be missing if instance is not saved, so use definitionId from the model.
        urlXML = this.bpmService.generateKeyXmlURL(values[0].data, instance.data[DEFINITION_ID] );
      } else if (!!values[1].data.active) {
        urlXML = this.bpmService.generateProcessInstanceURL(values[0].data, values[1].data.process.properties['activityId']);
        urlActivity = this.bpmService.generateActivityURL(values[0].data, values[1].data.process.properties['activityId']);
      } else {
        urlXML = this.bpmService.generateProcessInstanceHistoryURL(values[0].data, values[1].data.process.properties['activityId']);
      }

      this.bpmService.executeCustomProcessRequestGet(urlXML, {
        'skipInterceptor': true
      }).then((response) => {
        if (response.data.bpmn20Xml) {
          this.getActivityDetailsForDiagram(urlActivity, response);
        } else {
          this.getDiagram(urlActivity, response, values[0].data);
        }
      }, () => {
        this.setMessage('widgets.process.select.proper.value');
      });
    });
  }

  /**
   * Extracts the specific version of the WF diagram from the process engine.
   *
   * @param urlActivity the url for getting the list of activities.
   *
   * @param wfData the data of the workflow.
   *
   * @param engineName the name of the engine.
   */
  getDiagram(urlActivity, wfData, engineName) {
    let urlXML = this.bpmService.generateXmlURL(engineName, wfData.data.definitionId ? wfData.data.definitionId : wfData.data.processDefinitionId);
    this.bpmService.executeCustomProcessRequestGet(urlXML, {
      'skipInterceptor': true
    }).then((response) => {
      this.getActivityDetailsForDiagram(urlActivity, response);
    }, () => {
      this.setMessage('widgets.process.select.proper.value');
    });
  }

  /**
   * Gets the current activity of the workflow if any.
   *
   * @urlActivity the request path for the activity details.
   *
   * @diagramData the data of the diagram.
   */
  getActivityDetailsForDiagram(urlActivity, diagramData) {
    // We skip the interceptor because the workflow process may be over or not started and no current activity can be displayed.
    if (urlActivity) {
      this.bpmService.executeCustomProcessRequestGet(urlActivity, {
        'skipInterceptor': true
      }).then((activity) => {
        this.processResponse(diagramData, activity);
      }, () => {
        this.processResponse(diagramData, undefined);
      });
    } else {
      this.processResponse(diagramData, undefined);
    }
  }

  setMessage(message) {
    this.message = message;
    this.publishWidgetReadyEvent();
  }

  widgetShouldBeEmpty() {
    return this.context.isModeling() && (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY || this.config.selectObjectMode === SELECT_OBJECT_CURRENT);
  }

  ngOnDestroy() {
    // properly destroy BpmnJS to prevent memory leaks
    if (this.viewer) {
      this.viewer.destroy();
    }

    if (this.resizeListener) {
      this.resizeListener();
    }
    if (this.mediaQueryList) {
      this.mediaQueryList.removeListener(this.mqlListener);
    }
    if (this.mediaQueryListScreen) {
      this.mediaQueryListScreen.removeListener(this.mqlListener);
    }
  }
}
