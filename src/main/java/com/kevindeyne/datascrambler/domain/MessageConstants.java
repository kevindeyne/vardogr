package com.kevindeyne.datascrambler.domain;

public class MessageConstants {

    private MessageConstants() {}

    public static final String MSG_CONFIG_FOUND = "Config file found.";
    public static final String MSG_CONFIG_NOT_FOUND = "The model is built on the production data. Provide read-only credentials. No production data is stored. Some metadata queries will be run.";

    public static final String MSG_BUILD_COMPLETED = "Model created. You can now use this model to generate data. Do this by calling generate";

    public static final String MSG_DIST_FOUND = "Distribution model found";
    public static final String MSG_DIST_NOT_FOUND = "No distribution model found";
    public static final String MSG_DIST_REQUIRED = "Please generate a distribution model first by running 'build'";

    public static final String MSG_GEN_COMPLETED = "Model successfully applied to target database";
}
