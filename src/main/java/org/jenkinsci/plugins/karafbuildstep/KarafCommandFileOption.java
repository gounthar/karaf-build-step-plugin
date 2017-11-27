package org.jenkinsci.plugins.karafbuildstep;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
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

            // Add file
            command.append(" -f " + getFile().trim());

            // Debug
            listener.getLogger().println(getFile());

            // Launch Command
            LOGGER.info("Running shell/batch command: {}", command.toString());
            Proc proc = launcher.launch(command.toString(), build.getEnvironment(listener), listener.getLogger(), workspace);

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