import {Command} from 'common/command-chain/command-chain';

const NUMERIC_PROPERTIES = ['int', 'long', 'float', 'double'];

/**
 * Property data types converters. Used to convert properties when fetching from the platform.
 * These converters serve as a bridge between the platform defined data types & the data types
 * currently used by the UI
 *
 * @author Svetlozar Iliev
 */

export class BooleanTypeConverter extends Command {
  canHandle(dataType) {
    return dataType === 'boolean';
  }

  handle(dataType) {
    return dataType;
  }
}

export class NumericTypeConverter extends Command {
  canHandle(dataType) {
    return NUMERIC_PROPERTIES.indexOf(dataType) > -1;
  }

  handle() {
    return 'numeric';
  }
}

export class StringTypeConverter extends Command {
  canHandle(dataType) {
    return dataType === 'text';
  }

  handle() {
    return 'string';
  }
}

export class ObjectTypeConverter extends Command {
  canHandle(dataType) {
    return dataType === 'any';
  }

  handle() {
    return 'object';
  }
}

/**
 * Date time type converter serves to unify all different types of
 * date or/and time property types to a single output type
 */
export class DateTimeTypeConverter extends Command {
  canHandle(dataType) {
    return dataType === 'date' || dataType === 'time' || dataType === 'datetime';
  }

  handle() {
    return 'dateTime';
  }
}

/**
 * Default type converter usually stays at the end of the converter chain
 * and if the type is valid it preserves the type of the property as is
 */
export class DefaultTypeConverter extends Command {
  canHandle(dataType) {
    return !!dataType && typeof dataType === 'string';
  }

  handle(dataType) {
    return dataType;
  }
}