package com.easyvideoworktool;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final int navy = Color.rgb(8, 25, 53);
    private final int panel = Color.rgb(15, 36, 70);
    private final int edge = Color.rgb(55, 78, 112);
    private final int white = Color.rgb(245, 247, 250);
    private final int yellow = Color.rgb(255, 214, 45);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(0);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(navy);
        root.setPadding(dp(2), dp(2), dp(2), dp(2));
        setContentView(root);

        HorizontalScrollView topScroll = new HorizontalScrollView(this);
        topScroll.setHorizontalScrollBarEnabled(false);
        topScroll.setFillViewport(false);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        String[] tools = {"Import\nVideos", "Export\nVideo", "Delete", "Tracks", "Color\nAnd Light", "Blur\nSharpen\nNoise", "Merge\nJoin", "Video\nText"};
        for (int i=0;i<tools.length;i++) {
            TextView b = button(tools[i], i==2 ? Color.rgb(235,75,85) : white, panel, 13);
            top.addView(b, new LinearLayout.LayoutParams(dp(i==2?74:88), dp(54)));
        }
        topScroll.addView(top);
        root.addView(topScroll, new LinearLayout.LayoutParams(-1, dp(58)));

        LinearLayout upper = new LinearLayout(this);
        upper.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        String[] leftLabels = {"UnDo", "ReDo", "Cancel"};
        for (String s:leftLabels) left.addView(button(s, s.equals("Cancel")?0xFFDB929B:white, panel, 16), new LinearLayout.LayoutParams(-1, 0, 1));
        upper.addView(left, new LinearLayout.LayoutParams(dp(82), dp(246)));

        LinearLayout playback = new LinearLayout(this); playback.setOrientation(LinearLayout.VERTICAL);
        String[] playLabels = {"Play\n▶❚❚", "Forward\n⏩", "Back\n⏪", "Speed\nPlay"};
        for(String s:playLabels) playback.addView(button(s, white, panel, 16), new LinearLayout.LayoutParams(-1, 0, 1));
        upper.addView(playback, new LinearLayout.LayoutParams(dp(86), dp(246)));

        TextView player = button("PLAYER", Color.rgb(255,75,83), Color.rgb(5,12,31), 34);
        player.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        upper.addView(player, new LinearLayout.LayoutParams(0, dp(246), 1));

        LinearLayout right = new LinearLayout(this); right.setOrientation(LinearLayout.VERTICAL);
        right.addView(button("Crop", white, panel, 25), new LinearLayout.LayoutParams(-1, 0, 1));
        right.addView(button("Cover\nFrame  ▣", white, panel, 14), new LinearLayout.LayoutParams(-1, 0, 1));
        upper.addView(right, new LinearLayout.LayoutParams(dp(94), dp(246)));
        root.addView(upper, new LinearLayout.LayoutParams(-1, dp(250)));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.addView(button("UP", Color.BLACK, yellow, 18), new LinearLayout.LayoutParams(dp(78), dp(52)));
        controls.addView(button("ALL VIEW\nفقط اسلایدها", Color.BLACK, yellow, 12), new LinearLayout.LayoutParams(dp(92), dp(52)));
        controls.addView(button("5 ستون ▾", yellow, navy, 15), new LinearLayout.LayoutParams(0, dp(52), 1));
        controls.addView(button("Time\nLines  00:05", white, navy, 14), new LinearLayout.LayoutParams(dp(100), dp(52)));
        controls.addView(button("Select All", Color.BLACK, yellow, 13), new LinearLayout.LayoutParams(dp(84), dp(52)));
        controls.addView(button("Un Select", Color.BLACK, 0xFFFFF4C8, 13), new LinearLayout.LayoutParams(dp(84), dp(52)));
        controls.addView(button("Search\nBy Photo", 0xFF9B704A, 0xFFFFE7B8, 12), new LinearLayout.LayoutParams(dp(82), dp(52)));
        root.addView(controls, new LinearLayout.LayoutParams(-1, dp(56)));

        ScrollView storyboardScroll = new ScrollView(this);
        storyboardScroll.setFillViewport(false);
        LinearLayout storyboard = new LinearLayout(this);
        storyboard.setOrientation(LinearLayout.VERTICAL);
        storyboard.setBackgroundColor(Color.rgb(4,13,32));
        for(int r=0;r<12;r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for(int c=0;c<5;c++) {
                TextView thumb = button("", white, (r+c)%2==0?0xFF152A66:0xFF1C326E, 10);
                GradientDrawable frame = new GradientDrawable();
                frame.setColor((r+c)%2==0?0xFF152A66:0xFF1C326E);
                frame.setStroke(dp(1), edge);
                thumb.setBackground(frame);
                row.addView(thumb, new LinearLayout.LayoutParams(0, dp(72), 1));
            }
            storyboard.addView(row, new LinearLayout.LayoutParams(-1, dp(74)));
        }
        storyboardScroll.addView(storyboard);
        root.addView(storyboardScroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private TextView button(String text, int textColor, int bg, int size) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextColor(textColor); v.setTextSize(size);
        v.setGravity(Gravity.CENTER); v.setPadding(dp(3),dp(2),dp(3),dp(2));
        v.setBackground(shape(bg));
        v.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        v.setMaxLines(4);
        return v;
    }
    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setStroke(dp(1), edge); return d;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
}
