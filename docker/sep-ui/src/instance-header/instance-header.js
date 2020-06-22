import {View, Component, Inject, NgScope, NgElement, NgCompile} from 'app/app';
import {IconsService} from 'services/icons/icons-service';
import {InstanceObject} from 'models/instance-object';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {Configuration} from 'common/application-config';
import {SimpleDateToMomentFormat} from 'common/simple-date-to-moment-format';
import {MomentAdapter} from 'adapters/moment-adapter';
import {ModelUtils} from 'models/model-utils';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterIdocSaveEvent} from 'idoc/actions/events/after-idoc-save-event';
import 'components/instance-selector/instance-selector';

import _ from 'lodash';
import 'instance-header/instance-header-tooltip/instance-header-tooltip';
import instanceHeaderTemplate from './instance-header.html!text';
import 'instance-header/instance-header.css!css';

@Component({
  selector: 'seip-instance-header',
  properties: {
    'headerType': 'header-type',
    'context': 'context'
  }
})
@View({
  template: instanceHeaderTemplate
})
@Inject(NgScope, NgElement, Configuration, MomentAdapter, Eventbus, NgCompile)
export class InstanceHeader {

  constructor($scope, $element, configuration, momentAdapter, eventbus, $compile) {
    this.momentAdapter = momentAdapter;
    this.configuration = configuration;
    this.$element = $element;
    this.$compile = $compile;
    this.eventbus = eventbus;
    this.$scope = $scope;
  }

  ngOnInit() {
    this.relationsScopes = {};
    this.headerBoundProperties = {};
    this.datePattern = this.configuration.get(Configuration.UI_DATE_FORMAT);
    this.datetimePattern = `${this.datePattern} ${this.configuration.get(Configuration.UI_TIME_FORMAT)}`;

    this.context.getCurrentObject().then((currentObject) => {
      this.currentObject = currentObject;
      this.iconSize = IconsService.HEADER_ICON_SIZE[this.headerType];
      this.flatViewModel = ModelUtils.flatViewModel(currentObject.models.viewModel);

      this.$scope.$watch(() => {
        return this.currentObject.getHeader(this.headerType);
      }, this.headerChanged.bind(this));

      this.afterIdocSavehandler = this.eventbus.subscribe(AfterIdocSaveEvent, () => {
        InstanceHeader.enableHeaderLinks(this.headerDOM, true);
      });
    });
  }

  headerChanged(instanceHeaderTemplate) {
    if (!instanceHeaderTemplate) {
      return;
    }
    let validationModel = this.currentObject.getModels().validationModel;
    let viewModel = this.currentObject.getModels().viewModel;
    let linearizedHeaderTemplate = instanceHeaderTemplate.replace(/(\r\n|\n|\r)/gm, '');
    this.headerDOM = $(linearizedHeaderTemplate);
    this.headerDOM.find('[' + InstanceHeader.PROPERTY_ATTR + ']').each((i, element)=> {
      let property = $(element);
      let dataFormat = InstanceHeader.extractDataFormat(property);
      let propertyName = property.attr(InstanceHeader.PROPERTY_ATTR);
      let propertyValue = this.currentObject.getModels().validationModel[propertyName];

      if (!propertyValue) {
        return;
      }

      if (!this.headerBoundProperties[propertyName]) {
        let valueChangeHandler = validationModel[propertyName].subscribe('propertyChanged', (evt) => {
          let changedProperty = Object.keys(evt)[0];

          // When a property gets changed the new value should be applied in the header through the binding unless it is
          // an object property. The object properties are rendered using the instance-selector component in preview
          // mode and they auto update themselves.
          // For codelist type properties should be handled valueLable attribute change which is applied async than
          // value.
          if ((changedProperty === 'value' || changedProperty === 'valueLabel') && !InstanceObject.isObjectProperty(viewModel.flatModelMap[propertyName])) {
            this.applyPropertyValue(propertyName);
          }
        });

        this.headerBoundProperties[propertyName] = {
          selector: `[data-property='${propertyName}']`,
          dataFormat,
          valueChangeHandler
        };

        if (InstanceObject.isObjectProperty(viewModel.flatModelMap[propertyName])) {
          this.headerBoundProperties[propertyName].instanceSelectorTemplate = InstanceHeader.getInstanceSelectorTemplate(propertyName);
          this.headerBoundProperties[propertyName].instanceSelectorConfig = InstanceHeader.getInstanceSelectorConfig(propertyName, this.currentObject.getModels().id);
        }
      }

      this.applyPropertyValue(propertyName);
    });

    InstanceHeader.enableHeaderLinks(this.headerDOM, this.currentObject.isPersisted());

    let compiledHeader = this.compileHeader(this.headerDOM);
    this.$element.find('.instance-data').empty().append(compiledHeader);
    //anytime the header is changed it adds the necessary class
    let headerIcon = this.$element.find('.instance-data > span:first-child > img');
    headerIcon.parent().addClass('header-icon');
  }

  compileHeader(headerDOM) {
    if (this.headerScope) {
      this.headerScope.$destroy();
    }
    this.headerScope = this.$scope.$new();
    return this.$compile(headerDOM)(this.headerScope);
  }

  static enableHeaderLinks(headerDOM, enable) {
    if (headerDOM) {
      let toggleFunction = enable ? 'removeClass' : 'addClass';
      headerDOM.find('.instance-link').addBack()[toggleFunction](InstanceHeader.DISABLED_LINK).find('.document-version')[toggleFunction](InstanceHeader.HIDDEN);
    }
  }

  applyPropertyValue(propertyName) {
    if (this.headerBoundProperties[propertyName]) {
      let dataFormat = this.headerBoundProperties[propertyName].dataFormat;

      let displayValue = this.getPropertyDisplayValue(this.currentObject, propertyName, dataFormat);

      // TODO: A note for possible optimization. I tried to cache the jquery elements for the bound properties
      // in headerChanged handler and to simply execute property.empty().append(...) here but for some reason the
      // elements seem to be detached (not live) and this didn't worked. Probably there is a way to make it work though.

      // It's possible multiple elements with [data-property] attributes with equal values to exist inside the idoc
      // header (the default header) because object properties could be bound in the header and they are represented
      // with their headers, which in turn could have a [data-property] with a value as one in the default header. This
      // collision is resolved by checking the number of matched [data-property] elements and if more than one is found,
      // then find the one which doesn't have a parent element with attribute [data-property].
      let selector = this.headerBoundProperties[propertyName].selector;
      let selection = this.headerDOM.find(selector);
      if (selection.length > 1) {
        selection.each((index, element) => {
          let currentElement = $(element);
          // Getting the parent node first in order to exclude the currentElement from the check. The
          // $.parents('[data-property]') is not used because it traverses the dom upwards to the provided root and then
          // filters all found ancestors. The $.closest(selector, context) function itself matches on every turn and
          // stops when a match is found.
          let isNestedProperty = currentElement.parent().closest('[data-property]', this.headerDOM).length > 0;
          if (!isNestedProperty) {
            selection = currentElement;
            return false;
          }
        });
      }

      selection.empty().append(displayValue);
    }
  }

  getPropertyDisplayValue(currentObject, propertyName, dateFormat) {
    let propertyViewModel = this.flatViewModel.get(propertyName);
    let propertyValidationModel = currentObject.models.validationModel[propertyName];
    let displayValue = '';
    let isObjectProperty = false;
    if (InstanceObject.isObjectProperty(propertyViewModel)) {
      isObjectProperty = true;
      displayValue = this.headerBoundProperties[propertyName].instanceSelectorTemplate;
    } else if (propertyViewModel.dataType === 'datetime') {
      displayValue = this.formatDatetime(propertyValidationModel.value, true, dateFormat);
    } else if (propertyViewModel.dataType === 'date') {
      displayValue = this.formatDatetime(propertyValidationModel.value, false, dateFormat);
    } else if (propertyValidationModel.valueLabel) {
      displayValue = propertyValidationModel.valueLabel;
    } else if (_.isObject(propertyValidationModel.value)) {
      displayValue = propertyValidationModel.value.label;
    } else {
      displayValue = propertyValidationModel.value;
    }

    if (!isObjectProperty) {
      displayValue = _.escape(displayValue);
    }

    return displayValue;
  }

  static getInstanceSelectorTemplate(propertyName) {
    return `<seip-instance-selector ng-if="instanceHeader.currentObject.models.validationModel['${propertyName}']" instance-model-property="instanceHeader.currentObject.models.validationModel['${propertyName}']" config="instanceHeader.headerBoundProperties['${propertyName}'].instanceSelectorConfig" class="inline"></seip-instance-selector>`;
  }

  static getInstanceSelectorConfig(propertyName, instanceId) {
    return {
      propertyName,
      mode: MODE_PREVIEW,
      objectId: instanceId,
      selection: MULTIPLE_SELECTION,
      instanceHeaderType: HEADER_BREADCRUMB
    };
  }

  /**
   * Formats given date to user friendly string as defined in the configurations
   *
   * @param isoDate date in ISO string to be formatted
   * @param includeTime if true time will be added to the result
   * @param dateFormat optional format param which will be used for formatting of the date
   *
   * @returns {*} formatted string
   */
  formatDatetime(isoDate, includeTime, dateFormat) {
    if (!isoDate) {
      return '';
    }
    let date = new Date(isoDate);

    if (dateFormat) {
      return this.momentAdapter.format(date, dateFormat);
    }

    let pattern = includeTime ? this.datetimePattern : this.datePattern;
    return this.momentAdapter.format(date, pattern);
  }

  static extractDataFormat(property) {
    // take the data-format if present in inner element.
    let elementWithDataFormat = property.find('[' + InstanceHeader.DATA_FORMAT_PROPERTY + ']').eq(0);
    // in case the attribute is not stored in an inner span.
    let dataFormat = property.attr(InstanceHeader.DATA_FORMAT_PROPERTY);
    if (elementWithDataFormat.length > 0) {
      dataFormat = elementWithDataFormat.attr(InstanceHeader.DATA_FORMAT_PROPERTY);
    }

    if (dataFormat) {
      let formattedDataFormat = SimpleDateToMomentFormat.convertToMomentFormat(dataFormat);
      property.attr(InstanceHeader.DATA_FORMAT_PROPERTY, formattedDataFormat);
      return formattedDataFormat;
    }
    return null;
  }

  ngOnDestroy() {
    Object.keys(this.headerBoundProperties).forEach((propertyName) => {
      this.headerBoundProperties[propertyName].valueChangeHandler.unsubscribe();
    });
    this.afterIdocSavehandler.unsubscribe();
  }
}

InstanceHeader.DISABLED_LINK = 'disabled';
InstanceHeader.HIDDEN = 'hidden';
InstanceHeader.PROPERTY_ATTR = 'data-property';
InstanceHeader.DATA_FORMAT_PROPERTY = 'data-format';
