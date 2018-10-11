import {Injectable} from 'app/app';

/**
 * Stubs FileUploadIntegration allowing to configure if the call should succee or fail.
 * Allows method chaining like fileUploadIntegration.submit().done().error();
 */
@Injectable()
export class FileUploadIntegration {

  submit() {
    let timeout = this.timeout ? this.timeout : 0;

    let always = (fn) => {
      setTimeout(() => {
        fn();
      }, timeout);

      return {always, done, fail, abort};
    };

    let abort = () => $('body').append('<div class="file-upload-integration">Upload aborted!</div>');

    let fail = (fn) => {
      if (this.failRequest) {
        setTimeout(() => {
          fn({
            responseJSON: this.response
          });
        }, timeout);
      }

      return {always, done, fail, abort};
    };

    let done = (fn) => {
      if (!this.failRequest) {
        setTimeout(() => {
          fn(this.response);
        }, timeout);
      }
      return {always, done, fail, abort};
    };

    return {always, done, fail, abort};
  }

  switchToFail(response) {
    this.failRequest = true;
    this.response = response;
  }

  switchToSuccess(response) {
    this.failRequest = false;
    this.response = response;
  }

  setTimeout(timeout) {
    this.timeout = timeout;
  }

}