import {View, Component, Inject, NgElement, NgTimeout} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {UrlUtils} from 'common/url-utils';
import {MODE_PRINT} from 'idoc/idoc-constants';

import template from './video-player.html!text';
import './video-player.css!';

@Component({
  selector: 'seip-video-player',
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
export class VideoPlayer {
  constructor($element, authenticationService, instanceRestService, $timeout) {
    this.$element = $element;
    this.$timeout = $timeout;

    //Creates a new audio element to check if the browser (IE/Edge) is compatible with media elements.
    try {
      let audioEl = new Audio();
      audioEl = undefined;
    } catch(err) {
      this.errorMessage = 'media.file.format.not.supported';
      this.fireReadyEvent();
    }

    let src = instanceRestService.getContentDownloadUrl(this.instanceId);
    this.src = UrlUtils.appendQueryParam(src, AuthenticationService.TOKEN_REQUEST_PARAM, authenticationService.getToken());
  }

  ngOnInit() {
    this.videoElement = this.$element.find('video');

    if (this.mode !== MODE_PRINT) {
      this.videoElement.prop('controls', true);
    }

    // Show player as soon as possible
    this.videoElement.on('loadedmetadata', () => {
      this.videoElement.removeClass('hidden');
    });

    this.videoElement.on('loadeddata', () => {
      this.fireReadyEvent();
    });

    this.videoElement.on('error', () => {
      this.errorMessage = 'media.file.format.not.supported';
      this.fireReadyEvent();
    });
  }

  fireReadyEvent() {
    this.$timeout(() => {
      this.onReady();
    });
  }

  ngOnDestroy() {
    if (this.videoElement) {
      this.videoElement.off();
    }
  }
}
