package com.java.health.care.bed.test;

import com.java.health.care.bed.base.IBaseView;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/08/19 13:20
 * @Description
 */
public class Contracts {

    public interface IHomeSquareModel {
        /**
         * 加载广场首页数据
         */
        Observable<List<Article>> loadHomeSquareData(int pageNum);

        /**
         * 刷新广场数据
         */
        Observable<List<Article>> refreshHomeSquareData(int pageNum);
    }

    public interface IHomeSquareView extends IBaseView {
        void loadHomeSquareData(List<Article> homeSquareData);

        void refreshHomeSquareData(List<Article> homeSquareData);
    }

    public interface IHomeSquarePresenter {
        void loadHomeSquareData(int pageNum);

        void refreshHomeSquareData(int pageNum);
    }
}
