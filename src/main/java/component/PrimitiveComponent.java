package component;

import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public abstract class PrimitiveComponent {
    protected ByteBuffer buffer;

    public PrimitiveComponent(int size) {
        this.buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
}
