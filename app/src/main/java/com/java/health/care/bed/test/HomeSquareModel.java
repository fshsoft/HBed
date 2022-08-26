package com.java.health.care.bed.test;

import android.annotation.SuppressLint;
import com.java.health.care.bed.base.BaseModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class HomeSquareModel extends BaseModel implements Contracts.IHomeSquareModel {
    public HomeSquareModel() {
        retrofitManager();
    }

    @Override
    public Observable<List<Article>> loadHomeSquareData(int pageNum) {

        Observable<List<Article>> loadFromNet = loadHomeSquareDataFromNet(pageNum);

        return loadFromNet;
    }

    @SuppressLint("NewApi")
    private Observable<List<Article>> loadHomeSquareDataFromNet(int pageNum) {
        return mApiServer.loadHomeSquareData(pageNum).filter(squareData ->
                squareData.getErrorCode() == 0)
                .map(squareData -> {

                    List<Article> squareArticleList = new ArrayList<>();
                    squareData.getData().getDatas().stream().forEach(datasBean -> {
                        Article article = new Article();
                        article.articleId = datasBean.getId();
                        article.title = datasBean.getTitle();
                        article.author = datasBean.getAuthor();
                        article.chapterName = datasBean.getChapterName();
                        article.superChapterName = datasBean.getSuperChapterName();
                        article.time = datasBean.getPublishTime();
                        article.link = datasBean.getLink();
                        article.collect = datasBean.isCollect();
                        article.niceDate = datasBean.getNiceDate();
                        article.shareUser = datasBean.getShareUser();
                        article.isFresh = datasBean.isFresh();
                        squareArticleList.add(article);

                    });

                    return squareArticleList;
                });
    }

    @Override
    public Observable<List<Article>> refreshHomeSquareData(int pageNum) {
        return loadHomeSquareDataFromNet(pageNum);
    }


}
