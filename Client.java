package com.bjzs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsBatchSend;
import com.yunpian.sdk.model.SmsSingleSend;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


public class Client {

    public static void main(String[] args) {
        System.out.println("版本1.2执行开始。。。");
        prepare();
        System.out.println("准备结束。。。");
        String result = run();
    }


    public void sendCode(String mobile,String code){
        System.out.println("sendCode开始"+mobile+code);
        String apikey = "c122219681c9d923d2169b0a249654dd6f0f";
        String template = "【飞鱼 APP】您的验证码为：#code#，感谢您使用飞鱼APP，如非本人操作请忽略";
        String content = template.replace("#code#",code);

        YunpianClient clnt = new YunpianClient(apikey).init();
        Map<String, String> param = clnt.newParam(2);
        param.put(YunpianClient.MOBILE, mobile);
        param.put(YunpianClient.TEXT,content);
        //Result<SmsSingleSend> r = clnt.sms().single_send(param);
        Result<SmsBatchSend> br = clnt.sms().batch_send(param);
        System.out.println("SmsService sendCode result:"+br.getCode()+" detail:"+br.getDetail());
        //LOGGER.info("SmsService sendCode result:{}",r.getCode());
        clnt.close();
    }


    public static void prepare() {
        // 设置代理地址和端口
        String proxyHost = "127.0.0.1";
        String proxyPort = "8118";   //1080

        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);

        // 对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
    }


    public static String getData(String urls,StringBuilder alarm){
        try {
            URL url = new URL(urls);
            URLConnection connection = url.openConnection();
            //设置User-Agent,因为服务器的安全设置不接受Java程序作为客户端访问,否则报异常Server returned HTTP response code: 403
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            System.out.println("getData 方法开始,开始连接");
            connection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while((line=br.readLine())!=null){
                System.out.println(line);
                // 接收返回信息
                JSONObject jsonObj = JSON.parseObject(line);
                String date = TimeStamp2Date(jsonObj.get("date").toString(),"");
                System.out.println("时间:"+date);
                JSONObject obj = (JSONObject)jsonObj.get("ticker");
                if(Double.parseDouble(obj.get("last").toString())>1){
                    System.out.println("last:"+obj.get("last"));
                    alarm.append("last:"+obj.get("last"));

                }
//                System.out.println("vol:"+obj.get("vol"));
//                System.out.println("last:"+obj.get("last"));
//                System.out.println("low:"+obj.get("low"));
//                System.out.println("buy:"+obj.get("buy"));
//                System.out.println("sell:"+obj.get("sell"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return alarm.toString();
    }


    /**
     * Java将Unix时间戳转换成指定格式日期字符串
     * @param timestampString 时间戳 如："1473048265";
     * @param formats 要格式化的格式 默认："yyyy-MM-dd HH:mm:ss";
     *
     * @return 返回结果 如："2016-09-05 16:06:42";
     */
    public static String TimeStamp2Date(String timestampString, String formats) {
        if (TextUtils.isEmpty(formats))
            formats = "yyyy-MM-dd HH:mm:ss";
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
        return date;
    }


    public static String run() {
        String uri = "https://www.okex.com";
        String task = "/api/v1/ticker.do";
        String params = "?symbol=okb_usdt";
        String url = uri+task+params;
        StringBuilder alarm = new StringBuilder();
        System.out.println("run 方法开始");
        long hourtime = 60*60*1000;
        long after =0;
        boolean flag = true;
        while(true){
            try {
                long now = new Date().getTime();
                if(flag){
                    String result = getData(url,alarm);
                    if(StringUtils.isNotBlank(result)) {
                        new Client().sendCode("1312212,1222", result);
                        System.out.println("sendCode成功"+result);
                        after = now+hourtime;
                        flag=false;
                    }
                }
                if(after<now){
                    flag = true;
                }
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
