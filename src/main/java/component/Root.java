package component;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Root {
    private final List<Entry> entries;

    public Root(List<File> files) {
        entries = files.stream()
                .map(Entry::new)
                .collect(Collectors.toList());
    }

    public byte[] toBytes() {
        byte[] finalArray;
        finalArray = new byte[]{};
        for(var entry : entries)
            finalArray = ArrayUtils.addAll(finalArray, entry.getBuffer().array());
        return finalArray;
    }

    public long fullSize() {
        return entries.stream()
                .mapToLong(
                        entry -> entry.getBuffer().array().length)
                .sum();
    }

    public void setCluster(int entry, int cluster) {
        entries.get(entry).setCluster(cluster);
    }
}
