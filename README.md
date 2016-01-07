Android Custom Tabs Browser
===========================

This sample shows how browser vendors can support the 
[custom tabs](https://developer.chrome.com/multidevice/android/customtabs) protocol.

![Custom Tabs Provider Demo](http://googlechrome.github.io/custom-tabs-provider/images/demo.gif)

How to implement a Custom Tab Provider?
---------------------------------------

There are two steps required for browsers to implement the custom tabs protocol:

1. Handle the [custom tab intent](https://github.com/GoogleChrome/custom-tabs-client/blob/master/customtabs/src/android/support/customtabs/CustomTabsIntent.java): [CustomTabController](app/src/main/java/com/example/android/customtabsbrowser/CustomTabController.java) demonstrates how to extract configurations options from the custom tab intent. CustomTabController does not depend on a concrete browser implementation and can easily be reused in other projects.
2. Provide a [CustomTabsService](http://developer.android.com/reference/android/support/customtabs/CustomTabsService.html): see [MyCustomTabsService](app/src/main/java/com/example/android/customtabsbrowser/MyCustomTabsService.java) for a dummy implementation. Browsers should make sure to implement `CustomTabsServicer#warumUp()` and prefetching via `CustomTabsServicer#mayLaunchUrl(...)`.

Testing
---------------------------------------

Use [this version](http://googlechrome.github.io/custom-tabs-provider/apks/custom-tabs-demo.apk) of the Custom Tabs Sample app to test your implementation. 

Pre-requisites
--------------

- Android SDK v15
- This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

License
-------

Copyright 2014 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
