# Azure Face Extractor Transform

[![cm-available](https://cdap-users.herokuapp.com/assets/cm-available.svg)](https://docs.cdap.io/cdap/current/en/integrations/cask-market.html)
![cdap-action](https://cdap-users.herokuapp.com/assets/cdap-action.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Join CDAP community](https://cdap-users.herokuapp.com/badge.svg?t=wrangler)](https://cdap-users.herokuapp.com?t=1)

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
| **Continue Processing If There Are Errors?** | **Y** | false | Indicates if the pipeline should continue if processing a single image fails. |

Build
-----
To build your plugins:

    mvn clean package -DskipTests

The build will create a .jar and .json file under the ``target`` directory.
These files can be used to deploy your plugins.

UI Integration
--------------
The CDAP UI displays each plugin property as a simple textbox. To customize how the plugin properties
are displayed in the UI, you can place a configuration file in the ``widgets`` directory.
The file must be named following a convention of ``[plugin-name]-[plugin-type].json``.

See [Plugin Widget Configuration](http://docs.cdap.io/cdap/current/en/hydrator-manual/developing-plugins/packaging-plugins.html#plugin-widget-json)
for details on the configuration file.

The UI will also display a reference doc for your plugin if you place a file in the ``docs`` directory
that follows the convention of ``[plugin-name]-[plugin-type].md``.

When the build runs, it will scan the ``widgets`` and ``docs`` directories in order to build an appropriately
formatted .json file under the ``target`` directory. This file is deployed along with your .jar file to add your
plugins to CDAP.

Deployment
----------
You can deploy your plugins using the CDAP CLI:

    > load artifact <target/plugin.jar> config-file <target/plugin.json>

For example, here if your artifact is named 'azure-face-transform-1.0.0.jar':

    > load artifact target/azure-face-transform-1.0.0.jar config-file target/pdf-extractor-transform-1.0.0.json

## Mailing Lists

CDAP User Group and Development Discussions:

- `cdap-user@googlegroups.com <https://groups.google.com/d/forum/cdap-user>`__

The *cdap-user* mailing list is primarily for users using the product to develop
applications or building plugins for appplications. You can expect questions from
users, release announcements, and any other discussions that we think will be helpful
to the users.

## IRC Channel

CDAP IRC Channel: #cdap on irc.freenode.net


## License and Trademarks

Copyright © 2016-2017 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

Cask is a trademark of Cask Data, Inc. All rights reserved.

Apache, Apache HBase, and HBase are trademarks of The Apache Software Foundation. Used with
permission. No endorsement by The Apache Software Foundation is implied by the use of these marks.
