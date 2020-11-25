import creator.Fat32Creator;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Fat32Creator creator = Fat32Creator
                .builder()
                .clusterSize(2)
                .sectorSize(512)
                .size(4096)
                .build();
        byte [] bytes = new byte[] {
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
                (byte) 0x05,
                (byte) 0x06,
                (byte) 0x07,
                (byte) 0x08,
        };
        creator.addData(bytes);
        creator.assembly("test.img");
    }
}
