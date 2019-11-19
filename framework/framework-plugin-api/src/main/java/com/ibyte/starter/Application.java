package com.ibyte.starter;

import com.ibyte.common.util.StringHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@SpringBootApplication
public class Application {

    public static void start(String[] args) {
        long t = System.currentTimeMillis();
        SpringApplication.run(Application.class, args);
        t = (System.currentTimeMillis() - t) / 1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(StringHelper.join(new Object[]{"系统启动成功：", sdf.format(new Date()), "，共耗时：", t, "秒"}));
    }

    public static void main(String[] args) {
        start(args);
    }
}
