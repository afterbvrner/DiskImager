package component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BootSector extends Sector {
    public static final String BS_OEMName = "FATSviat";

    // два последних байта загрузочного сектора
    public static final int FIRST_LAST_BYTE = 0x55;
    public static final int SECOND_LAST_BYTE = 0xaa;

    public BootSector() {
        super(512);
        // три магических байта
        buffer.put(0x00, (byte) 0xeb);
        buffer.put(0x01, (byte) 0x3c);
        buffer.put(0x02, (byte) 0x90);
        // 8 символов, название производителя
        buffer.position(3);
        buffer.put(BS_OEMName.getBytes(), 0, 8);
        // количество байт на сектор
        buffer.position(11);
        ByteBuffer b = ByteBuffer.allocate(2);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putShort((short) 512);
        byte[] bytesPerSec = b.array();
        buffer.put(bytesPerSec, 0, 2);
        // два магических байта
        byte[] barr = new byte[1];
        buffer.position(510);
        barr[0] = (byte) FIRST_LAST_BYTE;
        buffer.put(barr, 0, 1);
        buffer.position(511);
        barr[0] = (byte) SECOND_LAST_BYTE;
        buffer.put(barr, 0, 1);
    }
}
