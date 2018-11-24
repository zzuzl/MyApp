package test;

import cn.zlihj.domain.Staff;
import cn.zlihj.enums.WorkType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

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
        search("daf");
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

        TermQuery query = new TermQuery(new Term("name", queryString));
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println(topDocs.scoreDocs.length);
        for (ScoreDoc doc : topDocs.scoreDocs) {
            Document hit = indexSearcher.doc(doc.doc);
            System.out.println(hit.getField("name").stringValue());
        }
        directory.close();
    }
}
