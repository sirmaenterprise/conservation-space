import {Inject, Injectable} from 'app/app';

@Injectable()
@Inject('$timeout', '$q')
export class LabelRestService {
  constructor($timeout, $q) {
    this.$timeout = $timeout;
    this.$q = $q;
  }

  getLabels(language) {
    var deferred = this.$q.defer();
    var data = {
      'en': {
        'fallback': 'Fallback',
        'dashboard': 'Dashboard',
        'homepage': 'Home page',
        'homepage.unbind': 'Home page unbind',
        'menu': 'Menu',
        'menu.unbind': 'Menu unbind',
        'welcome': 'Welcome {{username}}. Current date is {{currentDate}}.',
        'month.january': 'January',
        'month.february': 'February',
        'month.march': 'March',
        'month.april': 'April',
        'month.may': 'May',
        'month.june': 'June',
        'month.july': 'July',
        'month.august': 'August',
        'month.september': 'September',
        'month.october': 'October',
        'month.november': 'November',
        'month.december': 'December',
        'day.sunday': 'Sunday',
        'day.monday': 'Monday',
        'day.tuesday': 'Tuesday',
        'day.wednesday': 'Wednesday',
        'day.thursday': 'Thursday',
        'day.friday': 'Friday',
        'day.saturday': 'Saturday',
      },
      'bg': {
        'dashboard': 'Работен плот',
        'homepage': 'Начална страница',
        'homepage.unbind': 'Начална страница без връзка',
        'menu': 'Меню',
        'menu.unbind': 'Меню без връзка',
        'welcome': 'Добре дошли {{username}}. Текущата дата е {{currentDate}}.',
        'month.january': 'Януари',
        'month.february': 'Февруари',
        'month.march': 'Март',
        'month.april': 'Април',
        'month.may': 'Май',
        'month.june': 'Юни',
        'month.july': 'Юли',
        'month.august': 'Август',
        'month.september': 'Септември',
        'month.october': 'Октомври',
        'month.november': 'Ноември',
        'month.december': 'Декември',
        'day.sunday': 'Неделя',
        'day.monday': 'Понеделник',
        'day.tuesday': 'Вторник',
        'day.wednesday': 'Сряда',
        'day.thursday': 'Четвъртък',
        'day.friday': 'Петък',
        'day.saturday': 'Събота',
      }
    };
    // Simulate ajax request
    this.$timeout(function () {
      deferred.resolve(data[language]);
    }, 10);
    return deferred.promise;
  }
}
