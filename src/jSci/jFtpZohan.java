package jSci;

import com.zehon.exception.FileTransferException;
import com.zehon.sftp.SFTPClient;
import java.io.File;

public class jFtpZohan {
  public void jFtp() {}
  
  public boolean enviarArchivo(String destFolder, String filePath, String hostName, String user) {
    boolean envio = true;
    try {
      File source = new File(filePath);
      System.out.println("filePath");
      try {
        System.out.println("Uploading: " + source);
        //SFTPClient ftp = new SFTPClient(hostName, 22, user, "claver12.ppk", true);
        SFTPClient ftp = new SFTPClient(hostName,7522, user, "h2h&7h8#!");
        ftp.sendFile(filePath, destFolder);
        System.out.println(filePath + " got sftp-ed successfully to  folder " + destFolder);
      } catch (FileTransferException e) {
        System.out.println("Fail to ssftp  to  folder " + destFolder);
        e.printStackTrace();
      } 
    } catch (Exception e) {
      envio = false;
      System.out.println(e.toString());
      e.printStackTrace();
    } 
    return envio;
  }
}
