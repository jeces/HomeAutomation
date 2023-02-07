package com.example.project_01

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        var ip = "192.168.0.4"
        var port = 8083
        var cod = ""
        var window_state = ""
        var blind_state = ""
        var door_state = ""
        var fan_state = ""
        var led_state = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        /* RPI 에서 데이터 받아오기 */
        Thread(Runnable {
            while (!Thread.interrupted()) try {
                Thread.sleep(1000)
                var mSendData: SendData? = null
                mSendData = SendData(cod)
                mSendData.start()
            } catch (e: InterruptedException) {

            }
        }).start()

        /* 맨 아래 text 현재 날짜와 시간 나타내기 */
        Thread(Runnable {
            while (!Thread.interrupted()) try {
                Thread.sleep(1000)
                val currentDateTime = Calendar.getInstance().time
                var dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)
                runOnUiThread { textView_time.setText(dateFormat)}

            } catch (e: InterruptedException) {

            }
        }).start()

        /* 영상 액티비티 창 띄우기 */
        this.imageButton_cctv.setOnClickListener {
            /* 인텐트 객체 만들기 */
            val intent = Intent(this, CameraActivity::class.java)
            /* 새로운 CameraActivity 창 띄우기 */
            startActivity(intent)
        }

        /* 블라인드 버튼 누를 시 팝업창 띄움 */
        imageButton_blind.setOnClickListener {
            showSettingPopup_blind()
        }
        /* 창문 버튼 누를 시 팝업창 띄움 */
        imageButton_window.setOnClickListener {
            showSettingPopup_window()
        }
        /* 환풍기 버튼 누를 시 팝업창 띄움 */
        imageButton_fan.setOnClickListener {
            showSettingPopup_fan()
        }
        /* LED 버튼 누를 시 팝업창 띄움 */
        imageButton_led.setOnClickListener {
            showSettingPopup_led()
        }
    }

    /* 푸시 알림 채널 */
    private fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean,
                                          name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /* RPI 와 통신 */
    inner class SendData(s: String) : Thread() {

        var s1 = s
        override fun run() {
            try {
                //수신 데이터 임의크기 설정
                val buf2 = "                                                                                                              ".toByteArray()
                //UDP 통신용 소켓 생성
                val socket = DatagramSocket()
                //서버 주소 변수
                val serverAddr = InetAddress.getByName(ip)

                //보낼 데이터 생성
                var buf = s1.toByteArray()

                //패킷으로 변경
                val packet = DatagramPacket(buf, buf.size, serverAddr, port)
                val packet2 = DatagramPacket(buf2, buf2.size, serverAddr, port)

                //패킷 전송!
                socket.send(packet)

                cod = ""
                //데이터 수신 대기
                socket.receive(packet2)
                //데이터 수신되었다면 문자열로 변환
                val msg = String(packet2.data)
                println(msg)

                //string 에 표시
                var msg1 = msg.trim()
                var str1 = msg1.split(',')
                println(str1)
//                textView_api_dust.text = "${str1[0]} ㎍/m³"

                if (str1[0].toDouble() <= 30.0)
                {
                    textView_api_dust.text = "${str1[0]} ㎍/m³ (좋음)"
                    textView_api_dust.setTextColor(Color.GREEN)
                }
                else if (str1[0].toDouble() <= 80)
                {
                    textView_api_dust.text = "${str1[0]} ㎍/m³ (보통)"
                    textView_api_dust.setTextColor(Color.YELLOW)
                }
                else if (str1[0].toDouble() <= 150)
                {
                    textView_api_dust.text = "${str1[0]} ㎍/m³ (나쁨)"
                    textView_api_dust.setTextColor(Color.MAGENTA)
                }
                else {
                    textView_api_dust.text = "${str1[0]} ㎍/m³ (매우 나쁨)"
                    textView_api_dust.setTextColor(Color.RED)
                }

                textView_api_humi.text = "${str1[1]} %"
                textView_api_temper.text = "${str1[2]} ˚C"

                textView_temper.text = "${str1[3]} ˚C"
                textView_humi.text = "${str1[4]} %"

                if (str1[5].toDouble() <= 30)
                {
                    textView_dust.text = "${str1[5]} ㎍/m³ (좋음)"
                    textView_dust.setTextColor(Color.GREEN)
                }
                else if (str1[5].toDouble() <= 80)
                {
                    textView_dust.text = "${str1[5]} ㎍/m³ (보통)"
                    textView_dust.setTextColor(Color.YELLOW)
                }
                else if (str1[5].toDouble() <= 150)
                {
                    textView_dust.text = "${str1[5]} ㎍/m³ (나쁨)"
                    textView_dust.setTextColor(Color.MAGENTA)
                }
                else {
                    textView_dust.text = "${str1[5]} ㎍/m³ (매우 나쁨)"
                    textView_dust.setTextColor(Color.RED)
                }
                println(str1[6])
                if (str1[6] == "Wclose")
                {
                    imageView_windowstate.setImageResource(R.drawable.icons_closedwindow)
                    window_state = "Wclose"
                    headup_window_state()
                }
                else if (str1[6] == "Wopen") {
                    imageView_windowstate.setImageResource(R.drawable.icons_openwindow)
                    window_state = "Wopen"
                    headup_window_state()
                }
                else if (str1[6] == "Wstop"){
                    imageView_windowstate.setImageResource(R.drawable.icons_stop)
                    window_state = "Wstop"
                    headup_window_state()
                }
                if (str1[7] == "Bclose")
                {
                    imageView_blindstate.setImageResource(R.drawable.icons_closedblind)
                    blind_state = "Bclose"
                    headup_blind_state()
                }
                else if (str1[7] == "Bopen") {
                    imageView_blindstate.setImageResource(R.drawable.icons_openblind)
                    blind_state = "Bopen"
                    headup_blind_state()
                }
                else if (str1[7] == "Bstop"){
                    imageView_blindstate.setImageResource(R.drawable.icons_stop)
                    blind_state = "Bstop"
                    headup_blind_state()
                }
//                if ("${str1[5]}" == "닫힘")
//                {
//                    imageView_doorstate.setImageResource(R.drawable.icons_closeddoor)
//                    door_state = "닫힘"
//                    headup_door_state()
//                }
//                else {
//                    imageView_doorstate.setImageResource(R.drawable.icons_opendoor)
//                    door_state = "열림"
//                    headup_door_state()
//                }
                if (str1[8] == "Foff")
                {
                    imageView_fanstate.setImageResource(R.drawable.icons_fanoff)
                    fan_state = "Foff"
                    headup_fan_state()
                }
                else if(str1[8] == "Fon"){
                    imageView_fanstate.setImageResource(R.drawable.icons_fanon)
                    fan_state = "Fon"
                    headup_fan_state()
                }
                if (str1[9] == "Loff")
                {
                    imageView_ledstate.setImageResource(R.drawable.icons_ledoff)
                    led_state = "Loff"
                    headup_led_state()
                }
                else if(str1[9] == "Lon"){
                    imageView_ledstate.setImageResource(R.drawable.icons_ledon)
                    led_state = "Lon"
                    headup_led_state()
                }

            } catch (e: Exception) {
                println("연결실패")
            }
        }
    }

    /* 창문 푸시 알림 부분 */
    private fun headup_window_state()
    {
        if (window_state == "Wopen") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "창문 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 창문이 열렸습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(1, builder.build())    // 11

        }
        if (window_state == "Wclose") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "창문 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 창문이 닫혔습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(1, builder.build())    // 11
        }
        if (window_state == "Wstop") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "창문 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 창문을 멈췄습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(1, builder.build())    // 11
        }
    }

    /* 블라인드 푸시 알림 부분 */
    private fun headup_blind_state()
    {
        if (blind_state == "Bopen") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "블라인드 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 블라인드가 열렸습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(2, builder.build())    // 11
        }
        if (blind_state == "Bclose") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "블라인드 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 블라인드가 닫혔습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(2, builder.build())    // 11
        }
        if (blind_state == "Bstop") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "블라인드 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 블라인드를 멈췄습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(2, builder.build())    // 11
        }
    }

    /* 현관문 푸시 알림 부분 */
    private fun headup_door_state()
    {
        if (door_state == "Dopen") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "현관문 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 현관문이 열렸습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(3, builder.build())    // 11
        }
        if (door_state == "Dclose") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "현관문 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 현관문이 닫혔습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(3, builder.build())    // 11
        }
    }

    /* 환풍기 푸시 알림 부분 */
    private fun headup_fan_state()
    {
        if (fan_state == "Fon") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "환풍기 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 환풍기가 켜졌습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(4, builder.build())    // 11
        }
        if (fan_state == "Foff") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "환풍기 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - 환풍기가 꺼졌습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(4, builder.build())    // 11
        }
    }

    /* LED 푸시 알림 부분 */
    private fun headup_led_state()
    {
        if (led_state == "Lon") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "LED 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - LED 가 켜졌습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(5, builder.build())    // 11
        }
        if (led_state == "Loff") {
            createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_HIGH,
                    false, getString(R.string.app_name), "App notification channel") // 1

            val channelId = "$packageName-${getString(R.string.app_name)}" // 2
            val title = "LED 상태 알림"
            val content = "${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)} - LED 가 꺼졌습니다 !"

            val intent = Intent(baseContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val fullScreenPendingIntent = PendingIntent.getActivity(baseContext, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

            val builder = NotificationCompat.Builder(this, channelId)  // 4
            builder.setSmallIcon(R.drawable.icons_alarm)    // 5
            builder.setContentTitle(title)    // 6
            builder.setContentText(content)    // 7
            builder.priority = NotificationCompat.PRIORITY_HIGH    // 8
            builder.setAutoCancel(true)   // 9
            builder.setFullScreenIntent(fullScreenPendingIntent, true)   // 10

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(5, builder.build())    // 11
        }
    }

    /* 창문 버튼 누를 시 뜨는 팝업창 부분 */
    private fun showSettingPopup_window() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("창문 작동 제어")
        builder.setMessage("원하는 작동을 선택하십시오.")
        builder.setIcon(R.drawable.icons_alarm)

        var listener = object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when (p1) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        cod = "W11" // 창문 OPEN
                        Toast.makeText(applicationContext, "창문 OPEN!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        window_state = "Wopen"
                        headup_window_state()
                        imageView_windowstate.setImageResource(R.drawable.icons_openwindow)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        cod = "W10" // 창문 CLOSE
                        Toast.makeText(applicationContext, "창문 CLOSE!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        window_state = "Wclose"
                        headup_window_state()
                        imageView_windowstate.setImageResource(R.drawable.icons_closedwindow)
                    }
                    DialogInterface.BUTTON_NEUTRAL -> {
                        cod = "W00" // 창문 STOP
                        Toast.makeText(applicationContext, "창문 STOP!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        window_state = "Wstop"
                        headup_window_state()
                        imageView_windowstate.setImageResource(R.drawable.icons_stop)
                    }
                }
            }
        }
        builder.setPositiveButton("OPEN", listener)
        builder.setNegativeButton("CLOSE", listener)
        builder.setNeutralButton("STOP", listener)
        builder.show()
    }

    /* 블라인드 버튼 누를 시 뜨는 팝업창 부분 */
    private fun showSettingPopup_blind() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("블라인드 작동 제어")
        builder.setMessage("원하는 작동을 선택하십시오.")
        builder.setIcon(R.drawable.icons_alarm)

        var listener = object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when (p1) {

                    DialogInterface.BUTTON_POSITIVE -> {
                        cod = "B11" // 블라인드 OPEN
                        Toast.makeText(applicationContext, "블라인드 OPEN!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        blind_state = "Bopen"
                        headup_blind_state()
                        imageView_blindstate.setImageResource(R.drawable.icons_openblind)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        cod = "B10" // 블라인드 CLOSE
                        Toast.makeText(applicationContext, "블라인드 CLOSE!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        blind_state = "Bclose"
                        headup_blind_state()
                        imageView_blindstate.setImageResource(R.drawable.icons_closedblind)
                    }
                    DialogInterface.BUTTON_NEUTRAL -> {
                        cod = "B00" // 블라인드 STOP
                        Toast.makeText(applicationContext, "블라인드 STOP!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        blind_state = "Bstop"
                        headup_blind_state()
                        imageView_blindstate.setImageResource(R.drawable.icons_stop)
                    }
                }
            }
        }
        builder.setPositiveButton("OPEN", listener)
        builder.setNegativeButton("CLOSE", listener)
        builder.setNeutralButton("STOP", listener)
        builder.show()
    }

    /* 환풍기 버튼 누를 시 뜨는 팝업창 부분 */
    private fun showSettingPopup_fan() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("환풍기 작동 제어")
        builder.setMessage("원하는 작동을 선택하십시오.")
        builder.setIcon(R.drawable.icons_alarm)

        var listener = object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when (p1) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        cod = "F11" // 환풍기 ON
                        Toast.makeText(applicationContext, "환풍기 ON!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        fan_state = "Fon"
                        headup_fan_state()
                        imageView_fanstate.setImageResource(R.drawable.icons_fanon)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        cod = "F00" // 환풍기 OFF
                        Toast.makeText(applicationContext, "환풍기 OFF!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        fan_state = "Foff"
                        headup_fan_state()
                        imageView_fanstate.setImageResource(R.drawable.icons_fanoff)
                    }
                }
            }
        }
        builder.setPositiveButton("ON", listener)
        builder.setNegativeButton("OFF", listener)
        builder.show()
    }
    /* LED 버튼 누를 시 뜨는 팝업창 부분 */
    private fun showSettingPopup_led() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("LED 작동 제어")
        builder.setMessage("원하는 작동을 선택하십시오.")
        builder.setIcon(R.drawable.icons_alarm)

        var listener = object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when (p1) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        cod = "L11" // LED ON
                        Toast.makeText(applicationContext, "LED ON!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        led_state = "Lon"
                        headup_led_state()
                        imageView_ledstate.setImageResource(R.drawable.icons_ledon)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        cod = "L00" // LED OFF
                        Toast.makeText(applicationContext, "LED OFF!", Toast.LENGTH_SHORT).show()
                        var mSendData: SendData? = null
                        mSendData = SendData(cod)
                        mSendData.start()
                        led_state = "Loff"
                        headup_led_state()
                        imageView_ledstate.setImageResource(R.drawable.icons_ledoff)
                    }
                }
            }
        }
        builder.setPositiveButton("ON", listener)
        builder.setNegativeButton("OFF", listener)
        builder.show()
    }
}