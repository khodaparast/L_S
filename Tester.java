package luceneProject;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;
import java.util.ArrayList;


public class Tester {
    Searcher searcher = new Searcher();

    public Tester() throws IOException {
    }

    public static void main(String[] args) throws IOException, ParseException {
        Tester test = new Tester();

//        test.searchQuestionBodyByTerm("conver int string java");
//        test.searchQuestionBodyByQueryDateRange("android java",2008,12,1,2015,10,3);

//        ArrayList<String> al = new ArrayList<>();
//        al.add("java");
//        al.add("Windows");
//        test.searchQuestionByTag("convert int string",al);

//        test.searchAnswerBodyByQuestionTermAndAcceptedFlag("c java",true);           // <----original   done!
        test.getBestAnswer("4105331");
    }

    // ------------------------------------------------1. --------------------------------------------------------------
    public ArrayList<String> searchQuestionBodyByTerm(String input) throws IOException, ParseException {

        searcher.searchQuestionBodyByTerm(input);
        return null;
    }

    //------------------------------------------------ 2. --------------------------------------------------------------
    public ArrayList<String> searchQuestionBodyByQueryDateRange(String query, int startYear, int startMonth, int startDay,
                                                                int endYear, int endMonth, int endDay) throws IOException, ParseException {

        System.out.println("query in tester: "+query);
        searcher.searchQuestionBodyByQueryDateRange(query, startYear, startMonth, startDay, endYear, endMonth, endDay);

        //This function should return all question ids with given query in their bodies which asked
        // between startYear/startMonth/startDay and endYear/endMonth/endDay
        return null;
    }

    // ------------------------------------------------3. --------------------------------------------------------------
    public ArrayList<String> searchQuestionByTag(String query, ArrayList<String> tags) throws IOException, ParseException {

        searcher.searchQuestionByTag(query, tags);


        //This function should return all question ids with given query in their bodies which are associated with given tags
        return null;

    }

    //------------------------------------------------ 4. --------------------------------------------------------------
    public ArrayList<String> searchAnswerBodyByQuestionTermAndAcceptedFlag(String questionTerm, Boolean accpeted) throws IOException, ParseException {

        searcher.searchAnswerBodyByQuestionTermAndAcceptedFlag(questionTerm, accpeted);
        //Return all answers for all questions which have given question terms in their bodies according to the accepted flag
        return null;
    }

    //------------------------------------------------ 5. --------------------------------------------------------------
    public String getBestAnswer(String questionId) throws IOException, ParseException {

        searcher.getBestAnswer(questionId);

        //return the answer id which have the highest number of votes associated with given question id
        return null;
    }
}
