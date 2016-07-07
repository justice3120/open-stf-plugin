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
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

public class STFDeviceConditionAxis extends Axis {

  public String type;

  @DataBoundConstructor
  public STFDeviceConditionAxis(String name, String type, List<String> values) {
    super(name, values);
    this.type = type;
  }

  @Extension
  public static class DescriptorImpl extends AxisDescriptor {

    public DescriptorImpl() {
      super(STFDeviceConditionAxis.class);
      load();
    }

    @Override
    public Axis newInstance(StaplerRequest req, JSONObject formData) throws FormException {
      String name = formData.getString("name");
      String type = formData.getString("type");
      List<String> values = JSONArray.toList(formData.getJSONArray("values"), String.class);
      return new STFDeviceConditionAxis(name, type, values);
    }

    @Override
    public String getDisplayName() {
      return "STF Device Condition";
    }

    /**
     * Setting device condition types on jelly.
     * This Method called by Jenkins.
     * @return List of Device condition types.
     */
    public ListBoxModel doFillTypeItems() {

      Hudson hudsonInstance = Hudson.getInstance();
      if (hudsonInstance == null) {
        return new ListBoxModel();
      }

      STFBuildWrapper.DescriptorImpl descriptor = hudsonInstance
          .getDescriptorByType(STFBuildWrapper.DescriptorImpl.class);
      Utils.setupSTFApiClient(descriptor.stfApiEndpoint, descriptor.stfToken);

      return Utils.getSTFDeviceAttributeListBoxItems();
    }

    /**
     * Setting device condition values on jelly.
     * This Method called by Jenkins.
     * @return List of Device condition values.
     */
    public ListBoxModel doFillValuesItems(@QueryParameter String type) {

      Hudson hudsonInstance = Hudson.getInstance();
      if (hudsonInstance == null) {
        return new ListBoxModel();
      }

      STFBuildWrapper.DescriptorImpl descriptor = hudsonInstance
          .getDescriptorByType(STFBuildWrapper.DescriptorImpl.class);
      Utils.setupSTFApiClient(descriptor.stfApiEndpoint, descriptor.stfToken);

      return Utils.getSTFDeviceAttributeValueListBoxItems(type);
    }
  }
}
