import {Injectable} from 'app/app';

@Injectable()
export class Configuration {
  constructor() {
    this.configs = {};
    this.configs[Configuration.SEARCH_PAGE_SIZE] = 25;
    this.configs[Configuration.UI_TEXTAREA_MIN_CHARS] = 50;
    this.configs[Configuration.UI_DATE_FORMAT] = 'DD.MM.YY';
    this.configs[Configuration.UI_TIME_FORMAT] = 'HH:mm';
    this.configs[Configuration.APPLICATION_MODE_DEVELOPMENT] = true;
    this.configs[Configuration.BPM_ENGINE_NAME] = true;
    this.configs[Configuration.TYPES_CONFIG] = ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image'];
    this.configs[Configuration.OBJECT_PROP_INITIAL_LOAD_LIMIT] = 3;
    this.configs[Configuration.USER_MANAGEMENT_USER_PROPERTIES] = '{"columns":["firstName","lastName","email","isMemberOf"]}';
    this.configs[Configuration.MAILBOX_STATUS_POLL_INTERVAL] = '15000';
    this.configs[Configuration.UPLOAD_MAX_SIMULTANEOUS_NUMBER_FILES] = 5;
    this.configs[Configuration.SYSTEM_LANGUAGE] = 'en';
  }

  get(key) {
    return this.configs[key];
  }

  getJson(key) {
    return JSON.parse(this.get(key));
  }

  getArray(key){
    return this.configs[key];
  }
}

Configuration.APPLICATION_MODE_DEVELOPMENT = 'application.mode.development';
Configuration.UI_TEXTAREA_MIN_CHARS = 'ui.textarea.min.chars';
Configuration.SEARCH_PAGE_SIZE = 'search.result.pager.pagesize';
Configuration.UI_DATE_FORMAT = 'ui.date.format';
Configuration.UI_TIME_FORMAT = 'ui.time.format';
Configuration.BPM_ENGINE_NAME = 'processes.camunda.engine.name';
Configuration.TYPES_CONFIG = 'widget.image.types';
Configuration.USER_MANAGEMENT_USER_PROPERTIES = 'user.management.user.properties';
Configuration.MAILBOX_STATUS_POLL_INTERVAL = 'subsystem.emailintegration.mailbox.status.poll.interval';
Configuration.OBJECT_PROP_INITIAL_LOAD_LIMIT = 'object.properties.initial.load.limit';
Configuration.EAI_DAM_ENABLED = 'eai.dam.enabled';
Configuration.UPLOAD_MAX_SIMULTANEOUS_NUMBER_FILES = 'file.upload.max.simultaneous.files.count';
Configuration.SYSTEM_LANGUAGE = 'system.language';