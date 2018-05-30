package wiki.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;
import joptsimple.internal.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.obj.BeCompRelationResult;
import wiki.data.obj.LinkParenthesisPair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPageParser {
    private final static Logger LOGGER = LogManager.getLogger(WikiPageParser.class);

    private static final String DISAMBIGUATION_TITLE = "(disambiguation)";

    private static final String CAT_REGEX = "\\[\\[Category:(.*)\\]\\]";
    private static final Pattern CAT_PATTERN = Pattern.compile(CAT_REGEX);

    private static final String DIS_REGEX = "^\\{\\{(disambig.*|Disambig.*)\\}\\}$";
    private static final Pattern DIS_PATTERN = Pattern.compile(DIS_REGEX);

    private static final String PARENTHESIS_REGEX_1 = "\\[\\[(.*)\\]\\]";
    private static final Pattern PARENTHESIS_PATTERN_1 = Pattern.compile(PARENTHESIS_REGEX_1);

    private static final String PARENTHESIS_REGEX_2 = "(.*)\\s?\\((.*)\\)";
    private static final Pattern PARENTHESIS_PATTERN_2 = Pattern.compile(PARENTHESIS_REGEX_2);

    private static final String[] PART_NAME_CATEGORIES = {"name", "given name", "surname"};
    private static final String[] DISAMBIGUATION_CATEGORIES = {"disambig", "disambiguation"};

    private static final String URL_PATTERN = "(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";

    private static StanfordCoreNLP sPipeline;

    public static HashSet<String> STOP_WORDS;


    public static void initResources() {
        if(STOP_WORDS == null) {
            try (InputStream resource = WikiPageParser.class.getClassLoader().getResourceAsStream("stop_words_en.json")) {
                Type type = new TypeToken<HashSet<String>>() {
                }.getType();
                Gson gson = new Gson();
                STOP_WORDS = gson.fromJson(new InputStreamReader(resource), type);
            } catch (IOException e) {
                LOGGER.error("failed to load STOP_WORDS", e);
            }
        }

        if(sPipeline == null) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
            sPipeline = new StanfordCoreNLP(props);
        }
    }

    public static Set<String> extractCategories(String title, String line) {

        Set<String> categories = new HashSet<>();
        if (line != null && !line.isEmpty()) {
            Matcher catMatch = CAT_PATTERN.matcher(line);
            while (catMatch.find()) {
                String cat = catMatch.group(1);
                cat = trimDisambig(cat);
                cat = getTrimTextOnly(cat);
                if(!cat.equalsIgnoreCase(title)) {
                    categories.add(cat);
                }
            }

            Matcher disMatch = DIS_PATTERN.matcher(line);
            while (disMatch.find()) {
                String cat = disMatch.group(1);
                String[] cats = cat.split("\\|");
                categories.addAll(Arrays.asList(cats));
            }
        }
        return categories;
    }

    public static boolean isPartName(String line) {
        if (line != null && !line.isEmpty()) {
            line = line.toLowerCase();
            if (line.contains("===as surname==="))
                return true;
            else if (line.contains("===as given name==="))
                return true;
            else if (line.contains("===given names==="))
                return true;
            else if (line.contains("==as a surname=="))
                return true;
            else if (line.contains("==people with the surname=="))
                return true;
            else if (line.contains("==family name and surname=="))
                return true;
            else if (line.contains("category:given names"))
                return true;
        }
        return false;
    }

    public static boolean isPartNameInCategories(Collection<String> categories) {
        if (categories != null) {
            for (String cat : categories) {
                for (String partName : PART_NAME_CATEGORIES) {
                    if (cat.equalsIgnoreCase(partName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static LinkParenthesisPair extractLinksAndParenthesis(String line) {
        Set<String> parenthesis = new HashSet<>();
        Set<String> links = new HashSet<>();

        Matcher parenthesisMatcher1 = PARENTHESIS_PATTERN_1.matcher(line);
        while (parenthesisMatcher1.find()) {
            String inPar = parenthesisMatcher1.group(1);
            String[] splittedLine = inPar.split("\\|");
            for (String splLine : splittedLine) {
                Matcher parenthesisMatcher2 = PARENTHESIS_PATTERN_2.matcher(splLine);
                if(parenthesisMatcher2.matches()) {
                    String link = parenthesisMatcher2.group(1);
                    link = getTrimTextOnly(link);
                    links.add(link);
                    if (parenthesisMatcher2.group(2) != null) {
                        String parenth = parenthesisMatcher2.group(2);
                        if(!parenth.equalsIgnoreCase("disambiguation")) {
                            parenth = getTrimTextOnly(parenth);
                            parenthesis.add(parenth);
                        }
                    }
                } else {
                    splLine = getTrimTextOnly(splLine);
                    links.add(splLine);
                }
            }
        }
        return new LinkParenthesisPair(links, parenthesis);
    }

    public static String normalizeString(String toNorm) {
        List<String> wordsList = new ArrayList<>();
        String cleanText = getTrimTextOnly(toNorm).toLowerCase();
        String[] cleanTextSplit = cleanText.split(" ");
        for (String token : cleanTextSplit) {
            if (!token.isEmpty() && !STOP_WORDS.contains(token)) {
                String tokenLemma = new Sentence(token).lemma(0);
                wordsList.add(tokenLemma);
            }
        }

        return Strings.join(wordsList, " ");
    }

    public static Set<String> normalizeStringSet(Set<String> toNormList) {
        Set<String> retNormList = new HashSet<>();
        if (toNormList != null)
            for (String str : toNormList) {
                if (!str.isEmpty()) {
                    retNormList.add(normalizeString(str));
                }
            }

        return retNormList;
    }

    public static boolean isDisambiguation(Collection<String> categories) {
        if (categories != null) {
            for (String cat : categories) {
                for (String dis : DISAMBIGUATION_CATEGORIES) {
                    if (cat.equalsIgnoreCase(dis)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String extractFirstPageParagraph(String text) {
        String firstParagraph = "";
        int firstSentenceStartIndex = text.indexOf("'''");
        if(firstSentenceStartIndex >= 0) {
            int lastTempIndex = text.indexOf("\n", firstSentenceStartIndex);
            if (lastTempIndex == -1) {
                lastTempIndex = text.length();
            }

            firstParagraph = text.substring(firstSentenceStartIndex, lastTempIndex);
            if(extractBeAIndex(firstParagraph) == -1) {
                firstParagraph = extractFirstPageParagraph(text.substring(lastTempIndex));
            }
        }

        String firstParagraphRemJson = removeJsonTags(firstParagraph);
        String firstParagraphCleanLinks = normTextLinks(firstParagraphRemJson);
        String firstParagraphCleanUrl = cleanUrlPattern(firstParagraphCleanLinks);

        return firstParagraphCleanUrl;
    }

    public static BeCompRelationResult extractBeCompRelationFromFirstSentence(String firstSentence) {
        if(firstSentence.contains(".")) {
            firstSentence = firstSentence.substring(0, firstSentence.indexOf("."));
        }
        return extractBeCompRelation(firstSentence);
    }

    private static String cleanUrlPattern(String textToClean) {
        Pattern p = Pattern.compile(URL_PATTERN,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(textToClean);
        while (m.find()) {
            try {
                textToClean = textToClean.replaceAll(m.group(0), "").trim();
            } catch (Exception e) { }
        }
        return textToClean;
    }

    private static int extractBeAIndex(String sentence) {
        int result = -1;

        if(sentence.contains("is a")) {
            result = sentence.indexOf("is a");
        } else if(sentence.contains("are a")) {
            result = sentence.indexOf("are a");
        } else if(sentence.contains("was a")) {
            result = sentence.indexOf("was a");
        } else if(sentence.contains("were a")) {
            result = sentence.indexOf("were a");
        } else if(sentence.contains("be a")) {
            result = sentence.indexOf("be a");
        } else if(sentence.contains("is the")) {
            result = sentence.indexOf("is the");
        } else if(sentence.contains("are the")) {
            result = sentence.indexOf("are the");
        } else if(sentence.contains("was the")) {
            result = sentence.indexOf("was the");
        } else if(sentence.contains("were the")) {
            result = sentence.indexOf("were the");
        } else if(sentence.contains("be the")) {
            result = sentence.indexOf("be the");
        }

        return result;
    }

    // Remove Json, html and parenthesis blocks {{.*}}/<.*>/(.*) mostly hyper links
    private static String removeJsonTags(String text) {
        text = removeParenthesis("{{", "}}", text);
        text = removeParenthesis("<", ">", text);
        text = removeParenthesis("(", ")", text);
        text = text.replaceAll("'", "");
        text = text.replaceAll("&nbsp;", " ");

        return text;
    }

    private static String removeParenthesis(String parenthesisTypeBegin, String parenthesisTypeEnd, String text) {
        while(text.contains(parenthesisTypeBegin) || text.contains(parenthesisTypeEnd)) {
            StringBuffer buff = new StringBuffer(text);
            int end = buff.indexOf(parenthesisTypeEnd);
            int start;
            if(end != -1) {
                start = findInnerStartIndex(text.substring(0, end), -1, parenthesisTypeBegin);
                if(start == -1) {
                    return text;
                }
            } else {
                start = buff.indexOf(parenthesisTypeBegin);
                if(start != -1) {
                    end = buff.length() - parenthesisTypeEnd.length();
                }
            }

            if(start == -1 || end == -1) {
                return text;
            }

            buff = buff.delete(start, end + parenthesisTypeEnd.length());
            text = buff.toString();
        }

        return text;
    }

    private static int findInnerStartIndex(String text, int startIndex, String parenthesisTypeBegin) {
        int sIndex = text.indexOf(parenthesisTypeBegin);
        if(sIndex == -1) {
            return startIndex;
        }

        return findInnerStartIndex(text.substring(sIndex + parenthesisTypeBegin.length()), sIndex, parenthesisTypeBegin);
    }

    // Every thing in between [[.*]]
    private static String normTextLinks(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '[') {
                StringBuilder resultIn = new StringBuilder();
                while (i + 1 < text.length() && text.charAt(++i) != ']') {
                    if (text.charAt(i) == '[') {
                        continue;
                    }
                    else if (text.charAt(i) == '|') {
                        resultIn = new StringBuilder(); // clean (multi separated link)
                        continue;
                    }
                    resultIn.append(text.charAt(i));
                }
                result.append(resultIn);
            } else {
                if (text.charAt(i) != ']') {
                    result.append(text.charAt(i));
                }
            }
        }

        return result.toString();
    }

    private static BeCompRelationResult extractBeCompRelation(String text) {
        BeCompRelationResult beCompRel = new BeCompRelationResult();
        if(text != null && !text.isEmpty()) {
            CoreDocument document = new CoreDocument(text);
            sPipeline.annotate(document);
            CoreSentence sentence = document.sentences().get(0);
            Stack<SemanticGraphEdge> semStack = getEdgesStack(sentence.dependencyParse().edgeListSorted());
            while(!semStack.empty()) {
                final SemanticGraphEdge edge = semStack.pop();
                String govValue = edge.getGovernor().get(CoreAnnotations.ValueAnnotation.class);
                String govValueNorm = cleanValue(edge.getGovernor().get(CoreAnnotations.LemmaAnnotation.class));
                String relation = edge.getRelation().getShortName();
                String tarValue = edge.getDependent().get(CoreAnnotations.ValueAnnotation.class);
                String tarValueNorm = cleanValue(edge.getDependent().get(CoreAnnotations.LemmaAnnotation.class));
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

    public static String cleanValue(String value) {
        String cleanText = null;
        if(value != null && !value.isEmpty() && !STOP_WORDS.contains(value.toLowerCase())) {
            cleanText = getTrimTextOnly(value).toLowerCase();
        }

        return cleanText;
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

    private static LinkedList<IndexedWord> extractAmodRel(SemanticGraphEdge edge, Stack<SemanticGraphEdge> semStack) {
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

    private static Stack<SemanticGraphEdge> getEdgesStack(List<SemanticGraphEdge> edgeList) {
        Stack<SemanticGraphEdge> edgeStack = new Stack<>();
        for(int i = edgeList.size() - 1 ; i >= 0 ; i--) {
            edgeStack.push(edgeList.get(i));
        }

        return edgeStack;
    }

    private static String trimDisambig(String cat) {
        if (cat.toLowerCase().contains(DISAMBIGUATION_TITLE)) {
            cat = cat.replace(DISAMBIGUATION_TITLE, "");
        }
        return cat;
    }

    private static String getTrimTextOnly(String text) {
        return text.replaceAll("[^a-zA-Z0-9]", " ").trim();
    }
}
