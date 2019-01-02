/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoapp;

import cryptoapp.security.AES;
import cryptoapp.security.DES;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 *
 * @author chetan mudgal
 */
public class MessageTransfer {
    String message, hostname, choice = "AES";
    int port;
  
    String pubKeyRSA_sender;
    PrivateKey privateKeyRSA_sender;
    
    private static PublicKey getPublicKey(String base64PublicKey){
        PublicKey publicKey = null;
        try{
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }
    
    private static byte[] encrypt(String key, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(key));  

        return cipher.doFinal(message.getBytes());  
    }
    
    public static byte[] encrypt(PrivateKey key, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
        cipher.init(Cipher.ENCRYPT_MODE, key);  

        return cipher.doFinal(Base64.getDecoder().decode(message));  
    }
    
    String addSignature(String msg) throws Exception
    {
        try { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(Base64.getDecoder().decode(msg)); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            }
            String encrypted=Base64.getEncoder().encodeToString(encrypt(privateKeyRSA_sender,hashtext));
            System.out.println("Hash Generated (MD5): " + hashtext);
            System.out.println("Encrypted Hash: " + encrypted);
            System.out.println("Digitally Signed Message sent: " + encrypted + msg);
            // System.out.println(encrypted.length());
            return (encrypted + msg); 
        }  
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        }
    }
    
    
    MessageTransfer(Socket s,String msg,WritableGui gui,String choice, String pubKeyRSA_sender, PrivateKey privateKeyRSA_sender) throws IOException, NoSuchAlgorithmException, Exception{
      
        this.choice = choice;
        this.pubKeyRSA_sender = pubKeyRSA_sender;
        this.privateKeyRSA_sender = privateKeyRSA_sender;
        this.message=msg;
      
         System.out.println("now i am here");
           new Thread(){
               @Override
               public void run(){
                   System.out.println("thread created");
                 DataOutputStream dos = null;
                   try {
                       System.out.println("thread created1");
                       DataInputStream dis=null;
                       try {
                           dis = new DataInputStream(s.getInputStream());
                       } catch (IOException ex) {
                           Logger.getLogger(MessageTransfer.class.getName()).log(Level.SEVERE, null, ex);
                       }
                      System.out.println("thread created2");
                       dos = new DataOutputStream(s.getOutputStream());
                          System.out.println("thread created2");
                          
                         String pubKeyRSA_receiver=dis.readUTF();
                         System.out.println("thread created3");
                       // generating AES or DES key
                       KeyGenerator keyGen;
                       SecretKey key;
                      
                       if(choice.equals("AES"))
                       {
                           keyGen = KeyGenerator.getInstance("AES");
                           keyGen.init(128);
                           key= keyGen.generateKey();
                       }
                       else
                       {
                           key = KeyGenerator.getInstance("DES").generateKey();
                       }
                       
                       String secretKey=Base64.getEncoder().encodeToString(key.getEncoded());
                       System.out.println(choice + " key generated: " + secretKey);
                       // encrypting AES or DES key with public RSA key of the receiver
                       String encKey=Base64.getEncoder().encodeToString(encrypt(pubKeyRSA_receiver,secretKey));
                       System.out.println("Encypted " + choice + " key using RSA: " + encKey);
                       String messageCopy = message;
                       if(choice.equals("AES"))
                           message = AES.encrypt(message, secretKey);
                       else
                           message = DES.encrypt(message, secretKey);
                       System.out.println("Original Message: " + messageCopy);
                       System.out.println("Encrypted Message: " + message);
                       // generating digital signature using MD-5
                       
                       System.out.println("thread created3");
                       dos.writeUTF(pubKeyRSA_sender);
                       dos.writeUTF(addSignature(message));
                       //              dos.writeUTF(msg);
                       dos.writeUTF(message);
                       String finalmessage = "";
                       for(int i=0;i<5;i++)
                           finalmessage+=" ";
                       gui.write("Sent:" + finalmessage + messageCopy);
                       dos.writeUTF(choice + "---" + encKey);
                       System.out.println("Message sent");
                   } catch (IOException ex) {
                       Logger.getLogger(MessageTransfer.class.getName()).log(Level.SEVERE, null, ex);
                   } catch (NoSuchAlgorithmException ex) {
                       Logger.getLogger(MessageTransfer.class.getName()).log(Level.SEVERE, null, ex);
                   } catch (Exception ex) {
                       Logger.getLogger(MessageTransfer.class.getName()).log(Level.SEVERE, null, ex);
                   } finally {
                       try {
                           dos.close();
                       } catch (IOException ex) {
                           Logger.getLogger(MessageTransfer.class.getName()).log(Level.SEVERE, null, ex);
                       }
                   }
              }
          }.start();
                   
       
    }
}
