import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
public class GenerateTexture {
    public static void main(String[] args) throws Exception {
        int width = 64;
        int height = 64;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Прозрачный фон
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width, height);

        // --- ЦВЕТА РОБОТА ---
        Color metal = new Color(160, 160, 160);     // Светлый металл
        Color darkMetal = new Color(80, 80, 80);   // Темный металл
        Color visor = new Color(0, 255, 255);      // Светящийся голубой визор
        Color antenna = new Color(200, 50, 50);    // Красная антенна
        Color ideaRed = new Color(254, 40, 87);    // Логотип IDEA

        // ==========================================
        // ГОЛОВА (Head) - Робот
        // ==========================================
        // Верх головы (металл)
        g.setColor(metal);
        g.fillRect(8, 0, 8, 8);

        // Лицо (спереди)
        g.setColor(darkMetal);
        g.fillRect(8, 8, 8, 8);

        // Затылок
        g.setColor(metal);
        g.fillRect(24, 8, 8, 8);

        // Бока
        g.setColor(metal);
        g.fillRect(0, 8, 8, 8);
        g.fillRect(16, 8, 8, 8);

        // Визор (светящиеся глаза)
        g.setColor(visor);
        g.fillRect(9, 11, 6, 2); // Горизонтальный визор

        // Антенна на макушке (спереди)
        g.setColor(antenna);
        g.fillRect(11, 8, 2, 3);
        g.fillRect(10, 9, 4, 1);

        // Рот (вентиляционная решетка)
        g.setColor(Color.BLACK);
        g.fillRect(10, 14, 4, 1);
        g.drawLine(11, 14, 11, 15);
        g.drawLine(12, 14, 12, 15);
        g.drawLine(13, 14, 13, 15);

        // ==========================================
        // ТЕЛО (Body) - Робот
        // ==========================================
        g.setColor(darkMetal);
        // Спереди
        g.fillRect(20, 20, 8, 12);
        // Зад
        g.fillRect(32, 20, 8, 12);
        // Бока
        g.fillRect(16, 20, 4, 12);
        g.fillRect(28, 20, 4, 12);
        // Верх и низ
        g.fillRect(20, 16, 8, 4);
        g.fillRect(28, 16, 8, 4);

        // Логотип IDEA на груди
        g.setColor(ideaRed);
        g.fillRect(22, 23, 4, 4);
        g.setColor(Color.WHITE);
        g.fillRect(23, 24, 2, 2);

        // ==========================================
        // ПРАВАЯ РУКА (Right Arm) - Робот
        // ==========================================
        g.setColor(metal);
        // Спереди/Сзади
        g.fillRect(44, 20, 4, 12);
        g.fillRect(48, 20, 4, 12);
        // Бока
        g.fillRect(40, 20, 4, 12);
        g.fillRect(52, 20, 4, 12);
        // Верх/Низ
        g.fillRect(44, 16, 4, 4);
        g.fillRect(48, 16, 4, 4);
        // "Кисть" (темный металл)
        g.setColor(darkMetal);
        g.fillRect(44, 28, 4, 4);
        g.fillRect(48, 28, 4, 4);
        g.fillRect(40, 28, 4, 4);
        g.fillRect(52, 28, 4, 4);

        // ==========================================
        // ЛЕВАЯ РУКА (Left Arm) - Робот
        // ==========================================
        g.setColor(metal);
        g.fillRect(36, 52, 4, 12);
        g.fillRect(40, 52, 4, 12);
        g.fillRect(32, 52, 4, 12);
        g.fillRect(44, 52, 4, 12);
        g.fillRect(36, 48, 4, 4);
        g.fillRect(40, 48, 4, 4);
        g.setColor(darkMetal);
        g.fillRect(36, 60, 4, 4);
        g.fillRect(40, 60, 4, 4);
        g.fillRect(32, 60, 4, 4);
        g.fillRect(44, 60, 4, 4);

        // ==========================================
        // ПРАВАЯ НОГА (Right Leg) - Робот
        // ==========================================
        g.setColor(darkMetal);
        g.fillRect(4, 20, 4, 12);
        g.fillRect(8, 20, 4, 12);
        g.fillRect(0, 20, 4, 12);
        g.fillRect(12, 20, 4, 12);
        g.fillRect(4, 16, 4, 4);
        g.fillRect(8, 16, 4, 4);
        // "Обувь" (металл)
        g.setColor(metal);
        g.fillRect(4, 28, 4, 4);
        g.fillRect(8, 28, 4, 4);
        g.fillRect(0, 28, 4, 4);
        g.fillRect(12, 28, 4, 4);

        // ==========================================
        // ЛЕВАЯ НОГА (Left Leg) - Робот
        // ==========================================
        g.setColor(darkMetal);
        g.fillRect(20, 52, 4, 12);
        g.fillRect(24, 52, 4, 12);
        g.fillRect(16, 52, 4, 12);
        g.fillRect(28, 52, 4, 12);
        g.fillRect(20, 48, 4, 4);
        g.fillRect(24, 48, 4, 4);
        // "Обувь"
        g.setColor(metal);
        g.fillRect(20, 60, 4, 4);
        g.fillRect(24, 60, 4, 4);
        g.fillRect(16, 60, 4, 4);
        g.fillRect(28, 60, 4, 4);

        g.dispose();

        File file = new File("src/main/resources/assets/magicwand/textures/player_skin.png");
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);

        System.out.println("Скин робота создан!");
    }
}