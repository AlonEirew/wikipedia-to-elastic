package wiki.data.relations;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import wiki.data.obj.BeCompRelationResult;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

public class BeCompRelationExtractor implements IRelationsExtractor<BeCompRelationResult> {

    private static StanfordCoreNLP sPipeline;

    public static void initResources(LangConfiguration langConfig) {
        String lang = langConfig.getCoreNlpLang();
        if(sPipeline == null && lang != null) {
            if(lang.equals("english")) {
                Properties props = new Properties();
                props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
                sPipeline = new StanfordCoreNLP(props);
            } else {
                sPipeline = new StanfordCoreNLP(lang);
            }
        }
    }

    @Override
    public BeCompRelationResult extract(String firstSentence) {
        if(firstSentence.contains(".")) {
            firstSentence = firstSentence.substring(0, firstSentence.indexOf("."));
        }
        return extractBeCompRelation(firstSentence);
    }

    private BeCompRelationResult extractBeCompRelation(String text) {
        BeCompRelationResult beCompRel = new BeCompRelationResult();
        if(text != null && !text.isEmpty()) {
            CoreDocument document = new CoreDocument(text);
            sPipeline.annotate(document);
            CoreSentence sentence = document.sentences().get(0);
            Stack<SemanticGraphEdge> semStack = this.getEdgesStack(sentence.dependencyParse().edgeListSorted());
            while(!semStack.empty()) {
                final SemanticGraphEdge edge = semStack.pop();
                String govValue = edge.getGovernor().get(CoreAnnotations.ValueAnnotation.class);
                String govValueNorm = WikiPageParser.cleanValue(edge.getGovernor().get(CoreAnnotations.LemmaAnnotation.class));
                String relation = edge.getRelation().getShortName();
                String tarValue = edge.getDependent().get(CoreAnnotations.ValueAnnotation.class);
                String tarValueNorm = WikiPageParser.cleanValue(edge.getDependent().get(CoreAnnotations.LemmaAnnotation.class));
                if (relation.equalsIgnoreCase("acl")) {
                    break;
                } else if (relation.equalsIgnoreCase("cop")) {
                    beCompRel.addBeCompRelation(govValue);
                    beCompRel.addBeCompRelationsNorm(govValueNorm);
                } else if (relation.equalsIgnoreCase("nsubj")) {
                    beCompRel.addBeCompRelation(tarValue);
                    beCompRel.addBeCompRelationsNorm(tarValueNorm);
                } else if (relation.equalsIgnoreCase("dep")) {
                    beCompRel.addBeCompRelation(govValue);
                    beCompRel.addBeCompRelationsNorm(govValueNorm);
                } else if (relation.equalsIgnoreCase("compound")) {
                    beCompRel.addBeCompRelation(tarValue + " " + govValue);
                    beCompRel.addBeCompRelationsNorm(tarValueNorm + " " + govValueNorm);
                    final LinkedList<IndexedWord> amodRel = extractAmodRel(edge, semStack);
                    beCompRel.addBeCompRelationList(amodRel);
                    beCompRel.addBeCompRelationListNorm(amodRel);
                } else if (relation.equalsIgnoreCase("amod")) {
                    final LinkedList<IndexedWord> amodRel = extractAmodRel(edge, semStack);
                    beCompRel.addBeCompRelationList(amodRel);
                    beCompRel.addBeCompRelationListNorm(amodRel);
                } else if (relation.equalsIgnoreCase("conj") || relation.equalsIgnoreCase("appos")) {
                    beCompRel.addBeCompRelation(tarValue);
                    beCompRel.addBeCompRelationsNorm(tarValueNorm);
                }
            }
        }
        return beCompRel;
    }

    private Stack<SemanticGraphEdge> getEdgesStack(List<SemanticGraphEdge> edgeList) {
        Stack<SemanticGraphEdge> edgeStack = new Stack<>();
        for(int i = edgeList.size() - 1 ; i >= 0 ; i--) {
            edgeStack.push(edgeList.get(i));
        }

        return edgeStack;
    }

    private LinkedList<IndexedWord> extractAmodRel(SemanticGraphEdge edge, Stack<SemanticGraphEdge> semStack) {
        LinkedList<IndexedWord> resultList = new LinkedList<>();
        IndexedWord localGov = edge.getGovernor();
        resultList.add(edge.getDependent());
        if(!semStack.empty()) {
            SemanticGraphEdge peek = semStack.peek();
            while (peek.getGovernor().get(CoreAnnotations.ValueAnnotation.class).equals(localGov.get(CoreAnnotations.ValueAnnotation.class))
                    && peek.getRelation().getShortName().equals("amod")) {
                resultList.addFirst(peek.getDependent());
                semStack.pop();
                if(semStack.empty()) {
                    break;
                }
                peek = semStack.peek();
            }

            if(peek.getGovernor().get(CoreAnnotations.ValueAnnotation.class).equals(localGov.get(CoreAnnotations.ValueAnnotation.class))
                    && peek.getRelation().getShortName().equals("compound")) {
                resultList.addLast(peek.getDependent());
            }
        }

        resultList.addLast(localGov);
        return resultList;
    }
}

//    private static Set<String> extractBeCompRelation(String text) {
//        Set<String> beCompRel = new HashSet<>();
//        Annotation document = new Annotation(text);
//        try {
//            sPipeline.annotate(document);
//            CoreMap sentence = document.get(CoreAnnotations.SentencesAnnotation.class).get(0);
//            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
//            for (RelationTriple triple : triples) {
//                System.out.println(triple.confidence + "\t" +
//                        triple.subjectLemmaGloss() + "\t" +
//                        triple.relationLemmaGloss() + "\t" +
//                        triple.objectLemmaGloss());
//
//                if (triple.relationLemmaGloss().equalsIgnoreCase("be")) {
//                    beCompRel.add(triple.subjectLemmaGloss());
//                    beCompRel.add(triple.objectLemmaGloss());
//                }
//            }
//        } catch (AssertionError e) {}
//        return beCompRel;
//    }