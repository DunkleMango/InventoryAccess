package com.github.dunklemango.inventoryaccess.command;

public enum CommandRegex {
    REGEX_SPLIT_QUOTES_OR_WHITESPACE("(\"[^\"]*\")|(\\S+)");

    public final String regex;

    private CommandRegex(String regex) {
        this.regex = regex;
    }
}
