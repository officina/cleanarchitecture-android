/*
 * Copyright (c) 2016 Umberto Marini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mariniu.core.navigation;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.mariniu.core.LibConfiguration;
import com.mariniu.core.activity.BaseActivity;
import com.mariniu.core.fragment.BaseFragment;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public class NavigationManager {

    private static final String LOG_TAG = "NavigationManager";
    private static final boolean LOG = LibConfiguration.isLoggerEnabled();

    private static NavigationManager sInstance = new NavigationManager();
    // si può trasformare in una lista per gestire più livelli di fork
    private ScreenLinkInterface lastForkScreen;
    private ScreenLinkInterface activeScreen;

    private ScreenLinkInterface[] screenLinksList;
    private ScreenLinkInterface defaultActiveScreen;

    private NavigationManager() {

    }

    public static NavigationManager getInstance() {
        return sInstance;
    }

    /**
     * Set the list of {@link ScreenLinkInterface} known by this manager.
     *
     * @param screenLinksList an array of {@link ScreenLinkInterface} objects
     */
    public void bindScreenLinkValues(ScreenLinkInterface[] screenLinksList) {
        this.screenLinksList = screenLinksList;
    }

    /**
     * Set the {@link ScreenLinkInterface} object as default screen for this manager.
     *
     * @param defaultScreenLink a {@link ScreenLinkInterface} object
     */
    public void bindDefaultScreenLink(ScreenLinkInterface defaultScreenLink) {
        this.activeScreen = this.defaultActiveScreen = defaultScreenLink;
    }

    public ScreenLinkInterface getActiveScreen() {
        return activeScreen;
    }

    public boolean isRoot(ScreenLinkInterface screen, Activity act) {
        try {
            if (screen.getClass().getField(screen.getName()).getAnnotation(Root.class) != null && screen.getClass().getField(screen.getName()).getAnnotation(Root.class).value() == act.getClass()) {
                return true;
            }
        } catch (NoSuchFieldException e) {
            if (LOG) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * @return il primo screen con l'annotations {@link Root}
     */
    private ScreenLinkInterface getRoot(Activity act) {
        for (ScreenLinkInterface scr : screenLinksList) {
            if (isRoot(scr, act)) {
                return scr;
            }
        }

        if (LOG) {
            Log.e(LOG_TAG, "Non è presente un elemento Root della navigazione");
        }
        return null;
    }

    public void goTo(BaseActivity activity, ScreenLinkInterface screen) {
        if (screen != null) {
            // ricarico lo stesso fragment (migliorabile con un updateUI)
            if (screen == activeScreen) {
                goToNoHistory(activity, screen);
                return;
            }

            screen = checkConditions(activity, activeScreen, screen);
            if (screen != null) {
                // se rientro della fork oppure è un elemento di root;
                if ((lastForkScreen != null && lastForkScreen == activeScreen) || isRoot(screen, activity)) {
                    // reset in caso di elemento root
                    lastForkScreen = null;
                } else {
                    if (screen.getPreviousScreen() == null && (activeScreen.getPreviousScreen() != null || isRoot(activeScreen, activity))) {
                        // è il caso di una fork con navigazione propria
                        // l'activeScreen può essere raggiunto da diversi punti che vengono salvati in lastForkScreen
                        // mentre la restante navigazione procede al solito
                        if (lastForkScreen == null) {
                            lastForkScreen = activeScreen;
                        } else {
                            if (LOG) {
                                Log.e(LOG_TAG, "Errore", new RuntimeException("Al momento non è possibile fare fork a più livelli"));
                            }
                        }
                    }
                    // else: è il caso di altri elementi dopo la forked anche questi senza history:
                    // ritorneranno alla lastForkedScreen impostata precedentemente
                }

                goToNoHistory(activity, screen);
            } else {
                if (LOG) {
                    Log.i(LOG_TAG, "Condizione bloccante");
                }
            }
        } else {
            if (LOG) {
                Log.e(LOG_TAG, "Screen non può essere null!");
            }
        }
    }

    /**
     * Sovrascrive solo l'activeScreen
     */
    private void goToNoHistory(BaseActivity activity, ScreenLinkInterface screen) {
        activeScreen = screen;
        loadFragment(activity, activeScreen);
    }

    /**
     * @return true if have a previous screen
     */
    public boolean goBack(BaseActivity activity) {
        if (activeScreen == null) {
            return false;
        }

        NavigationBackPress fragment = getCurrentFragment(activity);
        if (fragment == null || !fragment.onBackPressed()) {
            if (activeScreen.getPreviousScreen() != null) {
                ScreenLinkInterface screen = checkConditions(activity, activeScreen, activeScreen.getPreviousScreen());
                if (screen != null) {
                    goToNoHistory(activity, screen);
                }
                return true;
            } else if (lastForkScreen != null) {
                ScreenLinkInterface screen = checkConditions(activity, activeScreen, lastForkScreen);
                if (screen != null) {
                    goToNoHistory(activity, screen);
                }
                if (screen == lastForkScreen) {
                    lastForkScreen = null;
                }
                return true;
            } else if (isRoot(activeScreen, activity)) {
                return false;
            }
            // in caso di problemi vado alla root
            else {
                if (LOG) {
                    Log.e(LOG_TAG, "Problemi di navigazione. Vado alla root");
                }
                ScreenLinkInterface screen = checkConditions(activity, activeScreen, getRoot(activity));
                if (screen != null) {
                    goToNoHistory(activity, screen);
                }
                return true;
            }
        } else {
            // back press gestito in locale
            return true;
        }
    }

    public BaseFragment getCurrentFragment(BaseActivity activity) {
        return (BaseFragment) activity.getSupportFragmentManager().findFragmentByTag(activeScreen.getName());
    }

    private Fragment loadFragment(BaseActivity activity, ScreenLinkInterface screen) {
        Fragment fragment = null;
        try {
            Method[] methods = screen.getFragment().getMethods();
            Method newInstance = null;
            for (Method m : methods) {
                if (m.getName().equals("newInstance")) {
                    newInstance = m;
                    break;
                }
            }
            if (newInstance != null) {
                if (newInstance.getParameterTypes().length > 0) {

                    Object[] par = screen.getParams();
                    // L'istanza è unica quindi dopo il get va chiamato il reset (screen.resetParams());
                    // il reset non funziona perchè in caso di back si perdono i parametri. Servirebbe
                    // qualcosa che obblighi a specificare i parametri sempre se vengono utilizzati almeno uan volta
                    if (par == null) {
                        par = new Object[newInstance.getParameterTypes().length];
                    }
                    fragment = (Fragment) newInstance.invoke(null, par);
                } else {
                    fragment = (Fragment) newInstance.invoke(null);
                }
            } else {
                fragment = (Fragment) screen.getFragment().newInstance();
            }
        } catch (Exception e) {
            if (LOG) {
                Log.e(LOG_TAG, "Errore nella creazione del fragment via reflection.", e);
            }
        }

        activity.getSupportFragmentManager().beginTransaction().replace(activity.getContentViewId(), fragment, screen.getName()).commit();
        activity.getSupportFragmentManager().executePendingTransactions();
        return fragment;
    }

    private ScreenLinkInterface checkConditions(Activity activity, ScreenLinkInterface screenFrom, ScreenLinkInterface screenTo) {
        HashSet<ScreenLinkInterface> setScreen = new HashSet<>();
        for (NavigationCondition con : NavigationCondition.getConditionsList()) {
            ScreenLinkInterface result = con.checkCondition(activity, screenFrom, screenTo);
            if (result == null) {
                return null;
            }

            if (result != screenTo) {
                setScreen.add(result);
            }
        }

        if (setScreen.size() >= 1) {
            if (setScreen.size() > 1) {
                if (LOG) {
                    Log.e(LOG_TAG, "Errore nelle conditions", new RuntimeException("Soddisfatte diverse condizioni con diversi risultati"));
                }
            }
            return setScreen.iterator().next();
        } else {
            return screenTo;
        }
    }

    public void mergeBranches(BaseActivity activity) {
        if (lastForkScreen != null) {
            goTo(activity, lastForkScreen);
        } else {
            if (LOG) {
                Log.w(LOG_TAG, "lastForkScreen is null! Go to root");
            }
            goTo(activity, getRoot(activity));
        }
    }
}
