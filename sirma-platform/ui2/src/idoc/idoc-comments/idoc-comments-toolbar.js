import {View, Component, Inject, NgElement} from 'app/app';
import 'services/i18n/translate-service';
import 'idoc/comments/comments-filter/comments-filter';
import 'components/ui-preference/ui-preference';
import commentsToolbarTemplate from 'idoc/idoc-comments/idoc-comments-toolbar.html!text';

@Component({
  selector: 'seip-idoc-comments-toolbar',
  properties: {
    'commentsComponent': 'comments-component',
    'context': 'context'
  }
})
@View({
  template: commentsToolbarTemplate
})
@Inject(NgElement)
export class IdocCommentsToolbar {

  constructor(element) {
    this.filterConfig = this.commentsComponent.config.filtersConfig;
    this.element = element;

    this.uiPreferenceConfig = {
      sourceElements: {
        top: '.idoc-wrapper .fixed-container'
      },
      copyParentWidth: '.idoc-comments-wrapper'
    };
  }

  ngOnDestroy() {
    this.element.remove();
  }
}