import {Component, View, Inject, NgScope} from 'app/app';
import {InstanceRestService} from 'services/rest/instance-service';
import {Configuration} from 'common/application-config';
import {MomentAdapter} from 'adapters/moment-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {EMF_VERSION} from 'instance/instance-properties';
import {CONTENT_ID} from 'instance/instance-properties';

import FileSaver from 'file-saver';

import 'search/components/common/pagination';
import 'components/help/contextual-help';
import 'instance-header/static-instance-header/static-instance-header';

import './versions.css!css';
import versionsTemplate from 'idoc/system-tabs/versions/versions.html!text';

const NOT_PROCESSED_VERSION = 'versions.not.processed';
const NOT_COMPARABLE_TOOLTIP = 'versions.not.comparable';
const INCOMPARABLE_MIME_TYPES = ['image', 'video', 'audio', 'executable', 'octet-stream'];

@Component({
  selector: 'seip-versions',
  properties: {
    'context': 'context'
  }
})
@View({
  template: versionsTemplate
})
@Inject(InstanceRestService, Configuration, MomentAdapter, TranslateService, NgScope, NotificationService)
export class Versions {
  constructor(instanceRestService, configuration, momentAdapter, translateService, $scope, notificationService) {
    this.momentAdapter = momentAdapter;
    this.translateService = translateService;
    this.datePattern = configuration.get(Configuration.UI_DATE_FORMAT) + ' ' + configuration.get(Configuration.UI_TIME_FORMAT);
    this.pageSize = configuration.get('search.result.pager.pagesize');
    this.instanceRestService = instanceRestService;
    this.notificationService = notificationService;
    this.notComparableTooltip = this.translateService.translateInstant(NOT_COMPARABLE_TOOLTIP);
    this.selectedVersions = [];
    this.comparisonStarted = false;
    this.$scope = $scope;
  }

  ngOnInit() {
    this.checkIfComparable().then((comparable)=> {
      this.comparable = comparable;
      this.loadVersions(0, this.pageSize).then((response) => {
        this.paginationConfig = {
          total: response.data.versionsCount,
          showFirstLastButtons: true,
          page: 1,
          pageSize: this.pageSize,
          pageRotationStep: 2
        };
      });
    });

    this.paginationCallback = (pageNumber) => {
      this.onPageChanged(pageNumber);
    };
  }

  disableCompareButton() {
    return this.comparisonStarted || !this.isComparisonEnabled() || !this.comparable;
  }

  isComparisonEnabled() {
    return this.selectedVersions.length === 2;
  }

  //Can't compare images, videos, audios or instances without preview
  checkIfComparable() {
    return this.context.getCurrentObject().then((object)=> {
      return !!(this.isComparableMimetype(object.instanceType) && object.models.validationModel[CONTENT_ID] && object.models.validationModel[CONTENT_ID].defaultValue);
    }
    );
  }

  isComparableMimetype(instanceType) {
    for (let mimetype of INCOMPARABLE_MIME_TYPES) {
      if (instanceType.indexOf(mimetype) !== -1) {
        return false;
      }
    }
    return true;
  }

  onPageChanged(params) {
    let offset = (params.pageNumber - 1) * this.pageSize;
    this.loadVersions(offset, this.pageSize).then((response) => {
      this.paginationConfig.total = response.data.versionsCount;
    });
  }

  loadVersions(offset, pageSize) {
    return this.instanceRestService.getVersions(this.context.getCurrentObjectId(), offset, pageSize).then((response) => {
      this.versions = response.data.versions;
      this.populateVersionsConfiguration();
      this.loadModifiedByHeader();
      return response;
    });
  }

  populateVersionsConfiguration() {
    this.versions.forEach((version) => {
      version.config = this.createVersionConfig(version);
      if (this.comparable && this.isComparableMimetype(version.properties.primaryContentMimetype)) {
        version.config.comparable = true;
      } else {
        version.config.notComparableTooltip = this.notComparableTooltip;
      }
    });
  }

  loadModifiedByHeader() {
    let versionsModifiedByInfo = new Map();
    this.versions.forEach(version => Versions.addToVersionModifiedByInfo(versionsModifiedByInfo, version));
    return this.instanceRestService.loadBatch([...versionsModifiedByInfo.keys()], {params: {properties: [HEADER_COMPACT]}}).then(({'data':result}) => {
      for (const {id, headers: {compact_header}} of result) {
        let versions = versionsModifiedByInfo.get(id);
        if (versions) {
          versions.forEach(version => version.versionModifiedByHeader = compact_header);
        }
      }
    });
  }

  static addToVersionModifiedByInfo(versionsModifiedByInfo, version) {
    let versionModifierId = version.properties.modifiedBy.results[0];
    let modifiedVersions = versionsModifiedByInfo.get(versionModifierId);
    if (modifiedVersions) {
      modifiedVersions.push(version);
    } else {
      versionsModifiedByInfo.set(versionModifierId,[version]);
    }
  }

  checkDisabled(version) {
    //If not comparable the checkboxes are always disabled
    //or if comparison is enabled and checkbox for version is not selected.
    //If comparison is enabled this mean we have two checkbox version selected so all others checkboxes
    //have to be disabled.

    return !version.config.comparable || this.isComparisonEnabled() && !version.selected;
  }

  compareVersions() {
    this.comparisonStarted = true;
    let id = this.context.getCurrentObjectId();
    let first = this.selectedVersions[0];
    let second = this.selectedVersions[1];
    return this.instanceRestService.compareVersions(id, first.id, second.id).then((response) => {

      let blob = new Blob([response.data], {
        type: 'application/octet-stream'
      });

      FileSaver.saveAs(blob, `${id}-${first.properties[EMF_VERSION]}-vs-${second.properties[EMF_VERSION]}.pdf`);
      this.comparisonStarted = false;
    }).catch(()=> {
      this.comparisonStarted = false;
      this.notificationService.remove();
      this.notificationService.warning(this.translateService.translateInstant('versions.compare.error'));
    });
  }

  handleSelection(newVersion) {
    if (newVersion.selected) {
      //if checkbox was selected remove it form selected versions.
      if (this.selectedVersions[0].id === newVersion.id) {
        this.selectedVersions.shift();
      } else {
        this.selectedVersions.pop();
      }
    } else {
      //if checkbox was not selected add it to selected versions.
      this.selectedVersions.push(newVersion);
    }

    //Reverse selection of checkbox
    newVersion.selected = !newVersion.selected;
  }

  createVersionConfig(version) {
    let config = {
      disabled: !version.properties.hasViewContent
    };
    config.tooltip = config.disabled ? this.translateService.translateInstant(NOT_PROCESSED_VERSION) : '';
    return config;
  }

  getCreatedOn(version) {
    return this.momentAdapter.format(new Date(version.properties.modifiedOn), this.datePattern);
  }
}
