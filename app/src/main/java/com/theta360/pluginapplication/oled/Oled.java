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

package com.theta360.pluginapplication.oled;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.*;
import android.util.Log;

import java.io.InputStream;
import java.io.IOException;


public class Oled  {
    private final Context context;

    private static final String ACTION_OLED_DISPLAY_SET = "com.theta360.plugin.ACTION_OLED_DISPLAY_SET";
    private static final String ACTION_OLED_BRIGHTNESS_SET = "com.theta360.plugin.ACTION_LED_BRIGHTNESS_SET";
    private static final String ACTION_OLED_IMAGE_SHOW = "com.theta360.plugin.ACTION_OLED_IMAGE_SHOW";
    private static final String ACTION_OLED_IMAGE_BLINK = "com.theta360.plugin.ACTION_OLED_IMAGE_BLINK";
    private static final String ACTION_OLED_HIDE = "com.theta360.plugin.ACTION_OLED_HIDE";

    public static final String DISPLAY_SET_PLUGIN = "plug-in";
    public static final String DISPLAY_SET_BASIC = "basic";

    public static final int OLED_WIDTH = 128;
    public static final int OLED_HEIGHT = 24;

    public int black = 0xFF000000 ;
    public int white = 0xFFFFFFFF ;

    private Bitmap screen = null;
    public int imgWidth = 0;
    public int imgHeight = 0;

    public Oled (Context context) {
        this.context = context;
        screen = Bitmap.createBitmap(OLED_WIDTH, OLED_HEIGHT, Bitmap.Config.ARGB_8888 );
    }

    public void displaySet(String mode) {
        Intent oledIntentSet = new Intent(ACTION_OLED_DISPLAY_SET);
        if ( mode.equals(DISPLAY_SET_PLUGIN) ) {
            oledIntentSet.putExtra("display", DISPLAY_SET_PLUGIN);
        } else {
            oledIntentSet.putExtra("display", DISPLAY_SET_BASIC);
        }
        this.context.sendBroadcast(oledIntentSet);
    }

    public void brightness (int value) {
        if (value<0) {
            value = 0;
        }
        if (value>100) {
            value=100;
        }
        Intent oledBrightnessIntent = new Intent(ACTION_OLED_BRIGHTNESS_SET);
        oledBrightnessIntent.putExtra("target", "OLED");
        oledBrightnessIntent.putExtra("brightness",  value);
        context.sendBroadcast(oledBrightnessIntent);
    }
    public void hide () {
        Intent imageIntent = new Intent(ACTION_OLED_HIDE);
        context.sendBroadcast(imageIntent);
    }
    public void blink (int msec) {
        if (msec<250) {
            msec=250;
        }
        if (msec>2000) {
            msec=2000;
        }
        Intent imageIntent = new Intent(ACTION_OLED_IMAGE_BLINK);
        imageIntent.putExtra("bitmap", screen);
        imageIntent.putExtra("period", msec);
        context.sendBroadcast(imageIntent);
    }

    public void draw () {
        Intent imageIntent = new Intent(ACTION_OLED_IMAGE_SHOW);
        imageIntent.putExtra("bitmap", screen);
        context.sendBroadcast(imageIntent);
    }

    public void clear () {
        clear(black);
    }
    public void clear (int color) {
        for (int width=0; width<OLED_WIDTH; width++) {
            for (int height = 0; height < OLED_HEIGHT; height++) {
                screen.setPixel(width, height, color);
            }
        }
    }

    public void invert (boolean invFlag) {
        if (invFlag) {
            Intent imageIntent = new Intent(ACTION_OLED_IMAGE_SHOW);
            Bitmap invertScreen = Bitmap.createBitmap(OLED_WIDTH, OLED_HEIGHT, Bitmap.Config.ARGB_8888 );

            for (int width=0; width<OLED_WIDTH; width++) {
                for (int height = 0; height < OLED_HEIGHT; height++) {
                    if ( screen.getPixel(width, height) == white ) {
                        invertScreen.setPixel(width, height, black);
                    } else {
                        invertScreen.setPixel(width, height, white);
                    }
                }
            }
            imageIntent.putExtra("bitmap", invertScreen);
            context.sendBroadcast(imageIntent);
        } else {
            draw();
        }
    }

    public void setBitmap(int threshold, String assetsFileName) {
        setBitmap(0, 0, OLED_WIDTH, OLED_HEIGHT, 0, 0, threshold, assetsFileName) ;
    }
    public void setBitmap(int threshold, Bitmap inBitmap) {
        setBitmap(0, 0, OLED_WIDTH, OLED_HEIGHT, 0, 0, threshold, inBitmap) ;
    }
    public void setBitmap(int scnX, int scnY, int scnWidth, int scnHeight, int imgX, int imgY, int threshold, String assetsFileName) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        Bitmap fileBitmap = null;

        try {
            inputStream = assetManager.open(assetsFileName);
            fileBitmap = BitmapFactory.decodeStream(inputStream);

            setBitmap(scnX, scnY, scnWidth, scnHeight, imgX, imgY, threshold, fileBitmap) ;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setBitmap(int scnX, int scnY, int scnWidth, int scnHeight, int imgX, int imgY, int threshold, Bitmap inBitmap) {
        int xStart;
        int xEnd;
        int yStart;
        int yEnd;
        int scnOffsetX=scnX;
        int scnOffsetY=scnY;

        if (    ( (0<=scnX) && (scnX<OLED_WIDTH) ) && ( (0<scnWidth) || ((scnX+scnWidth)<=OLED_WIDTH) ) &&
                ( (0<=scnY) && (scnY<OLED_HEIGHT) ) && ( (0<scnHeight) || ((scnY+scnHeight)<=OLED_HEIGHT) )  )
        {
            xStart = scnX;
            xEnd = scnX+scnWidth;
            yStart = scnY;
            yEnd = scnY+scnHeight;
        } else {
            return;
        }

        imgWidth = inBitmap.getWidth();
        imgHeight = inBitmap.getHeight();

        int imgOffsetX = 0;
        int imgOffsetY = 0;
        if (    ( (0<=imgX) && (imgX<imgWidth) ) && ((imgX+scnWidth)<=imgWidth) &&
                ( (0<=imgY) && (imgY<imgHeight) ) && ((imgY+scnHeight)<=imgHeight) )
        {
            imgOffsetX = imgX;
            imgOffsetY = imgY;
        } else {
            return ;
        }

        for (int width=xStart; width<xEnd; width++) {
            for (int height=yStart; height<yEnd; height++) {

                int iColor = inBitmap.getPixel(imgOffsetX+(width-scnOffsetX), +imgOffsetY+(height-scnOffsetY) );

                // int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
                //Y =  0.299 x R + 0.587  x G + 0.114  x B
                double dY = 0.299*(iColor&0x000000FF) + 0.587*((iColor&0x0000FF00)>>8) + 0.114*((iColor&0x00FF0000)>>16);
                int Y = (int)(dY+0.5);
                if (Y<0) { Y = 0 ;}
                if (Y>255) { Y = 255 ;}

                if ( Y < threshold ) {
                    screen.setPixel(width, height, black);
                } else {
                    screen.setPixel(width, height, white);
                }
            }
        }

    }

    public void pixel(int x, int y) {
        pixel(x, y, white, false);
    }
    public void pixel(int x, int y, int color, boolean xor) {
        if (    ( (0<=x) && (x<OLED_WIDTH) )  &&
                ( (0<=y) && (y<OLED_HEIGHT) )  )
        {
            if (xor) {
                if ( color == white ) {
                    if ( screen.getPixel(x, y) == white ) {
                        screen.setPixel(x, y, black);
                    } else {
                        screen.setPixel(x, y, white);
                    }
                }
            } else {
                screen.setPixel(x, y, color);
            }
        }
    }

    public void line(int x0, int y0, int x1, int y1) {
        line(x0, y0, x1, y1, white, false);
    }
    public void line(int x0, int y0, int x1, int y1, int color, boolean xor) {
        int tmp ;
        int dx;
        int dy;

        boolean steep = (Math.abs(y1 - y0) > Math.abs(x1 - x0) );
        if ( steep ) {
            //swap x0,y0
            tmp = x0;
            x0 = y0;
            y0 = tmp;

            //swap x1,y1
            tmp = x1;
            x1 = y1;
            y1 = tmp;
        }

        if (x0>x1) {
            //swap x0,x1
            tmp = x0;
            x0 = x1;
            x1 = tmp;

            //swap y0,y1
            tmp = y0;
            y0 = y1;
            y1 = tmp;
        }
        dx = x1 - x0;
        dy = Math.abs(y1 - y0);

        int err = dx/2;
        int ystep ;
        if (y0 < y1) {
            ystep = 1;
        } else {
            ystep = -1;
        }

        int y = y0;
        for (int x=x0; x<x1; x++) {
            if (steep) {
                pixel(y, x, color, xor);
            } else {
                pixel(x, y, color, xor);
            }
            err -= dy;
            if (err < 0) {
                y += ystep;
                err += dx;
            }
        }
    }
    public void lineH(int x, int y, int width) {
        line(x, y,(x+width), y);
    }
    public void lineH(int x, int y, int width, int color, boolean xor) {
        line(x, y,(x+width), y, color, xor);
    }
    public void lineV(int x, int y, int height) {
        line(x, y, x,(y+height));
    }
    public void lineV(int x, int y, int height, int color, boolean xor) {
        line(x, y, x,(y+height), color, xor);
    }

    public void rect(int x, int y, int width, int height) {
        rect(x, y, width, height, white, false);
    }
    public void rect(int x, int y, int width, int height, int color, boolean xor) {
        lineH(x,y, width, color, xor);
        lineH(x,y+height-1, width, color, xor);

        int tempHeight = height-2;
        if ( tempHeight >= 1 ) {
            lineV(x,y+1, tempHeight, color, xor);
            lineV(x+width-1, y+1, tempHeight, color, xor);
        }
    }
    public void rectFill(int x, int y, int width, int height) {
        rectFill(x, y, width, height, white, false);
    }
    public void rectFill(int x, int y, int width, int height, int color, boolean xor) {
        for (int i=y; i<y+height; i++) {
            lineH(x, i, width, color, xor);
        }
    }

    public void circle(int x0, int y0, int radius) {
        circle(x0, y0, radius, white, false);
    }
    public void circle(int x0, int y0, int radius, int color, boolean xor) {
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        pixel(x0, y0+radius, color, xor);
        pixel(x0, y0-radius, color, xor);
        pixel(x0+radius, y0, color, xor);
        pixel(x0-radius, y0, color, xor);

        while ( x < y ) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            pixel(x0 + x, y0 + y, color, xor);
            pixel(x0 - x, y0 + y, color, xor);
            pixel(x0 + x, y0 - y, color, xor);
            pixel(x0 - x, y0 - y, color, xor);

            pixel(x0 + y, y0 + x, color, xor);
            pixel(x0 - y, y0 + x, color, xor);
            pixel(x0 + y, y0 - x, color, xor);
            pixel(x0 - y, y0 - x, color, xor);
        }
    }
    public void circleFill(int x0, int y0, int radius) {
        circleFill(x0, y0, radius, white, false);
    }
    public void circleFill(int x0, int y0, int radius, int color, boolean xor) {
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        //重複して描画する箇所が多重反転している問題を
        //シンプルな方法で解決できていないので
        //一時的にxorモードをオフにしています
        if (xor) { return; }

        for (int i=y0-radius; i<=y0+radius; i++) {
            pixel(x0, i, color, xor);
        }

        while (x<y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            for (int i=y0-y; i<=y0+y; i++) {
                pixel(x0+x, i, color, xor);
                pixel(x0-x, i, color, xor);
            }
            for (int i=y0-x; i<=y0+x; i++) {
                pixel(x0+y, i, color, xor);
                pixel(x0-y, i, color, xor);
            }
        }
    }

    public static final int FONT_WIDTH = 6;
    public static final int FONT_HEIGHT = 8;
    private static final short ASCII[][] = {
            {0x00, 0x00, 0x00, 0x00, 0x00},
            {0x3E, 0x5B, 0x4F, 0x5B, 0x3E},
            {0x3E, 0x6B, 0x4F, 0x6B, 0x3E},
            {0x1C, 0x3E, 0x7C, 0x3E, 0x1C},
            {0x18, 0x3C, 0x7E, 0x3C, 0x18},
            {0x1C, 0x57, 0x7D, 0x57, 0x1C},
            {0x1C, 0x5E, 0x7F, 0x5E, 0x1C},
            {0x00, 0x18, 0x3C, 0x18, 0x00},
            {0xFF, 0xE7, 0xC3, 0xE7, 0xFF},
            {0x00, 0x18, 0x24, 0x18, 0x00},
            {0xFF, 0xE7, 0xDB, 0xE7, 0xFF},
            {0x30, 0x48, 0x3A, 0x06, 0x0E},
            {0x26, 0x29, 0x79, 0x29, 0x26},
            {0x40, 0x7F, 0x05, 0x05, 0x07},
            {0x40, 0x7F, 0x05, 0x25, 0x3F},
            {0x5A, 0x3C, 0xE7, 0x3C, 0x5A},
            {0x7F, 0x3E, 0x1C, 0x1C, 0x08},
            {0x08, 0x1C, 0x1C, 0x3E, 0x7F},
            {0x14, 0x22, 0x7F, 0x22, 0x14},
            {0x5F, 0x5F, 0x00, 0x5F, 0x5F},
            {0x06, 0x09, 0x7F, 0x01, 0x7F},
            {0x00, 0x66, 0x89, 0x95, 0x6A},
            {0x60, 0x60, 0x60, 0x60, 0x60},
            {0x94, 0xA2, 0xFF, 0xA2, 0x94},
            {0x08, 0x04, 0x7E, 0x04, 0x08},
            {0x10, 0x20, 0x7E, 0x20, 0x10},
            {0x08, 0x08, 0x2A, 0x1C, 0x08},
            {0x08, 0x1C, 0x2A, 0x08, 0x08},
            {0x1E, 0x10, 0x10, 0x10, 0x10},
            {0x0C, 0x1E, 0x0C, 0x1E, 0x0C},
            {0x30, 0x38, 0x3E, 0x38, 0x30},
            {0x06, 0x0E, 0x3E, 0x0E, 0x06},
            {0x00, 0x00, 0x00, 0x00, 0x00},
            {0x00, 0x00, 0x5F, 0x00, 0x00},
            {0x00, 0x07, 0x00, 0x07, 0x00},
            {0x14, 0x7F, 0x14, 0x7F, 0x14},
            {0x24, 0x2A, 0x7F, 0x2A, 0x12},
            {0x23, 0x13, 0x08, 0x64, 0x62},
            {0x36, 0x49, 0x56, 0x20, 0x50},
            {0x00, 0x08, 0x07, 0x03, 0x00},
            {0x00, 0x1C, 0x22, 0x41, 0x00},
            {0x00, 0x41, 0x22, 0x1C, 0x00},
            {0x2A, 0x1C, 0x7F, 0x1C, 0x2A},
            {0x08, 0x08, 0x3E, 0x08, 0x08},
            {0x00, 0x80, 0x70, 0x30, 0x00},
            {0x08, 0x08, 0x08, 0x08, 0x08},
            {0x00, 0x00, 0x60, 0x60, 0x00},
            {0x20, 0x10, 0x08, 0x04, 0x02},
            {0x3E, 0x51, 0x49, 0x45, 0x3E},
            {0x00, 0x42, 0x7F, 0x40, 0x00},
            {0x72, 0x49, 0x49, 0x49, 0x46},
            {0x21, 0x41, 0x49, 0x4D, 0x33},
            {0x18, 0x14, 0x12, 0x7F, 0x10},
            {0x27, 0x45, 0x45, 0x45, 0x39},
            {0x3C, 0x4A, 0x49, 0x49, 0x31},
            {0x41, 0x21, 0x11, 0x09, 0x07},
            {0x36, 0x49, 0x49, 0x49, 0x36},
            {0x46, 0x49, 0x49, 0x29, 0x1E},
            {0x00, 0x00, 0x14, 0x00, 0x00},
            {0x00, 0x40, 0x34, 0x00, 0x00},
            {0x00, 0x08, 0x14, 0x22, 0x41},
            {0x14, 0x14, 0x14, 0x14, 0x14},
            {0x00, 0x41, 0x22, 0x14, 0x08},
            {0x02, 0x01, 0x59, 0x09, 0x06},
            {0x3E, 0x41, 0x5D, 0x59, 0x4E},
            {0x7C, 0x12, 0x11, 0x12, 0x7C},
            {0x7F, 0x49, 0x49, 0x49, 0x36},
            {0x3E, 0x41, 0x41, 0x41, 0x22},
            {0x7F, 0x41, 0x41, 0x41, 0x3E},
            {0x7F, 0x49, 0x49, 0x49, 0x41},
            {0x7F, 0x09, 0x09, 0x09, 0x01},
            {0x3E, 0x41, 0x41, 0x51, 0x73},
            {0x7F, 0x08, 0x08, 0x08, 0x7F},
            {0x00, 0x41, 0x7F, 0x41, 0x00},
            {0x20, 0x40, 0x41, 0x3F, 0x01},
            {0x7F, 0x08, 0x14, 0x22, 0x41},
            {0x7F, 0x40, 0x40, 0x40, 0x40},
            {0x7F, 0x02, 0x1C, 0x02, 0x7F},
            {0x7F, 0x04, 0x08, 0x10, 0x7F},
            {0x3E, 0x41, 0x41, 0x41, 0x3E},
            {0x7F, 0x09, 0x09, 0x09, 0x06},
            {0x3E, 0x41, 0x51, 0x21, 0x5E},
            {0x7F, 0x09, 0x19, 0x29, 0x46},
            {0x26, 0x49, 0x49, 0x49, 0x32},
            {0x03, 0x01, 0x7F, 0x01, 0x03},
            {0x3F, 0x40, 0x40, 0x40, 0x3F},
            {0x1F, 0x20, 0x40, 0x20, 0x1F},
            {0x3F, 0x40, 0x38, 0x40, 0x3F},
            {0x63, 0x14, 0x08, 0x14, 0x63},
            {0x03, 0x04, 0x78, 0x04, 0x03},
            {0x61, 0x59, 0x49, 0x4D, 0x43},
            {0x00, 0x7F, 0x41, 0x41, 0x41},
            {0x02, 0x04, 0x08, 0x10, 0x20},
            {0x00, 0x41, 0x41, 0x41, 0x7F},
            {0x04, 0x02, 0x01, 0x02, 0x04},
            {0x40, 0x40, 0x40, 0x40, 0x40},
            {0x00, 0x03, 0x07, 0x08, 0x00},
            {0x20, 0x54, 0x54, 0x78, 0x40},
            {0x7F, 0x28, 0x44, 0x44, 0x38},
            {0x38, 0x44, 0x44, 0x44, 0x28},
            {0x38, 0x44, 0x44, 0x28, 0x7F},
            {0x38, 0x54, 0x54, 0x54, 0x18},
            {0x00, 0x08, 0x7E, 0x09, 0x02},
            {0x18, 0xA4, 0xA4, 0x9C, 0x78},
            {0x7F, 0x08, 0x04, 0x04, 0x78},
            {0x00, 0x44, 0x7D, 0x40, 0x00},
            {0x20, 0x40, 0x40, 0x3D, 0x00},
            {0x7F, 0x10, 0x28, 0x44, 0x00},
            {0x00, 0x41, 0x7F, 0x40, 0x00},
            {0x7C, 0x04, 0x78, 0x04, 0x78},
            {0x7C, 0x08, 0x04, 0x04, 0x78},
            {0x38, 0x44, 0x44, 0x44, 0x38},
            {0xFC, 0x18, 0x24, 0x24, 0x18},
            {0x18, 0x24, 0x24, 0x18, 0xFC},
            {0x7C, 0x08, 0x04, 0x04, 0x08},
            {0x48, 0x54, 0x54, 0x54, 0x24},
            {0x04, 0x04, 0x3F, 0x44, 0x24},
            {0x3C, 0x40, 0x40, 0x20, 0x7C},
            {0x1C, 0x20, 0x40, 0x20, 0x1C},
            {0x3C, 0x40, 0x30, 0x40, 0x3C},
            {0x44, 0x28, 0x10, 0x28, 0x44},
            {0x4C, 0x90, 0x90, 0x90, 0x7C},
            {0x44, 0x64, 0x54, 0x4C, 0x44},
            {0x00, 0x08, 0x36, 0x41, 0x00},
            {0x00, 0x00, 0x77, 0x00, 0x00},
            {0x00, 0x41, 0x36, 0x08, 0x00},
            {0x02, 0x01, 0x02, 0x04, 0x02},
            {0x3C, 0x26, 0x23, 0x26, 0x3C},
            {0x1E, 0xA1, 0xA1, 0x61, 0x12},
            {0x3A, 0x40, 0x40, 0x20, 0x7A},
            {0x38, 0x54, 0x54, 0x55, 0x59},
            {0x21, 0x55, 0x55, 0x79, 0x41},
            {0x21, 0x54, 0x54, 0x78, 0x41},
            {0x21, 0x55, 0x54, 0x78, 0x40},
            {0x20, 0x54, 0x55, 0x79, 0x40},
            {0x0C, 0x1E, 0x52, 0x72, 0x12},
            {0x39, 0x55, 0x55, 0x55, 0x59},
            {0x39, 0x54, 0x54, 0x54, 0x59},
            {0x39, 0x55, 0x54, 0x54, 0x58},
            {0x00, 0x00, 0x45, 0x7C, 0x41},
            {0x00, 0x02, 0x45, 0x7D, 0x42},
            {0x00, 0x01, 0x45, 0x7C, 0x40},
            {0xF0, 0x29, 0x24, 0x29, 0xF0},
            {0xF0, 0x28, 0x25, 0x28, 0xF0},
            {0x7C, 0x54, 0x55, 0x45, 0x00},
            {0x20, 0x54, 0x54, 0x7C, 0x54},
            {0x7C, 0x0A, 0x09, 0x7F, 0x49},
            {0x32, 0x49, 0x49, 0x49, 0x32},
            {0x32, 0x48, 0x48, 0x48, 0x32},
            {0x32, 0x4A, 0x48, 0x48, 0x30},
            {0x3A, 0x41, 0x41, 0x21, 0x7A},
            {0x3A, 0x42, 0x40, 0x20, 0x78},
            {0x00, 0x9D, 0xA0, 0xA0, 0x7D},
            {0x39, 0x44, 0x44, 0x44, 0x39},
            {0x3D, 0x40, 0x40, 0x40, 0x3D},
            {0x3C, 0x24, 0xFF, 0x24, 0x24},
            {0x48, 0x7E, 0x49, 0x43, 0x66},
            {0x2B, 0x2F, 0xFC, 0x2F, 0x2B},
            {0xFF, 0x09, 0x29, 0xF6, 0x20},
            {0xC0, 0x88, 0x7E, 0x09, 0x03},
            {0x20, 0x54, 0x54, 0x79, 0x41},
            {0x00, 0x00, 0x44, 0x7D, 0x41},
            {0x30, 0x48, 0x48, 0x4A, 0x32},
            {0x38, 0x40, 0x40, 0x22, 0x7A},
            {0x00, 0x7A, 0x0A, 0x0A, 0x72},
            {0x7D, 0x0D, 0x19, 0x31, 0x7D},
            {0x26, 0x29, 0x29, 0x2F, 0x28},
            {0x26, 0x29, 0x29, 0x29, 0x26},
            {0x30, 0x48, 0x4D, 0x40, 0x20},
            {0x38, 0x08, 0x08, 0x08, 0x08},
            {0x08, 0x08, 0x08, 0x08, 0x38},
            {0x2F, 0x10, 0xC8, 0xAC, 0xBA},
            {0x2F, 0x10, 0x28, 0x34, 0xFA},
            {0x00, 0x00, 0x7B, 0x00, 0x00},
            {0x08, 0x14, 0x2A, 0x14, 0x22},
            {0x22, 0x14, 0x2A, 0x14, 0x08},
            {0xAA, 0x00, 0x55, 0x00, 0xAA},
            {0xAA, 0x55, 0xAA, 0x55, 0xAA},
            {0x00, 0x00, 0x00, 0xFF, 0x00},
            {0x10, 0x10, 0x10, 0xFF, 0x00},
            {0x14, 0x14, 0x14, 0xFF, 0x00},
            {0x10, 0x10, 0xFF, 0x00, 0xFF},
            {0x10, 0x10, 0xF0, 0x10, 0xF0},
            {0x14, 0x14, 0x14, 0xFC, 0x00},
            {0x14, 0x14, 0xF7, 0x00, 0xFF},
            {0x00, 0x00, 0xFF, 0x00, 0xFF},
            {0x14, 0x14, 0xF4, 0x04, 0xFC},
            {0x14, 0x14, 0x17, 0x10, 0x1F},
            {0x10, 0x10, 0x1F, 0x10, 0x1F},
            {0x14, 0x14, 0x14, 0x1F, 0x00},
            {0x10, 0x10, 0x10, 0xF0, 0x00},
            {0x00, 0x00, 0x00, 0x1F, 0x10},
            {0x10, 0x10, 0x10, 0x1F, 0x10},
            {0x10, 0x10, 0x10, 0xF0, 0x10},
            {0x00, 0x00, 0x00, 0xFF, 0x10},
            {0x10, 0x10, 0x10, 0x10, 0x10},
            {0x10, 0x10, 0x10, 0xFF, 0x10},
            {0x00, 0x00, 0x00, 0xFF, 0x14},
            {0x00, 0x00, 0xFF, 0x00, 0xFF},
            {0x00, 0x00, 0x1F, 0x10, 0x17},
            {0x00, 0x00, 0xFC, 0x04, 0xF4},
            {0x14, 0x14, 0x17, 0x10, 0x17},
            {0x14, 0x14, 0xF4, 0x04, 0xF4},
            {0x00, 0x00, 0xFF, 0x00, 0xF7},
            {0x14, 0x14, 0x14, 0x14, 0x14},
            {0x14, 0x14, 0xF7, 0x00, 0xF7},
            {0x14, 0x14, 0x14, 0x17, 0x14},
            {0x10, 0x10, 0x1F, 0x10, 0x1F},
            {0x14, 0x14, 0x14, 0xF4, 0x14},
            {0x10, 0x10, 0xF0, 0x10, 0xF0},
            {0x00, 0x00, 0x1F, 0x10, 0x1F},
            {0x00, 0x00, 0x00, 0x1F, 0x14},
            {0x00, 0x00, 0x00, 0xFC, 0x14},
            {0x00, 0x00, 0xF0, 0x10, 0xF0},
            {0x10, 0x10, 0xFF, 0x10, 0xFF},
            {0x14, 0x14, 0x14, 0xFF, 0x14},
            {0x10, 0x10, 0x10, 0x1F, 0x00},
            {0x00, 0x00, 0x00, 0xF0, 0x10},
            {0xFF, 0xFF, 0xFF, 0xFF, 0xFF},
            {0xF0, 0xF0, 0xF0, 0xF0, 0xF0},
            {0xFF, 0xFF, 0xFF, 0x00, 0x00},
            {0x00, 0x00, 0x00, 0xFF, 0xFF},
            {0x0F, 0x0F, 0x0F, 0x0F, 0x0F},
            {0x38, 0x44, 0x44, 0x38, 0x44},
            {0x7C, 0x2A, 0x2A, 0x3E, 0x14},
            {0x7E, 0x02, 0x02, 0x06, 0x06},
            {0x02, 0x7E, 0x02, 0x7E, 0x02},
            {0x63, 0x55, 0x49, 0x41, 0x63},
            {0x38, 0x44, 0x44, 0x3C, 0x04},
            {0x40, 0x7E, 0x20, 0x1E, 0x20},
            {0x06, 0x02, 0x7E, 0x02, 0x02},
            {0x99, 0xA5, 0xE7, 0xA5, 0x99},
            {0x1C, 0x2A, 0x49, 0x2A, 0x1C},
            {0x4C, 0x72, 0x01, 0x72, 0x4C},
            {0x30, 0x4A, 0x4D, 0x4D, 0x30},
            {0x30, 0x48, 0x78, 0x48, 0x30},
            {0xBC, 0x62, 0x5A, 0x46, 0x3D},
            {0x3E, 0x49, 0x49, 0x49, 0x00},
            {0x7E, 0x01, 0x01, 0x01, 0x7E},
            {0x2A, 0x2A, 0x2A, 0x2A, 0x2A},
            {0x44, 0x44, 0x5F, 0x44, 0x44},
            {0x40, 0x51, 0x4A, 0x44, 0x40},
            {0x40, 0x44, 0x4A, 0x51, 0x40},
            {0x00, 0x00, 0xFF, 0x01, 0x03},
            {0xE0, 0x80, 0xFF, 0x00, 0x00},
            {0x08, 0x08, 0x6B, 0x6B, 0x08},
            {0x36, 0x12, 0x36, 0x24, 0x36},
            {0x06, 0x0F, 0x09, 0x0F, 0x06},
            {0x00, 0x00, 0x18, 0x18, 0x00},
            {0x00, 0x00, 0x10, 0x10, 0x00},
            {0x30, 0x40, 0xFF, 0x01, 0x01},
            {0x00, 0x1F, 0x01, 0x01, 0x1E},
            {0x00, 0x19, 0x1D, 0x17, 0x12},
            {0x00, 0x3C, 0x3C, 0x3C, 0x3C},
            {0x00, 0x00, 0x00, 0x00, 0x00}
    };

    public void setChar(int x, int y, char c) {
        setChar(x, y, c, white, false);
    }
    public void setChar(int x, int y, char c, int color, boolean xor) {
        int asciiOffset =  0x00;
        int bitMask = 0x80;
        int charPos = c - asciiOffset;

        if ( (x+(FONT_WIDTH-1)) > OLED_WIDTH ) { return; }
        if ( (y+FONT_HEIGHT) > OLED_HEIGHT ) { return; }

        if ( 0x00<=c && c<=0xFE ) {
            for (int x0 = 0; x0<(FONT_WIDTH-1); x0++) {
                int asciiChar = ASCII[charPos][x0];
                for (int y0=0; y0<FONT_HEIGHT; y0++) {
                    int bit = (asciiChar<<y0) & bitMask ;
                    if (bit==bitMask) {
                        pixel((x+x0), (y+FONT_HEIGHT-y0), color, xor);
                    }
                }
            }
        }
    }

    public void setString(int x, int y, String str) {
        setString(x, y, str, white, false);
    }
    public void setString(int x, int y, String str, int color, boolean xor) {
        int posX = x;
        int posY = y;

        if ( (x+FONT_WIDTH) > OLED_WIDTH ) { return; }
        if ( (y+FONT_HEIGHT) > OLED_HEIGHT ) { return; }

        for (int pos=0; pos<str.length(); pos++) {
            char c = str.charAt(pos);
            setChar( posX, posY, c, color, xor);
            posX += FONT_WIDTH ;
            if (posX>=OLED_WIDTH) {
                break;
            }
        }
    }

}
