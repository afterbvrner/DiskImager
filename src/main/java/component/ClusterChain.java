package component;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ClusterChain {
    private List<Cluster> clusters;
    private AllocationTable allocationTable;
    private int sectorSize;
    private int clusterSize;

    @Builder
    public ClusterChain(int amount, int sectorSize, int clusterSize) {
        clusters = new LinkedList<>(Collections.nCopies(amount, new Cluster(clusterSize, sectorSize)));
        allocationTable = new AllocationTable(amount);
        this.sectorSize = sectorSize;
        this.clusterSize = clusterSize;
    }

    public void addData(byte[] data) {
        byte[][] sectorBytes = cutArray(data);
        List<Sector> sectors = Arrays
                .stream(sectorBytes)
                .map(sectorByteArray -> {
                    Sector sect = new Sector(sectorSize);
                    sect.put(sectorByteArray);
                    return sect;
                })
                .collect(Collectors.toList());
        if(sectors.size() <= clusterSize) {
            Cluster cluster = new Cluster(clusterSize, sectorSize);
            cluster.setSectors(sectors);
            clusters.set(allocationTable.getFirstFreeCluster(), cluster);
        } else {
            List<Cluster> chain = new LinkedList<>();
            int offset = 0;
            while (offset + clusterSize <= sectors.size()) {
                Cluster cluster = new Cluster(clusterSize, sectorSize);
                cluster.setSectors(sectors.subList(offset, offset + clusterSize));
                chain.add(cluster);
                offset += clusterSize;
            }
            for(int i = 0; i < chain.size(); i++) {
                if(i != 0)
                    allocationTable.setNext(
                            allocationTable.getFirstFreeCluster(),
                            allocationTable.getSecondFree()
                    );
                clusters.set(allocationTable.getFirstFreeCluster(), chain.get(i));
            }
        }
        allocationTable.setNext(allocationTable.getFirstFreeCluster(), 0x0FFFFFFF);
    }

    private byte[][] cutArray(byte[] data) {
        int rest = data.length % sectorSize;
        int chunks = data.length / sectorSize + (rest > 0 ? 1 : 0);
        byte[][] sectorBytes = new byte[chunks][];
        for(int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++){
            sectorBytes[i] = Arrays.copyOfRange(data, i * sectorSize, i * sectorSize + sectorSize);
        }
        if (rest > 0)
            sectorBytes[chunks - 1] = Arrays.copyOfRange(
                    data,
                    (chunks - 1) * sectorSize,
                    (chunks - 1) * sectorSize + rest);
        return sectorBytes;
    }

    public byte[] toBytes() {
        byte[] finalBytes = new byte[]{};
        for (Cluster cluster : clusters) {
            finalBytes = ArrayUtils.addAll(finalBytes, cluster.toBytes());
        }
        return finalBytes;
    }
}
