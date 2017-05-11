# Azure Face Extractor Transform


Description
-----------
This transform leverages the [CognitiveJ](https://github.com/CognitiveJ/cognitivej) library, and in turn the [Azure Cognitive APIs](https://azure.microsoft.com/en-us/services/cognitive-services/) to extract 
faces and other metadata, including emotions, from a set of images. It is usually used in conjunction with the Whole File Reader plugin since it requires the entire contents of each image to be loaded into a single message and passed into the transform. 
Due to this, there may be memory issues when loading large images. This leverages the [Face API](https://docs.microsoft.com/en-us/azure/cognitive-services/face/) and the [Emotion API](https://docs.microsoft.com/en-us/azure/cognitive-services/emotion/home) specifically for this task. 

Use Case
--------
A developer is analyzing a large number of files and would like to identify faces in those photos to see how many men and women are in the photos. The user can combine this plugin with the whole file reader and filter using wrangler plugins based on the data extracted.

Properties
----------
| Configuration | Required | Default | Description |
| :------------ | :------: | :------ | :---------- |
| **Source Field Name** | **Y** | None | This is the name of the field on the input record containing the image file. It must be of type ``bytes`` and it must contain the entire contents of the image file. |
| **Face API Key** | **Y** | None | This key is obtained from the Azure Portal after enabling the Face API. |
| **Emotion API Key** | **Y** | None | This key is obtained from the Azure Portal after enabling the Emotion API. |
| **Continue Processing If There Are Errors?** | **Y** | false | Indicates if the pipeline should continue if processing a single PDF fails. |

Usage Notes
-----------

This plugin requires an Azure account as well as having the Face and Emotion APIs enabled on the account. This plugin will make two calls per image, and pricing is based on requests to the service. Please see the [Pricing details](https://azure.microsoft.com/en-us/pricing/details/cognitive-services/) for more information about pricing.

This plugin requires the entire contents of the image File to be loaded into memory for processing. This could cause issues when reading large images.

From the Azure Docs:
* The supported input image formats includes JPEG, PNG, GIF(the first frame), BMP. Image file size should be no larger than 4MB. 
* If a user has already called the Face API, they can submit the face rectangles as an optional input. Otherwise, Emotion API will first compute the rectangles. 
* The detectable face size range is 36x36 to 4096x4096 pixels. Faces out of this range will not be detected. 
* For each image, the maximum number of faces detected is 64 and the faces are ranked by face rectangle size in descending order. If no face is detected, an empty array will be returned. 
* Some faces may not be detected due to technical challenges, e.g. very large face angles (head-pose), large occlusion. Frontal and near-frontal faces have the best results. 
* The emotions contempt and disgust are experimental.
  
