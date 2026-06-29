/*
 * Copyright 2024 TimeCalc Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timecalc;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int MAX_EXPR_LENGTH = 200;
    private static final int MAX_ACTIVE_DIGITS = 20;

    private TextView displayView;
    private TextView hintView;
    private TextView activeView;

    private double total = 0.0;
    private String activeStr = "0";
    private final StringBuilder committedExpr = new StringBuilder();
    private boolean dotUsed = false;

    private final Handler timer = new Handler();
    private Runnable commitTask;
    private Runnable hintTask;

    private int screenW;
    private int screenH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screenW = getResources().getDisplayMetrics().widthPixels;
        screenH = getResources().getDisplayMetrics().heightPixels;

        if (savedInstanceState != null) {
            total = savedInstanceState.getDouble("total", 0.0);
            committedExpr.replace(0, committedExpr.length(),
                    savedInstanceState.getString("expr", ""));
            activeStr = savedInstanceState.getString("active", "0");
            dotUsed = savedInstanceState.getBoolean("dot", false);
        }

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0d1117"));
        setContentView(root);

        // ---- Display area (top, weight 3) ----
        LinearLayout displayArea = new LinearLayout(this);
        displayArea.setOrientation(LinearLayout.VERTICAL);
        displayArea.setPadding(screenW / 16, screenH / 35, screenW / 16, screenH / 60);
        root.addView(displayArea, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.0f));

        // displayView — shows total + expression
        displayView = new TextView(this);
        displayView.setText("0");
        displayView.setTextColor(Color.parseColor("#58a6ff"));
        displayView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        displayView.setPadding(screenW / 20, 0, screenW / 20, 0);
        displayView.setBackground(roundedBg(Color.parseColor("#161b22"), px(10.0f)));
        displayArea.addView(displayView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.1f));

        // hintView — transient feedback ("+ 5", "cleared")
        hintView = new TextView(this);
        hintView.setText("");
        hintView.setTextColor(Color.parseColor("#484f58"));
        hintView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        hintView.setPadding(screenW / 20, 0, screenW / 20, 0);
        displayArea.addView(hintView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.6f));

        // activeView — current number being typed
        activeView = new TextView(this);
        activeView.setText("0");
        activeView.setTextColor(Color.WHITE);
        activeView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        activeView.setPadding(screenW / 16, 0, screenW / 16, 0);
        activeView.setBackground(roundedBg(Color.parseColor("#161b22"), px(12.0f)));
        displayArea.addView(activeView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.8f));

        // ---- Button grid (bottom, weight 4) ----
        final LinearLayout buttonArea = new LinearLayout(this);
        buttonArea.setOrientation(LinearLayout.VERTICAL);
        buttonArea.setPadding(screenW / 16, screenH / 70, screenW / 16, screenH / 40);
        root.addView(buttonArea, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 4.0f));

        final String[][] btnLabels = {
                {"7", "8", "9", "\u232B"},
                {"4", "5", "6", "+"},
                {"1", "2", "3", "="},
                {"00", "0", ".", "C"}
        };

        final int gapPx = px(2.0f);

        for (int row = 0; row < 4; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setWeightSum(4.0f);
            buttonArea.addView(rowLayout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

            for (int col = 0; col < 4; col++) {
                final String label = btnLabels[row][col];
                Button btn = new Button(this);
                btn.setText(label);
                btn.setGravity(Gravity.CENTER);
                btn.setAllCaps(false);
                btn.setPadding(0, 0, 0, 0);
                btn.setElevation(px(2.0f));

                if ("=".equals(label)) {
                    btn.setBackground(btnBg("#238636", "#2ea043"));
                    btn.setTextColor(Color.WHITE);
                } else if ("C".equals(label)) {
                    btn.setBackground(btnBg("#da3633", "#f85149"));
                    btn.setTextColor(Color.WHITE);
                } else if ("\u232B".equals(label)) {
                    btn.setBackground(btnBg("#21262d", "#30363d"));
                    btn.setTextColor(Color.parseColor("#f85149"));
                } else if ("+".equals(label)) {
                    btn.setBackground(btnBg("#1f6feb", "#388bfd"));
                    btn.setTextColor(Color.WHITE);
                } else {
                    btn.setBackground(btnBg("#21262d", "#30363d"));
                    btn.setTextColor(Color.WHITE);
                }

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                lp.setMargins(
                        col == 0 ? 0 : gapPx,
                        gapPx,
                        col == 3 ? 0 : gapPx,
                        gapPx);
                btn.setLayoutParams(lp);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonClick(label);
                    }
                });

                rowLayout.addView(btn);
            }
        }

        // Post-layout font sizing
        root.post(new Runnable() {
            @Override
            public void run() {
                float fPx;

                if (displayView.getHeight() > 0)
                    displayView.setTextSize(0, displayView.getHeight() * 0.16f);
                if (hintView.getHeight() > 0)
                    hintView.setTextSize(0, hintView.getHeight() * 0.35f);
                if (activeView.getHeight() > 0)
                    activeView.setTextSize(0, activeView.getHeight() * 0.32f);

                if (buttonArea.getChildCount() > 0) {
                    ViewGroup firstRow = (ViewGroup) buttonArea.getChildAt(0);
                    if (firstRow.getChildCount() > 0) {
                        fPx = firstRow.getChildAt(0).getHeight() * 0.30f;
                    } else {
                        fPx = 0.0f;
                    }
                } else {
                    fPx = 0.0f;
                }

                if (fPx < 1.0f) {
                    fPx = px(14.0f);
                }

                for (int r = 0; r < buttonArea.getChildCount(); r++) {
                    ViewGroup row = (ViewGroup) buttonArea.getChildAt(r);
                    for (int c = 0; c < row.getChildCount(); c++) {
                        ((Button) row.getChildAt(c)).setTextSize(0, fPx);
                    }
                }
            }
        });

        updateDisplay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("total", total);
        outState.putString("expr", committedExpr.toString());
        outState.putString("active", activeStr);
        outState.putBoolean("dot", dotUsed);
    }

    // --------------------------------------------------------- Button Click

    private void onButtonClick(String label) {
        cancelCommit();
        cancelHintReset();

        switch (label) {
            case "C":
                total = 0.0;
                committedExpr.setLength(0);
                activeStr = "0";
                dotUsed = false;
                setHint("cleared", "#f85149");
                updateDisplay();
                break;

            case "\u232B":  // backspace
                if (activeStr.length() <= 1) {
                    activeStr = "0";
                    dotUsed = false;
                } else {
                    if (activeStr.charAt(activeStr.length() - 1) == '.') {
                        dotUsed = false;
                    }
                    activeStr = activeStr.substring(0, activeStr.length() - 1);
                    if (activeStr.matches("0+")) {
                        activeStr = "0";
                        dotUsed = false;
                    }
                }
                updateDisplay();
                if (!activeStr.equals("0")) {
                    scheduleCommit();
                }
                break;

            case "+":
            case "=":
                doCommit();
                break;

            case ".":
                if (!dotUsed) {
                    if (activeStr.equals("0")) {
                        activeStr = "0";
                    }
                    activeStr += ".";
                    dotUsed = true;
                }
                updateDisplay();
                scheduleCommit();
                break;

            case "00":
                if (!activeStr.equals("0") && activeStr.length() < MAX_ACTIVE_DIGITS) {
                    activeStr += "00";
                    updateDisplay();
                    scheduleCommit();
                }
                break;

            default:  // digits 0-9
                if (activeStr.equals("0")) {
                    activeStr = label;
                } else if (activeStr.length() < MAX_ACTIVE_DIGITS) {
                    activeStr += label;
                }
                updateDisplay();
                scheduleCommit();
                break;
        }
    }

    // --------------------------------------------------------- Commit

    private void doCommit() {
        try {
            String num = activeStr;

            // Strip leading zeros (preserve "0." / "0,")
            if (num.length() > 1 && num.startsWith("0")
                    && !num.startsWith("0.") && !num.startsWith("0,")) {
                num = num.replaceFirst("^0+", "");
            }

            double value = Double.parseDouble(num);

            if (!num.equals("0") && !num.equals("0.") && !num.equals("0.0")
                    && !Double.isInfinite(value) && !Double.isNaN(value)) {

                total = new BigDecimal(total + value)
                        .setScale(10, RoundingMode.HALF_UP)
                        .doubleValue();

                String formatted = formatNumber(value);

                if (committedExpr.length() > 0) {
                    committedExpr.append(" + ");
                }
                committedExpr.append(formatted);

                if (committedExpr.length() > MAX_EXPR_LENGTH) {
                    String trimmed = committedExpr.substring(
                            committedExpr.length() - MAX_EXPR_LENGTH + 10);
                    committedExpr.setLength(0);
                    committedExpr.append(trimmed);
                    int idx = trimmed.indexOf(" + ");
                    if (idx >= 0) {
                        committedExpr.replace(0, idx + 3, "... ");
                    }
                }

                setHint("+ " + formatted, "#58a6ff");

                activeStr = "0";
                dotUsed = false;
                updateDisplay();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void scheduleCommit() {
        cancelCommit();
        commitTask = new Runnable() {
            @Override
            public void run() {
                doCommit();
            }
        };
        timer.postDelayed(commitTask, 800L);
    }

    private void cancelCommit() {
        if (commitTask != null) {
            timer.removeCallbacks(commitTask);
            commitTask = null;
        }
    }

    // --------------------------------------------------------- Hints

    private void setHint(String text, String colorHex) {
        cancelHintReset();
        hintView.setTextColor(Color.parseColor(colorHex));
        hintView.setText(text);
        hintTask = new Runnable() {
            @Override
            public void run() {
                hintView.setText("");
            }
        };
        timer.postDelayed(hintTask, 2000L);
    }

    private void cancelHintReset() {
        if (hintTask != null) {
            timer.removeCallbacks(hintTask);
            hintTask = null;
        }
    }

    // --------------------------------------------------------- Display

    private void updateDisplay() {
        String num = formatNumber(total);
        if (committedExpr.length() > 0) {
            displayView.setText("= " + num + "  (" + committedExpr.toString() + ")");
        } else {
            displayView.setText(num);
        }
        activeView.setText(activeStr);
    }

    // --------------------------------------------------------- Formatting

    private String formatNumber(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "0";
        }

        if (d != Math.floor(d) || Double.isInfinite(d)) {
            // Has fractional part
            String s = String.format(Locale.US, "%,.10f", d);
            while (s.contains(".")) {
                if (s.endsWith("0") || s.endsWith(".")) {
                    if (s.endsWith(".")) {
                        return s.substring(0, s.length() - 1);
                    }
                    s = s.substring(0, s.length() - 1);
                } else {
                    return s;
                }
            }
            return s;
        }

        // Integer
        if (Math.abs(d) < 1.0E15) {
            return String.format(Locale.US, "%,d", (long) d);
        }

        // Large integer — manual comma insertion
        String digits = String.format(Locale.US, "%.0f", d);
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        boolean negative = digits.startsWith("-");
        for (int i = 0; i < len; i++) {
            if (i > (negative ? 1 : 0) && (len - i) % 3 == 0) {
                sb.append(',');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    // --------------------------------------------------------- Drawable Helpers

    private int px(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBg(int color, int radiusPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radiusPx);
        return gd;
    }

    private StateListDrawable btnBg(String normalHex, String pressedHex) {
        StateListDrawable sld = new StateListDrawable();
        int radius = px(8.0f);
        GradientDrawable normal = roundedBg(Color.parseColor(normalHex), radius);
        GradientDrawable pressed = roundedBg(Color.parseColor(pressedHex), radius);
        GradientDrawable disabled = roundedBg(Color.parseColor("#151b23"), radius);

        sld.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sld.addState(new int[]{-android.R.attr.state_enabled}, disabled);
        sld.addState(new int[]{}, normal);
        return sld;
    }
}
