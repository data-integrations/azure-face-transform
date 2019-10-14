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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;

import javax.annotation.Nullable;

/**
 * Azure Face Extractor plugin configuration.
 */
public class AzureFaceExtractorConfig extends PluginConfig {
  public static final String SOURCE_FIELD_NAME = "sourceFieldName";
  public static final String CONTINUE_ON_ERROR = "continueOnError";
  public static final String FACES_SUBSCRIPTION_KEY = "facesSubscriptionKey";
  public static final String EMOTION_SUBSCRIPTION_KEY = "emotionSubscriptionKey";

  @Name(SOURCE_FIELD_NAME)
  @Description("Specifies the input field containing the binary pdf data.")
  private final String sourceFieldName;

  @Name(CONTINUE_ON_ERROR)
  @Macro
  @Description("Set to true if this plugin should ignore errors.")
  private Boolean continueOnError;

  @Name(FACES_SUBSCRIPTION_KEY)
  @Description("The Azure Faces API subscription key.")
  @Macro
  private String facesSubscriptionKey;

  @Name(EMOTION_SUBSCRIPTION_KEY)
  @Description("The Azure emotion API subscription key.")
  @Macro
  private String emotionSubscriptionKey;

  public AzureFaceExtractorConfig(String sourceFieldName, Boolean continueOnError,
                                  String facesSubscriptionKey, String emotionSubscriptionKey) {
    this.sourceFieldName = sourceFieldName;
    this.continueOnError = continueOnError;
    this.facesSubscriptionKey = facesSubscriptionKey;
    this.emotionSubscriptionKey = emotionSubscriptionKey;
  }

  private AzureFaceExtractorConfig(Builder builder) {
    sourceFieldName = builder.sourceFieldName;
    continueOnError = builder.continueOnError;
    facesSubscriptionKey = builder.facesSubscriptionKey;
    emotionSubscriptionKey = builder.emotionSubscriptionKey;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(AzureFaceExtractorConfig copy) {
    return builder()
      .setSourceFieldName(copy.sourceFieldName)
      .setContinueOnError(copy.continueOnError)
      .setFacesSubscriptionKey(copy.facesSubscriptionKey)
      .setEmotionSubscriptionKey(copy.emotionSubscriptionKey);
  }

  public String getSourceFieldName() {
    return sourceFieldName;
  }

  public boolean getContinueOnError() {
    return continueOnError;
  }

  public String getFacesSubscriptionKey() {
    return facesSubscriptionKey;
  }

  public String getEmotionSubscriptionKey() {
    return emotionSubscriptionKey;
  }

  public void validate(FailureCollector failureCollector, @Nullable Schema inputSchema) {
    if (inputSchema == null) {
      failureCollector.addFailure("Could not get the input schema to validate.",
                                  "Provide a valid input schema.");
      throw failureCollector.getOrThrowException();
    }

    Schema.Field field = inputSchema.getField(sourceFieldName);
    if (field == null) {
      failureCollector.addFailure(
        String.format("Source field '%s' must be present in the input schema.", sourceFieldName), null)
        .withConfigProperty(SOURCE_FIELD_NAME);
      throw failureCollector.getOrThrowException();
    }

    Schema nonNullableSchema = field.getSchema().isNullable()
      ? field.getSchema().getNonNullable()
      : field.getSchema();

    Schema.Type fieldType = nonNullableSchema.getType();
    if(nonNullableSchema.getLogicalType() != null || fieldType != Schema.Type.BYTES) {
      failureCollector.addFailure(
        String.format("Source field '%s' is of unexpected type '%s'.",
                      field.getName(),
                      nonNullableSchema.getDisplayName()),
        "Ensure it is of type 'bytes'.")
        .withConfigProperty(SOURCE_FIELD_NAME);
    }
  }


  public static final class Builder {
    private String sourceFieldName;
    private Boolean continueOnError;
    private String facesSubscriptionKey;
    private String emotionSubscriptionKey;

    private Builder() {
    }

    public Builder setSourceFieldName(String sourceFieldName) {
      this.sourceFieldName = sourceFieldName;
      return this;
    }

    public Builder setContinueOnError(Boolean continueOnError) {
      this.continueOnError = continueOnError;
      return this;
    }

    public Builder setFacesSubscriptionKey(String facesSubscriptionKey) {
      this.facesSubscriptionKey = facesSubscriptionKey;
      return this;
    }

    public Builder setEmotionSubscriptionKey(String emotionSubscriptionKey) {
      this.emotionSubscriptionKey = emotionSubscriptionKey;
      return this;
    }

    public AzureFaceExtractorConfig build() {
      return new AzureFaceExtractorConfig(this);
    }
  }
}
