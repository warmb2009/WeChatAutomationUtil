package per.edward.wechatautomationutil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import per.edward.wechatautomationutil.utils.Constant;

/**
 * 注意事项
 * 1、Android设备必须安装微信app
 * 2、Android Sdk Version
 * <p>
 * Created by Edward on 2018-03-15.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView()
    {
        findViewById(R.id.open_accessibility_setting).setOnClickListener(clickListener);
        findViewById(R.id.btn_save).setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.open_accessibility_setting:
                    OpenAccessibilitySettingHelper.jumpToSettingPage(getBaseContext());
                    break;
                case R.id.btn_save:
                    openWeChatApplication();
                    break;
            }
        }
    };


    private void openWeChatApplication() {
        PackageManager packageManager = getBaseContext().getPackageManager();
        Intent it = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        startActivity(it);
    }
}
