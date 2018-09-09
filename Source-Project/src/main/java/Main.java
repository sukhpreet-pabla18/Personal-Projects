/**
 * Created by Sukhpreet on 8/14/2018.
 */
import javafx.util.Pair;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    private File file;
    private String apiKey = null;
    private String searchEngineID = null;
    private final String searchURL;
    private TokenizerModel tokenmodel;
    private POSModel tagmodel;
    private ChunkerModel chunkmodel;
    private SentenceModel sentmodel;


    public Main() {
        try {
            file = new File("keys.txt");
        }
        catch (NullPointerException e) {
            System.out.println("File not found");
            System.exit(1);
        }

        try {
            Scanner scan = new Scanner(file);
            apiKey = scan.nextLine();
            searchEngineID = scan.nextLine();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
            System.exit(1);
        }
        searchURL = "https://www.googleapis.com/customsearch/v1?";
        try {
            InputStream modelIn = new FileInputStream("en-token.bin");
            tokenmodel = new TokenizerModel(modelIn);
            InputStream modelIn2 = new FileInputStream("en-pos-maxent.bin");
            tagmodel = new POSModel(modelIn2);
            InputStream modelIn3 = new FileInputStream("en-chunker.bin");
            chunkmodel = new ChunkerModel(modelIn3);
            InputStream modelIn4 = new FileInputStream("en-sent.bin");
            sentmodel = new SentenceModel(modelIn4);
        }
        catch (IOException e) {
            System.out.println("Error, please run the program again");
            System.exit(1);
        }
    }

//-----------------------------------------------------------------------------------------------------------------------
//PART 1

    /* Uses Google CustomSearchAPI to get batches of 10 search results at a time
       Uses buildSearchString() to get the URL of the list of 10 search results
       Adds every link in search results to the listOfURLS ArrayList, which is utilized by scrapePages()
    */
    public ArrayList<String> search(String query) {
        if (!query.substring(0, 8).equals("How many"))
            throw new IllegalArgumentException("Query must start with \"How many\"");
        ArrayList<String> listOfURLs = new ArrayList<String>();
        for (int startIndex = 1; startIndex < 20; startIndex+=10) {
            try {
                String pUrl = buildSearchString(query, startIndex);
                URL url = new URL(pUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                    if (line.contains("\"link\""))            //"link" section denotes start of new source
                        listOfURLs.add(line.substring(line.indexOf("http"), line.indexOf("\",")));
                }
            } catch (IOException e) {
                System.out.println("Couldn't connect to Google CustomSearch webpage, try again");
                System.exit(1);
            }
        }
        return listOfURLs;
    }


    /* searchString: search Query
       start: index of first search result
       Builds the URL that we'll be connecting to in search() to get search results from Google CustomSearch
     */
    private String buildSearchString(String searchString, int start) {
        String toSearch = searchURL + "key=" + apiKey + "&cx=" + searchEngineID + "&q=";

        // replace spaces in the search query with +
        String newSearchString = searchString.replace(" ", "%20");

        toSearch += newSearchString;

        // specify response format as json
        toSearch += "&alt=json";

//        exactTerms = exactTerms.replace(" ", "%20");
//        toSearch += "&exactTerms=" + exactTerms;

        // specify starting result number
        toSearch += "&start=" + start;
        return toSearch;
    }


//-----------------------------------------------------------------------------------------------------------------------
//PART 2

    /* Query is tokenized (separated into its constituent words) and then tagged (each word's part of speech is determined)
       Using the results of the tokenizer and tagger, query is then chunked (the different types of phrases -- i.e. noun
       phrases, verb phrases, prepositional phrases, etc. -- in the query are determined)
       Every noun phrase is added to the phrases ArrayList
       After all noun phrases in the query are found, the ArrayList is returned
     */
    public ArrayList<String> createPhrases(String query) {
        query = query.substring(9);                        //gets rid of the "How many" from the query
        ArrayList<String> phrases = new ArrayList<String>();
        Tokenizer tokenizer = new TokenizerME(tokenmodel);
        POSTaggerME tagger = new POSTaggerME(tagmodel);
        ChunkerME chunker = new ChunkerME(chunkmodel);

//        WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(query);
        String[] tags = tagger.tag(tokens);
        POSSample taggedSentence = new POSSample(tokens, tags);
        System.out.println("Tagged sentence: " + taggedSentence.toString());

        String[] chunks = chunker.chunk(tokens, tags);
        System.out.print("Chunked sentence: ");
        for (int i = 0; i < chunks.length; i++) {
            System.out.print(chunks[i] + "  ");
            if (chunks[i].equals("B-NP")) {                   //finds and builds the entire noun phrase
                String nounPhrase = tokens[i];
                for (int j = i + 1; j < chunks.length; j++) {
                    if (chunks[j].equals("I-NP"))
                        nounPhrase += " " + tokens[j];
                    else {
                        i = j-1;
                        break;
                    }
                }
                phrases.add(nounPhrase);
            }
        }
        System.out.println();
        return phrases;
    }


    /* For each URL obtained from the search() method:
        1. JSoup gets the webpage and scrapes it for all its text
        2. The SentenceDetectorME instance separates out each sentence from the text
        3. If a sentence contains all the phrases in the phrases list, it is searched for a double
        4. If the sentence contains a double, we put (index of URL in search results, double value) in the TreeMap and
           move on to the next URL
        5. If the sentence doesn't contain a double, we keep going through the sentences until we find another one
           containing all the phrases in the phrases list
     */
    public TreeMap<Integer, Double> scrapePages(String query, ArrayList<String> listOfURLs) {
        TreeMap<Integer, Double> map = new TreeMap<Integer, Double>();
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentmodel);
        Document doc = null;
        ArrayList<String> phrases = createPhrases(query);

        for (int i = 0; i < listOfURLs.size(); i++) {
            try {
                doc = Jsoup.connect(listOfURLs.get(i)).get();
            } catch (IOException e) {
                continue;
            }
            doc = doc.normalise();
            Element body = doc.body();
            String sentences[] = sentenceDetector.sentDetect(body.wholeText());
            boolean goodSentence;
            for (String s: sentences) {
                goodSentence = true;
                for (String p: phrases) {
                    if (!s.contains(p)) {
                        goodSentence = false;
                        break;
                    }
                }
                if (goodSentence) {
                    boolean gotValue = false;
                    double val = findDouble(s);            //finds the first double in the sentence
                    if (val != Double.POSITIVE_INFINITY) {
                        map.put(listOfURLs.size() - i, val);  //the key is # of search results - index of search result
                        break;
                    }
                }
            }
        }
        return map;
    }


    /* Helper method that finds and returns the first double in a String
       Returns Double.POSITIVE_INFINITY if no double found
     */
    public double findDouble(String s) {
        s = s.replaceAll(",", "");
        String dblAsString = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean decimal = false;
            if (c >= 48 && c <= 57) {
                dblAsString += c;
                for (int j = i+1; j < s.length(); j++) {
                    char c2 = s.charAt(j);
                    if (c2 == 46) {
                        if (decimal)
                            break;
                        decimal = true;
                        dblAsString += c2;
                    }
                    else if (c2 >= 48 && c2 <= 57)
                        dblAsString += c2;
                    else
                        break;
                }
                return Double.parseDouble(dblAsString);
            }
        }
        return Double.POSITIVE_INFINITY;
    }


//-------------------------------------------------------------------------------------------------------------------------------
//PART 3

    //Inputs: TreeMap mapping the source's score to the value in the source (found in Part 2), and an error (in decimal form)
    //Output: HashMap mapping the bucketIndex (w.r.t. to the histogram) to a Pair mapping the list of values in the
    //bucket to the total score of all the sources which are represented in the bucket
    private HashMap<Integer, Pair<ArrayList<Double>, Integer>> makeHistogram(TreeMap<Integer, Double> inputMap,
                double error) {
        assert inputMap != null && !inputMap.isEmpty() && error > 0 && error < 1 : "Invalid inputs";

        HashMap<Integer, Pair<ArrayList<Double>, Integer>> histogram = new HashMap<Integer, Pair<ArrayList<Double>, Integer>>();
        ArrayList<Double> origValues = new ArrayList<Double>(inputMap.values());

        //sortedValues will be used just to find the 1st and 3rd quartiles of scores
        ArrayList<Double> sortedValues = new ArrayList<Double>(inputMap.values());
        sortedValues.sort(null);

        //MIN is the smallest value and MAX is the biggest value in the histogram
        final double MAX = sortedValues.get(sortedValues.size()-1);
        final double MIN = sortedValues.get(0);

        final double SIZE = (MAX - MIN) * error; //Size of each histogram bucket

        ArrayList<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(inputMap.entrySet());

        //For loop iterates through all the values but skips the ones that are not between first and third quartiles
        for (int valueIndex = 0; valueIndex < origValues.size(); valueIndex++) {
            double value = origValues.get(valueIndex);
            int bucketNum = (int) ((origValues.get(valueIndex) - MIN) / SIZE);

            //If the bucket's already in the map, update the list and replace the mapping, with the value being a Pair
            // containing the updated list and the updated score
            if (histogram.containsKey(bucketNum)) {
                ArrayList<Double> lst = histogram.get(bucketNum).getKey();
                lst.add(value);
                histogram.put(bucketNum, new Pair(lst, histogram.get(bucketNum).getValue() + entryList.get(valueIndex).getKey()));
            }
            //If the bucket's not already in the map, add the mapping to the map, with the value being a Pair
            //mapping a list (which just contains the value at valueIndex) to the score of the source
            else {
                ArrayList<Double> newLst = new ArrayList<Double>();
                newLst.add(origValues.get(valueIndex));
                histogram.put(bucketNum, new Pair(newLst, entryList.get(valueIndex).getKey()));
            }
        }
        return histogram;
    }


//-----------------------------------------------------------------------------------------------------------------------------
//PART 4

    //Input : a HashMap mapping the bucketIndex to a Pair that maps the values in the bucket
    //to the total score of all the sources represented in the bucket (output of makeHistogram())
    //Output : the median of the values in the bucket with the lowest total score
    private double getValue(HashMap<Integer, Pair<ArrayList<Double>, Integer>> inputMap) {
        if (inputMap.isEmpty())
            throw new IllegalArgumentException("Map is empty");

        //inputMap.values() gets a Collection of all the values that are mapped to in the map
        ArrayList<Pair<ArrayList<Double>, Integer>> mapValues = new ArrayList<Pair<ArrayList<Double>, Integer>>(inputMap.values());
        int maxIndex = -1;
        double maxScore = Integer.MIN_VALUE;

        //Finds the lowest total score and the index at which it was found
        for (int i = 0; i < mapValues.size(); i++) {
            Pair<ArrayList<Double>, Integer> pair = mapValues.get(i);
            if (pair.getValue() > maxScore) {
                maxScore = pair.getValue();
                maxIndex = i;
            }
        }

        //Get the list at index minIndex (represents the bucket of values with the lowest total score) and returns the median
        ArrayList<Double> chosenBucket = mapValues.get(maxIndex).getKey();
        chosenBucket.sort(null); //Need to sort the list of values before finding the median
        return findMedian(chosenBucket); //Get the median of the bucket
    }

    //Input: a list of all the values in the histogram bucket
    //Output: the median of the list
    private double findMedian(ArrayList<Double> values) {
        if (values == null || values.size() == 0)
            throw new IllegalArgumentException("List is either null or empty");
        if (values.size() % 2 == 1)
            return values.get(values.size() / 2);
        return (values.get(values.size() / 2) + values.get(values.size() / 2 + 1)) / 2;
    }

//-----------------------------------------------------------------------------------------------------------------------------
    //PART 5
    private double solution(String query, double error) {
        HashMap<Integer, Pair<ArrayList<Double>, Integer>> indexToPairMap = null;
        ArrayList<String> listOfURLs = search(query);
        TreeMap<Integer, Double> rankToValuesMap = scrapePages(query, listOfURLs);
        indexToPairMap = makeHistogram(rankToValuesMap, error);
        double answer = getValue(indexToPairMap);
        return answer;
    }

//-----------------------------------------------------------------------------------------------------------------------------
    //MAIN METHOD
    public static void main (String[] args) {
        System.out.println("What is your query?");
        Scanner scan = new Scanner(System.in);
        String query = scan.nextLine();
        Main main = new Main();
        double sol = main.solution(query, 0.02);
        System.out.println("Answer: " + sol);

//        main.scrapePages();
//        System.out.println("How many people died in the Civil War");
//        main.createPhrases(scan.nextLine());
//        main.createPhrases("How many people died in the Civil War");

    }
}
