package luceneProject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
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
    public IndexReader indexReader = IndexReader.open(FSDirectory.open(INDEX_SAMPLE_DIRECTORY), true);
    public IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    public Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

    //------------------------------------------------- 1. ----------------------------------------------------
    public ArrayList<String> searchQuestionBodyByTerm(String term) throws IOException, ParseException {

        Term t = new Term("Body", term);
        Query keywordQuery = new TermQuery(t);

        System.out.println("Query :  " + keywordQuery);
        TopDocs hits = indexSearcher.search(keywordQuery, Integer.MAX_VALUE); // run the query
        System.out.println("Query Results found by term : " + hits.totalHits);
        for (int i = 0; i < hits.totalHits; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);//get the next  document
//            System.out.println("ID :" + doc.get("Id"));
            resultList.add(doc.get("Id"));
        }
        return resultList;
    }

    //--------------------------------------------------- 2. ----------------------------------------------------------
    public ArrayList<String> searchQuestionBodyByQueryDateRange(String query, int startYear, int startMonth, int startDay,
                                                                int endYear, int endMonth, int endDay) throws IOException, ParseException {

        String fixQ[] = query.split(" ");
        String Q = "";
        for (int i = 0; i < fixQ.length; i++) {
            fixQ[i] = " +" + fixQ[i];
            Q = Q + fixQ[i];
        }
        HashMap fixedDate = DateFix.fixSearchDate(startMonth, startDay, endMonth, endDay);
        String start = "" + startYear + fixedDate.get("startMonth") + fixedDate.get("startDay");
        String end = "" + endYear + fixedDate.get("endMonth") + fixedDate.get("endDay");
        System.out.println("startDate : " + start);
        System.out.println("endDate : " + end);
        int startDate = Integer.parseInt(start);
        int endDate = Integer.parseInt(end);

        System.out.println("Search : " + Q + " from : " + startDate + " TO " + endDate);

        // body query
        QueryParser bodyQP = new QueryParser(Version.LUCENE_30, "Body", analyzer);
        Query bodyQuery = bodyQP.parse(Q);

        TopDocs hits = indexSearcher.search(bodyQuery, Integer.MAX_VALUE);
        System.out.println("Match Document/s : " + hits.totalHits);
        for (int i = 0; i < hits.totalHits; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);
            int docdate = Integer.parseInt(DateFix.fixSearchedDateResult(doc.get("CreationDate")));

            if (docdate >= startDate && docdate <= endDate) {
                System.out.println(" ID :" + doc.get("Id") + " docDate : " + docdate);

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
        Query cityQuery = bodyQP.parse(keyword);
// tag query
        QueryParser tagsQP = new QueryParser(Version.LUCENE_30, "Tags", analyzer);
        Query tagsQuery = tagsQP.parse(strTag);
// final query
        BooleanQuery finalQuery = new BooleanQuery();
        finalQuery.add(cityQuery, BooleanClause.Occur.MUST);
        finalQuery.add(tagsQuery, BooleanClause.Occur.MUST);

        TopDocs hits = indexSearcher.search(finalQuery, Integer.MAX_VALUE);
        System.out.println("Query Results found >> " + hits.totalHits);
        for (int i = 0; i < hits.totalHits; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);//get the next  document
            System.out.println(i + "  " + doc.get("Id") + " " + doc.get("Tags") + " " + doc.get("Body"));
            resultList.add(doc.get("Id"));
        }
        return resultList;
    }

    //--------------------------------------------- 4. ---------------------------------------------
    public ArrayList<String> searchAnswerBodyByQuestionTermAndAcceptedFlag(String questionTerm, Boolean accpeted) throws IOException, ParseException {

        IndexReader indexReader2 = IndexReader.open(FSDirectory.open(INDEX_Q_A_DIRECTORY), true);
        IndexSearcher indexSearcherQA = new IndexSearcher(indexReader2);

        ArrayList<String> idsWithAcceptedAnswerList = new ArrayList<>();

        QueryParser questionTermQuery = new QueryParser(Version.LUCENE_30, "Body", analyzer);
        Query qTerm = questionTermQuery.parse(questionTerm);
        TopDocs hitBody = indexSearcher.search(qTerm, Integer.MAX_VALUE);
        for (int i = 0; i < hitBody.totalHits; i++) {
            Document doc = indexSearcher.doc(hitBody.scoreDocs[i].doc);
            String id = doc.get("Id");

            QueryParser qp = new QueryParser(Version.LUCENE_30, "QId", analyzer);
            Query query = qp.parse(id);
            TopDocs hits = indexSearcherQA.search(query, Integer.MAX_VALUE);

            for (int j = 0; j < hits.totalHits; j++) {
                Document document = indexSearcherQA.doc(hits.scoreDocs[j].doc);
                idsWithAcceptedAnswerList.add(document.get("AId"));
                System.out.println("QId : " + document.get("QId") + " " + " AId : " + document.get("AId") + " Accepted : " + document.get("accepted"));

            }


        }
        return idsWithAcceptedAnswerList;
    }

    //------------------------------------------ 5. ----------------------------------------------------------
    public String getBestAnswer(String questionId) throws IOException, ParseException {
        System.out.println("Question ID :"+questionId);
        IndexReader QAReader = IndexReader.open(FSDirectory.open(new File("Q_ADir")), true);
        IndexSearcher QASearcher = new IndexSearcher(QAReader);

        QueryParser QAParser = new QueryParser(Version.LUCENE_30, "QId", analyzer);
        Query query = QAParser.parse(questionId);
        TopDocs hits = QASearcher.search(query, Integer.MAX_VALUE);

        System.out.println("Number of answers >>>  "+hits.totalHits);
        int maxVote = 0;
        String Id = "";
        for (int i = 0; i < hits.totalHits; i++) {
            Document doc = QASearcher.doc(hits.scoreDocs[i].doc);//get the next  document
            System.out.println("AId : " + doc.get("AId")+" Score : "+answerScore(doc.get("AId")));

            if( answerScore(doc.get("AId")) > maxVote){
                maxVote=answerScore(doc.get("AId"));
                Id=doc.get("AId");
            }
        }
        System.out.println("Answer Id With Max Score : "+Id);
        System.out.println("Score : "+ maxVote);
        return Id;
}

    //--------------------------------------------------------1.2. ---------------------------------------------------
    public ArrayList<String> searchQuestionBodyByTerm2(String term) throws IOException, ParseException {

        String fixedTerm = "";
        String[] newTerm = term.split(" ");
        for (int i = 0; i < newTerm.length; i++) {
            newTerm[i] = "+" + newTerm[i];
            fixedTerm = fixedTerm + " " + newTerm[i] + " ";

        }
        System.out.println("Fixed Queyr : " + fixedTerm);

        QueryParser qp = new QueryParser(Version.LUCENE_34, "Body", analyzer);
        Query keywordQuery = qp.parse(fixedTerm);

        System.out.println("Query :  " + keywordQuery);
        TopDocs hits = indexSearcher.search(keywordQuery, Integer.MAX_VALUE); // run the query
        System.out.println("Query Results found : " + hits.totalHits);
        for (int i = 0; i < hits.totalHits; i++) {
            Document doc = indexSearcher.doc(hits.scoreDocs[i].doc);//get the next  document
            System.out.println("ID :" + doc.get("Id"));
            resultList.add(doc.get("Id"));
        }


        return resultList;
    }

//----------------------------------------------------------------------------------------------
    public  int answerScore(String AId) throws IOException, ParseException {

        IndexReader answerReader= IndexReader.open(FSDirectory.open(new File("A_Dir")), true);
        IndexSearcher answerSearcher = new IndexSearcher(answerReader);
        QueryParser AnswerParser=new QueryParser(Version.LUCENE_30,"Id",analyzer);
        Query q=AnswerParser.parse(AId);
        TopDocs MaxScoreHit=answerSearcher.search(q,Integer.MAX_VALUE);
        int maxVote=0;
        String Id="";
        for (int j=0;j<MaxScoreHit.totalHits;j++){
            Document document=answerSearcher.doc(MaxScoreHit.scoreDocs[j].doc);

            if (Integer.parseInt(document.get("Score"))>maxVote ){
                maxVote=Integer.parseInt(document.get("Score"));
                Id=document.get("Id");
            }
        }
        return maxVote;
    }


}
