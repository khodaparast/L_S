package luceneProject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by "P.Khodaparast" on 10/18/2016.
 */
public class Searcher {
    public Searcher() throws IOException {
    }

    public static final File INDEX_BODY_DIRECTORY = new File("IndexQuestionBodyDir");
    public static final File INDEX_SAMPLE_DIRECTORY = new File("samplePostsDir");
    public static final File INDEX_Q_A_DIRECTORY = new File("Q_ADir");
    public static final File INDEX_A_DIRECTORY = new File("A_Dir");
    public static ArrayList<String> resultList = new ArrayList<>();
    //Searching
    public IndexReader indexReader = IndexReader.open(FSDirectory.open(INDEX_BODY_DIRECTORY), true);
    public IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    public Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

    //------------------------------------------------- 1. ----------------------------------------------------

    public ArrayList<String> searchQuestionBodyByTerm(String term) throws IOException, ParseException {

        QueryParser qp = new QueryParser(Version.LUCENE_34, "Body", analyzer);
        Query keywordQuery = qp.parse(term);

        System.out.println("Query :  " + keywordQuery);
        long startTime = System.currentTimeMillis();
        TopDocs hits = indexSearcher.search(keywordQuery, 100); // run the query
        long endTime = System.currentTimeMillis();

        System.out.println("Query Results found : " + hits.totalHits);
        System.out.println("Time : " + (endTime - startTime) + " ms" + "\n");
        for (int i = 0; i < 100; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);//get the next  document
            System.out.println("ID :" + doc.get("Id"));
            resultList.add(doc.get("Id"));
        }

        return resultList;
    }

    //--------------------------------------------------- 2. ----------------------------------------------------------
    public ArrayList<String> searchQuestionBodyByQueryDateRange(String query, int startYear, int startMonth, int startDay,
                                                                int endYear, int endMonth, int endDay) throws IOException, ParseException {


        HashMap fixedDate = DateFix.fixSearchDate(startMonth, startDay, endMonth, endDay);
        String start = "" + startYear + fixedDate.get("startMonth") + fixedDate.get("startDay");
        String end = "" + endYear + fixedDate.get("endMonth") + fixedDate.get("endDay");


        System.out.println("Search Query : " + query + "   from : " + start + " TO " + end);

        // body query
        QueryParser bodyQP = new QueryParser(Version.LUCENE_30, "Body", analyzer);
        Query bodyQuery = bodyQP.parse(query);

        long startTime = System.currentTimeMillis();
        TopDocs hits = indexSearcher.search(bodyQuery, Integer.MAX_VALUE);
        long endTime = System.currentTimeMillis();
        System.out.println("Time : " + (endTime - startTime) + " ms" + "\n");

        System.out.println("Match Document/s : " + hits.totalHits + "\n");
        for (int i = 0; i < 100; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);
            int docdate = Integer.parseInt(DateFix.fixSearchedDateResult(doc.get("CreationDate")));

            if (docdate >= Integer.parseInt(start) && docdate <= Integer.parseInt(end)) {
                System.out.println(" ID : " + doc.get("Id") + "     docCreationDate : " + docdate);
                resultList.add(doc.get("Id"));
            }
        }
        return resultList;
    }

    //--------------------------------------------- 3. ----------------------------------------------------------
    public ArrayList<String> searchQuestionByTag(String keyword, ArrayList<String> tags) throws ParseException, IOException {
        ArrayList<String> tagList = tags;
        String strTag = "";
        for (int i = 0; i < tagList.size(); i++) {
            strTag += " " + "+" + tagList.get(i) + " ";
        }
        System.out.println("Query : " + keyword);
        System.out.println("TagsList : " + strTag);
        System.out.println("Seaching...");
// body query
        QueryParser bodyQP = new QueryParser(Version.LUCENE_30, "Body", analyzer);
        //4105331
        Query cityQuery = bodyQP.parse(keyword);
// tag query
        QueryParser tagsQP = new QueryParser(Version.LUCENE_30, "Tags", analyzer);
        Query tagsQuery = tagsQP.parse(strTag);
// final query
        BooleanQuery finalQuery = new BooleanQuery();
        finalQuery.add(cityQuery, BooleanClause.Occur.MUST);
        finalQuery.add(tagsQuery, BooleanClause.Occur.MUST);

        long startTime = System.currentTimeMillis();
        TopDocs hits = indexSearcher.search(finalQuery, Integer.MAX_VALUE);
        long endTime = System.currentTimeMillis();
        System.out.println("Time : " + (endTime - startTime) + " ms" + "\n");

        System.out.println("Query Results found >> " + hits.totalHits);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);//get the next  document
            System.out.println(i + "  " + doc.get("Id"));
            resultList.add(doc.get("Id"));
        }
        return resultList;
    }

    //--------------------------------------------- 4. ---------------------------------------------
    public ArrayList<String> searchAnswerBodyByQuestionTermAndAcceptedFlag(String questionTerm, Boolean accpeted) throws IOException, ParseException {

        IndexReader indexReader2 = IndexReader.open(FSDirectory.open(INDEX_Q_A_DIRECTORY), true);
        IndexSearcher indexSearcherQA = new IndexSearcher(indexReader2);

        QueryParser questionTermQuery = new QueryParser(Version.LUCENE_30, "Body", analyzer);
        Query qTerm = questionTermQuery.parse(questionTerm);
        QueryParser qp = new QueryParser(Version.LUCENE_30, "QId", analyzer);
        if (accpeted.equals(true)) {
            TopDocs hitBody = indexSearcher.search(qTerm, Integer.MAX_VALUE);
            System.out.println("Questions including \'"+questionTerm+"\' : "+hitBody.totalHits);

            for (int i = 0; i < 100; i++) {
                Document doc = indexSearcher.doc(hitBody.scoreDocs[i].doc);
                String id = doc.get("Id");
                Query queryId = qp.parse(id);
                TopDocs hits = indexSearcherQA.search(queryId, Integer.MAX_VALUE);
                for (int j = 0; j < hits.totalHits; j++) {
                    Document document = indexSearcherQA.doc(hits.scoreDocs[j].doc);
                    if(document.get("accepted").equals("1")) {
                        resultList.add(document.get("AId"));
                        System.out.println("QId : " + document.get("QId") + " " + "     AId : " + document.get("AId") + "    Accepted : " + document.get("accepted"));
                    }
                }
            }
        }
        else {
            Searcher s=new Searcher();
            resultList=s.searchQuestionBodyByTerm(questionTerm);
        }
        return resultList;
    }
//------------------------------------------ 5. ----------------------------------------------------------
    public String getBestAnswer(String questionId) throws IOException, ParseException {
        System.out.println("Question ID :" + questionId);
        IndexReader QAReader = IndexReader.open(FSDirectory.open(new File("Q_ADir")), true);
        IndexSearcher QASearcher = new IndexSearcher(QAReader);

        QueryParser QAParser = new QueryParser(Version.LUCENE_30, "QId", analyzer);
        Query query = QAParser.parse(questionId);
        TopDocs hits = QASearcher.search(query, Integer.MAX_VALUE);

        System.out.println("Number of answers >>>  " + hits.totalHits);
        int maxVote = 0;
        String Id = "";
        for (int i = 0; i < hits.totalHits; i++) {
            Document doc = QASearcher.doc(hits.scoreDocs[i].doc);//get the next  document
            System.out.println("AId : " + doc.get("AId") + "      Score : " + answerScore(doc.get("AId")));

            if (answerScore(doc.get("AId")) > maxVote) {
                maxVote = answerScore(doc.get("AId"));
                Id = doc.get("AId");
            }
        }
        System.out.println("Answer Id With Max Score : " + Id);
        System.out.println("Score : " + maxVote);
        return Id;
    }

    //----------------------------------------------------------------------------------------------
    public int answerScore(String AId) throws IOException, ParseException {

        IndexReader answerReader = IndexReader.open(FSDirectory.open(new File("A_Dir")), true);
        IndexSearcher answerSearcher = new IndexSearcher(answerReader);
        QueryParser AnswerParser = new QueryParser(Version.LUCENE_30, "Id", analyzer);
        Query q = AnswerParser.parse(AId);
        TopDocs MaxScoreHit = answerSearcher.search(q, Integer.MAX_VALUE);
        int maxVote = 0;
        String Id = "";
        for (int j = 0; j < MaxScoreHit.totalHits; j++) {
            Document document = answerSearcher.doc(MaxScoreHit.scoreDocs[j].doc);

            if (Integer.parseInt(document.get("Score")) > maxVote) {
                maxVote = Integer.parseInt(document.get("Score"));
                Id = document.get("Id");
            }
        }
        return maxVote;
    }


}
