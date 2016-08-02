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
                R.array.emoji_all
                };

        for (int resId : emojiResArray) {
            int[] emojiIntArray = context.getResources().getIntArray(resId);
            for (int emojiInt : emojiIntArray) {
                emojiStrings.add(getEmojiStringByUnicode(emojiInt));
            }
        }
        return emojiStrings;
    }

    private static String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

}
