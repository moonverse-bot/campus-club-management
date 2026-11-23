package org.example.campusclubmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class CampusClubManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusClubManagementApplication.class, args);
    }

    /**
     * 启动成功后自动打开浏览器（访问前端页面）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:5173/";
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                // Windows 备用方案
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            }
            System.out.println("\n✅ 已自动打开浏览器：" + url);
        } catch (Exception e) {
            System.out.println("\n⚠️ 无法自动打开浏览器，请手动访问：" + url);
        }
    }
}
