package com.backendwebsite.DatabaseBuilder.Step.FetchMatchDetails;

import com.backendwebsite.DatabaseBuilder.Context.FetchMatchDetailsContext;
import com.backendwebsite.DatabaseBuilder.DTO.RiotApi.MatchDetails.MatchDTO;
import com.backendwebsite.DatabaseBuilder.Step.IStep;
import com.backendwebsite.DatabaseBuilder.Step.Log.StepLog;
import com.backendwebsite.DatabaseBuilder.Step.StepsOrder;
import com.backendwebsite.DatabaseBuilder.Util.LogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidateMatchDetailsStep implements IStep<FetchMatchDetailsContext> {
    private static final Logger logger = LoggerFactory.getLogger(ValidateMatchDetailsStep.class);

    @Override
    public void execute(FetchMatchDetailsContext context) {
        long startTime = System.currentTimeMillis();
        try {
            int skipped = 0;
            
            for (MatchDTO match : context.fetchedMatchDetails) {
                if (match == null || match.metadata == null || match.metadata.matchId == null) {
                    context.logs.computeIfAbsent(getClass().getSimpleName(), k -> new java.util.ArrayList<>())
                            .add(new StepLog(StepsOrder.RequestStatus.FAILED, this.getClass().getSimpleName(),
                                    "Invalid match details - missing metadata.matchId", System.currentTimeMillis() - startTime));
                    logger.warn("Skipping invalid match details due to missing metadata.matchId");
                    skipped++;
                    continue;
                }

                match._id = "matchDetail:" + context.region + ":" + match.metadata.matchId;
                context.validatedMatchDetails.add(match);
            }

            String summary = String.format("Validated count: %d skipped: %d", context.validatedMatchDetails.size(), skipped);
            context.logs.computeIfAbsent(getClass().getSimpleName(), k -> new java.util.ArrayList<>())
                    .add(new StepLog(StepsOrder.RequestStatus.SUCCESSFUL, this.getClass().getSimpleName(),
                            summary, System.currentTimeMillis() - startTime));

            logger.info(LogFormatter.formatStepLog(getClass().getSimpleName(), StepsOrder.RequestStatus.SUCCESSFUL,
                    summary, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            context.logs.computeIfAbsent(getClass().getSimpleName(), k -> new java.util.ArrayList<>())
                    .add(new StepLog(StepsOrder.RequestStatus.FAILED, this.getClass().getSimpleName(),
                            "Exception: " + e.getMessage(), System.currentTimeMillis() - startTime));
            logger.error(LogFormatter.formatStepLog(getClass().getSimpleName(), StepsOrder.RequestStatus.FAILED,
                    "Exception while validating match details", System.currentTimeMillis() - startTime), e);
        }
    }
}
