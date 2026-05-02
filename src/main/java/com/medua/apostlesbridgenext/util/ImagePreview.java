package com.medua.apostlesbridgenext.util;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.handler.ImagePreviewHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

public final class ImagePreview {
    private final String url;
    private final Identifier textureId;
    private volatile boolean loading;
    private volatile boolean failed;
    private volatile String failureReason = "Image preview failed";
    private volatile boolean gif;
    private volatile int width;
    private volatile int height;

    public ImagePreview(String url) {
        this.url = url;
        this.textureId = Identifier.of(ApostlesBridgeNextClient.MODID, "image_preview/" + sha1(url));
    }

    public void load(MinecraftClient client) {
        if (loading || failed || width > 0) {
            return;
        }

        loading = true;
        CompletableFuture.supplyAsync(this::download).whenComplete((result, throwable) -> {
            if (throwable != null || result.image() == null) {
                failureReason = throwable != null ? shortReason(throwable) : result.error();
                failed = true;
                loading = false;
                return;
            }

            client.execute(() -> {
                try {
                    gif = result.gif();
                    width = result.image().getWidth();
                    height = result.image().getHeight();
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> url, result.image());
                    client.getTextureManager().registerTexture(textureId, texture);
                } finally {
                    loading = false;
                }
            });
        });
    }

    public void render(DrawContext context, MinecraftClient client, int maxWidth, int maxHeight) {
        int previewWidth = width;
        int previewHeight = height;
        if (failed) {
            drawMessage(context, client, failureReason);
            return;
        }
        if (previewWidth <= 0 || previewHeight <= 0) {
            drawMessage(context, client, "Loading image...");
            return;
        }

        float scale = Math.min((float) maxWidth / previewWidth, (float) maxHeight / previewHeight);
        scale = Math.min(scale, 1.0f);
        int scaledWidth = Math.max(1, Math.round(previewWidth * scale));
        int scaledHeight = Math.max(1, Math.round(previewHeight * scale));

        drawFrame(context, scaledWidth, scaledHeight);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, ImagePreviewHandler.PADDING + 1, ImagePreviewHandler.PADDING + 1, 0, 0, scaledWidth, scaledHeight, previewWidth, previewHeight, previewWidth, previewHeight);
        if (gif) {
            drawGifBadge(context, client);
        }
    }

    private DownloadResult download() {
        try {
            URLConnection connection = openConnectionWithRedirects(URI.create(url), 0);
            String contentType = connection.getContentType();
            byte[] bytes;

            try (InputStream stream = connection.getInputStream()) {
                bytes = readBytes(stream);
            }

            NativeImage image = decodeImage(bytes);
            if (image != null) {
                return new DownloadResult(image, null, isGifUrl(url) || isGif(contentType, bytes));
            } else {
                String type = contentType == null ? "unknown content" : contentType.split(";")[0];
                return new DownloadResult(null, "Preview decode failed: " + type, false);
            }
        } catch (IOException | IllegalArgumentException exception) {
            return new DownloadResult(null, shortReason(exception), false);
        }
    }

    private static NativeImage decodeImage(byte[] bytes) {
        try {
            return NativeImage.read(bytes);
        } catch (IOException ignored) {
            return decodeWithImageIo(bytes);
        }
    }

    private static NativeImage decodeWithImageIo(byte[] bytes) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bufferedImage == null) {
                return null;
            }

            NativeImage image = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    image.setColorArgb(x, y, bufferedImage.getRGB(x, y));
                }
            }
            return image;
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    private static URLConnection openConnectionWithRedirects(URI uri, int redirectCount) throws IOException {
        if (redirectCount > 5) {
            throw new IOException("Too many redirects");
        }

        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "image/png,image/jpeg,image/jpg,image/*;q=0.8,*/*;q=0.5");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 ApostlesBridgeNext/" + ApostlesBridgeNextClient.VERSION);

        if (!(connection instanceof HttpURLConnection httpConnection)) {
            return connection;
        }

        httpConnection.setInstanceFollowRedirects(false);
        int status = httpConnection.getResponseCode();
        if (status >= 300 && status < 400) {
            String location = httpConnection.getHeaderField("Location");
            httpConnection.disconnect();
            if (location == null || location.isBlank()) {
                throw new IOException("Redirect without location");
            }
            return openConnectionWithRedirects(uri.resolve(location), redirectCount + 1);
        }
        if (status >= 400) {
            httpConnection.disconnect();
            throw new IOException("HTTP " + status);
        }

        return httpConnection;
    }

    private static byte[] readBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static String shortReason(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "Image preview failed";
        }
        return "Preview failed: " + message;
    }

    private static boolean isGif(String contentType, byte[] bytes) {
        if (contentType != null && contentType.toLowerCase().startsWith("image/gif")) {
            return true;
        }
        return bytes.length >= 6
                && bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == '8'
                && (bytes[4] == '7' || bytes[4] == '9')
                && bytes[5] == 'a';
    }

    private static boolean isGifUrl(String value) {
        try {
            String path = URI.create(value).getPath();
            return path != null && path.toLowerCase().endsWith(".gif");
        } catch (IllegalArgumentException exception) {
            return value.toLowerCase().contains(".gif");
        }
    }

    private static void drawMessage(DrawContext context, MinecraftClient client, String message) {
        int textWidth = client.textRenderer.getWidth(message);
        drawFrame(context, textWidth + 8, client.textRenderer.fontHeight + 8);
        context.drawTextWithShadow(client.textRenderer, message, ImagePreviewHandler.PADDING + 5, ImagePreviewHandler.PADDING + 5, ColorUtil.TEXT_WHITE);
    }

    private static void drawGifBadge(DrawContext context, MinecraftClient client) {
        int left = ImagePreviewHandler.PADDING + 4;
        int top = ImagePreviewHandler.PADDING + 4;
        int right = left + client.textRenderer.getWidth("GIF") + 8;
        int bottom = top + client.textRenderer.fontHeight + 5;

        context.fill(left, top, right, bottom, ColorUtil.DARK_PURPLE_BADGE);
        context.drawTextWithShadow(client.textRenderer, "GIF", left + 4, top + 3, ColorUtil.TEXT_WHITE);
    }

    private static void drawFrame(DrawContext context, int width, int height) {
        int left = ImagePreviewHandler.PADDING;
        int top = ImagePreviewHandler.PADDING;
        int right = left + width + 2;
        int bottom = top + height + 2;

        context.fill(left, top, right, bottom, ColorUtil.IMAGE_PREVIEW_BACKGROUND);
        context.drawHorizontalLine(left, right - 1, top, ColorUtil.DARK_PURPLE_BORDER);
        context.drawHorizontalLine(left, right - 1, bottom - 1, ColorUtil.DARK_PURPLE_BORDER);
        context.drawVerticalLine(left, top, bottom - 1, ColorUtil.DARK_PURPLE_BORDER);
        context.drawVerticalLine(right - 1, top, bottom - 1, ColorUtil.DARK_PURPLE_BORDER);
    }

    private static String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private record DownloadResult(NativeImage image, String error, boolean gif) {
    }
}
