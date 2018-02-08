package sakura.sakura_video_wd;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.kyleduo.switchbutton.SwitchButton;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.base.FrameListener;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.base.WilddogVideoInitializer;
import com.wilddog.video.base.WilddogVideoView;
import com.wilddog.video.call.CallStatus;
import com.wilddog.video.call.Conversation;
import com.wilddog.video.call.RemoteStream;
import com.wilddog.video.call.WilddogVideoCall;
import com.wilddog.video.call.stats.LocalStreamStatsReport;
import com.wilddog.video.call.stats.RemoteStreamStatsReport;
import com.wilddog.wilddogauth.WilddogAuth;

import net.bither.util.NativeUtil;

import java.io.File;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static sakura.sakura_video_wd.R.id.img_secreen;


public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private MyHorizontalScrollView mHorizontalScrollView;
    private HorizontalScrollViewAdapter mAdapter;
    private static final String TAG = "ConversationActivity";
    private static int YANSHI = 30;
    private int TIME = 120;
    private int MAX = 300;
    //每隔1s执行一次.
    static Handler handler = new Handler();
    private boolean isInConversation = false;
    boolean ischeck = true;
    private RemoteStream remoteStream;
    int screenWidth;
    int screenHeight;
    private WilddogVideoCall video;
    private LocalStream localStream;
    private Conversation mConversation;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private AlertDialog alertDialog;
    private Map<Conversation, AlertDialog> conversationAlertDialogMap;
    //AlertDialog列表
    private WilddogVideoCall.Listener inviteListener = new WilddogVideoCall.Listener() {
        @Override
        public void onCalled(final Conversation conversation, String s) {
            if (!TextUtils.isEmpty(s)) {
                Toast.makeText(ConversationActivity.this, "对方邀请时候携带的信息是:" + s, Toast.LENGTH_SHORT).show();
            }
            mConversation = conversation;
            mConversation.setConversationListener(conversationListener);
            mConversation.setStatsListener(statsListener);
            AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
            builder.setMessage("邀请你加入会话");
            builder.setTitle("加入邀请");
            builder.setNegativeButton("拒绝邀请", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mConversation.reject();
                }
            });
            builder.setPositiveButton("确认加入", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    conversationAlertDialogMap.remove(conversation);
                    mConversation.accept(localStream);
                    isInConversation = true;
                }
            });
            alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            conversationAlertDialogMap.put(conversation, alertDialog);
        }

        @Override
        public void onTokenError(WilddogVideoError wilddogVideoError) {
        }

    };

    private Conversation.StatsListener statsListener = new Conversation.StatsListener() {
        @Override
        public void onLocalStreamStatsReport(LocalStreamStatsReport localStreamStatsReport) {
            changeLocalData(localStreamStatsReport);
        }

        @Override
        public void onRemoteStreamStatsReport(RemoteStreamStatsReport remoteStreamStatsReport) {
            changeRemoteData(remoteStreamStatsReport);
        }
    };

    private WilddogVideoView remote_video_view;
    private WilddogVideoView local_video_view;
    private FrameLayout fullscreen_content;
    private Dialog dialog;
    private VerticalTextView tv_lianjieid;
    private FloatingActionsMenu multiple_actions_left;
    private SwitchButton sb_nofade;
    private float mhue, msaturation, mlum;
    private static int MID_VALUE = 127;
    private Bitmap mbitmap;
    private int now = 1;
    private ImageView img_backon;
    private ImageView img_nexton;
    //  private ExecutorService executorService2;
    private File appDir;
    private ExecutorService fixThreadPool;
    private String screenCaptureFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams
                .FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View
                .SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_conversation);
        initView();
        //创建线程池
        //  executorService2 = Executors.newFixedThreadPool(24);
        fixThreadPool = newFixThreadPool(256);
        // 创建一个核心线程数为3、最大线程数为8，缓存队列大小为5的线程池
        //  LogUtil.setLogLevel(Logger.Level.DEBUG);
        //初始化Video
        WilddogVideoInitializer.initialize(getApplicationContext(), Constants.VIDEO_APPID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult()
                .getToken());
        //获取video对象
        video = WilddogVideoCall.getInstance();
        video.start();
        initVideoRender();
        createAndShowLocalStream();
        conversationAlertDialogMap = new HashMap<>();
        //在使用inviteToConversation方法前需要先设置会话邀请监听，否则使用邀请功能会抛出IllegalStateException异常
        video.setListener(inviteListener);
        dialog = WindowUtil.showLoadingDialog(ConversationActivity.this);
        dialog.show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            inviteToConversation(getIntent().getStringExtra("id"));
                            tv_lianjieid.setText("连接ID:" + getIntent().getStringExtra("id"));
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        screenWidth = WindowUtil.getScreenWidth(ConversationActivity.this);
        screenHeight = WindowUtil.getScreenHeight(ConversationActivity.this);
        if (screenHeight == 1776) {
            screenHeight = WindowUtil.getScreenHeight(ConversationActivity.this) + 144;
        }
        remote_video_view.setOnTouchListener(new ZoomListenter(screenWidth, screenHeight, ConversationActivity.this));

        SPUtil.putAndApply(ConversationActivity.this, "isbig", "1");
        //  int time = (int) SPUtil.get(ConversationActivity.this, "TIME", 0);
        screenCaptureFile = Environment.getExternalStorageDirectory().getPath() + "/ScreenCapture";
        TIME = 80;
        handler.postDelayed(runnable, TIME); // 在初始化方法里.
        MAX = 50 * 10;
        sb_nofade.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                deleteDir(screenCaptureFile);
            }
        });
        //这里给出的是一个加经验值
        mhue = (127 - MID_VALUE) * 1.0f / 255 * 180;
        msaturation = 127 * 1.0f / MID_VALUE;
        mlum = 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCanSee = true;
        multiple_actions_left.collapse();
        ViewGroup.LayoutParams layoutParams2 = img_screen.getLayoutParams();
        layoutParams2.width = screenWidth + 380;
        //layoutParams2.width = layoutParams2.width;
        //layoutParams2.height = screenHeight;
        // remote_video_view.setLayoutParams(layoutParams2);
        img_screen.setLayoutParams(layoutParams2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isCanSee = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        deleteDir(screenCaptureFile);

        if (mConversation != null) {
            mConversation.close();
            mConversation = null;
        }
        //需要离开会话时调用此方法，并做资源释放和其他自定义操作
        if (local_video_view != null) {
            local_video_view.release();
            local_video_view = null;
        }
        if (remote_video_view != null) {
            remote_video_view.release();
            remote_video_view = null;
        }
        if (mConversation != null) {
            mConversation.close();
        }
        if (localStream != null) {
            if (!localStream.isClosed()) {
                localStream.close();
            }
        }

        video.stop();
        WilddogSync.getInstance().goOffline();

    }

    ArrayList<String> screen = new ArrayList<>();
    ArrayList<String> screenname = new ArrayList<>();


    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public void delete(final String filePath) {
        fixThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        });
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param pPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public void deleteDir(final String pPath) {
        appDir = null;
        fixThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                screen.clear();
                screenname.clear();
                File dir = new File(pPath);
                deleteDirWihtFile(dir);
            }
        });
    }

    public void deleteDirWihtFile(final File dir) {

        fixThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (dir == null || !dir.exists() || !dir.isDirectory()) {
                    return;
                }
                for (File file : dir.listFiles()) {
                    if (file.isFile()) {
                        file.delete(); // 删除所有文件
                    } else if (file.isDirectory()) {
                        deleteDirWihtFile(file); // 递规的方式删除文件夹
                    }
                }
                dir.delete();// 删除目录本身
            }
        });
    }

    private boolean ispuse = false;

    public void saveBitmap(final String fileName, final int i) {
        // 创建对应大小的bitmap
        remote_video_view.setFrameListener(new FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {

                savePic2Phone(ConversationActivity.this, bitmap, fileName, i);

            }
        });
    }

    public void saveBitmaplocal(final String fileName, final int i) {
        // 创建对应大小的bitmap
        local_video_view.setFrameListener(new FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                savePic2Phone(ConversationActivity.this, bitmap, fileName, i);
            }
        });
    }

    private void savePic2Phone(Context context, final Bitmap bmp, final String fileName, int i) {
        fixThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 首先保存图片
                if (appDir == null) {
                    appDir = new File(screenCaptureFile);
                    if (!appDir.exists()) {
                        appDir.mkdir();
                    }
                }
                //第一个参数表示压缩源 bitmap
                //第二个参数表示压缩之后图片保存的路径
                //第三个参数表示压缩后图片的最大的大小 单位为 Kb
                NativeUtil.compressBitmap(bmp, screenCaptureFile + fileName, true);
            }
        });
    }

    Runnable backrunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isback[0]) {
                    img_screen.setVisibility(View.VISIBLE);
                    if (screen.size() != 0) {
                        if (!screen.isEmpty()) {
                            last = last - 1;
                            // EasyToast.showShort(ConversationActivity.this, String.valueOf(last));
                            boolean file = new File(screenCaptureFile + last + ".png").exists();
                            if (file) {
                                now = last;
                                Log.e(TAG, "backrunnable file:" + last);
                                mbitmap = BitmapFactory.decodeFile(screenCaptureFile + last + ".png");
                                if (isfanse) {
                                    Bitmap newBitmap = GrayFilter.changeToGray(mbitmap);
                                    Bitmap handleimagenagetive = handleimagenagetive(newBitmap);
                                    //把添加滤镜后的效果显示在imageview上
                                    img_screen.setBackground(new BitmapDrawable(handleimagenagetive));
                                } else {
                                    img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                                }
                            } else {
                                if (last < Integer.parseInt(screenname.get(0))) {
                                    last = Integer.parseInt(screenname.get(1));
                                    Toast.makeText(ConversationActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                                    // EasyToast.showShort(ConversationActivity.this, "没有更多了");
                                }
                                Log.e(TAG, "backrunnable last:" + last);
                            }
                        }
                    }
                    handler.postDelayed(this, 70);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    Runnable nextrunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isnext[0]) {
                    img_screen.setVisibility(View.VISIBLE);
                    if (screen.size() != 0) {
                        if (!screen.isEmpty()) {
                            last = ++last;
                            //  EasyToast.showShort(ConversationActivity.this, String.valueOf(last));
                            boolean file = new File(screenCaptureFile + last + ".png").exists();
                            if (file) {
                                now = last;
                                Log.e(TAG, "nextrunnable file:" + last);
                                mbitmap = BitmapFactory.decodeFile(screenCaptureFile + last + ".png");
                                if (isfanse) {
                                    Bitmap newBitmap = GrayFilter.changeToGray(mbitmap);
                                    Bitmap handleimagenagetive = handleimagenagetive(newBitmap);
                                    //把添加滤镜后的效果显示在imageview上
                                    img_screen.setBackground(new BitmapDrawable(handleimagenagetive));
                                } else {
                                    img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                                }
                            } else {
                                if (last > Integer.parseInt(screenname.get(screenname.size() - 2))) {
                                    last = Integer.parseInt(screenname.get(screenname.size() - 2));
                                    Toast.makeText(ConversationActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                                    //  EasyToast.showShort(ConversationActivity.this, "没有更多了");
                                }
                                Log.e(TAG, "nextrunnable last:" + last);
                            }
                        }
                    }
                    handler.postDelayed(this, 70);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private int i = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isCanSee) {
                    if (sb_nofade.isChecked()) {
                        if (ishasdate) {
                            if (!ispuse) {
//                                String isbig = (String) SPUtil.get(ConversationActivity.this, "isbig", "");
//                                if ("0".equals(isbig)) {
//                                    handler.postDelayed(this, TIME);
//                                } else {
                                i++;
                                screen.add(screenCaptureFile + i + ".png");
                                screenname.add(String.valueOf(i));
                                if (MAX < screen.size()) {
                                    delete(screen.get(0));
                                    screen.remove(0);
                                    screenname.remove(0);
                                }
                                saveBitmap(String.valueOf(i) + ".png", i);
                                handler.postDelayed(this, TIME);
                                //   }
                            } else {
                                YANSHI = YANSHI - 1;
                                if (YANSHI < 0) {
                                    YANSHI = 0;
                                }
                                if (YANSHI == 0) {
                                    handler.postDelayed(this, TIME);
                                } else {
//                                    String isbig = (String) SPUtil.get(ConversationActivity.this, "isbig", "");
//                                    if ("0".equals(isbig)) {
//                                        handler.postDelayed(this, TIME);
//                                    } else {
                                    i++;
                                    screen.add(screenCaptureFile + i + ".png");
                                    screenname.add(String.valueOf(i));
                                    if (MAX < screen.size()) {
                                        delete(screen.get(0));
                                        screen.remove(0);
                                        screenname.remove(0);
                                    }
                                    saveBitmaplocal(String.valueOf(i) + ".png", i);
                                    if (MAX == screen.size()) {
                                        nowScreen = nowScreen - 1;
                                    }
                                    handler.postDelayed(this, TIME);
//                                    }
                                }
                            }
                        } else {
                            handler.postDelayed(this, TIME);
                        }
                    } else {
                        handler.postDelayed(this, TIME);
                    }
                } else {
                    handler.postDelayed(this, TIME);
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void changeLocalData(final LocalStreamStatsReport localStats) {
    }


    private boolean ishasdate = false;

    public void changeRemoteData(final RemoteStreamStatsReport remoteStats) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String isbig = (String) SPUtil.get(ConversationActivity.this, "isbig", "");
                if ("0".equals(isbig)) {
                    ll_data.setVisibility(View.GONE);
                } else {
                    ll_data.setVisibility(View.VISIBLE);
                    tvRemoteDimensions.setTextColor(R.color.viewfinder_laser);
                    tvRemoteFps.setTextColor(R.color.viewfinder_laser);
                    tvRemoteRecBytes.setTextColor(R.color.viewfinder_laser);
                    tvRemoteRate.setTextColor(R.color.viewfinder_laser);
                    tvRemoteDimensions.setText("dimension:" + remoteStats.getWidth() + "x" + remoteStats.getHeight());
                    tvRemoteFps.setText("fps:" + remoteStats.getFps());
                    tvRemoteRecBytes.setText("received:" + convertToMB(remoteStats.getBytesReceived()) + "MB");
                    tvRemoteRate.setText("rate:" + remoteStats.getBitsReceivedRate() + "Kb/s" + " delay" + remoteStats.getDelay() + "ms");
                    if (String.valueOf(remoteStats.getBitsReceivedRate()).equals("0")) {
                        ishasdate = false;
                    } else {
                        ishasdate = true;
                    }

                }
            }
        });

    }


    public String convertToMB(BigInteger value) {
        try {
            float result = Float.parseFloat(String.valueOf(value)) / (1024 * 1024);
            return decimalFormat.format(result);
        } catch (Exception e) {

        }
        return "";
    }


    private void createAndShowLocalStream() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        LocalStreamOptions options = builder.dimension(LocalStreamOptions.Dimension.DIMENSION_120P).build();
        //创建本地视频流，通过video对象获取本地视频流
        localStream = LocalStream.create(options);
        //开启音频/视频，设置为 false 则关闭声音或者视频画面
        localStream.enableAudio(false);
        localStream.enableVideo(false);
        //为视频流绑定播放控件
        //localStream.attach(remote_video_view);
    }

    //初始化视频展示控件
    private void initVideoRender() {
        //获取EglBase对象
        //初始化视频展示控件位置，大小
//        local_video_layout.setPosition(LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
        local_video_view.setZOrderMediaOverlay(true);
        local_video_view.setMirror(true);
//        remote_video_layout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
    }

    private void closeConversation() {
        if (mConversation != null) {
            mConversation.close();
            mConversation = null;
            //挂断时会释放本地流，如需继续显示本地流，则挂断后要重新获取一次本地流
        }
    }

    //调用inviteToConversation 方法发起会话
    private void inviteToConversation(String participant) {
        String data = "extra data";
        //创建连接参数对象
        mConversation = video.call(participant, localStream, data);
        mConversation.setConversationListener(conversationListener);
        mConversation.setStatsListener(statsListener);
    }

    private Conversation.Listener conversationListener = new Conversation.Listener() {
        @Override
        public void onCallResponse(CallStatus callStatus) {
            switch (callStatus) {
                case ACCEPTED:
                    isInConversation = true;
                    break;
                case REJECTED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConversationActivity.this, "对方拒绝你的邀请", Toast.LENGTH_SHORT).show();
                            isInConversation = false;
                        }
                    });
                    break;
                case BUSY:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConversationActivity.this, "对方正在通话中,稍后再呼叫", Toast.LENGTH_SHORT).show();
                            isInConversation = false;
                        }
                    });
                    break;
                case TIMEOUT:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConversationActivity.this, "呼叫对方超时,请稍后再呼叫", Toast.LENGTH_SHORT).show();
                            isInConversation = false;
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStreamReceived(RemoteStream remoteStream) {
            ConversationActivity.this.remoteStream = remoteStream;
            remoteStream.attach(remote_video_view);
            remoteStream.enableAudio(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    remote_video_view.setScalingType(WilddogVideoView.ScalingType.SCALE_ASPECT_FIT);
                    local_video_view.setScalingType(WilddogVideoView.ScalingType.SCALE_ASPECT_FIT);
                }
            });
        }

        @Override
        public void onClosed() {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            isInConversation = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeConversation();
                    Toast.makeText(ConversationActivity.this, "对方挂断", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onError(WilddogVideoError wilddogVideoError) {
            if (wilddogVideoError != null) {
                isInConversation = false;
            }
        }
    };

    private VerticalTextView tvRemoteDimensions;
    private VerticalTextView tvRemoteFps;
    private VerticalTextView tvRemoteRecBytes;
    private VerticalTextView tvRemoteRate;
    private LinearLayout ll_data;

    private boolean isCanSee = false;
    private ImageView img_screen;
    private ImageView img_back;
    private ImageView img_next;
    private int nowScreen = 0;
    final boolean[] isback = {false};
    final boolean[] isnext = {false};
    int last = 0;

    private boolean isluzhicheck = false;
    private Bitmap nowbitmip;

    private void initView() {
        img_screen = (ImageView) findViewById(img_secreen);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_next = (ImageView) findViewById(R.id.img_next);

        img_backon = (ImageView) findViewById(R.id.img_backon);
        img_nexton = (ImageView) findViewById(R.id.img_nexton);

        remote_video_view = (WilddogVideoView) findViewById(R.id.remote_video_view);
        local_video_view = (WilddogVideoView) findViewById(R.id.local_video_view);
        fullscreen_content = (FrameLayout) findViewById(R.id.fullscreen_content);
        tvRemoteDimensions = (VerticalTextView) findViewById(R.id.tv_remote_dimensions);
        tvRemoteFps = (VerticalTextView) findViewById(R.id.tv_remote_fps);
        tvRemoteRecBytes = (VerticalTextView) findViewById(R.id.tv_remote_recbytes);
        tvRemoteRate = (VerticalTextView) findViewById(R.id.tv_remote_rate);
        tv_lianjieid = (VerticalTextView) findViewById(R.id.tv_lianjieid);
        ll_data = (LinearLayout) findViewById(R.id.ll_data);
        sb_nofade = (SwitchButton) findViewById(R.id.sb_nofade);
        multiple_actions_left = (FloatingActionsMenu) findViewById(R.id.multiple_actions_left);
        FloatingActionButton btn_1 = (FloatingActionButton) findViewById(R.id.btn_1);
        FloatingActionButton btn_2 = (FloatingActionButton) findViewById(R.id.btn_2);
        FloatingActionButton btn_3 = (FloatingActionButton) findViewById(R.id.btn_3);
        FloatingActionButton btn_4 = (FloatingActionButton) findViewById(R.id.btn_4);
        FloatingActionButton btn_5 = (FloatingActionButton) findViewById(R.id.btn_5);

        local_video_view.setMirror(ismirror);
        remote_video_view.setMirror(ismirror);
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);
        btn_4.setOnClickListener(this);
        btn_5.setOnClickListener(this);


        img_screen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        remote_video_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {

                    //设置适配器
                    mAdapter = new HorizontalScrollViewAdapter(ConversationActivity.this, screen);
                    mHorizontalScrollView.initDatas(mAdapter);

                    ispuse = true;
                    nowScreen = screen.size() - 1;
                    last = i;
                    now = last;
                    if (sb_nofade.isChecked()) {
                        img_back.setVisibility(View.VISIBLE);
                        img_next.setVisibility(View.VISIBLE);
                        img_backon.setVisibility(View.VISIBLE);
                        img_nexton.setVisibility(View.VISIBLE);
                    }
                    isluzhicheck = sb_nofade.isChecked();
                    if (!isluzhicheck) {
                        // 创建对应大小的bitmap
                        remote_video_view.setFrameListener(new FrameListener() {
                            @Override
                            public void onFrame(final Bitmap bitmap) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        nowbitmip = bitmap;
                                        img_screen.setBackground(new BitmapDrawable(bitmap));
                                        remoteStream.detach();
                                        localStream.detach();
                                        remoteStream.attach(local_video_view);
                                        local_video_view.setVisibility(View.VISIBLE);
                                        img_screen.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });
                    } else {
                        // 创建对应大小的bitmap
                        remote_video_view.setFrameListener(new FrameListener() {
                            @Override
                            public void onFrame(final Bitmap bitmap) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        nowbitmip = bitmap;
                                        img_screen.setBackground(new BitmapDrawable(bitmap));
                                        remoteStream.detach();
                                        localStream.detach();
                                        remoteStream.attach(local_video_view);
                                        local_video_view.setVisibility(View.VISIBLE);
                                        img_screen.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });
                    }
                } catch (Exception e) {

                }
            }
        });

        local_video_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ispuse = false;
                nowScreen = 0;
                now = 0;
                YANSHI = 100;
                img_back.setVisibility(View.GONE);
                img_next.setVisibility(View.GONE);

                img_backon.setVisibility(View.GONE);
                img_nexton.setVisibility(View.GONE);

                remoteStream.detach();
                localStream.detach();
                remoteStream.attach(remote_video_view);
                local_video_view.setVisibility(View.GONE);
                img_screen.setVisibility(View.GONE);
                deleteDir(screenCaptureFile);
            }
        });


        img_backon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_screen.setVisibility(View.VISIBLE);
                if (screen.size() != 0) {
                    if (!screen.isEmpty()) {
                        last = last - 1;
                        //EasyToast.showShort(ConversationActivity.this, String.valueOf(last));
                        boolean file = new File(screenCaptureFile + last + ".png").exists();
                        if (file) {
                            now = last;
                            Log.e(TAG, "backrunnable file:" + last);
                            mbitmap = BitmapFactory.decodeFile(screenCaptureFile + last + ".png");
                            if (isfanse) {
                                Bitmap newBitmap = GrayFilter.changeToGray(mbitmap);
                                Bitmap handleimagenagetive = handleimagenagetive(newBitmap);
                                //把添加滤镜后的效果显示在imageview上
                                img_screen.setBackground(new BitmapDrawable(handleimagenagetive));
                            } else {
                                img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                            }
                        } else {
                            if (last < Integer.parseInt(screenname.get(0))) {
                                last = Integer.parseInt(screenname.get(1));
                                Toast.makeText(ConversationActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                                //  EasyToast.showShort(ConversationActivity.this, "没有更多了");
                            }
                            Log.e(TAG, "backrunnable last:" + last);
                        }
                    }
                }
            }
        });

        img_back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isback[0] = true;
                        handler.postDelayed(backrunnable, 200);
                        break;
                    case MotionEvent.ACTION_UP:
                        isback[0] = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        img_nexton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_screen.setVisibility(View.VISIBLE);
                if (screen.size() != 0) {
                    if (!screen.isEmpty()) {
                        last = ++last;
                        //  EasyToast.showShort(ConversationActivity.this, String.valueOf(last));
                        boolean file = new File(screenCaptureFile + last + ".png").exists();
                        if (file) {
                            now = last;
                            mbitmap = BitmapFactory.decodeFile(screenCaptureFile + last + ".png");
                            if (isfanse) {
                                Bitmap newBitmap = GrayFilter.changeToGray(mbitmap);
                                Bitmap handleimagenagetive = handleimagenagetive(newBitmap);
                                //把添加滤镜后的效果显示在imageview上
                                img_screen.setBackground(new BitmapDrawable(handleimagenagetive));
                            } else {
                                img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                            }
                        } else {
                            if (last > Integer.parseInt(screenname.get(screenname.size() - 2))) {
                                last = Integer.parseInt(screenname.get(screenname.size() - 2));
                                Toast.makeText(ConversationActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                                //  EasyToast.showShort(ConversationActivity.this, "没有更多了");
                            }
                        }
                    }
                }
            }
        });

        img_next.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isnext[0] = true;
                        handler.postDelayed(nextrunnable, 200);
                        break;
                    case MotionEvent.ACTION_UP:
                        isnext[0] = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.id_horizontalScrollView);
        //添加滚动回调
        mHorizontalScrollView
                .setCurrentImageChangeListener(new MyHorizontalScrollView.CurrentImageChangeListener() {
                    @Override
                    public void onCurrentImgChanged(int position,
                                                    View viewIndicator) {
                        img_screen.setVisibility(View.VISIBLE);
                        img_screen.setImageURI(Uri.parse("file://" + screen.get(position)));
                        viewIndicator.setBackgroundColor(Color
                                .parseColor("#AA024DA4"));
                    }
                });
        //添加点击回调
        mHorizontalScrollView.setOnItemClickListener(new MyHorizontalScrollView.OnItemClickListener() {

            @Override
            public void onClick(View view, int position) {
                img_screen.setVisibility(View.VISIBLE);
                img_screen.setImageURI(Uri.parse("file://" + screen.get(position)));
                view.setBackgroundColor(Color.parseColor("#AA024DA4"));
            }
        });

    }

    private boolean ismirror = true;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                //全屏
                multiple_actions_left.collapse();
                ViewGroup.LayoutParams layoutParams2 = remote_video_view.getLayoutParams();
                layoutParams2.width = screenWidth;
                layoutParams2.height = screenHeight;
                remote_video_view.setLayoutParams(layoutParams2);
                break;
            case R.id.btn_2:
                multiple_actions_left.collapse();
                ispuse = false;
                nowScreen = 0;
                img_back.setVisibility(View.GONE);
                img_next.setVisibility(View.GONE);

                img_backon.setVisibility(View.GONE);
                img_nexton.setVisibility(View.GONE);

                remoteStream.detach();
                localStream.detach();
                remoteStream.attach(remote_video_view);
                local_video_view.setVisibility(View.GONE);
                img_screen.setVisibility(View.GONE);
                deleteDir(screenCaptureFile);
                break;
            case R.id.btn_3:
                //######## 画面镜像逻辑 ########
                multiple_actions_left.collapse();
                ismirror = !ismirror;
                remote_video_view.setMirror(ismirror);
                local_video_view.setMirror(ismirror);
                break;
            case R.id.btn_4:
                //这里给出的是一个加经验值
                mhue = (127 - MID_VALUE) * 1.0f / 255 * 180;
                msaturation = 127 * 1.0f / MID_VALUE;
                //截图亮度设置
                mlum = mlum + 0.5f;
                if (mlum > 3) {
                    mlum = 1;
                }
                if (isluzhicheck) {
                    mbitmap = BitmapFactory.decodeFile(screenCaptureFile + now + ".png");
                    img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                } else {
                    if (mlum == 1) {
                        mbitmap = nowbitmip;
                        img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                    } else {
                        mbitmap = nowbitmip;
                        img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                    }
                }
                break;
            case R.id.btn_5:
                try {
                    mhue = (127 - MID_VALUE) * 1.0f / 255 * 180;
                    msaturation = 127 * 1.0f / MID_VALUE;
                    mlum = 1;
                    //截图反色设置
                    if (!isfanse) {
                        if (isluzhicheck) {
                            mbitmap = BitmapFactory.decodeFile(screenCaptureFile + now + ".png");
                            Bitmap newBitmap = GrayFilter.changeToGray(mbitmap);
                            Bitmap handleimagenagetive = handleimagenagetive(newBitmap);
                            //把添加滤镜后的效果显示在imageview上
                            img_screen.setBackground(new BitmapDrawable(handleimagenagetive));
                        } else {
                            mbitmap = nowbitmip;
                            Bitmap newBitmap = GrayFilter.changeToGray(mbitmap);
                            Bitmap handleimagenagetive = handleimagenagetive(newBitmap);
                            //把添加滤镜后的效果显示在imageview上
                            img_screen.setBackground(new BitmapDrawable(handleimagenagetive));
                        }
                    } else {
                        if (isluzhicheck) {
                            mbitmap = BitmapFactory.decodeFile(screenCaptureFile + now + ".png");
                            img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                        } else {
                            mbitmap = nowbitmip;
                            img_screen.setBackground(new BitmapDrawable(ImageOperation.imageoperation(mbitmap, mhue, msaturation, mlum)));
                        }
                    }
                    isfanse = !isfanse;
                } catch (Exception e) {

                }
                break;
            default:
                break;
        }
    }

    private boolean isfanse = false;

    //底片效果
    public static Bitmap handleimagenagetive(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int heigh = bitmap.getHeight();
        int r, g, b, a;
        int color;
        int[] oldpixel = new int[width * heigh];
        int[] newpixel = new int[width * heigh];
        Bitmap bitmaphandle = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmaphandle);
        bitmap.getPixels(oldpixel, 0, width, 0, 0, width, heigh);
        for (int i = 0; i < width * heigh; i++) {
            color = oldpixel[i];
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;
            if (r > 255) {
                r = 255;
            } else if (r < 0) {
                r = 0;
            }
            if (g > 255) {
                g = 255;
            } else if (r < 0) {
                g = 0;
            }
            if (b > 255) {
                b = 255;
            } else if (r < 0) {
                b = 0;
            }
            //合成新颜色
            newpixel[i] = Color.argb(a, r, g, b);
        }
        bitmaphandle.setPixels(newpixel, 0, width, 0, 0, width, heigh);
        return bitmaphandle;
    }


    public static ExecutorService newFixThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

}
