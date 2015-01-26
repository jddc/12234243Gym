

package BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author joshua
 */

//Conexion BD Huellas MySQL
public class ConexionBD {
    public String puerto = "3306";
    public String nomServidor = "localhost";
    public String db = "gym";
    public String user = "root";
    public String pass = "";
    Connection conn = null;
    
    public Connection conectar(){
        try{
            String ruta= "jdbc:mysql://";
            String servidor = nomServidor+":"+puerto+"/";
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(ruta+servidor+db,user,pass);
            if(conn != null){
                System.out.println("Conexion a la base de datos listo ...");
            }else if(conn == null){
                throw new SQLException();
            }
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        catch (ClassNotFoundException e){
            JOptionPane.showMessageDialog(null, "Se produjo el siguiente error class Not Found: "+e.getLocalizedMessage());
        }
        catch (NullPointerException e){
            JOptionPane.showMessageDialog(null, "Se produjo el siguiente error: Null"+e.getMessage());
        }
        finally{
            return conn;
        }   
    } //termina conectar
    
    
    public void desconectar(){
        conn = null;
        System.out.println("Desconexion de la base de datos");
    }
}
