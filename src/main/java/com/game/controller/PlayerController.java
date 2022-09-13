package com.game.controller;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.http.CreateNewPlayerRequest;
import com.game.http.GetPlayerCountRequest;
import com.game.http.GetPlayerListRequest;
import com.game.http.UpdatePlayerRequest;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    PlayerService service;

    @Autowired
    public PlayerController(PlayerService service){
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPlayer(@PathVariable String id){
        return new ResponseEntity<>(service.getPlayer(id), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<Object> getPlayerList(@ModelAttribute GetPlayerListRequest request){

        return new ResponseEntity<>(service.getPlayers(request), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> createPlayer(@RequestBody CreateNewPlayerRequest newPlayerRequest){
        return new ResponseEntity<>(service.createNewPlayer(newPlayerRequest), HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Object> updatePlayer(@RequestBody UpdatePlayerRequest request,  @PathVariable String id){
        return new ResponseEntity<>(service.updatePlayer(request, id), HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Object> getCount( @ModelAttribute GetPlayerCountRequest request){
        return new ResponseEntity<>(service.getCount(request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePlayer(@PathVariable String id){
        service.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
