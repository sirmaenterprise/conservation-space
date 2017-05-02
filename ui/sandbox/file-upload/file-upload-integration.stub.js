import {Injectable} from 'app/app';

/**
 * Stubs FileUploadIntegration allowing to configure if the call should succee or fail.
 * Allows method chaining like fileUploadIntegration.submit().done().error();
 */
@Injectable()
export class FileUploadIntegration {

  submit(control) {
    var timeout = this.timeout ? this.timeout : 1000;

    var fail = (f) => {
      if (this.failRequest) {
        setTimeout(function () {
          f();
        }, timeout);
      }

      return {
        abort: function () {
          $('body').append('<div class="file-upload-integration">Upload aborted!</div>');
        }
      }
    };

    var done = (f) => {
      if (!this.failRequest) {
        setTimeout(function () {
          f({
            'emf:contentId': 'contentId'
          });
        }, timeout);
      }

      return {
        fail: fail
      }
    };

    return {
      done: done
    }
  }

  swithToFail() {
    this.failRequest = true;
  }

  switchToSuccess() {
    this.failRequest = false;
  }

  setTimeout(timeout) {
    this.timeout = timeout;
  }

}