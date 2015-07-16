
package Formulario;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author jodandu
 */
public class CargarImagen {

    private BufferedImage image;

    public CargarImagen(String ruta) {
        try{
            image = ImageIO.read(new File(ruta));
        //ImageIcon pic = new ImageIcon(imgURL); 
         if (image != null) {
         System.out.println(image.getSource());

         } else {
            JOptionPane.showMessageDialog(null, "No se encontro foto del cliente","Foto Cliente",JOptionPane.ERROR_MESSAGE);

         } 
        }catch(IOException e){
        
        }
        
    }

    public CargarImagen() {
    }
    
    public void dibujaImagen(Graphics g, int width, int height){
        g.drawImage(image, 10, 10, width,height,null);
    }
    
}
