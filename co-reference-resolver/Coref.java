
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.Integer.*;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class Coref {

// Original punctuation added at concactenation
// Better splitting into sentences ( 2017. )  //use what coref uses in main!

  static String line;
  static ArrayList<String> changedSentences = new ArrayList<>();
  static String[] sentences;

  public static void resolveChain (List l)
  {
    ArrayList <String> pairList = new ArrayList<>();
    if ( l.size() >1 )
    {
      for (int i=1; i<l.size();i++)
       {
         pairList.clear();
         pairList.add(l.get(0).toString());
         pairList.add(l.get(i).toString());
         resolve(pairList);
       }
             
    }

   else{ 
       //System.out.println("++++++++++++++++++++");
       //System.out.println("WAS HERE!");
      }
 }

public static void resolve(ArrayList l)
{
      //System.out.println("more than one "+ l.size()); 
      //System.out.println("THE LIST === " + l);
      System.out.println("------------------------------------");
      System.out.println("The original sentence : " + line);
      System.out.println("------------------------------------");
      
      sentences = line.split("(?<=[a-z])\\.\\s+");         
      
      String firstElement = l.get(0).toString(); //"President Trump" in sentence 2

      String secondElement = l.get(1).toString();  //"him" in sentence 3
      
      int sent_num = Integer.parseInt(secondElement.substring(secondElement.length() - 1)); // sentence number in which to make change
      //System.out.println( "REPLACE IN = "+ sent_num);

      String replace_in; //sentence in which words will be matched and replaced
 
      //System.out.println("size is now "+ changedSentences.size()+"\n"+ changedSentences); 

      if (changedSentences.size()==sent_num) //then this sentence had an earlier anaphora resolved
      {
           replace_in = changedSentences.get(sent_num-1);
           //System.out.println("earlier anaphora in " + replace_in);
           changedSentences.remove(sent_num-1);
      }
      else{
          replace_in =  sentences[sent_num-1];
          //System.out.println("Original REPLACE_IN= " + replace_in);
      }

      Pattern pattern = Pattern.compile(("\"(.*?)\""));  // extract string in double quotation
      Matcher matcher = pattern.matcher(firstElement); // President Trump
      Matcher matcher2 = pattern.matcher(secondElement); // him
      
     String newSentence="";
     if (matcher.find() && matcher2.find())
      {

       System.out.println();   
                  
       String replaceby = matcher.group(1);
       String original = matcher2.group(1);
       newSentence = replace_in.replace(original,replaceby);
       
       //System.out.println(newSentence);
       //System.out.println();
      
        
      }
    else{
      //System.out.println("Nothing found in double quotation");
     }

    //adding changed sentence and sentences in between (without anaphora), to the final arrayList 
    if(changedSentences.size()+1<sent_num)
      {
        int difference = sent_num - changedSentences.size();
        int currentSize = changedSentences.size();
        for(int i=0; i<difference; i++)
        {
          if(currentSize+i!=sent_num-1){
            changedSentences.add(sentences[currentSize+i]);
            //System.out.println("COMPARING "+String.valueOf(currentSize+i)+" "+ String.valueOf(sent_num-1) + "appended Original sentence\n");
           }
          else
            changedSentences.add(newSentence);               
        }
         
      }
     else{ 
        //No sentence in between current and last anaphoric sentence! 
        changedSentences.add(newSentence);
      }
    
   }  

public static void displayFull()
{
if(changedSentences.size() > 0){
 System.out.println("==============================================");

//Handle ending sentences that don't have anaphoras.
 if (changedSentences.size()!= sentences.length )
  { 
    int difference = sentences.length - changedSentences.size();
    int currentSize = changedSentences.size(); 
 
    for (int i=0; i<difference; i++){
      changedSentences.add(sentences[currentSize+i]);
    }
    
  }
  System.out.println(changedSentences);
  System.out.println("==============================================");
 }
}


  /** Usage: java -cp "*" StanfordCoreNlpDemo [inputFile [outputTextFile [outputXmlFile]]] */
  public static void main(String[] args) throws IOException {

    PrintWriter out;
    out = new PrintWriter(System.out);

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter sentence for Coreference resolution: ");
        line = in.readLine();
                
           
    // Create a CoreNLP pipeline. To build the default pipeline, you can just use:
    //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    // Here's a more complex setup example:
    //   Properties props = new Properties();
    //   props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
    //   props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
    //   props.put("ner.applyNumericClassifiers", "false");
    //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // Add in sentiment
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
    Annotation annotation;
    annotation = new Annotation(line);

    // run all the selected Annotators on this text
    pipeline.annotate(annotation);

    // this prints out the results of sentence analysis to file(s) in good formats
    pipeline.prettyPrint(annotation, out);
    
    // Access the Annotation in code
    // The toString() method on an Annotation just prints the text of the Annotation
    // But you can see what is in it with other methods like toShorterString()
    //out.println();
    //out.println("The top level annotation");
    //out.println(annotation.toShorterString());
    //out.println();

    // An Annotation is a Map with Class keys for the linguistic analysis types.
    // You can get and use the various analyses individually.
    // For instance, this gets the parse tree of the first sentence in the text.
    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
    if (sentences != null && ! sentences.isEmpty()) {
      CoreMap sentence = sentences.get(0);
      //out.println("The keys of the first sentence's CoreMap are:");
      //out.println(sentence.keySet());
      //out.println();
      //out.println("The first sentence is:");
      //out.println(sentence.toShorterString());
      //out.println();
      //out.println("The first sentence tokens are:");
      for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        //out.println(token.toShorterString());
      }
      Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
      //out.println();
      //out.println("The first sentence parse tree is:");
      tree.pennPrint(out);
      //out.println();
      //out.println("The first sentence basic dependencies are:");
      //out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));
      //out.println("The first sentence collapsed, CC-processed dependencies are:");
      SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
      out.println(graph.toString(SemanticGraph.OutputFormat.LIST));

      // Access coreference. In the coreference link graph,
      // each chain stores a set of mentions that co-refer with each other,
      // along with a method for getting the most representative mention.
      // Both sentence and token offsets start at 1!
      out.println("=======================================");
      out.println("Coreference information");
      out.println("=======================================");
      Map<Integer, CorefChain> corefChains =
          annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class);
      if (corefChains == null) { return; }
	//out.println("No Coreference Found");
        for (Map.Entry<Integer,CorefChain> entry: corefChains.entrySet()) {
        out.println("Chain " + entry.getKey());
        List myList = new ArrayList();  
        for (CorefChain.CorefMention m : entry.getValue().getMentionsInTextualOrder()) {
           myList.add(m);
          // create list 
          // everytime we have more than one m in a chain call resolver(list)
          //
          // We need to subtract one since the indices count from 1 but the Lists start from 0
          //List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);
          // We subtract two for end: one for 0-based indexing, and one because we want last token of mention not one following.
         // out.println("  " + m + ", i.e., 0-based character offsets [" + tokens.get(m.startIndex - 1).beginPosition() +
           //       ", " + tokens.get(m.endIndex - 2).endPosition() + ")");
          out.println(m);
          
        }
	
        resolveChain(myList);
       
     }
      out.println();
      displayFull();

     // out.println("The first sentence overall sentiment rating is " + sentence.get(SentimentCoreAnnotations.SentimentClass.class));
    }
    IOUtils.closeIgnoringExceptions(out);
    //IOUtils.closeIgnoringExceptions(xmlOut);
    
  }

}
