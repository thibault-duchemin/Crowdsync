package nlp;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.list.PointerTargetTreeNode;
import net.didion.jwnl.dictionary.Dictionary;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class JWNLHelper {

    private MaxentTagger tagger;

    public JWNLHelper() {
        tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        try {
            JWNL.initialize(JWNLConfig.getInputStream());
        } catch (JWNLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public List<String> tagSentence(String question){
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
                "invertible=true");
        List<CoreLabel> cls = tokenizerFactory.getTokenizer(new StringReader(question)).tokenize();
        tagger.tagCoreLabels(cls);
        List<String> POSTokens = new ArrayList<>();
        for (CoreLabel cl : cls)
            POSTokens.add(cl.tag());
        return POSTokens;
    }

    public List<CoreLabel> tagQuestion(String question){
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
                "invertible=true");
        List<CoreLabel> cls = tokenizerFactory.getTokenizer(new StringReader(question)).tokenize();
        tagger.tagCoreLabels(cls);
        return cls;
    }

    public String tagTerm(String term){
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
                "invertible=true");
        List<CoreLabel> cls = tokenizerFactory.getTokenizer(new StringReader(term)).tokenize();
        tagger.tagCoreLabels(cls);
        String POSTokens = null;
        for (CoreLabel cl : cls)
            POSTokens = cl.tag();
        return POSTokens;
    }

    public String[] getSynsets(String posStr, String term) throws JWNLException{
        POS pos = POS.NOUN;
        if(posStr.charAt(0) == 'N'){
            pos = POS.NOUN;
        }  else if(posStr.charAt(0) == 'V'){
            pos = POS.VERB;
        } else if(posStr.charAt(0) == 'R'){
            pos = POS.ADVERB;
        } else if(posStr.charAt(0) == 'J'){
            pos = POS.ADJECTIVE;
        }
        IndexWord iw = Dictionary.getInstance().lookupIndexWord(pos, term);
        ArrayList<String> synsets = new ArrayList<>();
        if(iw != null){
            for(Synset synset: iw.getSenses()){
                Word[] words = synset.getWords();
                for(Word word: words){
                    synsets.add(word.getLemma().replace("_", " "));
                }
            }
        }
        return synsets.toArray(new String[synsets.size()]);
    }

    public ArrayList<String> getHypernyms(String posStr, String term) throws JWNLException{
        POS pos = POS.NOUN;
        if(posStr.charAt(0) == 'N'){
            pos = POS.NOUN;
        }  else if(posStr.charAt(0) == 'V'){
            pos = POS.VERB;
        } else if(posStr.charAt(0) == 'R'){
            pos = POS.ADVERB;
        } else if(posStr.charAt(0) == 'J'){
            pos = POS.ADJECTIVE;
        }
        IndexWord iw = Dictionary.getInstance().lookupIndexWord(pos, term);

        ArrayList<String> hypernym = new ArrayList<>();
        if(iw != null){
            for(Synset synset: iw.getSenses()){
                PointerTargetTree pointerTargetTree = PointerUtils.getInstance().getHypernymTree(synset);
                PointerTargetTreeNode rootNode = pointerTargetTree.getRootNode();
                PointerTargetTreeNode[] nodes = pointerTargetTree.findAll(rootNode);
                for(PointerTargetTreeNode node : nodes){
                    Word[] words = node.getSynset().getWords();
                    for(Word word: words){
                        hypernym.add(word.getLemma().replace("_", " "));
                    }
                }
            }
        }

        return hypernym;
    }

    public String[] getSynonyms(String posStr, String term) throws JWNLException{
        POS pos = POS.NOUN;
        if(posStr.charAt(0) == 'N'){
            pos = POS.NOUN;
        }  else if(posStr.charAt(0) == 'V'){
            pos = POS.VERB;
        } else if(posStr.charAt(0) == 'R'){
            pos = POS.ADVERB;
        } else if(posStr.charAt(0) == 'J'){
            pos = POS.ADJECTIVE;
        }
        IndexWord iw = Dictionary.getInstance().lookupIndexWord(pos, term);

        ArrayList<String> synonym = new ArrayList<>();
        if(iw != null){
            for(Synset synset: iw.getSenses()){
                PointerTargetTree pointerTargetTree = PointerUtils.getInstance().getSynonymTree(synset, 5);
                PointerTargetTreeNode rootNode = pointerTargetTree.getRootNode();
                PointerTargetTreeNode[] nodes = pointerTargetTree.findAll(rootNode);
                for(PointerTargetTreeNode node : nodes){
                    Word[] words = node.getSynset().getWords();
                    for(Word word: words){
                        synonym.add(word.getLemma().replace("_", " "));
                    }
                }
            }
        }

        return synonym.toArray(new String[synonym.size()]);
    }

    public ArrayList<String> getMeronyms(String posStr, String term) throws JWNLException{
        POS pos = POS.NOUN;
        if(posStr.charAt(0) == 'N'){
            pos = POS.NOUN;
        }  else if(posStr.charAt(0) == 'V'){
            pos = POS.VERB;
        } else if(posStr.charAt(0) == 'R'){
            pos = POS.ADVERB;
        } else if(posStr.charAt(0) == 'J'){
            pos = POS.ADJECTIVE;
        }
        IndexWord iw = Dictionary.getInstance().lookupIndexWord(pos, term);

        ArrayList<String> meronym = new ArrayList<>();
        if(iw != null){
            for(Synset synset: iw.getSenses()){
                PointerTargetNodeList pointerTargetNodeList = PointerUtils.getInstance().getMeronyms(synset);
                for(int i=0; i<pointerTargetNodeList.size(); i++){
                    if(pointerTargetNodeList.get(i) instanceof PointerTargetNode){
                        PointerTargetNode pointerTargetNode = (PointerTargetNode) pointerTargetNodeList.get(i);
                        Word[] words = pointerTargetNode.getSynset().getWords();
                        for(Word word: words){
                            meronym.add(word.getLemma().replace("_", " "));
                        }
                    }
                }
            }
        }
        return meronym;
    }

    public static void main(String[] args) {
        JWNLHelper jwnlHelper = new JWNLHelper();
        String pos = jwnlHelper.tagTerm("waving");

        try {
            String[] temp = jwnlHelper.getSynsets(pos, "waving");
            ArrayList<String> temp1 = jwnlHelper.getHypernyms(pos, "waving");
            String[] temp2 = jwnlHelper.getSynonyms(pos, "waving");
            ArrayList<String> temp3 = jwnlHelper.getMeronyms("n", "human");
        } catch (JWNLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
