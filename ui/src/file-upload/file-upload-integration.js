import {Injectable} from 'app/app';

/**
 * Mediator between the file upload component and the jquery file upload. Used to stub
 */
@Injectable()
export class FileUploadIntegration {

  submit(fileUploadControl) {
    return fileUploadControl.submit();
  }

}