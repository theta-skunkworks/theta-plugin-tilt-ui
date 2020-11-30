/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package skunkworks.tiltui;

import java.util.function.Consumer;

import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import com.theta360.pluginapplication.task.ChangeApertureTask;
import com.theta360.pluginapplication.task.ChangeEvTask;
import com.theta360.pluginapplication.task.ChangeExpProgTask;
import com.theta360.pluginapplication.task.ChangeFilterTask;
import com.theta360.pluginapplication.task.ChangeFormatTask;
import com.theta360.pluginapplication.task.ChangeIsoTask;
import com.theta360.pluginapplication.task.ChangeShutterSpeedTask;
import com.theta360.pluginapplication.task.ChangeWbTask;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginapplication.task.ShutterButtonTask;
import com.theta360.pluginapplication.task.ChangeExposureDelayTask;
import com.theta360.pluginapplication.task.ChangeCaptureModeTask;
import com.theta360.pluginapplication.task.GetCameraStatusTask;



public class MainActivity extends PluginActivity {
    private static final String TAG = "TiltUI";

    //Attitude
    private SensorManager sensorManager;
    private Attitude attitude;

    //プラグイン起動時のMode長押し後 Upをスルーする用
    private boolean onKeyDownModeButton = false;

    private boolean runningChgParam=false;

    //長押し後のボタン離し認識用
    private boolean onKeyLongPressWlan = false;
    private boolean onKeyLongPressFn = false;

    private boolean occurUpsideDownButtonPress = false;

    //パラメータ増減用
    private static final String PARAM_UP = "+";
    private static final String PARAM_DOWN = "-";
    private String onParamUpDownReq = "";

    //表示系クラス（OLED描画クラス継承, LED表示も含む）
    DisplayInfo displayInfo=null;
    //表示スレッド終了用
    private boolean mFinished;

    //プラグイン終了時の設定復帰用
    private String restoreCaptureMode = "";
    private int restoreExposureDelay = 0;


    // <<設定変更タスクの共通コールバック処理>>
    //通常処理用
    Consumer<String> consumerAfterButtonExec = string -> commonAfterButtonExec(string);
    void commonAfterButtonExec(String inStr){
        Log.d(TAG, "consumerAfterButtonExec() : inStr=" + inStr);

        runningChgParam=false;
    }
    //露出プログラムAUTO かつ filterがoff以外 → 別の露出モード 変更用
    Consumer<String> consumerNextChgExpProgExec = string -> commonNextChgExpProgExec(string);
    void commonNextChgExpProgExec(String inStr){
        Log.d(TAG, "commonNextChgExpProgExec() : inStr=" + inStr);
        displayInfo.strLastFilter = inStr;
        new ChangeExpProgTask(consumerAfterButtonExec, displayInfo.expProgApi2Disp, "+").execute();
    }
    //露出プログラム MANU -> AUTO にするときのフィルター復旧用
    Consumer<String> consumerRestoreFilterExec = string -> commonRestoreFilterExec(string);
    void commonRestoreFilterExec(String inStr){
        Log.d(TAG, "commonRestoreFilterExec() : inStr=" + inStr);
        new ChangeFilterTask(consumerAfterButtonExec, displayInfo.strLastFilter).execute();
    }

    //<<その他 各タスク固有のコールバック>>
    private GetCameraStatusTask.Callback mGetCameraStatusCallback = new GetCameraStatusTask.Callback() {
        @Override
        public void onUpdateCameraStatus( boolean busy,
                                          int recordedTime,
                                          int compositeShootingElapsedTime,
                                          String captureMode,
                                          int exposureDelay,
                                          String strExpProg,
                                          String strAv,
                                          String strTv,
                                          String strIso,
                                          String strExpComp,
                                          String strWb,
                                          String strColorTemperature,
                                          String strFilter,
                                          String strFormatType ) {
            displayInfo.busy = busy;
            displayInfo.recordedTime = recordedTime;
            displayInfo.compositeShootingElapsedTime = compositeShootingElapsedTime;
            displayInfo.captureMode = captureMode;
            displayInfo.exposureDelay = exposureDelay;
            displayInfo.strbeforExpProg = displayInfo.strExpProg;
            displayInfo.strExpProg = strExpProg;
            displayInfo.strAv = strAv;
            displayInfo.strTv = strTv;
            displayInfo.strIso = strIso;
            displayInfo.strExpComp = strExpComp;
            displayInfo.strWb = strWb;
            if ( !displayInfo.strWb.equals(ChangeWbTask.CT) ) {
                displayInfo.strLastPresetWb = displayInfo.strWb;
            }

            displayInfo.strColorTemperature = strColorTemperature;
            displayInfo.strFilter = strFilter;

            if ( strFormatType.equals("raw+") ) {
                displayInfo.stillFormat = DisplayInfo.STILL_FORMAT_RAW_P;
            } else {
                displayInfo.stillFormat = DisplayInfo.STILL_FORMAT_JPEG;
            }

            if ( !displayInfo.strExpProg.equals(displayInfo.strbeforExpProg) ) {
                if ( strExpProg.equals("1") ) {         // - MANU -
                    displayInfo.chgParam = displayInfo.CHG_PARAM_FNO;
                } else if ( strExpProg.equals("2") ) {  // - AUTO -
                    displayInfo.chgParam = displayInfo.CHG_PARAM_EV;
                } else if ( strExpProg.equals("3") ) {  // - Av -
                    displayInfo.chgParam = displayInfo.CHG_PARAM_EV;
                } else if ( strExpProg.equals("4") ) {  // - Tv -
                    displayInfo.chgParam = displayInfo.CHG_PARAM_EV;
                } else if ( strExpProg.equals("9") ) {  // - ISO -
                    displayInfo.chgParam = displayInfo.CHG_PARAM_EV;
                }
            }

        }
    };


    private GetCameraStatusTask.Callback mSaveRestoreData = new GetCameraStatusTask.Callback() {
        @Override
        public void onUpdateCameraStatus( boolean busy,
                                          int recordedTime,
                                          int compositeShootingElapsedTime,
                                          String captureMode,
                                          int exposureDelay,
                                          String strExpProg,
                                          String strAv,
                                          String strTv,
                                          String strIso,
                                          String strExpComp,
                                          String strWb,
                                          String strColorTemperature,
                                          String strFilter,
                                          String strFormatType ) {
            //以下2つを保存
            restoreCaptureMode = captureMode;
            restoreExposureDelay = exposureDelay;
        }
    };


    //==============================================================
    // MainActivity 定型
    //==============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init Attitude
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        attitude = new Attitude(sensorManager);

        //OLEDディスプレイまわり初期化
        displayInfo = new DisplayInfo(getApplicationContext());
        displayInfo.brightness(DisplayInfo.BRIGHTNESS_BASE);     //輝度設定
        displayInfo.clear(displayInfo.black); //表示領域クリア設定
        displayInfo.draw();                     //表示領域クリア結果を反映


        // Set enable to close by pluginlibrary, If you set false, please call close() after finishing your end processing.
        setAutoClose(true);
        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {
            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {

                switch (keyCode) {
                    case KeyReceiver.KEYCODE_CAMERA :
                    case KeyEvent.KEYCODE_VOLUME_UP :
                        new ShutterButtonTask(displayInfo.multipleShooting).execute();

                        break;
                    case KeyReceiver.KEYCODE_MEDIA_RECORD :
                        //プラグイン起動時のMode長押し後 onKeyUp() を無処理とするための仕掛け
                        onKeyDownModeButton = true;
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {

                switch (keyCode) {
                    case KeyReceiver.KEYCODE_WLAN_ON_OFF :
                        if (onKeyLongPressWlan) {
                            onKeyLongPressWlan=false;
                        } else {
                            //短押しの処理
                            if ( (displayInfo.curTilt == displayInfo.TILT_UPSIDE_DOWN) &&
                                    (displayInfo.captureMode.equals(ChangeCaptureModeTask.CAPMODE_IMAGE)) ) {
                                //逆さま姿勢でボタン操作がをしたときは WB Modeを変更しないようにするためのフラグ設定
                                occurUpsideDownButtonPress = true;

                                //連続撮影設定 切り替え
                                switch ( displayInfo.multipleShooting ) {
                                    case DisplayInfo.M_SHOOT_SINGLE :
                                        displayInfo.multipleShooting = DisplayInfo.M_SHOOT_TIMESHIFT;
                                        break;
                                    case DisplayInfo.M_SHOOT_TIMESHIFT :
                                        displayInfo.multipleShooting = DisplayInfo.M_SHOOT_INTERVAL;
                                        break;
                                    case DisplayInfo.M_SHOOT_INTERVAL :
                                        displayInfo.multipleShooting = DisplayInfo.M_SHOOT_INT_COMP;
                                        break;
                                    case DisplayInfo.M_SHOOT_INT_COMP :
                                        displayInfo.multipleShooting = DisplayInfo.M_SHOOT_SINGLE;
                                        break;
                                }
                            } else {
                                //パラメータ＋方向
                                onParamUpDownReq = PARAM_UP;
                            }
                        }

                        break;
                    case KeyReceiver.KEYCODE_MEDIA_RECORD :
                        //プラグイン起動時のMode長押し後 onKeyUp() を無処理とするための仕掛け
                        if (onKeyDownModeButton) {
                            if ( displayInfo.curTilt == displayInfo.TILT_UPSIDE_DOWN) {
                                //逆さま姿勢でボタン操作がをしたときは WB Modeを変更しないようにするためのフラグ設定
                                occurUpsideDownButtonPress = true;

                                //キャプチャーモード変更
                                runningChgParam=true;
                                new ChangeCaptureModeTask(consumerAfterButtonExec, ChangeCaptureModeTask.CAPMODE_TGGLE).execute();
                            } else {
                                //露出プログラム変更
                                runningChgParam=true;
                                if ( displayInfo.strExpProg.equals("2") ) {
                                    // filterをoffにしたあと、露出プログラムを変更する
                                    Log.d(TAG, "filter off & Chg ExpProg");
                                    new ChangeFilterTask(consumerNextChgExpProgExec, "off").execute();
                                    //commonAfterCommandExec
                                } else if ( displayInfo.strExpProg.equals("1") ) {
                                    // 露出プログラムを変更したあと、filterを復旧する
                                    new ChangeExpProgTask(consumerRestoreFilterExec, displayInfo.expProgApi2Disp, "+").execute();

                                } else {
                                    Log.d(TAG,"Chg ExpProg");
                                    new ChangeExpProgTask(consumerAfterButtonExec, displayInfo.expProgApi2Disp, "+").execute();
                                }
                            }
                        }
                        onKeyDownModeButton = false;

                        break;
                    case KeyEvent.KEYCODE_FUNCTION :
                        if (onKeyLongPressFn) {
                            onKeyLongPressFn=false;
                        } else {
                            if ( displayInfo.curTilt == displayInfo.TILT_UPSIDE_DOWN) {
                                //逆さま姿勢でボタン操作がをしたときは WB Modeを変更しないようにするためのフラグ設定
                                occurUpsideDownButtonPress = true;

                                //タイマー On/Off 切り替え
                                new ChangeExposureDelayTask(consumerAfterButtonExec, ChangeExposureDelayTask.EXPOSURE_DELAY_TGGLE).execute();
                            } else {
                                //パラメータ－方向
                                onParamUpDownReq = PARAM_DOWN;
                            }
                        }

                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {

                switch (keyCode) {
                    case KeyReceiver.KEYCODE_WLAN_ON_OFF:
                        onKeyLongPressWlan=true;

                        break;
                    case KeyEvent.KEYCODE_FUNCTION :
                        onKeyLongPressFn=true;

                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //プラグイン起動時の情報を保存
        new GetCameraStatusTask(mSaveRestoreData).execute();

        //前回起動時に保存した情報を読む
        restorePluginInfo();

        //restoreした情報を元に状態復帰する
        new ChangeCaptureModeTask(consumerAfterButtonExec,  displayInfo.captureMode).execute();
        new ChangeExposureDelayTask(consumerAfterButtonExec, displayInfo.exposureDelay).execute();
        new ChangeExpProgTask(consumerAfterButtonExec, displayInfo.expProgApi2Disp, displayInfo.strExpProg).execute();

        if ( displayInfo.strExpProg.equals("1") ) { //MANU
            new ChangeApertureTask(consumerAfterButtonExec, displayInfo.strAv).execute();
            new ChangeShutterSpeedTask(consumerAfterButtonExec, displayInfo.strTv).execute();
            new ChangeIsoTask(consumerAfterButtonExec, displayInfo.strIso).execute();

        } else if ( displayInfo.strExpProg.equals("2") ) {  //AUTO
            new ChangeEvTask(consumerAfterButtonExec, displayInfo.strExpComp).execute();
            if ( displayInfo.captureMode.equals("image") ) {
                new ChangeFilterTask(consumerAfterButtonExec, displayInfo.strFilter).execute();
            }

        } else if ( displayInfo.strExpProg.equals("3") ) {  //Av
            new ChangeEvTask(consumerAfterButtonExec, displayInfo.strExpComp).execute();
            new ChangeApertureTask(consumerAfterButtonExec, displayInfo.strAv).execute();

        } else if ( displayInfo.strExpProg.equals("4") ) {  //Tv
            new ChangeEvTask(consumerAfterButtonExec, displayInfo.strExpComp).execute();
            new ChangeShutterSpeedTask(consumerAfterButtonExec, displayInfo.strTv).execute();

        } else if ( displayInfo.strExpProg.equals("9") ) {  //ISO
            new ChangeEvTask(consumerAfterButtonExec, displayInfo.strExpComp).execute();
            new ChangeIsoTask(consumerAfterButtonExec, displayInfo.strIso).execute();

        }
        new ChangeWbTask(consumerAfterButtonExec, displayInfo.strWb, displayInfo.strColorTemperature).execute();

        //スレッド開始
        mFinished = false;
        drawOledThread();
    }

    @Override
    protected void onPause() {
        // Do end processing

        //スレッドを終わらせる指示。終了待ちしていません。
        mFinished = true;

        //次回起動時のために必要な情報を保存
        savePluginInfo();

        //プラグイン起動前の状態に復旧する
        new ChangeCaptureModeTask(consumerAfterButtonExec,  restoreCaptureMode).execute();
        new ChangeExposureDelayTask(consumerAfterButtonExec, restoreExposureDelay).execute();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    //==============================================================
    // スレッド (定常ループ)
    //==============================================================
    public void drawOledThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //ループ手前でやることがあるならば・・・

                //描画ループ
                while (mFinished == false) {
                    //ステータスチェックと描画
                    try {
                        //姿勢情報取得&姿勢関連情報更新
                        displayInfo.setTiltStatus(attitude.getDegAzimath(), attitude.getDegPitch(), attitude.getDegRoll());

                        //内部状態獲得
                        new GetCameraStatusTask(mGetCameraStatusCallback).execute();

                        // OLED表示
                        displayInfo.displayTiltUI();

                        // 逆さま戻しあり && WB変更可能 -> Preset <-> CT 切り替え
                        if ( displayInfo.lastEvent == DisplayInfo.EVENT_UPSIDE_DOWN ) {
                            displayInfo.lastEvent = DisplayInfo.EVENT_NON;
                            if ( displayInfo.chgParam == DisplayInfo.CHG_PARAM_WB ) {

                                if( occurUpsideDownButtonPress ) {
                                    occurUpsideDownButtonPress = false;
                                } else {
                                    if ( displayInfo. strWb.equals( ChangeWbTask.CT )  ) {
                                        new ChangeWbTask(consumerAfterButtonExec, displayInfo.strLastPresetWb, "").execute();
                                    } else {
                                        new ChangeWbTask(consumerAfterButtonExec, ChangeWbTask.CT, "").execute();
                                    }
                                }

                            }
                        }

                        //捻り操作検出時の処理
                        switch ( displayInfo.lastEvent ) {
                            case DisplayInfo.EVENT_TWIST_R :
                                displayInfo.lastEvent =DisplayInfo.EVENT_NON;

                                if ( displayInfo.captureMode.equals("image") ) {
                                    displayInfo.stillFormat = DisplayInfo.STILL_FORMAT_RAW_P;
                                    new ChangeFormatTask(consumerAfterButtonExec, "raw+").execute();
                                }
                                break;
                            case DisplayInfo.EVENT_TWIST_L :
                                displayInfo.lastEvent =DisplayInfo.EVENT_NON;

                                if ( displayInfo.captureMode.equals("image") ) {
                                    displayInfo.stillFormat = DisplayInfo.STILL_FORMAT_JPEG;
                                    new ChangeFormatTask(consumerAfterButtonExec, "jpeg").execute();
                                }
                                break;
                        }

                        //長押し中の要求
                        if ( onKeyLongPressWlan ) {
                            onParamUpDownReq=PARAM_UP;
                        }
                        if ( onKeyLongPressFn ) {
                            onParamUpDownReq=PARAM_DOWN;
                        }
                        //パラメータ変更実行
                        if( !onParamUpDownReq.equals("") ) {
                            if (runningChgParam) {
                                //前の処理実行中なら無処理
                                onParamUpDownReq = "";
                            } else {
                                //実行中でなかったら
                                runningChgParam=true;
                                changeParam(onParamUpDownReq);
                                onParamUpDownReq = "";
                            }
                       }

                        //もろもろが高頻度になりすぎないようスリープする
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Deal with error.
                        e.printStackTrace();
                    } finally {
                        //
                    }

                }
            }
        }).start();
    }

    void changeParam(String inUpDown) {
        if ( (inUpDown.equals(PARAM_UP) || inUpDown.equals(PARAM_DOWN) ) ) {
            switch (displayInfo.chgParam) {
                case DisplayInfo.CHG_PARAM_EV :
                    new ChangeEvTask(consumerAfterButtonExec, inUpDown).execute();
                    break;
                case DisplayInfo.CHG_PARAM_FNO :
                    new ChangeApertureTask(consumerAfterButtonExec, inUpDown).execute();
                    break;
                case DisplayInfo.CHG_PARAM_SS :
                    new ChangeShutterSpeedTask(consumerAfterButtonExec, inUpDown).execute();
                    break;
                case DisplayInfo.CHG_PARAM_ISO :
                    new ChangeIsoTask(consumerAfterButtonExec, inUpDown).execute();
                    break;
                case DisplayInfo.CHG_PARAM_WB :
                    if ( displayInfo. strWb.equals( ChangeWbTask.CT )  ) {
                        new ChangeWbTask(consumerAfterButtonExec, ChangeWbTask.CT, inUpDown).execute();
                    } else {
                        new ChangeWbTask(consumerAfterButtonExec, inUpDown, "").execute();
                    }
                    break;
                case DisplayInfo.CHG_PARAM_OPT :
                    new ChangeFilterTask(consumerAfterButtonExec, inUpDown).execute();
                    break;
                default:
                    //無処理
            }
        }
    }
    //==============================================================
    // 設定保存・復帰
    //==============================================================
    private static final String SAVE_KEY_M_SHOOTING  = "multipleShooting";
    private static final String SAVE_KEY_EXP_DELAY  = "exposureDelay";
    private static final String SAVE_KEY_CAP_MODE  = "captureMode";
    private static final String SAVE_KEY_EXP_PROG  = "strExpProg";
    private static final String SAVE_KEY_AV  = "strAv";
    private static final String SAVE_KEY_TV  = "strTv";
    private static final String SAVE_KEY_ISO  = "strIso";
    private static final String SAVE_KEY_EXP_COMP  = "strExpComp";
    private static final String SAVE_KEY_WB  = "strWb";
    private static final String SAVE_KEY_WB_LAST  = "strLastPresetWb";
    private static final String SAVE_KEY_WB_CT  = "strColorTemperature";
    private static final String SAVE_KEY_FILTER  = "strFilter";
    private static final String SAVE_KEY_FILTER_LAST  = "strLastFilter";
    private static final String SAVE_KEY_CHG_PARAM  = "chgParam";

    SharedPreferences sharedPreferences;

    void restorePluginInfo() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        displayInfo.multipleShooting = sharedPreferences.getInt(SAVE_KEY_M_SHOOTING, DisplayInfo.M_SHOOT_SINGLE);
        displayInfo.exposureDelay = sharedPreferences.getInt(SAVE_KEY_EXP_DELAY,0);
        displayInfo.captureMode = sharedPreferences.getString(SAVE_KEY_CAP_MODE, "image");
        displayInfo.strExpProg = sharedPreferences.getString(SAVE_KEY_EXP_PROG, "2"); //=auto
        displayInfo.strbeforExpProg = displayInfo.strExpProg;

        displayInfo.strAv                = sharedPreferences.getString(SAVE_KEY_AV          , "2.1");
        displayInfo.strTv                = sharedPreferences.getString(SAVE_KEY_TV          , "0.01666666"); //=1/60
        displayInfo.strIso               = sharedPreferences.getString(SAVE_KEY_ISO         , "100");
        displayInfo.strExpComp           = sharedPreferences.getString(SAVE_KEY_EXP_COMP    , "0.0");
        displayInfo.strWb                = sharedPreferences.getString(SAVE_KEY_WB          , "auto");
        displayInfo.strLastPresetWb      = sharedPreferences.getString(SAVE_KEY_WB_LAST     , "auto");
        displayInfo.strColorTemperature  = sharedPreferences.getString(SAVE_KEY_WB_CT       , "5000");
        displayInfo.strFilter            = sharedPreferences.getString(SAVE_KEY_FILTER      , "off");
        displayInfo.strLastFilter        = sharedPreferences.getString(SAVE_KEY_FILTER_LAST , "off");

        displayInfo.chgParam = sharedPreferences.getInt(SAVE_KEY_CHG_PARAM , DisplayInfo.CHG_PARAM_EV);
    }

    void savePluginInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SAVE_KEY_M_SHOOTING, displayInfo.multipleShooting);
        editor.putInt(SAVE_KEY_EXP_DELAY, displayInfo.exposureDelay);
        editor.putString(SAVE_KEY_CAP_MODE, displayInfo.captureMode);
        editor.putString(SAVE_KEY_EXP_PROG, displayInfo.strExpProg);

        editor.putString(SAVE_KEY_AV          , displayInfo.strAv               );
        editor.putString(SAVE_KEY_TV          , displayInfo.strTv               );
        editor.putString(SAVE_KEY_ISO         , displayInfo.strIso              );
        editor.putString(SAVE_KEY_EXP_COMP    , displayInfo.strExpComp          );
        editor.putString(SAVE_KEY_WB          , displayInfo.strWb               );
        editor.putString(SAVE_KEY_WB_LAST     , displayInfo.strLastPresetWb     );
        editor.putString(SAVE_KEY_WB_CT       , displayInfo.strColorTemperature );
        editor.putString(SAVE_KEY_FILTER      , displayInfo.strFilter           );
        editor.putString(SAVE_KEY_FILTER_LAST , displayInfo.strLastFilter       );

        editor.putInt(SAVE_KEY_CHG_PARAM , displayInfo.chgParam);

        editor.commit();
    }

}
