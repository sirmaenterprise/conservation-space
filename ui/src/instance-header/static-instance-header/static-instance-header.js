import {View, Component, Inject, NgElement, NgCompile, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {IconsService} from 'services/icons/icons-service';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {ToTrustedHtml} from 'filters/to-trusted-html';
import staticInstanceHeaderTemplate from 'instance-header/static-instance-header/static-instance-header.html!text';
import 'instance-header/static-instance-header/static-instance-header.css!css';

@Component({
  selector: 'seip-static-instance-header',
  properties: {
    'config': 'config',
    'headerType': 'header-type',
    'header': 'header'
  }
})
@View({
  template: staticInstanceHeaderTemplate
})
@Inject(NgElement, NgCompile, NgScope)
export class StaticInstanceHeader extends Configurable {

  constructor($element, $compile, $scope) {
    super({
      disabled: false
    });
    this.$element = $element;
    this.instanceDataElement = this.$element.find('.instance-data');
    this.iconSize = IconsService.HEADER_ICON_SIZE[this.headerType];
    $scope.$watch(() => {
      return this.header;
    }, () => {
      if (this.header) {
        this.clearData();
        this.innerScope = $scope.$new();
        let compiledHeader = $compile(this.header.replace(/(\r\n|\n|\r)/gm, ''))(this.innerScope);
        this.instanceDataElement.append(compiledHeader);
      }
    });
  }

  ngAfterViewInit() {
    let headerIcon = this.$element.find('.instance-data > span:first-child > img');
    headerIcon.parent().addClass('header-icon');
  }

  clearData() {
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    this.instanceDataElement.empty();
  }

  ngOnDestroy() {
    this.clearData();
  }
}