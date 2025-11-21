package com.backendwebsite.DatabaseBuilder.Step.FetchMatch;

import com.backendwebsite.DatabaseBuilder.Client.RiotApiClient;
import com.backendwebsite.DatabaseBuilder.Context.FetchMatchesContext;
import com.backendwebsite.DatabaseBuilder.Domain.Match.PlayerMatches;
import com.backendwebsite.DatabaseBuilder.Step.IStep;
import com.backendwebsite.DatabaseBuilder.Step.Log.StepLog;
import com.backendwebsite.DatabaseBuilder.Step.StepsOrder;
import com.backendwebsite.DatabaseBuilder.Util.LogFormatter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;


@Component
public class PullMatchesFromRiotStep implements IStep<FetchMatchesContext> {
    private static final Logger logger = LoggerFactory.getLogger(PullMatchesFromRiotStep.class);
    private final RiotApiClient riotApiClient;
    private final ObjectMapper mapper;

    PullMatchesFromRiotStep(RiotApiClient riotApiClient, ObjectMapper mapper){
        this.riotApiClient = riotApiClient;
        this.mapper = mapper;
    }

    @Override
    public void execute(FetchMatchesContext context) {
        getMatchesFromRiot(context);
    }

    public void getMatchesFromRiot(FetchMatchesContext context) {
        for (String puuid : context.puuids) {
            long stepStartTime = System.currentTimeMillis();
            String urnRiot = "/lol/match/v5/matches/by-puuid/" + puuid + "/ids" + "?type=" + context.type  +
                    "&start=0&count=20";
            RiotApiClient.Response response = riotApiClient.sendRequest(urnRiot, context.region.name());

            try {
                List<String> matchIds = new ArrayList<>();
                if (response != null && response.body() != null && response.body().isArray()) {
                    matchIds = mapper.convertValue(response.body(), new TypeReference<>() {});
                } else if (response != null && response.body() != null) {
                    try {
                        matchIds = mapper.convertValue(response.body(), new TypeReference<>() {});
                    } catch (IllegalArgumentException iae) {
                        logger.error("Unexpected Riot response body shape for puuid {}: {}", puuid, response.body());
                    }
                } else {
                    logger.error("Empty response body for puuid {} (urn={})", puuid, urnRiot);
                }

                String _id = "playerMatches:" + context.region + ":" + puuid;
                PlayerMatches playerMatches = new PlayerMatches(matchIds, puuid, _id, "");

                // Use puuid as the key so it aligns with existingMatches (which uses puuid as key)
                context.fetchedMatches.put(_id, playerMatches);

                String msg = (response != null && response.status() == StepsOrder.RequestStatus.SUCCESSFUL)
                        ? "Fetched " + matchIds.size() + " match ids for puuid: " + puuid
                        : "Failed to fetch matches for puuid: " + puuid;
                 // log to context
                 context.logs.computeIfAbsent(getClass().getSimpleName(), k -> new ArrayList<>())
                         .add(new StepLog(response != null ? response.status() : StepsOrder.RequestStatus.FAILED,
                                 this.getClass().getSimpleName(),
                                 msg,
                                 System.currentTimeMillis() - stepStartTime));

                logger.info(LogFormatter.formatStepLog(getClass().getSimpleName(), response != null ? response.status() :
                        StepsOrder.RequestStatus.FAILED, msg, System.currentTimeMillis() - stepStartTime));

             } catch (Exception e){
                 String msg = "Exception fetching matches for puuid: " + puuid;
                 // log exception to context logs and logger
                 context.logs.computeIfAbsent(getClass().getSimpleName(), k -> new ArrayList<>())
                         .add(new StepLog(StepsOrder.RequestStatus.FAILED, this.getClass().getSimpleName(),
                                 msg + " - " + e.getMessage(),
                                 System.currentTimeMillis() - stepStartTime));
                logger.error(LogFormatter.formatStepLog(getClass().getSimpleName(), StepsOrder.RequestStatus.FAILED, msg,
                        System.currentTimeMillis() - stepStartTime), e);
             }
         }
     }
 }
