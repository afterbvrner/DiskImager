package component;

import lombok.Getter;
import lombok.Setter;

public class Sector extends PrimitiveComponent {
    @Getter
    @Setter
    private int size;

    public Sector(int size) {
        super(size);
        this.size = size;
    }

    public void put(byte[] data) {
        buffer.put(data);
    }
}
