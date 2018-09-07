/**
 * Created by Sukhpreet on 8/14/2018.
 */
import javafx.util.Pair;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;

import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    File file;
    String apiKey = null;
    String searchEngineID = null;
    final String searchURL;
    public ArrayList<String> listOfURLs;
    TokenizerModel tokenmodel;
    POSModel tagmodel;
    ChunkerModel chunkmodel;

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
        listOfURLs = new ArrayList<String>();
        try {
            InputStream modelIn = new FileInputStream("en-token.bin");
            tokenmodel = new TokenizerModel(modelIn);
            InputStream modelIn2 = new FileInputStream("en-pos-maxent.bin");
            tagmodel = new POSModel(modelIn2);
            InputStream modelIn3 = new FileInputStream("en-chunker.bin");
            chunkmodel = new ChunkerModel(modelIn3);
        }
        catch (Exception e) {
            System.out.println("Error, please run the program again");
            System.exit(1);
        }
    }

    public void createPhrases(String query) {
        Tokenizer tokenizer = new TokenizerME(tokenmodel);
        POSTaggerME tagger = new POSTaggerME(tagmodel);
        ChunkerME chunker = new ChunkerME(chunkmodel);


        WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE;
        String[] tokens = whitespaceTokenizer.tokenize(query);
        String[] tags = tagger.tag(tokens);
        POSSample taggedSentence = new POSSample(tokens, tags);
        System.out.println("Tagged sentence: " + taggedSentence.toString());

        String[] chunks = chunker.chunk(tokens, tags);
        System.out.print("Chunked sentence: ");
        for (String s: chunks)
            System.out.print(s + "  ");
        System.out.println();
    }
//-----------------------------------------------------------------------------------------------------------------------------
//PART 2
    //NOTE: Don't worry about the printing stuff. That's just for debugging purposes
    public TreeMap<Integer, Double> search(String query, String exactTerms, double givenValue) {
        if (!query.substring(0, 8).equals("How many"))
            throw new IllegalArgumentException("Query must start with \"How many\"");
        TreeMap<Integer, Double> map = new TreeMap<Integer, Double>();
        for (int startIndex = 1; startIndex < 30; startIndex+=10) {
            try {
                //map that'll map the PageRank to the number in the source that's closest to givenValue
                String pUrl = buildSearchString(query, startIndex, exactTerms);
                int index = startIndex - 1; //index = PageRank, it's initialized to 0, 10, 20, etc
                URL url = new URL(pUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                    if (line.contains("\"link\"")) {  //only 10 occurrences of "link" in each batch of search results, so "link"
                        index++;                     // section denotes start of new source
                        listOfURLs.add(line.substring(line.indexOf("http"), line.indexOf("\",")));
                    }
                    else if (line.contains(("snippet"))) {
                        //Formatting for when a number is given as ab,cd or ab, cd rather than abcd
                        line = line.replace(", ", ",");
                        line = line.replace(",", "");
                        Scanner scan = new Scanner(line);
                        ArrayList<Double> nums = new ArrayList<Double>();

                        //While the line has another double, get it and add it to nums
                        while (scan.useDelimiter("\\D+").hasNextDouble()) {
                            double nxt = scan.useDelimiter("\\D+").nextDouble();
                            nums.add(nxt);
                        }

                        //Map the index (PageRank) to the closest value in nums to givenValue using the helper method
                        if (nums.size() > 0)
                            map.put(index, findClosestDoubleToGivenVal(nums, givenValue));
                    }
                }
                //return buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(map.values());
        return map;
    }

    //Find the closest double in the ArrayList to the givenValue (value that the debater provided)
    private double findClosestDoubleToGivenVal(ArrayList<Double> lst, double givenValue) {
        double diff = Math.abs(lst.get(0) - givenValue);
        int index = 0;
        for (int i = 1; i < lst.size(); i++) {
            if (Math.abs(lst.get(i) - givenValue) < diff) {
                diff = Math.abs(lst.get(i) - givenValue);
                index = i;
            }
        }
        return lst.get(index);
    }

    /* searchString: search Query
       start: index of first search result
       numOfResults: number of results
       exactTerms: filters out search results that don't contain this String
     */
    private String buildSearchString(String searchString, int start, String exactTerms) {
        String toSearch = searchURL + "key=" + apiKey + "&cx=" + searchEngineID + "&q=";

        // replace spaces in the search query with +
        String newSearchString = searchString.replace(" ", "%20");

        toSearch += newSearchString;

        // specify response format as json
        toSearch += "&alt=json";

        //specify exact phrase that each result must contain

        exactTerms = exactTerms.replace(" ", "%20");
//        toSearch += "&exactTerms=" + exactTerms;

        // specify starting result number
        toSearch += "&start=" + start;

        // specify the number of results you need from the starting position
//        toSearch += "&num=" + numOfResults;

        //System.out.println("Search URL: " + toSearch);
        return toSearch;
    }

//-------------------------------------------------------------------------------------------------------------------------------
//PART 3

    //Inputs are a TreeMap mapping the source's score to the value in the source (found in Part 1/2), and an error (in decimal form)
    //Output is a HashMap mapping the bucketIndex (w.r.t. to the histogram) to a Pair mapping the list of values in the
    //bucket to the total score of all the sources which are represented in the bucket
    private HashMap<Integer, Pair<ArrayList<Double>, Integer>> makeHistogram(TreeMap<Integer, Double> inputMap,
                double error) {
        assert inputMap != null && !inputMap.isEmpty() && error > 0 && error < 1 : "Invalid inputs";

        HashMap<Integer, Pair<ArrayList<Double>, Integer>> histogram = new HashMap<Integer, Pair<ArrayList<Double>, Integer>>();
        ArrayList<Double> origValues = new ArrayList<Double>(inputMap.values());

        //sortedValues will be used just to find the 1st and 3rd quartiles of scores
        ArrayList<Double> sortedValues = new ArrayList<Double>(inputMap.values());
        sortedValues.sort(null);

        //Starting index is the 1st quartile
        final double MAX = sortedValues.get(sortedValues.size()-1);
        final double MIN = sortedValues.get(0);

//        System.out.println("MIN: " + MIN);
//        System.out.println("MAX: " + MAX);
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

    //Input: a list of all the values in the histogram bucket, and the desired quartile
    //Output: the value in the list at the specified quartile
    private double findMedian(ArrayList<Double> values) {
        if (values == null || values.size() == 0)
            throw new IllegalArgumentException("List is either null or empty");
        if (values.size() % 2 == 1)
            return values.get(values.size() / 2);
        return (values.get(values.size() / 2) + values.get(values.size() / 2 + 1)) / 2;
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
        int minIndex = -1;
        double minScore = Integer.MAX_VALUE;

        //Finds the lowest total score and the index at which it was found
        for (int i = 0; i < mapValues.size(); i++) {
            Pair<ArrayList<Double>, Integer> pair = mapValues.get(i);
            if (pair.getValue() < minScore) {
                minScore = pair.getValue();
                minIndex = i;
            }
        }

        //Get the list at index minIndex (represents the bucket of values with the lowest total score) and returns the median
        ArrayList<Double> chosenBucket = mapValues.get(minIndex).getKey();
        chosenBucket.sort(null); //Need to sort the list of values before finding the median
        return findMedian(chosenBucket); //Get the median of the bucket
    }

//-----------------------------------------------------------------------------------------------------------------------------
    //PART 5
    private double solution(String query, String exactTerms, double error, double givenValue) {
        HashMap<Integer, Pair<ArrayList<Double>, Integer>> indexToPairMap = null;
        TreeMap<Integer, Double> rankToValuesMap = search(query, exactTerms, givenValue);
        indexToPairMap = makeHistogram(rankToValuesMap, 0.05);
        double answer = getValue(indexToPairMap);
        return answer;
    }

//-----------------------------------------------------------------------------------------------------------------------------
    //MAIN METHOD
    public static void main (String[] args) {
        //Getting and printing the answer (30 search results)
        Main main = new Main();
        System.out.println("How many people died in the Civil War");
        main.createPhrases("How many people died in the Civil War");
        double answer = main.solution("How many school shootings occurred in the US in 2016", "", 0.05, 400);
        System.out.println("Answer: " + answer);
    }
}
