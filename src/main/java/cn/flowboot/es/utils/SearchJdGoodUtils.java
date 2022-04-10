package cn.flowboot.es.utils;

import cn.flowboot.es.doc.Good;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
@Slf4j
@Component
public class SearchJdGoodUtils {

    public static void main(String[] args) throws IOException {
        Integer text = getTotalNums();

        System.out.println(text);
    }

    private static Integer getTotalNums() throws IOException {
        String url = "https://search.jd.com/Search?keyword=java";
        //解析网页
        Document document = Jsoup.parse(new URL(url), 30000);
        String text = document.getElementById("J_topPage").getElementsByTag("i").eq(0).text();
        return Integer.parseInt(text);
    }

    /**
     * 获取书籍
     * @param keyword 查询关键字
     * @return
     * @throws Exception
     */
    public Set<Good> getGoodAll(String keyword) throws Exception {
        log.info("开始捕获关于：{} 数据",keyword);
        Set<Good> goods = new HashSet<>();
//        for (int s = 2; s <= 5; s++) {
//
//        }
        //getTotalNums()
        for (int i = 5; i < 8; i++) {
            Random random = new Random();
            Thread.sleep(random.nextInt(5000)+36000);
            Set<Good> list = this.getGoodByPage(keyword, i,0);
            log.info("{} 捕获 {} 条",keyword,list.size());
            goods.addAll(list);

        }
        return goods;
    }

    private Set<Good> getGoodByPage(String keyword, int page, int sort) throws IOException, InterruptedException {
        String url = "https://search.jd.com/Search?keyword="+keyword+"&page="+page;//+"&psort="+sort;
        //解析网页
        Element element = null;
        int i = 0;
        do{
            Document document = Jsoup.parse(new URL(url), 30000);
            element = document.getElementById("J_goodsList");
            if (i >= 4){

                break;
            }else if (i > 0){
                log.error("{} 重新尝试获取",keyword);
                Thread.sleep(new Random().nextInt(5000)+22000);
            }
            i++;
        } while (element == null);

        if (element == null){
            log.error("获取失败");
            Thread.sleep(5000);
            return new HashSet<>();
        }
        //获取li标签
        Elements elements = element.getElementsByTag("li");
        Set<Good> goods = new HashSet<>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            Elements elementsByClass = el.getElementsByClass("p-price");
            String price = null;
            if (elementsByClass.size() != 0) {
                price = elementsByClass.get(0).getElementsByTag("i").eq(0).text();
            }
            if (price == null){
                continue;
            }
            String title = el.getElementsByClass("p-name").eq(0).text();

            Good good = Good.builder()
                    .title(title)
                    .imgUrl(img)
                    .price(Double.parseDouble(price.trim()))
                    .build();
            goods.add(good);
        }
        return goods;
    }

}
