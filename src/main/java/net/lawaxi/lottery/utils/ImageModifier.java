package net.lawaxi.lottery.utils;

import net.lawaxi.lottery.handler.config;

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

    private static config config;

    public static void setConfig(config config) {
        ImageModifier.config = config;
    }

    public static InputStream modifyImage(String name, String imageUrl) throws IOException {
        // 下载图片
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream imageStream = connection.getInputStream();

        return modifyImage(name, imageStream);
    }


    public static InputStream modifyImage(String name, InputStream imageStream) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageStream);
        BufferedImage modifiedImage = new BufferedImage(500, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = modifiedImage.createGraphics();

        //底纹
        Color borderColor = getRandomColor();
        g2d.setColor(borderColor);
        g2d.fillRect(0, 0, 500, 700);

        //图片
        Image scaledImage = originalImage.getScaledInstance(300, 400, Image.SCALE_SMOOTH);
        g2d.drawImage(scaledImage, 100, 100, null);

        //花边
        Area innerEdge = createInnerEdge(modifiedImage.getWidth(), modifiedImage.getHeight());

        Color innerEdgeColor = getRandomSimilarColor(borderColor);
        g2d.setColor(innerEdgeColor);
        g2d.fill(innerEdge);

        g2d.setStroke(new BasicStroke(5));
        g2d.draw(innerEdge);

        //文字
        g2d.setFont(config.font);
        g2d.setColor(Color.WHITE);
        String text = name + "生日快乐";
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int x = (500 - textWidth) / 2;
        int y = 500 + 80 + fontMetrics.getHeight();
        g2d.drawString(text, x, y);


        g2d.dispose();

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
        int numArcs = 20;
        int arcHeight = 50;
        Area innerEdge = new Area();

        for (int i = 0; i < numArcs; i++) {
            int x = i * (width / numArcs);
            int y = 0;
            Arc2D arc = new Arc2D.Double(x, y, width / numArcs, arcHeight, 0, 180, Arc2D.OPEN);
            innerEdge.add(new Area(arc));
        }

        for (int i = 0; i < numArcs; i++) {
            int x = i * (width / numArcs);
            int y = height - arcHeight;
            Arc2D arc = new Arc2D.Double(x, y, width / numArcs, arcHeight, 0, -180, Arc2D.OPEN);
            innerEdge.add(new Area(arc));
        }

        for (int i = 0; i < numArcs; i++) {
            int x = 0;
            int y = i * (height / numArcs);
            Arc2D arc = new Arc2D.Double(x, y, arcHeight, height / numArcs, 90, 180, Arc2D.OPEN);
            innerEdge.add(new Area(arc));
        }

        for (int i = 0; i < numArcs; i++) {
            int x = width - arcHeight;
            int y = i * (height / numArcs);
            Arc2D arc = new Arc2D.Double(x, y, arcHeight, height / numArcs, -90, 180, Arc2D.OPEN);
            innerEdge.add(new Area(arc));
        }

        return innerEdge;
    }
}
