import {View, Inject, NgElement, NgScope, NgCompile} from "app/app";
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {StatusCodes} from 'services/rest/status-codes';
import {Widget} from "idoc/widget/widget";
import {InstanceRestService} from "services/rest/instance-service";
import {StaticInstanceHeader} from 'instance-header/static-instance-header/static-instance-header';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import 'idoc/widget/object-link-widget/object-link-widget.css!';
import template from "./object-link-widget.html!text";

const PROPERTY_IS_DELETE = 'emf:isDeleted';
const NO_PERMISSIONS = 'error.object.forbidden';

@Widget
@View({
  template
})
@Inject(InstanceRestService, TranslateService, Eventbus, NgElement, NgScope, NgCompile)
export class ObjectLinkWidget {

  constructor(instanceRestService, translateService, eventbus, $element, $scope, $compile) {
    this.$element = $element;
    this.$scope = $scope;
    this.$compile = $compile;
    this.eventbus = eventbus;
    this.translateService = translateService;
    this.instanceRestService = instanceRestService;

    var loadInstanceConfig = {
      skipInterceptor: true,
      params: {
        deleted: true,
        properties: [HEADER_COMPACT, PROPERTY_IS_DELETE]
      }
    };

    this.instanceRestService.load(this.config.selectedObject, loadInstanceConfig).then((response)=> {
      var instance = response.data;
      this.insertLink(instance.headers.compact_header, {
        disabled: instance.properties[PROPERTY_IS_DELETE]
      });
    }).catch((error) => {
      if (error && error.status === StatusCodes.FORBIDDEN) {
        this.insertLink(this.getNoPermissionLink());
      }
    });
  }

  insertLink(header, config) {
    this.header = header;
    this.headerConfig = config || {};

    var widget = this.$compile(this.getObjectLinkWidgetTemplate())(this.$scope.$new())[0];
    this.$element.append(widget);
    this.triggerWidgetReadyEvent();
  }

  triggerWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  getNoPermissionLink() {
    return '<a href="javascript:void(0)">' + this.translateService.translateInstant(NO_PERMISSIONS) + '</a>';
  }

  getObjectLinkWidgetTemplate() {
    return '<seip-static-instance-header header-type="\'' + HEADER_COMPACT + '\'" ' +
      'header="objectLinkWidget.header" config="objectLinkWidget.headerConfig"></seip-static-instance-header>';
  }
}