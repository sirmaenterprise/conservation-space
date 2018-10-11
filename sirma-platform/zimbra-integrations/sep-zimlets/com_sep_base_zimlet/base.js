function com_sep_base_zimlet() {
}

com_sep_base_zimlet.prototype = new ZmZimletBase();
com_sep_base_zimlet.prototype.constructor = com_sep_base_zimlet;

com_sep_base_zimlet.prototype.postMessage = function (target, payload) {
  if(!target.topic || !payload.command) {
    throw "'topic' and 'command' are required for message to be sent!";
  }
  payload.topic = target.topic
  window.parent.postMessage(payload, '*');
};

com_sep_base_zimlet.prototype.isComposeTab = function (viewId) {
  return viewId.indexOf("COMPOSE") !== -1;
};

com_sep_base_zimlet.prototype.findAllRecipients = function (to, cc) {
  return to.concat(cc).map(function (recipient) {
	return recipient.address;
  }).filter(function (address, index, array) {
    return array.indexOf(address) === index;
  });
};