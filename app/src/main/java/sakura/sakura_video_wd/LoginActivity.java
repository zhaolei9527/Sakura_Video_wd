package sakura.sakura_video_wd;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sakura.bottommenulibrary.bottompopfragmentmenu.BottomMenuFragment;
import sakura.bottommenulibrary.bottompopfragmentmenu.MenuItem;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText;
    private TextView button;
    private WilddogAuth mauth;
    public final static int SCANNING_REQUEST_CODE = 1;
    public final static int REQUEST_IMAGE = 100;

    private ImageView img_sao;
    private boolean isInlogin = false;
    private WilddogAuth auth;
    private List<String> list;
    private List<String> myUserList;

    private SpinerPopWindow<String> mSpinerPopWindow;
    private String[] split;
    private FrameLayout img_push;
    private LinearLayout ll_line;

    private String lianjieid;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == SCANNING_REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    final String result = bundle.getString(CodeUtils.RESULT_STRING);
                    lianjieid = result;
                    Toast.makeText(LoginActivity.this, "Id:" + result, Toast.LENGTH_LONG).show();
                    new CommomDialog(LoginActivity.this, R.style.dialog, "", new CommomDialog.OnCloseListener() {
                        @Override
                        public void onClick(Dialog dialog, boolean confirm) {
                            EditText content = (EditText) dialog.findViewById(R.id.content);
                            if (TextUtils.isEmpty(content.getText().toString())) {
                                Toast.makeText(LoginActivity.this, content.getHint().toString(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (confirm) {
                                dialog.dismiss();
                                for (int i = 0; i < myUserList.size(); i++) {
                                    String s = myUserList.get(i);
                                    String[] split = s.split("@");
                                    if (split[0].equals(result)) {
                                        myUserList.remove(i);
                                    }
                                }
                                myUserList.add(content.getText().toString() + "@" + result);
                                String userlist = "";
                                for (int i = 0; i < myUserList.size(); i++) {
                                    userlist = userlist + "&" + myUserList.get(i);
                                }
                                SPUtil.putAndApply(LoginActivity.this, "userlist", userlist);
                                editText.setText(content.getText().toString());
                            } else {
                                dialog.dismiss();
                            }
                        }
                    }).setTitle("提示").show();
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(LoginActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                if (data != null) {
                    Uri uri = data.getData();
                    try {
                        CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                            @Override
                            public void onAnalyzeSuccess(Bitmap mBitmap, final String result) {
                                Toast.makeText(LoginActivity.this, "Id:" + result, Toast.LENGTH_LONG).show();
                                lianjieid = result;
                                new CommomDialog(LoginActivity.this, R.style.dialog, "", new CommomDialog.OnCloseListener() {
                                    @Override
                                    public void onClick(Dialog dialog, boolean confirm) {
                                        EditText content = (EditText) dialog.findViewById(R.id.content);
                                        if (TextUtils.isEmpty(content.getText().toString())) {
                                            Toast.makeText(LoginActivity.this, content.getHint().toString(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (confirm) {
                                            dialog.dismiss();
                                            for (int i = 0; i < myUserList.size(); i++) {
                                                String s = myUserList.get(i);
                                                String[] split = s.split("@");
                                                if (split[0].equals(result)) {
                                                    myUserList.remove(i);
                                                }
                                            }
                                            myUserList.add(content.getText().toString() + "@" + result);
                                            String userlist = "";
                                            for (int i = 0; i < myUserList.size(); i++) {
                                                userlist = userlist + "&" + myUserList.get(i);
                                            }
                                            SPUtil.putAndApply(LoginActivity.this, "userlist", userlist);
                                            editText.setText(content.getText().toString());
                                        } else {
                                            dialog.dismiss();
                                        }
                                    }
                                }).setTitle("提示").show();
                            }

                            @Override
                            public void onAnalyzeFailed() {
                                Toast.makeText(LoginActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * popupwindow显示的ListView的item点击事件
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSpinerPopWindow.dismiss();
            editText.setText(split[position + 1].split("@")[0]);
            lianjieid = split[position + 1].split("@")[1];
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                       /*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
       /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        initView();
        myUserList = new ArrayList<String>();
        list = new ArrayList<String>();
        String userlist = (String) SPUtil.get(LoginActivity.this, "userlist", "");
        Log.e("LoginActivity", userlist);

        if (userlist != null) {
            split = userlist.split("&");
            for (int i = 1; i < split.length; i++) {
                list.add(split[i].split("@")[0]);
                myUserList.add(split[i]);
            }
        }

        Log.e("LoginActivity", myUserList.toString());
        Log.e("LoginActivity", list.toString());
        mSpinerPopWindow = new SpinerPopWindow<String>(this, list, itemClickListener);
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setDeniedMessage("")
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        if (isInlogin) {
                            return;
                        }
                        isInlogin = true;
                        //获取Sync & Auth 对象
                        auth = WilddogAuth.getInstance();
                        String userid = (String) SPUtil.get(LoginActivity.this, "userid", "");
                        if (userid.isEmpty()) {
                            Random rand = new Random();
                            final int randNum = rand.nextInt(1000000);
                            SPUtil.putAndApply(LoginActivity.this, "userid", String.valueOf(randNum));
                        }
                        auth.createUserWithEmailAndPassword((String) SPUtil.get(LoginActivity.this, "userid", "") + "@qq.com", (String) SPUtil.get(LoginActivity.this, "userid", ""))
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                            //注册成功
                                            //进行登录
                                            login();
                                        } else {
                                            // 错误处理
                                            Log.d("result", task.getException().toString());
                                            if (task.getException().toString().contains("22203")) {
                                                //注册失败，帐号已经存在，进行登录
                                                login();
                                            }
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });
    }

    private void login() {
        auth.signInWithEmailAndPassword((String) SPUtil.get(LoginActivity.this, "userid", "") + "@qq.com", (String) SPUtil.get(LoginActivity.this, "userid", "")).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "登录成功!", Toast.LENGTH_SHORT).show();
                    //身份认证成功
                    String uid = auth.getCurrentUser().getUid();
                    //用户可以使用任意自定义节点来保存用户数据，但是不要使用 [wilddogVideo]节点存放私有数据
                    //以防和Video SDK 数据发生冲突
                    //本示例采用根节点下的[users] 节点作为用户列表存储节点
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(uid, true);
                    SyncReference userRef = WilddogSync.getInstance().getReference("users");
                    userRef.goOnline();
                    userRef.updateChildren(map);
                    userRef.child(uid).onDisconnect().removeValue();

                    if (!TextUtils.isEmpty(uid)) {
                        button.setVisibility(View.VISIBLE);
                        isInlogin = false;
                    }
                } else {
                    //处理失败
                    //throw new RuntimeException("auth 失败" + task.getException().getMessage());
                    Log.e("error", task.getException().getMessage());
                    Toast.makeText(LoginActivity.this, "登录失败!", Toast.LENGTH_SHORT).show();
                    isInlogin = false;
                }
            }
        });
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.editText);
        button = (TextView) findViewById(R.id.button);
        img_push = (FrameLayout) findViewById(R.id.img_push);
        ll_line = (LinearLayout) findViewById(R.id.ll_line);

        button.setVisibility(View.GONE);
        button.setOnClickListener(this);
        img_push.setOnClickListener(this);

        FloatingActionButton btn_1 = (FloatingActionButton) findViewById(R.id.btn_1);
        btn_1.setOnClickListener(this);
        FloatingActionButton btn_2 = (FloatingActionButton) findViewById(R.id.btn_2);
        btn_2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                submit();
                break;
            case R.id.btn_1:
                new BottomMenuFragment(LoginActivity.this)
                        .addMenuItems(new MenuItem("从相册选择"))
                        .addMenuItems(new MenuItem("面对面扫描"))
                        .setOnItemClickListener(new BottomMenuFragment.OnItemClickListener() {
                            @Override
                            public void onItemClick(TextView menu_item, int position) {
                                if (position == 1) {
                                    Intent intent = new Intent(LoginActivity.this, CaptureActivity.class);
                                    startActivityForResult(intent, SCANNING_REQUEST_CODE);
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, REQUEST_IMAGE);
                                }
                            }
                        })
                        .show();
                break;
            case R.id.btn_2:
                startActivity(new Intent(LoginActivity.this, SettingActivity.class));
                break;
            case R.id.img_push:
                mSpinerPopWindow.setWidth(ll_line.getWidth());
                mSpinerPopWindow.showAsDropDown(ll_line);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editText.setText("");
    }

    private void submit() {
        // validate
        String editTextString = editText.getText().toString().trim();
        if (TextUtils.isEmpty(editTextString)) {
            Toast.makeText(this, "连接id不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lianjieid != null) {
            SPUtil.putAndApply(LoginActivity.this, "id", lianjieid);
            startActivity(new Intent(LoginActivity.this, ConversationActivity.class).putExtra("id", lianjieid));
        } else {
            editText.setText("");
            Toast.makeText(this, "请重新选择连接对象", Toast.LENGTH_SHORT).show();
        }
    }
}
