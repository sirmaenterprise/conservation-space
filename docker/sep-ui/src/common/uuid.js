// Borrowed from http://stackoverflow.com/a/2117523/1119400
export default function(compact) {
  var template = compact ? 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx' : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
  var currentTime = new Date().getTime();
  var result = template.replace(/[xy]/g, function(c) {
    var random = (currentTime + Math.random() * 16) % 16 | 0;
    currentTime = Math.floor(currentTime / 16);
    return (c === 'x' ? random : (random & 0x7 | 0x8)).toString(16);
  });
  return result;
};