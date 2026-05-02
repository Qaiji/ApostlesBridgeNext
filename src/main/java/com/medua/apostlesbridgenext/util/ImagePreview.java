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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ImagePreview {
    private static final Pattern META_IMAGE_PATTERN = Pattern.compile(
            "<meta\\s+[^>]*(?:property|name)=[\"'](?:og:image|twitter:image)[\"'][^>]*content=[\"']([^\"']+)[\"'][^>]*>|" +
                    "<meta\\s+[^>]*content=[\"']([^\"']+)[\"'][^>]*(?:property|name)=[\"'](?:og:image|twitter:image)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    private final String url;
    private final Identifier textureId;
    private volatile boolean loading;
    private volatile boolean failed;
    private volatile String failureReason = "Image preview failed";
    private volatile boolean gif;
    private volatile String videoLabel;
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
                    videoLabel = result.videoLabel();
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
        if (videoLabel != null) {
            drawBadge(context, client, videoLabel);
        } else if (gif) {
            drawGifBadge(context, client);
        }
    }

    private DownloadResult download() {
        DownloadResult lastResult = new DownloadResult(null, "Image preview failed", false, null);
        for (URI candidateUri : previewCandidates(url)) {
            lastResult = download(candidateUri);
            if (lastResult.image() != null) {
                return lastResult;
            }
        }
        return lastResult;
    }

    private DownloadResult download(URI uri) {
        return download(uri, 0);
    }

    private DownloadResult download(URI uri, int htmlPreviewDepth) {
        try {
            URLConnection connection = openConnectionWithRedirects(uri, 0);
            String contentType = connection.getContentType();
            byte[] bytes;

            try (InputStream stream = connection.getInputStream()) {
                bytes = readBytes(stream);
            }

            NativeImage image = decodeImage(bytes);
            if (image != null) {
                return new DownloadResult(image, null, isGifUrl(uri.toString()) || isGif(contentType, bytes), videoLabel(url));
            } else if (isHtml(contentType) && htmlPreviewDepth < 2) {
                URI previewImage = extractHtmlPreviewImage(uri, bytes);
                if (previewImage != null) {
                    return download(previewImage, htmlPreviewDepth + 1);
                }
            } else {
                String type = contentType == null ? "unknown content" : contentType.split(";")[0];
                return new DownloadResult(null, "Preview decode failed: " + type, false, null);
            }
            String type = contentType == null ? "unknown content" : contentType.split(";")[0];
            return new DownloadResult(null, "Preview decode failed: " + type, false, null);
        } catch (IOException | IllegalArgumentException exception) {
            return new DownloadResult(null, shortReason(exception), false, null);
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

    private static boolean isHtml(String contentType) {
        return contentType != null && contentType.toLowerCase().startsWith("text/html");
    }

    private static URI extractHtmlPreviewImage(URI pageUri, byte[] bytes) {
        String html = new String(bytes, StandardCharsets.UTF_8);
        Matcher matcher = META_IMAGE_PATTERN.matcher(html);
        if (!matcher.find()) {
            return null;
        }

        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return pageUri.resolve(unescapeHtml(value.trim()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String unescapeHtml(String value) {
        return value
                .replace("&amp;", "&")
                .replace("&#38;", "&")
                .replace("&quot;", "\"")
                .replace("&#34;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
    }

    private static boolean isGifUrl(String value) {
        try {
            String path = URI.create(value).getPath();
            return path != null && path.toLowerCase().endsWith(".gif");
        } catch (IllegalArgumentException exception) {
            return value.toLowerCase().contains(".gif");
        }
    }

    private static String videoLabel(String value) {
        try {
            String path = URI.create(value).getPath();
            return videoExtensionLabel(path);
        } catch (IllegalArgumentException exception) {
            return videoExtensionLabel(value);
        }
    }

    private static String videoExtensionLabel(String value) {
        if (value == null) {
            return null;
        }

        String lowerValue = value.toLowerCase();
        if (lowerValue.endsWith(".mp4") || lowerValue.contains(".mp4?")) {
            return "MP4";
        }
        if (lowerValue.endsWith(".webm") || lowerValue.contains(".webm?")) {
            return "WEBM";
        }
        if (lowerValue.endsWith(".mov") || lowerValue.contains(".mov?")) {
            return "MOV";
        }
        return null;
    }

    private static List<URI> previewCandidates(String value) {
        Set<URI> candidates = new LinkedHashSet<>();
        try {
            URI uri = URI.create(value);
            addCandidate(candidates, uri);
            addDiscordAttachmentCandidates(candidates, uri);
            addVideoThumbnailCandidates(candidates, uri);
        } catch (IllegalArgumentException ignored) {
        }
        return new ArrayList<>(candidates);
    }

    private static void addDiscordAttachmentCandidates(Set<URI> candidates, URI uri) {
        String host = host(uri);
        String path = uri.getPath();
        if (path == null || !path.startsWith("/attachments/")) {
            return;
        }

        if (host.equals("media.discordapp.net") || host.equals("cdn.discordapp.com")) {
            addCandidate(candidates, withDiscordFormat(uri, "png"));
            addCandidate(candidates, withDiscordFormat(uri, "jpg"));
            addCandidate(candidates, withHost(uri, "cdn.discordapp.com"));
            addCandidate(candidates, withHost(withDiscordFormat(uri, "png"), "cdn.discordapp.com"));
            addCandidate(candidates, withHost(withDiscordFormat(uri, "jpg"), "cdn.discordapp.com"));
            addCandidate(candidates, withoutQuery(uri));
            addCandidate(candidates, withHost(withoutQuery(uri), "cdn.discordapp.com"));
        }
    }

    private static void addVideoThumbnailCandidates(Set<URI> candidates, URI uri) {
        if (videoLabel(uri.toString()) == null) {
            return;
        }

        addCandidate(candidates, withQuery(uri, "format=webp"));
        addCandidate(candidates, withQuery(uri, "format=png"));
        addExtensionCandidates(candidates, uri);

        URI embeddedUrl = embeddedDiscordExternalUrl(uri);
        if (embeddedUrl != null) {
            addCandidate(candidates, embeddedUrl);
            addExtensionCandidates(candidates, embeddedUrl);
        }
    }

    private static void addExtensionCandidates(Set<URI> candidates, URI uri) {
        addCandidate(candidates, withExtension(uri, ".gif"));
        addCandidate(candidates, withExtension(uri, ".webp"));
        addCandidate(candidates, withExtension(uri, ".png"));
        addCandidate(candidates, withExtension(uri, ".jpg"));
    }

    private static URI embeddedDiscordExternalUrl(URI uri) {
        String host = host(uri);
        String path = uri.getPath();
        if (!host.endsWith("discordapp.net") || path == null) {
            return null;
        }

        int markerIndex = path.indexOf("/https/");
        if (markerIndex < 0) {
            return null;
        }

        try {
            return URI.create("https://" + path.substring(markerIndex + "/https/".length()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static URI withoutQuery(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
        } catch (Exception exception) {
            return uri;
        }
    }

    private static URI withHost(URI uri, String host) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), host, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (Exception exception) {
            return uri;
        }
    }

    private static URI withQuery(URI uri, String query) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
        } catch (Exception exception) {
            return uri;
        }
    }

    private static URI withDiscordFormat(URI uri, String format) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return withQuery(uri, "format=" + format + "&quality=lossless");
        }

        String[] parts = query.split("&");
        StringBuilder builder = new StringBuilder();
        boolean replacedFormat = false;
        boolean replacedQuality = false;
        for (String part : parts) {
            if (part.isBlank() || part.equals("=")) {
                continue;
            }

            String replacement = part;
            if (part.startsWith("format=")) {
                replacement = "format=" + format;
                replacedFormat = true;
            } else if (part.startsWith("quality=")) {
                replacement = "quality=lossless";
                replacedQuality = true;
            }

            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(replacement);
        }

        if (!replacedFormat) {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append("format=").append(format);
        }
        if (!replacedQuality) {
            builder.append("&quality=lossless");
        }

        return withQuery(uri, builder.toString());
    }

    private static URI withExtension(URI uri, String extension) {
        String path = uri.getPath();
        if (path == null) {
            return uri;
        }

        int extensionIndex = path.lastIndexOf('.');
        if (extensionIndex < 0) {
            return uri;
        }

        try {
            return new URI(uri.getScheme(), uri.getAuthority(), path.substring(0, extensionIndex) + extension, uri.getQuery(), uri.getFragment());
        } catch (Exception exception) {
            return uri;
        }
    }

    private static String host(URI uri) {
        String host = uri.getHost();
        return host == null ? "" : host.toLowerCase();
    }

    private static void addCandidate(Set<URI> candidates, URI uri) {
        if (uri != null) {
            candidates.add(uri);
        }
    }

    private static void drawMessage(DrawContext context, MinecraftClient client, String message) {
        int textWidth = client.textRenderer.getWidth(message);
        drawFrame(context, textWidth + 8, client.textRenderer.fontHeight + 8);
        context.drawTextWithShadow(client.textRenderer, message, ImagePreviewHandler.PADDING + 5, ImagePreviewHandler.PADDING + 5, ColorUtil.TEXT_WHITE);
    }

    private static void drawGifBadge(DrawContext context, MinecraftClient client) {
        drawBadge(context, client, "GIF");
    }

    private static void drawBadge(DrawContext context, MinecraftClient client, String label) {
        int left = ImagePreviewHandler.PADDING + 4;
        int top = ImagePreviewHandler.PADDING + 4;
        int right = left + client.textRenderer.getWidth(label) + 8;
        int bottom = top + client.textRenderer.fontHeight + 5;

        context.fill(left, top, right, bottom, ColorUtil.DARK_PURPLE_BADGE);
        context.drawTextWithShadow(client.textRenderer, label, left + 4, top + 3, ColorUtil.TEXT_WHITE);
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

    private record DownloadResult(NativeImage image, String error, boolean gif, String videoLabel) {
    }
}
