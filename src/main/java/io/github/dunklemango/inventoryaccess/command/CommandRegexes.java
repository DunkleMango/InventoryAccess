package io.github.dunklemango.inventoryaccess.command;

public enum CommandRegexes {
    REGEX_SPLIT_QUOTES_OR_WHITESPACE("(\"[^\"]*\")|(\\S+)");

    public final String regex;

    private CommandRegexes(String regex) {
        this.regex = regex;
    }
}
