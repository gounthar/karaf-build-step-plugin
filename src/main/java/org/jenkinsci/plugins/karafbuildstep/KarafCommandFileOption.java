package org.jenkinsci.plugins.karafbuildstep;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
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
 * KarafCommandFileOption
 *
 * @author <a href="mailto:jgalego1990@gmail.com">João Galego</a>
 */
public class KarafCommandFileOption extends KarafCommandOption {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(KarafCommandFileOption.class);

    public final String file;

    @DataBoundConstructor
    public KarafCommandFileOption(String file) {
        super();
        this.file = file;
    }

    public String getFile() {

        return file;
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
            LOGGER.debug("Setting Karaf home to '{}'", mKarafHome);

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

            LOGGER.debug("Setting Karaf client to '{}'", mKarafClient);
            cmd.append(mKarafClient);

            // Set flags
            String mKarafFlags;
            if (!karafStep.getFlags().isEmpty()) {
                mKarafFlags = karafStep.getFlags().trim();
            }
            else {
                mKarafFlags = "";
            }

            LOGGER.debug("Using flags '{}'", mKarafFlags);
            cmd.append(" " + mKarafFlags);

            // Set file option
            String mKarafFile = getFile().trim();
            cmd.append(" -f " + mKarafFile);

            // Launch command
            LOGGER.info("Running karaf command (client: {}, flags: {}, file: {})", mKarafClient, mKarafFlags, mKarafFile);
            ProcStarter ps = launcher.new ProcStarter();
            ps.cmdAsSingleString(cmd.toString()).stdout(listener);
            Proc proc = launcher.launch(ps);

            return proc.join();
        }
        catch (IOException | InterruptedException ex) {

            throw new KarafCommandException(ex);
        }
    }

    @Extension
    public static final class DescriptorImpl extends KarafCommandOptionDescriptor {

        @Override
        public String getDisplayName() {

            return "Execute From File";
        }
    }
}