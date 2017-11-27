package org.jenkinsci.plugins.karafbuildstep;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * KarafCommandScriptOption
 *
 * @author <a href="mailto:jgalego1990@gmail.com">João Galego</a>
 */
public class KarafCommandScriptOption extends KarafCommandOption {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(KarafCommandScriptOption.class);

    private String script;

    @DataBoundConstructor
    public KarafCommandScriptOption(String script) {
        super();
        this.script = script;
    }

    public String getScript() {

        return script;
    }

    @DataBoundSetter
    public void setScript(String script) {

        this.script = script;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int execute(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, KarafBuildStepBuilder karafStep)
            throws KarafCommandException {

        try {
            // Get Karaf home
            String mKarafHome = karafStep.getUseCustomKaraf() ? karafStep.getKarafHome() : karafStep.getDescriptor().getDefaultKarafHome();
            LOGGER.info("Setting Karaf home to {}", mKarafHome);

            // Initialize command
            StringBuilder command = new StringBuilder();
            command.append(mKarafHome);

            // Select Karaf client according to the OS
            if (launcher.isUnix()) {
                command.append("/bin/client");
            }
            else {
                command.append("/bin/client.bat");
            }

            // Add flags
            if (!karafStep.getFlags().isEmpty()) {
                command.append(" " + karafStep.getFlags().trim());
            }

            // Create temp file with script contents
            FilePath mFile = workspace.createTextTempFile("karaf_script", null, getScript());

            // Add file
            command.append(" -f " + mFile.getParent() + "/" + mFile.getName());

            // Launch Command
            LOGGER.info("Running shell/batch command: {}", command.toString());
            Proc proc = launcher.launch(command.toString(), build.getEnvironment(listener), listener.getLogger(), workspace);

            // Exit Code
            int exitCode = proc.join();

            // Delete temp file
            mFile.delete();

            return exitCode;
        }
        catch (IOException | InterruptedException ex) {

            throw new KarafCommandException(ex);
        }
    }

    @Extension
    public static final class DescriptorImpl extends KarafCommandOptionDescriptor {

        @Override
        public String getDisplayName() {

            return "Execute From Script";
        }
    }
}