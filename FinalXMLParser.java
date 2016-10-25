package luceneProject;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

public class FinalXMLParser {
    static BodyFix fix = new BodyFix();

    public static void main(String[] args) throws XMLStreamException, IOException {
        //Parse XML
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in = new FileInputStream("samplePosts.xml");
        XMLStreamReader parser = inputFactory.createXMLStreamReader(in);
        parser.nextTag();

        //write id and body to output
        FileOutputStream fileOutputStream = new FileOutputStream("samplePosts.txt", true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        outputStreamWriter.write("Id,CreationDate,Tags,Body "+"\n");


        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals("row")) {
                    String id = parser.getAttributeValue(null, "Id");
                    String date = parser.getAttributeValue(null, "CreationDate");
                    String tags = parser.getAttributeValue(null, "Tags");

                    String postTypeId = parser.getAttributeValue(null, "PostTypeId");
                    System.out.println("PostTypeId : "+postTypeId);
                    if (Long.parseLong(postTypeId) == 1) {

                        String body = parser.getAttributeValue(null, "Body");
                        tags=fix.cleanTags(tags);
                        body = fix.cleanBody(body);

                        System.out.println(id );
                        outputStreamWriter.append(id+","+date+","+tags+","+ body + "\n");
                    }
                }
            }



        }
    }
}
