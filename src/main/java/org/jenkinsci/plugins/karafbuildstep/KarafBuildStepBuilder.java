package org.jenkinsci.plugins.karafbuildstep;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
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

	private boolean useCustomKaraf;
	private String karafHome;
	private String flags;
	private String script;

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public KarafBuildStepBuilder(boolean useCustomKaraf, String karafHome, String flags, String script) {
		this.useCustomKaraf = useCustomKaraf;
		this.karafHome = karafHome;
		this.flags = flags;
		this.script = script;
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

	public String getScript() {
		return script;
	}

	@Override
	public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		try{
			// Get Karaf Home
			String karafHome = getUseCustomKaraf() ? getKarafHome() : getDescriptor().getDefaultKarafHome();

			if(!karafHome.isEmpty()) {
				// Construct Command
				listener.getLogger().println(getScript());

				// Initialize command
				StringBuilder command = new StringBuilder();
				command.append(karafHome);

				// Select Karaf client according to the OS
				if(SystemUtils.IS_OS_LINUX) {
					command.append("/bin/client");
				} else if (SystemUtils.IS_OS_WINDOWS) {
					command.append("/bin/client.bat");
				}

				// Add flags
				String flags = getFlags();
				if(!flags.isEmpty()) {
					command.append(" " + getFlags().trim());
				}

				// Add script file
				command.append(" -f " + getScript().trim());

				// Launch Command
				Proc proc = launcher.launch(command.toString(), build.getEnvironment(listener), listener.getLogger(), workspace);

				// Exit Code
				int exitCode = proc.join();

				// Error Handling and Build Result
				if(exitCode == 0) {
					build.setResult(Result.SUCCESS);
				} else {
					build.setResult(Result.FAILURE);
				}
			}else{
				listener.getLogger().println("ERROR: KARAF_HOME is not specified!");
			}
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
			listener.getLogger().print(e.getMessage());
		}
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		private String defaultKarafHome;

		public DescriptorImpl() {
			load();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		public String getDisplayName() {
			return "Execute Karaf script";
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

		private String getDefaultKarafHome() {
			return defaultKarafHome;
		}

		public FormValidation doCheckKarafHome(@QueryParameter String value, @QueryParameter boolean useCustomKaraf) {

			if(useCustomKaraf && value.isEmpty()) {
				return FormValidation.warning("This field should not be empty");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckScript(@QueryParameter String value) {

			if(value.isEmpty()) {
				return FormValidation.error("This field should not be empty");
			}

			return FormValidation.ok();
		}
	}
}
