package tw.fatminmin.xposed.minminguard.blocker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import tw.fatminmin.xposed.minminguard.Main;

/**
 * Created by fatminmin on 2015/10/27.
 */
public class NameBlocking {
    private static boolean isAdView(Context context, String pkgName, String clazzName)
    {
        // corner case
        if(clazzName.startsWith("com.google.ads")) {
            return true;
        }
        for (Map<String, String> m : Main.adNetworkFields) {
            String name = m.get("name");
            String banner = m.get("banner");
            String bannerPrefix = m.get("bannerPrefix");

            // prefix is used to detect adview obfuscate by proguard
            if(banner.equals(clazzName) || clazzName.startsWith(bannerPrefix))
            {
                Util.notifyAdNetwork(context, pkgName, name);
                return true;
            }
        }
        return false;
    }

    public static void clearAdViewInLayout(final String packageName, final View view) {

        if(isAdView(view.getContext(), packageName, view.getClass().getName())) {
            Main.removeAdView(view, packageName, true);
            Util.log(packageName, "clearAdViewInLayout: " + view.getClass().getName());
        }

        if(view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for(int i = 0; i < vg.getChildCount(); i++) {
                clearAdViewInLayout(packageName, vg.getChildAt(i));
            }
        }
    }

    public static void nameBasedBlocking(final String packageName, final XC_LoadPackage.LoadPackageParam lpparam) {

        Class<?> viewGroup = XposedHelpers.findClass("android.view.ViewGroup", lpparam.classLoader);
        XposedBridge.hookAllMethods(viewGroup, "addView", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View v = (View) param.args[0];

                if (isAdView(v.getContext(), packageName, v.getClass().getName())) {
                    Main.removeAdView(v, packageName, true);
                    Util.log(packageName, "Name based blocking: " + v.getClass().getName());
                }

            }
        });
    }
}
