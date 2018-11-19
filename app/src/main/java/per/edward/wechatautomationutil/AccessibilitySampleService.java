package per.edward.wechatautomationutil;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.BitmapFactory;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.DataOutputStream;
import java.io.IOException;

import per.edward.wechatautomationutil.utils.Constant;
import per.edward.wechatautomationutil.utils.LogUtil;

import static java.lang.System.out;

/**
 * Created by Edward on 2018-01-30.
 */
@TargetApi(18)
public class AccessibilitySampleService extends AccessibilityService {
    private final int TEMP = 2000;
    public Bitmap bitmap;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        flag = false;
    }

    private AccessibilityNodeInfo accessibilityNodeInfo;

    /**
     * 是否已经发送过朋友圈，true已经发送，false还未发送
     */
    public static boolean flag = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        int eventType = event.getEventType();
        LogUtil.e(eventType + "             " + Integer.toHexString(eventType) + "         " + event.getClassName());
        accessibilityNodeInfo = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                if (!flag && event.getClassName().equals("android.widget.ListView")) {
                    clickCircleOfFriendsBtn();//点击发送朋友圈按钮
                }

                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                LogUtil.e("内容更新");
                if(event.getClassName().equals("android.widget.TextView"))
                {
                    messageDev();
                }

                else
                {
                    LogUtil.e("class: " + event.getClassName().toString());

                }

                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

                if (event.getClassName().equals("com.tencent.mm.ui.LauncherUI")) {//第一次启动app
                    flag = false;
                    jumpToGZH();
                    //jumpToCircleOfFriends();//进入朋友圈页面
                }

                if (!flag && event.getClassName().equals("com.tencent.mm.plugin.sns.ui.SnsUploadUI")) {
                    String content = sharedPreferences.getString(Constant.CONTENT, "");
                    inputContentFinish(content);//写入要发送的朋友圈内容
                }

                if (!flag && event.getClassName().equals("com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedPreferences != null) {
                                int index = sharedPreferences.getInt(Constant.INDEX, 0);
                                int count = sharedPreferences.getInt(Constant.COUNT, 0);
                                choosePicture(index, count);
                            }
                        }
                    }, TEMP);
                }

                break;
        }
    }


    /**
     * 跳进公众号
     */
    private void jumpToGZH() {
        LogUtil.e("第一次跳入");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.e("开始run");
                List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/azj");
                //List<AccessibilityNodeInf"o> list = accessibilityNodeInfo.findAccessibilityNodeInfosByText("省钱大表哥");

                if (list != null && list.size() != 0)
                {
                    LogUtil.e("list!=null");
                    AccessibilityNodeInfo tempInfo = list.get(1);
                    LogUtil.e(" " + list.size());
                    if (tempInfo != null && tempInfo.getParent() != null)
                    {
                        LogUtil.e("click");
                        tempInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    else
                    {
                        LogUtil.e("省钱大表哥 1");
                    }
                }
                else
                {
                    LogUtil.e("省钱大表哥为0");
                }
            }
        }, TEMP);
    }

    private void messageDev() {
        //doCaptureScreeKITKAT();//截图

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String tag = "com.tencent.mm:id/mq";//所有的语言聊天

                List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(tag);
                if (list != null && list.size() != 0) {

                    AccessibilityNodeInfo tempInfo = list.get(list.size() - 1);
                    //CharSequence classname = tempInfo.getClassName();

                    Rect bounds = new Rect();
                    tempInfo.getBoundsInScreen(bounds);

                    //String size = "bottom:" + bounds.bottom + "\nleft:"+ bounds.left + "\nright:"+ bounds.right + "\ntop:" + bounds.top;

                    doCaptureScreeKITKAT(bounds);

                }
            }

        }, TEMP);
    }

    /**
     * 跳进朋友圈
     */
    private void jumpToCircleOfFriends() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByText("朋友圈");

                List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/apt");

                if (list != null && list.size() != 0) {
                    AccessibilityNodeInfo tempInfo = list.get(0);
                    if (tempInfo != null && tempInfo.getParent() != null) {
                        tempInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }, TEMP);
    }

    /**
     * 粘贴文本
     *
     * @param tempInfo
     * @param contentStr
     * @return true 粘贴成功，false 失败
     */
    private boolean pasteContent(AccessibilityNodeInfo tempInfo, String contentStr) {
        if (tempInfo == null) {
            return false;
        }
        if (tempInfo.isEnabled() && tempInfo.isClickable() && tempInfo.isFocusable()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", contentStr);
            if (clipboard == null) {
                return false;
            }
            clipboard.setPrimaryClip(clip);
            //tempInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            //tempInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            return true;
        }
        return false;
    }

    private boolean sendMsg() {
        List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/apt");
        //List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByText("发表");//微信6.6.6版本修改为发表
        if (performClickBtn(list)) {
            flag = true;//标记为已发送
            return true;
        }
        return false;
    }

    /**
     * 写入朋友圈内容
     *
     * @param contentStr
     */
    private void inputContentFinish(final String contentStr) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessibilityNodeInfo == null) {
                    return;
                }
                List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("添加照片按钮");
                if (nodeInfoList == null ||
                        nodeInfoList.size() == 0 ||
                        nodeInfoList.get(0) == null ||
                        nodeInfoList.get(0).getParent() == null ||
                        nodeInfoList.get(0).getParent().getParent() == null ||
                        nodeInfoList.get(0).getParent().getParent().getParent() == null ||
                        nodeInfoList.get(0).getParent().getParent().getParent().getChildCount() == 0) {
                    return;
                }
                AccessibilityNodeInfo tempInfo = nodeInfoList.get(0).getParent().getParent().getParent().getChild(1);//微信6.6.6
                if (pasteContent(tempInfo, contentStr)) {
                    sendMsg();
                }
            }
        }, TEMP);
    }

    /**
     * @param accessibilityNodeInfoList
     * @return
     */
    private boolean performClickBtn(List<AccessibilityNodeInfo> accessibilityNodeInfoList) {
        if (accessibilityNodeInfoList != null && accessibilityNodeInfoList.size() != 0) {
            for (int i = 0; i < accessibilityNodeInfoList.size(); i++) {
                AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfoList.get(i);
                if (accessibilityNodeInfo != null) {
                    if (accessibilityNodeInfo.isClickable() && accessibilityNodeInfo.isEnabled()) {
                        //accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 选择图片
     *
     * @param startPicIndex 从第startPicIndex张开始选
     * @param picCount      总共选picCount张
     */
    private void choosePicture(final int startPicIndex, final int picCount) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessibilityNodeInfo == null) {
                    return;
                }
                List<AccessibilityNodeInfo> accessibilityNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("预览");
                if (accessibilityNodeInfoList == null ||
                        accessibilityNodeInfoList.size() == 0 ||
                        accessibilityNodeInfoList.get(0).getParent() == null ||
                        accessibilityNodeInfoList.get(0).getParent().getChildCount() == 0) {
                    return;
                }
                AccessibilityNodeInfo tempInfo = accessibilityNodeInfoList.get(0).getParent().getChild(3);

                for (int j = startPicIndex; j < startPicIndex + picCount; j++) {
                    AccessibilityNodeInfo childNodeInfo = tempInfo.getChild(j);
                    if (childNodeInfo != null) {
                        for (int k = 0; k < childNodeInfo.getChildCount(); k++) {
                            if (childNodeInfo.getChild(k).isEnabled() && childNodeInfo.getChild(k).isClickable()) {
                                //childNodeInfo.getChild(k).performAction(AccessibilityNodeInfo.ACTION_CLICK);//选中图片
                            }
                        }
                    }
                }

                List<AccessibilityNodeInfo> finishList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("完成(" + picCount + "/9)");//点击确定
                //performClickBtn(finishList);
            }
        }, TEMP);
    }


    /**
     * 点击发送朋友圈按钮
     */
    private void clickCircleOfFriendsBtn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessibilityNodeInfo == null) {
                    return;
                }

                //com.tencent.mm:id/apt
                List<AccessibilityNodeInfo> accessibilityNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("更多功能按钮");
                //performClickBtn(accessibilityNodeInfoList);
                //openAlbum();
            }
        }, TEMP);
    }


    /**
     * 打开相册
     */
    private void openAlbum() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessibilityNodeInfo == null) {
                    return;
                }

                List<AccessibilityNodeInfo> accessibilityNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("从相册选择");
                traverseNode(accessibilityNodeInfoList);
            }
        }, TEMP);
    }

    private boolean traverseNode(List<AccessibilityNodeInfo> accessibilityNodeInfoList) {
        if (accessibilityNodeInfoList != null && accessibilityNodeInfoList.size() != 0) {
            AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfoList.get(0).getParent();
            if (accessibilityNodeInfo != null && accessibilityNodeInfo.getChildCount() != 0) {
                accessibilityNodeInfo = accessibilityNodeInfo.getChild(0);
                if (accessibilityNodeInfo != null) {
                    accessibilityNodeInfo = accessibilityNodeInfo.getParent();
                    if (accessibilityNodeInfo != null) {
                        //accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击从相册中选择
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void onInterrupt() {

    }


    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("服务被杀死!");
    }

    /**
     * 截图
     */
    public void doCaptureScreeKITKAT(Rect bounds) {
        String command = "/system/bin/screencap -p";
        getScreenShotStream(command, true, true, bounds);
    }

    /**
     * 获取截屏流，转换成位图 * *
     *
     * @param commands * @param isRoot * @param isNeedResultMsg
     */
    public void getScreenShotStream(String commands, boolean isRoot, boolean isNeedResultMsg, Rect bounds)
    {
        if (commands == null)
            return;

        Process process = null;
        DataOutputStream os;

        try
        {
            process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());

            os.writeBytes(commands + "\n");
            os.flush();

            os.writeBytes("exit\n");
            os.flush();
            if (isNeedResultMsg)
            {
                this.bitmap = BitmapFactory.decodeStream(process.getInputStream());
                Bitmap new_bitmap = imageCropWithRect(this.bitmap, bounds);
                String text = getText(new_bitmap);
                LogUtil.e(text);
            }
            os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (process != null)
                {
                    process.exitValue();
                }
            }
            catch (IllegalThreadStateException e)
            {
                process.destroy();
            }
        }
    }

    /** * 按长方形裁切图片 *
     *
     *  @param bitmap
     *  @return
     */

    public static Bitmap imageCropWithRect(Bitmap bitmap, Rect bounds)
    {
        if (bitmap == null)
        {
            return null;
        }
        // 得到图片的宽，高
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();


        // 下面这句是关键 直接进行裁切
        Bitmap bmp = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.right - bounds.left, bounds.bottom - bounds.top, null, false);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }


        return bmp;
        // Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null, // false);
    }



    public String getText(Bitmap bitmap)
    {

        //String TESSBASE_PATH = "/storage/emulated/0/";
        String TESSBASE_PATH = Environment.getExternalStorageDirectory().toString();
        //String DEFAULT_LANGUAGE = "eng";
        String DEFAULT_LANGUAGE = "chi_sim";
        String EXPECTED_FILE = TESSBASE_PATH + "/tessdata/" + DEFAULT_LANGUAGE + ".traineddata";

        //String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        //LogUtil.e("Datapath is :" + DATAPATH);
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        //tessBaseAPI.init(TESSBASE_PATH, "chi_sim");
        LogUtil.e(TESSBASE_PATH);
        LogUtil.e(DEFAULT_LANGUAGE);
        tessBaseAPI.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();

        return text;
    }



}
