package com.marko.anonymous_feedback_bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class TrelloService {


    @Value("${trello.api.key}")
    private  String apiKey;

    @Value("${trello.api.token}")
    private  String apiToken;

    @Value("${trello.id.list}")
    private  String idList;

    private final RestTemplate restTemplate;

    public TrelloService() {
        this.restTemplate = new RestTemplate();
    }

    public void createCard(String name, String description) {

        String url = "https://api.trello.com/1/cards" +
                "?idList=" + idList +
                "&name=" + name +
                "&desc=" + description +
                "&key=" + apiKey +
                "&token=" + apiToken;

        String response = restTemplate.postForObject(url, null, String.class);
        System.out.println("Card created: " + response);
    }


}
