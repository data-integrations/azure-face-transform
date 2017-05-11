/*
 * Copyright © 2017 Cask Data, Inc.
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

package co.cask.hydrator.plugin;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Macro;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.plugin.PluginConfig;
import co.cask.cdap.etl.api.Emitter;
import co.cask.cdap.etl.api.PipelineConfigurer;
import co.cask.cdap.etl.api.Transform;
import co.cask.cdap.etl.api.TransformContext;
import cognitivej.vision.emotion.Emotion;
import cognitivej.vision.face.scenario.FaceScenarios;
import cognitivej.vision.face.task.Face;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Extracts faces from an image using the Azure Cognitive APIs
 */
@Plugin(type = Transform.PLUGIN_TYPE)
@Name("AzureFaceExtractor")
@Description("Extracts the faces in a given image using Azure Cognitive APIs.")
public final class AzureFaceExtractor extends Transform<StructuredRecord, StructuredRecord> {
  private static final Logger LOG = LoggerFactory.getLogger(AzureFaceExtractor.class);

  private final Config config;
  private static final Schema outputSchema =
    Schema.recordOf("output",
                    Schema.Field.of("raw_image_data", Schema.nullableOf(Schema.of(Schema.Type.BYTES))),
                    Schema.Field.of("rectangle_left", Schema.nullableOf(Schema.of(Schema.Type.INT))),
                    Schema.Field.of("rectangle_top", Schema.nullableOf(Schema.of(Schema.Type.INT))),
                    Schema.Field.of("rectangle_height", Schema.nullableOf(Schema.of(Schema.Type.INT))),
                    Schema.Field.of("rectangle_width", Schema.nullableOf(Schema.of(Schema.Type.INT))),
                    Schema.Field.of("face_id", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("age", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("mustache", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("beard", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("sideburns", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("gender", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("glasses", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("head_pose_roll", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("head_pose_yaw", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("head_pose_pitch", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("smile", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("happiness", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("neutral", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("surprise", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("fear", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("anger", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("contempt", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("disgust", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("sadness", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))));
  private FaceScenarios faceScenarios;

  @VisibleForTesting
  public AzureFaceExtractor(Config config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) throws IllegalArgumentException {
    super.configurePipeline(pipelineConfigurer);
    Schema inputSchema = pipelineConfigurer.getStageConfigurer().getInputSchema();
    config.validate(inputSchema);
    pipelineConfigurer.getStageConfigurer().setOutputSchema(outputSchema);
  }

  @Override
  public void initialize(TransformContext context) throws Exception {
    super.initialize(context);
    faceScenarios = new FaceScenarios(config.facesSubscriptionKey,
                                      config.emotionSubscriptionKey);
  }

  @Override
  public void transform(StructuredRecord in, Emitter<StructuredRecord> emitter) throws Exception {
    if (in.get(config.sourceFieldName) != null) {
      InputStream inputStream = new ByteArrayInputStream((byte[]) in.get(config.sourceFieldName));
      List<Face> faces = faceScenarios.findFaces(inputStream);
      inputStream.reset();
      List<Emotion> emotions = faceScenarios.findEmotionFaces(inputStream);
      if (faces != null) {
        for (Face face : faces) {
          try {
            Emotion theEmotion = null;
            for (Emotion e : emotions) {
              if (e.faceRectangle.left == face.faceRectangle.left && e.faceRectangle.top == face.faceRectangle.top) {
                theEmotion = e;
                break;
              }
            }
            StructuredRecord.Builder outputBuilder = StructuredRecord.builder(outputSchema)
              .set("raw_image_data", in.get(config.sourceFieldName))
              .set("rectangle_left", face.faceRectangle.left)
              .set("rectangle_top", face.faceRectangle.top)
              .set("rectangle_height", face.faceRectangle.height)
              .set("rectangle_width", face.faceRectangle.width)
              .set("face_id", face.faceId)
              .set("age", face.faceAttributesResp.age)
              .set("mustache", face.faceAttributesResp.facialHair.mustache)
              .set("beard", face.faceAttributesResp.facialHair.beard)
              .set("sideburns", face.faceAttributesResp.facialHair.sideburns)
              .set("gender", face.faceAttributesResp.gender.name())
              .set("glasses", face.faceAttributesResp.glasses.name())
              .set("head_pose_roll", face.faceAttributesResp.headPose.roll)
              .set("head_pose_yaw", face.faceAttributesResp.headPose.yaw)
              .set("head_pose_pitch", face.faceAttributesResp.headPose.pitch)
              .set("smile", face.faceAttributesResp.smile);

            if (theEmotion != null) {
              for (Map.Entry<Emotion.EmotionScore, Double> emotionScore : theEmotion.scores.scores().entrySet()) {
                if (emotionScore.getKey().name().toLowerCase().equals("suprise")) {
                  outputBuilder.set("surprise", emotionScore.getValue());
                } else {
                  outputBuilder.set(emotionScore.getKey().name().toLowerCase(), emotionScore.getValue());
                }
              }
            }
            emitter.emit(outputBuilder.build());
          } catch (Exception e) {
            if (!config.continueOnError) {
              throw e;
            } else {
              LOG.warn("Received an exception from Azure webservices. Ignoring because continue on error is true.", e);
            }
          }
        }
      }
      inputStream.close();
    }
  }

  /**
   * Azure Face Extractor plugin configuration.
   */
  public static class Config extends PluginConfig {
    @Name("sourceFieldName")
    @Description("Specifies the input field containing the binary pdf data.")
    private final String sourceFieldName;

    @Macro
    @Description("Set to true if this plugin should ignore errors.")
    private Boolean continueOnError;

    @Name("facesSubscriptionKey")
    @Description("The Azure Faces API subscription key.")
    @Macro
    private String facesSubscriptionKey;

    @Name("emotionSubscriptionKey")
    @Description("The Azure emotion API subscription key.")
    @Macro
    private String emotionSubscriptionKey;

    public Config(String sourceFieldName, Boolean continueOnError,
                  String facesSubscriptionKey, String emotionSubscriptionKey) {
      this.sourceFieldName = sourceFieldName;
      this.continueOnError = continueOnError;
      this.facesSubscriptionKey = facesSubscriptionKey;
      this.emotionSubscriptionKey = emotionSubscriptionKey;
    }

    public void validate(Schema inputSchema) {
      if (inputSchema == null) {
        throw new IllegalArgumentException("Could not get the input schema to validate.");
      }
      if (inputSchema.getField(sourceFieldName) == null) {
        throw new IllegalArgumentException("Source field was not present in the input schema. Schema: " +
                                             inputSchema.toString());
      }
      if (!inputSchema.getField(sourceFieldName).getSchema().isSimpleOrNullableSimple()) {
        throw new IllegalArgumentException("Input field must be a simple type but was type: " +
                                             inputSchema.getField(sourceFieldName).getSchema().getType());
      }
      Schema.Type fieldType = inputSchema.getField(sourceFieldName).getSchema().getType();
      if (inputSchema.getField(sourceFieldName).getSchema().isNullable()) {
        fieldType = inputSchema.getField(sourceFieldName).getSchema().getNonNullable().getType();
      }
      if(fieldType != Schema.Type.BYTES) {
        throw new IllegalArgumentException("Input field must be bytes but was: " +
                                             inputSchema.getField(sourceFieldName).getSchema().getType());
      }
    }
  }
}
