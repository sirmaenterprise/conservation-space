import {Inject, Injectable} from 'app/app';
import {MomentAdapter} from 'adapters/moment-adapter';
import {HtmlUtil} from 'common/html-util';
import StringMatcher from 'string-matcher';

@Injectable()
@Inject(MomentAdapter)
export class CommentsFilterService {

  constructor(momentAdapter) {
    this.momentAdapter = momentAdapter;
  }

  filter(comments, filter) {
    if (filter.author) {
      comments = comments.filter(this.filterByAuthor(filter.author));
    }

    if (filter.fromDate.length && filter.toDate.length) {
      comments = comments.filter(this.filterByDate(filter.fromDate, filter.toDate));
    } else {
      if (filter.fromDate.length) {
        comments = comments.filter(this.filterByFromDate(filter.fromDate));
      }

      if (filter.toDate.length) {
        comments = comments.filter(this.filterByToDate(filter.toDate));
      }
    }

    if (filter.commentStatus) {
      comments = comments.filter(this.filterByStatus(filter.commentStatus));
    }

    if (filter.keyword.length) {
      comments = comments.filter(this.filterByKeyword(filter.keyword));
    }
    return comments;
  }

  filterByKeyword(keyword) {
    let matcher = new StringMatcher(keyword);
    return function (comment) {
      if (matcher.match(HtmlUtil.stripHtml(comment.getDescription()), true) !== -1) {
        return true;
      }
      if (comment.getReplies() && comment.getReplies().length) {
        for (let reply of comment.getReplies()) {
          if (matcher.match(HtmlUtil.stripHtml(reply.getDescription()), true) !== -1) {
            return true;
          }
        }
      }
      return false;
    };
  }

  filterByStatus(status) {
    return (comment) => {
      return comment.getStatus() === status;
    };
  }

  filterByAuthor(authorsId) {
    let authorsMap = {};
    for (let authorId of authorsId) {
      authorsMap[authorId] = authorId;
    }

    return function (comment) {
      if (authorsMap[comment.getAuthorId()] !== undefined) {
        return true;
      }
      if (comment.getReplies() && comment.getReplies().length) {
        for (let reply of comment.getReplies()) {
          if (authorsMap[reply.getAuthorId()] !== undefined) {
            return true;
          }
        }
      }
      return false;
    };
  }

  filterByFromDate(from) {
    return (comment) => {
      if (this.momentAdapter.isAfter(comment.getCreatedDate(), from)) {
        return true;
      }
      if (comment.getReplies() && comment.getReplies().length) {
        for (let reply of comment.getReplies()) {
          if (this.momentAdapter.isAfter(reply.getCreatedDate(), from)) {
            return true;
          }
        }
      }
      return false;
    };
  }

  filterByToDate(to) {
    return (comment) => {
      if (this.momentAdapter.isBefore(comment.getCreatedDate(), to)) {
        return true;
      }
      if (comment.getReplies() && comment.getReplies().length) {
        for (let reply of comment.getReplies()) {
          if (this.momentAdapter.isBefore(reply.getCreatedDate(), to)) {
            return true;
          }
        }
      }
      return false;
    };
  }

  filterByDate(from, to) {
    return (comment) => {
      return this.filterByFromDate(from)(comment) && this.filterByToDate(to)(comment);
    };
  }
}