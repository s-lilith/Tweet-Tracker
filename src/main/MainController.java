package main;

import com.google.gson.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import java.lang.Object;

import javafx.stage.Window;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Rectangle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.event.KeyEvent;
import java.lang.Integer;

public class MainController implements Initializable {
    @FXML
    private TextField searchBar;
    @FXML
    private TextField loopSearchBar;
    @FXML
    private TextField streamSearchBar;
    @FXML
    private Button popularSearchButton;
    @FXML
    private Button operatorsDialogButton;
    @FXML
    private CheckBox localizedCheckBox;
    @FXML
    private CheckBox twittaCheckBox;
    @FXML
    private Button streamSearchButton;
    @FXML
    private Button streamStopButton;
    @FXML
    private Button loadJsonButton;
    @FXML
    private Button saveToJsonButton;
    @FXML
    private Button twittStartButton;
    @FXML
    private Button twittStopButton;
    @FXML
    private Button openMapButton;
    @FXML
    private Button openChartButton;
    @FXML
    private ListView<String> tweetListView;
    @FXML
    private WebView wordCloudWebView;
    @FXML
    private TableView<AnalyzedTweet> tweetStatsTableView;
    @FXML
    private TableColumn<AnalyzedTweet, Integer> favoritedColumn;
    @FXML
    private TableColumn<AnalyzedTweet, Integer> retweetedColumn;
    @FXML
    private TableColumn<AnalyzedTweet, Integer> followersColumn;
    @FXML
    private TableColumn<AnalyzedTweet, Double> influenceColumn;
    @FXML
    private TableColumn<AnalyzedTweet, String> tweetNumberColumn;

    TweetHandler tweetHandler;

    private List<Position> positions;

    private JsonArray jsonTweetList;

    private Gson gson;
    ObservableList<String> tweetsObsList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tweetHandler = new TweetHandler();
        positions = new ArrayList<>();
        gson = new Gson();

        tweetsObsList = FXCollections.observableList(new ArrayList<String>());

        tweetListView.setItems(tweetsObsList);

       // Inizializzazione tabella statistiche
        favoritedColumn.setCellValueFactory(new PropertyValueFactory<>("favorited"));
        retweetedColumn.setCellValueFactory(new PropertyValueFactory<>("retweeted"));
        followersColumn.setCellValueFactory(new PropertyValueFactory<>("followers"));
        influenceColumn.setCellValueFactory(new PropertyValueFactory<>("influence"));
        tweetNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tweetNumber"));

        // Gestione evento click su un elemento della lista
        tweetListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String tweet = tweetListView.getSelectionModel().getSelectedItem();

                // L'utente potrebbe cliccare su una riga vuota
                if (tweet != null) {
                    // Recupero la numerazione del tweet nella lista e rimuovo newline e break
                    tweet = tweet.replace("\n", "").replace("\r", "");
                    int tweetNumber = Integer.parseInt(tweet.replaceAll(":.*", "").replaceAll("#", ""));

                    // recupero l'id del tweet dal testo
                    String id = tweet.replaceAll(".*[|| id:]", "");
                    Status tweetFound = tweetHandler.searchById(id);
                    if (tweetFound != null) {
                        AnalyzedTweet analyzedTweet = new AnalyzedTweet(tweetNumber, tweetFound.getFavoriteCount(),
                                tweetFound.getRetweetCount(), tweetFound.getUser().getFollowersCount());
                        tweetStatsTableView.getItems().add(analyzedTweet);
                    }
                }

            }
        });
    }

    // Carica la lista dei tweet nel JsonArray
    public void loadTweetList() {
        if (jsonTweetList != null) {
            // tweetListView.getItems().clear();
            tweetsObsList.clear();
            tweetStatsTableView.getItems().clear();
            int tweetCounter = 0;
            for (JsonElement o : jsonTweetList) {
                tweetCounter++;
                String text = "#" + tweetCounter + ": " + o.getAsJsonObject().get("text").toString() + " || id:"
                        + o.getAsJsonObject().get("id");
                // tweetListView.getItems().add(text);
                tweetsObsList.add(text);
            }
        }
    }

    // Gestisce il click sul pulsante "Popular Search"
    public void popularSearch() {
        
        String searchText = searchBar.getText();
        

        if (searchText.length() > 0) {
            // Il metodo search effettua la chiamata a twitter e restituisce una lista di
            // tweet
            jsonTweetList = tweetHandler.search(searchText);

            // Chiama i metodi per popolare la lista e creare la cloud word
            loadTweetList();
            createWordCloud();
            
        }
    }
   

        public void postAutomaticTweet(){
            twittStartButton.setDisable(true);
            twittaCheckBox.setDisable(true);
            popularSearchButton.setDisable(true);
            operatorsDialogButton.setDisable(true);
            loadJsonButton.setDisable(true);
            saveToJsonButton.setDisable(true);
            streamSearchButton.setDisable(true);
            openMapButton.setDisable(true);
            openChartButton.setDisable(true);
            streamStopButton.setDisable(true);
            twittStopButton.setDisable(false);
            loopSearchBar.setDisable(true);
            searchBar.setDisable(true);
            localizedCheckBox.setDisable(true);
            streamSearchBar.setDisable(true);
            
            String secloop = loopSearchBar.getText();
            long foo;
            //conversione stringa a long
            foo = Long.parseLong(secloop);
                  if(foo != 0){
                    popularSearch();
                    //timeline funziona come un loop ed esegue un'iterazione ogni foo +3 sec 
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(foo+4), e -> {  
                   
                    popularSearch(); 
                    if(twittaCheckBox.isSelected()) {
                    Timeline timelin3 = new Timeline(new KeyFrame(Duration.seconds(4),  r -> {
                        Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null).requestFocus();
                    postTweet();
                    })); 
                    timelin3.play();
                    }
                    }));
                   twittStopButton.setOnAction((e -> {
                    timeline.stop();
                    stopAutomaticTweet();
                    }));
                    timeline.setCycleCount(6);
                    timeline.play();   
            }
            
    }    
   
    public void stopAutomaticTweet() {
        
        localizedCheckBox.setDisable(false);
        twittaCheckBox.setDisable(false);
        popularSearchButton.setDisable(false);
        operatorsDialogButton.setDisable(false);
        loadJsonButton.setDisable(false);
        saveToJsonButton.setDisable(false);
        streamSearchButton.setDisable(false);
        openMapButton.setDisable(false);
        openChartButton.setDisable(false);
        streamStopButton.setDisable(false);
        twittStartButton.setDisable(false);
        twittStopButton.setDisable(true);
        loopSearchBar.setDisable(false);
        searchBar.setDisable(false);
        localizedCheckBox.setDisable(false);
        streamSearchBar.setDisable(false);
    }

    @FXML
    void openMap(ActionEvent event) {
        try {
            initializeTweetPositions();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/map.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            MapController mapController = fxmlLoader.getController();
            mapController.transferPositions(positions);
            Stage stage = new Stage();
            stage.setTitle("Mappa");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't load map window");
        }
    }

    // Mostra la finestra con gli operatori utilizzabili con la search per
    // popolarità
    public void showOperatorsDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/operatorsDialog.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
           // OperatorsDialogController operatorsDialogController = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("Operatori search");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't load operators dialog");
        }
    }

    public void streamSearch() {
        String searchText = streamSearchBar.getText();
        if (searchText.length() > 0) {
            tweetStatsTableView.getItems().clear();
            tweetListView.getItems().clear();
            tweetHandler.startStreamSearch(searchText, tweetsObsList, localizedCheckBox.isSelected());
        }

        localizedCheckBox.setDisable(true);
        popularSearchButton.setDisable(true);
        operatorsDialogButton.setDisable(true);
        loadJsonButton.setDisable(true);
        saveToJsonButton.setDisable(true);
        streamSearchButton.setDisable(true);
        openMapButton.setDisable(true);
        openChartButton.setDisable(true);
        streamStopButton.setDisable(false);
    }

    public void stopStreamSearch() {
        jsonTweetList = tweetHandler.stopStreamSearch();

        localizedCheckBox.setDisable(false);
        popularSearchButton.setDisable(false);
        operatorsDialogButton.setDisable(false);
        loadJsonButton.setDisable(false);
        saveToJsonButton.setDisable(false);
        streamSearchButton.setDisable(false);
        openMapButton.setDisable(false);
        openChartButton.setDisable(false);
        streamStopButton.setDisable(true);

        initializeTweetPositions();
        createWordCloud();
    }

    public void createWordCloud() {
    
       
        // Creo il testo da passare alla funzione per creare la word cloud
        StringBuilder cloudWordText = new StringBuilder();
        for (JsonElement o : jsonTweetList) {
            cloudWordText.append(o.getAsJsonObject().get("text").toString());
        }
        String data = cloudWordText.toString();
        WebEngine engine = wordCloudWebView.getEngine();
        
        engine.load(getClass().getResource("/html/wordCloud.html").toString());
        engine.setJavaScriptEnabled(true);
       //  Una volta che la pagina è stata caricata, posso chiamare i metodi per la word
       //  cloud
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    
                   
                    JSObject jsObject = (JSObject) engine.executeScript("window");
                    Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null).requestFocus();
                    Window ow = Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            
                    double owx = ow.getX();
                    double owy = ow.getY();
                    try {
                    Robot nu = new Robot();
                    nu.mouseMove((int)owx+550, (int)owy+388);
                    nu.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
                    nu.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
                    nu.mouseMove((int)owx, (int)owy);
                    Robot ro;
                  
                        ro = new Robot();
                        ro.keyPress(KeyEvent.VK_END);
                        
                        ro.keyRelease(KeyEvent.VK_END);
                    } catch (AWTException e) {
                        // Auto-generated catch block
                        e.printStackTrace();
                    }
                    jsObject.call("initialize", data);
                   // webViewClickListener(engine, jsObject);
    
                }

            }
            
        });
        
    }

    // Permette di scegliere un file JSON e se il formato è corretto ne carica il
    // contenuto
    public void loadJson() {
        jsonTweetList = new JsonArray();

        String jsonFilePath = null;

        FileChooser fc = new FileChooser();
        // filtro per mostrare solo i file con estensione .json
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            jsonFilePath = selectedFile.getAbsolutePath();
        } else {
            System.out.println("Il file non è valido o non è stato selezionato");
        }

        JsonParser parser = new JsonParser();
        try {
            if (jsonFilePath != null)
                jsonTweetList = (JsonArray) parser.parse(new FileReader(jsonFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // creo stringa per cloud word
        StringBuilder tweetText = new StringBuilder();
        for (JsonElement o : jsonTweetList) {
            tweetText.append(o.getAsJsonObject().get("text").toString());
        }
        createWordCloud();
        loadTweetList();
    }

    // Gestisce il salvataggio dei tweet in un file JSON
    public void saveToJson() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Salva tweet");
        fc.setInitialFileName("myTweets");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON file", "*.json"));

        File file = fc.showSaveDialog(null);
        try {
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
            fileWriter.write(gson.toJson(jsonTweetList));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // inizializza la lista positions che verrà passata alla mappa per generare i
    // marker
    public void initializeTweetPositions() {
        if (jsonTweetList != null) {
            JsonObject jsonGeoLocation = new JsonObject();

            positions.clear();
            for (Object o : jsonTweetList) {
                JsonObject jsonPlace = (JsonObject) ((JsonObject) o).get("place");
                String tweetText = ((JsonObject) o).get("text").getAsString();
                String image = "";

                JsonArray jsonimage = (JsonArray) ((JsonObject) o).get("mediaEntities");

                for (int n = 0; n < jsonimage.size(); n++) {
                    JsonObject elementiArray = (JsonObject) ((JsonArray) jsonimage).get(n).getAsJsonObject();

                    if (elementiArray.get("mediaURLHttps") != null) {

                    
                        image = elementiArray.get("mediaURLHttps").getAsString();
                    }
                }

                jsonGeoLocation = (JsonObject) ((JsonObject) o).get("geoLocation");
                if (jsonGeoLocation != null) {
                    double latitude = Double.parseDouble(jsonGeoLocation.get("latitude").toString());
                    double longitude = Double.parseDouble(jsonGeoLocation.get("longitude").toString());
                    positions.add(new Position(latitude, longitude, tweetText, image));
                } else if (jsonPlace != null) {
                    JsonArray jsonPlaceString = jsonPlace.get("boundingBoxCoordinates").getAsJsonArray();
                    GeoLocation[][] geoLocations = gson.fromJson(jsonPlaceString, GeoLocation[][].class);
                    positions.add(positionWithBB(geoLocations, tweetText, image));
                }

            }

        }
    }

    // Dato un campo GeoLocation e una descrizione provenienti da un tweet,
    // restituisce l'oggetto position corrispondente
    private Position positionWithBB(GeoLocation[][] geoLocations, String tweetText, String image) {
        // getBoundingBoxCoordinates restituisce una matrice 1x4, quindi
        // [0][0],[0][1],[0][2],[0][3]
        // formula centro per n punti: (x1+x2+x3+x4)/4,(y1,y2,y3,y4)/4
        double latitude = (geoLocations[0][0].getLatitude() + geoLocations[0][1].getLatitude()
                + geoLocations[0][2].getLatitude() + geoLocations[0][3].getLatitude()) / 4;
        double longitude = (geoLocations[0][0].getLongitude() + geoLocations[0][1].getLongitude()
                + geoLocations[0][2].getLongitude() + geoLocations[0][3].getLongitude()) / 4;
        return new Position(latitude, longitude, tweetText, image);
    }

    public void openChart() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/chart.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            ChartController chartController = fxmlLoader.getController();
            chartController.transferJsonTweets(jsonTweetList);
            Stage stage = new Stage();
            stage.setTitle("Grafico");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't load chart window");
        }
    }

    public void postTweet() {
        try {
        String CONSUMER_KEY = "";
        String CONSUMER_SECRET = "";
        String ACCESS_TOKEN = "";
        String ACCESS_TOKEN_SECRET = "";

        FileReader fr;
        
            fr = new FileReader("keys.json");
        BufferedReader br = new BufferedReader(fr);
        Gson gson = new Gson();

        JsonObject keys;
       
            keys = gson.fromJson(br.readLine(), JsonObject.class);
        
            CONSUMER_KEY = keys.get("CONSUMER_KEY").getAsString();
            CONSUMER_SECRET = keys.get("CONSUMER_SECRET").getAsString();
            ACCESS_TOKEN = keys.get("ACCESS_TOKEN").getAsString();
            ACCESS_TOKEN_SECRET = keys.get("ACCESS_TOKEN_SECRET").getAsString();

            Twitter twit = new TwitterFactory().getInstance();
            twit.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
            AccessToken accessToken = new AccessToken(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
            twit.setOAuthAccessToken(accessToken);

        // Window w = wordCloudWebView.getScene().getWindow();
        //  double xWindow = w.getX();
        //  double yWindow = w.getY();
        //  double widthWindow = w.getWidth();
        //  double heightWindow = w.getHeight();
        //  double x = owx + 30.0 + oww/3;
        //  double y = owy - 5.0 + owh/2 ;
        //  double width = oww - x - oww/32;
        //  double height = owh - y - owh/8 ;
            Window ow = Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            
            double owx = ow.getX();
            double owy = ow.getY();
            double oww = ow.getWidth();
            double owh = ow.getHeight();

            BufferedImage createScreenCapture = new Robot().createScreenCapture(new Rectangle((int)owx+500, (int)owy+369,(int)oww-520 ,(int)owh-395));
            ImageIO.write(createScreenCapture, "png", new File("screen.png"));
            StatusUpdate tweet = new StatusUpdate("#Team19");
            File imagefile = new File("screen.png");
            tweet.setMedia(imagefile);
            twit.updateStatus(tweet);

            System.out.println("Successfully updated the status in Twitter.");
            br.close();
            }catch (FileNotFoundException e) {

            e.printStackTrace();
           }
            catch (JsonSyntaxException e) {

             e.printStackTrace();}
             catch (IOException e) {

             e.printStackTrace();
            } catch (AWTException e) {

             e.printStackTrace();
             } catch (TwitterException e) {

             e.printStackTrace();
             }
    }
    

}
