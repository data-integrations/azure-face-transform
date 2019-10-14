/*
 * Copyright © 2019 Cask Data, Inc.
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
package io.cdap.plugin.azure.face.extractor;

import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.api.validation.ValidationException;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AzureFaceExtractorConfigTest {

  private static final String MOCK_STAGE = "mockStage";
  private static final Schema SCHEMA =
    Schema.recordOf("people",
                    Schema.Field.of("name", Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("id", Schema.of(Schema.Type.LONG)),
                    Schema.Field.of("nullable", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("decimal", Schema.decimalOf(2)),
                    Schema.Field.of("date", Schema.of(Schema.LogicalType.DATE)),
                    Schema.Field.of("record", Schema.nullableOf(
                      Schema.recordOf("record",
                                      Schema.Field.of("string_value", Schema.nullableOf(
                                        Schema.of(Schema.Type.STRING))),
                                      Schema.Field.of("int_value", Schema.nullableOf(
                                        Schema.of(Schema.Type.LONG)))
                      ))
                    ),
                    Schema.Field.of("bytes", Schema.of(Schema.Type.BYTES)));
  private static final AzureFaceExtractorConfig VALID_CONFIG = new AzureFaceExtractorConfig(
    "bytes",
    false,
    "facesSubscriptionKey",
    "emotionSubscriptionKey");

  @Test
  public void testValidConfig() {
    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    VALID_CONFIG.validate(failureCollector, SCHEMA);
    Assert.assertTrue(failureCollector.getValidationFailures().isEmpty());
  }

  @Test
  public void testValidateSourceFieldNameNull() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName(null)
      .build();
    List<String> paramNames = Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    try {
      config.validate(failureCollector, SCHEMA);
    } catch (ValidationException e) {
      assertValidationFailed(failureCollector, paramNames);
    }
  }

  @Test
  public void testValidateSourceFieldNameNonExist() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName("test")
      .build();
    List<String> paramNames = Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    try {
      config.validate(failureCollector, SCHEMA);
    } catch (ValidationException e) {
      assertValidationFailed(failureCollector, paramNames);
    }
  }

  @Test
  public void testValidateSourceFieldNameNonSimpleType() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName("record")
      .build();
    List<String> paramNames =  Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector, SCHEMA);
    assertValidationFailed(failureCollector, paramNames);
  }

  @Test
  public void testValidateSourceFieldNameDecimalType() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName("decimal")
      .build();
    List<String> paramNames = Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector, SCHEMA);
    assertValidationFailed(failureCollector, paramNames);
  }

  @Test
  public void testValidateSourceFieldNameDateType() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName("date")
      .build();
    List<String> paramNames = Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector, SCHEMA);
    assertValidationFailed(failureCollector, paramNames);
  }

  @Test
  public void testValidateSourceFieldNameNonBytesType() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName("id")
      .build();
    List<String> paramNames = Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector, SCHEMA);
    assertValidationFailed(failureCollector, paramNames);

  }

  @Test
  public void testValidateSourceNullableSimpleType() {
    AzureFaceExtractorConfig config = AzureFaceExtractorConfig.builder(VALID_CONFIG)
      .setSourceFieldName("nullable")
      .build();
    List<String> paramNames = Collections.singletonList(AzureFaceExtractorConfig.SOURCE_FIELD_NAME);

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector, SCHEMA);
    assertValidationFailed(failureCollector, paramNames);

  }

  private static void assertValidationFailed(MockFailureCollector failureCollector, List<String> paramNames) {
    List<ValidationFailure> failureList = failureCollector.getValidationFailures();
    Assert.assertEquals(paramNames.size(), failureList.size());
    Iterator<String> paramNamesIterator = paramNames.iterator();
    for (ValidationFailure failure : failureList) {
      List<ValidationFailure.Cause> causeList = failure.getCauses()
        .stream()
        .filter(cause -> cause.getAttribute(CauseAttributes.STAGE_CONFIG) != null)
        .collect(Collectors.toList());
      Assert.assertEquals(1, causeList.size());
      ValidationFailure.Cause cause = causeList.get(0);
      if (paramNamesIterator.hasNext()) {
        Assert.assertEquals(paramNamesIterator.next(), cause.getAttribute(CauseAttributes.STAGE_CONFIG));
      }
    }
  }
}
