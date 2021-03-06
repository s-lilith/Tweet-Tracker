package main;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TweetHandler {

    //spostare su un altro file
    private static String CONSUMER_KEY;
    private static String CONSUMER_SECRET;
    private static String ACCESS_TOKEN;
    private static String ACCESS_TOKEN_SECRET;

    private final ConfigurationBuilder cb;

    private TwitterStream twitterStream;

    private Twitter twitter;

    private List<Status> streamTweets = new ArrayList<>();

    private Gson gson;

    public TweetHandler(){
        try {
            FileReader fr = new FileReader("keys.json");
            BufferedReader br = new BufferedReader(fr);
            Gson gson = new Gson();
        
            JsonObject keys = gson.fromJson(br.readLine(), JsonObject.class);
            CONSUMER_KEY = keys.get("CONSUMER_KEY").getAsString();
            CONSUMER_SECRET = keys.get("CONSUMER_SECRET").getAsString();
            ACCESS_TOKEN = keys.get("ACCESS_TOKEN").getAsString();
            ACCESS_TOKEN_SECRET = keys.get("ACCESS_TOKEN_SECRET").getAsString();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
                .setTweetModeExtended(true);
               
                
               

        Configuration config = cb.build();
        //inizializzazione twitter stream
        twitterStream = new TwitterStreamFactory(config).getInstance();
        //inizializzazione twitter search
        twitter = new TwitterFactory(config).getInstance();

        gson = new Gson();
    }

    public void startStreamSearch(String searchText, ObservableList<String> tweetsObsList, boolean localized){
        //L'utente pu?? inserire pi?? parole separate da una virgola
        String[] keywords = searchText.toLowerCase().split(",");
        StatusListener listener = new StatusListener() {
            int count = 0;
            @Override
            public void onStatus(Status status) {
                //Il filtro per le parole va inserito qui se si usano altri filtri perch?? la filter query combina i filtri
                // con un "OR", cio?? ottengo tweet in Italia OR tweet con le parole chiave scelte. Io voglio un AND.
                String statusText = status.getText();
               
               
             if (Arrays.stream(keywords).parallel().anyMatch(statusText.toLowerCase()::contains)){
                    count++; 

                    String text = "#" + count + ": " + statusText + " || id:" + status.getId() ;
                    streamTweets.add(status);                                                       
                    //L'interfaccia utente non pu?? essere aggiornata direttamente da un thread che non fa parte
                    //dell'applicazione quindi ?? necessario usare questo metodo. Questo sistema va modificato in quanto
                    //non ?? corretto che sia una classe diversa dal controller a gestire l'aspetto di un'interfaccia.
                        Platform.runLater(new Runnable() {
                        @Override
                            public void run() {
                            //Aggiorno listview
                            tweetsObsList.add(text);
                            }
                        });
                   // }
                }
            }
             

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }
        
        };
        twitterStream.addListener(listener);

        FilterQuery filterQuery = new FilterQuery();

        //coordinate di Roma
        double latitude = 41.902782;
        double longitude = 12.496366;

        //creer?? due nuove coppie di coordinate che indicano due angoli opposti del quadrato che delimita l'area
        //in cui ricerco i tweet. Per farlo mi baser?? sulle coordinate di Roma.
        double lat1 = latitude - 6;
        double long1 = longitude - 6;
        double lat2 = latitude + 5;
        double long2 = longitude + 6;
        double[][] italyBB = {{long1, lat1}, {long2, lat2}};

        //UK boundary box
    //    double[][] ukBB = {{-10.85449,49.82380},{2.02148,59.4785}};

        //Spain boundary box
      //  double[][] esBB = {{-9.39288,35.94685},{3.03948,43.74833}};

        //filterQuery.locations(ukBB);
        //filterQuery.locations(esBB);
        filterQuery.locations(italyBB);
        //se l'utente vuole tweet geolocalizzati allora uso la boundary box
        //altrimenti, se interessato solo a determinate parole, filtro solo per parole
        if (localized)
            twitterStream.filter(filterQuery);
        else
            twitterStream.filter(keywords);
    }
    //Chiude la comunicazione con i server di Twitter e restituisce l'array JSON dei tweet
    public JsonArray stopStreamSearch(){
        twitterStream.cleanUp();
        twitterStream.shutdown();
        return gson.toJsonTree(streamTweets).getAsJsonArray();
    }

    //Implementa la ricerca per popolarit??
    public JsonArray search(String searchTerm){
        List<Status> tweetList = new ArrayList<>();
        Query query = new Query(searchTerm);
        //100 ?? il numero massimo di tweet ottenibili con le API standard
        query.setCount(100);
        //Utilizzo la capitale come centro per il Geocode, lat 41.902782 long 12.496366
        //Radius pari a 650km perch?? i punti pi?? lontani (in linea d'aria) da Roma sono in val d'Aosta e in Sicilia
        //ed entrambi sono vicini a questo valore
        query.setGeoCode(new GeoLocation(41.902782, 12.496366), 650, Query.KILOMETERS);
        query.setLang("it");

        try {
            tweetList = twitter.search(query).getTweets();


          //  StatusUpdate tweet = new StatusUpdate("");
           // File image = new File("/Users/bianc/OneDrive/Desktop/immagine.jpg");
           // tweet.setMedia(image);
           // twitter.updateStatus(tweet);
          //  System.out.println("Post twittato.");

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return gson.toJsonTree(tweetList).getAsJsonArray();
    }

    //Dato l'id di un tweet, restituisce il tweet a cui corrisponde
    public Status searchById(String id){
        Status status = null;
        try {
            status = twitter.showStatus(Long.parseLong(id));
            if (status == null) {
                System.out.println("Tweet not found");
            } else {
                System.out.println("@" + status.getUser().getScreenName()
                        + " - " + status.getText());
            }
        } catch (TwitterException e) {
            System.err.print("Failed to search tweets: " + e.getMessage());
            e.printStackTrace();
        }
        return status;
    }
}