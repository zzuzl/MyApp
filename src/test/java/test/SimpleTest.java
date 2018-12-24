package test;

import cn.zlihj.domain.Staff;
import cn.zlihj.enums.WorkType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.CopyObjectResult;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    @Test
    public void testImage() throws IOException {
        String path = "/Users/zhanglei53/Desktop/WechatIMG6.jpeg";
        BufferedImage bufImage = ImageIO.read(new File(path));
        int width = bufImage.getWidth();
        int height = bufImage.getHeight();
        int w = width;
        int x=0, y=0;
        if (width > height) {
            x += (width - height) / 2;
            w = height;
        } else if (width < height) {
            y += (height - width) / 2;
            w = width;
        }
        BufferedImage subimage = bufImage.getSubimage(x, y, w, w);
        ImageIO.write(subimage, "jpg", new File("/Users/zhanglei53/Desktop/1.jpg"));
    }

    @Test
    public void testCos() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials("AKIDIQNCnEdaYTGD7OSUIVXO6e4JQNchpMzs", "uhKwMbheR3BtZ4NZiieS7jAPVWM1qbDg");
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
        // 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20M以下的文件使用该接口
        File localFile = new File("/Users/zhanglei53/Desktop/WechatIMG3.jpeg");
        // 指定要上传到 COS 上对象键
        // 对象键（Key）是对象在存储桶中的唯一标识。例如，在对象的访问域名 `bucket1-1250000000.cos.ap-guangzhou.myqcloud.com/doc1/pic1.jpg` 中，对象键为 doc1/pic1.jpg, 详情参考 [对象键](https://cloud.tencent.com/document/product/436/13324)
        String key = "WechatIMG3.jpeg";
        PutObjectRequest putObjectRequest = new PutObjectRequest("zlihj-zpk-1251746773", key, localFile);
        PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
        System.out.println(putObjectResult.getMetadata().getRawMetadata());

        cosclient.shutdown();
    }
}
