package net.lawaxi.lottery.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ImageModifier {

    public static InputStream modifyImage(String name, String imageUrl) throws IOException {
        // 下载图片
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream imageStream = connection.getInputStream();

        return modifyImage(name, imageStream);
    }


    public static InputStream modifyImage(String name, InputStream imageStream) throws IOException {

        // 将图片加载到BufferedImage
        BufferedImage originalImage = ImageIO.read(imageStream);

        // 创建新的BufferedImage，用于修改
        BufferedImage modifiedImage = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = modifiedImage.createGraphics();

        // 绘制外缘边框
        g2d.setColor(getRandomColor());
        g2d.fillRect(0, 0, 800, 100);
        g2d.fillRect(0, 100, 100, 600);
        g2d.fillRect(700, 100, 100, 600);
        g2d.fillRect(0, 700, 800, 200);

        // 绘制内缘边框（波浪样式）
        g2d.setColor(getRandomColor());
        int waveHeight = 30;
        int numWaves = 20;
        int waveWidth = 800 / numWaves;
        for (int i = 0; i < numWaves; i++) {
            g2d.fillArc(i * waveWidth, 0, waveWidth, 100, 0, 180);
            g2d.fillArc(i * waveWidth, 700, waveWidth, 100, 0, 180);
        }

        // 绘制内缘边框（左右）
        g2d.setColor(getRandomColor());
        g2d.fillRect(0, 100, 100, 600);
        g2d.fillRect(700, 100, 100, 600);

        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 36));
        g2d.drawString(name + "生日快乐", 350, 750);

        g2d.dispose();

        // 将修改后的BufferedImage转换为InputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(modifiedImage, "jpg", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    // 生成随机颜色
    private static Color getRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r, g, b);
    }
}
