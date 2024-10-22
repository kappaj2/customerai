package com.sk.customer.filereaders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.reader.tika.TikaDocumentReader;

import static org.assertj.core.api.Assertions.assertThat;

public class TikaDocumentReaderTest {

     @ParameterizedTest
     @CsvSource({
             "classpath:/word-sample.docx,word-sample.docx,Two kinds of links are possible, those that refer to an external website",
             "classpath:/word-sample.doc,word-sample.doc,The limited permissions granted above are perpetual and will not be revoked by OASIS",
             "classpath:/sample2.pdf,sample2.pdf,Consult doc/pdftex/manual.pdf from your tetex distribution for more",
             "classpath:/sample.ppt,sample.ppt,Sed ipsum tortor, fringilla a consectetur eget, cursus posuere sem.",
             "classpath:/sample.pptx,sample.pptx,Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
             "https://github.com/spring-projects/spring-ai/,https://github.com/spring-projects/spring-ai/,An Application Framework for AI Engineering" })
     public void testDocx(String resourceUri, String resourceName, String contentSnipped) {

          var docs = new TikaDocumentReader(resourceUri).get();
          assertThat(docs).hasSize(1);

          var doc = docs.get(0);

          assertThat(doc.getMetadata()).containsKeys(TikaDocumentReader.METADATA_SOURCE);
          assertThat(doc.getMetadata().get(TikaDocumentReader.METADATA_SOURCE)).isEqualTo(resourceName);
          assertThat(doc.getContent()).contains(contentSnipped);
     }
}
