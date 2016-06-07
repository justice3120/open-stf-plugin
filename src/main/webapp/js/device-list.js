updateDeviceList();

var stfElements = document.getElementsByClassName('stf-filter');
for (var i = 0; i < stfElements.length; i++) {
    stfElements[i].addEventListener('blur', updateDeviceList, false);
}

function updateDeviceList(evt) {
    var filter = {};
    var conditions = document.getElementsByClassName('stf-filter');
    for (var i = 0; i < conditions.length; i++) {
      var key = conditions[i].name.split(".")[2];
      var value = conditions[i].value == "" ? "any" : conditions[i].value;
      filter[key] = value;
    }
    desc.getStfApiEndpoint(function(t){
        var stfUrlArray = t.responseJSON.split("/");
        desc.getDeviceListJSON(filter, function(t) {
            var devices = t.responseJSON;
            document.getElementById('deviceList').innerHTML = "";
            for (var i = 0; i < devices.length; i++) {
                var div = document.createElement("div");
                div.setAttribute("class", "device-list-item");
                var img = document.createElement("img");
                img.setAttribute("src", stfUrlArray[0] + "//" + stfUrlArray[2] + "/static/app/devices/icon/x120/" + (devices[i].image == "" ?  "_default.jpg" : devices[i].image));
                img.setAttribute("class", "device-list-item-image");
                var pName = document.createElement("p");
                pName.setAttribute("class", "device-list-item-name");
                pName.innerText = devices[i].name == "" ? "(No Name)" : devices[i].name;
                var pStatus = document.createElement("p");
                if (devices[i].present) {
                    if (devices[i].owner == null) {
                        pStatus.innerText = "Ready";
                        pStatus.setAttribute("class", "device-list-item-status ready");
                    } else {
                        img.setAttribute("class", "device-list-item-image device-is-busy");
                        pName.setAttribute("class", "device-list-item-name device-is-busy");
                        pStatus.innerText = "Using";
                        pStatus.setAttribute("class", "device-list-item-status using");
                    }
                } else {
                    img.setAttribute("class", "device-list-item-image device-is-busy");
                    pName.setAttribute("class", "device-list-item-name device-is-busy");
                    pStatus.innerText = "Disconnected";
                    pStatus.setAttribute("class", "device-list-item-status disconnected");
                }
                div.appendChild(img);
                div.appendChild(pName);
                div.appendChild(pStatus);
                document.getElementById('deviceList').appendChild(div);
            }
        });
    });
}
