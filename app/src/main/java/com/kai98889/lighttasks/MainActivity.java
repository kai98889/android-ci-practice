package com.kai98889.lighttasks;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(28, 46, 45);
    private static final int MUTED = Color.rgb(99, 116, 111);
    private static final int ACCENT = Color.rgb(8, 127, 115);
    private TaskList tasks = new TaskList();
    private SharedPreferences preferences;
    private LinearLayout rows;
    private EditText input;
    private TextView progress;
    private boolean readable = true;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("tasks", MODE_PRIVATE);
        load();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(244, 246, 242));
        root.setPadding(dp(24), dp(20), dp(24), dp(16));
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                v.setPadding(dp(24) + bars.left, dp(20) + bars.top, dp(24) + bars.right, dp(16) + bars.bottom);
            } else {
                v.setPadding(dp(24), dp(20) + insets.getSystemWindowInsetTop(), dp(24), dp(16) + insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        setContentView(root);
        root.requestApplyInsets();
        TextView date = text(new SimpleDateFormat("MM.dd  EEEE", Locale.getDefault()).format(new Date()), 14, ACCENT);
        date.setTypeface(null, Typeface.BOLD);
        root.addView(date);
        TextView title = text(getString(R.string.heading), 30, INK);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(16), 0, dp(8));
        root.addView(title);
        root.addView(text(getString(R.string.subtitle), 14, MUTED));
        progress = text("", 14, ACCENT);
        progress.setPadding(0, dp(24), 0, dp(16));
        root.addView(progress);

        LinearLayout compose = new LinearLayout(this);
        compose.setOrientation(LinearLayout.VERTICAL);
        compose.setPadding(dp(16), dp(12), dp(16), dp(12));
        compose.setBackground(shape(Color.WHITE, 18));
        input = new EditText(this);
        input.setTextSize(16);
        input.setTextColor(INK);
        input.setHint(R.string.input_hint);
        input.setSingleLine(true);
        input.setMaxLines(1);
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(160)});
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setEnabled(readable);
        compose.addView(input, new LinearLayout.LayoutParams(-1, dp(56)));
        Button add = new Button(this);
        add.setText(R.string.add);
        add.setTextColor(Color.WHITE);
        add.setAllCaps(false);
        add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        add.setEnabled(readable);
        compose.addView(add, new LinearLayout.LayoutParams(-1, dp(52)));
        add.setOnClickListener(v -> addTask());
        input.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) { addTask(); return true; }
            return false;
        });
        root.addView(compose);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, dp(20), 0, dp(8));
        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(rows);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView footer = text(getString(R.string.local_only), 12, MUTED);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
        render();
    }

    private void addTask() {
        if (!readable) return;
        try { tasks.add(input.getText().toString()); }
        catch (IllegalArgumentException e) {
            input.setError(getString(R.string.blank_task));
            return;
        }
        input.setText("");
        save();
        render();
    }

    private void render() {
        rows.removeAllViews();
        progress.setText(getString(R.string.progress, tasks.completed(), tasks.all().size()));
        if (!readable || tasks.all().isEmpty()) {
            TextView empty = text(getString(readable ? R.string.empty : R.string.storage_error), 16, MUTED);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(12), dp(40), dp(12), dp(40));
            rows.addView(empty);
            return;
        }
        java.util.List<TaskList.Task> snapshot = tasks.all();
        for (int i = 0; i < snapshot.size(); i++) {
            final int index = i;
            TaskList.Task task = snapshot.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(6), dp(8));
            row.setBackground(shape(Color.WHITE, 14));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.bottomMargin = dp(10);
            rows.addView(row, rowParams);
            CheckBox check = new CheckBox(this);
            check.setText(task.text);
            check.setTextSize(16);
            check.setMinHeight(dp(48));
            check.setTextColor(task.done ? MUTED : INK);
            check.setChecked(task.done);
            if (task.done) check.setPaintFlags(check.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            row.addView(check, new LinearLayout.LayoutParams(0, -2, 1));
            check.setOnCheckedChangeListener((button, checked) -> {
                tasks.toggle(index); save(); render();
            });
            Button delete = new Button(this);
            delete.setText("×");
            delete.setTextSize(24);
            delete.setTextColor(MUTED);
            delete.setBackgroundColor(Color.TRANSPARENT);
            delete.setContentDescription(getString(R.string.delete_task, task.text));
            row.addView(delete, new LinearLayout.LayoutParams(dp(48), dp(48)));
            delete.setOnClickListener(v -> { tasks.remove(index); save(); render(); });
        }
    }

    private void load() {
        try {
            JSONArray data = new JSONArray(preferences.getString("items", "[]"));
            TaskList loaded = new TaskList();
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                loaded.restore(item.getString("text"), item.getBoolean("done"));
            }
            tasks = loaded;
        } catch (JSONException | IllegalArgumentException e) {
            readable = false;
        }
    }

    private void save() {
        JSONArray data = new JSONArray();
        for (TaskList.Task task : tasks.all()) {
            JSONObject item = new JSONObject();
            try { item.put("text", task.text); item.put("done", task.done); }
            catch (JSONException e) { throw new IllegalStateException(e); }
            data.put(item);
        }
        preferences.edit().putString("items", data.toString()).apply();
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        return view;
    }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color); background.setCornerRadius(dp(radius));
        return background;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
