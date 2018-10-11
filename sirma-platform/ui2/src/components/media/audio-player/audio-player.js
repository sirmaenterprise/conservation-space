import {View, Component, Inject, NgElement, NgTimeout} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {UrlUtils} from 'common/url-utils';
import {MODE_PRINT} from 'idoc/idoc-constants';

import template from './audio-player.html!text';
import './audio-player.css!';

@Component({
  selector: 'seip-audio-player',
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
export class AudioPlayer {
  constructor($element, authenticationService, instanceRestService, $timeout) {
    this.$element = $element;
    this.$timeout = $timeout;

    //Creates a new audio element to check if the browser (IE/Edge) is compatible with audio elements.
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
    this.audioElement = this.$element.find('audio');

    if (this.mode !== MODE_PRINT) {
      this.audioElement.prop('controls', true);
    }

    this.audioElement.on('loadedmetadata', () => {
      this.audioElement.removeClass('hidden');
    });

    this.audioElement.on('loadeddata', () => {
      this.fireReadyEvent();
    });

    this.audioElement.on('error', () => {
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
    if (this.audioElement) {
      this.audioElement.off();
    }
  }
}
