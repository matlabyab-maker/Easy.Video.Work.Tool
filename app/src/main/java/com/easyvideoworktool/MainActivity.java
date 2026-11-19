package com.easyvideoworktool;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int REQUEST_IMPORT_VIDEOS = 1001;
    private final ArrayList<Uri> importedVideos = new ArrayList<>();
    private final int navy = Color.rgb(8, 25, 53);
    private final int panel = Color.rgb(15, 36, 70);
    private final int edge = Color.rgb(55, 78, 112);
    private final int white = Color.rgb(245, 247, 250);
    private final int yellow = Color.rgb(255, 214, 45);

    private VideoView videoView;
    private TextView playerPlaceholder;
    private TextView playButton;
    private TextView columnsButton;
    private LinearLayout storyboard;
    private int columnCount = 5;
    private boolean videoPrepared = false;
    private boolean isPlaying = false;
    private int currentVideoIndex = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(0);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(navy);
        root.setPadding(dp(2), dp(2), dp(2), dp(2));
        setContentView(root);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        String[] tools = {"Import\nVideos", "Export\nVideo", "Delete", "Tracks", "Color\nAnd Light", "Blur\nSharpen\nNoise", "Merge\nJoin", "Video\nText"};
        for (int i = 0; i < tools.length; i++) {
            TextView b = button(tools[i], i == 2 ? Color.rgb(235, 75, 85) : white, panel, 10);
            b.setMaxLines(3);
            b.setIncludeFontPadding(false);
            if (i == 0) b.setOnClickListener(v -> openVideoPicker());
            top.addView(b, new LinearLayout.LayoutParams(0, dp(58), 1));
        }
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));

        LinearLayout upper = new LinearLayout(this);
        upper.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        String[] leftLabels = {"UnDo", "ReDo", "Cancel"};
        for (String s : leftLabels) {
            left.addView(button(s, s.equals("Cancel") ? 0xFFDB929B : white, panel, 16),
                    new LinearLayout.LayoutParams(-1, 0, 1));
        }
        upper.addView(left, new LinearLayout.LayoutParams(dp(82), dp(246)));

        LinearLayout playback = new LinearLayout(this);
        playback.setOrientation(LinearLayout.VERTICAL);
        playButton = button("Play\n▶❚❚", white, panel, 16);
        playButton.setOnClickListener(v -> togglePlayback());
        playback.addView(playButton, new LinearLayout.LayoutParams(-1, 0, 1));
        String[] otherPlaybackLabels = {"Forward\n⏩", "Back\n⏪", "Speed\nPlay"};
        for (String s : otherPlaybackLabels) {
            playback.addView(button(s, white, panel, 16), new LinearLayout.LayoutParams(-1, 0, 1));
        }
        upper.addView(playback, new LinearLayout.LayoutParams(dp(86), dp(246)));

        FrameLayout playerFrame = new FrameLayout(this);
        playerFrame.setBackgroundColor(Color.rgb(5, 12, 31));
        videoView = new VideoView(this);
        videoView.setBackgroundColor(Color.BLACK);
        playerFrame.addView(videoView, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        playerPlaceholder = button("PLAYER", Color.rgb(255, 75, 83), Color.TRANSPARENT, 34);
        playerPlaceholder.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        playerPlaceholder.setClickable(false);
        playerFrame.addView(playerPlaceholder, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        playerPlaceholder.bringToFront();
        videoView.setOnPreparedListener(mp -> {
            videoPrepared = true;
            playerPlaceholder.setVisibility(View.GONE);
        });
        videoView.setOnCompletionListener(mp -> {
            isPlaying = false;
            playButton.setText("Play\n▶❚❚");
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            videoPrepared = false;
            isPlaying = false;
            playButton.setText("Play\n▶❚❚");
            Toast.makeText(this, "This video could not be played", Toast.LENGTH_SHORT).show();
            return true;
        });
        upper.addView(playerFrame, new LinearLayout.LayoutParams(0, dp(246), 1));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.addView(button("Crop", white, panel, 25), new LinearLayout.LayoutParams(-1, 0, 1));
        right.addView(button("Cover\nFrame  ▣", white, panel, 14), new LinearLayout.LayoutParams(-1, 0, 1));
        upper.addView(right, new LinearLayout.LayoutParams(dp(94), dp(246)));
        root.addView(upper, new LinearLayout.LayoutParams(-1, dp(250)));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.addView(button("UP", Color.BLACK, yellow, 18), new LinearLayout.LayoutParams(dp(78), dp(52)));
        controls.addView(button("ALL VIEW\nفقط اسلایدها", Color.BLACK, yellow, 12), new LinearLayout.LayoutParams(dp(92), dp(52)));
        columnsButton = button(columnCount + " ستون ▾", yellow, navy, 15);
        columnsButton.setOnClickListener(v -> showColumnsMenu());
        controls.addView(columnsButton, new LinearLayout.LayoutParams(0, dp(52), 1));
        controls.addView(button("Time\nLines  00:05", white, navy, 14), new LinearLayout.LayoutParams(dp(100), dp(52)));
        controls.addView(button("Select All", Color.BLACK, yellow, 13), new LinearLayout.LayoutParams(dp(84), dp(52)));
        controls.addView(button("Un Select", Color.BLACK, 0xFFFFF4C8, 13), new LinearLayout.LayoutParams(dp(84), dp(52)));
        controls.addView(button("Search\nBy Photo", 0xFF9B704A, 0xFFFFE7B8, 12), new LinearLayout.LayoutParams(dp(82), dp(52)));
        root.addView(controls, new LinearLayout.LayoutParams(-1, dp(56)));

        ScrollView storyboardScroll = new ScrollView(this);
        storyboardScroll.setFillViewport(false);
        storyboard = new LinearLayout(this);
        storyboard.setOrientation(LinearLayout.VERTICAL);
        storyboard.setBackgroundColor(Color.rgb(4, 13, 32));
        rebuildStoryboard();
        storyboardScroll.addView(storyboard);
        root.addView(storyboardScroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void showColumnsMenu() {
        PopupMenu menu = new PopupMenu(this, columnsButton);
        for (int count = 1; count <= 15; count++) {
            menu.getMenu().add(Menu.NONE, count, count, count + " ستون");
        }
        menu.setOnMenuItemClickListener(item -> {
            setColumnCount(item.getItemId());
            return true;
        });
        menu.show();
    }

    private void setColumnCount(int count) {
        if (count < 1 || count > 15) return;
        columnCount = count;
        columnsButton.setText(columnCount + " ستون ▾");
        rebuildStoryboard();
    }

    private void rebuildStoryboard() {
        if (storyboard == null) return;
        storyboard.removeAllViews();
        final int rows = 12;
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < columnCount; c++) {
                final int index = r * columnCount + c;
                boolean hasVideo = index < importedVideos.size();
                TextView thumb = button(hasVideo ? "Video " + (index + 1) : "", white,
                        (r + c) % 2 == 0 ? 0xFF152A66 : 0xFF1C326E, 10);
                GradientDrawable frame = new GradientDrawable();
                frame.setColor(index == currentVideoIndex && hasVideo ? 0xFF344F91 :
                        ((r + c) % 2 == 0 ? 0xFF152A66 : 0xFF1C326E));
                frame.setStroke(dp(index == currentVideoIndex && hasVideo ? 2 : 1),
                        index == currentVideoIndex && hasVideo ? yellow : edge);
                thumb.setBackground(frame);
                if (hasVideo) thumb.setOnClickListener(v -> {
                    currentVideoIndex = index;
                    loadVideo(currentVideoIndex);
                    rebuildStoryboard();
                });
                row.addView(thumb, new LinearLayout.LayoutParams(0, dp(72), 1));
            }
            storyboard.addView(row, new LinearLayout.LayoutParams(-1, dp(74)));
        }
    }

    private void loadVideo(int index) {
        if (index < 0 || index >= importedVideos.size()) return;
        videoPrepared = false;
        isPlaying = false;
        if (playButton != null) playButton.setText("Play\n▶❚❚");
        if (playerPlaceholder != null) playerPlaceholder.setVisibility(View.VISIBLE);
        videoView.stopPlayback();
        videoView.setVideoURI(importedVideos.get(index));
    }

    private void togglePlayback() {
        if (importedVideos.isEmpty()) {
            Toast.makeText(this, "Please import a video first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!videoPrepared) {
            Toast.makeText(this, "Video is preparing", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoView.isPlaying()) {
            videoView.pause();
            isPlaying = false;
            playButton.setText("Play\n▶❚❚");
        } else {
            videoView.start();
            isPlaying = true;
            playButton.setText("Pause\n❚❚");
        }
    }

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_IMPORT_VIDEOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT_VIDEOS || resultCode != RESULT_OK || data == null) return;

        int added = 0;
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                if (uri != null && !importedVideos.contains(uri)) {
                    keepVideoPermission(data, uri);
                    importedVideos.add(uri);
                    added++;
                }
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            if (!importedVideos.contains(uri)) {
                keepVideoPermission(data, uri);
                importedVideos.add(uri);
                added++;
            }
        }

        if (added > 0) {
            // Keep the current clip when adding more; the first import becomes the initial clip.
            if (importedVideos.size() == added) currentVideoIndex = 0;
            rebuildStoryboard();
            loadVideo(currentVideoIndex);
        }
        Toast.makeText(this, added > 0
                ? added + " video(s) imported. Total: " + importedVideos.size()
                : "No new videos selected", Toast.LENGTH_SHORT).show();
    }

    private void keepVideoPermission(Intent result, Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri, result.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some document providers do not offer persistable permissions.
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
            isPlaying = false;
            if (playButton != null) playButton.setText("Play\n▶❚❚");
        }
    }

    private TextView button(String text, int textColor, int bg, int size) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(textColor);
        v.setTextSize(size);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(3), dp(2), dp(3), dp(2));
        v.setBackground(bg == Color.TRANSPARENT ? new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT) : shape(bg));
        v.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        v.setMaxLines(4);
        return v;
    }

    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setStroke(dp(1), edge);
        return d;
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
