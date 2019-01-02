/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chetan mudgal
 */
public class ServerPage extends ChatGui {
    private ServerSocket ss=null;
    private Socket s=null;
    ServerPage(int port) throws IOException{
        ss=new ServerSocket(port);
      //   s=ss.accept();
       // new MessageListener(this,s);
    }
   
    public void startservice() throws IOException{
        System.out.println("services are started");
      
        new Thread(){
             @Override
             public void run(){
                 try {
                     s=ss.accept();
                     setSendSocket(s);
                     System.out.println(""+s.getInetAddress()+" "+s.getPort());
                 } catch (IOException ex) {
                     Logger.getLogger(ServerPage.class.getName()).log(Level.SEVERE, null, ex);
                 }
         
                // new MessageListener(ServerPage.this,s, ServerPage.this.getpubkey(), ServerPage.this.getPrivateKey());
             }
         }.start();
    }
    public  Socket getSocket(){
    return s;
    }
     
}
