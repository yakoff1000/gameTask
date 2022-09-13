package com.game.entity.Comparators;

import com.game.entity.Player;

import java.util.Comparator;

public class PlayerCompareById implements Comparator<Player> {

    @Override
    public int compare(Player o1, Player o2) {
        return (int) (o1.getId() - o2.getId());
    }

}
