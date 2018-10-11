import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {UserLoadedEvent} from 'services/identity/user-service';

@Injectable()
@Inject(PromiseAdapter)
export class UserService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.currentUser = {
      id: 'john@domain',
      mailboxSupportable: true,
      name: 'John',
      username: 'john',
      isAdmin: true,
      language: 'en'
    };
  }

  getCurrentUser(reload = false) {
    return this.promiseAdapter.promise((resolve) => {
      if (this.currentUser && !reload) {
        resolve(this.currentUser);
      } else {
        this.eventbus.publish(new UserLoadedEvent(this.currentUser));
        resolve(this.currentUser);
      }
    });
  }

  changePassword(oldPassword, newPassword) {
    return this.promiseAdapter.resolve({});
  }

  getCurrentUserId() {
    return this.currentUser.id;
  }

}

