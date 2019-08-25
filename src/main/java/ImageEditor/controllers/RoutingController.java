package ImageEditor.controllers;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class RoutingController {

    Map<String, BufferedImage> images = new HashMap<>();
    BufferedImage img = null;
    ImageProcessorController imageProcessorController = new ImageProcessorController();

    @RequestMapping(value = "/image/add", method = RequestMethod.POST)
    public HashMap<String, Object> addImage(HttpServletRequest requestEntity) throws Exception {
        BufferedImage uploadedImage = ImageIO.read(requestEntity.getInputStream());

        String uniqueID = UUID.randomUUID().toString();

        images.put(uniqueID, uploadedImage);

        HashMap<String, Object> map = new HashMap<>();

        map.put("id", uniqueID);
        map.put("width", uploadedImage.getWidth());
        map.put("height", uploadedImage.getHeight());

        return map;

    }

    @RequestMapping(value = "/image/delete/{id}", method = RequestMethod.DELETE)
    public String deleteImage(@PathVariable String id) throws Exception {

        if (images.get(id)==null){
            return "404";
        }
        images.remove(id);
        return "Object deleted";
    }


    @RequestMapping(value = "/image/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String id) throws IOException {
        BufferedImage newImage = images.get(id);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(convertImageToDisplay(newImage));
    }


    @RequestMapping(value = "/image/{id}/size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, Object> getImageSize(@PathVariable String id) throws IOException {
        BufferedImage newImage = images.get(id);

        int height = images.get(id).getHeight();
        int width = images.get(id).getWidth();
        HashMap<String, Object> map = new HashMap<>();

        map.put("Height", height);
        map.put("Width", width);

        return map;
    }

    @RequestMapping(value = "/image/{id}/scale/{percent}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getScaledImage(@PathVariable String id, @PathVariable String percent) throws IOException {
        BufferedImage newImage = images.get(id);
        BufferedImage scaledImage = imageProcessorController.bicubic(newImage, percent);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(convertImageToDisplay(scaledImage));
    }


    @RequestMapping(value = "/image/{id}/histogram", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, Object> getImageHistogram(@PathVariable String id) throws IOException {
        HashMap<String, Object> map = imageProcessorController.histogram(images.get(id));

        return map;
    }


    @RequestMapping(value = "/image/{id}/greyscale", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getGreyscale(@PathVariable String id) throws IOException {

        BufferedImage newImage = imageProcessorController.greyScale(images.get(id));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(convertImageToDisplay(newImage));
    }

    @RequestMapping(value = "/image/{id}/blur/{radius}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBlur(@PathVariable String id, @PathVariable int radius) throws IOException {

        BufferedImage newImage = imageProcessorController.blur(images.get(id),radius);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(convertImageToDisplay(newImage));
    }

    @RequestMapping(value = "/image/{id}/crop/{start}/{stop}/{width}/{height}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCroppedImage(@PathVariable String id, @PathVariable int start, @PathVariable int stop, @PathVariable int width, @PathVariable int height) throws IOException {

        BufferedImage newImage = imageProcessorController.crop(images.get(id),start,stop,width,height);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(convertImageToDisplay(newImage));
    }


    private byte[] convertImageToDisplay(BufferedImage newImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ImageIO.write(newImage, "png", outputStream);
        return outputStream.toByteArray();
    }

}
