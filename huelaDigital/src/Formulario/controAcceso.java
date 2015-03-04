package Formulario;

import BD.ConexionBD;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.StringTokenizer;


public final class controAcceso extends javax.swing.JFrame {
   String urlFoto = "";
   String nombreUsuario = "";
   Vector usuario; 
   
   //Variables para camaraWeb
   private Dimension ds = new Dimension(321, 268);
   private Dimension cs = WebcamResolution.VGA.getSize();
   private Webcam wCam = Webcam.getWebcams().get(0); //1 logitech 0//camaraweb default
   private WebcamPanel wCamPanel = new WebcamPanel(wCam, ds, false);
   
    //Variables globales para operaciones de la huella
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    Date date = new Date();
    
    public controAcceso() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Imposible modificar el tema visual","Look and Feel invalido",
            JOptionPane.ERROR_MESSAGE);
        }
        usuario = new Vector();
        for (Webcam webcam : Webcam.getWebcams()) {
            System.out.println("Webcam detected: " + webcam.getName());
        }
        
        
       initComponents();
       wCam.setViewSize(cs);
       wCamPanel.setFillArea(true);
       panelFoto.setLayout(new FlowLayout());
       panelFoto.add(wCamPanel);
      // obtenerUsuarios();
    }
    
    protected void Iniciar(){
        Lector.addDataListener(new DPFPDataAdapter(){
            @Override public void dataAcquired(final DPFPDataEvent e){
                SwingUtilities.invokeLater(new Runnable() {@Override
                    public void run(){
                        EnviarTexto("La huella digital ha sido capturada");
                        procesarCaptura(e.getSample());
                    }
                });
            }//Termina dataAcquired
        });//Termina LectorListener
        
        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter(){
            @Override public void readerConnected(final DPFPReaderStatusEvent e){
                SwingUtilities.invokeLater(new Runnable() {@Override
                    public void run(){
                        EnviarTexto("El sensor de huella digital esta activado o conectado");
                    }
                });
            }//Termina readerConnected
             public void readerDisConnected(final DPFPReaderStatusEvent e){
                SwingUtilities.invokeLater(new Runnable() {@Override
                    public void run(){
                        EnviarTexto("El sensor de huella digital esta desactivado o desconectado");
                    }
                });
            }//Termina readerDisconnected
        });//Termina LectorStatusListener
        
        
        Lector.addSensorListener(new DPFPSensorAdapter(){
            @Override public void fingerTouched(final DPFPSensorEvent e){
                SwingUtilities.invokeLater(new Runnable() {@Override
                    public void run(){
                        EnviarTexto("El dedo ha sido colocado sobre el lector de huella digital");
                    }
                });
            }//Termina fingerTouched
            @Override public void fingerGone(final DPFPSensorEvent e){
                SwingUtilities.invokeLater(new Runnable() {@Override
                    public void run(){
                        EnviarTexto("El dedo ha sido quitado lector de huella digital");
                        
                        try {
                            indentificarHuella();
                            Reclutador.clear();
                        } catch (IOException e) {
                            Logger.getLogger(controAcceso.class.getName()).log(Level.SEVERE,null,e);
                        }
                    }
                });
            }//Termina fingerGone
        });//Termina SensorListener
        
        Lector.addErrorListener(new DPFPErrorAdapter(){
            public void dataAcquired(final DPFPErrorEvent e){
                SwingUtilities.invokeLater(new Runnable() {@Override
                    public void run(){
                        EnviarTexto("Error"+e.getError());
                    }
                });
            }//Termina dataAcquired
        });//Termina LectorErrorListener
    }//Termina metodo Iniciar
    
    public DPFPFeatureSet featuresInscription;
    public DPFPFeatureSet featuresVerification;
    
    public DPFPFeatureSet extraerCaracteristicas (DPFPSample sample,DPFPDataPurpose purpose){
        DPFPFeatureExtraction extractor = 
        DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        }catch(DPFPImageQualityException e){
            return null;
        }   
    }
    
    public Image crearImagenHuella(DPFPSample sample){
       return  DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }
    
    public void dibujarHuella(Image image){
        lblHuella.setIcon(new ImageIcon(
            image.getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(),Image.SCALE_DEFAULT))
        );
        repaint();
    }//Termina dibujarHuella
    
    public void estadoHuellas(){
        EnviarTexto("Muestra de huellas necesarias para guardar Template");
        Reclutador.getFeaturesNeeded();
    }
    
    public void EnviarTexto(String string){
        txtStatus.append(string+ "\n");
    }
    
    public void escribirEnUsuario(String string){
        txtDatosUsuario.append(string+ "\n");
    }
    
    
   
    public void start(){
        Lector.startCapture();
        EnviarTexto("Utilizando el lector de huella digital");
    }
    
    public void stop(){
        Lector.stopCapture();
        EnviarTexto("No se esta utilizando el lector de huella digital");
    }
    
    public DPFPTemplate getTemplate(){
        return template;
    }
    
    public void setTemplate(DPFPTemplate template){
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }
    
    public void procesarCaptura(DPFPSample sample){
        //Procesar la muestra de la huella y crear un conjunto de caracteristicas para la inscripcion
        featuresInscription = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        //Procesar la muestra de la huella y crear un conjunto de caracteristicas para la verificacion
        featuresVerification = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
        
        //Comprobar la calidad de la muestra de la huella y lo agrega al reclutador si es bueno
        if(featuresInscription != null){
            try{
                System.out.println("Las caracteristicas de la huella han sido creadas");
                Reclutador.addFeatures(featuresInscription); //Agrega las caracteristicas de la huella a la plantilla a crear
                //Dibuja la huella dactilar capturada
                Image image = crearImagenHuella(sample);
                dibujarHuella(image);
               // btnVerificar.setEnabled(true);
               // btnIdentificar.setEnabled(true);
                
            }catch(DPFPImageQualityException e){
                System.err.println("Error: "+e.getMessage());
            }
            
            finally {
                estadoHuellas();
                //comprueba si la plantilla ha sido creada
                switch(Reclutador.getTemplateStatus()){
                    case TEMPLATE_STATUS_READY: //informe de exito y se detiene la captura de la huella
                        stop();
                        setTemplate(Reclutador.getTemplate());
                        EnviarTexto("La plantilla de la huella ha sido creada, ya puede verificarla y comprobarla");
                        /*btnIdentificar.setEnabled(false);
                        btnVerificar.setEnabled(false);
                        btnGuardar.setEnabled(true);
                        btnGuardar.grabFocus();*/
                        break;
                     
                    case TEMPLATE_STATUS_FAILED: //informe de fallas y reinicia la captura de huellas
                        Reclutador.clear();
                        stop();
                        estadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(controAcceso.this, "La plantilla de la huella no ha sido creda, reinicie el procedimiento");
                        start();
                        break;
                        
                }
            }
        }
    }//Termina procesarCaptura
    
    ConexionBD cn = new ConexionBD();
    
    public void guardarFoto(){
        /*imgFoto = b.getImagen();
        urlFoto = urlFoto = "C:\\Users\\joshua\\Desktop\\GYM\\fotosUsuarios\\user_"+nombreUsuario+".jpg";
        System.out.println(urlFoto);
        try {
          ImageIO.write((RenderedImage) imgFoto, "jpg", new File(urlFoto));
          EnviarTexto("Se guardo la imagen del usuario correctamente");
        }catch (IOException e){
            EnviarTexto("Error al guardar la imagen del usuario");
        }*/
    }
    
    public void guardarHuella() throws SQLException{
        //Contiene los datos del template de la huella actual
        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer sizeHuella = template.serialize().length;
 
        //Pregunta el nombre de la persona a la cual correspande la huella
        //nombreUsuario = JOptionPane.showInputDialog("Nombre");
        try {
            //Establece los valores para la sentencia SQL
            Connection c = cn.conectar();
            guardarFoto();
            PreparedStatement guardarStmt = c.prepareStatement("UPDATE customers SET fingerprint=?, abs_photo_route=? WHERE id=?");
            guardarStmt.setString(2, urlFoto);
            guardarStmt.setBinaryStream(1, datosHuella,sizeHuella);
            guardarStmt.setString(3,usuario.get(0).toString());
            //Ejecuta la sentencia
            guardarStmt.execute();
            guardarStmt.close();
            JOptionPane.showMessageDialog(null, "Huella y Foto guardada correctamente");
            cn.desconectar();
           // btnGuardar.setEnabled(false);
           // btnVerificar.grabFocus();
        } catch (  SQLException e) {
            System.err.println("Error al guardar los datos de la huella: "+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }//Termina guardarHuell
    
    public void obtenerUsuarios(){
        try {
            Connection c = cn.conectar();
            PreparedStatement consulta = c.prepareStatement("SELECT id, name, last_name  FROM customers where abs_photo_route = \"\" order by id desc limit 10");
            ResultSet rs = consulta.executeQuery();
            
            while(rs.next()){
                String id_cliente = rs.getString("id");
                String nombre = rs.getString("name");
                String apellidos = rs.getString("last_name");
                //comboUsuarios.addItem("ID "+id_cliente+" "+nombre+" "+apellidos);
            }
        } catch (SQLException e) {
             System.err.println("Error al incluir a los usuarios en el checkbox: "+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }
    
    public void obtenerUsuario(String id){
        try {
            Connection c = cn.conectar();
            PreparedStatement consulta = c.prepareStatement("SELECT name,last_name, age, birthday, created_at FROM customers WHERE id=?");
            consulta.setString(1, id);
            ResultSet rs = consulta.executeQuery();
            //Borramos todos los items del usuario seleccionado anteriormente
            usuario.removeAllElements();
             
            if(rs.next()){
                usuario.addElement(id);
                usuario.addElement(rs.getString("name"));
                usuario.addElement(rs.getString("last_name"));
                escribirEnUsuario("Usuario: "+rs.getString("name")+" "+rs.getString("last_name"));
                escribirEnUsuario("Edad: "+rs.getString("age"));
                escribirEnUsuario("Fecha de Nacimiento: "+rs.getString("birthday"));
                escribirEnUsuario("Fecha de Incripcion: "+rs.getString("created_at"));
            }
        } catch (SQLException e) {
             System.err.println("Error al incluir a los usuarios en el checkbox: "+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }
    
    public void verificarHuella(String nombre){
        try {
            Connection c = cn.conectar();
            PreparedStatement verificarSmt = c.prepareStatement("SELECT huehuella FROM somhue WHERE huenombre=?");
            verificarSmt.setString(1, nombre);
            ResultSet rs = verificarSmt.executeQuery();
            //Si se encuentra el nombre en la base de datos
            if(rs.next()){
                //Lee la plantilla correspondiente a la persona indicada
                byte templateBuffer[] = rs.getBytes("huehuella");
                
     
                //Crea una nueva plantilla a partir de la guardada en la base de datos
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                //Envia la plantilla creada al objeto contenedor del template del componente de huella digital
                setTemplate(referenceTemplate);
                
                //Compara las caracteristicas de la huella recientemente capturada 
                //con la plantilla guardada al usuario especifico en la base de datos
                DPFPVerificationResult result = Verificador.verify(featuresInscription, getTemplate());
                //Compara las plantillas (actual vs BD)
                if(result.isVerified())
                    JOptionPane.showMessageDialog(null, "La huella capturada coincide con la de "+nombre, "Verificacion de Huella",JOptionPane.INFORMATION_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null, "La huella capturada NO coincide con la de "+nombre, "Verificacion de Huella",JOptionPane.ERROR_MESSAGE);
            }else{ //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
                JOptionPane.showMessageDialog(null, "No existe un registro que coincida con "+nombre, "Verificacion de Huella",JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar los datos la huella: "+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }//Termina verificar huella
    
    //Identifica a una persona registrada por medio de su huella digital
    public void indentificarHuella() throws IOException{
        try {
            Connection c = cn.conectar();
            PreparedStatement identificarSmt = c.prepareStatement("SELECT id,name,last_name,birthday,age,fingerprint,abs_photo_route FROM customers");
            ResultSet rs = identificarSmt.executeQuery();
         
            while(rs.next()){
                //Lee la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("fingerprint");
                System.out.println(templateBuffer);
                int id_customer = rs.getInt("id");
                String nombre = rs.getString("name");
                String apellidos = rs.getString("last_name");
                urlFoto = rs.getString("abs_photo_route");
                System.out.println(nombre);
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                
                 Verificador.setFARRequested(DPFPVerification.LOW_SECURITY_FAR);
                 
                 //System.out.println(getTemplate());
                DPFPVerificationResult result = Verificador.verify(featuresVerification, getTemplate());
                
                if(result.isVerified()){
                    //JOptionPane.showMessageDialog(null, "La huella capturada es de "+nombre,"Identificacion huella",JOptionPane.INFORMATION_MESSAGE);
                    //Cargamos la foto del usuario
                    txtDatosUsuario.setText("");
                    escribirEnUsuario("Usuario: "+rs.getString("name")+" "+rs.getString("last_name"));
                    escribirEnUsuario("Edad: "+rs.getString("age"));
                    escribirEnUsuario("Fecha de Nacimiento: "+rs.getString("birthday"));
                    //Cargamos la foto del usuario
                    cargarFoto();
                    //Registramos asistencia del cliente
                    registrarAsistencia(id_customer,nombre,apellidos );
                    return;
                }    
            }
            JOptionPane.showMessageDialog(null, "No existe ningun registro que coincida con la huella ","Identificacion huella",JOptionPane.ERROR_MESSAGE);
            setTemplate(null);
        } catch (SQLException e) {
            System.err.println("Error al indentificar la huella dactilar "+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }//Termina identificarHuella
    
    public void registrarAsistencia(int id,String name, String last_name){
        try {
            Connection c = cn.conectar();
            PreparedStatement ps = c.prepareStatement("INSERT INTO assistance (id_customer,customer_name,customer_last_name) VALUES (?,?,?)");
            ps.setInt(1,id);
            ps.setString(2, name);
            ps.setString(3, last_name);
            ps.executeUpdate();
        }catch (SQLException e) {
            System.err.println("Error al registrar la asistencia del usuario"+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }//Termina registrarAsistencia
    
    public void cargarFoto() throws IOException{
        //Hacemos una instancia de la clase CargarImagen para cargar la foto del usuario en el jPanel
        CargarImagen imagen = new CargarImagen(urlFoto);
        panelFoto.removeAll();
        //Invocamos el metodo dibuja imagen, pasando de parametros, el commponente y su size a dibujar (width, height)
        imagen.dibujaImagen(panelFoto.getGraphics(),321,268);
        
    }
 
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanHue = new javax.swing.JPanel();
        lblHuella = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtStatus = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        panelFoto = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDatosUsuario = new javax.swing.JTextArea();
        btnSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Toma de foto y huella digital");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        PanHue.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Huella Digital"));

        javax.swing.GroupLayout PanHueLayout = new javax.swing.GroupLayout(PanHue);
        PanHue.setLayout(PanHueLayout);
        PanHueLayout.setHorizontalGroup(
            PanHueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanHueLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblHuella, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addContainerGap())
        );
        PanHueLayout.setVerticalGroup(
            PanHueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanHueLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        txtStatus.setColumns(20);
        txtStatus.setRows(5);
        jScrollPane1.setViewportView(txtStatus);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Foto Usuario"));

        panelFoto.setBackground(new java.awt.Color(0, 0, 0));
        panelFoto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout panelFotoLayout = new javax.swing.GroupLayout(panelFoto);
        panelFoto.setLayout(panelFotoLayout);
        panelFotoLayout.setHorizontalGroup(
            panelFotoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 319, Short.MAX_VALUE)
        );
        panelFotoLayout.setVerticalGroup(
            panelFotoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 266, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(panelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos usuario"));

        txtDatosUsuario.setColumns(20);
        txtDatosUsuario.setRows(5);
        jScrollPane2.setViewportView(txtDatosUsuario);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 10, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(PanHue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(78, 78, 78)
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PanHue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(95, 95, 95)))
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed
       
   
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
       
        Iniciar();
        start();
        estadoHuellas();
       // btnGuardar.setEnabled(false);
       // btnIdentificar.setEnabled(false);
       // btnVerificar.setEnabled(false);
        btnSalir.grabFocus();
        
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        stop();
    }//GEN-LAST:event_formWindowClosing
    
    Player player;
    String dispositivoSeleccionado;
    Component comp;  
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(controAcceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(controAcceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(controAcceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(controAcceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new controAcceso().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanHue;
    private javax.swing.JButton btnSalir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHuella;
    private javax.swing.JPanel panelFoto;
    private javax.swing.JTextArea txtDatosUsuario;
    private javax.swing.JTextArea txtStatus;
    // End of variables declaration//GEN-END:variables
}