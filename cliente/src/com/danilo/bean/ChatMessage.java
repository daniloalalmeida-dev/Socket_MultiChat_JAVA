/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danilo.bean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Danilo Almeida
 */
public class ChatMessage implements Serializable { //Ao invés de trabalhar com strings, ele trabalha com o objeto ChatMessage, em que envia as informações abaixo:
    
    private String name;    //nome
    private String text;    //texto da mensagem
    private String nameReserved; //armazena o nome do cliente que recebe msg reservada
    private Set<String> setOnlines = new HashSet<String>(); //lista que armazena os clientes online do servidor
    private Action action; //ação a ser enumerada com mensagens

    
    //criados os métodos get & set
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNameReserved() {
        return nameReserved;
    }

    public void setNameReserved(String nameReserved) {
        this.nameReserved = nameReserved;
    }

    public Set<String> getSetOnlines() {
        return setOnlines;
    }

    public void setSetOnlines(Set<String> setOnlines) {
        this.setOnlines = setOnlines;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
        
    public enum Action {   //classe do action 
        CONNECT, DISCONNECT, SEND_ONE, SEND_ALL, USERS_ONLINE
    }
}
