import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GenerateTexture {
    public static void main(String[] args) throws Exception {
        int size = 512;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Заливаем весь фон ЧЕРНЫМ цветом
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, size, size);

        // 2. Делаем круг ПРОЗРАЧНЫМ (вырезаем его)
        int padding = (int) (size * 0.2f); // Размер круга (20% отступа)
        int circleSize = size - (padding * 2);

        // Очищаем круг (делаем его прозрачным)
        g.setComposite(AlphaComposite.Clear);
        g.fillOval(padding, padding, circleSize, circleSize);

        // Возвращаем режим рисования
        g.setComposite(AlphaComposite.SrcOver);

        // 3. Рисуем перекрестие (внутри круга)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        int cx = size / 2;
        int cy = size / 2;
        int gap = 10;

        // Тонкие линии
        g.drawLine(padding, cy, cx - gap, cy);
        g.drawLine(cx + gap, cy, size - padding, cy);
        g.drawLine(cx, padding, cx, cy - gap);
        g.drawLine(cx, cy + gap, cx, size - padding);

        // Толстые отрезки
        g.setStroke(new BasicStroke(6f));
        g.drawLine(cx - 30, cy, cx - gap, cy);
        g.drawLine(cx + gap, cy, cx + 30, cy);
        g.drawLine(cx, cy - 30, cx, cy - gap);
        g.drawLine(cx, cy + gap, cx, cy + 30);

        // Штрихи
        g.setStroke(new BasicStroke(2f));
        for (int i = -60; i <= 60; i += 20) {
            if (i == 0) continue;
            g.drawLine(cx + i, cy - 5, cx + i, cy + 5);
            g.drawLine(cx - 5, cy + i, cx + 5, cy + i);
        }

        // 4. Красная точка
        g.setColor(Color.RED);
        g.fillOval(cx - 2, cy - 2, 4, 4);

        g.dispose();

        File file = new File("src/main/resources/assets/magicwand/textures/gui/sniper_scope.png");
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);

        System.out.println("Текстура с прозрачным кругом создана!");
    }
}