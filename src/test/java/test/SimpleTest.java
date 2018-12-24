package test;

import cn.zlihj.domain.Staff;
import cn.zlihj.enums.WorkType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import java.io.File;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Iterator;

public class SimpleTest {
    private static File indexFile = new File("/Users/zhanglei53/index");

    static {
        if (!indexFile.exists()) {
            indexFile.mkdirs();
        }
    }

    @Test
    public void testJson() throws JsonProcessingException {
        Staff staff = new Staff();
        staff.setType(WorkType.AZ);
        System.out.println(new ObjectMapper().writeValueAsString(staff));
    }

    @Test
    public void testLucene() throws Exception {
        String[] arr = new String[]{"daf wadf", "had哈"};
        createIndex(arr);
        search("a");
    }

    private void createIndex(String[] arr) throws Exception {
        FSDirectory directory = FSDirectory.open(Paths.get("/Users/zhanglei53/index"));
        IndexWriterConfig config = new IndexWriterConfig();
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document document = null;
        for (String s : arr) {
            document = new Document();
            Field name = new Field("name", s, TextField.TYPE_STORED);
            document.add(name);
            indexWriter.addDocument(document);
        }
        indexWriter.close();
        directory.close();
    }

    private void search(String queryString) throws Exception {
        FSDirectory directory = FSDirectory.open(Paths.get("/Users/zhanglei53/index"));
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));

        QueryParser queryParser = new QueryParser("name", new StandardAnalyzer());
        Query query = queryParser.parse(queryString);
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println(topDocs.scoreDocs.length);
        for (ScoreDoc doc : topDocs.scoreDocs) {
            Document hit = indexSearcher.doc(doc.doc);
            System.out.println(hit.getField("name").stringValue());
        }
        directory.close();
    }

    @Test
    public void testNumberFormat() {
        double d = 1.8810151676E10;
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        System.out.println(d);
        System.out.println(numberFormat.format(d));
    }

    public static void main(String[] args) throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\"><plist version=\"1.0\"><dict>\t<key>IMEI</key>\t<string>35 699606 779422 1</string>\t<key>PRODUCT</key>\t<string>iPhone7,1</string>\t<key>UDID</key>\t<string>e7d7b1c194da9e6b87b06d91f68c305ece5ae1f2</string>\t<key>VERSION</key>\t<string>16A366</string></dict></plist>";
        org.dom4j.Document document = DocumentHelper.parseText(content);

        //获取根节点对象
        Element rootElement = document.getRootElement();
        Iterator iterator = rootElement.element("dict").elementIterator();

        while (iterator.hasNext()) {
            Element next = (Element) iterator.next();
            if (next.getStringValue().equalsIgnoreCase("UDID")) {
                System.out.println(((Element) iterator.next()).getStringValue());
            }
        }
    }
}
