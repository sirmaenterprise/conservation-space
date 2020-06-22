import {Component, View} from 'app/app';

@Component({
  selector: 'app-bootstrap'
})
@View({
  template: `<div>
    <simple-component on-load="appBootstrap.echoMessage()"></simple-component>
    <div class="message">{{ appBootstrap.message }}</div>
  </div>`
})
export class AppBootstrap {

  echoMessage() {
    this.message = 'echo';
  }

}

@Component({
  selector: 'simple-component',
  events: ['onLoad']
})
@View({
  template: '<div>Simple component</div>'
})
class SimpleComponent {

  constructor() {
    this.onLoad();
  }

}
