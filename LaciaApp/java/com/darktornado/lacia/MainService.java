package com.darktornado.lacia;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainService extends Service {

    private LinearLayout layout;
    private WindowManager mManager;
    private TextView left;
    private BitmapDrawable[] icons = new BitmapDrawable[5];
    TextToSpeech tts;
    private String[] chatData = null;

    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String type = intent.getStringExtra("type");
            startForeground(1, new Notification());
            Notification.Builder noti = new Notification.Builder(this);
            noti.setSmallIcon(R.mipmap.icon);
            noti.setContentTitle("Lacia");
            noti.setContentText("Lacia is Running...");
            noti.setAutoCancel(true);
            noti.setOngoing(false);
            noti.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Thread.sleep(100);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) nm.notify(1, noti.build());
            else nm.notify(1, new Notification());
            Thread.sleep(100);
            nm.cancel(1);
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    tts.setLanguage(Locale.KOREAN);
                }
            });
        } catch (Exception e) {
            toast(e.toString());
        }
        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createLeftArea();
        chatData = intent.getStringArrayExtra("chat_data");
        return START_NOT_STICKY;
    }



    private void createLeftArea() {
        try {
            mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            final WindowManager.LayoutParams mParams;
            if (Build.VERSION.SDK_INT >= 26) {
                mParams = new WindowManager.LayoutParams(
                        dip2px(15), dip2px(22),
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, //2038
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            } else {
                mParams = new WindowManager.LayoutParams(
                        dip2px(15), dip2px(22),
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            }
            mParams.gravity = Gravity.LEFT | Gravity.TOP;
            if (left != null) mManager.removeView(left);
            left = new TextView(this);
            left.setBackgroundColor(Color.argb(0, 0, 0, 0));
            left.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            left.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent ev) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_UP:
                            openListMenu(Gravity.LEFT, -1);
                            break;
                    }
                    return false;
                }
            });

            mManager.addView(left, mParams);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void openListMenu(int gravity, final int side) {
        try {
            final WindowManager.LayoutParams mParams;
            if (Build.VERSION.SDK_INT >= 26) {
                mParams = new WindowManager.LayoutParams(
                        -2, -2,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, //2038
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            } else {
                mParams = new WindowManager.LayoutParams(
                        -2, -2,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            }
            mParams.gravity = gravity | Gravity.TOP;

            final LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(1);
            AssetManager am = this.getAssets();
            String[] images = {"input", "web", "music", "home", "close"};
            final TextView[] btns = new TextView[5];
            int mar = dip2px(5);
            LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(dip2px(45), dip2px(45));
            margin.setMargins(mar, mar, mar, mar);
            for (int n = 0; n < 5; n++) {
                btns[n] = new TextView(this);
                if (icons[n] == null) {
                    icons[n] = new BitmapDrawable(BitmapFactory.decodeStream(am.open("icon/" + images[n] + ".png")));
                    icons[n].setAlpha(220);
                }
                btns[n].setBackgroundDrawable(icons[n]);
                btns[n].setId(n);
                btns[n].setLayoutParams(margin);
                ViewCompat.setElevation(btns[n], dip2px(3));
                btns[n].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case 0:
                                inputVoice();
                                break;
                            case 1:
                                openActivity(WebActivity.class);
                                break;
                            case 2:
                                startService(new Intent(MainService.this, MusicService.class));
                                say("음악을 재생합니다.");
                                break;
                            case 3:
                                openActivity(MainActivity.class);
                                break;
                        }

                        ScaleAnimation ani = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        ani.setDuration(400);

                        for (int n = 0; n < 5; n++) {
                            btns[n].startAnimation(ani);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mManager.removeView(layout);
                            }
                        }, 300);
                    }
                });
                ScaleAnimation ani = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ani.setDuration(400);
                btns[n].setAnimation(ani);
                layout.addView(btns[n]);
            }
            btns[0].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    inputChat();

                    ScaleAnimation ani = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ani.setDuration(400);
                    for (int n = 0; n < 5; n++) {
                        btns[n].startAnimation(ani);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mManager.removeView(layout);
                        }
                    }, 300);
                    return true;
                }
            });
            layout.setBackgroundColor(Color.TRANSPARENT);

            mManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            mManager.addView(layout, mParams);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    public void inputVoice() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("Input Ready");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    toast("Input End");
                }

                @Override
                public void onError(int error) {
                    try {
                        toast("Error Code : " + error);
                        stt.destroy();
                    } catch (Exception e) {
                        toast("OnError\n" + e.toString());
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    final ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (result.get(0).contains(("러스티"))) {
                        result.add(0, result.get(0).replace("러스티", "너스티"));
                    }
                    if (result.get(0).contains(("러스트"))) {
                        result.add(0, result.get(0).replace("러스트", "너스티"));
                    }
                    if (result.get(0).contains(("비너스")) && Math.floor(Math.random() * 2) == 0) {
                        result.add(0, result.get(0).replace("비너스", "티너스"));
                    }
                    final String que = (String) result.get(0);
                    stt.destroy();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showAnswer(que);
                        }
                    }, 500);
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void inputChat(){
        try {
            final FakeDialog dialog = new FakeDialog(this);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(1);
            TextView txt1 = new TextView(this);
            txt1.setText("채팅 : ");
            txt1.setTextSize(18);
            txt1.setTextColor(Color.BLACK);
            layout.addView(txt1);
            final EditText txt2 = new EditText(this);
            txt2.setHint("채팅을 입력하세요...");
            txt2.setTextColor(Color.BLACK);
            txt2.setHintTextColor(Color.GRAY);
            txt2.setSingleLine(true);
            layout.addView(txt2);
            int pad = dip2px(10);
            layout.setPadding(pad, pad, pad, pad);
            ScrollView scroll = new ScrollView(this);
            scroll.addView(layout);
            dialog.setView(scroll);
            dialog.setTitle("채팅 입력");
            dialog.setInputEnabled(true);
            dialog.setNegativeButton("취소", null);
            dialog.setPositiveButton("확인", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String input = txt2.getText().toString();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showAnswer(input);
                        }
                    }, 1000);
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    public void showAnswer(String msg) {
        try{
            if(msg.equals("길 찾기")) msg = "길찾기";
            String cmd = msg.split(" ")[0];
            final String data = msg.replaceFirst(cmd+" ", "");
            Calendar day = Calendar.getInstance();
            switch(cmd) {
                case "음악":
                case "노래":
                    if(data.equals("정지")){
                        stopService(new Intent(this, MusicService.class));
                        say("음악을 정지합니다.");
                    }else {
                        startService(new Intent(this, MusicService.class));
                        say("음악을 재생합니다.");
                    }
                    break;
                case "검색":
                    say(data+"에 대한 검색결과입니다.");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showWebView(data);
                        }
                    }, 1000);
                    break;
                case "길찾기":
                    say("길찾기를 실행합니다.");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showWebView("길찾기");
                        }
                    }, 1000);
                    break;
                case "노선도":
                    say("노선도를 띄웁니다.");
                    openActivity(MetroActivity.class);
                    break;
                case "시간":
                    say("현재 시각은 " + day.get(Calendar.HOUR) + "시 " + day.get(Calendar.MINUTE) + "분 " + day.get(Calendar.SECOND) + "초입니다.");
                    break;
                case "달력":
                    openActivity(CalendarActivity.class);
                    say("달력을 띄웁니다.");
                    break;
                case "날씨":
                    if(msg.equals("날씨")) {
                        say("전국 날씨입니다.");
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                showWebView("전국 날씨");
                            }
                        }, 500);
                    } else {
                        say(data+"의 날씨 정보입니다.");
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String[] info = Lacia.getWeather(data);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(info==null) toast("해당 지역을 찾을 수 없습니다.");
                                                else showDialog(data+" 날씨 : "+info[0], info[1]);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }, 500);
                    }
                    break;
                case "와이파이":
                    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean wifiOn = wm.isWifiEnabled();
                    if (data.equals("꺼")) {
                        if (wifiOn) {
                            wm.setWifiEnabled(false);
                            say("와이파이를 껐습니다.");
                        } else {
                            say("이미 와이파이가 꺼진 상태입니다.");
                        }
                    } else {
                        if (!wifiOn) {
                            wm.setWifiEnabled(true);
                            say("와이파이를 켰습니다.");
                        } else {
                            say("이미 와이파이가 켜진 상태입니다.");
                        }
                    }
                    break;
                case "번역":
                case "번역기":
                    say("번역기를 실행합니다.");
                    openActivity(Translator.class);
                    break;
                default:
                    String reply = Lacia.getReply(chatData, msg);
                    if(reply==null) say("무슨 말인지 잘 모르겠어요.");
                    else say(reply);
                    break;
            }

        }catch (Exception e){
            toast(e.toString());
        }
    }

    private void openActivity(Class className) {
        Intent intent = new Intent(this, className);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showWebView(String data){
        Intent intent = new Intent(this, WebActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("data", data);
        startActivity(intent);
    }


    private void say(String chat) {
        tts.speak(chat, TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(left!=null){
            mManager.removeView(left);
            left = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showDialog(String title, String msg){
        try {
            FakeDialog dialog = new FakeDialog(this);
            dialog.setTitle(title);
            dialog.setMessage(msg);
            dialog.setNegativeButton("닫기", null);
            dialog.show();
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    public void toast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                toast.getView().setBackgroundColor(Lacia.getColor(190));
                int pad = dip2px(5);
                toast.getView().setPadding(pad, pad, pad, pad);
                toast.show();
            }
        });
    }


    public int dip2px(int dips) {
        return (int) Math.ceil(dips * this.getResources().getDisplayMetrics().density);
    }


}
