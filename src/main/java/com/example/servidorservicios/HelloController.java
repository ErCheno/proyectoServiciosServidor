package com.example.servidorservicios;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextField usuarioTextField;
    @FXML
    private PasswordField claveTextField;
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
    int port = 14148;
    String sUser = "emilio";
    String sPassword = "123";
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
                String[] dividirRemotoFile = urlTextField.getText().split("/");
                for (int i = 0; i < dividirRemotoFile.length; i++) {
                    contador++;

                }

                fileUrl = urlTextField.getText();
                contador = Math.min(contador, dividirRemotoFile.length - 1);
                fileRemotoUrl = dividirRemotoFile[contador];

                textoDinamico = new Text("Fichero: "+fileRemotoUrl);
                vBoxInformacion.getChildren().add(textoDinamico);

                descargarArchivo(fileUrl);
            }



        }catch (ArrayIndexOutOfBoundsException ex){
            mostrarError("Debe de añadir el archivo");
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }




    @FXML
    protected void onEnviarButtonClick() {
        FTPClient ftpClient = new FTPClient();
        try{
            ftpClient.connect(sFTP);
            ftpClient.login(usuarioTextField.getText(), claveTextField.getText());

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //ftpClient.enterLocalPassiveMode();

            long inicio=System.currentTimeMillis();
            long tamanyo=fileUrl.length();



            File firstLocalFile = new File("C:/Users/emili/IdeaProjects/proyectoServiciosServidor/gatocomuneuropeo.jpeg");
            System.out.println(fileRemotoUrl);
            InputStream inputStream = new FileInputStream(firstLocalFile);
            long tiempofinal=System.currentTimeMillis();

            boolean done = ftpClient.storeFile(fileRemotoUrl, inputStream);
            inputStream.close();
            System.out.println("The first file is uploaded successfully. "+tamanyo+" bytes en "+
                    (tiempofinal-inicio)/1000+" b/s");

            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

            if (done) {
                textoDinamico = new Text("------ Fichero cargado con éxito");
                vBoxInformacion.getChildren().add(textoDinamico);
            }else {
                System.out.println("El fichero no se envió");
            }
            emailBody = "Transferencia exitosa:\n" +
                    "Servidor FTP: " + sFTP + "\n" +
                    "Usuario FTP: " + sUser + "\n" +
                    "Archivo: " + fileUrl;
            enviarCorreo(emailTo, emailSubject, emailBody);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
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
       /* final Properties prop = new Properties();
        prop.put("mail.smtp.username", "usuario@gmail.com");
        prop.put("mail.smtp.password", "passwordEmail");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = ExtendedSSLSession.getInstance(properties, new Authenticator() {
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
        try {
            URL urlArchivo = new URL(url.replace("\\", "/"));  // Reemplazar barras invertidas con barras normales
            HttpURLConnection connection = (HttpURLConnection) urlArchivo.openConnection();

            int longitudBytes = connection.getContentLength();
            if (longitudBytes < 0) {
                throw new IOException("No se pudo obtener la longitud del archivo");
            }

            // Extraer el nombre del archivo de la URL
            String fileName = Paths.get(urlArchivo.getPath()).getFileName().toString();

            Platform.runLater(() -> {
                textoDinamico = new Text("La longitud del archivo es: " + longitudBytes + " bytes");
                vBoxInformacion.getChildren().add(textoDinamico);
            });

            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream outputStream = new FileOutputStream(fileName)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return fileName;
        } catch (MalformedURLException e) {
            mostrarError("URL mal formada: " + e.getMessage());
        } catch (IOException e) {
            mostrarError("Error durante la descarga: " + e.getMessage());
        } finally {
            try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }


    public void iniciarServidor(){
        Platform.runLater(()->{
            ftpTextField.setText(sFTP);
            usuarioTextField.setText(sUser);
            claveTextField.setText(sPassword);
            puertoServTextField.setText(String.valueOf(port));
        });

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
            return ruta.substring(indexPunto + 1).toLowerCase();
        }
        return null;
    }

    private static void mostrarRespuestaServidor(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null & replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }
    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            textoDinamico = new Text("ERROR");
            vBoxInformacion.getChildren().add(textoDinamico);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

}