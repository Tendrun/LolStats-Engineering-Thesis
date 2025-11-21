package com.backendwebsite.Api.Service;

import com.backendwebsite.Api.DTO.ChampionDetails.ChampionDetailsResponse;
import com.backendwebsite.DatabaseBuilder.Client.CouchDBClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import static com.backendwebsite.Helper.KeysLoader.loadSecretValue;

@Service
public class ChampionService {
    private final CouchDBClient couchDBClient;
    private final ObjectMapper mapper;

    public ChampionService(CouchDBClient couchDBClient, ObjectMapper mapper) {
        this.couchDBClient = couchDBClient;
        this.mapper = mapper;
    }

    public List<ChampionDetailsResponse> getAllChampDetailsFromCouchDB() {
        List<ChampionDetailsResponse> championDetailsList = new ArrayList<>();

        String urn = "/championdetails/_all_docs?include_docs=true";

        CouchDBClient.Response response = couchDBClient.sendGet(urn);

        try {
            for (JsonNode row : response.body().get("rows")) {
                JsonNode doc = row.get("doc");
                ChampionDetailsResponse championDetails = mapper.treeToValue(doc, ChampionDetailsResponse.class);
                championDetailsList.add(championDetails);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return championDetailsList;
    }
}
