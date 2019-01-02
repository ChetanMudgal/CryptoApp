/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoapp;

import java.net.Socket;

/**
 *
 * @author chetan mudgal
 */
public interface WritableGui {
    void write(String s);
    void clear();
    void setSendSocket(Socket sendSocket); 
    
     
    
}
