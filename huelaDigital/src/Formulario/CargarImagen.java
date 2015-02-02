
package Formulario;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author jodandu
 */
public class CargarImagen {

    private BufferedImage image;

    public CargarImagen(String ruta) throws IOException {
        image = ImageIO.read(new File(ruta));
       //ImageIcon pic = new ImageIcon(imgURL); 
        if (image != null) {
        System.out.println(image.getSource());
       
        } else {
         System.err.println("Couldn't find file: " +ruta);
        } 
    }

    public CargarImagen() {
    }
    
    public void dibujaImagen(Graphics g){
        g.drawImage(image, 10, 10, 300,240,null);
    }
    
}
