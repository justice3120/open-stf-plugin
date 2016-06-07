package hudson.plugins.openstf.util;

import com.sun.jersey.api.client.ClientHandlerException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher.ProcStarter;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.android_emulator.AndroidEmulator;
import hudson.plugins.openstf.STFBuildWrapper.DescriptorImpl;
import hudson.plugins.openstf.STFException;
import hudson.plugins.openstf.Messages;
import hudson.plugins.openstf.exception.ApiFailedException;
import hudson.plugins.openstf.exception.NoDeviceAvailableException;
import hudson.plugins.openstf.exception.WaitDeviceReleaseInterruptedException;
import hudson.plugins.openstf.exception.WaitDeviceReleaseTimeoutException;
import hudson.remoting.Callable;
import hudson.remoting.Future;
import hudson.util.ArgumentListBuilder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.DevicesApi;
import io.swagger.client.api.UserApi;
import io.swagger.client.model.AddUserDevicePayload;
import io.swagger.client.model.DeviceListResponseDevices;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

  /**
   * Expands the variable in the given string to its value in the variables available to this build.
   * The Jenkins-specific build variables take precedence over environment variables.
   *
   * @param envVars  Map of the environment variables.
   * @param buildVars  Map of the build-specific variables.
   * @param json  The json token set which may or may not contain variables in the format <tt>${foo}</tt>.
   * @return  The given json, with applicable variable expansions done.
   */
  public static JSONObject expandVariables(EnvVars envVars, Map<String,String> buildVars,
        JSONObject filter) {
    final Map<String,String> vars = new HashMap<String,String>(envVars);
    if (buildVars != null) {
      // Build-specific variables, if any, take priority over environment variables
      vars.putAll(buildVars);
    }

    for (Iterator<String> fi = filter.keys(); fi.hasNext(); ) {
      String key = fi.next();
      String value = filter.get(key).toString();

      String result = Util.fixEmptyAndTrim(value);
      if (result != null) {
        result = Util.replaceMacro(result, vars);
      }
      filter.put(key, result);
    }
    return filter;
  }

  /**
   * Gets default charset on node.
   * This method try to get the charset by using Jenkins method first.
   * If that fails, get the JVM Default Charset.
   * @return detected charset.
   */
  public static Charset getDefaultCharset() {
    try {
      return Computer.currentComputer().getDefaultCharset();
    } catch (NullPointerException ex) {
      return Charset.defaultCharset();
    }
  }

  public static ComboBoxModel getSTFDeviceAttributeComboBoxItems(String attribute) {
    ComboBoxModel items = new ComboBoxModel();
    items.add("any");
    for (String value: getSTFDeviceAttributeValueSet(attribute)) {
      items.add(value);
    }

    return items;
  }

  public static void setupSTFApiClient(String stfApiEndpoint, String stfToken) {
    ApiClient stfApiClient = new ApiClient();
    stfApiClient.setBasePath(stfApiEndpoint);
    stfApiClient.setApiKeyPrefix("Bearer");
    stfApiClient.setApiKey(stfToken);
    Configuration.setDefaultApiClient(stfApiClient);
  }

  public static List<DeviceListResponseDevices> getDeviceList() throws ApiFailedException {
    return getDeviceList(null);
  }

  /**
   * Gets a list of devices that match the given filter.
   * @param stfApiEndpoint The STF API endpoint URL to use to get STF devices.
   * @param stfToken The STF access token to use to get STF devices.
   * @param filter Conditions of the STF device you want to get.
   * @return List of STF devices that meet the filter.
   */
  public static List<DeviceListResponseDevices> getDeviceList(JSONObject filter) throws ApiFailedException {

    List<DeviceListResponseDevices> deviceList = new ArrayList<DeviceListResponseDevices>();
    String fields = "serial,name,model,version,sdk,image,present,owner";
    DevicesApi stfDevicesApi = new DevicesApi();

    try {
      deviceList = stfDevicesApi.getDevices(fields).getDevices();
    }  catch (ApiException ex) {
      throw new ApiFailedException("GET /devices API failed");
    }

    if (filter != null) {
      for (Iterator<String> fi = filter.keys(); fi.hasNext(); ) {
        String key = fi.next();
        String value = filter.get(key).toString();

        if (!value.equals("any")) {
          for (Iterator<DeviceListResponseDevices> di = deviceList.listIterator();
              di.hasNext(); ) {
            DeviceListResponseDevices device = di.next();
            Class klass = device.getClass();
            try {
              Field field = klass.getField(key);

              if (field.get(device) != null) {
                if (!value.equals(field.get(device).toString())) {
                  di.remove();
                }
              } else {
                if (!value.equals("null")) {
                  di.remove();
                }
              }
            } catch (NoSuchFieldException ex) {
              //ignore
            } catch (IllegalAccessException ex) {
              //ignore
            }
          }
        }
      }
    }
    return deviceList;
  }

  /**
   * Gets a STF device that matches the given id.
   * @param deviceId The id of the device you want to get.
   * @return a STF device that matches the given id.
   */
  public static DeviceListResponseDevices getSTFDeviceById(String deviceId) throws ApiFailedException {
    DeviceListResponseDevices device = null;

    DevicesApi stfDevicesApi = new DevicesApi();
    String fields = "serial,name,model,version,sdk,image,present,owner,remoteConnectUrl";
    try {
      device = stfDevicesApi.getDeviceBySerial(deviceId, fields).getDevice();
    } catch (ApiException ex) {
      throw new ApiFailedException("GET /devices/{serial} API failed");
    }
    return device;
  }

  public static void reserveSTFDevice(DeviceListResponseDevices device)
      throws ApiFailedException {

    UserApi stfUserApi = new UserApi();
    AddUserDevicePayload body = new AddUserDevicePayload();
    body.setSerial(device.serial);

    try {
      stfUserApi.addUserDevice(body);
    } catch (ApiException ex) {
      throw new ApiFailedException("POST /api/v1/user/devices API failed");
    }
  }

  public static void remoteConnectSTFDevice(DeviceListResponseDevices device)
      throws ApiFailedException {

    UserApi stfUserApi = new UserApi();
    try {
      stfUserApi.remoteConnectUserDeviceBySerial(device.serial);
    } catch (ApiException ex) {
      throw new ApiFailedException("POST /user/devices/{serial}/remoteConnect API failed");
    }
  }

  public static void remoteDisconnectSTFDevice(DeviceListResponseDevices device)
      throws ApiFailedException {

    UserApi stfUserApi = new UserApi();
    try {
      stfUserApi.remoteDisconnectUserDeviceBySerial(device.serial);
    } catch (ApiException ex) {
      throw new ApiFailedException("DELETE /api/v1/user/devices/{serial}/remoteConnect API failed");
    }
  }

  public static void releaseSTFDevice(DeviceListResponseDevices device) throws ApiFailedException {

    UserApi stfUserApi = new UserApi();
    try {
      stfUserApi.deleteUserDeviceBySerial(device.serial);
    } catch (ApiException ex) {
      throw new ApiFailedException("DELETE /api/v1/user/devices/{serial} API failed");
    }
  }

  /**
   * Validates whether the given URL looks like a valid STF API endpoint.
   * @param stfApiEndpoint The URL string to validate.
   * @return Whether the URL looks valid or not.
   */
  public static FormValidation validateSTFApiEndpoint(String stfApiEndpoint) {
    if (stfApiEndpoint == null || stfApiEndpoint.equals("")) {
      return FormValidation.ok();
    }

    if (!(stfApiEndpoint.startsWith("http://") || stfApiEndpoint.startsWith("https://"))) {
      return FormValidation.error(Messages.MALFORMED_STF_API_ENDPOINT_URL());
    }

    ApiClient stfApiClient = new ApiClient();
    stfApiClient.setBasePath(stfApiEndpoint);
    UserApi stfUserApi = new UserApi(stfApiClient);
    try {
      stfUserApi.getUser();
    } catch (ApiException ex) {
      String expectedMessage = "{\"success\":false,\"description\":\"Requires Authentication\"}";
      if (!(ex.getCode() == 401) || !ex.getResponseBody().equals(expectedMessage)) {
        return FormValidation.error(Messages.INVALID_STF_API_ENDPOINT_URL());
      }
    } catch (ClientHandlerException ex) {
      String message = ex.getMessage();
      if (message.startsWith("java.net.UnknownHostException:")) {
        return FormValidation.error(Messages.CANNOT_RESOLVE_HOST());
      } else if (message.startsWith("java.net.ConnectException:")) {
        return FormValidation.error(message.replaceAll("java.net.ConnectException: ", ""));
      } else {
        return FormValidation.error(message);
      }
    }

    return FormValidation.ok();
  }

  /**
   * Validates whether the given token looks like a valid STF access token.
   * @param stfApiEndpoint The STF API Endpoint URL for use in verification.
   * @param stfToken The token string to validate.
   * @return Whether the STF token looks valid or not.
   */
  public static FormValidation validateSTFToken(String stfApiEndpoint, String stfToken) {
    if (stfApiEndpoint == null || stfApiEndpoint.equals("")) {
      if (!(stfToken == null || stfToken.equals(""))) {
        return FormValidation.error(Messages.STF_API_ENDPOINT_NOT_SET());
      }
    } else {
      if (validateSTFApiEndpoint(stfApiEndpoint).kind == FormValidation.Kind.OK) {
        if (stfToken == null || stfToken.equals("")) {
          return FormValidation.error(Messages.STF_TOKEN_REQUIRED());
        }
        if (!verifyToken(stfApiEndpoint, stfToken)) {
          return FormValidation.error(Messages.STF_TOKEN_NOT_VALID());
        }
      } else {
        return FormValidation.error(Messages.STF_API_ENDPOINT_NOT_VALID());
      }
    }
    return FormValidation.ok();
  }


  private static TreeSet<String> getSTFDeviceAttributeValueSet(String attribure) {

    DescriptorImpl descriptor = Hudson.getInstance().getDescriptorByType(DescriptorImpl.class);
    if (descriptor != null) {
      Utils.setupSTFApiClient(descriptor.stfApiEndpoint, descriptor.stfToken);
    }

    TreeSet<String> items = new TreeSet<String>();

    try {
      for (DeviceListResponseDevices device : getDeviceList()) {
        Class klass = device.getClass();
        try {
          Field field = klass.getField(attribure);

          if (field.get(device) != null) {
            items.add(field.get(device).toString());
          }
        } catch (NoSuchFieldException ex) {
          //ignore
        } catch (IllegalAccessException ex) {
          //ignore
        }
      }
    } catch (ApiFailedException ex) {
      //ignore
    }

    return items;
  }

  private static boolean verifyToken(String stfApiEndpoint, String stfToken) {
    setupSTFApiClient(stfApiEndpoint, stfToken);
    UserApi stfUserApi = new UserApi();
    try {
      stfUserApi.getUser();
      return true;
    } catch (ApiException ex) {
      return false;
    }
  }
}
