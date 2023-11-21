/**
 * Author: Declan Onunkwo
 * Date: 29-oct-2023
 *
 * Description: Generates all necessary persistent files required for a faster similarity
 *              calculation.
 *              Step 1: Make a persistent Byte Array for a URL's HashTable object
 *              Step 2: Map the URL to its Byte Array using Extendible Hashing (class provided)
 *              Step 3: Repeat Step 1 and 2 for every URL in the array of links/URLs
 *              Step 4: Create a persistent file for the ExtendibleHashing Object.
 */

package Generators;

import HashClasses.*;

import javax.json.*;
import java.io.*;
import java.util.Properties;

public class PersistentFilesGenerator {

    public static void main(String[] args) throws Exception {

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
        String filePath = properties.getProperty("filepath") + File.separator + "MyLinks.json";
        File inputFile = new File(filePath);

        // READ json file
        InputStream inputStream = null;
        try{
            inputStream = new FileInputStream(inputFile);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        JsonReader reader = Json.createReader(inputStream);
        JsonArray jArray = reader.readArray();
        inputStream.close();
        reader.close();

        createPersistentFiles(jArray);
    }

    private static void createPersistentFiles(JsonArray arrayOfLinks) throws Exception {
        WebScraper scraper = new WebScraper();

        ExtendibleHashing urlMapToFile = new ExtendibleHashing();
        CustomHashTable dictionary = new CustomHashTable();
        CustomHashTable myUrls = new CustomHashTable();

        for(int i=0; i<arrayOfLinks.size(); i++){ // populate dictionary
            String url = arrayOfLinks.getString(i);
            myUrls.add(url); // populate myLinksAsArray

            for(String s : scraper.webScrape(url)){
                dictionary.countForIdf(s,i+1);
            }
        }

        for(int i=0; i<arrayOfLinks.size(); i++){
            CustomHashTable ht = new CustomHashTable();
            String url = arrayOfLinks.getString(i);
            String[] arrayOfWords = scraper.webScrape(url);

            for(String s : arrayOfWords){
                double idf = (double) arrayOfLinks.size() / dictionary.getIdfCount(s);
                ht.advancedAdd(s,arrayOfWords.length,idf);
            }

            // For each url, serialize its hashtable object and map to its url to serialized object
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(ht);
            urlMapToFile.insert(url,bos.toByteArray());
        }

        // Serialize the ExtendibleHashing Object to be used by other classes
        FileOutputStream fos = new FileOutputStream("SerializedExtensibleHashingClass");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(urlMapToFile);

        // Serialize the Dictionary Object to be used by other classes
        FileOutputStream fos1 = new FileOutputStream("SerializedDictionary");
        ObjectOutputStream oos1 = new ObjectOutputStream(fos1);
        oos1.writeObject(dictionary);

        // Serialize the myUrls Object to be used by other classes
        FileOutputStream fos2 = new FileOutputStream("SerializedUrlList");
        ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
        oos2.writeObject(myUrls);

    }
}