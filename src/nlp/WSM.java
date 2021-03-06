package nlp;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @author: OmerTanwirHassan
 * Date: 6/8/14
 */
public class WSM {

    /**
     * Only public method.  Input sentence and get indexes of the relevant terms in the sentence.
     * @param textTokens
     * @return
     */
    public List<String[]> getTuples(List<String> textTokens) throws IOException, JWNLException {
        Map<String, Term> expandedTermsMap = getExpandedTermsList("res/Keywords.xlsx");
        //List<CoreLabel> textTokens = tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();

        List<String[]> tuples = new ArrayList<>();
        boolean actionItemFlag = false;
        String lastActionItem="", midText="";
//        for (CoreLabel tokenCl : textTokens) {
//            String tokenTextLemma = lemmatizeTerm(tokenCl.originalText());
        int i=0;
        for (String token : textTokens) {
            String tokenTextLemma = lemmatizeTerm(token).toLowerCase();
            if (!expandedTermsMap.containsKey(tokenTextLemma)) {
                if (actionItemFlag==true)   midText += tokenTextLemma + " ";
                continue;
            }
            if (actionItemFlag==false && expandedTermsMap.get(tokenTextLemma).getLabel().equals("action-item")) {
                actionItemFlag = true;
                lastActionItem = tokenTextLemma;
            } else if (actionItemFlag==true) {
                if (expandedTermsMap.get(tokenTextLemma).getLabel().equals("action-item")) {
                    lastActionItem = tokenTextLemma;
                    midText = "";
                } else if (expandedTermsMap.get(tokenTextLemma).getLabel().equals("datetime")) {
                    tuples.add(new String[]{lastActionItem, midText.trim(), token});
                    actionItemFlag = false;
                    midText = "";
                } else {
                    midText += tokenTextLemma + " ";
                }
            }

            if (expandedTermsMap.containsKey(tokenTextLemma) &&
                    expandedTermsMap.get(tokenTextLemma).getLabel().equals("datetime")) {
                for (int j = (i-5 < 0 ? 0 : i-5); j <= (i+5 >= textTokens.size()-1 ? textTokens.size()-2 : i+5); j++) {
                    if (textTokens.get(j).equalsIgnoreCase("next") &&
                            (textTokens.get(j+1).equalsIgnoreCase("meeting") ||
                                    textTokens.get(j+1).equalsIgnoreCase("time")))
                        tuples.add(new String[]{textTokens.get(j) + " " + textTokens.get(j+1), token});
                }
            }
            i++;
        }
        return tuples;
    }

    List<CoreLabel> posTagSentence(String sentence) {
        List<CoreLabel> cls = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        tagger.tagCoreLabels(cls);
        return cls;
    }

    Set<Integer> readTermsListAndCreateSenseIdList(String filePath) throws IOException {
        List<String> keywordList = new ArrayList<>();
        List<String> posList = new ArrayList<>();
        FileInputStream file = new FileInputStream(new File(filePath));
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);
        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell keyword = row.getCell(0);
            Cell pos = row.getCell(1);
            if (keyword.getStringCellValue().equals("Keyword") || pos.getStringCellValue().equals("Type"))
                continue;
            keywordList.add(keyword.getStringCellValue());
            posList.add(pos.getStringCellValue());
        }
        file.close();

        Set<Integer> senseIdList = new HashSet<>();
        for (int i=0; i < keywordList.size(); i++) {
            String keyword = keywordList.get(i), pos = posList.get(i);
            //int[] s = wordnet.getSenseIds(keyword, pos);
        }
        return senseIdList;
    }

    Map<String, Term> getExpandedTermsList(String filePath) throws IOException, JWNLException {
        Map<String, Term> map = new HashMap<>();
        FileInputStream file = new FileInputStream(new File(filePath));
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);
        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            //Cell keyword = row.getCell(0);
            String keyword = row.getCell(0).getStringCellValue().toLowerCase();
            if (keyword.equalsIgnoreCase("Keyword")) continue;
//            Cell pos = row.getCell(1);
//            Cell label = row.getCell(2);
            String pos = row.getCell(1).getStringCellValue().toLowerCase();
            String label = row.getCell(2).getStringCellValue().toLowerCase();
            if (!map.containsKey(keyword))
                map.put(keyword, new Term(keyword, label, pos));
            String[] synonyms = jwnlHelper.getSynsets(pos, keyword);
            for (String synonym : synonyms)
                if (!map.containsKey(synonym))  // TODO: convert pos eg 'noun' to 'N' etc
                    map.put(synonym, new Term(synonym, label, pos));
        }
        file.close();
        return map;
        /*Set<Term> expandedTermsList = new HashSet<>();
        for (Map.Entry<String, Term> entry : map.entrySet()) {
            String term = entry.getKey();
            Term termObj = entry.getValue();
            // TODO: convert pos eg 'noun' to 'N' etc
            String[] synonyms = jwnlHelper.getSynsets(termObj.getPos(), term);
            for (String synonym : synonyms)
                expandedTermsList.add(new Term(synonym, termObj.getLabel(), termObj.getPos()));
        }*/
        //return expandedTermsList;
    }

    String lemmatizeTerm(String term) {
        Annotation document = new Annotation(term);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> lemmatizedSentence = new ArrayList<>();
        for (CoreMap sentence : sentences)
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class))
                lemmatizedSentence.add(token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
        return lemmatizedSentence.get(0);
    }



//    static RiWordnet wordnet;
    static MaxentTagger tagger;
    static TokenizerFactory<CoreLabel> tokenizerFactory;
    static JWNLHelper jwnlHelper;
    static StanfordCoreNLP pipeline;
    void initialize() {
        tagger = new MaxentTagger(
                "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
                "invertible=true");
        //wordnet = new RiWordnet(this, "C:\\Program Files (x86)\\WordNet\\2.1");
        jwnlHelper = new JWNLHelper();

        Properties props = new Properties();
        props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);

    }
    public WSM() {
        initialize();
    }

    public static void main(String[] args) {
        try {
            String text = "action action developers asdf Bob time May twenty-first during the next meeting";

            // Do NER on question
            /*String serializedClassifier = "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz";
            AbstractSequenceClassifier<CoreLabel> classifier =
                    CRFClassifier.getClassifierNoExceptions(serializedClassifier);
            List<CoreLabel> queryTokensCL = classifier.classify(text).get(0);
            for (CoreLabel coreLabel : queryTokensCL) {
                coreLabel.setNER(coreLabel.get(CoreAnnotations.AnswerAnnotation.class));
                System.out.println(coreLabel.originalText()+"\t"+coreLabel.get(CoreAnnotations.AnswerAnnotation.class));
            }
            System.exit(0);*/

            WSM wsm = new WSM();
            //List<Integer> ind = wsm.getTuples("Our acquisition led to know legal problems");
            //System.out.println(ind);
            List<String[]> y = wsm.getTuples(new ArrayList<>(Arrays.asList(text.split(" "))));
            for (String[] y1 : y) {
                for (String y2 : y1)
                    System.out.print(y2 + " ");
                System.out.println("");
            }

            //System.out.println(wsm.getTuples(text));
            //wsm.getTuples()

            System.exit(0);


            JWNLHelper jwnlHelper = new JWNLHelper();
            String[] temp = jwnlHelper.getSynsets("N", "meeting");
            for (String s : temp)
                System.out.println(s);
            POS pos = POS.NOUN;
            String term = "meeting";
            IndexWord iw = Dictionary.getInstance().lookupIndexWord(pos, term);
            ArrayList<String> synsets = new ArrayList<>();
            long[] x = iw.getSynsetOffsets();
            //iw.getSenses()[0].getWords()[0].getSynset().
            for (long l : x)
                System.out.println(l);


//            RiWordnet wordnet = new RiWordnet(null);
//            String[] pos1 = wordnet.getPos(term);
//            for (String s : pos1)
//                System.out.println(s);
//            int[] ids = wordnet.getSenseIds(term, pos1[1]);
//            for (int id : ids)
//                System.out.println(id);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
