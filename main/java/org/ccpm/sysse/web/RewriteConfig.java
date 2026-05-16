package org.ccpm.sysse.web;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.rule.Join;
import jakarta.servlet.ServletContext;

@RewriteConfiguration
public class RewriteConfig extends HttpConfigurationProvider {
    @Override
    public Configuration getConfiguration(ServletContext context) {
        ConfigurationBuilder builder = ConfigurationBuilder.begin()
            .addRule(Join.path("/").to("/index.xhtml"));

        builder.addRule(Join.path("/{p}").to("/{p}.xhtml"))
            .where("p").matches("[a-zA-Z0-9_-]+");

        return builder;
    }

    @Override
    public int priority() {
        return 10;
    }
}
