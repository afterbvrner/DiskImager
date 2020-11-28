package creator;

import component.BootSector;
import component.ClusterChain;
import component.PrimitiveComponent;
import component.Root;
import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class Fat32Creator extends PrimitiveComponent {
    private final BootSector bootSector;
    private Root root;
    private final ClusterChain clusterChain;
    private final Logger log = Logger.getLogger(Fat32Creator.class);
    private boolean isReady = false;
    int size;
    int sectorSize;
    int clusterSize;

    @Builder
    public Fat32Creator(int size, int sectorSize, int clusterSize) {
        super(size);
        clusterChain = ClusterChain.builder()
                .amount(size / (sectorSize * clusterSize))
                .sectorSize(sectorSize)
                .clusterSize(clusterSize)
                .build();
        bootSector = new BootSector();
        this.size = size;
        this.sectorSize = sectorSize;
        this.clusterSize = clusterSize;
    }

    // TODO: Двойная запись директории
    public void addFiles(List<File> files) throws IOException {
        root = new Root(files);
        long rootClusterAmount = root.fullSize() / (sectorSize * clusterSize) + 1;
        byte[] rootBytes = root.toBytes();
        clusterChain.addData(rootBytes);
        for (int i = 0; i < files.size(); i++) {
            root.setCluster(i,
                    clusterChain.addData(
                            FileUtils.readFileToByteArray(files.get(i))
                    ));
        }
        IntStream
                .range(0, (int) rootClusterAmount)
                .forEach(clusterChain::clear);
        clusterChain.addData(root.toBytes());
    }

    public void assembly(String filename) throws IOException {
        byte[] boot = bootSector.getBuffer().array();
        log.info("Boot sector created");
        // TODO: Пустые сектора
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
