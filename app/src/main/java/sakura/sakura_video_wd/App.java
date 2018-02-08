package sakura.sakura_video_wd;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by 赵磊 on 2017/10/12.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化 Auth SDK
        //初始化WilddogApp,完成初始化之后可在项目任意位置通过getInstance()获取Sync & Auth对象
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl("https://" + Constants.SYNC_APPID + ".wilddogio" + ".com");
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(getApplicationContext(), options);
        ZXingLibrary.initDisplayOpinion(this);
        CrashReport.initCrashReport(getApplicationContext(), "f42b457819", false);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code..
    }
}
