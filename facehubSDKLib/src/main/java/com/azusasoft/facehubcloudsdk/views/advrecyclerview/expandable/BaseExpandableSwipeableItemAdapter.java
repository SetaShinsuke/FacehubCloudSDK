/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.azusasoft.facehubcloudsdk.views.advrecyclerview.expandable;

import android.support.v7.widget.RecyclerView;

import com.azusasoft.facehubcloudsdk.views.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.swipeable.annotation.SwipeableItemReactions;

public interface BaseExpandableSwipeableItemAdapter<GVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder> {

    @SwipeableItemReactions
    int onGetGroupItemSwipeReactionType(GVH holder, int groupPosition, int x, int y);

    @SwipeableItemReactions
    int onGetChildItemSwipeReactionType(CVH holder, int groupPosition, int childPosition, int x, int y);

    void onSetGroupItemSwipeBackground(GVH holder, int groupPosition, @SwipeableItemDrawableTypes int type);

    void onSetChildItemSwipeBackground(CVH holder, int groupPosition, int childPosition, @SwipeableItemDrawableTypes int type);
}
