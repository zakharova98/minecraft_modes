package com.example.magicwand.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Приводит файл скина к виду, который ждёт игра (аналог того, что делает ванильный загрузчик скинов):
 * - принимает 64x64 и старый формат 64x32 (достраивает левую руку/ногу);
 * - убирает прозрачность у базового слоя тела;
 * - определяет, "тонкие" ли руки (Алекс) по пустой колонке в текстуре руки.
 */
final class SkinImageProcessor {

    record Result(byte[] png, boolean detectedSlim) {}

    private static final int MAX_BYTES = 30_000;

    private SkinImageProcessor() {}

    static Result process(Path file) throws IOException {
        BufferedImage src = ImageIO.read(file.toFile());
        if (src == null) {
            throw new IOException("файл не является PNG-картинкой");
        }
        int w = src.getWidth();
        int h = src.getHeight();
        if (w != 64 || (h != 64 && h != 32)) {
            throw new IOException("нужен скин 64x64 (или старый 64x32), а в файле " + w + "x" + h);
        }
        boolean legacy = (h == 32);

        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 64, h, src.getRGB(0, 0, 64, h, null, 0, 64), 0, 64);

        // Скины Алекса оставляют колонку текстуры правой руки прозрачной - по ней определяем тип рук.
        // Проверяем ДО того, как зальём базовый слой непрозрачным цветом.
        boolean slim = !legacy && ((img.getRGB(54, 20) >>> 24) == 0);

        if (legacy) {
            copyRect(img, 4, 16, 16, 32, 4, 4);
            copyRect(img, 8, 16, 16, 32, 4, 4);
            copyRect(img, 0, 20, 24, 32, 4, 12);
            copyRect(img, 4, 20, 16, 32, 4, 12);
            copyRect(img, 8, 20, 8, 32, 4, 12);
            copyRect(img, 12, 20, 16, 32, 4, 12);
            copyRect(img, 44, 16, -8, 32, 4, 4);
            copyRect(img, 48, 16, -8, 32, 4, 4);
            copyRect(img, 40, 20, 0, 32, 4, 12);
            copyRect(img, 44, 20, -8, 32, 4, 12);
            copyRect(img, 48, 20, -16, 32, 4, 12);
            copyRect(img, 52, 20, -8, 32, 4, 12);
        }

        setNoAlpha(img, 0, 0, 32, 16);
        if (legacy) {
            notchTransparencyHack(img, 32, 0, 64, 32);
        }
        setNoAlpha(img, 0, 16, 64, 32);
        setNoAlpha(img, 16, 48, 48, 64);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        byte[] bytes = out.toByteArray();
        if (bytes.length > MAX_BYTES) {
            throw new IOException("скин слишком большой после обработки (" + bytes.length + " байт)");
        }
        return new Result(bytes, slim);
    }

    /** Копия прямоугольника с зеркалом по X (так достраивается левая сторона для старых скинов). */
    private static void copyRect(BufferedImage img, int x, int y, int xOff, int yOff, int width, int height) {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int mirrored = width - 1 - i;
                img.setRGB(x + xOff + mirrored, y + yOff + j, img.getRGB(x + i, y + j));
            }
        }
    }

    private static void setNoAlpha(BufferedImage img, int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                img.setRGB(x, y, img.getRGB(x, y) | 0xFF000000);
            }
        }
    }

    /** У старых скинов слой "шапки" непрозрачный - если в области нет прозрачных пикселей, делаем её прозрачной. */
    private static void notchTransparencyHack(BufferedImage img, int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                if ((img.getRGB(x, y) >>> 24) < 128) return;
            }
        }
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                img.setRGB(x, y, img.getRGB(x, y) & 0x00FFFFFF);
            }
        }
    }
}
