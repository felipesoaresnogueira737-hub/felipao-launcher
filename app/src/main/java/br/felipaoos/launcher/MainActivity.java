package br.felipaoos.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

/**
 * FELIPÃO OS - launcher minimalista.
 * Uma única tela inicial, sem serviços em segundo plano e sem animações pesadas.
 */
public class MainActivity extends Activity {

    private static final int BLUE = 0xFF3B82FF;
    private static final int PURPLE = 0xFFA855F7;
    private static final int DIM = 0xFF8A90B4;
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    // Ordem de tentativa para abrir o YouTube
    private static final String[] YOUTUBE = {
            "com.google.android.youtube.tv",
            "com.google.android.youtube",
            "com.liskovsoft.smarttubetv"
    };

    private LinearLayout home;
    private LinearLayout settings;
    private Button ytButton;
    private Button settingsFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        home = buildHome();
        settings = buildSettings();
        root.addView(home, new FrameLayout.LayoutParams(MATCH, MATCH));
        root.addView(settings, new FrameLayout.LayoutParams(MATCH, MATCH));

        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setFormat12Hour("HH:mm");
        clock.setTextColor(DIM);
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        FrameLayout.LayoutParams clp =
                new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP | Gravity.END);
        clp.setMargins(0, dp(20), dp(28), 0);
        root.addView(clock, clp);

        setContentView(root);
        showHome();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showHome(); // botão HOME do controle sempre volta para a tela inicial
    }

    @Override
    public void onBackPressed() {
        if (settings.getVisibility() == View.VISIBLE) {
            showHome();
        }
        // Na tela inicial o BACK é ignorado: a home não pode "fechar".
    }

    // ---------------------------------------------------------------- telas

    private LinearLayout buildHome() {
        LinearLayout col = column();

        col.addView(logo(), margins(0, 0, 0, dp(48)));

        ytButton = makeButton("Abrir YouTube", 30, dp(64), dp(26), true);
        ytButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openYouTube(); }
        });
        col.addView(ytButton, margins(0, 0, 0, dp(28)));

        Button cfg = makeButton("Configurações", 18, dp(30), dp(12), false);
        cfg.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showSettings(); }
        });
        col.addView(cfg, margins(0, 0, 0, 0));
        return col;
    }

    private LinearLayout buildSettings() {
        LinearLayout col = column();

        TextView title = new TextView(this);
        title.setText("Configurações");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        col.addView(title, margins(0, 0, 0, dp(24)));

        settingsFirst = addSetting(col, "Wi-Fi e rede", Settings.ACTION_WIFI_SETTINGS);
        addSetting(col, "Tela e vídeo", Settings.ACTION_DISPLAY_SETTINGS);
        addSetting(col, "Data e hora", Settings.ACTION_DATE_SETTINGS);
        addSetting(col, "Sobre o aparelho", Settings.ACTION_DEVICE_INFO_SETTINGS);
        addSetting(col, "Todas as configurações", Settings.ACTION_SETTINGS);

        Button back = makeButton("Voltar", 18, dp(30), dp(12), false);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showHome(); }
        });
        col.addView(back, new LinearLayout.LayoutParams(dp(380), WRAP));
        return col;
    }

    private Button addSetting(LinearLayout col, String label, final String action) {
        Button b = makeButton(label, 18, dp(30), dp(12), false);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openSystem(action); }
        });
        col.addView(b, margins2(dp(380), 0, 0, 0, dp(12)));
        return b;
    }

    private void showHome() {
        settings.setVisibility(View.GONE);
        home.setVisibility(View.VISIBLE);
        ytButton.requestFocus();
    }

    private void showSettings() {
        home.setVisibility(View.GONE);
        settings.setVisibility(View.VISIBLE);
        settingsFirst.requestFocus();
    }

    // ------------------------------------------------------------- ações

    private void openYouTube() {
        PackageManager pm = getPackageManager();
        for (String pkg : YOUTUBE) {
            Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (i == null) i = pm.getLaunchIntentForPackage(pkg);
            if (i != null) {
                try {
                    startActivity(i);
                    overridePendingTransition(0, 0);
                    return;
                } catch (Exception ignored) {
                    // tenta o próximo pacote
                }
            }
        }
        Toast.makeText(this, "YouTube não está instalado nesta caixa", Toast.LENGTH_LONG).show();
    }

    private void openSystem(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception e) {
            Toast.makeText(this, "Não disponível neste aparelho", Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------------------------- construção de UI

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        return l;
    }

    private LinearLayout logo() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.addView(neon("FELIPÃO", 0xFFCFE0FF, BLUE));
        row.addView(neon("OS", 0xFFEAD5FF, PURPLE), margins(dp(18), 0, 0, 0));
        return row;
    }

    private TextView neon(String text, int core, int glow) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
        t.setTextColor(core);
        t.setLetterSpacing(0.06f);
        t.setShadowLayer(dp(14), 0, 0, glow);
        return t;
    }

    private Button makeButton(String text, int sp, int padH, int padV, boolean big) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        b.setPadding(padH, padV, padH, padV);
        b.setFocusable(true);
        b.setStateListAnimator(null);

        GradientDrawable normal;
        if (big) {
            normal = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xFF1D3A8A, 0xFF5B21B6});
        } else {
            normal = new GradientDrawable();
            normal.setColor(0xFF0C0C18);
            normal.setStroke(dp(1), 0xFF1E1E3A);
        }
        normal.setCornerRadius(dp(big ? 22 : 14));

        GradientDrawable focused = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{BLUE, PURPLE});
        focused.setCornerRadius(dp(big ? 22 : 14));
        focused.setStroke(dp(3), Color.WHITE);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        states.addState(new int[]{}, normal);
        b.setBackground(states);

        b.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean hasFocus) {
                float s = hasFocus ? 1.06f : 1f;
                v.animate().scaleX(s).scaleY(s).setDuration(120).start();
            }
        });
        return b;
    }

    private LinearLayout.LayoutParams margins(int l, int t, int r, int b) {
        return margins2(WRAP, l, t, r, b);
    }

    private LinearLayout.LayoutParams margins2(int w, int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, WRAP);
        lp.setMargins(l, t, r, b);
        return lp;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
