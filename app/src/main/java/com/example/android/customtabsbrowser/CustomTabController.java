/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.customtabsbrowser;

import android.app.Activity;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.List;

import static android.support.customtabs.CustomTabsIntent.EXTRA_ACTION_BUTTON_BUNDLE;
import static android.support.customtabs.CustomTabsIntent.EXTRA_CLOSE_BUTTON_ICON;
import static android.support.customtabs.CustomTabsIntent.EXTRA_EXIT_ANIMATION_BUNDLE;
import static android.support.customtabs.CustomTabsIntent.EXTRA_MENU_ITEMS;
import static android.support.customtabs.CustomTabsIntent.EXTRA_SESSION;
import static android.support.customtabs.CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE;
import static android.support.customtabs.CustomTabsIntent.EXTRA_TOOLBAR_COLOR;
import static android.support.customtabs.CustomTabsIntent.KEY_DESCRIPTION;
import static android.support.customtabs.CustomTabsIntent.KEY_ICON;
import static android.support.customtabs.CustomTabsIntent.KEY_MENU_ITEM_TITLE;
import static android.support.customtabs.CustomTabsIntent.KEY_PENDING_INTENT;
import static android.support.customtabs.CustomTabsIntent.NO_TITLE;

/**
 * Configures a custom tab. Extracts all configuration parameters from the activity's intent.
 * Clients need to provide a
 * {@link Callback} to handle different
 * configuration parameters.
 * <p/>
 * To properly support a custom tab lifecycle, clients need to:
 * <ol>
 * <li>notify the controller whenever a new website is loaded via: {@link #onTitleChange(String)}.
 * </li>
 * <p/>
 * <li>hook the controller into the activity lifecycle:</li>
 * </ol>
 * <pre>
 *   <code>
 *   @Override
 *   protected void onStart() {
 *     super.onStart();
 *     if (mCustomTabController.hasCustomTabIntent()) {
 *       // we launch a custom tab
 *       mCustomTabController.launch();
 *     } else {
 *       // we launch the browser
 *     }
 *   }
 *   @Override
 *   public void finish() {
 *     super.finish();
 *     // must be called after super.finish()
 *     mCustomTabsController.finish();
 *   }
 *
 *   @Override
 *   public boolean onCreateOptionsMenu(Menu menu) {
 *     mCustomTabsController.updateMenu(menu);
 *     getMenuInflater().inflate(R.menu.menu_main, menu);
 *     return true;
 *   }
 *   @Override
 *   public boolean onOptionsItemSelected(MenuItem item) {
 *     if (mCustomTabsController.onOptionsItemSelected(item)) {
 *       return true;
 *     }
 *     switch (item.getItemId()) {
 *       case R.id.action_open_in_browser:
 *         startActivity(new Intent(Intent.ACTION_VIEW, getIntent().getData()));
 *         return true;
 *     }
 *     return super.onOptionsItemSelected(item);
 *   }
 *   </pre>
 * </code>
 */
public class CustomTabController {

    /**
     * Callback for browsers implementing the custom tabs protocol.
     */
    public interface Callback {

        /**
         * Set the title for this chrome tab. If no title is provided, the title string will be
         * empty. This will be called every time a new custom tab is launched.
         */
        void setTitle(String title);

        /**
         * Set the url for this chrome tab. This will be called every time a new custom tab is
         * launched.
         */
        void setUrl(String url);

        /**
         * Set the action button visibility (either {@value View#VISIBLE} or {@value View#GONE}
         * for this chrome tab. This will be called every time a new custom tab is launched.
         */
        void setActionButtonVisibility(int visible);

        /**
         * Set the action button click listener for this custom chrome tab. Only called if a custom
         * action is provided.
         */
        void setActionButtonOnClickListener(Button.OnClickListener customTabsController);

        /**
         * Set the action button icon. Only called if a custom action is provided.
         */
        void setActionButtonImageDrawable(Drawable drawable);

        /**
         * Set the action button description. Only called if a custom action is provided.
         */
        void setActionButtonContentDescription(CharSequence description);

        /**
         * Set the close action icon. Only called if a custom icon is provided.
         */
        void setActionBarCloseDrawable(Drawable drawable);

        /**
         * Set the action bar background. Only called if a custom background color is provided.
         */
        void setActionBarBackgroundDrawable(Drawable drawable);

        /**
         * Callback for errors.
         */
        void onError(String description, Exception e);
    }

    private static final String KEY_ANIM_ENTER_RES_ID = "android:activity.animEnterRes";
    private static final String KEY_ANIM_EXIT_RES_ID = "android:activity.animExitRes";
    private static final int NO_COLOR = -1;
    private final Activity mActivity;
    private final Callback mCallback;
    private final MenuItemClickListener mMenuItemClickListener = new MenuItemClickListener();
    private final ActionButtonOnClickListener mActionButtonOnClickListener =
            new ActionButtonOnClickListener();

    public CustomTabController(Activity activity, Callback callback) {
        mActivity = activity;
        mCallback = callback;
    }

    /**
     * Launches the custom tab. Should be called from {@link Activity#onStart()}
     */
    public void launch() {
        if (!hasCustomTabIntent()) {
            return;
        }
        String url = mActivity.getIntent().getDataString();
        mCallback.setUrl(url);
        updateToolbarColor();
        updateBackButtonIcon();
        updateToolbarAction();
    }

    /**
     * Triggers the exit animation. Should be called after <pre>super.finish()</pre> in
     * {@link Activity#finish()} ()}
     */
    public void finish() {
        Bundle animation = mActivity.getIntent()
                .getBundleExtra(EXTRA_EXIT_ANIMATION_BUNDLE);
        if (animation == null) {
            return;
        }
        mActivity.overridePendingTransition(
                animation.getInt(KEY_ANIM_ENTER_RES_ID),
                animation.getInt(KEY_ANIM_EXIT_RES_ID));
    }

    /**
     * Updates the title if configured. Should be called every time a new website is loaded in the
     * custom tab.
     */
    public void onTitleChange(String title) {
        int showTitle = mActivity.getIntent().getIntExtra(EXTRA_TITLE_VISIBILITY_STATE, NO_TITLE);
        if (showTitle == NO_TITLE) {
            title = "";
        }
        mCallback.setTitle(title);
    }

    /**
     * Populates the menu. Should be called as a first statement in
     * {@link Activity#onCreateOptionsMenu(Menu)}.
     */
    public void updateMenu(Menu menu) {
        if (!hasCustomTabIntent()) {
            return;
        }
        List<Bundle> menuBundles = mActivity.getIntent()
                .getParcelableArrayListExtra(EXTRA_MENU_ITEMS);
        if (menuBundles == null) {
            return;
        }
        for (int i = 0; i < menuBundles.size(); i++) {
            Bundle menuBundle = menuBundles.get(i);
            PendingIntent pendingIntent = menuBundle
                    .getParcelable(KEY_PENDING_INTENT);
            String title = menuBundle.getString(KEY_MENU_ITEM_TITLE);
            if (!TextUtils.isEmpty(title) && pendingIntent != null) {
                menu.add(Menu.NONE, i, Menu.NONE, title)
                        .setOnMenuItemClickListener(mMenuItemClickListener);
            }
        }
    }

    /**
     * Performs a menu action. Should be called from
     * {@link Activity#onOptionsItemSelected(MenuItem)}.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            mActivity.finish();
            return true;
        }
        return false;
    }

    /**
     * Returns true if the activity was started with a valid custom tab intent.
     */
    public boolean hasCustomTabIntent() {
        Bundle extras = mActivity.getIntent().getExtras();
        if (extras == null) {
            return false;
        }
        if (!extras.containsKey(EXTRA_SESSION)) {
            return false;
        }
        return mActivity.getIntent().getDataString() != null;
    }

    private void updateToolbarAction() {
        Bundle bundle = mActivity
                .getIntent()
                .getParcelableExtra(EXTRA_ACTION_BUTTON_BUNDLE);
        if (bundle == null) {
            mCallback.setActionButtonVisibility(View.GONE);
            return;
        }
        mCallback.setActionButtonVisibility(View.VISIBLE);

        String description = bundle.getString(KEY_DESCRIPTION, "");
        mCallback.setActionButtonContentDescription(description);

        Bitmap icon = bundle.getParcelable(KEY_ICON);
        mCallback.setActionButtonImageDrawable(new BitmapDrawable(mActivity.getResources(), icon));
        mCallback.setActionButtonOnClickListener(mActionButtonOnClickListener);
    }

    private void updateBackButtonIcon() {
        Bitmap bitmap = mActivity.getIntent().getParcelableExtra(EXTRA_CLOSE_BUTTON_ICON);
        if (bitmap == null) {
            return;
        }
        mCallback.setActionBarCloseDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
    }

    private void updateToolbarColor() {
        int color = mActivity.getIntent().getIntExtra(EXTRA_TOOLBAR_COLOR, NO_COLOR);
        if (color == NO_COLOR) {
            return;
        }
        mCallback.setActionBarBackgroundDrawable(new ColorDrawable(color));
        Window window = mActivity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(darken(color, 0.25));
        }
    }

    private int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darkenColor(red, fraction);
        green = darkenColor(green, fraction);
        blue = darkenColor(blue, fraction);
        int alpha = Color.alpha(color);
        return Color.argb(alpha, red, green, blue);
    }

    private int darkenColor(int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

    private void send(PendingIntent pendingIntent) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            mCallback.onError("Exception when triggering pending intent", e);
        }
    }

    private class MenuItemClickListener implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            List<Bundle> menuBundles = mActivity.getIntent()
                    .getParcelableArrayListExtra(EXTRA_MENU_ITEMS);
            if (menuBundles == null) {
                return true;
            }

            PendingIntent pendingIntent = menuBundles.get(item.getItemId())
                    .getParcelable(KEY_PENDING_INTENT);
            if (pendingIntent == null) {
                return true;
            }
            send(pendingIntent);
            return false;
        }
    }

    private class ActionButtonOnClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Bundle extras = mActivity.getIntent().getParcelableExtra(EXTRA_ACTION_BUTTON_BUNDLE);
            if (extras == null) {
                return;
            }

            final PendingIntent pendingIntent = extras.getParcelable(KEY_PENDING_INTENT);
            if (pendingIntent == null) {
                return;
            }

            send(pendingIntent);
        }

    }
}

