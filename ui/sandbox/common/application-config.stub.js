import {Injectable} from 'app/app';

@Injectable()
export class Configuration {
  constructor() {
    this.configs = {};
    this.configs[Configuration.UI_TEXTAREA_MIN_CHARS] = 50;
    this.configs[Configuration.UI_DATE_FORMAT] = 'DD.MM.YY';
    this.configs[Configuration.UI_TIME_FORMAT] = 'HH:mm';
    this.configs[Configuration.APPLICATION_MODE_DEVELOPMENT] = true;
    this.configs[Configuration.CAMUNDA_EENGINE] = true;
    this.configs[Configuration.TYPES_CONFIG] = ["http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image"]
  }

  get(key) {
    return this.configs[key];
  }

  getArray(key){
    return this.configs[key];
  }
}

Configuration.APPLICATION_MODE_DEVELOPMENT = 'application.mode.development';
Configuration.UI_TEXTAREA_MIN_CHARS = 'ui.textarea.min.chars';
Configuration.UI_DATE_FORMAT = 'ui.date.format';
Configuration.UI_TIME_FORMAT = 'ui.time.format';
Configuration.CAMUNDA_EENGINE = 'processes.camunda.engine.name';
Configuration.TYPES_CONFIG = 'widget.image.types';