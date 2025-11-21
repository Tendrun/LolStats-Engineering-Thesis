package com.backendwebsite.Api.Controller;

import com.backendwebsite.Api.DTO.ChampionDetails.ChampionDetailsResponse;
import com.backendwebsite.Api.Service.ChampionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class ChampionController {

    private final ChampionService championService;

    @Autowired
    public ChampionController(ChampionService championService) {
        this.championService = championService;
    }

    @GetMapping("/getChampions")
    public ResponseEntity<List<ChampionDetailsResponse>> getChampions() {
        List<ChampionDetailsResponse> listOfChampions = championService.getAllChampDetailsFromCouchDB();
        return ResponseEntity.ok(listOfChampions);
    }
}
