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


public class capturaHuella extends javax.swing.JFrame {
   jmfVideo b = new jmfVideo();
   Image imgFoto = null;
   String urlFoto = "";
   String nombreUsuario = "";
   
    /**
     * Creates new form capturaHuella
     */
     //Variables globales para operaciones de la huella
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    Date date = new Date();
    public capturaHuella() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Imposible modificar el tema visual","Look and Feel invalido",
            JOptionPane.ERROR_MESSAGE);
        }
       initComponents();
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
                btnVerificar.setEnabled(true);
                btnIdentificar.setEnabled(true);
                
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
                        btnIdentificar.setEnabled(false);
                        btnVerificar.setEnabled(false);
                        btnGuardar.setEnabled(true);
                        btnGuardar.grabFocus();
                        break;
                     
                    case TEMPLATE_STATUS_FAILED: //informe de fallas y reinicia la captura de huellas
                        Reclutador.clear();
                        stop();
                        estadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(capturaHuella.this, "La plantilla de la huella no ha sido creda, reinicie el procedimiento");
                        start();
                        break;
                        
                }
            }
        }
    }//Termina procesarCaptura
    
    ConexionBD cn = new ConexionBD();
    
    public void guardarFoto(){
        imgFoto = b.getImagen();
        urlFoto = "C:\\Users\\joshua\\Desktop\\GYM\\fotosUsuarios\\user_"+nombreUsuario+".jpg";
        try {
          ImageIO.write((RenderedImage) imgFoto, "jpg", new File(urlFoto));
          EnviarTexto("Se guardo la imagen del usuario correctamente");
        }catch (IOException e){
            EnviarTexto("Error al guardar la imagen del usuario");
        }
    }
    
    public void guardarHuella() throws SQLException{
        //Contiene los datos del template de la huella actual
        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer sizeHuella = template.serialize().length;
        
        //Pregunta el nombre de la persona a la cual correspande la huella
        nombreUsuario = JOptionPane.showInputDialog("Nombre");
        try {
            //Establece los valores para la sentencia SQL
            Connection c = cn.conectar();
            guardarFoto();
            PreparedStatement guardarStmt = c.prepareStatement("INSERT INTO usuarios (nombre, huella, foto) values (?,?,?)");
            guardarStmt.setString(1, nombreUsuario);
            guardarStmt.setBinaryStream(2, datosHuella,sizeHuella);
            guardarStmt.setString(3,urlFoto);
            //Ejecuta la sentencia
            guardarStmt.execute();
            guardarStmt.close();
            JOptionPane.showMessageDialog(null, "Huella guardada correctamente");
            cn.desconectar();
            btnGuardar.setEnabled(false);
            btnVerificar.grabFocus();
        } catch (  SQLException e) {
            System.err.println("Error al guardar los datos de la huella: "+e.getMessage());
        }finally{
            cn.desconectar();
        }
    }//Termina guardarHuella
    
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
            PreparedStatement identificarSmt = c.prepareStatement("SELECT nombre,huella,foto FROM usuarios");
            ResultSet rs = identificarSmt.executeQuery();
         
            while(rs.next()){
                //Lee la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("huella");
                System.out.println(templateBuffer);
                String nombre = rs.getString("nombre");
                urlFoto = rs.getString("foto");
                System.out.println(nombre);
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                
                 Verificador.setFARRequested(DPFPVerification.LOW_SECURITY_FAR);
                 
                 //System.out.println(getTemplate());
                DPFPVerificationResult result = Verificador.verify(featuresVerification, getTemplate());
                
                if(result.isVerified()){
                    JOptionPane.showMessageDialog(null, "La huella capturada es de "+nombre,"Identificacion huella",JOptionPane.INFORMATION_MESSAGE);
                    //Cargamos la foto del usuario
                    cargarFoto();
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
    
    public void cargarFoto(){
        //BufferedImage foto = null;
        //Graphics g;
        
        
JLabel img = new JLabel(" ");

ImageIcon image = new ImageIcon(urlFoto);

panelFoto.add(img); // "dibujar" es mi panel ok...

//Propiedades de la etiqueta
img.setIcon(image);
img.setSize(135,135);
img.setLocation(550,20);
img.setVisible(true); 
        //ImageIcon pic = new ImageIcon(urlFoto);
        
       // System.out.println(pic)
      
        //System.out.println(urlFoto);
      // panelFoto.add(new JLabel(pic));
       
       img.setIcon(image);
       img.setSize(135,135);
img.setLocation(550,20);
img.setVisible(true); 
       
       
    }
    
 
 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanHue = new javax.swing.JPanel();
        lblHuella = new javax.swing.JLabel();
        PanBtns = new javax.swing.JPanel();
        btnVerificar = new javax.swing.JButton();
        btnIdentificar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtStatus = new javax.swing.JTextArea();
        comboDispositivos = new javax.swing.JComboBox();
        btnIniciar = new javax.swing.JButton();
        btnTomar = new javax.swing.JButton();
        panelFoto = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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
                .addComponent(lblHuella, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addContainerGap())
        );
        PanHueLayout.setVerticalGroup(
            PanHueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanHueLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        PanBtns.setBorder(javax.swing.BorderFactory.createTitledBorder("Acciones"));

        btnVerificar.setText("Verificar");
        btnVerificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerificarActionPerformed(evt);
            }
        });

        btnIdentificar.setText("Identificar");
        btnIdentificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIdentificarActionPerformed(evt);
            }
        });

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanBtnsLayout = new javax.swing.GroupLayout(PanBtns);
        PanBtns.setLayout(PanBtnsLayout);
        PanBtnsLayout.setHorizontalGroup(
            PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanBtnsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanBtnsLayout.createSequentialGroup()
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(PanBtnsLayout.createSequentialGroup()
                        .addComponent(btnIdentificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVerificar, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanBtnsLayout.setVerticalGroup(
            PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanBtnsLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVerificar)
                    .addComponent(btnIdentificar))
                .addGap(32, 32, 32)
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalir)
                    .addComponent(btnGuardar))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        txtStatus.setColumns(20);
        txtStatus.setRows(5);
        jScrollPane1.setViewportView(txtStatus);

        comboDispositivos.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnIniciar.setText("Iniciar");
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });

        btnTomar.setText("Tomar Foto");
        btnTomar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTomarActionPerformed(evt);
            }
        });

        panelFoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panelFotoLayout = new javax.swing.GroupLayout(panelFoto);
        panelFoto.setLayout(panelFotoLayout);
        panelFotoLayout.setHorizontalGroup(
            panelFotoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );
        panelFotoLayout.setVerticalGroup(
            panelFotoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 255, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 648, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PanBtns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(PanHue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboDispositivos, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(btnTomar))
                            .addComponent(panelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PanHue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PanBtns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(comboDispositivos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTomar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed

    private void btnVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerificarActionPerformed
        // TODO add your handling code here:
        String nombre = JOptionPane.showInputDialog("Nombre a verificar: ");
        verificarHuella(nombre);
        Reclutador.clear();
    }//GEN-LAST:event_btnVerificarActionPerformed
       
   
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        //CARGAMOS LIBRERIA JMF PARA MAJEJO MULTIMEDIA 
        //LIMPIAMOS EL COMBO
        comboDispositivos.removeAllItems();
        //VARIABLE PARA TODOS LOS DISPOSITIVOS
        Vector listaDispositivos = null;
        //OBTENER TODOS LOS DISPOSITIVOS INSYTALADOS
        listaDispositivos = CaptureDeviceManager.getDeviceList(null);
        for (int i = 0; i < listaDispositivos.size(); i++) {
            //OBETNER INFORMACION DEL DISPOSITIVO
            CaptureDeviceInfo info = (CaptureDeviceInfo) listaDispositivos.get(i);
            String nombreDispositivo = info.getName().toString();
            //SOLO DISPOSITIVOS DEL TIPO IMAGE
             if(nombreDispositivo.indexOf("image")!= -1 || nombreDispositivo.indexOf("Image")!= -1 ){
                 //CARGAMOS EL DISPOSITVO AL COMBO
                 comboDispositivos.addItem(nombreDispositivo);
             }
        }
        comboDispositivos.setSelectedIndex(0);
        Iniciar();
        start();
        estadoHuellas();
        btnGuardar.setEnabled(false);
        btnIdentificar.setEnabled(false);
        btnVerificar.setEnabled(false);
        btnSalir.grabFocus();
        
    }//GEN-LAST:event_formWindowOpened

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // TODO add your handling code here:
        try {
            guardarHuella();
            Reclutador.clear();
            lblHuella.setIcon(null);
            start();
        } catch (SQLException e) {
            Logger.getLogger(capturaHuella.class.getName()).log(Level.SEVERE,null,e);
        }
        
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        stop();
    }//GEN-LAST:event_formWindowClosing

    private void btnIdentificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIdentificarActionPerformed
        // TODO add your handling code here:
         try {
            indentificarHuella();
            Reclutador.clear();
        } catch (IOException e) {
            Logger.getLogger(capturaHuella.class.getName()).log(Level.SEVERE,null,e);
        }
    }//GEN-LAST:event_btnIdentificarActionPerformed
    
    Player player;
    String dispositivoSeleccionado;
    Component comp;
    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
        
        /*try {
            dispositivoSeleccionado = comboDispositivos.getSelectedItem().toString();
            //SE OBTIENE EL DISPOSITIVO
            CaptureDeviceInfo device = CaptureDeviceManager.getDevice(dispositivoSeleccionado);
            //OBTENER FUENTE DE CAPTURA
            MediaLocator localizador = device.getLocator();
            //SE INICIALIZA EL PLAYER Y SE EJECUTA
            player = Manager.createRealizedPlayer(localizador);
            //SE INCIA EL PLAYER
            player.start();
            if((comp = player.getVisualComponent()) != null){
                panelFoto.add(comp,BorderLayout.CENTER);
            }
        } catch (Exception e) {
        }*/
        
        panelFoto.setLayout(new javax.swing.BoxLayout(panelFoto, javax.swing.BoxLayout.LINE_AXIS));
        //se añade el componente de video
        panelFoto.add(b.Componente());
        //imagen.setText("");
    }//GEN-LAST:event_btnIniciarActionPerformed

    private void btnTomarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTomarActionPerformed

        b.capturarImagen();
        b.stop();
        guardarFoto();
    }//GEN-LAST:event_btnTomarActionPerformed
  
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
            java.util.logging.Logger.getLogger(capturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(capturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(capturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(capturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new capturaHuella().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanBtns;
    private javax.swing.JPanel PanHue;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnIdentificar;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnTomar;
    private javax.swing.JButton btnVerificar;
    private javax.swing.JComboBox comboDispositivos;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblHuella;
    private javax.swing.JPanel panelFoto;
    private javax.swing.JTextArea txtStatus;
    // End of variables declaration//GEN-END:variables
}