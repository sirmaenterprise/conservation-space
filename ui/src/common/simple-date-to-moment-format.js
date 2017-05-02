/**
 * Converts sent server side SimpleDataFormat to analogous Moment.js format.
 *  Note: some formats in SimpleDataFormat are not supported in moment.js.
 *
 *  Table used to map the sequence of conversion
 *   JAVA ->  Moment.js
 *   G    ->  NOT SUPPORTED!  Era designator
 *   YYYY ->  g               Week year
 *   y    ->  Y               Year
 *   M    ->  SAME            Month in year
 *   w    ->  SAME            Week in year
 *   W    ->  NONE!           Week in month
 *   D    ->  DDD             Day in year
 *   d    ->  D               Day in month
 *   F    ->  NOT SUPPORTED!  Day of week in month
 *   E    ->  d               Day name in week
 *   u    ->  e               Day number of week
 *   a    ->  SAME!           Am/pm marker
 *   H    ->  SAME!           Hour in day (0-23)
 *   k    ->  NONE! (using H) Hour in day (1-24)
 *   K    ->  h               Hour in am/pm (0-11)
 *   h    ->  NONE!           Hour in am/pm (1-12)
 *   m    ->  SAME!           Minute in hour
 *   s    ->  SAME!           Second in minute
 *   S    ->  SSS             Millisecond
 *   z    ->  (DEPRECATED IN MOMENT, replacing with Z)
 *   Z    ->  SAME!           Time zone
 *   X    ->  Z               Time zone
 **/
export class SimpleDateToMomentFormat {

  static convertToMomentFormat(format) {
    let localFormatString = format;
    // The order is significant
    /* YY -> gg  */localFormatString = localFormatString.replace(/Y/g, 'g');
    /* y  -> Y   */localFormatString = localFormatString.replace(/y/g,'Y');
    /* D  -> DDD */localFormatString = localFormatString.replace(/D{1,}/g,'DDD');
    /* d  -> D   */localFormatString = localFormatString.replace(/d{1,}/g,'DD');
    /* E  -> d   */localFormatString = localFormatString.replace(/E{4,}/g,'dddd');
    /* E  -> d   */localFormatString = localFormatString.replace(/E{1,3}/g,'ddd');
    /* u  -> e   */localFormatString = localFormatString.replace(/u/g,'e');
    /* k  -> H   */localFormatString = localFormatString.replace(/k/g,'H');
    /* K  -> h   */localFormatString = localFormatString.replace(/K/g,'h');
    /* S  -> SSS */localFormatString = localFormatString.replace(/S/,'SSS');
    /* X  -> Z   */localFormatString =localFormatString.replace(/X/g,'Z');
    /* z  -> Z   */localFormatString = localFormatString.replace(/z/g,'Z');
    return (localFormatString);
  }
}