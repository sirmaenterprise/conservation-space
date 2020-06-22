import {Injectable} from 'app/app';

@Injectable()
export class PollingUtils {

  constructor() {
    this.tasks = {};
  }

  /**
   * Starts a polling task using the given interval. The task id is used to store the task and prevent creation of
   * multiple identical tasks. The method returns the task wrapped inside a custom object with a handy function for
   * stopping the task which is useful when a polling is executed from a directive which may be destroyed, then the
   * polling could be stopped as well.
   *
   * @param taskId The task id to be used for the task store. Prevents similar tasks creation.
   * @param fn The function to be executed during the polling.
   * @param interval The time interval on which the task should be executed.
   * @param stopOnBlur If the task should be stopped when the browser tab loses focus and started when it gains the
   *  focus again. Default is true.
   * @returns {*} An object wrapping the task, which has a stop function to stop the polling.
   */
  pollInfinite(taskId, fn, interval, stopOnBlur = true) {
    if (this.tasks[taskId]) {
      this.tasks[taskId].stop();
    }
    this.tasks[taskId] = new PollingTask(taskId, fn, interval, stopOnBlur);
    return this.tasks[taskId];
  }
}

class PollingTask {

  constructor(taskId, fn, interval, stopOnBlur) {
    this.taskId = taskId;
    this.fn = fn;
    this.interval = interval;

    if (stopOnBlur) {
      let focusEventName = 'focus.' + taskId;
      let blurEventName = 'blur.' + taskId;
      $(window).off(focusEventName).on(focusEventName, function () {
        if (!this.started) {
          this.start();
        }
      }.bind(this));

      $(window).off(blurEventName).on(blurEventName, function () {
        this.stop();
      }.bind(this));
    }

    this.start();
  }

  start() {
    this.started = true;
    this.poller = setInterval(this.fn, this.interval);
  }

  stop() {
    this.started = false;
    clearInterval(this.poller);
  }
}