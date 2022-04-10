package cn.flowboot.es.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/25
 */
public class JsoupUtils {

    // 代理隧道验证信息
    final static String ProxyUser = "H01234567890123D";
    final static String ProxyPass = "0123456789012345";

    // 代理服务器
    final static String ProxyHost = "http-dyn.abuyun.com";
    final static Integer ProxyPort = 9020;
    public static Document getDocument(String url)
    {
        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(ProxyUser, ProxyPass.toCharArray());
            }
        });

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ProxyHost, ProxyPort));

        try {
            // 此处自己处理异常、其他参数等
            Document doc = Jsoup.connect(url)
                    .followRedirects(false)
                    .timeout(3000)
                    .proxy(proxy)
                    // 忽略证书校验
                    .validateTLSCertificates(false)
                    .get();

           return doc;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
