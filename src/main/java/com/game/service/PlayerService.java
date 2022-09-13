package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Comparators.PlayerCompareByBirthday;
import com.game.entity.Comparators.PlayerCompareByExperience;
import com.game.entity.Comparators.PlayerCompareById;
import com.game.entity.Comparators.PlayerCompareByName;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.http.*;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    PlayerRepository repository;
    private Integer countPlayers;

    @Autowired
    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    public CreateNewPlayerResponse createNewPlayer(CreateNewPlayerRequest request) {

        String name = request.getName();
        String title = request.getTitle();
        Long birthday = request.getBirthday();
        Integer experience = request.getExperience();
        Boolean banned = request.getBanned();
        Race race = request.getRace();

        if (name == null ||
                title == null ||
                birthday == null ||
                experience == null ||
                name.length() > 12 ||
                title.length() > 30 ||
                name.equals("") ||
                birthday < 0 ||
                experience < 0 ||
                experience > 10000000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(birthday);
        int year = calendar.get(Calendar.YEAR);

        if (year < 2000 || year > 3000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        int level = (int) (Math.sqrt(2500 + (200 * experience)) - 50) / 100;
        int untilNextLevel = (50 * (level + 1) * (level + 2)) - experience;

        Player newPlayer = new Player();
        newPlayer.setName(name);
        newPlayer.setTitle(title);
        newPlayer.setRace(race);
        newPlayer.setProfession(request.getProfession());
        newPlayer.setExperience(experience);
        newPlayer.setLevel(level);
        newPlayer.setBirthday(calendar.getTime());
        newPlayer.setUntinNextLeve(untilNextLevel);
        newPlayer.setBanned(banned);

        Player savePlayer = repository.save(newPlayer);


        CreateNewPlayerResponse response = new CreateNewPlayerResponse(
                savePlayer.getId(),
                savePlayer.getName(),
                savePlayer.getTitle(),
                savePlayer.getRace(),
                savePlayer.getProfession(),
                savePlayer.getBirthday().getTime(),
                savePlayer.getBanned(),
                savePlayer.getExperience(),
                savePlayer.getLevel(),
                savePlayer.getUntinNextLeve());
        return response;
    }

    public List<GetPlayerListResponse> getPlayers(GetPlayerListRequest request) {

        String name = request.getName();
        String title = request.getTitle();
        Profession profession = request.getProfession();
        Race race = request.getRace();
        Long after = request.getAfter();
        Long before = request.getBefore();
        Boolean banned = request.getBanned();
        Integer minExperience = request.getMinExperience();
        Integer maxExperience = request.getMaxExperience();
        Integer minLevel = request.getMinLevel();
        Integer maxLevel = request.getMaxLevel();
        PlayerOrder order = request.getOrder();
        Integer pageNumber = request.getPageNumber();
        Integer pageSize = request.getPageSize();

        List<GetPlayerListResponse> playerList = new ArrayList<>();
        List<Player> allPlayers = new ArrayList<>(repository.findAll());

        Comparator comparator;
        if (order.equals(PlayerOrder.BIRTHDAY)) {
            comparator = new PlayerCompareByBirthday();
        } else if (order.equals(PlayerOrder.NAME)) {
            comparator = new PlayerCompareByName();
        } else if (order.equals(PlayerOrder.EXPERIENCE)) {
            comparator = new PlayerCompareByExperience();
        } else {
            comparator = new PlayerCompareById();
        }

        Collections.sort(allPlayers, comparator);

        if (name != null) {
            allPlayers = allPlayers.stream()
                    .filter(p -> p.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (title != null) {
            allPlayers = allPlayers.stream()
                    .filter(p -> p.getTitle().contains(title))
                    .collect(Collectors.toList());
        }
        if (profession != null) {
            allPlayers = allPlayers.stream()
                    .filter(p -> p.getProfession().toString().equals(profession.toString()))
                    .collect(Collectors.toList());
        }
        if (race != null) {
            allPlayers = allPlayers.stream()
                    .filter(p -> p.getRace().toString().equals(race.toString()))
                    .collect(Collectors.toList());
        }
        if (after != null && before != null && after <= before) {
            allPlayers = allPlayers.stream()
                    .filter(p -> (p.getBirthday().getTime() >= after && p.getBirthday().getTime() < before))
                    .collect(Collectors.toList());
        }
        if (((minExperience != 0) || (maxExperience != Integer.MAX_VALUE)) && minExperience <= maxExperience) {
            allPlayers = allPlayers.stream()
                    .filter(p -> (p.getExperience() >= minExperience && p.getExperience() < maxExperience))
                    .collect(Collectors.toList());
        }
        if (((minLevel != 0) || (maxLevel != Integer.MAX_VALUE)) && minLevel <= maxLevel) {
            allPlayers = allPlayers.stream()
                    .filter(p -> (p.getLevel() >= minLevel && p.getLevel() < maxLevel))
                    .collect(Collectors.toList());
        }
        if (banned != null) {
            allPlayers = allPlayers.stream()
                    .filter(p -> p.getBanned() == banned)
                    .collect(Collectors.toList());
        }

        countPlayers = allPlayers.size();

        for (int i = (pageNumber) * pageSize; i < pageSize * (pageNumber + 1) && i < allPlayers.size(); i++) {
            playerList.add(new GetPlayerListResponse(allPlayers.get(i)));
        }

        return playerList;
    }

    public Integer getCount(GetPlayerCountRequest request) {
        GetPlayerListRequest listRequest = new GetPlayerListRequest();
        listRequest.setName(request.getName());
        listRequest.setTitle(request.getTitle());
        listRequest.setRace(request.getRace());
        listRequest.setProfession(request.getProfession());
        listRequest.setAfter(request.getAfter());
        listRequest.setBefore(request.getBefore());
        listRequest.setBanned(request.getBanned());
        listRequest.setMinExperience(request.getMinExperience());
        listRequest.setMaxExperience(request.getMaxExperience());
        listRequest.setMinLevel(request.getMinLevel());
        listRequest.setMaxLevel(request.getMaxLevel());

        List playerList = getPlayers(listRequest);

        return countPlayers;
    }

    public void deletePlayer(String id) {

        Player player = findAPlayer(id);
        repository.delete(player);

    }

    public GetPlayerResponse getPlayer(String id) {
        Player player = findAPlayer(id);
        return new GetPlayerResponse(player);
    }

    public UpdatePlayerResponse updatePlayer(UpdatePlayerRequest request, String id) {
        Player player = findAPlayer(id);

        String name = request.getName();
        String title = request.getTitle();
        Race race = request.getRace();
        Profession profession = request.getProfession();
        Long birthday = request.getBirthday();
        Integer experience = request.getExperience();
        Boolean banned = request.getBanned();


        if (name != null && player.getName() != name) {
            player.setName(name);
        }
        if(title != null && title != player.getTitle()){
            player.setTitle(title);
        }
        if(race != null && race != player.getRace()){
            player.setRace(race);
        }
        if(profession != null && profession != player.getProfession()){
            player.setProfession(profession);
        }
        if(birthday != null && birthday != player.getBirthday().getTime()){
            if(birthday < new GregorianCalendar(2000, 01, 01).getTime().getTime()
                    || birthday > new GregorianCalendar(3000, 01, 01).getTime().getTime() ){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            player.setBirthday(new Date(birthday));
        }
        if(experience != null && experience != player.getExperience()){
            if(experience < 0 || experience > 10000000){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            int level = (int) (Math.sqrt(2500 + (200 * experience)) - 50) / 100;
            int untilNextLevel = (50 * (level + 1) * (level + 2)) - experience;
            player.setExperience(experience);
            player.setLevel(level);
            player.setUntinNextLeve(untilNextLevel);
            String s = "";
        }
        if(banned != null && banned != player.getBanned()){
            player.setBanned(banned);
        }

        Player savedPlayer = repository.save(player);

        return new UpdatePlayerResponse(savedPlayer);
    }

    public Player findAPlayer(String id) {
        Player player = null;
        try {
            Double idDouble = Double.parseDouble(id);
            if (idDouble % 1 != 0 || idDouble <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<Player> optional = repository.findById(Long.parseLong(id));
            if (optional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            player = optional.get();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return player;
    }

}
