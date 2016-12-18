package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by root on 17.12.16.
 */

public class VkontakteView extends RelativeLayout {
    public static final String LINK_16_9 = "https://pp.vk.me/c635101/v635101176/1e663/_0G1x1vvySI.jpg";

    private Context mContext;
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    public VkontakteView(Context context) {
        super(context);
        mContext = context;
    }

    public VkontakteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void test() {
        ImageView imageView = new ImageView(mContext);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        this.addView(imageView);
        mImageLoader.displayImage(LINK_16_9, imageView);
    }
}
