package generator;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public final class ShortNameGenerator {

    private final Set<String> usedNames;

    public ShortNameGenerator(Set<String> usedNames) {
        this.usedNames = Collections.unmodifiableSet(usedNames);
    }

    public static boolean validChar(char toTest) {
        if (toTest >= 'A' && toTest <= 'Z') return true;
        if (toTest >= '0' && toTest <= '9') return true;

        return (toTest == '_' || toTest == '^' || toTest == '$' ||
                toTest == '~' || toTest == '!' || toTest == '#' ||
                toTest == '%' || toTest == '&' || toTest == '-' ||
                toTest == '{' || toTest == '}' || toTest == '(' ||
                toTest == ')' || toTest == '@' || toTest == '\''||
                toTest == '`');
    }

    public static boolean isSkipChar(char c) {
        return (c == '.') || (c == ' ');
    }

    private String tidyString(String dirty) {
        final StringBuilder result = new StringBuilder();
        for (int src=0; src < dirty.length(); src++) {
            final char toTest = Character.toUpperCase(dirty.charAt(src));
            if (isSkipChar(toTest)) continue;

            if (validChar(toTest)) {
                result.append(toTest);
            } else {
                result.append('_');
            }
        }

        return result.toString();
    }

    private boolean cleanString(String s) {
        for (int i=0; i < s.length(); i++) {
            if (isSkipChar(s.charAt(i))) return false;
            if (!validChar(s.charAt(i))) return false;
        }

        return true;
    }

    private String stripLeadingPeriods(String str) {
        final StringBuilder sb = new StringBuilder(str.length());

        for (int i=0; i < str.length(); i++) {
            if (str.charAt(i) != '.') { //NOI18N
                sb.append(str.substring(i));
                break;
            }
        }

        return sb.toString();
    }

    public ShortName generateShortName(String longFullName)
            throws IllegalStateException {

        longFullName =
                stripLeadingPeriods(longFullName).toUpperCase(Locale.ROOT);

        final String longName;
        final String longExt;
        final int dotIdx = longFullName.lastIndexOf('.');
        final boolean forceSuffix;

        if (dotIdx == -1) {
            /* no dot in the name */
            forceSuffix = !cleanString(longFullName);
            longName = tidyString(longFullName);
            longExt = ""; /* so no extension */
        } else {
            /* split at the dot */
            forceSuffix = !cleanString(longFullName.substring(
                    0, dotIdx));
            longName = tidyString(longFullName.substring(0, dotIdx));
            longExt = tidyString(longFullName.substring(dotIdx + 1));
        }

        final String shortExt = (longExt.length() > 3) ?
                longExt.substring(0, 3) : longExt;

        if (forceSuffix || (longName.length() > 8) ||
                usedNames.contains(new ShortName(longName, shortExt).
                        asSimpleString().toLowerCase(Locale.ROOT))) {

            /* we have to append the "~n" suffix */

            final int maxLongIdx = Math.min(longName.length(), 8);

            for (int i=1; i < 99999; i++) {
                final String serial = "~" + i; //NOI18N
                final int serialLen = serial.length();
                final String shortName = longName.substring(
                        0, Math.min(maxLongIdx, 8-serialLen)) + serial;
                final ShortName result = new ShortName(shortName, shortExt);

                if (!usedNames.contains(
                        result.asSimpleString().toLowerCase(Locale.ROOT))) {

                    return result;
                }
            }

            throw new IllegalStateException(
                    "could not generate short name for \""
                            + longFullName + "\"");
        }

        return new ShortName(longName, shortExt);
    }

}
