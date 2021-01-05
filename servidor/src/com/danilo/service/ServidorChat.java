/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danilo.service;

import com.danilo.bean.ChatMessage;
import com.danilo.bean.ChatMessage.Action;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.awt.windows.ThemeReader;

/**
 * @author Danilo Almeida
 */
public class ServidorChat {

    private ServerSocket serverSocket;
    private Socket socket;
    private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>(); 
//Todo usuário que se conectar ao servidor entram nesta lista

    //método construtor que define o socket para conexão
    public ServidorChat() { 
        try { //criado o bloco try / catch, então iniciando o servidor.
            serverSocket = new ServerSocket(5555);

            System.out.println("Servidor on!");

            while (true) { //manter o server socket esperando uma conexão
                socket = serverSocket.accept();

                new Thread(new ListenerSocket(socket)).start(); //utilização da thread
            }

        } catch (IOException ex) {
            Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ListenerSocket implements Runnable { //listiner para ler o socket

        private ObjectOutputStream output;  //realiza as saídas de mensagens do servidor
        private ObjectInputStream input;    //recebe as mensagens do servidor

        public ListenerSocket(Socket socket) {
            try { //bloco try / catch para execução 
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream (socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //método run do "ouvinte do servidor"
        @Override
        public void run() {
            ChatMessage message = null;
            try {
                //recebe as mensagens do cliente
                while ((message = (ChatMessage) input.readObject()) != null) {
                    Action action = message.getAction();
                    
                    //testes para descobrir o tipo de mensagem enviada pelo cliente
                    if (action.equals(Action.CONNECT)) {
                        boolean isConnect = connect(message, output);
                        if (isConnect) {
                            mapOnlines.put(message.getName(), output);
                            sendOnlines();
                        }
                    } else if (action.equals(Action.DISCONNECT)) {
                        disconnect(message, output);
                        sendOnlines();
                        return;
                    } else if (action.equals(Action.SEND_ONE)) {
                        sendOne(message);
                    } else if (action.equals(Action.SEND_ALL)) {
                        sendAll(message);
                    }
                }
            } catch (IOException ex) {
                ChatMessage cm = new ChatMessage();
                cm.setName(message.getName());
                disconnect(cm, output);
                sendOnlines();
                System.out.println(message.getName() + " deixou o chat!");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //CONECTAR
    //assim que houver um pedido de conexão, o método abaixo irá ler objeto ChatMessage
    private boolean connect(ChatMessage message, ObjectOutputStream output) {
        if (mapOnlines.size() == 0) {
            message.setText("YES");
            send(message, output);
            return true; //cliente Ok, retorne True
        }

        //Testar se o nome do cliente atual é diferente dos nomes da lista
        if (mapOnlines.containsKey(message.getName())) {
            message.setText("NO");
            send(message, output);
            return false;
        } else {
            message.setText("YES");
            send(message, output);
            return true;
        }
    }

    //DESCONECTAR
    private void disconnect(ChatMessage message, ObjectOutputStream output) {
        mapOnlines.remove(message.getName());

        message.setText("Deixou o sala!");

        message.setAction(Action.SEND_ONE);

        sendAll(message);

        System.out.println("User " + message.getName() + " sai da sala");
    }

    //SEND
    private void send(ChatMessage message, ObjectOutputStream output) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //SENDONE
    private void sendOne(ChatMessage message) {
        //Percorre a lista e verifica as chaves "kv"
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            //Verifica a chave "kv" selecionada, então os dados daquele message é enviado a ele
            if (kv.getKey().equals(message.getNameReserved())) {
                try {
                    kv.getValue().writeObject(message);
                } catch (IOException ex) {
                    Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    //SENDALL
    private void sendAll(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            //Se a chave "kv" é diferente daquele usuário que está envaindo a message,
            //então os dados daquela mensagem é enviado a todos menos ele.
            if (!kv.getKey().equals(message.getName())) {
                message.setAction(Action.SEND_ONE);
                try {
                    kv.getValue().writeObject(message);
                } catch (IOException ex) {
                    Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    //atualização automática da lista de usuários do chat
    private void sendOnlines() {  
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            setNames.add(kv.getKey());
        }

        ChatMessage message = new ChatMessage();
        message.setAction(Action.USERS_ONLINE);
        message.setSetOnlines(setNames);

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
