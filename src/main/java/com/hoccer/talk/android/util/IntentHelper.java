package com.hoccer.talk.android.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class IntentHelper {

    private static final Logger LOG = Logger.getLogger(IntentHelper.class);

    // Constants ---------------------------------------------------------

    public static final String OI_FILE_MANAGER_URI = "content://org.openintents.filemanager/";
    public static final String OI_PICK_FILE = "org.openintents.action.PICK_FILE";


    // Static Methods ----------------------------------------------------

    public static boolean isOiFileManagerIntent(Intent intent) {

        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_STREAM)) {
            Uri streamUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

            if (streamUri == null) {
                return false;
            }

            if (streamUri.toString().contains(OI_FILE_MANAGER_URI)) {
                return true;
            }

        } else if (OI_PICK_FILE.equals(intent.getAction()) && intent.getData() != null) {
            Uri fileSchemeContentUri = intent.getData();
            if (fileSchemeContentUri.toString().startsWith("file://")) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return list of components which matches the intent action; first element is the user choosen
     *         default
     */
    public static ComponentInformation[] getPossibleComponents(String pAction, Context pContext,
            String... pCategories) {

        // create Intent from params
        Intent homeIntent = new Intent(pAction);
        for (String cat : pCategories) {

            homeIntent.addCategory(cat);
        }

        HashMap<String, ComponentInformation> activityInfos = queryIntentActivitiesAsMap(
                homeIntent, pContext);
        ArrayList<ComponentInformation> resultList = new ArrayList<ComponentInformation>(
                activityInfos.size());
        try {
            ComponentInformation chosen = getPreferredActivity(pAction, activityInfos, pContext);
            activityInfos.remove(chosen.componentName.getClassName());
            resultList.add(chosen); // default activity should be the 1st
            // element
        } catch (NoSuchElementException e) {
            LOG.error("No preferred activity found", e);
        }

        resultList.addAll(activityInfos.values());

        // possible activities to array
        return resultList.toArray(new ComponentInformation[activityInfos.size()]);
    }

    public static HashMap<String, ComponentInformation> queryIntentActivitiesAsMap(Intent pIntent,
            Context pContext) {

        // ResolveInfo: Information that is returned from resolving an intent
        // against an IntentFilter
        List<ResolveInfo> homeResolveInfos = pContext.getPackageManager().queryIntentActivities(
                pIntent, 0);

        // Possible Activities
        HashMap<String, ComponentInformation> activityInfos = new HashMap<String, ComponentInformation>(
                homeResolveInfos.size());
        // Fills ComponentNames (=activites) for homeResolveInfos and determines
        // bestScore
        for (ResolveInfo currentResolveInfo : homeResolveInfos) {

            ActivityInfo activityInfo = currentResolveInfo.activityInfo;
            // create ComponentName from current activity.
            ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName,
                    activityInfo.name);

            activityInfos.put(name.getClassName(), new ComponentInformation(name,
                    currentResolveInfo.match));
            LOG.debug("rival activity: " + name + "for (intent-)action: " + pIntent.getAction());
        }

        return activityInfos;
    }

    private static ComponentInformation getPreferredActivity(String pAction,
            HashMap<String, ComponentInformation> pComponents, Context pContext) {

        ArrayList<IntentFilter> filters = new ArrayList<IntentFilter>();
        ArrayList<ComponentName> activityNames = new ArrayList<ComponentName>();
        pContext.getPackageManager().getPreferredActivities(filters, activityNames, null);
        LOG.debug("found " + filters.size() + "preferred activities");

        ComponentInformation chosen = null;

        for (int i = 0; i < filters.size(); i++) {

            IntentFilter filter = filters.get(i);
            if (filter.getAction(0).equals(pAction)) {

                String className = activityNames.get(i).getClassName();
                chosen = pComponents.get(className);
                break;
            }
        }

        if (chosen == null) {
            throw new NoSuchElementException("No preferred activity found for action '" + pAction
                    + "'!");
        }

        return chosen;
    }

    public static Intent getExplicitIntentForClass(String pActivityClass) {
        int dotPos = pActivityClass.lastIndexOf(".");
        String pkg = pActivityClass.substring(0, dotPos);
        ComponentName compName = new ComponentName(pkg, pActivityClass);
        Intent intent = new Intent();
        intent.setComponent(compName);
        return intent;
    }

    public static boolean isIntentResolvable(Intent pIntent, Context pContext) {

        HashMap<String, ComponentInformation> components = IntentHelper.queryIntentActivitiesAsMap(
                pIntent, pContext);
        if (components.size() > 0) {
            LOG.debug(pIntent + " can be handled by " + components.size() + " Activities");
            return true;
        }

        LOG.debug(pIntent + " is not be resolvable on this phone");
        return false;
    }

    public static Drawable getIconForIntent(Intent pIntent, Context context) {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getActivityIcon(pIntent);
        } catch (NameNotFoundException e) {
            icon = context.getResources().getDrawable(android.R.drawable.ic_menu_help);
        }

        return icon;
    }
}
