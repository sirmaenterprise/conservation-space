import {Component, Inject} from 'app/app';
import {WindowResizedEvent} from 'layout/window-resized/window-resized-event';
import {Eventbus} from 'services/eventbus/eventbus';
import _ from 'lodash';

@Component({
  selector: 'window-resized-handler'
})
@Inject(Eventbus)
export class WindowResizedHandler {

  constructor(eventbus) {
    $(window).resize(
      _.debounce((event) => {
        if (event.target === window) {
          eventbus.publish(new WindowResizedEvent());
        }
      }, 150)
    );
  }

}