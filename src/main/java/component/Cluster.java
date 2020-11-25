package component;

import exception.InternalImageError;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

@Data
public class Cluster {
    private int size;
    private int sectorSize;
    private List<Sector> sectors;

    public Cluster(int size, int sectorSize) {
        this.sectorSize = sectorSize;
        this.size = size;
    }

    public void setSectors(List<Sector> sectors) {
        if(sectors.size() > size)
            throw new InternalImageError("Wrong cluster size");
        this.sectors = sectors;
        while(this.sectors.size() != size)
            this.sectors.add(new Sector(sectorSize));
    }

    public byte[] toBytes() {
        byte[] finalArray;
        if(sectors == null) {
            finalArray = new byte[sectorSize * size];
            Arrays.fill(finalArray, (byte) 0x00);
            return finalArray;
        }
        finalArray = new byte[]{};
        for(var sector : sectors)
            finalArray = ArrayUtils.addAll(finalArray, sector.getBuffer().array());
        return finalArray;
    }
}
