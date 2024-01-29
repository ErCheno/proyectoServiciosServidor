package com.example.servidorservicios;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import javax.net.ssl.ExtendedSSLSession;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextField usuarioTextField;
    @FXML
    private TextField claveTextField;
    @FXML
    private TextField urlTextField;
    @FXML
    private TextField ftpTextField;
    @FXML
    private TextField puertoServTextField;
    @FXML
    private Button cargarButton;
    @FXML
    private VBox vBoxInformacion;

    private String fileUrl = "";
    private String fileRemotoUrl = "";
    String sFTP = "localhost";
    int port = 21;
    String sUser = "emilio";
    String sPassword = "psp234";
    FTPClient client = new FTPClient();
    String emailTo = "jesua.educa@gmail.com";
    String emailSubject = "Informe de Transferencia";
    String emailBody;
    Text textoDinamico;
    int contador = -1;


    @FXML
    protected void onCargarButtonClick() {
        try{
            textoDinamico = new Text("Bienvenidos a la prueba de servicios");
            vBoxInformacion.getChildren().add(textoDinamico);

            String tipoArchivo = comprobadorFichero(urlTextField.getText());
            if (tipoArchivo != null){

            }
            //ftpClient.enterLocalPassiveMode();
            // APPROACH #1: uploads first file using an InputStream

                String[] dividirRemotoFile = urlTextField.getText().split("/");
            for (int i = 0; i < dividirRemotoFile.length; i++) {
                contador++;
            }

            fileUrl = urlTextField.getText();
            fileRemotoUrl = dividirRemotoFile[contador];

            File firstLocalFile = new File(fileUrl);
            textoDinamico = new Text("Fichero: "+fileRemotoUrl);
            vBoxInformacion.getChildren().add(textoDinamico);

            descargarArchivo(fileUrl);

            /*try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                int longitudBytes = connection.getContentLength();
                System.out.println("La longitud del archivo es: " + longitudBytes + " bytes");

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream outputStream = new FileOutputStream(dividirRemotoFile[contador])) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("Archivo descargado exitosamente.");
            } catch (IOException e) {
                e.printStackTrace();
            }
*/

            String firstRemoteFile = "coche_informe_serv.pdf";
            InputStream inputStream;
            inputStream = new FileInputStream(firstLocalFile);

            boolean done;
            done = client.storeFile(firstRemoteFile, inputStream);
            if (done) {
                textoDinamico = new Text("------ Fichero cargado con éxito");
                vBoxInformacion.getChildren().add(textoDinamico);

            }
        }catch (ArrayIndexOutOfBoundsException ex){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            textoDinamico = new Text("ERROR");
            vBoxInformacion.getChildren().add(textoDinamico);
            alert.setContentText("Error: debe de añadir un archivo");
            alert.showAndWait();
        } catch (IOException ex) {
        System.out.println("Error: " + ex.getMessage());
        ex.printStackTrace();

    } /*finally {
        try {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
     }*/

    }



    @FXML
    protected void onEnviarButtonClick() {
        try{
            emailBody = "Transferencia exitosa:\n" +
                    "Servidor FTP: " + sFTP + "\n" +
                    "Usuario FTP: " + sUser + "\n" +
                    "Archivo: " + fileUrl;
            enviarCorreo(emailTo, emailSubject, emailBody);
        }finally {
        try {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
     }

    }
    public HelloController(){
        iniciarServidor();
    }
    private static void enviarCorreo(String to, String subject, String body) {
 /*       String from = "tu_correo@gmail.com";
        String password = "tu_contraseña_correo";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        ExtendedSSLSession session = ExtendedSSLSession.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
        try {
        MessageFormat message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);

        System.out.println("Correo electrónico enviado.");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private String descargarArchivo(String url) throws IOException {

        URLConnection connection = new URL(url).openConnection();

        int longitudBytes = connection.getContentLength();
        textoDinamico = new Text("La longitud del archivo es: "+longitudBytes+" bytes");
        vBoxInformacion.getChildren().add(textoDinamico);
        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
             FileOutputStream outputStream = new FileOutputStream(fileRemotoUrl)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return fileRemotoUrl;
    }

    public void iniciarServidor(){
        try {
            client.connect(sFTP);
            client.login(sUser, sPassword);
            client.setFileType(FTP.BINARY_FILE_TYPE);

        }catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String comprobadorFichero(String ruta){
        int indexPunto = ruta.lastIndexOf('.');
        if (indexPunto != -1 && indexPunto < ruta.length() - 1){
            String tipo = ruta.substring(indexPunto + 1).toLowerCase();
            return tipo;
        }
        return null;
    }


    private static void impromirDetallesArchivo(FTPFile[] files) {
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (FTPFile file : files) {
            String details = file.getName();
            if (file.isDirectory()) {
                details = "[" + details + "]";
            }
            details += "\t\t" + file.getSize();
            details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());
            System.out.println(details);
        }
    }

    private static void imprimirNombres(String files[]) {
        if (files != null & files.length > 0) {
            for (String aFile: files) {
                System.out.println(aFile);
            }
        }
    }
    private static void mostrarRespuestaServidor(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null & replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }


}