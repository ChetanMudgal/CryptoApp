/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoapp;

import cryptoapp.security.AES;
import cryptoapp.security.DES;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author chetan mudgal
 */
public class MessageListener {

    WritableGui gui;
     String pubKeyRSA_receiver;
    PrivateKey privateKeyRSA_receiver;

    
     public static PublicKey getPublicKey(String base64PublicKey){
        PublicKey publicKey = null;
        try{
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey));
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
    
    public static byte[] decrypt(String key, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, getPublicKey(key));
        // System.out.println("werwER");
        return cipher.doFinal(encrypted);
    }
    
    public static byte[] decrypt(PrivateKey key, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encrypted);
    }
    
    boolean verifySignature(String cipher, String oppPublicKeyRSA) throws Exception
    {
        String sig=cipher.substring(0,344);
        String msg=cipher.substring(344);
        System.out.println("Digital Signature recieved: " + sig);
        String hashReceived=Base64.getEncoder().encodeToString(decrypt(oppPublicKeyRSA,Base64.getDecoder().decode(sig)));
        MessageDigest md = MessageDigest.getInstance("MD5"); 
        byte[] messageDigest = md.digest(Base64.getDecoder().decode(msg)); 
        BigInteger no = new BigInteger(1, messageDigest); 
        String hashtext = no.toString(16); 
        while (hashtext.length() < 32) { 
            hashtext = "0" + hashtext; 
        }
        System.out.println("Received Hash: " + hashReceived);
        System.out.println("Hash Generated: " + hashtext);
        if(hashtext.equals(hashReceived))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    MessageListener(WritableGui gui, Socket s, String pubKeyRSA, PrivateKey privateKeyRSA) {
        this.gui = gui;
        this.pubKeyRSA_receiver = pubKeyRSA;
        this.privateKeyRSA_receiver = privateKeyRSA;
        new Thread() {
            @Override
            public void run() {
               
                String line = "";
                try {
                     // sending public key to sender
                DataOutputStream dos=new DataOutputStream(s.getOutputStream());
                dos.writeUTF(pubKeyRSA_receiver);
                    System.out.println("this is written: "+pubKeyRSA_receiver);
//                    InputStream is;
//                    is = s.getInputStream();
//                    System.out.println("Listener: "+s.getInetAddress()+" "+s.getPort());
                    //BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    
                     // reading public key RSA of sender
                String publicKeyRSA_sender = dis.readUTF();
                 System.out.println("this is received: "+publicKeyRSA_sender);
                
                
                 // reading hash or digital signature
                String digitalSignature = dis.readUTF();
              //  System.out.println("this is received: "+digitalSignature);
                
                 // System.out.println("Digital Signature received: " + digitalSignature);
                System.out.println("Verifying Digital Signature...");
                
                
                // reading message
                   line = dis.readUTF();
                
                // reading encrypted AES or DES key to decrypt the message
                String encKey = dis.readUTF();
                
                
                    System.out.println("Listening...");
//                    while (!((line=dis.readUTF()).equals("STOP"))) {
//                        System.out.println("Message received");
//                        if (line != null) {
//                            gui.write("Received: "+line);
//                        }
//                    }


                if(verifySignature(digitalSignature, publicKeyRSA_sender))
                {
                    System.out.println("Verified successfully!");
                    
                    // decyrpt the AES or DES key with the private key of RSA
                    String decKey=null;
                    try {
                        decKey = new String(decrypt(privateKeyRSA_receiver, Base64.getDecoder().decode(encKey.substring(6))));
                    } catch (Exception ex) {
                        Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    
                    // decyrpt the message
                    String choice = encKey.substring(0, 3);
                    System.out.println("Encrypted " + choice + " key received: " + encKey.substring(6));
                    System.out.println("Decrypted " + choice + " key using my private RSA key: " + decKey);
                    
                    System.out.println("Encrypted message: " + line);
                    
                    if(choice.equals("AES"))
                        line = AES.decrypt(line, decKey);
                    else
                        try {
                            line = DES.decrypt(line, decKey);
                    } catch (Exception ex) {
                        Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    System.out.println("Decrypted message: " + line);
                    
                    // System.out.println(6);
                    // String pubkey=dis.readUTF();
                    // System.out.println(7);
                    // System.out.println(pubkey);
                
                    if(line != null){
                        gui.write(line);
                        // gui.write(choice + " key decyrpted : " + decKey);
                        // gui.write(encryptKey);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Digital Signature could not be verified!\nNetwork not secure!");
                }
                } catch (IOException ex) {
                    Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             
        }.start();
    }

}
