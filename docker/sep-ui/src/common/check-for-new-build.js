import {Injectable, Inject} from 'app/app';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {BootstrapService} from 'services/bootstrap-service';

/**
 * Checks for new build UI builds by fetching build-info.txt on each view change
 * and performs browser refresh if new UI build is detected.
 *
 * During UI build a window variable named 'buildInfo' is added that contains the build timestamp.
 */
@Injectable()
@Inject(Eventbus)
export class CheckForNewBuild extends BootstrapService {

  /* istanbul ignore next */
  constructor(eventbus) {
    super();

    eventbus.subscribe(RouterStateChangeStartEvent, () => {
      if (window.buildInfo) {
        $.ajax({
          url: 'build-info.txt',
          cache: false,
          success: data => {
            if (data !== window.buildInfo) {
              window.location.reload();
            }
          }
        });
      }
    });
  }

  initialize() {

  }

}