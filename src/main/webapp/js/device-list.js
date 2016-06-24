Q(document).ready(function(){
  updateDeviceList();

  Q('.stf-filter').each(function(){
    Q(this).blur(updateDeviceList);
  });
});

function updateDeviceList(evt) {
    var filter = {};
    Q('.stf-filter').each(function(){
      var key = this.name.split(".")[2];
      var value = this.value == "" ? "any" : this.value;
      filter[key] = value;
    });
    desc.getStfApiEndpoint(function(t){
        var stfUrlArray = t.responseJSON.split("/");
        desc.getDeviceListJSON(filter, function(t) {
            var devices = t.responseJSON;
            Q('#deviceList').html('');
            Q.each(devices, function(index, device){
              var div = Q('<div />').addClass('device-list-item');
              var imgUrl = stfUrlArray[0] + "//" + stfUrlArray[2] + "/static/app/devices/icon/x120/" + (device.image == "" ?  "_default.jpg" : device.image);
              var img = Q('<img />').addClass('device-list-item-image').attr('src', imgUrl);
              var deviceName = device.name == "" ? "(No Name)" : device.name;
              var pName = Q('<p />').addClass('device-list-item-name').text(deviceName);
              var pStatus = Q('<p />');
              if (device.present) {
                if (device.owner == null) {
                  pStatus.text('Ready').addClass('device-list-item-status ready');
                } else {
                  img.addClass('device-is-busy');
                  pName.addClass('device-is-busy');
                  pStatus.text('Using').addClass('device-list-item-status using');
                }
              } else {
                img.addClass('device-is-busy');
                pName.addClass('device-is-busy');
                pStatus.text('Disconnected').addClass('device-list-item-status disconnected');
              }
              Q('#deviceList').append(div.append(img).append(pName).append(pStatus));
            });
        });
    });
}
