import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {IconsService} from 'services/icons/icons-service';
import {HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {Configuration} from 'common/application-config';
import {SimpleDateToMomentFormat} from 'common/simple-date-to-moment-format';
import {MomentAdapter} from 'adapters/moment-adapter';
import {ModelUtils} from 'models/model-utils';
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
@Inject(NgScope, '$compile', NgElement, Configuration, MomentAdapter)
export class InstanceHeader {
  constructor($scope, $compile, $element, configuration, momentAdapter) {
    this.configuration = configuration;
    this.momentAdapter = momentAdapter;
    this.element = $element;
    this.context.getCurrentObject().then((currentObject) => {
      this.currentObject = currentObject;
      this.flatViewModel = ModelUtils.flatViewModel(currentObject.models.viewModel);

      this.iconSize = IconsService.HEADER_ICON_SIZE[this.headerType];
      $scope.$watch(() => {
        return currentObject.getHeader(this.headerType);
      }, (instanceHeader) => {
        instanceHeader = instanceHeader.replace(/(\r\n|\n|\r)/gm, '');
        // parse HTML string into DOM element
        let htmlHeader = $(instanceHeader);
        htmlHeader.find('[' + InstanceHeader.PROPERTY_ATTR + ']').each((i, element)=> {
          let property = $(element);
          let dataFormat = this.extractDataFormat(property);

          let propertyValue = property.attr(InstanceHeader.PROPERTY_ATTR);

          if (!currentObject.models.validationModel[propertyValue]) {
            return;
          }
          let ngBindString;
          if (dataFormat) {
            ngBindString = `instanceHeader.getPropertyDisplayValue(instanceHeader.currentObject, '${propertyValue}', '${dataFormat}')`;
          } else {
            ngBindString = `instanceHeader.getPropertyDisplayValue(instanceHeader.currentObject, '${propertyValue}')`;
          }
          property.attr('ng-bind-html', ngBindString);
        });
        htmlHeader.find('.instance-link').addBack().addClass(InstanceHeader.DISABLED_LINK).find('.document-version').addClass(InstanceHeader.HIDDEN);
        let persistedWatch = $scope.$watch(()=> {
          return currentObject.isPersisted();
        }, (persisted)=> {
          if (persisted) {
            htmlHeader.find('.instance-link').addBack().removeClass(InstanceHeader.DISABLED_LINK).find('.document-version').removeClass(InstanceHeader.HIDDEN);
            persistedWatch();
          }
        });

        // compile the header
        let compiledHeader = $compile(htmlHeader);
        // link the compiled template with the scope.
        let headerElement = compiledHeader($scope);

        // Clear node and append to DOM
        $element.find('.instance-data').empty().append(headerElement);
        //anytime the header is changed it adds the necessary class
        let headerIcon = this.element.find('.instance-data > span:first-child > img');
        headerIcon.parent().addClass('header-icon');
      });
    });
  }

  getPropertyDisplayValue(currentObject, propertyName, dateFormat) {
    let propertyViewModel = this.flatViewModel.get(propertyName);
    let propertyValidationModel = currentObject.models.validationModel[propertyName];
    let displayValue = '';
    if (currentObject.constructor.isObjectProperty(propertyViewModel)) {
      if (propertyValidationModel.value) {
        propertyValidationModel.value.forEach((value) => {
          if ('' !== displayValue) {
            displayValue += ', ';
          }
          displayValue += value.headers[HEADER_BREADCRUMB];
        });
      }
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
    return displayValue;
  }

  /**
   * Formats given date to user friendly string as defined in the configurations
   * @param isoDate date in ISO string to be formatted
   * @param includeTime if true time will be added to the result
   * @param dateFormat optional format param which will be used for formatting of the date
   * @returns {*} formatted string
   */
  formatDatetime(isoDate, includeTime, dateFormat) {
    if (!isoDate || isoDate === '') {
      return '';
    }
    let date = new Date(isoDate);

    if (dateFormat) {
      return this.momentAdapter.format(date, dateFormat);
    }

    let pattern = this.configuration.get(Configuration.UI_DATE_FORMAT);
    if (includeTime) {
      pattern += ' ' + this.configuration.get(Configuration.UI_TIME_FORMAT);
    }
    return this.momentAdapter.format(date, pattern);
  }

  extractDataFormat(property) {
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

}
InstanceHeader.DISABLED_LINK = 'disabled';
InstanceHeader.HIDDEN = 'hidden';
InstanceHeader.PROPERTY_ATTR = 'data-property';
InstanceHeader.DATA_FORMAT_PROPERTY = 'data-format';
