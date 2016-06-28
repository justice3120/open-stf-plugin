package hudson.plugins.openstf.axis;

import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.model.Hudson;
import hudson.plugins.openstf.STFBuildWrapper;
import hudson.plugins.openstf.util.Utils;
import hudson.util.ListBoxModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.List;

public class ModelAxis extends Axis {

  @DataBoundConstructor
  public ModelAxis(String name, List<String> values) {
    super(name, values);
  }

  @Extension
  public static class DescriptorImpl extends AxisDescriptor {

    List<String> cachedValues = null;

    public DescriptorImpl() {
      super(ModelAxis.class);
      load();
    }

    @Override
    public Axis newInstance(StaplerRequest req, JSONObject formData) throws FormException {
      String name = formData.getString("name");
      List<String> values = JSONArray.toList(formData.getJSONArray("values"), String.class);
      cachedValues = values;
      save();
      return new ModelAxis(name, values);
    }

    @Override
    public String getDisplayName() {
      return "STF Device Model";
    }

    /**
     * Setting device model values on jelly.
     * This Method called by Jenkins.
     * @return List of Device Model values.
     */
    public ListBoxModel doFillValuesItems() {

      Hudson hudsonInstance = Hudson.getInstance();
      if (hudsonInstance == null) {
        return new ListBoxModel();
      }

      STFBuildWrapper.DescriptorImpl descriptor = hudsonInstance
          .getDescriptorByType(STFBuildWrapper.DescriptorImpl.class);
      Utils.setupSTFApiClient(descriptor.stfApiEndpoint, descriptor.stfToken);

      return Utils.getSTFDeviceAttributeValueListBoxItems("model");
    }

    @JavaScriptMethod
    public JSONArray getCachedValuesJSON() {
      return JSONArray.fromObject(cachedValues);
    }
  }
}
