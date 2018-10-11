'use strict';

function elementToStopMoving(element) {
  let prevx = null;
  let prevy = null;

  return function () {
    return element.getLocation().then(loc => {
      let x = loc.x;
      let y = loc.y;
      if (prevx === x && prevy === y) {
        return true;
      }

      prevx = x;
      prevy = y;
      return false;
    });
  };
}

module.exports = {
  elementToStopMoving
};