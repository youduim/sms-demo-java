package smsdemo.youdu.im.Action;

import com.google.gson.Gson;
import im.youdu.sdk.client.AppClient;
import im.youdu.sdk.encrypt.AESCrypto;
import im.youdu.sdk.entity.ReceiveMessage;
import im.youdu.sdk.entity.SmsBody;
import im.youdu.sdk.exception.AESCryptoException;
import im.youdu.sdk.exception.GeneralEntAppException;
import im.youdu.sdk.exception.ParamParserException;
import im.youdu.sdk.util.Helper;
import smsdemo.youdu.im.Untity.BackResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Crypto {
    public int size=0;
    public String fromUser="";
    private AppClient appClient;
    private AESCrypto crypto;
    public Crypto(){

    }
    public Crypto(AppClient appClient){
        this.appClient=appClient;
        crypto=new AESCrypto(appClient.getAppId(), appClient.getAppAeskey());
    }

    public String getInput(HttpServletRequest request){
        //读取用户发送的request请求中的数据
        String resStr = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            resStr = sb.toString();
        }catch (IOException e) {
            System.out.println(e);
        }
        return resStr;
    }

    //接收客户端传来的加密数据并解析
    public SmsBody decrypt( String encryptMsg){
        ReceiveMessage receiveMessage;
        SmsBody body=null;
        try {
            receiveMessage = appClient.decrypt(encryptMsg);
            body = receiveMessage.getAsSmsMsg();
            fromUser=receiveMessage.getFromUser();
            System.out.println("发送人:"+fromUser);
            size=body.getMobileList().size();
        }catch (IOException e){
            System.out.println(e);
        }catch (ParamParserException e){
            System.out.println(e);
        }catch (GeneralEntAppException e){
            System.out.println(e);
        }
        return body;
    }

//    加密要返回给发送者的数据
    public String encrypt(String json){
        String src="";
        BackResponse backResponse = new BackResponse();
        try {
            System.out.println("发送的json字符串:"+json);
            String cipherText = crypto.encrypt(Helper.utf8Bytes(json));
            System.out.println("加密后的数据" + cipherText);

            backResponse.setErrcode(0);
            backResponse.setErrmsg("ok");
            backResponse.setEncrypt(cipherText);
            src = new Gson().toJson(backResponse);
        }catch (ParamParserException e){
            System.out.println(e);
        }catch (AESCryptoException e){
            System.out.println(e);
        }
        return src;
    }
}
