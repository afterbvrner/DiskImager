package component;

import exception.InternalImageError;
import generator.ShortName;
import generator.ShortNameGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Entry extends PrimitiveComponent {
    public Entry(File file) {
        super(32);
        setup(file);
    }

    private void setup(File file) {
        ShortNameGenerator generator = new ShortNameGenerator(Collections.emptySet());
        ShortName name = generator.generateShortName(file.getName());
        // Filename
        buffer.put(name.asSimpleString().getBytes(StandardCharsets.US_ASCII));
        // Privileges
        buffer.put(11, checkPrivileges(file));
        // Dates
        try {
            setDates(file);
        } catch (IOException e) {
            throw new InternalImageError("Cannot get file attributes");
        }
        // File length
        buffer.putInt(28, (int) file.length());
    }

    private void setDates(File file) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(
                Path.of(file.getPath()),
                BasicFileAttributes.class
        );
        // Creation date
        Calendar creationTime = Calendar.getInstance();
        creationTime.setTime(new Date(attributes.creationTime().toMillis()));
        buffer.put(13,
                (byte) (creationTime.get(Calendar.MILLISECOND) + creationTime.get(Calendar.MINUTE) % 2)
        );
        buffer.putLong(14, calendarToFat(creationTime));
        // Last access
        Calendar lastAccess = Calendar.getInstance();
        lastAccess.setTime(new Date(attributes.lastAccessTime().toMillis()));
        buffer.position(18);
        buffer.put(dateToBytes(lastAccess), 0, 2);
        // Last record date
        Calendar lastRecord = Calendar.getInstance();
        lastRecord.setTime(new Date(attributes.lastAccessTime().toMillis()));
        buffer.position(24);
        buffer.putLong(22, calendarToFat(lastRecord));
    }

    public void setCluster(int cluster) {
        buffer.putShort(20, (short) cluster);
    }

    private byte checkPrivileges(File file) {
        if (file.isDirectory())
            return 0x10;
        if (file.isHidden())
            return 0x02;
        if (file.canRead() && !file.canWrite())
            return 0x01;
        return 0;
    }

    private long calendarToFat(Calendar calendar) {
        return ((calendar.get(Calendar.YEAR) - 1980) << 25) |
                ((calendar.get(Calendar.MONTH) + 1) << 21) |
                (calendar.get(Calendar.DAY_OF_MONTH) << 16) |
                (calendar.get(Calendar.HOUR) << 11) |
                (calendar.get(Calendar.MINUTE) << 5) |
                (calendar.get(Calendar.MINUTE) >> 1);
    }

    private byte[] dateToBytes(Calendar calendar) {
        return new byte[]{
                (byte) (calendar.get(Calendar.DAY_OF_MONTH) & (byte) 0x0F +
                        calendar.get(Calendar.MONTH)),
                (byte) ((byte) calendar.get(Calendar.YEAR) - 1980)
        };
    }
}
