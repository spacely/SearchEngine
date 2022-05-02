

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


public class InvertedIndexProject {

    private List<String> all_indexes = new ArrayList<String>();
    private List<String> all_contents = new ArrayList<String>();
private List<String> tokens_for_search = new ArrayList<String>();
    private Map<String,Integer> tf = new HashMap<String,Integer>();

    private Map<String, String> map_index_to_contents = new HashMap<String, String>();
    private List<String> queryFileLines = new ArrayList<String>();
    private Map<String, List<String>> invertedLists = new HashMap<>();

    final static String outputFilePath = "/Users/saheedadepoju/Documents/CSE272/SearchEngine/write.txt";
    final static String tfOutputFilePath = "/Users/saheedadepoju/Documents/CSE272/SearchEngine/tf_inverted_list.txt";

    private List<String> stopwords = Arrays.asList("a", "Are","AS","able", "about",
            "across", "after", "all", "almost", "also", "am", "among", "an",
            "and", "any", "are", "as", "at", "be", "because", "been", "but",
            "by", "can", "cannot", "could", "dear", "did", "do", "does",
            "either", "else", "ever", "every", "for", "from", "get", "got",
            "had", "has", "have", "he", "her", "hers", "him", "his", "how",
            "however", "i", "if", "in", "into", "is", "it", "its", "just",
            "least", "let", "like", "likely", "may", "me", "might", "most",
            "must", "my", "neither", "no", "nor", "not", "of", "off", "often",
            "on", "only", "or", "other", "our", "own", "rather", "said", "say",
            "says", "she", "should", "since", "so", "some", "than", "that",
            "the", "their", "them", "then", "there", "these", "they", "this",
            "tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why",
            "will", "with", "would", "yet", "you", "your","given","options","u");


    public static void main(String args[]) throws IOException {

        InvertedIndexProject iip = new InvertedIndexProject();
        iip.getAll_indexes("/Users/saheedadepoju/Documents/CSE272/SearchEngine/ohsumed.88-91");
        iip.getAllContents("/Users/saheedadepoju/Documents/CSE272/SearchEngine/ohsumed.88-91");

        // System.out.println(iip.all_indexes.get(iip.all_contents.size()));
        //System.out.println(iip.all_contents.get(iip.all_contents.size()-1));

        for (int i = 0; i < iip.all_contents.size(); i++) {
            iip.mapIndexToContents(iip.all_indexes.get(i), iip.all_contents.get(i));
            // System.out.println("Indexed "+ i);
        }

        iip.queryFileLines = iip.parseQueryfile("/Users/saheedadepoju/Documents/CSE272/query.ohsu.1-63");
        iip.tokens_for_search = iip.trimQueryFile(iip.queryFileLines);

        for (int i = 0; i < iip.tokens_for_search.size(); i++) {
            //System.out.println(iip.tokens_for_search.get(i));
            String tokens[] = iip.tokens_for_search.get(i).split(" ");
            for(int k=0;k<tokens.length;k++) {
                if(iip.stopwords.contains(tokens[k])){
                    continue;
                }
                for (Map.Entry<String, String> entry : iip.map_index_to_contents.entrySet()) {
                    String text_entry = entry.getValue();

                    if (text_entry.contains(tokens[k].toLowerCase())) {//check for the stop words
                      //  System.out.println("Token currently being searched and found " + tokens[k]);
                        //System.out.println("Text searched" + iip.queryFileLines.get(i));
                       // System.out.println("Document is " + entry.getKey());
                        //System.out.println("Content is " + entry.getValue());
                       // List<String> docs_list = new ArrayList<String>();
                        if(iip.invertedLists.containsKey(tokens[k])){
                            List<String> docs_list = iip.invertedLists.get(tokens[k]);
                            docs_list.add(entry.getKey());
                            iip.invertedLists.put(tokens[k],docs_list);
                        }
                        else{
                            List<String> new_doc_list = new ArrayList<String>();
                            new_doc_list.add(entry.getKey());
                            iip.invertedLists.put(tokens[k],new_doc_list);
                        }

                    }

                    // System.out.println(iip.map_index_to_contents.containsValue(iip.queryFileLines.get(i)));

                }
            }
        }

        iip.WriteInvertedList(iip.invertedLists);
        iip.generate_tf(iip.invertedLists);
        iip.writeTrecTF(iip.tf);



    }

    public void getAll_indexes(String filename) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        while ((line = br.readLine()) != null) if (line.contains(".I")) {

            all_indexes.add(line);

        }
        br.close();

    }

    public void getAllContents(String filename) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        while ((line = br.readLine()) != null) if (line.contains(".W")) {

            all_contents.add(br.readLine());

        }
        br.close();


    }

    public void mapIndexToContents(String index, String contents) {
        map_index_to_contents.put(index, contents);

    }

    public List<String> parseQueryfile(String filename) {
        List<String> lines = new ArrayList<>();


        try {
            lines = Files.readAllLines(Paths.get(filename));


        } catch (IOException e) {
            System.out.println("Got to the end of the file.");
            System.out.println(e.getMessage());
            e.printStackTrace();

        }

        return lines;
    }

    public List<String> trimQueryFile(List<String> AllQueryTokens) {
        List<String> trimmed_query_tokens = new ArrayList<String>();
        for (String query_lines : AllQueryTokens) {
            //System.out.println((query_lines));
            //query_lines = query_lines.replace("\\s","");
            //System.out.println((query_lines));

            if (query_lines.trim().isEmpty() || query_lines.contains("</top>") || query_lines.contains("<top>") || query_lines.contains("<num>") || query_lines.contains("<title") || query_lines.contains("<desc>")) {
                continue;

            }
            trimmed_query_tokens.add(query_lines);
        }
        return trimmed_query_tokens;


    }

    public void WriteInvertedList(Map<String, List<String>> invertedLists ) throws IOException{

        File file = new File(outputFilePath);

        BufferedWriter bf = null;
        bf = new BufferedWriter(new FileWriter(file));

        for (Map.Entry<String, List<String>> entry :
                invertedLists.entrySet()) {

            // put key and value separated by a colon
            bf.write(entry.getKey() + ":"
                    + entry.getValue());

            // new line
            bf.newLine();
        }

        bf.flush();

    }

    public void writeTrecTF(Map<String,Integer> inverted_in_TF) throws IOException{
        File file = new File(tfOutputFilePath);

        BufferedWriter bf = null;
        bf = new BufferedWriter(new FileWriter(file));

        for (Map.Entry<String, Integer> entry :
                inverted_in_TF.entrySet()) {

            // put key and value separated by a colon
            bf.write(entry.getKey() + ":"
                    + entry.getValue());

            // new line
            bf.newLine();
        }

        bf.flush();

    }

    public void generate_tf(Map<String, List<String>> invertedLists){



        for (Map.Entry<String, List<String>> entry :
                invertedLists.entrySet()) {

            tf.put(entry.getKey(),entry.getValue().size());
        }

    }

}
