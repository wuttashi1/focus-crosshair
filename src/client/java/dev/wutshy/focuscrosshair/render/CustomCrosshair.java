package dev.wutshy.focuscrosshair.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.wutshy.focuscrosshair.config.PngImport;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.LoggerFactory;

public final class CustomCrosshair {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("focuscrosshair", "custom");
    private final Path saved = FabricLoader.getInstance().getConfigDir().resolve("focuscrosshair/custom.png");
    private boolean initialized, available;
    private int width, height;

    public boolean available() { return available; }

    public void loadOnce() {
        if (initialized) return;
        initialized = true;
        if (!Files.isRegularFile(saved)) return;
        try { install(PngImport.read(saved), false); }
        catch (IOException | RuntimeException exception) {
            LoggerFactory.getLogger("focuscrosshair").warn("Could not load saved custom crosshair; using Focus", exception);
        }
    }

    public void importFile(Path path) throws IOException {
        install(PngImport.read(path), true);
        initialized = true;
    }

    private void install(byte[] bytes, boolean persist) throws IOException {
        NativeImage image = NativeImage.read(bytes);
        DynamicTexture texture = null;
        try {
            int nextWidth = image.getWidth(), nextHeight = image.getHeight();
            if (nextWidth > PngImport.MAX_DIMENSION || nextHeight > PngImport.MAX_DIMENSION) throw new IOException("Image is too large");
            texture = new DynamicTexture(() -> "Focus Crosshair custom PNG", image);
            if (persist) {
                Files.createDirectories(saved.getParent());
                Path temporary = saved.resolveSibling("custom.png.tmp");
                Files.write(temporary, bytes);
                try { Files.move(temporary, saved, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, saved, StandardCopyOption.REPLACE_EXISTING); }
            }
            Minecraft.getInstance().getTextureManager().register(ID, texture);
            width = nextWidth;
            height = nextHeight;
            available = true;
        } catch (IOException | RuntimeException exception) {
            if (texture != null) texture.close();
            else image.close();
            throw exception;
        }
    }

    public void draw(GuiGraphicsExtractor graphics, double size, int color) {
        if (!available) return;
        float factor = (float)(size / Math.max(width, height));
        graphics.pose().pushMatrix();
        graphics.pose().scale(factor);
        graphics.pose().translate(-width / 2f, -height / 2f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ID, 0, 0, 0, 0, width, height, width, height, color);
        graphics.pose().popMatrix();
    }
}
