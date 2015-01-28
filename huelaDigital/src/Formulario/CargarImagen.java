
package Formulario;

import java.awt.Graphics;
import javax.swing.ImageIcon;

/**
 *
 * @author jodandu
 */
public class CargarImagen {

    String ruta = "";
    ImageIcon dibujo;

    public CargarImagen( String ruta) {
        this.ruta = ruta;
        System.out.println(ruta);
        this.dibujo  = new ImageIcon(new ImageIcon(getClass().getResource(ruta)).getImage());
    }

    public CargarImagen() {
    }
    
    public void dibujaImagen(Graphics g){
        g.drawImage(dibujo.getImage(), 10, 10, 300,240,null);
    }
    
}
