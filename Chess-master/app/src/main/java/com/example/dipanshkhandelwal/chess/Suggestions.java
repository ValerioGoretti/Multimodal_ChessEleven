package com.example.dipanshkhandelwal.chess;

import java.util.ArrayList;

public class Suggestions {

    private ArrayList<String> tasks=new ArrayList<>();
    private String firstMessage="Please, say 'Assistente' to use the vocal interface. When the assistent is listening, try the following commands: \n'Muovi il pedone da c2 a c4'\n'Apri le impostazioni'\n'Dammi un suggerimento'\n'Proietta la partita'";


    public String yesNoSuggestions(){
        return "'yes'\n'no'";
    }
    public String getFirstMessage(){return firstMessage;}


}
