package net.lawaxi.lottery.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
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
        // 读取上传的图片
        BufferedImage originalImage = ImageIO.read(imageStream);

        // 创建一个新的图像，宽1000，高1200
        BufferedImage modifiedImage = new BufferedImage(1000, 1200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = modifiedImage.createGraphics();

        // 设置外边框颜色为随机颜色
        Color borderColor = getRandomColor();
        g2d.setColor(borderColor);
        g2d.fillRect(0, 0, 1000, 1200);

        // 缩放原图到800x800并放置在指定位置
        Image scaledImage = originalImage.getScaledInstance(800, 800, Image.SCALE_SMOOTH);
        g2d.drawImage(scaledImage, 100, 100, null);

        // 在下方边框中间写文字“生日快乐”
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 40));
        g2d.setColor(Color.WHITE); // 文字颜色
        String text = name + "生日快乐";
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int x = (1000 - textWidth) / 2;
        int y = 1000 + 100 + fontMetrics.getHeight();
        g2d.drawString(text, x, y);

        // 创建内边缘波浪样式
        Area innerEdge = createInnerEdge(modifiedImage.getWidth(), modifiedImage.getHeight());

        // 设置内边缘颜色为随机颜色，并涂实心
        Color innerEdgeColor = getRandomSimilarColor(borderColor);
        g2d.setColor(innerEdgeColor);
        g2d.fill(innerEdge);

        // 绘制内边缘
        g2d.setStroke(new BasicStroke(5)); // 内边缘线条粗细
        g2d.draw(innerEdge);

        g2d.dispose();

        // 将修改后的图像转换为InputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(modifiedImage, "jpg", outputStream);
        byte[] modifiedImageData = outputStream.toByteArray();
        return new ByteArrayInputStream(modifiedImageData);
    }

    private static Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private static Color getRandomSimilarColor(Color baseColor) {
        Random random = new Random();
        int r = Math.min(255, baseColor.getRed() + random.nextInt(51) - 25);
        int g = Math.min(255, baseColor.getGreen() + random.nextInt(51) - 25);
        int b = Math.min(255, baseColor.getBlue() + random.nextInt(51) - 25);
        return new Color(r, g, b);
    }

    private static Area createInnerEdge(int width, int height) {
        int numArcs = 20; // 调整波浪样式的弧的数量
        int arcHeight = 50; // 调整波浪的高度
        Area innerEdge = new Area();

        for (int i = 0; i < numArcs; i++) {
            int x = i * (width / numArcs);
            int y = (i % 2 == 0) ? height : height - arcHeight;
            Arc2D arc = new Arc2D.Double(x, y, width / numArcs, arcHeight, 0, 180, Arc2D.OPEN);
            innerEdge.add(new Area(arc));
        }

        return innerEdge;
    }
}
