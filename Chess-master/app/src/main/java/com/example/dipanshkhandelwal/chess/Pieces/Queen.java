package com.example.dipanshkhandelwal.chess.Pieces;

import com.example.dipanshkhandelwal.chess.Coordinates;
import com.example.dipanshkhandelwal.chess.Position;

import java.util.ArrayList;

/**
 * Created by DIPANSH KHANDELWAL on 03-06-2017
 */

public class Queen extends Piece{

    public Queen(boolean white) {
        super(white);
    }

    @Override
    public ArrayList<Coordinates> AllowedMoves(Coordinates coordinates , Position[][] board){

        return null;
    }
}
