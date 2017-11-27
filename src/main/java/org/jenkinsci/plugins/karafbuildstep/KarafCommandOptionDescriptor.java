package org.jenkinsci.plugins.karafbuildstep;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * KarafCommandOptionDescriptor
 *
 * @author <a href="mailto:jgalego1990@gmail.com">João Galego</a>
 */
public abstract class KarafCommandOptionDescriptor extends Descriptor<KarafCommandOption> {

    public static DescriptorExtensionList<KarafCommandOption, KarafCommandOptionDescriptor> all() {

        try {
            return Jenkins.getInstance().getDescriptorList(KarafCommandOption.class);
        }
        catch (NullPointerException ex) {
            throw ex;
        }
    }

}