package creator;

import component.BootSector;
import component.ClusterChain;
import component.PrimitiveComponent;
import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;

public class Fat32Creator extends PrimitiveComponent {
    private final BootSector bootSector;
    private final ClusterChain clusterChain;

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
        byte[] allocation = clusterChain.getAllocationTable().toBytes();
        byte[] clusters = clusterChain.toBytes();
        byte[] fat = ArrayUtils.addAll(
                ArrayUtils.addAll(
                        boot, allocation),
                clusters);
        File file = new File(filename);
        if(!file.createNewFile())
            throw new IOException("File creation error");
        FileUtils.writeByteArrayToFile(file, fat);
    }
}
