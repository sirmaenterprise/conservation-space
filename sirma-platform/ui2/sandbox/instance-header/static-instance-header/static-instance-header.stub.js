import {Component, View} from 'app/app';
import 'instance-header/static-instance-header/static-instance-header';
import thumbnailData from 'sandbox/instance-header/static-instance-header/thumbnail.data.json!';
import staticInstanceHeaderTemplateStub from 'static-instance-header-stub-template!text';

@Component({
  selector: 'static-instance-header-stub'
})
@View({
  template: staticInstanceHeaderTemplateStub
})
class StaticInstanceHeaderStub {
  constructor() {
    this.iconHeader = {
      headerType: 'default_header',
      header: '<span data-property="type"></span><span data-property="title">Title</span><span data-property="status"></span>'
    };
    this.thumbnailHeader = {
      headerType: 'default_header',
      header: '<span data-property="type"></span><span data-property="title">Title 2</span><span data-property="status"></span>'
    };

    this.disabledHeader = {
      headerType: 'default_header',
      header: '<span data-property="type"></span><span data-property="title"><a href="#" class="instance-link">Disabled header</a></span><span data-property="status"></span>',
      config: {
        disabled: true
      }
    };
  }
}