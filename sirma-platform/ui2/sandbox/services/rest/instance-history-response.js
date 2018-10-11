import _ from 'lodash';
import moment from 'moment';

const LINK_NEXT = 'https://api.github.com/search?offset=5&limit=5';
const LINK_LAST = 'https://api.github.com/search?offset=100&limit=5';
const LINK_REL_NEXT = `<${LINK_NEXT}>; rel="next"`;
const LINK_REL_LAST = `<${LINK_LAST}>; rel="last"`;

const headers = {
  Link: `${LINK_REL_NEXT}, ${LINK_REL_LAST}`
}

export function createSearchResponse(ids, limit, offset) {
  var data = [];
  var date = moment();
  if (limit === 'all') {
    limit = '100';
    offset = 0;
  }
  var minutesOffset = 30;
  _.range(offset, offset + parseInt(limit)).forEach(function(num) {
    data.push({
      user: {
        id: `${num}`,
        name: `User ${num}`
      },
      timestamp: date.subtract(minutesOffset, 'minutes').format(),
      text: `Activity #${num}: Somebody did something they should't have`
    });
    minutesOffset += minutesOffset;
  });

  return {
    headers: (header) => {
      return headers[header];
    },
    data: data
  };
};