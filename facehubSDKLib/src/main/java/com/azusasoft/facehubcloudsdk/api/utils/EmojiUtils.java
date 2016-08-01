package com.azusasoft.facehubcloudsdk.api.utils;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA_WORK on 2016/8/1.
 */
public class EmojiUtils {
    /** 注:复制emoji方法
     * http://apps.timwhitlock.info/emoji/tables/unicode
     * 复制某组，粘贴到编辑器
     * 替换:\n -> "" (勾选regex)
     * 替换:\U+ -> \n (勾选match case)
     */

    public static ArrayList<String> getEmojiStrings(Context context){
        ArrayList<String> emojiStrings = new ArrayList<>();
        int[] emojiResArray = new int[]{
                R.array.emoticons,R.array.additional_emotions
                ,R.array.other_additional_symbols
                ,R.array.uncategorized_native
                };

        for (int resId : emojiResArray) {
            int[] emojiIntArray = context.getResources().getIntArray(resId);
            for (int emojiInt : emojiIntArray) {
                emojiStrings.add(getEmojiStringByUnicode(emojiInt));
            }
        }
        return emojiStrings;
    }

//    private static final int[] startEnds = new int[]{
//            0x1f601,0x1f640,
//            0x1f645,0x1f64f,
////            0x2702,0x27b0,
////            0x1f680,0x1f6c0,
////            0x24c2,0x24c2,
////            0x1f170,0x1f251,
//            0x1f600,0x1f636
//    };
//
//    public static ArrayList<String> getEmojiStrings(){
//        ArrayList<String> result = new ArrayList<>();
//        for(int i=0;i<startEnds.length;i+=2){
//            result.add("\n第 " + i/2 + " 组 : ");
//            for(int j=startEnds[i];j<=startEnds[i+1];j++){
//                String s = getEmojiStringByUnicode(j);
//                if(j==0x1f601){
//                    LogX.fastLog("s : " + s);
//                }
//                result.add(s);
//            }
//        }
//        return result;
//    }

    private static String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

}
