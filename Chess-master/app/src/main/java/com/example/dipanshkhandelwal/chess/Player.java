package com.example.dipanshkhandelwal.chess;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.LinkedList;
import java.util.List;

public class Player {
    private List<Piece> prom= new LinkedList<Piece>(){{ add(Piece.BLACK_QUEEN); add(Piece.BLACK_BISHOP); add(Piece.BLACK_KNIGHT); add(Piece.BLACK_ROOK);}};

    public Move eseguiMossa(List<Move> moves) {

        int numero = (int) (Math.random()*moves.size());
        Move move = moves.get(numero);
        if (moves.get(numero).toString().length()==5){
            System.out.println("mossa promozione!!! " + moves.get(numero).toString());
            String p= moves.get(numero).toString().substring(4,5);
            System.out.println(p);
            switch (p){
                case "q":   move=new Move(moves.get(numero).getFrom(),moves.get(numero).getTo(),Piece.BLACK_QUEEN);
                    break;
                case "b":   move=new Move(moves.get(numero).getFrom(),moves.get(numero).getTo(),Piece.BLACK_BISHOP);
                    break;
                case "r":   move=new Move(moves.get(numero).getFrom(),moves.get(numero).getTo(),Piece.BLACK_ROOK);
                    break;
                case "k":   move=new Move(moves.get(numero).getFrom(),moves.get(numero).getTo(),Piece.BLACK_KNIGHT);
                    break;
            }
        }
        return move;
    }

   

}
