package com.github.Atgsasakazh5.my_ec_site.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;
    private final String backendUrl;

    @Autowired
    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.frontend.baseUrl}") String frontendBaseUrl,
            @Value("${app.backend.baseUrl}") String backendUrl
    ) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.backendUrl = backendUrl;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("メールアドレスのご本人様確認");

        // 注入したベースURLと、フロントエンドのパスを組み合わせる
        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + token;
        String text = String.format(
                "この度は、my-ec-siteにご登録いただき、誠にありがとうございます。\n\n" +
                        "以下のリンクをクリックして、メールアドレスの認証を完了してください。\n" +
                        "もしリンクをクリックできない場合は、URLをコピーしてブラウザのアドレスバーに貼り付けてください。\n\n" +
                        "▼認証用リンク\n" +
                        "%s\n\n" +
                        "※このリンクの有効期限は、発行から24時間です。\n" +
                        "有効期限を過ぎた場合は、お手数ですが再度ご登録手続きをお願いいたします。\n\n" +
                        "--------------------------------------------------\n" +
                        "【重要】このメールにお心当たりがない場合\n" +
                        "第三者が誤ってあなたのメールアドレスを登録した可能性があります。\n" +
                        "その場合は、大変お手数ですが本メールを破棄していただきますようお願いいたします。\n" +
                        "--------------------------------------------------\n\n" +
                        "※本メールは送信専用です。ご返信いただいてもお答えできませんのでご了承ください。\n\n" +
                        "--------------------\n" +
                        "my-ec-site運営事務局\n" +
                        "公式サイト: %s\n" +
                        "--------------------",
                verificationUrl, backendUrl
        );

        message.setText(text);
        mailSender.send(message);
    }
}
