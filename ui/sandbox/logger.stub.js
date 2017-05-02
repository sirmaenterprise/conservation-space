import {Injectable} from 'app/app';

@Injectable()
export class Logger {

  log(message, logOnServer) {
    console.log(message);
  }

  info(message, logOnServer) {
    console.info(message);
  }

  warn(message, logOnServer) {
    console.warn(message);
  }

  debug(message, logOnServer) {
    console.log(message);
  }

  error(message) {
    console.error(message);
  }
}