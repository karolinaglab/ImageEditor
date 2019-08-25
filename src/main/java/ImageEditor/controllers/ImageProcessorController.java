package ImageEditor.controllers;

import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.IntStream;



public class ImageProcessorController {


    public BufferedImage cloneImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);

        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public BufferedImage greyScale(BufferedImage image) {
        BufferedImage img = cloneImage(image);

        for(int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {

                Color c = new Color(img.getRGB(i, j));
                int r = (int)(c.getRed() * 0.299);
                int g = (int)(c.getGreen() * 0.587);
                int b = (int)(c.getBlue() * 0.114);

                int grey = r + g + b;
                Color tmpColor = new Color(grey, grey, grey);

                img.setRGB(i,j,tmpColor.getRGB());
            }
        }
        return img;
    }

    public BufferedImage bicubic(BufferedImage image, String percent) {
        BufferedImage img = cloneImage(image);
        double height = image.getHeight();
        double width = image.getWidth();

        double scale = Double.parseDouble(percent);

        int scaledHeight = (int)(height * scale);
        int scaledWidth = (int)(width * scale);

        BufferedImage bicubicImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());

        Graphics2D bg = bicubicImage.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        bg.scale(scale,scale);
        bg.drawImage(img, 0, 0, null);
        bg.dispose();

        return bicubicImage;
    }

    public HashMap<String, Object> histogram(BufferedImage image) {
        BufferedImage img = cloneImage(image);
        double height = image.getHeight();
        double width = image.getWidth();

        HashMap<String, Object> mapR = new HashMap<>();
        HashMap<String, Object> mapG = new HashMap<>();
        HashMap<String, Object> mapB = new HashMap<>();
        Double[] histTabR = new Double[256];
        Double[] histTabG = new Double[256];
        Double[] histTabB = new Double[256];
        for (int i = 0; i < 256; i++) {
           histTabR[i] = (double)0;
           histTabG[i] = (double)0;
           histTabB[i] = (double)0;
        }



        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color c = new Color(img.getRGB(i, j));
                int r = c.getRed();
                histTabR[r]++;
                int g = c.getGreen();
                histTabG[g]++;
                int b = c.getBlue();
                histTabB[b]++;
            }
        }

        normalize(histTabR);
        normalize(histTabG);
        normalize(histTabB);

        for (int i = 0; i < 256; i++) {
            mapR.put("" + i, histTabR[i]);
            mapG.put("" + i, histTabG[i]);
            mapB.put("" + i, histTabB[i]);
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("R:", mapR);
        map.put("G:", mapG);
        map.put("B:", mapB);

        return map;

    }


    private void normalize(Double[] arr){
        Double min = Collections.min(Arrays.asList(arr));
        Double max = Collections.max(Arrays.asList(arr));
        double scale = 1.0/(max-min);
        for (int i = 0; i < 256; i++){
            if(!max.equals(min))
                arr[i] = (arr[i]-min) * scale;
            else arr[i] = 1.0;
        }
    }




    public BufferedImage blur(BufferedImage image, int radius) {

        BufferedImage img = cloneImage(image);

        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];

        for (int i = 0; i < data.length; i++) {
            data[i] = weight;
        }

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage i = op.filter(img, null);

        return i;
    }


    public BufferedImage crop(BufferedImage image, int start, int stop, int width, int height) {
        BufferedImage img = cloneImage(image);



        return img.getSubimage(start, stop, width, height);

    }

}
