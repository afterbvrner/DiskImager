package generator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public final class ShortName {

    public final static Charset ASCII = StandardCharsets.US_ASCII;

    private final static byte[] ILLEGAL_CHARS = {
            0x22, 0x2A, 0x2B, 0x2C, 0x2E, 0x2F, 0x3A, 0x3B,
            0x3C, 0x3D, 0x3E, 0x3F, 0x5B, 0x5C, 0x5D, 0x7C
    };

    private final static byte ASCII_SPACE = 0x20;

    public final static ShortName DOT = new ShortName(".", "");

    public final static ShortName DOT_DOT = new ShortName("..", "");

    private final byte[] nameBytes;

    private ShortName(String nameExt) {
        if (nameExt.length() > 12) throw
                new IllegalArgumentException("name too long");

        final int i = nameExt.indexOf('.');
        final String nameString, extString;

        if (i < 0) {
            nameString = nameExt.toUpperCase(Locale.ROOT);
            extString = "";
        } else {
            nameString = nameExt.substring(0, i).toUpperCase(Locale.ROOT);
            extString = nameExt.substring(i + 1).toUpperCase(Locale.ROOT);
        }

        this.nameBytes = toCharArray(nameString, extString);
        checkValidChars(nameBytes);
    }

    ShortName(String name, String ext) {
        this.nameBytes = toCharArray(name, ext);
    }

    private static byte[] toCharArray(String name, String ext) {
        checkValidName(name);
        checkValidExt(ext);

        final byte[] result = new byte[11];
        Arrays.fill(result, ASCII_SPACE);
        System.arraycopy(name.getBytes(ASCII), 0, result, 0, name.length());
        System.arraycopy(ext.getBytes(ASCII), 0, result, 8, ext.length());

        return result;
    }

    public byte checkSum() {
        final byte[] dest = new byte[11];
        System.arraycopy(nameBytes, 0, dest, 0, 11);

        int sum = dest[0];
        for (int i = 1; i < 11; i++) {
            sum = dest[i] + (((sum & 1) << 7) + ((sum & 0xfe) >> 1));
        }

        return (byte) (sum & 0xff);
    }

    public static ShortName get(String name) throws IllegalArgumentException {
        if (name.equals(".")) return DOT;
        else if (name.equals("..")) return DOT_DOT;
        else return new ShortName(name);
    }

    public static boolean canConvert(String nameExt) {
        try {
            ShortName.get(nameExt);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
//
//    public static ShortName parse(byte[] data) {
//        final char[] nameArr = new char[8];
//
//        for (int i = 0; i < nameArr.length; i++) {
//            nameArr[i] = (char) LittleEndian.getUInt8(data, i);
//        }
//
//        if (LittleEndian.getUInt8(data, 0) == 0x05) {
//            nameArr[0] = (char) 0xe5;
//        }
//
//        final char[] extArr = new char[3];
//        for (int i = 0; i < extArr.length; i++) {
//            extArr[i] = (char) LittleEndian.getUInt8(data, 0x08 + i);
//        }
//
//        return new ShortName(
//                new String(nameArr).trim(),
//                new String(extArr).trim());
//    }

    public void write(byte[] dest) {
        System.arraycopy(nameBytes, 0, dest, 0, nameBytes.length);
    }

    public String asSimpleString() {
        final String name = new String(this.nameBytes, 0, 8, ASCII).trim();
        final String ext = new String(this.nameBytes, 8, 3, ASCII).trim();

        return ext.isEmpty() ? name : name + "." + ext;
    }

    @Override
    public String toString() {
        return "ShortName [" + asSimpleString() + "]"; //NOI18N
    }

    private static void checkValidName(String name) {
        checkString(name, "name", 1, 8);
    }

    private static void checkValidExt(String ext) {
        checkString(ext, "extension", 0, 3);
    }

    private static void checkString(String str, String strType,
                                    int minLength, int maxLength) {

        if (str == null)
            throw new IllegalArgumentException(strType +
                    " is null");
        if (str.length() < minLength)
            throw new IllegalArgumentException(strType +
                    " must have at least " + minLength +
                    " characters: " + str);
        if (str.length() > maxLength)
            throw new IllegalArgumentException(strType +
                    " has more than " + maxLength +
                    " characters: " + str);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShortName)) {
            return false;
        }

        final ShortName other = (ShortName) obj;
        return Arrays.equals(nameBytes, other.nameBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.nameBytes);
    }

    public static void checkValidChars(byte[] chars)
            throws IllegalArgumentException {

        if (chars[0] == 0x20) throw new IllegalArgumentException(
                "0x20 can not be the first character");

        for (int i=0; i < chars.length; i++) {
            if ((chars[i] & 0xff) != chars[i]) throw new
                    IllegalArgumentException("multi-byte character at " + i);

            final byte toTest = (byte) (chars[i] & 0xff);

            if (toTest < 0x20 && toTest != 0x05) throw new
                    IllegalArgumentException("caracter < 0x20 at" + i);

            for (byte illegalChar : ILLEGAL_CHARS) {
                if (toTest == illegalChar) throw new
                        IllegalArgumentException("illegal character " +
                        illegalChar + " at " + i);
            }
        }
    }
}
