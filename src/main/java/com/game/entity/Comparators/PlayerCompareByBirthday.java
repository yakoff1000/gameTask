package com.game.entity.Comparators;

import com.game.entity.Player;

import java.util.Comparator;

public class PlayerCompareByBirthday implements Comparator<Player> {
    @Override
    public int compare(Player o1, Player o2) {
        long milliseconds = o1.getBirthday().getTime()-o2.getBirthday().getTime();
        int result = 0;

        if (milliseconds > 0){
            result = 1;
        }else if(milliseconds < 0){
            result = -1;
        }

        return result;
    }
}
