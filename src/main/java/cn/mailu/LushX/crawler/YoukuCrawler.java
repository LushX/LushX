package cn.mailu.LushX.crawler;

import cn.mailu.LushX.entity.Video;
import cn.mailu.LushX.util.JsoupUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Drohe
 * @Description:优酷视频爬取
 * @Date:Created in 13:15 2017/11/6
 * @Modified By:
 */
public class YoukuCrawler {

    private static Logger logger= LoggerFactory.getLogger(YoukuCrawler.class);

   // private final static String YK_HOT_VIDEO_URL="http://list.youku.com/category/show/c_91.html";

    private final static String YK_HOT_VIDEO_URL="http://hotels.ctrip.com/hotel/441351.html";


    public void start(){

        Document ykHot= JsoupUtils.getDocWithPC(YK_HOT_VIDEO_URL);

        getYKVideosFromPcDocument(ykHot);

    }

    private List<Video> getYKVideosFromPcDocument(Document document) {

        List<Video> videos = new ArrayList<>();

        Elements videoElement=document.select("li.yk-col4");

        //Element typeElement=document.select("div.item.border ul li.current span").first();
        Element typeElement=document.select("span.score").first();

        String str=typeElement.toString();

        for(Element element:videoElement){

            Video video=new Video();
         String quickPicture =element.select("div.p-thumb img").get(0).attr("src");
         String title=element.select("div.p-thumb a").get(0).attr("title");
         String playUrl=element.select("div.p-thumb a").get(0).attr("href");
         String type= typeElement.text();
         video.setTitle(title);
         video.setImage(quickPicture);
         video.setPlayUrl(playUrl);
         video.setType(type);
         videos.add(video);
        }
        return null;
    }

    public static void main(String[] args){

        YoukuCrawler youkuCrawler=new YoukuCrawler();

        youkuCrawler.start();

    }


}
