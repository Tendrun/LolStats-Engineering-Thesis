package com.backendwebsite.DatabaseBuilder.Controller;

import com.backendwebsite.DatabaseBuilder.Context.BuildChampionAnalyticsContext;
import com.backendwebsite.DatabaseBuilder.Context.FetchMatchDetailsContext;
import com.backendwebsite.DatabaseBuilder.Context.FetchMatchesContext;
import com.backendwebsite.DatabaseBuilder.DTO.AppApi.AnaliseMatches.GetAnaliseMatchesRequest;
import com.backendwebsite.DatabaseBuilder.DTO.AppApi.Match.GetMatchesRequest;
import com.backendwebsite.DatabaseBuilder.DTO.AppApi.MatchDetails.GetMatchDetailsRequest;
import com.backendwebsite.DatabaseBuilder.DTO.AppApi.Player.GetPlayersRequest;
import com.backendwebsite.DatabaseBuilder.DTO.AppApi.Summary.LogSummary;
import com.backendwebsite.DatabaseBuilder.DTO.AppApi.Summary.StepLogSummary;
import com.backendwebsite.DatabaseBuilder.Director.ChampionAnalyticsDirector;
import com.backendwebsite.DatabaseBuilder.Director.FetchMatchDetailsDirector;
import com.backendwebsite.DatabaseBuilder.Director.FetchMatchesDirector;
import com.backendwebsite.DatabaseBuilder.Director.FetchPlayersDirector;
import com.backendwebsite.DatabaseBuilder.Step.Log.StepLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backendwebsite.DatabaseBuilder.Context.FetchPlayersContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/database")
public class DatabaseController {

    private final FetchPlayersDirector fetchPlayersDirector;
    private final FetchMatchesDirector fetchMatchesDirector;
    private final ChampionAnalyticsDirector championAnalyticsDirector;
    private final FetchMatchDetailsDirector fetchMatchDetailsDirector;


    public DatabaseController(FetchPlayersDirector fetchPlayersDirector, FetchMatchesDirector fetchMatchesDirector,
                              ChampionAnalyticsDirector championAnalyticsDirector,
                              FetchMatchDetailsDirector fetchMatchDetailsDirector) {
        this.fetchPlayersDirector = fetchPlayersDirector;
        this.fetchMatchesDirector = fetchMatchesDirector;
        this.championAnalyticsDirector = championAnalyticsDirector;
        this.fetchMatchDetailsDirector = fetchMatchDetailsDirector;
    }

    private LogSummary buildLogResponse(HashMap<String, List<StepLog>> contextLogs) {
        Map<String, StepLogSummary> contextLogsSummary = new HashMap<>();

        contextLogs.forEach((stepName, logs) -> {
            contextLogsSummary.put(stepName, StepLogSummary.fromStepLogs(stepName, logs));
        });

        LogSummary logSummary = new LogSummary(contextLogsSummary,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return logSummary;
    }


    @PostMapping("/FetchPlayers")
    public ResponseEntity<LogSummary> getPlayers(@RequestBody GetPlayersRequest req) {
        FetchPlayersContext context = new FetchPlayersContext(
                req.region(),
                req.tier(),
                req.division(),
                req.queue(),
                req.page()
        );

        fetchPlayersDirector.startWork(context);
        return ResponseEntity.ok(buildLogResponse(context.logs));
    }

    @PostMapping("/FetchMatches")
    public ResponseEntity<LogSummary> getMatches(@RequestBody GetMatchesRequest req) {
        FetchMatchesContext context = new FetchMatchesContext(req.region(),
                req.playerLimit(), req.type(), req.tier());

        fetchMatchesDirector.startWork(context);
        return ResponseEntity.ok(buildLogResponse(context.logs));
    }

    @PostMapping("/FetchMatchDetails")
    public ResponseEntity<LogSummary> fetchMatchDetails(@RequestBody GetMatchDetailsRequest req) {
        FetchMatchDetailsContext context = new FetchMatchDetailsContext(req.playerMatchLimit(), req.region());

        fetchMatchDetailsDirector.startWork(context);
        return ResponseEntity.ok(buildLogResponse(context.logs));
    }

    @PostMapping("/AnaliseMatches")
    public ResponseEntity<LogSummary> analiseMatches(@RequestBody GetAnaliseMatchesRequest req) {
        BuildChampionAnalyticsContext context = new BuildChampionAnalyticsContext(req.limitMatches());

        championAnalyticsDirector.startWork(context);
        return ResponseEntity.ok(buildLogResponse(context.logs));
    }
}