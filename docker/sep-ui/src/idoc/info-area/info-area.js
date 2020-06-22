import {View,Component,Inject,NgScope} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import uuid from 'common/uuid';
import infoAreaTemplate from './info-area.html!text';
import './info-area.css!css';

export const TOPIC = 'info-area';

@Component({
  selector: 'seip-info-area',
  properties: {
    'context': 'context'
  }
})
@View({
  template: infoAreaTemplate
})
@Inject(NgScope, Eventbus)
export class InfoArea {
  constructor($scope, eventbus) {
    this.$scope = $scope;
    this.messages = {};
    let channel = '';
    if (this.context && this.context.getUUID()) {
      channel = this.context.getUUID();
    }
    this.infoAreaUpdateEvent = eventbus.subscribe({
      channel,
      topic: TOPIC,
      callback: (data)=> {
        this.addMessage(data.id, data.message);
      }
    });
  }

  addMessage(id, message) {
    let messageId = id || uuid();
    if (message) {
      this.messages[messageId] = message;
    } else {
      delete this.messages[messageId];
    }
  }

  ngOnDestroy() {
    this.infoAreaUpdateEvent.unsubscribe();
  }
}
