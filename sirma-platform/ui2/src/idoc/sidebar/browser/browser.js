import {View, Component, Inject, NgScope} from 'app/app';
import {IdocContext} from 'idoc/idoc-context';
import {ObjectBrowserRestService} from 'services/rest/object-browser-service';
import 'components/object-browser/object-browser';
import _ from 'lodash';

import browserTemplate from './browser.html!text';

@Component({
  selector: 'seip-browser',
  properties: {
    'context': 'context'
  }
})
@View({
  template: browserTemplate
})
@Inject(NgScope, ObjectBrowserRestService)
class Browser {
  constructor($scope, objectBrowserRestService) {
    this.config = {};
    this.objectBrowserRestService = objectBrowserRestService;
    this.loader = this.getObjectBrowserLoader();

    this.context.getCurrentObject().then((entity) => {
      $scope.$watch(() => entity.getContextPath(), (nodePath) => {
        if (nodePath) {
          let root = IdocContext.getRootContextWithReadAccessInverted(nodePath);
          if (!root) {
            return;
          }
          this.config = _.merge(this.config, {
            id: entity.id,
            rootId: root.id,
            rootText: root.compactHeader,
            nodePath
          });
        }
      });
    });
  }

  getObjectBrowserLoader() {
    return {
      getNodes: (...args) => this.objectBrowserRestService.getChildNodes(...args)
    };
  }
}