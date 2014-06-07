package nlp;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import rita.wordnet.RiWordnet;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: OmerTanwirHassan
 * Date: 6/8/14
 */
public class WSM {
    public static void main(String[] args) throws JWNLException {
        JWNLHelper jwnlHelper = new JWNLHelper();
        String[] temp = jwnlHelper.getSynsets("N", "meeting");
        for (String s : temp)
            System.out.println(s);
        POS pos = POS.NOUN;
        String term = "meeting";
        IndexWord iw = Dictionary.getInstance().lookupIndexWord(pos, term);
        ArrayList<String> synsets = new ArrayList<>();
        long[] x = iw.getSynsetOffsets();
        for (long l : x)
            System.out.println(l);


        RiWordnet wordnet = new RiWordnet(null);
        String[] pos1 = wordnet.getPos(term);
        for (String s : pos1)
            System.out.println(s);
        int[] ids = wordnet.getSenseIds(term, pos1[1]);
        for (int id : ids)
            System.out.println(id);
    }

    List<CoreLabel> posTag(String question) {
        List<CoreLabel> cls = tokenizerFactory.getTokenizer(new StringReader(question)).tokenize();
        tagger.tagCoreLabels(cls);
        return cls;
    }

    List<Integer> findMatchingTerms(String sentenceStr, List<Integer> termsListSenseIds) {
        List<CoreLabel> sentence = posTag(sentenceStr);
        List<Integer> matchingTermsIndexes = new ArrayList<>();
        for (int i=0; i < sentence.size(); i++) {
            CoreLabel sentenceTerm = sentence.get(i);
            int[] questionTermSenseIds = wordnet.getSenseIds(sentenceTerm.word(), sentenceTerm.tag());
            for (int id : questionTermSenseIds)
                if (termsListSenseIds.contains(id))
                    matchingTermsIndexes.add(i);
        }
        return matchingTermsIndexes;
    }



    static RiWordnet wordnet;
    static MaxentTagger tagger;
    static TokenizerFactory<CoreLabel> tokenizerFactory;
    void initialize() {
        tagger = new MaxentTagger(
                "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
                "invertible=true");
        wordnet = new RiWordnet(null);
    }
    public WSM() {
        initialize();
    }
}
