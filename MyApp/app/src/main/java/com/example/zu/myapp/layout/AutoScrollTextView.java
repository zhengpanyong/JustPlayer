package com.example.zu.myapp.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zu on 2016/2/21.
 */
public class AutoScrollTextView extends TextView
{
    public AutoScrollTextView(Context context) {
        super(context);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
