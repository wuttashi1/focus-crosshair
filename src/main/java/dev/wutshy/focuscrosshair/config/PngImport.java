package dev.wutshy.focuscrosshair.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PngImport {
    public static final int MAX_BYTES = 1_048_576;
    public static final int MAX_DIMENSION = 512;

    private PngImport() {}

    public static byte[] read(Path path) throws IOException {
        if (!Files.isRegularFile(path) || Files.size(path) > MAX_BYTES) throw new IOException("PNG file exceeds 1 MiB or is not a regular file");
        byte[] bytes;
        try (var input = Files.newInputStream(path)) { bytes = input.readNBytes(MAX_BYTES + 1); }
        validate(bytes);
        return bytes;
    }

    public static void validate(byte[] bytes) throws IOException {
        if (bytes.length < 33 || bytes.length > MAX_BYTES) throw new IOException("Invalid PNG size");
        ByteBuffer header = ByteBuffer.wrap(bytes);
        if (header.getLong() != 0x89504E470D0A1A0AL || header.getInt() != 13 || header.getInt() != 0x49484452)
            throw new IOException("Expected a PNG image");
        int width = header.getInt(), height = header.getInt();
        if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION)
            throw new IOException("PNG dimensions must be between 1 and 512 pixels");
    }
}
