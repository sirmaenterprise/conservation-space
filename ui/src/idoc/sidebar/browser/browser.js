import {View,Component,Inject,NgScope} from 'app/app';
import browserTemplate from './browser.html!text';
import 'components/object-browser/object-browser';
import {Eventbus} from 'services/eventbus/eventbus';

@Component({
  selector: 'seip-browser',
  properties: {
    'context': 'context'
  }
})
@View({
  template: browserTemplate
})
@Inject(NgScope)
class Browser {
  constructor($scope) {
    this.config = {};

    this.context.getCurrentObject().then((entity) => {
      $scope.$watch(() => entity.getContextPath(), (nodePath) => {
        if (nodePath) {
          var root = nodePath[0];

          this.config = {
            id: entity.id,
            rootId: root.id,
            rootText: root.compactHeader,
            nodePath: nodePath
          };
        }
      });
    });
  }
}