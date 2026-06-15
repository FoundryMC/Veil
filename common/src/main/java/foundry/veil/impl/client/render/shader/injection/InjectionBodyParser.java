package foundry.veil.impl.client.render.shader.injection;

import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
public final class InjectionBodyParser {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*#version\\s+(\\d+)\\s*.*$", Pattern.MULTILINE);

    public static Result parse(String code) {
        int version = parseVersion(code);
        String trimmed = code.trim();
        int funcStart = -1;
        boolean isHead = true;
        int bracePos = -1;

        if (trimmed.startsWith("void tail(")) {
            funcStart = 0;
            isHead = false;
            bracePos = trimmed.indexOf('{');
        } else if (trimmed.startsWith("void head(")) {
            funcStart = 0;
            bracePos = trimmed.indexOf('{');
        } else {
            int idx = trimmed.indexOf("\nvoid tail(");
            if (idx >= 0) {
                funcStart = idx + 1;
                isHead = false;
                bracePos = trimmed.indexOf('{', funcStart);
            } else {
                idx = trimmed.indexOf("\nvoid head(");
                if (idx >= 0) {
                    funcStart = idx + 1;
                    bracePos = trimmed.indexOf('{', funcStart);
                }
            }
        }

        if (bracePos < 0) {
            return new Result("", "", true, version);
        }

        int depth = 0;
        int bodyStart = -1;
        for (int i = bracePos; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            int skip = skipIfCommentOrString(trimmed, i);
            if (skip >= 0) {
                i = skip;
                continue;
            }
            if (c == '{') {
                if (depth == 0) bodyStart = i + 1;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    String bodyStr = trimmed.substring(bodyStart, i).trim();
                    String rawGlobals = trimmed.substring(0, funcStart) + trimmed.substring(i + 1);
                    String globalsStr = stripNonCode(rawGlobals);
                    return new Result(bodyStr, globalsStr, isHead, version);
                }
            }
        }

        return new Result("", "", true, version);
    }

    private static int skipString(CharSequence s, int pos) {
        for (int i = pos + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') i++;
            else if (c == '"') return i;
        }
        return s.length() - 1;
    }

    private static int skipLineComment(CharSequence s, int pos) {
        for (int i = pos + 2; i < s.length(); i++) {
            if (s.charAt(i) == '\n') return i;
        }
        return s.length() - 1;
    }

    private static int skipBlockComment(CharSequence s, int pos) {
        for (int i = pos + 2; i < s.length() - 1; i++) {
            if (s.charAt(i) == '*' && s.charAt(i + 1) == '/') return i + 1;
        }
        return s.length() - 1;
    }

    private static int parseVersion(String code) {
        Matcher m = VERSION_PATTERN.matcher(code);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static String stripNonCode(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int skip = skipIfCommentOrString(s, i);
            if (skip >= 0) {
                if (s.charAt(i) == '"') out.append(s, i, skip + 1);
                i = skip;
                continue;
            }
            out.append(s.charAt(i));
        }
        return out.toString().trim();
    }

    private static int skipIfCommentOrString(CharSequence s, int i) {
        char c = s.charAt(i);
        if (c == '"') return skipString(s, i);
        if (c == '/' && i + 1 < s.length()) {
            char n = s.charAt(i + 1);
            if (n == '/') return skipLineComment(s, i);
            if (n == '*') return skipBlockComment(s, i);
        }
        return -1;
    }

    public record Result(String body, String globals, boolean isHead, int version) {
    }
}
