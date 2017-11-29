package org.jenkinsci.plugins.karafbuildstep;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
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

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(KarafCommandScriptOption.class);

    // Form parameters
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

    @Override
    public int execute(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, KarafBuildStepBuilder karafStep)
            throws KarafCommandException {

        try {
            // Initialize command
            StringBuilder cmd = new StringBuilder();

            // Set karaf home
            // Use the default karaf home if 'Use Custom Karaf' is unchecked
            String mKarafHome = karafStep.getUseCustomKaraf() ? karafStep.getKarafHome() : karafStep.getDescriptor().getDefaultKarafHome();
            LOGGER.debug("Setting Karaf home to {}", mKarafHome);

            // Set karaf client
            // Assuming that the client is placed inside the '${KARAF_HOME}/bin'
            // folder
            String mKarafClient;
            if (launcher.isUnix()) {
                mKarafClient = mKarafHome + "/bin/client";
            }
            else {
                mKarafClient = mKarafHome + "/bin/client.bat";
            }

            LOGGER.debug("Setting Karaf client to {}", mKarafClient);
            cmd.append(mKarafClient);

            // Set flags
            String mKarafFlags;
            if (!karafStep.getFlags().isEmpty()) {
                mKarafFlags = " " + karafStep.getFlags().trim();
            }
            else {
                mKarafFlags = "";
            }

            LOGGER.debug("Using flags '{}'", mKarafFlags);
            cmd.append(mKarafFlags);

            // Create a temp file with the script contents
            FilePath mFile = workspace.createTextTempFile("karaf_script", null, getScript());
            LOGGER.debug("Temporary file with the script contents successfully created");

            // Set file option
            String mKarafFile = mFile.getParent() + "/" + mFile.getName();
            cmd.append(" -f " + mKarafFile);

            // Launch command
            LOGGER.info("Running karaf command (client: {}, flags: {}, file: {})", mKarafClient, mKarafFlags, mKarafFile);
            ProcStarter ps = launcher.new ProcStarter();
            ps.cmdAsSingleString(cmd.toString()).stdout(listener);
            Proc proc = launcher.launch(ps);

            // Get exit code
            int exitCode = proc.join();

            // Delete temp file
            mFile.delete();
            LOGGER.debug("Temporarary file with the script contents successfully deleted");

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