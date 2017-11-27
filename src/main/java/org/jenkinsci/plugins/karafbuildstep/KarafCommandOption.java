package org.jenkinsci.plugins.karafbuildstep;

import java.io.Serializable;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;


/**
 * KarafCommandOption
 *
 * @author <a href="mailto:jgalego1990@gmail.com">João Galego</a>
 */
public abstract class KarafCommandOption implements Describable<KarafCommandOption>, Serializable {

    private static final long serialVersionUID = 1L;

    public abstract int execute(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, KarafBuildStepBuilder karafStep)
            throws KarafCommandException;

    @Override
    @SuppressWarnings({ "unchecked" })
    public Descriptor<KarafCommandOption> getDescriptor() {
        try {
            return Jenkins.getInstance().getDescriptorOrDie(getClass());
        }
        catch (NullPointerException ex) {
            throw ex;
        }
    }
}