/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument;

import java.net.InetAddress;

/**
 *
 * @author Higor
 */
public abstract class Instrument {
    
    protected String name; // nome do instrumento   
    protected int polyphony; // quantidade de notas
    protected String OscAddress; //endereço do OSC do instrumento
    protected String serverOscAddress; //endereço do OSC do instrumento
    protected InetAddress severAddress; // endereco do servidor
    protected int sendPort; // porta para envio msgOSC
    protected int receivePort; // porta pra receber msgOSC
    protected String typeFamily; //tipo do instrumento
    protected String specificProtocol; //procolo especifico do robo

    public Instrument(String name, int polyphony,String serverOscAddress, String OscAddress, InetAddress severAddress, int sendPort, int receivePort, String typeFamily, String specificProtocol) {
        
        this.name = name;
        this.polyphony = polyphony;
        this.OscAddress = OscAddress;
        this.severAddress = severAddress;
        this.sendPort = sendPort;
        this.receivePort = receivePort;
        this.typeFamily = typeFamily;
        this.specificProtocol = specificProtocol;
        this.serverOscAddress = serverOscAddress;
    }
    
    
    
    
    
}
