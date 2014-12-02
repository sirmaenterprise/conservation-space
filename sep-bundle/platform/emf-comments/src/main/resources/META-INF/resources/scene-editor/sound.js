var sound = document.createElement("audio");

function setSound(src) {
    sound.src = src;
    sound.play();
}

function setVolume(volume) {
    sound.volume = volume;
}

function start() {
    sound.play();
}

function pause() {
    sound.pause();
}