import {Component, View, Inject} from 'app/app';
import {InstanceRestService} from 'services/rest/instance-service';
import {StaticInstanceHeader} from 'instance-header/static-instance-header/static-instance-header';
import {Configuration} from 'common/application-config';
import {MomentAdapter} from 'adapters/moment-adapter';
import {Pagination} from 'search/components/common/pagination';
import {ContextualHelp} from 'components/help/contextual-help';
import {TranslateService} from 'services/i18n/translate-service';
import './versions.css!css';
import versionsTemplate from 'idoc/system-tabs/versions/versions.html!text';

const NOT_PROCESSED_VERSION = 'versions.not.processed';

@Component({
  selector: 'seip-versions',
  properties: {
    'context': 'context'
  }
})
@View({
  template: versionsTemplate
})
@Inject(InstanceRestService, Configuration, MomentAdapter, TranslateService)
export class Versions {
  constructor(instanceRestService, configuration, momentAdapter, translateService) {
    this.momentAdapter = momentAdapter;
    this.translateService = translateService;
    this.datePattern = configuration.get(Configuration.UI_DATE_FORMAT) + ' ' + configuration.get(Configuration.UI_TIME_FORMAT);
    this.pageSize = configuration.get('search.result.pager.pagesize');
    this.instanceRestService = instanceRestService;
    this.loadVersions(0, this.pageSize).then((response) => {
      this.paginationConfig = {
        total: response.data.versionsCount,
        showFirstLastButtons: true,
        page: 1,
        pageSize: this.pageSize,
        pageRotationStep: 2
      };
    });

    this.paginationCallback = (pageNumber) => {
      this.onPageChanged(pageNumber);
    };
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
      this.versions.forEach((version)=> {
        version.config = this.craeteVersionConfig(version);
      });
      return response;
    });
  }

  craeteVersionConfig(version) {
    let config = {
      disabled: !version.properties.hasViewContent
    };
    if (config.disabled) {
      config.tooltip = this.translateService.translateInstant(NOT_PROCESSED_VERSION);
    }else{
      config.tooltip = '';
    }
    return config;
  }

  getCreatedOn(version) {
    return this.momentAdapter.format(new Date(version.properties.modifiedOn), this.datePattern);
  }
}
