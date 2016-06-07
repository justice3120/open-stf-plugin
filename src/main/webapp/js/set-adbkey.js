adbPublicKeyFrame.onload = function() {
    adbPublicKeyFrame.document.getElementById('AdbPublicKey').addEventListener('change', setAdbKeyValue, false);
}

adbPrivateKeyFrame.onload = function() {
    adbPrivateKeyFrame.document.getElementById('AdbPrivateKey').addEventListener('change', setAdbKeyValue, false);
}

function setAdbKeyValue(evt) {
    var file = evt.target.files[0];
    var reader = new FileReader();
    reader.onload = function (e) {
        if (evt.target.id == "AdbPublicKey") {
            document.getElementsByName("open-stf.adbPublicKey")[0].value = reader.result;
        } else if (evt.target.id == "AdbPrivateKey") {
            document.getElementsByName("open-stf.adbPrivateKey")[0].value = reader.result;
        }
    }
    reader.readAsText(file);
}
