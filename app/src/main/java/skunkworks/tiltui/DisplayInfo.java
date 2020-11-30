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


import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.theta360.pluginapplication.oled.Oled;


public class DisplayInfo extends Oled {
    private static final String TAG = "DisplayInfo";

    public boolean busy;
    public int recordedTime;
    public int compositeShootingElapsedTime;

    public final static int BRIGHTNESS_LIMIT_MIN = 3;
    public final static int BRIGHTNESS_LIMIT_MAX = 100;
    public final static int BRIGHTNESS_BASE = 50;

    public final static int M_SHOOT_SINGLE = 0;
    public final static int M_SHOOT_TIMESHIFT = 1;
    public final static int M_SHOOT_INTERVAL = 2;
    public final static int M_SHOOT_INT_COMP = 3;
    public int multipleShooting;

    public int exposureDelay;
    public String captureMode;
    public String strbeforExpProg;
    public String strExpProg;
    public String strAv;
    public String strTv;
    public String strIso;
    public String strExpComp;
    public String strWb;
    public String strLastPresetWb;
    public String strColorTemperature;
    public String strFilter;
    public String strLastFilter;

    public final static int STILL_FORMAT_JPEG = 0;
    public final static int STILL_FORMAT_RAW_P = 1;
    public int stillFormat;


    public double beforDegAzimath;
    public double degAzimath;
    public double degPitch ;
    public double degRoll;

    public final static int TILT_ERROR = -1;
    public final static int TILT_BASE = 0;
    public final static int TILT_FRONT = 1;
    public final static int TILT_BACK = 2;
    public final static int TILT_RIGHT = 3;
    public final static int TILT_LEFT = 4;
    public final static int TILT_UPSIDE_DOWN = 5;
    public int curTilt;
    public int beforTilt;
    private boolean occurUpsideDown = false;

    private final static double TWIST_THRESH = 90.0;

    public final static int EVENT_NON = 0;
    public final static int EVENT_FRONT = 1;
    public final static int EVENT_BACK = 2;
    public final static int EVENT_RIGHT = 3;
    public final static int EVENT_LEFT = 4;
    public final static int EVENT_UPSIDE_DOWN = 5;
    public final static int EVENT_TWIST_R = 6;
    public final static int EVENT_TWIST_L = 7;
    public int lastEvent;

    public final static int CHG_PARAM_EV = 0;
    public final static int CHG_PARAM_FNO = 1;
    public final static int CHG_PARAM_SS = 2;
    public final static int CHG_PARAM_ISO = 3;
    public final static int CHG_PARAM_WB = 4;
    public final static int CHG_PARAM_OPT = 5;
    public int chgParam;

    private Map<String, String> expProgApi2Filename;
    private Map<String, String> wbApi2Filename;
    public Map<String, String> expProgApi2Disp;
    private Map<String, String> filterApi2Disp;

    private Map<String, Integer>  fnoApi2Bright;
    private Map<String, Integer>  ssApi2Bright;
    private Map<String, Integer>  isoApi2Bright;


    private final Context context;

    public DisplayInfo (Context context) {
        super(context);
        this.context = context;

        busy = false;
        recordedTime = 0;
        compositeShootingElapsedTime=0;
        multipleShooting = M_SHOOT_SINGLE;
        captureMode = "";
        exposureDelay = 0;
        strExpProg = "2";
        strbeforExpProg = strExpProg;
        strAv = "0";
        strTv = "0";
        strIso = "0";
        strExpComp = "0.0";
        strWb = "auto";
        strLastPresetWb = strWb;
        strColorTemperature = "5000";
        strFilter = "off";
        strLastFilter = strFilter;
        stillFormat = STILL_FORMAT_JPEG;


        wbApi2Filename = new HashMap<String, String>();
        wbApi2Filename.put("auto"                 , "wb0_auto.bmp"    );
        wbApi2Filename.put("daylight"             , "wb1_daylight.bmp");
        wbApi2Filename.put("shade"                , "wb2_shade.bmp"   );
        wbApi2Filename.put("cloudy-daylight"      , "wb3_cloudy.bmp"  );
        wbApi2Filename.put("incandescent"         , "wb4_lamp1.bmp"   );
        wbApi2Filename.put("_warmWhiteFluorescent", "wb5_lamp2.bmp"   );
        wbApi2Filename.put("_dayLightFluorescent" , "wb6_fluo1.bmp"   );
        wbApi2Filename.put("_dayWhiteFluorescent" , "wb7_fluo2.bmp"   );
        wbApi2Filename.put("fluorescent"          , "wb8_fluo3.bmp"   );
        wbApi2Filename.put("_bulbFluorescent"     , "wb9_fluo4.bmp"   );
        wbApi2Filename.put("_underwater"          , "wb10_underwater.bmp");

        expProgApi2Filename = new HashMap<String, String>();
        expProgApi2Filename.put("1", "TiltUI_1_MANU.bmp");
        expProgApi2Filename.put("2", "TiltUI_2_Auto.bmp");
        expProgApi2Filename.put("3", "TiltUI_3_Av.bmp");
        expProgApi2Filename.put("4", "TiltUI_4_Tv.bmp");
        expProgApi2Filename.put("9", "TiltUI_9_ISO.bmp");

        expProgApi2Disp = new HashMap<String, String>();
        expProgApi2Disp.put("1", "MANU");
        expProgApi2Disp.put("2", "AUTO");
        expProgApi2Disp.put("3", " Av ");
        expProgApi2Disp.put("4", " Tv ");
        expProgApi2Disp.put("9", "ISO ");

        filterApi2Disp = new HashMap<String, String>();
        filterApi2Disp.put("off", "off");
        filterApi2Disp.put("DR Comp", "DR Comp");
        filterApi2Disp.put("Noise Reduction", "NR");
        filterApi2Disp.put("hdr", "HDR");
        filterApi2Disp.put("Hh hdr", "Hh HDR");

        fnoApi2Bright = new HashMap<String, Integer>();
        fnoApi2Bright.put("5.6", 7);
        fnoApi2Bright.put("3.5", 3);
        fnoApi2Bright.put("2.1", 0);

        ssApi2Bright = new HashMap<String, Integer>();
        ssApi2Bright.put("4.0E-5"    , 26);
        ssApi2Bright.put("5.0E-5"    , 25);
        ssApi2Bright.put("6.25E-5"   , 24);
        ssApi2Bright.put("8.0E-5"    , 23);
        ssApi2Bright.put("1.0E-4"    , 22);
        ssApi2Bright.put("1.25E-4"   , 21);
        ssApi2Bright.put("1.5625E-4" , 20);
        ssApi2Bright.put("2.0E-4"    , 19);
        ssApi2Bright.put("2.5E-4"    , 18);
        ssApi2Bright.put("3.125E-4"  , 17);
        ssApi2Bright.put("4.0E-4"    , 16);
        ssApi2Bright.put("5.0E-4"    , 15);
        ssApi2Bright.put("6.25E-4"   , 14);
        ssApi2Bright.put("8.0E-4"    , 13);
        ssApi2Bright.put("0.001"     , 12);
        ssApi2Bright.put("0.00125"   , 11);
        ssApi2Bright.put("0.0015625" , 10);
        ssApi2Bright.put("0.002"     , 9);
        ssApi2Bright.put("0.0025"    , 8);
        ssApi2Bright.put("0.003125"  , 7);
        ssApi2Bright.put("0.004"     , 6);
        ssApi2Bright.put("0.005"     , 5);
        ssApi2Bright.put("0.00625"   , 4);
        ssApi2Bright.put("0.008"     , 3);
        ssApi2Bright.put("0.01"      , 2);
        ssApi2Bright.put("0.0125"    , 1);
        ssApi2Bright.put("0.01666666", 0); // = 1/60
        ssApi2Bright.put("0.02"      , -1);
        ssApi2Bright.put("0.025"     , -2);
        ssApi2Bright.put("0.03333333", -3);
        ssApi2Bright.put("0.04"      , -4);
        ssApi2Bright.put("0.05"      , -5);
        ssApi2Bright.put("0.06666666", -6);
        ssApi2Bright.put("0.07692307", -7);
        ssApi2Bright.put("0.1"       , -8);
        ssApi2Bright.put("0.125"     , -9);
        ssApi2Bright.put("0.16666666", -10);
        ssApi2Bright.put("0.2"       , -11);
        ssApi2Bright.put("0.25"      , -12);
        ssApi2Bright.put("0.33333333", -13);
        ssApi2Bright.put("0.4"       , -14);
        ssApi2Bright.put("0.5"       , -15);
        ssApi2Bright.put("0.625"     , -16);
        ssApi2Bright.put("0.76923076", -17);
        ssApi2Bright.put("1.0"       , -18);
        ssApi2Bright.put("1.3"       , -19);
        ssApi2Bright.put("1.6"       , -20);
        ssApi2Bright.put("2.0"       , -21);
        ssApi2Bright.put("2.5"       , -22);
        ssApi2Bright.put("3.2"       , -23);
        ssApi2Bright.put("4.0"       , -24);
        ssApi2Bright.put("5.0"       , -25);
        ssApi2Bright.put("6.0"       , -26);
        ssApi2Bright.put("8.0"       , -27);
        ssApi2Bright.put("10.0"      , -28);
        ssApi2Bright.put("13.0"      , -29);
        ssApi2Bright.put("15.0"      , -30);
        ssApi2Bright.put("20.0"      , -31);
        ssApi2Bright.put("25.0"      , -32);
        ssApi2Bright.put("30.0"      , -33);
        ssApi2Bright.put("60.0"      , -36);

        isoApi2Bright = new HashMap<String, Integer>();
        isoApi2Bright.put("80"   , 1);
        isoApi2Bright.put("100"  , 0);
        isoApi2Bright.put("125"  , -1);
        isoApi2Bright.put("160"  , -2);
        isoApi2Bright.put("200"  , -3);
        isoApi2Bright.put("250"  , -4);
        isoApi2Bright.put("320"  , -5);
        isoApi2Bright.put("400"  , -6);
        isoApi2Bright.put("500"  , -7);
        isoApi2Bright.put("640"  , -8);
        isoApi2Bright.put("800"  , -9);
        isoApi2Bright.put("1000" , -10);
        isoApi2Bright.put("1250" , -11);
        isoApi2Bright.put("1600" , -12);
        isoApi2Bright.put("2000" , -13);
        isoApi2Bright.put("2500" , -14);
        isoApi2Bright.put("3200" , -15);
        isoApi2Bright.put("4000" , -16);
        isoApi2Bright.put("5000" , -17);
        isoApi2Bright.put("6400" , -18);


    }

    void setTiltStatus(double inYaw, double inPitch, double inRoll) {
        beforDegAzimath = degAzimath;
        degAzimath = inYaw;
        degPitch = inPitch;
        degRoll = inRoll;

        beforTilt = curTilt;
        if ( (-45 <= degPitch) && (degPitch<45) ) {
            if ( (-45<=degRoll) && (degRoll<=45) ) {
                curTilt = TILT_BASE;
            } else if ( (45<=degRoll) && (degRoll<=135) ) {
                curTilt = TILT_RIGHT;
            } else if ( (-135<=degRoll) && (degRoll<=-45) ) {
                curTilt = TILT_LEFT;
            } else if ( ((135<=degRoll) && (degRoll<=180)) ||
                    ( (-180<=degRoll) && (degRoll<=-135) ) ) {
                curTilt = TILT_UPSIDE_DOWN;
                occurUpsideDown=true;
            } else {
                curTilt = TILT_ERROR;
            }
        } else if ( (45 <= degPitch) && (degPitch<=90) ) {
            curTilt = TILT_FRONT;
        } else if ( (-90 <= degPitch) && (degPitch<=-45) ) {
            curTilt = TILT_BACK;
        } else {
            curTilt = TILT_ERROR;
        }

        //捻りイベントチェック
        if ( curTilt == TILT_BASE ) {
            double diffAzimath = beforDegAzimath-degAzimath;
            if ( diffAzimath >= TWIST_THRESH ) {
                lastEvent = EVENT_TWIST_L;
            } else if ( diffAzimath <= -1.0*TWIST_THRESH) {
                lastEvent = EVENT_TWIST_R;
            } else {
                //捻りイベントはなし
            }
        }

        //傾きイベントチェック
        boolean occurChangeEvent = false;
        if ( (curTilt==TILT_BASE)&&(beforTilt!=TILT_BASE) ) {
            occurChangeEvent = true;

            if (occurUpsideDown) {
                occurUpsideDown=false;
                lastEvent =EVENT_UPSIDE_DOWN;
            } else {
                switch (beforTilt) {
                    case TILT_FRONT :
                        lastEvent =EVENT_FRONT;
                        break;
                    case TILT_BACK :
                        lastEvent =EVENT_BACK;
                        break;
                    case TILT_RIGHT :
                        lastEvent =EVENT_RIGHT;
                        break;
                    case TILT_LEFT :
                        lastEvent =EVENT_LEFT;
                        break;
                    default:
                        //無処理:起こらないはずが念のため
                        break;
                }
            }

            // 変更可能パラメータの遷移
            if ( occurChangeEvent ) {
                switch (lastEvent) {
                    case EVENT_FRONT :
                        if ( strExpProg.equals("1") ) {         // - MANU -
                            chgParam = CHG_PARAM_SS;
                        } else if ( strExpProg.equals("2") ) {  // - AUTO -
                            if ( captureMode.equals("image") ) {
                                chgParam = CHG_PARAM_OPT;
                            }
                        } else if ( strExpProg.equals("3") ) {  // - Av -
                            chgParam = CHG_PARAM_FNO;
                        } else if ( strExpProg.equals("4") ) {  // - Tv -
                            chgParam = CHG_PARAM_SS;
                        } else if ( strExpProg.equals("9") ) {  // - ISO -
                            chgParam = CHG_PARAM_ISO;
                        }
                        break;
                    case EVENT_BACK :
                        if ( strExpProg.equals("1") ) {         // - MANU -
                            chgParam = CHG_PARAM_WB;
                        }
                        break;
                    case EVENT_RIGHT :
                        if ( strExpProg.equals("1") ) {         // - MANU -
                            chgParam = CHG_PARAM_ISO;
                        } else {
                            chgParam = CHG_PARAM_WB;
                        }
                        break;
                    case EVENT_LEFT :
                        if ( strExpProg.equals("1") ) {         // - MANU -
                            chgParam = CHG_PARAM_FNO;
                        } else {
                            chgParam = CHG_PARAM_EV;
                        }
                        break;
                }
            }


        }

    }



    void setBrightness() {

        int setBrightness = BRIGHTNESS_BASE;
        if ( strExpProg.equals("1") ) { //MANU
            if ( fnoApi2Bright.containsKey(strAv) ) {
                setBrightness += fnoApi2Bright.get(strAv);
            } else {
                Log.d(TAG, "undifined strAv=" + strAv);
            }
            if ( ssApi2Bright.containsKey(strTv) ) {
                setBrightness += ssApi2Bright.get(strTv);
            } else {
                Log.d(TAG, "undifined strTv=" + strTv);
            }
            if ( isoApi2Bright.containsKey(strIso) ) {
                setBrightness += isoApi2Bright.get(strIso);
            } else {
                Log.d(TAG, "undifined strIso=" + strIso);
            }

        }

        if( setBrightness < BRIGHTNESS_LIMIT_MIN ){
            setBrightness = BRIGHTNESS_LIMIT_MIN;
        }
        if( setBrightness > BRIGHTNESS_LIMIT_MAX ){
            setBrightness = BRIGHTNESS_LIMIT_MAX;
        }
        Log.d(TAG, "setBrightness=" + String.valueOf(setBrightness));
        brightness(setBrightness);
    }

    void displayTiltUI() {
        //clear();
        setBrightness();

        if ( expProgApi2Filename.containsKey(strExpProg) ) {
            setBitmap(0, 0, 128, 24,  0, 0, 128, expProgApi2Filename.get(strExpProg) );
        } else {
            setBitmap(0, 0, 128, 24,  0, 0, 128, "TiltUI_0_ERROR.bmp" );
        }

        if ( captureMode.equals("video") ) {
            setBitmap(1, 0, 12, 8,  0, 0, 128, "capmode2_move.bmp" );
        } else {
            setBitmap(1, 0, 12, 8,  0, 0, 128, "capmode1_still.bmp" );
        }

        if ( exposureDelay == 0 ) {
            //空欄のまま
        } else {
            setBitmap(14, 0, 8, 8,  0, 0, 128, "timer.bmp" );
        }

        //連続撮影表示
        if ( captureMode.equals("image") ) {
            switch (multipleShooting) {
                case M_SHOOT_TIMESHIFT :
                    setBitmap(14, 0, 8, 8,  0, 0, 128, "timeshift.bmp" );
                    break;
                case M_SHOOT_INTERVAL :
                    setBitmap(22, 0, 12, 8,  0, 0, 128, "Int.bmp" );
                    break;
                case M_SHOOT_INT_COMP :
                    setBitmap(22, 0, 16, 8,  0, 0, 128, "IntComp.bmp" );
                    break;
                default:
                    break;
            }
        }

        // JPEG or RAW+ 表示
        if ( (stillFormat == STILL_FORMAT_RAW_P) && captureMode.equals("image") ) {
            setBitmap(105, 2, 22, 8,  0, 0, 128, "RAW+.bmp" );
        }

        displayTiltGide();
        displayChgParam();

        //BUSY中 ダイアログ表示
        if (busy) {
            int diarogPosX = 29;
            int diarogPosY = 6;
            int dalogWidth = (FONT_WIDTH * 14) + 3*2;
            int dialogHeight = FONT_HEIGHT + 3*2 + 1;
            String messageStr = "";

            if ( captureMode.equals("video") ) {
                int min = recordedTime/60;
                int sec = recordedTime%60;
                messageStr  = "  REC  " + String.format("%02d", min) + ":" + String.format("%02d", sec)  ;
            } else {
                switch (multipleShooting) {
                    case M_SHOOT_INT_COMP :
                        int hour = compositeShootingElapsedTime/3600;
                        int min = (compositeShootingElapsedTime%3600)/60;
                        int sec = compositeShootingElapsedTime%60;
                        messageStr  = "   "+ String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec)  ;
                        break;
                    case M_SHOOT_INTERVAL :
                    case M_SHOOT_TIMESHIFT :
                        messageStr  = " in progress. ";
                        break;
                    default:
                        break;
                }
            }

            if ( !messageStr.equals("") ) {
                rectFill(diarogPosX, diarogPosY, dalogWidth, dialogHeight, black, false);
                rect(diarogPosX+1,diarogPosY+1,dalogWidth-2, dialogHeight-2);
                setString( diarogPosX+3, diarogPosY+3, messageStr);

            }
        }

        draw();
    }

    void displayTiltGide() {

        if ( strExpProg.equals("1") ) {         // - MANU -
            setString(45,   0, "SS" );  // F
            setString(25,   8, "Fno" ); // L
            setString(57,   8, "ISO" ); // R
            setString(44,  16, "WB" );  // B
        } else if ( strExpProg.equals("2") ) {  // - AUTO -
            if ( captureMode.equals("image") ) {
                setString(42,   0, "Opt" ); // F
            }
            setString(30,   8, "EV" );  // L
            setString(58,   8, "WB" );  // R
        } else if ( strExpProg.equals("3") ) {  // - Av -
            setString(42,   0, "Fno" ); // F
            setString(30,   8, "EV" );  // L
            setString(58,   8, "WB" );  // R
        } else if ( strExpProg.equals("4") ) {  // - Tv -
            setString(45,   0, "SS" );  // F
            setString(30,   8, "EV" );  // L
            setString(58,   8, "WB" );  // R
        } else if ( strExpProg.equals("9") ) {  // - ISO -
            setString(42,   0, "ISO" ); // F
            setString(30,   8, "EV" );  // L
            setString(58,   8, "WB" );  // R
        } else {
            //無処理
        }

    }

    void displayChgParam() {
        switch (chgParam) {
            case CHG_PARAM_EV :
                setString(81, 4, "EV" );
                setString(81, 12, convertExpComp() );
                break;
            case CHG_PARAM_FNO :
                setString(81, 4, "Fno" );
                setString(81, 12, convertAv() );
                break;
            case CHG_PARAM_SS :
                setString(81, 4, "SS" );
                setString(81, 12, convertTv() );
                break;
            case CHG_PARAM_ISO :
                setString(81, 4, "ISO" );
                setString(81, 12, convertIso() );
                break;
            case CHG_PARAM_WB :
                setString(81, 4, "WB" );
                if ( wbApi2Filename.containsKey(strWb) ) {
                    setBitmap(81, 12, 24, 8,  0, 0, 128, wbApi2Filename.get(strWb) );
                } else {
                    if ( strWb.equals("_colorTemperature") ) {
                        setString(81, 12, strColorTemperature + "K");
                    } else {
                        setString(81, 12, "Undef");
                    }
                }
                break;
            case CHG_PARAM_OPT ://_filter
                setString(81, 4, "Opt" );
                setString(81, 12, filterApi2Disp.get(strFilter) );

                break;
            default:
                //無処理
        }
    }

    String convertExpProg(){
        String result = "";
        if ( expProgApi2Disp.containsKey(strExpProg) ) {
            result = expProgApi2Disp.get(strExpProg);
        } else {
            result = "ERR ";
        }
        return result;
    }

    String convertAv(){
        String result = "";
        double nowAv = Double.parseDouble(strAv);
        if ( nowAv == 0.0 ) {
            result = "---";
        } else {
            result = strAv;
        }
        return result;
    }

    String convertTv(){
        String result = "";
        double nowSs = Double.parseDouble(strTv);

        if ( nowSs == 0.0 ) {
            result = "------";
        } else if ( nowSs < 1.0 ) {
            String ssStr;
            double denominator = 1.0 / nowSs;
            // 0.4 (1/2.5), 0.625 (1/1.6), 0.76923076 (1/1.3) は小数点下位1桁表示
            if ( (nowSs==0.4) || (nowSs==0.625) || (nowSs==0.76923076) ) {
                ssStr = String.format("%.1f", denominator );
            } else {
                ssStr = String.format("%.0f", denominator );
            }
            result = "1/" + ssStr;
        } else {
            String ssStr;
            //1.3, 1.6, 2.5, 3.2 は小数点下位1桁表示
            if ( (nowSs==1.3) || (nowSs==1.6) || (nowSs==2.5) || (nowSs==3.2) ) {
                ssStr = String.format("%.1f", nowSs );
            } else {
                ssStr = String.format("%.0f", nowSs );
            }
            result = ssStr + "\"";
        }

        return result;
    }

    String convertIso(){
        String result = "";
        if ( strIso.equals("0") ) {
            result = "----";
        } else {
            result = strIso;
        }

        return result;
    }

    String convertExpComp() {
        String result = "";
        if ( strExpProg.equals("1") ) {
            result = "----";
        } else {
            if ( Double.parseDouble(strExpComp) >= 0.0 ){
                result = " " + strExpComp;
            } else {
                result = strExpComp;
            }
        }

        return result;
    }


}
