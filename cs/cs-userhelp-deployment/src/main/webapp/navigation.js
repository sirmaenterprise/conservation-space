function goBack(){
    var content = top.contentsFrame;
    return content.history.go(-1);
}
function goFwd(){
    var content = top.contentsFrame;
    return content.history.go(1);
}