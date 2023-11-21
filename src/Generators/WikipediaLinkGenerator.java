/**
 * Author: Declan Onunkwo
 * Date: 12-oct-2023
 *
 * Description: A program that generates 200 wikipedia links.
 */

package Generators;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class WikipediaLinkGenerator {
    public static void main(String[] args) throws IOException {

        System.out.println("\nStarting Links Generator...");

        HashSet<String> generatedLinks = generateRandomLinks();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(String s : generatedLinks){
            jsonArrayBuilder.add(s);
        }

        JsonArray linksArray = jsonArrayBuilder.build();

        writeJsonFile(linksArray);
    }

    private static void writeJsonFile(JsonArray linksArray) {

        // Create a new Json File (change path to match yours if you are borrowing this code)
        String filePath = "/Users/declan/IdeaProjects/Wikipedia-Page-Similarity-II/src/";
        File outputFile = new File(filePath + "MyLinks.json");

        OutputStream outputStream = null;
        try{
            outputStream = new FileOutputStream(outputFile);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Json.createWriter(outputStream);
        JsonWriter writer;

        // create a configuration to allow pretty_printing (a nice and organized output file)
        Map<String,Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory wFactory = Json.createWriterFactory(config);

        // write linksArray in our outputFile
        writer = wFactory.createWriter(outputStream);
        writer.writeArray(linksArray);
        writer.close();
        System.out.println("Links Generated in a 'MyLinks.json' file. Happy Coding! :)");
    }

    private static HashSet<String> generateRandomLinks() throws IOException {

        System.out.println("one second...\n");

        // get the wikipedia link or the source of our collection of links
        String sourceUrl = "https://en.wikipedia.org/wiki/User:The_Anome/The_three_thousand";

        // get the list of a[href]
        Document doc = Jsoup.connect(sourceUrl).get();
        Elements liHref = doc.select(".mw-parser-output  p a[href]");

        // randomly select one of the a[href] using a random index
        int sizeOfList = liHref.size();
        Random random = new Random();

        // initialize a hashset to store link--helps avoid repeats and a watchdog to exit long loop.
        HashSet<String> links = new HashSet<>();
        int watchDog = 0;

        while(links.size() < 200){
            // this is to make the bound 1 to sizeOfList. The 0th link was sourceURL :(
            int randomIndex = random.nextInt(sizeOfList - 1) + 1;

            // concatenate the href link we got with randomLink to get a full wiki link
            String randomLink = "https://en.wikipedia.org" + liHref.get(randomIndex).attr("href");

            links.add(randomLink);
            watchDog += 1;

            if(watchDog > 400){
                System.out.println("Unable to generate links--loops went above limit. Please Try again");
                System.exit(0);
            }
        }

        return links;
    }
}