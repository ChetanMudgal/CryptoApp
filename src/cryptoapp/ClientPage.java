/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoapp;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author chetan mudgal
 */
public class ClientPage extends ChatGui{
    private Socket s=null;
    ClientPage(String ip,int port) throws IOException{
    s=new Socket(ip,port);
    super.setSendSocket(s);
    startservices();
    }
    public void startservices(){
        System.out.println("services are started!");
     
    new MessageListener(this,s,ClientPage.this.getpubkey(), super.getPrivateKey());
    }
    public Socket getSocket(){
    return s;
    }
    
}
