import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer
{
    public static void main (String args[]) throws Exception {
        String requestMessageLine;
        String fileName;
        BufferedReader br;
        String lin = null;
        HashMap<String, String> config = new HashMap<String, String>();
        String[] trozos = new String[2];
        
        try {
            br = new BufferedReader(new FileReader("config.txt"));
            while ((lin = br.readLine()) != null) {
                trozos = lin.split("=");
                config.put(trozos[0], trozos[1]);
            } 
        } catch (IOException e) {
        
        }

        int myPort = Integer.parseInt(config.get("porto"));
        ServerSocket listenSocket = new ServerSocket (myPort);

        while(true) {
            System.out.println ("Escoitando o porto " + myPort);
            Socket connectionSocket = listenSocket.accept();
            BufferedReader inFromClient = new BufferedReader (new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream (connectionSocket.getOutputStream());
            
            // tratamos a primeira liña da petición
            requestMessageLine = inFromClient.readLine();
            System.out.println (requestMessageLine);

            String[] cachos = requestMessageLine.split("\\s");

            if (cachos[0].equals("GET")) {
                fileName = config.get("ruta") + cachos[1];
                String extension = "";
                int i = fileName.lastIndexOf('.');
                if(i > 0){
                extension = fileName.substring(i+1);
                }
                
                if(extension != "" && config.get("extension").contains(extension)){
                    if (fileName.startsWith("/") == true)
                        fileName = fileName.substring(1);
                    // ler o contido do ficheiro solicitado
                        File file = new File(fileName);
                if (file.exists()) {
                    // converter o ficheiro nun array de bytes
                    int numOfBytes = (int) file.length();
                    FileInputStream inFile = new FileInputStream (fileName);
                    byte[] fileInBytes = new byte[numOfBytes];
                    inFile.read(fileInBytes);

                    // enviar a contestación
                    outToClient.writeBytes ("HTTP/1.0 200 Document Follows\r\n");
                    if (fileName.endsWith(extension)){
                        outToClient.writeBytes ("\r\n");
                        outToClient.write(fileInBytes, 0, numOfBytes);
                    }
                    /*
                    if (fileName.endsWith(".jpg"))
                        outToClient.writeBytes ("Content-Type: image/jpeg\r\n");
                    if (fileName.endsWith(".gif"))
                        outToClient.writeBytes ("Content-Type: image/gif\r\n");
                    */
                    //outToClient.writeBytes ("Content-Length: " + numOfBytes + "\r\n");
                    outToClient.writeBytes ("\r\n");
                    outToClient.write(fileInBytes, 0, numOfBytes);
                } else {
                    File nonatopado = new File(config.get("error404"));
                    // converter o ficheiro nun array de bytes
                    int numOfBytes = (int) nonatopado.length();
                    FileInputStream inFile = new FileInputStream (config.get("error404"));
                    byte[] fileInBytes = new byte[numOfBytes];
                    inFile.read(fileInBytes);
                    outToClient.writeBytes("HTTP/1.0 404 NOT_FOUND\r\n");
                    outToClient.writeBytes ("\r\n");
                    outToClient.write(fileInBytes, 0, numOfBytes);
                    
                }
                // ler, sen tratar, o resto de liñas da petición
                requestMessageLine = inFromClient.readLine();
                while (requestMessageLine.length() >= 5) {
                    System.out.println (requestMessageLine);
                    requestMessageLine = inFromClient.readLine();
                }
                System.out.println (requestMessageLine);

                connectionSocket.close();
                }
            } else {
                System.out.println ("Petición incorrecta");
            }
        }
    }
}
