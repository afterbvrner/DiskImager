package component;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public abstract class PrimitiveComponent {
    protected ByteBuffer buffer;

    public PrimitiveComponent(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }
}
