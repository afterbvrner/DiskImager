package component;

import exception.NotEnoughSpaceException;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

@Data
public class AllocationTable {
    int size;
    int[] pointers;

    public AllocationTable(int size) {
        this.size = size;
        pointers = new int[size];
        Arrays.fill(pointers, 0);
        pointers[0] = 0x0FFFFFF8;
        pointers[1] = 0x0FFFFFFF;
    }

    public void setNext(int current, int next) {
        pointers[current] = next;
    }

    public int getFirstFreeCluster() {
        return IntStream
                .range(0, pointers.length)
                .filter(i -> pointers[i]==0)
                .findFirst()
                .orElseThrow(NotEnoughSpaceException::new);
    }

    public int getSecondFree() {
        return IntStream
                .range(0, pointers.length)
                .filter(i -> pointers[i]==0)
                .skip(1)
                .findFirst()
                .orElseThrow(NotEnoughSpaceException::new);
    }

    public byte[] toBytes() {
        ByteBuffer bytes = ByteBuffer.allocate(pointers.length * 4);
        Arrays.stream(pointers).forEach(bytes::putInt);
        return bytes.array();
    }
}
