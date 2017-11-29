package org.jenkinsci.plugins.karafbuildstep;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/**
 * Build step which executes various Karaf commands via Karaf client.
 *
 * @author <a href="mailto:jgalego1990@gmail.com">João Galego</a>
 *
 */
public class KarafBuildStepBuilder extends Builder implements SimpleBuildStep {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(KarafBuildStepBuilder.class);

    // Constants
    private static final String EMPTY_FIELD_MESSAGE = "This field should not be empty";

    // Form parameters
    private boolean useCustomKaraf;

    private String karafHome;

    private String flags;

    private KarafCommandOption option;

    @DataBoundConstructor
    public KarafBuildStepBuilder(boolean useCustomKaraf, String karafHome, String flags, KarafCommandOption option) {
        this.useCustomKaraf = useCustomKaraf;
        this.karafHome = karafHome;
        this.flags = flags;
        this.option = option;
    }

    public boolean getUseCustomKaraf() {
        return useCustomKaraf;
    }

    public String getKarafHome() {
        return karafHome;
    }

    public String getFlags() {
        return flags;
    }

    public KarafCommandOption getOption() {

        return option;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        try{
            LOGGER.info("Executing Karaf Build Step");
            int exitCode = option.execute(build, workspace, launcher, listener, this);

            LOGGER.info("Exit code: {}", exitCode);
            if (exitCode == 0) {
                build.setResult(Result.SUCCESS);
            }
            else {
                build.setResult(Result.FAILURE);
            }
        }
        catch (KarafCommandException ex) {
            LOGGER.error("Karaf Build Step failed to execute", ex);
            ex.printStackTrace();
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {

        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String defaultKarafHome = "/default/path/to/karaf";

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            defaultKarafHome = formData.getString("defaultKarafHome");

            // Can also use req.bindJSON(this, formData)
            save();

            return super.configure(req, formData);
        }

        @Override
        public String getDisplayName() {

            return "Execute Karaf command";
        }

        public String getDefaultKarafHome() {

            return defaultKarafHome;
        }

        public static DescriptorExtensionList<KarafCommandOption, KarafCommandOptionDescriptor> getOptionList() {

            return KarafCommandOptionDescriptor.all();
        }

        /* Form Validation */

        public FormValidation doCheckKarafHome(@QueryParameter String value, @QueryParameter boolean useCustomKaraf) {

            if(useCustomKaraf && value.isEmpty()) {
                return FormValidation.warning(EMPTY_FIELD_MESSAGE);
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckScript(@QueryParameter String value) {

            if(value.isEmpty()) {
                return FormValidation.error(EMPTY_FIELD_MESSAGE);
            }

            return FormValidation.ok();
        }
    }
}
