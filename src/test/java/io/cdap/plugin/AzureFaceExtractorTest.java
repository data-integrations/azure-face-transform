/*
 * Copyright © 2016-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.cdap.etl.mock.common.MockPipelineConfigurer;
import org.junit.Test;

/**
 * Tests {@link AzureFaceExtractor}.
 */
public class AzureFaceExtractorTest {
  private static final Schema INPUT = Schema.recordOf("input",
                                                      Schema.Field.of("body", Schema.of(Schema.Type.BYTES)));
  private static final Schema INVALID_INPUT = Schema.recordOf("input",
                                                      Schema.Field.of("a",
                                                                      Schema.arrayOf(Schema.of(Schema.Type.STRING))));

  // These are some arbitrary image files for testing
  private static String[] imageFiles = new String[] {
    "IMG_4376.JPG",
    "C819Ix6VoAAIcV8.jpg",
    "C9Y6dlMU0AAF6yh.jpg"
  };

  /*
  // Before enabling this test, you will need to provide your Face and Emotion API keys in the beginning of the function.
  @Test
  public void testDetectingFaces() throws Exception {
    String facesSubscriptionKey = "<YOUR_KEY_GOES_HERE>";
    String emotionSubscriptionKey = "<YOUR_KEY_GOES_HERE>";
    AzureFaceExtractor.Config config = new AzureFaceExtractor.Config("body", true, facesSubscriptionKey, emotionSubscriptionKey);
    Transform<StructuredRecord, StructuredRecord> transform = new AzureFaceExtractor(config);
    transform.initialize(null);
    ClassLoader classLoader = getClass().getClassLoader();
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    for (String fileName : imageFiles) {
      URL gzippedFile = classLoader.getResource(fileName);
      Path source = new Path(gzippedFile.getPath());
      FileSystem fileSystem = source.getFileSystem(new Configuration());
      byte[] imageFileData = new byte[(int) fileSystem.getFileStatus(source).getLen()];
      try (BufferedInputStream input = new BufferedInputStream(fileSystem.open(source))) {
        input.read(imageFileData);
      }
      transform.transform(StructuredRecord.builder(INPUT).set("body", imageFileData).build(), emitter);
    }
    Assert.assertEquals(14, emitter.getEmitted().size());
    Assert.assertEquals(23, emitter.getEmitted().get(0).getSchema().getFields().size());
    for (int i = 0; i < emitter.getEmitted().size(); i++) {
      Assert.assertNotNull(emitter.getEmitted().get(i).get("raw_image_data"));
      Assert.assertNotNull(emitter.getEmitted().get(i).get("face_id"));
    }
  }
  */

  @Test(expected = IllegalArgumentException.class)
  public void testFieldNotInInputSchema() throws Exception {
    AzureFaceExtractor.Config config = new AzureFaceExtractor.Config("body", true, "ANYTHING", "ANYTHING");
    Transform<StructuredRecord, StructuredRecord> transform = new AzureFaceExtractor(config);
    transform.configurePipeline(new MockPipelineConfigurer(INVALID_INPUT));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputTypeNotBytesSchema() throws Exception {
    AzureFaceExtractor.Config config = new AzureFaceExtractor.Config("a", true, "ANYTHING", "ANYTHING");
    Transform<StructuredRecord, StructuredRecord> transform = new AzureFaceExtractor(config);
    transform.configurePipeline(new MockPipelineConfigurer(INVALID_INPUT));
  }
}
