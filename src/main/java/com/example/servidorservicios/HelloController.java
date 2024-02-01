package com.example.servidorservicios;

import jakarta.mail.internet.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import jakarta.mail.*;
public class HelloController {

    //Atributos FXML

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


    //Atributos para conectarse al server FTP
    private String fileUrl = "";
    private String fileRemotoUrl = "";
    String sFTP = "localhost";
    int port = 14148;
    String sUser = "emilio";
    String sPassword = "123";
    FTPClient client = new FTPClient();
    String from = "emiliojesus786@gmail.com";
    String password = "pavycnhvpzwawihp";
    String emailTo = "pikachuloko98@gmail.com";
    String emailSubject = "Informe de Transferencia";
    String emailBody;
    Text textoDinamico;
    int contador = -1;


    @FXML
    protected void onCargarButtonClick() {
        try{
            textoDinamico = new Text("--------------------------\nBienvenidos a la prueba de servicios");
            vBoxInformacion.getChildren().add(textoDinamico);

            String tipoArchivo = comprobadorFichero(urlTextField.getText());
            if (tipoArchivo != null){
                String[] dividirRemotoFile = urlTextField.getText().split("/"); //Para dividir el fichero con solo el nombre
                for (int i = 0; i < dividirRemotoFile.length; i++) {
                    contador++;

                }

                fileUrl = urlTextField.getText();
                contador = Math.min(contador, dividirRemotoFile.length - 1);
                fileRemotoUrl = dividirRemotoFile[contador];

                textoDinamico = new Text("--------------------------\nFichero: "+fileRemotoUrl+"\n"+" Fichero cargado con éxito");
                vBoxInformacion.getChildren().add(textoDinamico);

                descargarArchivo(fileUrl);
            }



        }catch (FileNotFoundException ex){
            mostrarError("Fichero no encontrado");
        }
        catch (ArrayIndexOutOfBoundsException ex){
            mostrarError("Debe de añadir el archivo");
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Este método comprueba si el fichero es correcto
     * y posteriormente carga el fichero en cuestión
     *
     * Mediante un split se divide el fichero de ruta absoluta a remota
     */




    @FXML
    protected void onEnviarButtonClick() {
        FTPClient ftpClient = new FTPClient();
        try{
            ftpClient.connect(sFTP);
            mostrarRespuestaServidor(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            textoDinamico = new Text("Conectando con el servidor FTP");
            vBoxInformacion.getChildren().add(textoDinamico);
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                textoDinamico = new Text("Conexión fallida");
                vBoxInformacion.getChildren().add(textoDinamico);
                return;
            }


            boolean loginHecho = ftpClient.login(usuarioTextField.getText(), claveTextField.getText());
            mostrarRespuestaServidor(ftpClient);

            if (!loginHecho) {
                textoDinamico = new Text("No se pudo logear al servidor");
                vBoxInformacion.getChildren().add(textoDinamico);
                return;
            }
            long inicio=System.currentTimeMillis();
            long tamanyo=fileUrl.length();

            InputStream inputStream = new FileInputStream(fileRemotoUrl);
            long tiempofinal=System.currentTimeMillis();

            textoDinamico = new Text("El fichero se subió correctamente. "+tamanyo+" bytes en "+
                    (tiempofinal-inicio)/1000+" b/s");
            vBoxInformacion.getChildren().add(textoDinamico);

            boolean done = ftpClient.storeFile(fileRemotoUrl, inputStream);

            if (done) {
                textoDinamico = new Text("Conseguido el enviar fichero por FTP");
                vBoxInformacion.getChildren().add(textoDinamico);
            }else {
                System.out.println("El fichero no se envió");
            }
            FTPFile[] files1 = ftpClient.listFiles(fileRemotoUrl);
            if (files1!=null){
                imprimirDetallesFichero(files1);
            }

            String[] files2 = ftpClient.listNames();
            if (files2 != null){
                imprimirNombres(files2);
            }

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //ftpClient.enterLocalPassiveMode();



            inputStream.close();

            emailBody = "Transferencia exitosa:\n" +
                    "Servidor FTP: " + sFTP + "\n" +
                    "Usuario FTP: " + sUser + "\n" +
                    "Archivo: " + fileUrl;
            enviarCorreo(emailTo, emailSubject, emailBody);
        }catch (FileNotFoundException ex){
            mostrarError("Fichero no encontrado");
        }

        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                textoDinamico = new Text("Se ha desconectado");
                vBoxInformacion.getChildren().add(textoDinamico);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
     }
    }

    /**
     * El método crea dentro de él un nuevo FTPClient para conectarse nuevamente
     * Y mediante otros métodos se muestra toda la información de los ficheros
     *
     * Si no se encuentra el fichero mostrará un Alert de tipo ERROR
     */

    public HelloController(){
        iniciarServidor();
    }
    private static void enviarCorreo(String to, String subject, String body) {
        Properties prop = new Properties();
        prop.put("mail.smtp.username", "emiliojesus786@gmail.com");
        prop.put("mail.smtp.password", "pavycnhvpzwawihp");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(prop.getProperty("mail.smtp.username"), prop.getProperty("mail.smtp.password"));
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("emiliojesus786@gmail.com"));
            //InternetAddress[] toEmailAddresses = InternetAddress.parse("emiliojesus786@gmail.com, pikachuloko98@gmail.com");

   /*         Multipart multipart = getMultipart();
            message.setContent(multipart);*/
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Correo electrónico enviado.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static Multipart getMultipart() throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart();

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Please find the attachment sent using Jakarta Mail");
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        String filename = "C:\\Users\\2dam01\\Desktop\\servidorServicios\\FileZilla\\1024px-Collage_of_Six_Cats-02.jpg";
        messageBodyPart.attachFile(filename);
        // Add the BodyPart to the Multipart object
        multipart.addBodyPart(messageBodyPart);
        return multipart;
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
        try {
            client.connect(sFTP);
            client.login(sUser, sPassword);
            client.setFileType(FTP.BINARY_FILE_TYPE);
        }catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        Platform.runLater(()->{
            usuarioTextField.setText(sUser);
            ftpTextField.setText(sFTP);
            claveTextField.setText(sPassword);
            puertoServTextField.setText(String.valueOf(port));
        });


    }



    public String comprobadorFichero(String ruta){
        int indexPunto = ruta.lastIndexOf('.');
        if (indexPunto != -1 && indexPunto < ruta.length() - 1){
            return ruta.substring(indexPunto + 1).toLowerCase();
        }
        return null;
    }

    /**
     * Se comprueba el punto final de la ruta pasada por parámetro
     * y se verifica que se haya encontrado un punto y que el punto no sea el último carácter de la cadena
     * Si no se encuentra un punto en la cadena el método devuelve null
     * @param ftpClient
     */

    private void mostrarRespuestaServidor(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null & replies.length > 0) {
            for (String aReply : replies) {
                textoDinamico = new Text("SERVER: " + aReply);
                vBoxInformacion.getChildren().add(textoDinamico);

            }
        }
    }

    /**
     * Recoge las respuestas del servidor FTP después de realizar una operación
     * @param mensaje
     */
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
    private void imprimirDetallesFichero(FTPFile[] files) {
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (FTPFile file : files) {
            String details = file.getName();
            if (file.isDirectory()) {
                details = "[" + details + "]";
            }
            details += "\t\t" + file.getSize();
            details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());

            textoDinamico = new Text("--------------------------\nDetalles: \n"+details+"\n--------------------------\n");
            vBoxInformacion.getChildren().add(textoDinamico);

        }
    }
    private void imprimirNombres(String[] files) {
        if (files != null & files.length > 0) {
            textoDinamico = new Text("--------------------------\nFicheros:");
            vBoxInformacion.getChildren().add(textoDinamico);
            for (String aFile: files) {
                textoDinamico = new Text(aFile);
                vBoxInformacion.getChildren().add(textoDinamico);
            }
            textoDinamico = new Text("--------------------------");
            vBoxInformacion.getChildren().add(textoDinamico);
        }
    }

}