package com.game.entity.Comparators;

import com.game.entity.Player;

import java.util.Comparator;

public class PlayerCompareByName implements Comparator<Player> {
    @Override
    public int compare(Player o1, Player o2) {
        return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
    }
}
