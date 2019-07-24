package smsdemo.youdu.im;

import com.google.gson.Gson;
import im.youdu.sdk.client.AppClient;
import im.youdu.sdk.entity.SmsBody;
import im.youdu.sdk.entity.YDApp;
import im.youdu.sdk.exception.GeneralEntAppException;
import smsdemo.youdu.im.Action.Crypto;
import smsdemo.youdu.im.Untity.EncryptBody;
import smsdemo.youdu.im.Untity.SmsResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;

public class SmsServlet extends HttpServlet {
    private int buin;
    private String appId;
    private String srvHost;
    private String encodingAESKey;
    private YDApp ydApp;
    private AppClient appClient;

    @Override
    public void init() throws ServletException {
        Properties properties = new Properties();
        try {
            String cfgPath = this.getServletContext().getRealPath("/WEB-INF/classes/SmsDemo.properties");
            System.out.println("配置文件SmsDemo,prpperties路径:"+cfgPath);
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = new BufferedInputStream (new FileInputStream(cfgPath));
            properties.load(in);
            buin=Integer.valueOf(properties.getProperty("buin"));
            appId=properties.getProperty("appId");
            srvHost=properties.getProperty("srvHost");
            encodingAESKey=properties.getProperty("encodingAESKey");
            ydApp = new YDApp(buin, srvHost, "", appId, "", encodingAESKey);
            appClient=new AppClient(ydApp);
        }catch (FileNotFoundException e){
            System.out.println(e);
        }catch (IOException e){
            System.out.println(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            Crypto crypto = new Crypto(appClient);
            //获取request的数据流
            String encryptRequest = crypto.getInput(request);
            //把请求数据转换为json对象
            Object inputData = new Gson().fromJson(encryptRequest, EncryptBody.class);
            String encryptMsg = ((EncryptBody) inputData).getEncrypt();
            //解密收到的数据
            SmsBody body = crypto.decrypt(encryptMsg);
            System.out.println("获取到的body是:" + body);


            //TODO:在这里将短信发给短信平台
            //crypto.fromUser() 发送者
            //body.getMobileList(); 接收者
            //body.getContent(); 短信内容

            //加密
            String respond = createRespond(body);
            String encryptRespond = crypto.encrypt(respond);
            System.out.println("加密要返回给发送者的数据src:"+ encryptRespond);

            //返回数据
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print(encryptRespond);
            out.close();
            response.flushBuffer();

            //TODO:模拟发一条上行短信
            //if (crypto.size != 0) {
            //    appClient.sendSmsMsg(crypto.fromUser,"","131123060000","来自手机的短信");
            //}
        } catch (IOException e) {
            System.out.println(e);
        }
        //} catch (GeneralEntAppException e) {
        //    System.out.println(e);
        //}

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write("getaction is blank");
    }

    //    加密要返回给发送者的数据
    public String createRespond(SmsBody body) {
        String resJson = "";
        SmsResponse smsResponse = new SmsResponse();
        try {
            int size = body.getMobileList().size();
            String[] success = new String[size];
            //String[] fail = new String[size];
            for (int i = 0; i < body.getMobileList().size(); i++) {
                //System.out.println("第" + i + "个接收人是:" + body.getMobileList().get(i));
                success[i] = body.getMobileList().get(i);
            }
//            for (int i = 0; i < body.getMobileList().size(); i++) {
//                fail[i] = body.getMobileList().get(i);
//            }

            smsResponse.setSuccess(success);
            //smsResponse.setFail(fail);
            resJson = new Gson().toJson(smsResponse);
            System.out.println("返回的json字符串:" + resJson);
        }catch (Exception e){
            System.out.println(e);
        }
        return  resJson;
    }

}
