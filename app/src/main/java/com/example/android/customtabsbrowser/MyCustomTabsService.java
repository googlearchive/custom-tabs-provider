// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example.android.customtabsbrowser;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSessionToken;
import android.util.Log;

import java.util.List;

public class MyCustomTabsService extends CustomTabsService {

    private static final String TAG = "MyCustomTabsService";

    @Override
    protected boolean warmup(long flags) {
        Log.i(TAG, "warmup");
        return true;
    }

    @Override
    protected boolean newSession(CustomTabsSessionToken sessionToken) {
        Log.i(TAG, "newSession " + sessionToken);
        return true;
    }

    @Override
    protected boolean mayLaunchUrl(
            CustomTabsSessionToken sessionToken,
            Uri url,
            Bundle extras,
            List<Bundle> otherLikelyBundles) {
        Log.i(TAG, "mayLaunchUrl: " + url);
        return true;
    }

    @Override
    protected Bundle extraCommand(String commandName, Bundle args) {
        Log.i(TAG, "extraCommand: " + commandName);
        return Bundle.EMPTY;
    }

    @Override
    protected boolean updateVisuals(CustomTabsSessionToken customTabsSessionToken, Bundle bundle) {
        Log.i(TAG, "update visuals");
        return false;
    }

}
