/**
 * Author: Declan ONUNKWO
 * College: SUNY Oswego
 * CSC 365 Project 2
 * Fall 2023
 */

import Generators.WebScraper;
import HashClasses.CustomHashTable;
import HashClasses.ExtendibleHashing;

import javax.json.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;

public class SimilarityAlgorithm {
    private JFormattedTextField userInput;
    private JPanel panel;
    private JButton runButton;
    private JButton copyButton1;
    private JTextField outputLink1;
    private JTextField outputLink2;
    private JButton copyButton2;

    public JPanel getPanel() {
        return this.panel;
    }

    public SimilarityAlgorithm() throws IOException, ClassNotFoundException {

        // Read clusters
        HashMap<String, String> clusters = readClusters();

        // Deserialize before listeners

        // Deserialize Extensible hashing class
        FileInputStream fis = new FileInputStream("SerializedExtensibleHashingClass");
        ObjectInputStream ois = new ObjectInputStream(fis);
        ExtendibleHashing urlsMappedToObject = (ExtendibleHashing) ois.readObject();

        // Deserialize Dictionary
        FileInputStream fis1 = new FileInputStream("SerializedDictionary");
        ObjectInputStream ois1 = new ObjectInputStream(fis1);
        CustomHashTable dictionary = (CustomHashTable) ois1.readObject();

        // Deserialize myUrls list
        FileInputStream fis2 = new FileInputStream("SerializedUrlList");
        ObjectInputStream ois2 = new ObjectInputStream(fis2);
        CustomHashTable deserializedUrlList = (CustomHashTable) ois2.readObject();
        String[] myUrls = deserializedUrlList.toKeyList();

        runButton.addActionListener(e -> {
            try {
                String userInput = this.userInput.getText();
                findTwoMostSimilar(userInput, urlsMappedToObject, dictionary, myUrls,clusters);
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });

        copyButton1.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(outputLink1.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection,null);
        });

        copyButton2.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(outputLink2.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection,null);
        });
    }

    private HashMap<String, String> readClusters() throws IOException {

        // Instantiate map
        HashMap<String,String> clusters = new HashMap<>();

        // Properties
        Properties properties = new Properties();
        FileInputStream fis = null;
        try{
            fis = new FileInputStream("config.properties");
        }catch (FileNotFoundException e){
            System.out.println("change the filepath in `config.properties` to match yours.");
        }
        properties.load(fis);

        // GET json file location (change the .json file name below to match yours)
        String filePath = properties.getProperty("filepath") + File.separator + "10Clusters.json";
        File inputFile = new File(filePath);

        // READ json file
        InputStream inputStream = null;
        try{
            inputStream = new FileInputStream(inputFile);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        JsonReader reader = Json.createReader(inputStream);
        JsonObject jsonObject = reader.readObject();

        inputStream.close();
        reader.close();

        for(int i=0; i<jsonObject.size(); i++){
            JsonArray jsonArray = (JsonArray) jsonObject.get("Cluster " + (i+1));
            for(int j=0; j<jsonArray.size(); j++){
                clusters.put(jsonArray.getString(j),jsonArray.getString(0));
            }
        }

        return clusters;
    }

    private void findTwoMostSimilar
            (String userInput, ExtendibleHashing urlsMappedToObject, CustomHashTable dictionary,
             String[] myUrls, HashMap<String,String> clusters)
            throws IOException, ClassNotFoundException {

        WebScraper ws = new WebScraper();
        String[] userLinkWords = ws.webScrape(userInput);

        for(String s : userLinkWords){
            dictionary.countForIdf(s,201); // its 201 because this is the 201 url
        }

        CustomHashTable userUrlHashTable = new CustomHashTable();

        for(String s : userLinkWords){
            double idf = (double) userLinkWords.length / dictionary.getIdfCount(s);
            userUrlHashTable.advancedAdd(s,userLinkWords.length,idf);
        }

        double currSimilarityScore = 0;

        // initialize with a random low score less than zero
        double maxSimilarity = -1000;
        String urlOfMax = "";

        for(int i=0; i<myUrls.length; i++){
            ByteArrayInputStream bis = new ByteArrayInputStream(urlsMappedToObject.find(myUrls[i]));
            ObjectInputStream ois = new ObjectInputStream(bis);
            CustomHashTable myUrlHashTable = (CustomHashTable) ois.readObject();
            String[] myUrlWordList = myUrlHashTable.toKeyList();

            for(String s : myUrlWordList){
                double idf = (double) (myUrls.length + 1) / dictionary.getIdfCount(s);
                myUrlHashTable.setIdf(s,idf);
            }

            currSimilarityScore = doCosineSimilarity(userUrlHashTable, myUrlHashTable,myUrlWordList);

            if(currSimilarityScore > maxSimilarity){ // score must be greater than the smallest max to be considered
                maxSimilarity = currSimilarityScore;
                urlOfMax = myUrls[i];
            }
        }

        outputLink1.setText(urlOfMax);
        outputLink2.setText(clusters.get(urlOfMax));
    }

    public static double doCosineSimilarity
            (CustomHashTable userHt, CustomHashTable myHt, String[] myUrlWordList){

       // cosine similarity = A . B / (|A| * |B|), where A and B are vectors

        // numerator variables
        double a;
        double numerator = 0;

        // denominator variables
        double b = 0;
        double c = 0;

        for(String s : myUrlWordList){
            a = userHt.getTfidf(s) * myHt.getTfidf(s);
            b += (userHt.getTfidf(s) * userHt.getTfidf(s));
            c += (myHt.getTfidf(s) * myHt.getTfidf(s));
            numerator += a;
        }

        return numerator/((Math.sqrt(b)) * (Math.sqrt(c)));
    }
}
