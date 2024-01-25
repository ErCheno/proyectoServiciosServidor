package com.example.servidorservicios;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
    protected void onCargarButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onEnviarButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    String sFTP = "172.18.185.27";
    int port = 21;
    String sUser = "emilio";
    String sPassword = "psp234";
    FTPClient client = new FTPClient();
    public void iniciarServidor(){
        try {

            client.connect(sFTP, port);
            client.login(sUser, sPassword);

            client.setFileType(FTP.BINARY_FILE_TYPE);

            //ftpClient.enterLocalPassiveMode();
            // APPROACH #1: uploads first file using an InputStream
            File firstLocalFile = new File("/home/usuario/Imágenes/Aules.png");
            long inicio = System.currentTimeMillis();
            long tamanyo = firstLocalFile.length();


            String firstRemoteFile = "Aules.png";
            InputStream inputStream;
            inputStream = new FileInputStream(firstLocalFile);

            System.out.println("Start uploading first file");
            boolean done;
            done = client.storeFile(firstRemoteFile, inputStream);

            inputStream.close();

            long tiempofinal = System.currentTimeMillis();
            System.out.println("Tiempo:" + (tiempofinal - inicio));
            if (done) {
                System.out.println("The first file is uploaded successfully. " + tamanyo + " bytes en " +
                        (tiempofinal - inicio) / 1000 + " b/s");
            }
        }catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
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