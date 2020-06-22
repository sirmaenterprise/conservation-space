import {View, Component, Inject, NgElement, NgTimeout} from 'app/app';
import {AuthenticationService} from 'security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {CONTENT_TYPE} from 'services/rest/http-headers';

import template from './image-viewer.html!text';
import './image-viewer.css!';

@Component({
  selector: 'seip-image-viewer',
  properties: {
    'instanceId': 'instance-id',
    'mode': 'mode'
  },
  events: ['onReady']
})
@View({
  template
})
@Inject(NgElement, AuthenticationService, InstanceRestService, NgTimeout)
export class ImageViewer {
  constructor($element, authenticationService, instanceRestService, $timeout) {
    this.$element = $element;
    this.$timeout = $timeout;

    instanceRestService.preview(this.instanceId).then((response) => {
      let contentTypeHeader = response.headers(CONTENT_TYPE);
      let blobContentType;
      if (contentTypeHeader) {
        blobContentType = {
          type: contentTypeHeader
        };
      }
      let blob = new Blob([response.data], blobContentType);
      let urlCreator = window.URL || window.webkitURL;
      this.src = urlCreator.createObjectURL(blob);
    });
  }

  ngOnInit() {
    this.imageElement = this.$element.find('img');

    this.imageElement.on('load', () => {
      this.imageElement.removeClass('hidden');
      this.fireReadyEvent();
    });

    this.imageElement.on('error', () => {
      this.errorMessage = 'labels.preview.none';
      this.fireReadyEvent();
    });
  }

  fireReadyEvent() {
    this.$timeout(() => {
      this.onReady();
    });
  }

  ngOnDestroy() {
    if (this.imageElement) {
      this.imageElement.off();
    }
  }
}
