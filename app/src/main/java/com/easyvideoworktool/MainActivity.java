package com.easyvideoworktool;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ScrollView storyboardScroll;
    private StoryboardScrollBar storyboardScrollBar;
    private final ArrayList<FrameLayout> storyboardCellFrames = new ArrayList<>();
    private final Set<Integer> selectedFrames = new HashSet<>();
    private TextView upButton, betweenButton, downButton;
    private int columnCount = 5;
    private boolean videoPrepared = false;
    private boolean isPlaying = false;
    private int currentVideoIndex = 0;
    private static final int FIXED_FRAME_COUNT = 120;
    private static final int FRAME_WORKER_COUNT = 2;
    private final ArrayList<Bitmap> storyboardFrames = new ArrayList<>();
    private final ArrayList<ImageView> storyboardCells = new ArrayList<>();
    private final ExecutorService frameWorkers = Executors.newFixedThreadPool(FRAME_WORKER_COUNT);
    private final ArrayList<Future<?>> frameJobs = new ArrayList<>();
    private final AtomicInteger extractionGeneration = new AtomicInteger(0);
    private long currentVideoDurationMs = 0;

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
        LinearLayout positionButtons = new LinearLayout(this);
        positionButtons.setOrientation(LinearLayout.VERTICAL);
        upButton = button("UP", white, navy, 10);
        betweenButton = button("Between", Color.BLACK, yellow, 10);
        downButton = button("Down", white, navy, 10);
        positionButtons.addView(upButton, new LinearLayout.LayoutParams(-1, 0, 1));
        positionButtons.addView(betweenButton, new LinearLayout.LayoutParams(-1, 0, 1));
        positionButtons.addView(downButton, new LinearLayout.LayoutParams(-1, 0, 1));
        upButton.setOnClickListener(v -> moveStoryboardTo("UP"));
        betweenButton.setOnClickListener(v -> moveStoryboardTo("BETWEEN"));
        downButton.setOnClickListener(v -> moveStoryboardTo("DOWN"));
        controls.addView(positionButtons, new LinearLayout.LayoutParams(dp(78), dp(52)));
        controls.addView(button("ALL VIEW\nفقط اسلایدها", Color.BLACK, yellow, 12), new LinearLayout.LayoutParams(dp(92), dp(52)));
        columnsButton = button(columnCount + " ستون ▾", yellow, navy, 15);
        columnsButton.setOnClickListener(v -> showColumnsMenu());
        controls.addView(columnsButton, new LinearLayout.LayoutParams(0, dp(52), 1));
        controls.addView(button("Time\nLines  00:05", white, navy, 14), new LinearLayout.LayoutParams(dp(100), dp(52)));
        TextView selectAllButton = button("Select All", Color.BLACK, yellow, 13);
        TextView unselectButton = button("Un Select", Color.BLACK, 0xFFFFF4C8, 13);
        selectAllButton.setOnClickListener(v -> setAllFramesSelected(true));
        unselectButton.setOnClickListener(v -> setAllFramesSelected(false));
        controls.addView(selectAllButton, new LinearLayout.LayoutParams(dp(84), dp(52)));
        controls.addView(unselectButton, new LinearLayout.LayoutParams(dp(84), dp(52)));
        controls.addView(button("Search\nBy Photo", 0xFF9B704A, 0xFFFFE7B8, 12), new LinearLayout.LayoutParams(dp(82), dp(52)));
        root.addView(controls, new LinearLayout.LayoutParams(-1, dp(56)));

        storyboardScroll = new ScrollView(this);
        storyboardScroll.setFillViewport(false);
        storyboardScroll.setVerticalScrollBarEnabled(false);
        storyboardScroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        storyboard = new LinearLayout(this);
        storyboard.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < FIXED_FRAME_COUNT; i++) storyboardFrames.add(null);
        storyboard.setBackgroundColor(Color.rgb(4, 13, 32));
        rebuildStoryboard();
        storyboardScroll.addView(storyboard);
        FrameLayout storyboardArea = new FrameLayout(this);
        storyboardArea.addView(storyboardScroll, new FrameLayout.LayoutParams(-1, -1));
        storyboardScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (storyboardScrollBar != null) storyboardScrollBar.invalidate();
        });
        storyboardScroll.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (storyboardScrollBar != null) storyboardScrollBar.invalidate();
        });
        storyboardScrollBar = new StoryboardScrollBar(this);
        FrameLayout.LayoutParams scrollBarParams = new FrameLayout.LayoutParams(dp(22), -1, Gravity.RIGHT);
        storyboardArea.addView(storyboardScrollBar, scrollBarParams);
        root.addView(storyboardArea, new LinearLayout.LayoutParams(-1, 0, 1));
        storyboardScroll.post(() -> moveStoryboardTo("BETWEEN"));
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
        storyboardCells.clear();
        storyboardCellFrames.clear();
        final int rows = (FIXED_FRAME_COUNT + columnCount - 1) / columnCount;
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < columnCount; c++) {
                final int index = r * columnCount + c;
                if (index >= FIXED_FRAME_COUNT) break;
                FrameLayout cell = new FrameLayout(this);
                GradientDrawable frame = new GradientDrawable();
                frame.setColor((r + c) % 2 == 0 ? 0xFF152A66 : 0xFF1C326E);
                frame.setStroke(dp(1), edge);
                cell.setBackground(frame);
                ImageView thumbnail = new ImageView(this);
                thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                thumbnail.setBackgroundColor(Color.TRANSPARENT);
                Bitmap bitmap = storyboardFrames.get(index);
                if (bitmap != null && !bitmap.isRecycled()) thumbnail.setImageBitmap(bitmap);
                cell.addView(thumbnail, new FrameLayout.LayoutParams(-1, -1));
                TextView number = new TextView(this);
                number.setText(String.valueOf(index + 1));
                number.setTextColor(Color.WHITE);
                number.setTextSize(9);
                number.setGravity(Gravity.CENTER);
                number.setBackgroundColor(0x88000000);
                FrameLayout.LayoutParams numberParams = new FrameLayout.LayoutParams(dp(22), dp(18), Gravity.BOTTOM | Gravity.RIGHT);
                cell.addView(number, numberParams);
                cell.setOnClickListener(v -> {
                    if (selectedFrames.contains(index)) selectedFrames.remove(index);
                    else selectedFrames.add(index);
                    updateStoryboardSelection();
                    seekToStoryboardFrame(index);
                });
                storyboardCells.add(thumbnail);
                storyboardCellFrames.add(cell);
                row.addView(cell, new LinearLayout.LayoutParams(0, dp(72), 1));
            }
            storyboard.addView(row, new LinearLayout.LayoutParams(-1, dp(74)));
        }
        updateStoryboardSelection();
        if (storyboardScrollBar != null) storyboardScrollBar.invalidate();
    }

    private void setAllFramesSelected(boolean selected) {
        selectedFrames.clear();
        if (selected) for (int i = 0; i < FIXED_FRAME_COUNT; i++) selectedFrames.add(i);
        updateStoryboardSelection();
    }

    private void updateStoryboardSelection() {
        for (int i = 0; i < storyboardCellFrames.size(); i++) {
            FrameLayout cell = storyboardCellFrames.get(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor((i % 2 == 0) ? 0xFF152A66 : 0xFF1C326E);
            boolean selected = selectedFrames.contains(i);
            bg.setStroke(dp(selected ? 3 : 1), selected ? yellow : edge);
            cell.setBackground(bg);
        }
    }

    private void moveStoryboardTo(String position) {
        if (storyboardScroll == null || storyboard == null) return;
        int range = Math.max(0, storyboard.getHeight() - storyboardScroll.getHeight());
        int target;
        if ("UP".equals(position)) target = 0;
        else if ("DOWN".equals(position)) target = range;
        else target = range / 2;
        storyboardScroll.smoothScrollTo(0, target);
        upButton.setTextColor("UP".equals(position) ? Color.BLACK : white);
        upButton.setBackground(shape("UP".equals(position) ? yellow : navy));
        betweenButton.setTextColor("BETWEEN".equals(position) ? Color.BLACK : white);
        betweenButton.setBackground(shape("BETWEEN".equals(position) ? yellow : navy));
        downButton.setTextColor("DOWN".equals(position) ? Color.BLACK : white);
        downButton.setBackground(shape("DOWN".equals(position) ? yellow : navy));
    }

    private void seekToStoryboardFrame(int frameIndex) {
        if (!videoPrepared || currentVideoDurationMs <= 0 || frameIndex < 0 || frameIndex >= FIXED_FRAME_COUNT) return;
        int position = (int) Math.min(currentVideoDurationMs,
                (currentVideoDurationMs * frameIndex) / (FIXED_FRAME_COUNT - 1));
        videoView.seekTo(position);
        refreshSelectedFrame(frameIndex);
    }

    private void refreshSelectedFrame(int selected) {
        updateStoryboardSelection();
    }

    private class StoryboardScrollBar extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float thumbTop;
        private float thumbHeight;
        private boolean dragging;
        private float dragOffset;

        StoryboardScrollBar(android.content.Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setFocusable(true);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float trackLeft = w / 2f - dp(2);
            paint.setColor(0xFF35445D);
            canvas.drawRoundRect(trackLeft, dp(4), trackLeft + dp(4), h - dp(4), dp(3), dp(3), paint);
            int range = Math.max(0, storyboard.getHeight() - storyboardScroll.getHeight());
            if (range == 0) {
                thumbHeight = Math.max(dp(42), h - dp(8));
                thumbTop = dp(4);
            } else {
                thumbHeight = Math.max(dp(42), (h - dp(8)) * (storyboardScroll.getHeight() / (float) storyboard.getHeight()));
                thumbHeight = Math.min(h - dp(8), thumbHeight);
                thumbTop = dp(4) + (h - dp(8) - thumbHeight) * (storyboardScroll.getScrollY() / (float) range);
            }
            paint.setColor(yellow);
            canvas.drawRoundRect(dp(3), thumbTop, w - dp(3), thumbTop + thumbHeight, dp(6), dp(6), paint);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (storyboardScroll == null) return false;
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragging = true;
                    if (y >= thumbTop && y <= thumbTop + thumbHeight) dragOffset = y - thumbTop;
                    else { dragOffset = thumbHeight / 2f; moveToTouch(y); }
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging) { moveToTouch(y); return true; }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return true;
        }

        private void moveToTouch(float y) {
            float available = Math.max(1, getHeight() - dp(8) - thumbHeight);
            float top = Math.max(dp(4), Math.min(getHeight() - dp(4) - thumbHeight, y - dragOffset));
            float fraction = (top - dp(4)) / available;
            int range = Math.max(0, storyboard.getHeight() - storyboardScroll.getHeight());
            storyboardScroll.scrollTo(0, (int) (range * fraction));
            invalidate();
        }
    }

    private void cancelFrameExtraction() {
        extractionGeneration.incrementAndGet();
        for (Future<?> job : frameJobs) job.cancel(true);
        frameJobs.clear();
    }

    private void startFrameExtraction(Uri videoUri) {
        cancelFrameExtraction();
        final int generation = extractionGeneration.get();
        for (int i = 0; i < FIXED_FRAME_COUNT; i++) storyboardFrames.set(i, null);
        for (ImageView cell : storyboardCells) cell.setImageDrawable(null);
        currentVideoDurationMs = 0;

        MediaMetadataRetriever metadata = new MediaMetadataRetriever();
        try {
            metadata.setDataSource(this, videoUri);
            String duration = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            currentVideoDurationMs = duration == null ? 0 : Long.parseLong(duration);
        } catch (Exception e) {
            Toast.makeText(this, "Could not read video duration", Toast.LENGTH_SHORT).show();
            return;
        } finally {
            try { metadata.release(); } catch (Exception ignored) { }
        }
        final long durationMs = currentVideoDurationMs;
        if (durationMs <= 0) return;

        // Two independent workers process disjoint frame indexes; UI order remains chronological.
        for (int worker = 0; worker < FRAME_WORKER_COUNT; worker++) {
            
