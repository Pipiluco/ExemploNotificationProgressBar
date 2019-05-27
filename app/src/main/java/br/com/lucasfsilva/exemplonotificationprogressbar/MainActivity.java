package br.com.lucasfsilva.exemplonotificationprogressbar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import static br.com.lucasfsilva.exemplonotificationprogressbar.App.CHANNEL_ID_01;
import static br.com.lucasfsilva.exemplonotificationprogressbar.App.CHANNEL_ID_02;


public class MainActivity extends AppCompatActivity {
    private NotificationManagerCompat notificationManagerCompat;
    private EditText edtTitulo, edtMensagem;

    static List<Mensagem> MENSAGENS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManagerCompat = NotificationManagerCompat.from(this);

        edtTitulo = (EditText) findViewById(R.id.edtTitulo);
        edtMensagem = (EditText) findViewById(R.id.edtMensagem);

        MENSAGENS.add(new Mensagem("bom dia!", "Lucas"));
        MENSAGENS.add(new Mensagem("Olá!", null));
        MENSAGENS.add(new Mensagem("Oi", "Vívia"));
    }

    public void enviarNoChannel01(View view) {
        enviarNoChannel01Notification(this);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public static void enviarNoChannel01Notification(Context context) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

        RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply").setLabel("Sua resposta...").build();

        Intent replyIntent;
        PendingIntent replyPendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            replyIntent = new Intent(context, DirectReplyReceiver.class);
            replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, 0);
        } else {
            //start chat activity instead (PendingIntent.getActivity)
            //cancel notification with notificationManagerCompat.cancel(id)
        }

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_reply, "Reply", replyPendingIntent).addRemoteInput(remoteInput).build();

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("Me");
        messagingStyle.setConversationTitle("Group Chat");

        for (Mensagem chatMensagem : MENSAGENS) {
            NotificationCompat.MessagingStyle.Message message = new NotificationCompat.MessagingStyle.Message(chatMensagem.getTexto(), chatMensagem.getTimestamp(), chatMensagem.getRemetente());
            messagingStyle.addMessage(message);
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_01)
                .setSmallIcon(R.drawable.ic_one)
                .setStyle(messagingStyle)
                .addAction(replyAction)
                .setColor(Color.BLUE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notification);
    }

    public void enviarNoChannel02(View view) {
        final int progressMax = 100;

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID_02)
                .setSmallIcon(R.drawable.ic_two)
                .setContentTitle("Download")
                .setContentText("Download em progresso!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, true);

        notificationManagerCompat.notify(2, notification.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for (int i = 0; i < progressMax; i += 10) {
                    notification.setProgress(progressMax, i, false); // Sem estas duas linhas de código a barra de progresso não carrega
                    notificationManagerCompat.notify(2, notification.build());// Sem estas duas linhas de código a barra de progresso não carrega
                    SystemClock.sleep(1000);
                }
                notification.setContentText("Download completo!").setProgress(0, 0, false).setOngoing(false);
                notificationManagerCompat.notify(2, notification.build());
                notificationManagerCompat.cancel(2); // Fecha a notificação
            }
        }).start();
    }
}

