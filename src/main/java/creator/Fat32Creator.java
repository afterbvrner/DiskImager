package creator;

import component.BootSector;
import component.ClusterChain;
import component.PrimitiveComponent;
import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Fat32Creator extends PrimitiveComponent {
    private final BootSector bootSector;
    private final ClusterChain clusterChain;

    private final Logger log = Logger.getLogger(Fat32Creator.class);

    @Builder
    public Fat32Creator(int size, int sectorSize, int clusterSize) {
        super(size);
        clusterChain = ClusterChain.builder()
                .amount(size / (sectorSize * clusterSize))
                .sectorSize(sectorSize)
                .clusterSize(clusterSize)
                .build();
        bootSector = new BootSector();
    }

    public void addData(byte[] data) {
        clusterChain.addData(data);
    }

    public void assembly(String filename) throws IOException {
        byte[] boot = bootSector.getBuffer().array();
        log.info("Boot sector created");
        byte[] allocation = clusterChain.getAllocationTable().toBytes();
        log.info("Allocation table loaded");
        log.info("Converting cluster chain...");
        byte[] clusters = clusterChain.toBytes();
        log.info("Cluster chain created");
        byte[] fat = ArrayUtils.addAll(
                ArrayUtils.addAll(boot, allocation),
                clusters);
        log.info("Creating file \"" + filename + "\"");
        File file = new File(filename);
        if(!file.createNewFile())
            throw new IOException("File creation error");
        FileUtils.writeByteArrayToFile(file, fat);
        log.info("Successfully created disk image \"" + filename + "\"");
    }
}
