package hudson.plugins.openstf;

import hudson.model.Action;
import hudson.plugins.openstf.Messages;
import io.swagger.client.model.DeviceListResponseDevices;
import org.kohsuke.stapler.export.Exported;

import java.net.URL;

public class STFReservedDeviceAction implements Action {
  private final String stfApiEndpoint;
  private final DeviceListResponseDevices reservedDevice;

  public STFReservedDeviceAction(String stfApiEndpoint, DeviceListResponseDevices reservedDevice) {
    this.stfApiEndpoint = stfApiEndpoint;
    this.reservedDevice = reservedDevice;
  }

  @Exported
  public String getDeviceIcon() {
    String path = "/static/app/devices/icon/x120/";
    if (reservedDevice.image != null) {
      path += reservedDevice.image;
    } else {
      path += "_default.jpg";
    }
    try {
      URL iconUrl = new URL(new URL(stfApiEndpoint), path);
      return iconUrl.toString();
    } catch (Exception ex) {
      return "";
    }
  }

  @Exported
  public String getSummary() {
    return Messages.PUBLISH_RESERVED_DEVICE_INFO(reservedDevice.name, reservedDevice.sdk,
        reservedDevice.version);
  }

  public String getDisplayName() {
      return null;
  }

  public String getIconFileName() {
      return null;
  }

  public String getUrlName() {
      return null;
  }
}
