import {Injectable, Inject} from 'app/app';
import {Storage} from 'services/storage/storage';
import {WindowAdapter} from 'adapters/angular/window-adapter';

@Injectable()
@Inject(WindowAdapter)
export class SessionStorageService extends Storage {

  constructor(windowAdapter) {
    super(windowAdapter.window.sessionStorage);
  }
}